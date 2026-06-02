package bjut.baseball;

import com.sun.net.httpserver.HttpExchange;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Auth {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, String> SESSIONS = new ConcurrentHashMap<>();

    private Auth() {
    }

    public static boolean loggedIn(HttpExchange exchange) {
        String token = RequestUtil.cookie(exchange, "BJUTSESSION");
        return !token.isBlank() && SESSIONS.containsKey(token);
    }

    public static void login(HttpExchange exchange, String username) {
        String token = randomToken();
        SESSIONS.put(token, username);
        exchange.getResponseHeaders().add("Set-Cookie", "BJUTSESSION=" + token + "; Path=/; HttpOnly; SameSite=Lax");
    }

    public static void logout(HttpExchange exchange) {
        String token = RequestUtil.cookie(exchange, "BJUTSESSION");
        if (!token.isBlank()) {
            SESSIONS.remove(token);
        }
        exchange.getResponseHeaders().add("Set-Cookie", "BJUTSESSION=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
    }

    private static String randomToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) {
            out.append(String.format("%02x", b));
        }
        return out.toString();
    }
}
