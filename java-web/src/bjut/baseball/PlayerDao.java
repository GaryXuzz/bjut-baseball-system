package bjut.baseball;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class PlayerDao {
    public int countPlayers() throws SQLException {
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM players");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public List<Player> listPlayers() throws SQLException {
        String sql = """
                SELECT id, name, jersey_number, primary_position, is_pitcher, join_date
                FROM players
                ORDER BY CAST(NULLIF(jersey_number, '') AS UNSIGNED), name
                """;
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Player> players = new ArrayList<>();
            while (rs.next()) {
                players.add(map(rs));
            }
            return players;
        }
    }

    public Player findPlayer(int id) throws SQLException {
        String sql = """
                SELECT id, name, jersey_number, primary_position, is_pitcher, join_date
                FROM players
                WHERE id = ?
                """;
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int createPlayer(String name, String jerseyNumber, String primaryPosition, boolean pitcher) throws SQLException {
        String sql = """
                INSERT INTO players (name, jersey_number, primary_position, is_pitcher, join_date)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection c = Db.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, jerseyNumber);
                ps.setString(3, primaryPosition);
                ps.setBoolean(4, pitcher);
                ps.setDate(5, Date.valueOf(LocalDate.now()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    int id = keys.getInt(1);
                    createDefaultProfiles(c, id, pitcher);
                    assignPrimaryPosition(c, id, primaryPosition, pitcher);
                    c.commit();
                    return id;
                }
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void updatePlayer(int id, String name, String jerseyNumber, String primaryPosition, boolean pitcher) throws SQLException {
        String sql = """
                UPDATE players
                SET name = ?, jersey_number = ?, primary_position = ?, is_pitcher = ?
                WHERE id = ?
                """;
        try (Connection c = Db.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, jerseyNumber);
                ps.setString(3, primaryPosition);
                ps.setBoolean(4, pitcher);
                ps.setInt(5, id);
                ps.executeUpdate();
                createDefaultProfiles(c, id, pitcher);
                assignPrimaryPosition(c, id, primaryPosition, pitcher);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void deletePlayer(int id) throws SQLException {
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM players WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void createDefaultProfiles(Connection c, int playerId, boolean pitcher) throws SQLException {
        try (PreparedStatement fielder = c.prepareStatement("""
                INSERT IGNORE INTO fielder_profiles (player_id) VALUES (?)
                """)) {
            fielder.setInt(1, playerId);
            fielder.executeUpdate();
        }
        if (pitcher) {
            try (PreparedStatement pitcherProfile = c.prepareStatement("""
                    INSERT IGNORE INTO pitcher_profiles (player_id) VALUES (?)
                    """)) {
                pitcherProfile.setInt(1, playerId);
                pitcherProfile.executeUpdate();
            }
        }
    }

    private void assignPrimaryPosition(Connection c, int playerId, String primaryPosition, boolean pitcher) throws SQLException {
        String position = pitcher ? "投手" : primaryPosition;
        int positionId = ensurePosition(c, position == null || position.isBlank() ? "未指定" : position);
        try (PreparedStatement delete = c.prepareStatement("DELETE FROM player_positions WHERE player_id = ?")) {
            delete.setInt(1, playerId);
            delete.executeUpdate();
        }
        try (PreparedStatement insert = c.prepareStatement("""
                INSERT INTO player_positions (player_id, position_id) VALUES (?, ?)
                """)) {
            insert.setInt(1, playerId);
            insert.setInt(2, positionId);
            insert.executeUpdate();
        }
    }

    private int ensurePosition(Connection c, String name) throws SQLException {
        try (PreparedStatement select = c.prepareStatement("SELECT id FROM positions WHERE name = ?")) {
            select.setString(1, name);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement insert = c.prepareStatement(
                "INSERT INTO positions (name, description) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, name);
            insert.setString(2, "Created from Java Web form");
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private Player map(ResultSet rs) throws SQLException {
        Player p = new Player();
        p.id = rs.getInt("id");
        p.name = rs.getString("name");
        p.jerseyNumber = rs.getString("jersey_number");
        p.primaryPosition = rs.getString("primary_position");
        p.pitcher = rs.getBoolean("is_pitcher");
        Date date = rs.getDate("join_date");
        p.joinDate = date == null ? null : date.toLocalDate();
        return p;
    }
}
