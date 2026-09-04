package com.ric.apolite;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ApoApiClient {
    public interface Callback {
        void onSuccess(String body);
        void onError(int code, String message);
    }

    public static final String BASE_URL = "https://apo-apps-gateway.alfagift.id/";
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile String bearerToken;

    public void setBearerToken(String token) {
        bearerToken = token == null ? null : token.trim();
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void login(String nik, String pin, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("nik", nik);
            body.put("pin", pin);
            request("POST", "v1/auth/login", body.toString(), false, callback);
        } catch (Exception e) {
            callback.onError(-1, e.getMessage() == null ? "JSON error" : e.getMessage());
        }
    }

    public void activeShipments(Callback callback) {
        request("GET", "v1/receipt-revamp/apo-online-active-shipment", null, true, callback);
    }

    private void request(String method, String path, String json, boolean auth, Callback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + path);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                if (auth && bearerToken != null && !bearerToken.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
                }
                if (json != null) {
                    conn.setDoOutput(true);
                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                    conn.setFixedLengthStreamingMode(bytes.length);
                    try (OutputStream out = conn.getOutputStream()) {
                        out.write(bytes);
                    }
                }

                int code = conn.getResponseCode();
                InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
                String body = read(stream);
                if (code >= 200 && code < 300) {
                    main.post(() -> callback.onSuccess(body));
                } else {
                    main.post(() -> callback.onError(code, body));
                }
            } catch (Exception e) {
                String msg = e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "network error" : e.getMessage());
                main.post(() -> callback.onError(-1, msg));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }, "apo-lite-http").start();
    }

    private static String read(InputStream in) throws Exception {
        if (in == null) return "";
        StringBuilder b = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) b.append(line);
        }
        return b.toString();
    }

    public static String findToken(String body) {
        try {
            Object root = body.trim().startsWith("[") ? new JSONArray(body) : new JSONObject(body);
            return findTokenValue(root);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String findTokenValue(Object node) throws Exception {
        if (node instanceof JSONObject) {
            JSONObject o = (JSONObject) node;
            String[] keys = {"accessToken", "access_token", "token"};
            for (String key : keys) {
                if (o.has(key) && !o.isNull(key)) {
                    String v = o.optString(key, "");
                    if (!v.isEmpty()) return v;
                }
            }
            JSONArray names = o.names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    Object child = o.opt(names.getString(i));
                    String found = findTokenValue(child);
                    if (found != null) return found;
                }
            }
        } else if (node instanceof JSONArray) {
            JSONArray a = (JSONArray) node;
            for (int i = 0; i < a.length(); i++) {
                String found = findTokenValue(a.opt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
}
