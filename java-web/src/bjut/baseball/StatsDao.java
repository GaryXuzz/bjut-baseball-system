package bjut.baseball;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class StatsDao {
    public List<BattingStat> battingLeaderboard() throws SQLException {
        String sql = """
                SELECT p.name, p.jersey_number, fp.at_bats_total, fp.hits_total, fp.runs_total,
                       fp.rbi_total, fp.batting_average, fp.ops
                FROM fielder_profiles fp
                JOIN players p ON p.id = fp.player_id
                ORDER BY fp.batting_average DESC, fp.hits_total DESC
                LIMIT 20
                """;
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<BattingStat> rows = new ArrayList<>();
            while (rs.next()) {
                BattingStat row = new BattingStat();
                row.playerName = rs.getString("name");
                row.jerseyNumber = rs.getString("jersey_number");
                row.atBats = rs.getInt("at_bats_total");
                row.hits = rs.getInt("hits_total");
                row.runs = rs.getInt("runs_total");
                row.rbi = rs.getInt("rbi_total");
                row.battingAverage = rs.getBigDecimal("batting_average");
                row.ops = rs.getBigDecimal("ops");
                rows.add(row);
            }
            return rows;
        }
    }

    public List<PitchingStat> pitchingLeaderboard() throws SQLException {
        String sql = """
                SELECT p.name, p.jersey_number, pp.innings_pitched_total, pp.strikeouts_total,
                       pp.era, pp.whip
                FROM pitcher_profiles pp
                JOIN players p ON p.id = pp.player_id
                WHERE pp.innings_pitched_total > 0
                   OR pp.strikeouts_total > 0
                ORDER BY pp.era ASC, pp.innings_pitched_total DESC, pp.strikeouts_total DESC
                LIMIT 20
                """;
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PitchingStat> rows = new ArrayList<>();
            while (rs.next()) {
                PitchingStat row = new PitchingStat();
                row.playerName = rs.getString("name");
                row.jerseyNumber = rs.getString("jersey_number");
                row.innings = rs.getBigDecimal("innings_pitched_total");
                row.strikeouts = rs.getInt("strikeouts_total");
                row.era = rs.getBigDecimal("era");
                row.whip = rs.getBigDecimal("whip");
                rows.add(row);
            }
            return rows;
        }
    }

    public List<String> opponents() throws SQLException {
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement("SELECT DISTINCT opponent FROM game_records ORDER BY opponent");
             ResultSet rs = ps.executeQuery()) {
            List<String> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(rs.getString(1));
            }
            return rows;
        }
    }

    public List<MatchupRow> matchupRows(String opponent, int playerId) throws SQLException {
        String sql = """
                SELECT gr.id, p.name, gr.game_date, gr.opponent, gr.is_pitching_record, gr.hits, gr.rbi,
                       gr.strikeouts, gr.innings_pitched, gr.strikeouts_pitched, gr.earned_runs
                FROM game_records gr
                JOIN players p ON p.id = gr.player_id
                WHERE (? = '' OR gr.opponent = ?)
                  AND (? = 0 OR gr.player_id = ?)
                ORDER BY gr.game_date DESC, p.name
                LIMIT 100
                """;
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, opponent == null ? "" : opponent);
            ps.setString(2, opponent == null ? "" : opponent);
            ps.setInt(3, playerId);
            ps.setInt(4, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<MatchupRow> rows = new ArrayList<>();
                while (rs.next()) {
                    MatchupRow row = new MatchupRow();
                    row.id = rs.getInt("id");
                    row.playerName = rs.getString("name");
                    row.gameDate = rs.getDate("game_date").toLocalDate();
                    row.opponent = rs.getString("opponent");
                    row.pitchingRecord = rs.getBoolean("is_pitching_record");
                    row.hits = rs.getInt("hits");
                    row.rbi = rs.getInt("rbi");
                    row.strikeouts = rs.getInt("strikeouts");
                    row.innings = rs.getBigDecimal("innings_pitched");
                    row.strikeoutsPitched = rs.getInt("strikeouts_pitched");
                    row.earnedRuns = rs.getInt("earned_runs");
                    rows.add(row);
                }
                return rows;
            }
        }
    }
}
