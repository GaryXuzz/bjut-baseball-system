package bjut.baseball;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public final class Html {
    private Html() {
    }

    public static String page(String title, boolean loggedIn, String body) {
        return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + esc(title) + " - BJUT Baseball</title>"
                + "<style>" + style() + "</style></head><body>"
                + "<header><div><strong>POWER ARENA</strong><span>BJUT Baseball Java Web</span></div>"
                + "<nav>"
                + link("/", "Home")
                + link("/players", "Players")
                + link("/records/new", "Add Record")
                + link("/stats", "Stats")
                + link("/matchups", "Matchups")
                + link("/schema", "Schema")
                + (loggedIn ? "<form method=\"post\" action=\"/logout\"><button>Logout</button></form>" : link("/login", "Admin Login"))
                + "</nav></header><main>" + body + "</main></body></html>";
    }

    public static String error(String message) {
        return "<section class=\"panel danger\"><h1>Error</h1><p>" + esc(message) + "</p>"
                + "<p><a class=\"button\" href=\"/\">Back Home</a></p></section>";
    }

    public static String esc(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String money(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    public static String decimal(BigDecimal value, int scale) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(scale).toPlainString();
        }
        return value.setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

    public static String selected(String current, String value) {
        return value.equals(current) ? " selected" : "";
    }

    public static String checked(boolean value) {
        return value ? " checked" : "";
    }

    public static String hiddenInputs(Map<String, String> values, String... keys) {
        StringBuilder out = new StringBuilder();
        for (String key : keys) {
            out.append("<input type=\"hidden\" name=\"").append(esc(key)).append("\" value=\"")
                    .append(esc(values.getOrDefault(key, ""))).append("\">");
        }
        return out.toString();
    }

    private static String link(String href, String label) {
        return "<a href=\"" + href + "\">" + label + "</a>";
    }

    private static String style() {
        return """
                :root{color-scheme:light;--bg:#f7f8fb;--paper:#ffffff;--panel:#ffffff;--panel2:#f9fafb;--muted:#64748b;--text:#18212f;--heading:#0f172a;--red:#c62828;--red2:#991b1b;--line:#d9e1ea;--line2:#edf1f5;--green:#15803d;--green2:#166534;--gold:#d97706;--blue:#2563eb}
                *{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font-family:Arial,'PingFang SC','Microsoft YaHei',sans-serif;line-height:1.55}
                body:before{content:"";position:fixed;inset:0 0 auto;height:280px;background:linear-gradient(180deg,#fff 0,#fdf4f4 58%,rgba(247,248,251,0) 100%);z-index:-1}
                header{position:sticky;top:0;z-index:1;background:rgba(255,255,255,.94);border-bottom:1px solid var(--line);padding:16px 32px;display:flex;align-items:center;justify-content:space-between;gap:24px;box-shadow:0 10px 28px rgba(15,23,42,.08);backdrop-filter:blur(12px)}
                header strong{font-size:24px;letter-spacing:.08em;color:var(--red);font-weight:900}header span{display:block;color:var(--muted);font-size:13px;margin-top:3px}
                nav{display:flex;align-items:center;gap:10px;flex-wrap:wrap}nav form{margin:0}nav a,button,.button{border:1px solid var(--line);background:#fff;color:var(--heading);text-decoration:none;padding:9px 13px;border-radius:6px;font-weight:700;cursor:pointer;display:inline-flex;align-items:center;justify-content:center;min-height:38px;box-shadow:0 1px 2px rgba(15,23,42,.04);transition:background .16s ease,border-color .16s ease,color .16s ease,box-shadow .16s ease,transform .16s ease}
                nav a:hover,button:hover,.button:hover{background:#fff5f5;border-color:#efb8b8;color:var(--red);box-shadow:0 6px 18px rgba(198,40,40,.12);transform:translateY(-1px)}main{max-width:1180px;margin:0 auto;padding:32px}
                h1{font-size:30px;line-height:1.2;margin:0 0 16px;color:var(--heading)}h2{font-size:22px;line-height:1.25;margin:0 0 12px;color:var(--heading)}.muted{color:var(--muted)}
                .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:18px}.panel{background:var(--panel);border:1px solid var(--line);border-radius:8px;padding:22px;margin-bottom:22px;box-shadow:0 10px 30px rgba(15,23,42,.06)}
                a.panel{display:block;color:inherit;text-decoration:none}.panel:hover{border-color:#efb8b8;box-shadow:0 14px 36px rgba(198,40,40,.11)}.stat{font-size:36px;font-weight:900;color:var(--red);letter-spacing:.02em}.danger{border-color:#fecaca;background:#fff7f7;color:#7f1d1d}.success{border-color:#bbf7d0;background:#f0fdf4;color:#14532d}
                table{width:100%;border-collapse:separate;border-spacing:0;background:var(--paper);border:1px solid var(--line);border-radius:8px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.05)}th,td{border-bottom:1px solid var(--line2);padding:11px 12px;text-align:left;vertical-align:top}th{background:#f1f5f9;color:#334155;font-size:13px;text-transform:uppercase;letter-spacing:.04em}tr:last-child td{border-bottom:0}tr:hover td{background:#fff8f8}
                input,select{width:100%;padding:10px 11px;border-radius:6px;border:1px solid var(--line);background:#fff;color:var(--text);outline:none;transition:border-color .16s ease,box-shadow .16s ease}.form-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:14px}
                input:focus,select:focus{border-color:#ef9a9a;box-shadow:0 0 0 3px rgba(198,40,40,.12)}label{display:block;font-weight:800;color:#334155;margin-bottom:6px}.actions{display:flex;gap:8px;align-items:center;flex-wrap:wrap}.inline{display:inline}.red{background:var(--red);border-color:var(--red);color:#fff}.red:hover{background:var(--red2);border-color:var(--red2);color:#fff}.green{background:var(--green);border-color:var(--green);color:#fff}.green:hover{background:var(--green2);border-color:var(--green2);color:#fff}
                code,pre{background:#f8fafc;border:1px solid var(--line);border-radius:6px;padding:2px 5px;color:#334155}pre{padding:14px;overflow:auto}
                .charts-grid{grid-template-columns:repeat(auto-fit,minmax(320px,1fr))}.chart-card{min-width:0}.chart-list{display:grid;gap:12px;margin-top:16px}
                .chart-row{display:grid;gap:7px}.chart-meta{display:flex;justify-content:space-between;gap:12px;font-size:14px}.chart-meta span{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.chart-meta strong{color:var(--heading)}
                .bar-track{height:13px;background:#eef2f7;border:1px solid var(--line);border-radius:999px;overflow:hidden}.bar-fill{display:block;height:100%;border-radius:999px}.bar-red{background:linear-gradient(90deg,var(--red),var(--gold))}.bar-green{background:linear-gradient(90deg,var(--green),#0ea5e9)}
                @media (max-width:760px){
                  header{position:static;align-items:flex-start;padding:16px;flex-direction:column;gap:14px}
                  header strong{font-size:21px}nav{width:100%;overflow-x:auto;flex-wrap:nowrap;padding-bottom:4px}nav a,nav button{white-space:nowrap;font-size:13px;padding:8px 10px}
                  main{padding:16px}h1{font-size:24px}h2{font-size:19px}.panel{padding:16px}.grid,.charts-grid{grid-template-columns:1fr}
                  table{display:block;overflow-x:auto;white-space:nowrap}th,td{padding:9px 10px}.stat{font-size:30px}
                }
                """;
    }
}
