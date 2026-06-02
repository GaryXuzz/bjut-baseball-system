package bjut.baseball;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public final class Main {
    private static final PlayerDao PLAYER_DAO = new PlayerDao();
    private static final GameRecordDao RECORD_DAO = new GameRecordDao();
    private static final StatsDao STATS_DAO = new StatsDao();
    private static final ExportDao EXPORT_DAO = new ExportDao();
    private static final UserDao USER_DAO = new UserDao();

    public static void main(String[] args) throws IOException {
        int port = Db.getInt("server.port", 8080);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", Main::route);
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("BJUT Baseball Java Web running at http://localhost:" + port + "/");
    }

    private static void route(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if ("GET".equals(method) && "/".equals(path)) home(exchange);
            else if ("GET".equals(method) && "/login".equals(path)) loginPage(exchange, "");
            else if ("POST".equals(method) && "/login".equals(path)) loginSubmit(exchange);
            else if ("POST".equals(method) && "/logout".equals(path)) logout(exchange);
            else if ("GET".equals(method) && "/players".equals(path)) players(exchange);
            else if ("GET".equals(method) && "/players/new".equals(path)) requireLogin(exchange, () -> playerForm(exchange, null));
            else if ("POST".equals(method) && "/players/create".equals(path)) requireLogin(exchange, () -> createPlayer(exchange));
            else if ("GET".equals(method) && "/players/edit".equals(path)) requireLogin(exchange, () -> editPlayerForm(exchange));
            else if ("POST".equals(method) && "/players/update".equals(path)) requireLogin(exchange, () -> updatePlayer(exchange));
            else if ("POST".equals(method) && "/players/delete".equals(path)) requireLogin(exchange, () -> deletePlayer(exchange));
            else if ("GET".equals(method) && "/records/new".equals(path)) requireLogin(exchange, () -> recordForm(exchange));
            else if ("POST".equals(method) && "/records/create".equals(path)) requireLogin(exchange, () -> createRecord(exchange));
            else if ("POST".equals(method) && "/records/delete".equals(path)) requireLogin(exchange, () -> deleteRecord(exchange));
            else if ("GET".equals(method) && "/stats".equals(path)) stats(exchange);
            else if ("GET".equals(method) && "/matchups".equals(path)) matchups(exchange);
            else if ("GET".equals(method) && "/schema".equals(path)) schema(exchange);
            else if ("GET".equals(method) && "/export/players.csv".equals(path)) exportPlayers(exchange);
            else if ("GET".equals(method) && "/export/game-records.csv".equals(path)) exportGameRecords(exchange);
            else send(exchange, 404, Html.page("Not Found", Auth.loggedIn(exchange), Html.error("Page not found: " + path)));
        } catch (SQLException ex) {
            send(exchange, 500, Html.page("Database Error", Auth.loggedIn(exchange),
                    Html.error("Database operation failed. Check MySQL is running and config.properties is correct.<br><pre>" + Html.esc(ex.getMessage()) + "</pre>")));
        } catch (Exception ex) {
            send(exchange, 500, Html.page("Server Error", Auth.loggedIn(exchange), Html.error(ex.getMessage())));
        } finally {
            exchange.close();
        }
    }

    private static void home(HttpExchange exchange) throws IOException, SQLException {
        String body = "<section class=\"panel\"><h1>BJUT Baseball Team Management System</h1>"
                + "<p class=\"muted\">Java HttpServer + JDBC + MySQL version for COMP2004J Phase 2.</p></section>"
                + "<section class=\"grid\">"
                + statCard("Players", PLAYER_DAO.countPlayers(), "/players")
                + statCard("Game Records", RECORD_DAO.countGameRecords(), "/stats")
                + statCard("MySQL Schema", 7, "/schema")
                + "</section>"
                + "<section class=\"panel\"><h2>Core Functions</h2><p>"
                + "<a class=\"button\" href=\"/players\">View Players</a> "
                + "<a class=\"button\" href=\"/records/new\">Add Game Record</a> "
                + "<a class=\"button\" href=\"/stats\">View Statistics</a> "
                + "<a class=\"button\" href=\"/matchups\">Matchup Query</a> "
                + "<a class=\"button\" href=\"/export/players.csv\">Export Players CSV</a> "
                + "<a class=\"button\" href=\"/export/game-records.csv\">Export Records CSV</a></p></section>";
        send(exchange, 200, Html.page("Home", Auth.loggedIn(exchange), body));
    }

    private static String statCard(String label, int value, String href) {
        return "<a class=\"panel\" href=\"" + href + "\"><div class=\"muted\">" + label + "</div>"
                + "<div class=\"stat\">" + value + "</div></a>";
    }

    private static void loginPage(HttpExchange exchange, String message) throws IOException {
        String body = "<section class=\"panel\"><h1>Administrator Login</h1>"
                + (message.isBlank() ? "" : "<p class=\"danger panel\">" + Html.esc(message) + "</p>")
                + "<form method=\"post\" action=\"/login\" class=\"form-grid\">"
                + "<div><label>Username</label><input name=\"username\" required value=\"admin\"></div>"
                + "<div><label>Password</label><input name=\"password\" type=\"password\" required value=\"admin123\"></div>"
                + "<div><label>&nbsp;</label><button class=\"red\">Login</button></div>"
                + "</form></section>";
        send(exchange, 200, Html.page("Login", false, body));
    }

    private static void loginSubmit(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> form = RequestUtil.formParams(exchange);
        String username = RequestUtil.first(form, "username", "");
        String password = RequestUtil.first(form, "password", "");
        if (USER_DAO.isAdminLogin(username, password)) {
            Auth.login(exchange, username);
            redirect(exchange, "/");
        } else {
            loginPage(exchange, "Invalid username or password.");
        }
    }

    private static void logout(HttpExchange exchange) throws IOException {
        Auth.logout(exchange);
        redirect(exchange, "/");
    }

    private static void players(HttpExchange exchange) throws IOException, SQLException {
        boolean loggedIn = Auth.loggedIn(exchange);
        StringBuilder body = new StringBuilder("<section class=\"panel\"><h1>Players</h1>");
        if (loggedIn) {
            body.append("<p><a class=\"button green\" href=\"/players/new\">Add Player</a></p>");
        } else {
            body.append("<p class=\"muted\">Login as administrator to add, edit, or delete players.</p>");
        }
        body.append("</section><table><tr><th>ID</th><th>Name</th><th>Jersey</th><th>Primary Position</th><th>Type</th><th>Join Date</th><th>Actions</th></tr>");
        for (Player p : PLAYER_DAO.listPlayers()) {
            body.append("<tr><td>").append(p.id).append("</td><td>").append(Html.esc(p.name)).append("</td><td>")
                    .append(Html.esc(p.jerseyNumber)).append("</td><td>").append(Html.esc(p.primaryPosition)).append("</td><td>")
                    .append(p.pitcher ? "Pitcher" : "Fielder").append("</td><td>").append(Html.esc(p.joinDate)).append("</td><td>");
            if (loggedIn) {
                body.append("<div class=\"actions\"><a class=\"button\" href=\"/players/edit?id=").append(p.id).append("\">Edit</a>")
                        .append("<form class=\"inline\" method=\"post\" action=\"/players/delete\" onsubmit=\"return confirm('Delete this player?')\">")
                        .append("<input type=\"hidden\" name=\"id\" value=\"").append(p.id).append("\"><button class=\"red\">Delete</button></form></div>");
            } else {
                body.append("<span class=\"muted\">Admin only</span>");
            }
            body.append("</td></tr>");
        }
        body.append("</table>");
        send(exchange, 200, Html.page("Players", loggedIn, body.toString()));
    }

    private static void playerForm(HttpExchange exchange, Player p) throws IOException {
        boolean edit = p != null;
        String body = "<section class=\"panel\"><h1>" + (edit ? "Edit Player" : "Add Player") + "</h1>"
                + "<form method=\"post\" action=\"" + (edit ? "/players/update" : "/players/create") + "\" class=\"form-grid\">"
                + (edit ? "<input type=\"hidden\" name=\"id\" value=\"" + p.id + "\">" : "")
                + "<div><label>Name</label><input name=\"name\" required value=\"" + Html.esc(edit ? p.name : "") + "\"></div>"
                + "<div><label>Jersey Number</label><input name=\"jersey_number\" required value=\"" + Html.esc(edit ? p.jerseyNumber : "") + "\"></div>"
                + "<div><label>Primary Position</label><select name=\"primary_position\">"
                + positionOptions(edit ? p.primaryPosition : "捕手")
                + "</select></div>"
                + "<div><label>Player Type</label><select name=\"is_pitcher\">"
                + "<option value=\"false\"" + (edit && !p.pitcher ? " selected" : "") + ">Fielder / Batter</option>"
                + "<option value=\"true\"" + (edit && p.pitcher ? " selected" : "") + ">Pitcher</option>"
                + "</select></div>"
                + "<div><label>&nbsp;</label><button class=\"green\">" + (edit ? "Save Changes" : "Create Player") + "</button></div>"
                + "</form></section>";
        send(exchange, 200, Html.page(edit ? "Edit Player" : "Add Player", true, body));
    }

    private static void editPlayerForm(HttpExchange exchange) throws IOException, SQLException {
        int id = RequestUtil.intValue(RequestUtil.queryParams(exchange), "id", 0);
        Player p = PLAYER_DAO.findPlayer(id);
        if (p == null) {
            send(exchange, 404, Html.page("Player Not Found", true, Html.error("Player does not exist.")));
            return;
        }
        playerForm(exchange, p);
    }

    private static void createPlayer(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> form = RequestUtil.formParams(exchange);
        PLAYER_DAO.createPlayer(
                RequestUtil.first(form, "name", ""),
                RequestUtil.first(form, "jersey_number", ""),
                RequestUtil.first(form, "primary_position", "未指定"),
                Boolean.parseBoolean(RequestUtil.first(form, "is_pitcher", "false"))
        );
        redirect(exchange, "/players");
    }

    private static void updatePlayer(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> form = RequestUtil.formParams(exchange);
        PLAYER_DAO.updatePlayer(
                RequestUtil.intValue(form, "id", 0),
                RequestUtil.first(form, "name", ""),
                RequestUtil.first(form, "jersey_number", ""),
                RequestUtil.first(form, "primary_position", "未指定"),
                Boolean.parseBoolean(RequestUtil.first(form, "is_pitcher", "false"))
        );
        redirect(exchange, "/players");
    }

    private static void deletePlayer(HttpExchange exchange) throws IOException, SQLException {
        PLAYER_DAO.deletePlayer(RequestUtil.intValue(RequestUtil.formParams(exchange), "id", 0));
        redirect(exchange, "/players");
    }

    private static void recordForm(HttpExchange exchange) throws IOException, SQLException {
        StringBuilder options = new StringBuilder();
        for (Player p : PLAYER_DAO.listPlayers()) {
            options.append("<option value=\"").append(p.id).append("\">#").append(Html.esc(p.jerseyNumber))
                    .append(" ").append(Html.esc(p.name)).append("</option>");
        }
        String body = "<section class=\"panel\"><h1>Add Game Record</h1>"
                + "<form method=\"post\" action=\"/records/create\" class=\"form-grid\">"
                + "<div><label>Player</label><select name=\"player_id\">" + options + "</select></div>"
                + "<div><label>Date</label><input type=\"date\" name=\"game_date\" value=\"" + LocalDate.now() + "\"></div>"
                + "<div><label>Opponent</label><input name=\"opponent\" required value=\"Demo University\"></div>"
                + "<div><label>Record Type</label><select name=\"record_type\"><option value=\"batting\">Batting</option><option value=\"pitching\">Pitching</option></select></div>"
                + "<div><label>At Bats</label><input name=\"at_bats\" type=\"number\" min=\"0\" value=\"4\"></div>"
                + "<div><label>Hits</label><input name=\"hits\" type=\"number\" min=\"0\" value=\"2\"></div>"
                + "<div><label>Runs</label><input name=\"runs\" type=\"number\" min=\"0\" value=\"1\"></div>"
                + "<div><label>RBI</label><input name=\"rbi\" type=\"number\" min=\"0\" value=\"1\"></div>"
                + "<div><label>Walks</label><input name=\"walks\" type=\"number\" min=\"0\" value=\"0\"></div>"
                + "<div><label>Strikeouts Batting</label><input name=\"strikeouts\" type=\"number\" min=\"0\" value=\"1\"></div>"
                + "<div><label>Doubles</label><input name=\"doubles\" type=\"number\" min=\"0\" value=\"1\"></div>"
                + "<div><label>Innings Pitched</label><input name=\"innings\" type=\"number\" step=\"0.1\" min=\"0\" value=\"5.0\"></div>"
                + "<div><label>Hits Allowed</label><input name=\"hits_allowed\" type=\"number\" min=\"0\" value=\"3\"></div>"
                + "<div><label>Runs Allowed</label><input name=\"runs_allowed\" type=\"number\" min=\"0\" value=\"1\"></div>"
                + "<div><label>Earned Runs</label><input name=\"earned_runs\" type=\"number\" min=\"0\" value=\"1\"></div>"
                + "<div><label>Walks Allowed</label><input name=\"walks_allowed\" type=\"number\" min=\"0\" value=\"1\"></div>"
                + "<div><label>Strikeouts Pitched</label><input name=\"strikeouts_pitched\" type=\"number\" min=\"0\" value=\"6\"></div>"
                + "<div><label>Pitches</label><input name=\"pitches\" type=\"number\" min=\"0\" value=\"80\"></div>"
                + "<div><label>Strikes</label><input name=\"strikes\" type=\"number\" min=\"0\" value=\"52\"></div>"
                + "<div><label>&nbsp;</label><button class=\"green\">Create Record</button></div>"
                + "</form></section>";
        send(exchange, 200, Html.page("Add Game Record", true, body));
    }

    private static void createRecord(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> form = RequestUtil.formParams(exchange);
        int playerId = RequestUtil.intValue(form, "player_id", 0);
        LocalDate date = LocalDate.parse(RequestUtil.first(form, "game_date", LocalDate.now().toString()));
        String opponent = RequestUtil.first(form, "opponent", "");
        if ("pitching".equals(RequestUtil.first(form, "record_type", "batting"))) {
            RECORD_DAO.createPitchingRecord(
                    playerId, date, opponent,
                    new BigDecimal(RequestUtil.first(form, "innings", "0")),
                    RequestUtil.intValue(form, "hits_allowed", 0),
                    RequestUtil.intValue(form, "runs_allowed", 0),
                    RequestUtil.intValue(form, "earned_runs", 0),
                    RequestUtil.intValue(form, "walks_allowed", 0),
                    RequestUtil.intValue(form, "strikeouts_pitched", 0),
                    RequestUtil.intValue(form, "pitches", 0),
                    RequestUtil.intValue(form, "strikes", 0)
            );
        } else {
            RECORD_DAO.createBattingRecord(
                    playerId, date, opponent,
                    RequestUtil.intValue(form, "at_bats", 0),
                    RequestUtil.intValue(form, "hits", 0),
                    RequestUtil.intValue(form, "runs", 0),
                    RequestUtil.intValue(form, "rbi", 0),
                    RequestUtil.intValue(form, "walks", 0),
                    RequestUtil.intValue(form, "strikeouts", 0),
                    RequestUtil.intValue(form, "doubles", 0)
            );
        }
        redirect(exchange, "/stats");
    }

    private static void deleteRecord(HttpExchange exchange) throws IOException, SQLException {
        RECORD_DAO.deleteRecord(RequestUtil.intValue(RequestUtil.formParams(exchange), "id", 0));
        redirect(exchange, "/matchups");
    }

    private static void stats(HttpExchange exchange) throws IOException, SQLException {
        List<BattingStat> batting = STATS_DAO.battingLeaderboard();
        List<PitchingStat> pitching = STATS_DAO.pitchingLeaderboard();
        StringBuilder body = new StringBuilder("<section class=\"panel\"><h1>Statistics</h1><p class=\"muted\">Leaderboards are read from MySQL profile tables.</p></section>");
        body.append("<section class=\"grid charts-grid\">")
                .append(battingChart(batting))
                .append(pitchingChart(pitching))
                .append("</section>");
        body.append("<section class=\"panel\"><h2>Batting Leaderboard</h2><table><tr><th>Player</th><th>AB</th><th>H</th><th>R</th><th>RBI</th><th>AVG</th><th>OPS</th></tr>");
        for (BattingStat s : batting) {
            body.append("<tr><td>#").append(Html.esc(s.jerseyNumber)).append(" ").append(Html.esc(s.playerName)).append("</td><td>")
                    .append(s.atBats).append("</td><td>").append(s.hits).append("</td><td>").append(s.runs).append("</td><td>")
                    .append(s.rbi).append("</td><td>").append(Html.decimal(s.battingAverage, 3)).append("</td><td>").append(Html.decimal(s.ops, 3)).append("</td></tr>");
        }
        body.append("</table></section><section class=\"panel\"><h2>Pitching Leaderboard</h2><table><tr><th>Player</th><th>IP</th><th>SO</th><th>ERA</th><th>WHIP</th></tr>");
        for (PitchingStat s : pitching) {
            body.append("<tr><td>#").append(Html.esc(s.jerseyNumber)).append(" ").append(Html.esc(s.playerName)).append("</td><td>")
                    .append(Html.money(s.innings)).append("</td><td>").append(s.strikeouts).append("</td><td>")
                    .append(Html.decimal(s.era, 2)).append("</td><td>").append(Html.decimal(s.whip, 2)).append("</td></tr>");
        }
        body.append("</table></section>");
        send(exchange, 200, Html.page("Statistics", Auth.loggedIn(exchange), body.toString()));
    }

    private static String battingChart(List<BattingStat> rows) {
        StringBuilder out = new StringBuilder("<section class=\"panel chart-card\"><h2>Top Batting AVG</h2><div class=\"chart-list\">");
        int limit = Math.min(rows.size(), 8);
        for (int i = 0; i < limit; i++) {
            BattingStat stat = rows.get(i);
            int width = percent(stat.battingAverage, BigDecimal.ONE);
            out.append(chartRow("#" + Html.esc(stat.jerseyNumber) + " " + Html.esc(stat.playerName),
                    Html.decimal(stat.battingAverage, 3), width, "bar-red"));
        }
        if (limit == 0) {
            out.append("<p class=\"muted\">No batting data available.</p>");
        }
        return out.append("</div></section>").toString();
    }

    private static String pitchingChart(List<PitchingStat> rows) {
        StringBuilder out = new StringBuilder("<section class=\"panel chart-card\"><h2>Pitching Strikeouts</h2><div class=\"chart-list\">");
        int limit = Math.min(rows.size(), 8);
        int maxStrikeouts = 1;
        for (int i = 0; i < limit; i++) {
            maxStrikeouts = Math.max(maxStrikeouts, rows.get(i).strikeouts);
        }
        for (int i = 0; i < limit; i++) {
            PitchingStat stat = rows.get(i);
            int width = Math.max(4, Math.min(100, (int) Math.round((stat.strikeouts * 100.0) / maxStrikeouts)));
            out.append(chartRow("#" + Html.esc(stat.jerseyNumber) + " " + Html.esc(stat.playerName),
                    stat.strikeouts + " SO", width, "bar-green"));
        }
        if (limit == 0) {
            out.append("<p class=\"muted\">No pitching data available.</p>");
        }
        return out.append("</div></section>").toString();
    }

    private static String chartRow(String label, String value, int width, String className) {
        return "<div class=\"chart-row\"><div class=\"chart-meta\"><span>" + label + "</span><strong>" + value + "</strong></div>"
                + "<div class=\"bar-track\"><span class=\"bar-fill " + className + "\" style=\"width:" + width + "%\"></span></div></div>";
    }

    private static int percent(BigDecimal value, BigDecimal max) {
        if (value == null || max == null || BigDecimal.ZERO.compareTo(max) == 0) {
            return 4;
        }
        return Math.max(4, Math.min(100, value.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue()));
    }

    private static void matchups(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> query = RequestUtil.queryParams(exchange);
        String selected = RequestUtil.first(query, "opponent", "");
        int selectedPlayer = RequestUtil.intValue(query, "player_id", 0);
        boolean loggedIn = Auth.loggedIn(exchange);
        StringBuilder select = new StringBuilder("<option value=\"\">All opponents</option>");
        for (String opponent : STATS_DAO.opponents()) {
            select.append("<option value=\"").append(Html.esc(opponent)).append("\"").append(Html.selected(selected, opponent)).append(">")
                    .append(Html.esc(opponent)).append("</option>");
        }
        StringBuilder playerSelect = new StringBuilder("<option value=\"0\">All players</option>");
        for (Player player : PLAYER_DAO.listPlayers()) {
            String id = String.valueOf(player.id);
            playerSelect.append("<option value=\"").append(id).append("\"").append(Html.selected(String.valueOf(selectedPlayer), id)).append(">")
                    .append("#").append(Html.esc(player.jerseyNumber)).append(" ").append(Html.esc(player.name)).append("</option>");
        }
        StringBuilder body = new StringBuilder("<section class=\"panel\"><h1>Matchup Query</h1><form method=\"get\" class=\"form-grid\">"
                + "<div><label>Opponent</label><select name=\"opponent\">" + select + "</select></div>"
                + "<div><label>Player</label><select name=\"player_id\">" + playerSelect + "</select></div>"
                + "<div><label>&nbsp;</label><button>Search</button></div></form></section>");
        body.append("<table><tr><th>Date</th><th>Opponent</th><th>Player</th><th>Type</th><th>Performance</th>")
                .append(loggedIn ? "<th>Actions</th>" : "")
                .append("</tr>");
        for (MatchupRow row : STATS_DAO.matchupRows(selected, selectedPlayer)) {
            String performance = row.pitchingRecord
                    ? "IP " + Html.money(row.innings) + ", SO " + row.strikeoutsPitched + ", ER " + row.earnedRuns
                    : "H " + row.hits + ", RBI " + row.rbi + ", SO " + row.strikeouts;
            body.append("<tr><td>").append(row.gameDate).append("</td><td>").append(Html.esc(row.opponent)).append("</td><td>")
                    .append(Html.esc(row.playerName)).append("</td><td>").append(row.pitchingRecord ? "Pitching" : "Batting").append("</td><td>")
                    .append(performance).append("</td>");
            if (loggedIn) {
                body.append("<td><form class=\"inline\" method=\"post\" action=\"/records/delete\" onsubmit=\"return confirm('Delete this game record?')\">")
                        .append("<input type=\"hidden\" name=\"id\" value=\"").append(row.id).append("\">")
                        .append("<button class=\"red\">Delete</button></form></td>");
            }
            body.append("</tr>");
        }
        body.append("</table>");
        send(exchange, 200, Html.page("Matchups", loggedIn, body.toString()));
    }

    private static void exportPlayers(HttpExchange exchange) throws IOException, SQLException {
        sendCsv(exchange, "players.csv", EXPORT_DAO.playersCsv());
    }

    private static void exportGameRecords(HttpExchange exchange) throws IOException, SQLException {
        sendCsv(exchange, "game_records.csv", EXPORT_DAO.gameRecordsCsv());
    }

    private static void schema(HttpExchange exchange) throws IOException {
        String body = "<section class=\"panel\"><h1>MySQL Schema</h1>"
                + "<p>The Java Web version uses <code>sql/phase2_mysql_schema.sql</code>.</p>"
                + "<table><tr><th>Table</th><th>Purpose</th><th>Key Constraints</th></tr>"
                + row("players", "Player identity and basic data", "PK id, unique name + jersey")
                + row("positions", "Position dictionary", "PK id, unique name")
                + row("player_positions", "Many-to-many player-position relationship", "Composite PK, two FKs")
                + row("game_records", "Per-player per-game batting or pitching records", "FK player_id, checks for hits/at-bats, strikes/pitches")
                + row("fielder_profiles", "Batting and fielding aggregate statistics", "PK/FK player_id, checks for averages and totals")
                + row("pitcher_profiles", "Pitching aggregate statistics", "PK/FK player_id, checks for ERA/WHIP/strike percentage")
                + row("users", "Administrator accounts", "PK id, unique username")
                + "</table></section>";
        send(exchange, 200, Html.page("Schema", Auth.loggedIn(exchange), body));
    }

    private static String row(String a, String b, String c) {
        return "<tr><td><code>" + a + "</code></td><td>" + b + "</td><td>" + c + "</td></tr>";
    }

    private static String positionOptions(String selected) {
        String[] positions = {"投手", "捕手", "一垒手", "二垒手", "三垒手", "游击手", "左外野手", "中外野手", "右外野手", "未指定"};
        StringBuilder out = new StringBuilder();
        for (String position : positions) {
            out.append("<option value=\"").append(Html.esc(position)).append("\"").append(Html.selected(selected, position)).append(">")
                    .append(Html.esc(position)).append("</option>");
        }
        return out.toString();
    }

    private static void requireLogin(HttpExchange exchange, CheckedRunnable action) throws Exception {
        if (!Auth.loggedIn(exchange)) {
            redirect(exchange, "/login");
            return;
        }
        action.run();
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        exchange.sendResponseHeaders(303, -1);
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private static void sendCsv(HttpExchange exchange, String filename, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=utf-8");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
