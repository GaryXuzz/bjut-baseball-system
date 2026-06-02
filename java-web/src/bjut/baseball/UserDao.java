package bjut.baseball;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class UserDao {
    public boolean isAdminLogin(String username, String password) throws SQLException {
        String demoUser = Db.get("auth.demo.username", "admin");
        String demoPassword = Db.get("auth.demo.password", "admin123");
        if (demoUser.equals(username) && demoPassword.equals(password)) {
            return true;
        }

        String sql = "SELECT password_hash, is_admin FROM users WHERE username = ?";
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean("is_admin")) {
                    return false;
                }
                String hash = rs.getString("password_hash");
                return hash != null && hash.equals(password);
            }
        }
    }
}
