package bjut.baseball;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public final class GameRecordDao {
    public int countGameRecords() throws SQLException {
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM game_records");
             var rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public void createBattingRecord(int playerId, LocalDate gameDate, String opponent, int atBats, int hits,
                                    int runs, int rbi, int walks, int strikeouts, int doubles) throws SQLException {
        String sql = """
                INSERT INTO game_records
                (player_id, game_date, opponent, is_pitching_record, at_bats, hits, runs, rbi, walks, strikeouts, doubles, total_bases)
                VALUES (?, ?, ?, FALSE, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        int singles = Math.max(hits - doubles, 0);
        int totalBases = singles + doubles * 2;
        try (Connection c = Db.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, playerId);
                ps.setDate(2, Date.valueOf(gameDate));
                ps.setString(3, opponent);
                ps.setInt(4, atBats);
                ps.setInt(5, hits);
                ps.setInt(6, runs);
                ps.setInt(7, rbi);
                ps.setInt(8, walks);
                ps.setInt(9, strikeouts);
                ps.setInt(10, doubles);
                ps.setInt(11, totalBases);
                ps.executeUpdate();
                recalculateFielderProfile(c, playerId);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void createPitchingRecord(int playerId, LocalDate gameDate, String opponent, BigDecimal innings,
                                     int hitsAllowed, int runsAllowed, int earnedRuns, int walksAllowed,
                                     int strikeoutsPitched, int pitches, int strikes) throws SQLException {
        String sql = """
                INSERT INTO game_records
                (player_id, game_date, opponent, is_pitching_record, innings_pitched, hits_allowed, runs_allowed,
                 earned_runs, walks_allowed, strikeouts_pitched, pitches, strikes)
                VALUES (?, ?, ?, TRUE, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection c = Db.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, playerId);
                ps.setDate(2, Date.valueOf(gameDate));
                ps.setString(3, opponent);
                ps.setBigDecimal(4, innings);
                ps.setInt(5, hitsAllowed);
                ps.setInt(6, runsAllowed);
                ps.setInt(7, earnedRuns);
                ps.setInt(8, walksAllowed);
                ps.setInt(9, strikeoutsPitched);
                ps.setInt(10, pitches);
                ps.setInt(11, strikes);
                ps.executeUpdate();
                recalculatePitcherProfile(c, playerId);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void deleteRecord(int recordId) throws SQLException {
        try (Connection c = Db.connect()) {
            c.setAutoCommit(false);
            try {
                int playerId;
                boolean pitchingRecord;
                try (PreparedStatement select = c.prepareStatement("""
                        SELECT player_id, is_pitching_record
                        FROM game_records
                        WHERE id = ?
                        FOR UPDATE
                        """)) {
                    select.setInt(1, recordId);
                    try (ResultSet rs = select.executeQuery()) {
                        if (!rs.next()) {
                            c.commit();
                            return;
                        }
                        playerId = rs.getInt("player_id");
                        pitchingRecord = rs.getBoolean("is_pitching_record");
                    }
                }
                try (PreparedStatement delete = c.prepareStatement("DELETE FROM game_records WHERE id = ?")) {
                    delete.setInt(1, recordId);
                    delete.executeUpdate();
                }
                if (pitchingRecord) {
                    clearPitcherProfile(c, playerId);
                    recalculatePitcherProfile(c, playerId);
                } else {
                    clearFielderProfile(c, playerId);
                    recalculateFielderProfile(c, playerId);
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void recalculateFielderProfile(Connection c, int playerId) throws SQLException {
        String sql = """
                UPDATE fielder_profiles fp
                JOIN (
                    SELECT player_id,
                           COALESCE(SUM(at_bats),0) at_bats_total,
                           COALESCE(SUM(runs),0) runs_total,
                           COALESCE(SUM(hits),0) hits_total,
                           COALESCE(SUM(rbi),0) rbi_total,
                           COALESCE(SUM(walks),0) walks_total,
                           COALESCE(SUM(strikeouts),0) strikeouts_total,
                           COALESCE(SUM(doubles),0) doubles,
                           COALESCE(SUM(triples),0) triples,
                           COALESCE(SUM(home_runs_batting),0) home_runs,
                           COALESCE(SUM(total_bases),0) total_bases
                    FROM game_records
                    WHERE player_id = ? AND is_pitching_record = FALSE
                    GROUP BY player_id
                ) s ON fp.player_id = s.player_id
                SET fp.at_bats_total = s.at_bats_total,
                    fp.runs_total = s.runs_total,
                    fp.hits_total = s.hits_total,
                    fp.rbi_total = s.rbi_total,
                    fp.walks_total = s.walks_total,
                    fp.strikeouts_batting_total = s.strikeouts_total,
                    fp.doubles = s.doubles,
                    fp.triples = s.triples,
                    fp.home_runs_batting = s.home_runs,
                    fp.total_bases = s.total_bases,
                    fp.batting_average = IF(s.at_bats_total > 0, ROUND(s.hits_total / s.at_bats_total, 3), 0),
                    fp.slugging_percentage = IF(s.at_bats_total > 0, ROUND(s.total_bases / s.at_bats_total, 3), 0),
                    fp.on_base_percentage = IF(s.at_bats_total + s.walks_total > 0, ROUND((s.hits_total + s.walks_total) / (s.at_bats_total + s.walks_total), 3), 0),
                    fp.ops =
                        IF(s.at_bats_total + s.walks_total > 0, ROUND((s.hits_total + s.walks_total) / (s.at_bats_total + s.walks_total), 3), 0)
                        + IF(s.at_bats_total > 0, ROUND(s.total_bases / s.at_bats_total, 3), 0)
                """;
        try (PreparedStatement insert = c.prepareStatement("INSERT IGNORE INTO fielder_profiles (player_id) VALUES (?)")) {
            insert.setInt(1, playerId);
            insert.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
        }
    }

    private void clearFielderProfile(Connection c, int playerId) throws SQLException {
        String sql = """
                UPDATE fielder_profiles
                SET at_bats_total = 0,
                    runs_total = 0,
                    hits_total = 0,
                    rbi_total = 0,
                    walks_total = 0,
                    strikeouts_batting_total = 0,
                    batting_average = 0,
                    on_base_percentage = 0,
                    slugging_percentage = 0,
                    ops = 0,
                    doubles = 0,
                    triples = 0,
                    home_runs_batting = 0,
                    total_bases = 0,
                    hit_by_pitch = 0,
                    stolen_bases = 0,
                    caught_stealing = 0,
                    sacrifice_flys = 0,
                    sacrifice_hits = 0,
                    errors_fielding = 0,
                    passed_balls = 0
                WHERE player_id = ?
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
        }
    }

    private void recalculatePitcherProfile(Connection c, int playerId) throws SQLException {
        String sql = """
                UPDATE pitcher_profiles pp
                JOIN (
                    SELECT player_id,
                           COALESCE(SUM(innings_pitched),0) innings,
                           COALESCE(SUM(hits_allowed),0) hits_allowed,
                           COALESCE(SUM(runs_allowed),0) runs_allowed,
                           COALESCE(SUM(earned_runs),0) earned_runs,
                           COALESCE(SUM(walks_allowed),0) walks_allowed,
                           COALESCE(SUM(strikeouts_pitched),0) strikeouts,
                           COALESCE(SUM(pitches),0) pitches,
                           COALESCE(SUM(strikes),0) strikes
                    FROM game_records
                    WHERE player_id = ? AND is_pitching_record = TRUE
                    GROUP BY player_id
                ) s ON pp.player_id = s.player_id
                SET pp.innings_pitched_total = s.innings,
                    pp.hits_allowed_total = s.hits_allowed,
                    pp.runs_allowed_total = s.runs_allowed,
                    pp.earned_runs_total = s.earned_runs,
                    pp.walks_allowed_total = s.walks_allowed,
                    pp.strikeouts_total = s.strikeouts,
                    pp.pitches = s.pitches,
                    pp.strikes = s.strikes,
                    pp.era = IF(s.innings > 0, ROUND((s.earned_runs * 9) / s.innings, 2), 0),
                    pp.whip = IF(s.innings > 0, ROUND((s.walks_allowed + s.hits_allowed) / s.innings, 2), 0),
                    pp.strike_percentage = IF(s.pitches > 0, ROUND((s.strikes / s.pitches) * 100, 1), 0)
                """;
        try (PreparedStatement insert = c.prepareStatement("INSERT IGNORE INTO pitcher_profiles (player_id) VALUES (?)")) {
            insert.setInt(1, playerId);
            insert.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
        }
    }

    private void clearPitcherProfile(Connection c, int playerId) throws SQLException {
        String sql = """
                UPDATE pitcher_profiles
                SET innings_pitched_total = 0,
                    hits_allowed_total = 0,
                    runs_allowed_total = 0,
                    earned_runs_total = 0,
                    walks_allowed_total = 0,
                    strikeouts_total = 0,
                    home_runs_allowed_total = 0,
                    pitches = 0,
                    strikes = 0,
                    hit_by_pitch_allowed = 0,
                    batters_faced = 0,
                    era = 0,
                    whip = 0,
                    strike_percentage = 0,
                    wild_pitches = 0
                WHERE player_id = ?
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
        }
    }
}
