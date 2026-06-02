package bjut.baseball;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class ExportDao {
    public String playersCsv() throws SQLException {
        String sql = """
                SELECT p.id,
                       p.name,
                       p.jersey_number,
                       p.primary_position,
                       IF(p.is_pitcher, 'Pitcher', 'Fielder') AS player_type,
                       p.join_date,
                       fp.at_bats_total,
                       fp.hits_total,
                       fp.batting_average,
                       fp.ops,
                       pp.innings_pitched_total,
                       pp.era,
                       pp.whip
                FROM players p
                LEFT JOIN fielder_profiles fp ON fp.player_id = p.id
                LEFT JOIN pitcher_profiles pp ON pp.player_id = p.id
                ORDER BY CAST(NULLIF(p.jersey_number, '') AS UNSIGNED), p.name
                """;
        return csv(sql);
    }

    public String gameRecordsCsv() throws SQLException {
        String sql = """
                SELECT gr.id,
                       gr.game_date,
                       gr.opponent,
                       p.name AS player_name,
                       p.jersey_number,
                       IF(gr.is_pitching_record, 'Pitching', 'Batting') AS record_type,
                       gr.at_bats,
                       gr.hits,
                       gr.runs,
                       gr.rbi,
                       gr.walks,
                       gr.strikeouts,
                       gr.innings_pitched,
                       gr.hits_allowed,
                       gr.earned_runs,
                       gr.walks_allowed,
                       gr.strikeouts_pitched
                FROM game_records gr
                JOIN players p ON p.id = gr.player_id
                ORDER BY gr.game_date DESC, gr.id DESC
                """;
        return csv(sql);
    }

    private String csv(String sql) throws SQLException {
        StringBuilder out = new StringBuilder("\uFEFF");
        try (Connection c = Db.connect();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (i > 1) {
                    out.append(',');
                }
                out.append(escape(meta.getColumnLabel(i)));
            }
            out.append('\n');
            while (rs.next()) {
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    if (i > 1) {
                        out.append(',');
                    }
                    out.append(escape(rs.getString(i)));
                }
                out.append('\n');
            }
        }
        return out.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
