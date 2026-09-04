package com.ric.apolite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String APO_PACKAGE = "com.alfamart.apo";
    private LinearLayout root;
    private final ApoApiClient api = new ApoApiClient();
    private final List<OrderItem> orders = new ArrayList<>();
    private String orderStatus = "NEW";
    private OrderItem selectedOrder;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        showLogin();
    }

    private TextView text(String s, float sp) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(Color.rgb(25,25,25));
        v.setPadding(0, 12, 0, 12);
        return v;
    }

    private Button button(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setOnClickListener(l);
        b.setMinHeight(96);
        return b;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setPadding(20, 18, 20, 18);
        return e;
    }

    private void screen(String name) {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 40);
        TextView title = text(name, 24);
        title.setGravity(Gravity.START);
        root.addView(title);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void showLogin() {
        screen("APO Lite");
        root.addView(text("Login APO: NIK • PIN • Google Authenticator", 14));

        EditText user = input("NIK");
        EditText pass = input("PIN");
        EditText otp = input("Kode Google Authenticator 6 digit");
        pass.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        otp.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        root.addView(user);
        root.addView(pass);
        root.addView(otp);

        TextView status = text("Masukkan kode 6 digit yang sedang tampil di Google Authenticator.", 12);
        root.addView(status);

        final Button[] loginRef = new Button[1];
        loginRef[0] = button("Masuk", v -> {
            String nik = sanitizeNik(user.getText().toString());
            String pin = pass.getText().toString().trim();
            String otpCode = otp.getText().toString().trim();

            String validation = validateLogin(nik, pin, otpCode);
            if (validation != null) {
                status.setText(validation);
                return;
            }

            boolean gpsActive = isGpsActive();
            loginRef[0].setEnabled(false);
            status.setText("Memvalidasi NIK, PIN, OTP, dan status GPS...");

            api.login(nik, pin, otpCode, false, gpsActive, new ApoApiClient.Callback() {
                @Override public void onSuccess(String body) {
                    loginRef[0].setEnabled(true);
                    String token = ApoApiClient.findToken(body);
                    if (token == null || token.isEmpty()) {
                        String event = ApoApiClient.findErrorEvent(body);
                        String msg = ApoApiClient.findErrorMessage(body);
                        status.setText(formatAuthMessage(200, event, msg, "Respons login tidak berisi token sesi APO."));
                        return;
                    }
                    api.setBearerToken(token);
                    pass.setText("");
                    otp.setText("");
                    Toast.makeText(MainActivity.this, "Login APO berhasil", Toast.LENGTH_SHORT).show();
                    loadOrders();
                }

                @Override public void onError(int code, String message) {
                    loginRef[0].setEnabled(true);
                    String event = ApoApiClient.findErrorEvent(message);
                    String msg = ApoApiClient.findErrorMessage(message);
                    status.setText(formatAuthMessage(code, event, msg, compact(message)));
                }
            });
        });

        root.addView(loginRef[0]);
        root.addView(button("Buka Google Authenticator", v -> openGoogleAuthenticator()));
        root.addView(button("Buka APO Asli", v -> openOriginalApo("login / produksi")));
        root.addView(text("APO Lite tidak menyimpan PIN, OTP, QR, atau seed Authenticator. Token sesi hanya berada di memori proses aplikasi.", 12));
    }

    private static String sanitizeNik(String value) {
        StringBuilder out = new StringBuilder();
        String source = value == null ? "" : value;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (Character.isLetterOrDigit(c)) out.append(c);
        }
        return out.toString().toUpperCase(Locale.ROOT);
    }

    private static String validateLogin(String nik, String pin, String otp) {
        if (nik.isEmpty() && pin.isEmpty()) return "Masukkan NIK dan PIN Anda.";
        if (nik.length() < 8 || !nik.matches("^[a-zA-Z0-9]+$")) return "NIK minimal 8 karakter dan hanya huruf/angka.";
        if (pin.length() < 6) return "PIN minimal 6 digit.";
        if (otp.length() < 6) return "Masukkan kode Google Authenticator 6 digit.";
        if (!otp.matches("^[0-9]+$")) return "Kode Authenticator harus berupa angka.";
        return null;
    }

    private boolean isGpsActive() {
        try {
            Object service = getSystemService(LOCATION_SERVICE);
            LocationManager lm = service instanceof LocationManager ? (LocationManager) service : null;
            boolean provider = lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF;
            return provider && mode;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String formatAuthMessage(int code, String event, String message, String fallback) {
        String e = event == null ? "" : event.trim();
        String m = message == null ? "" : message.trim();
        if ("AUTH_SSO_OTP_NOT_VALID".equals(e)) return "Kode Google Authenticator tidak valid. Gunakan kode 6 digit terbaru.";
        if ("AUTH_SSO_OTP_TOO_LONG".equals(e)) return "Kode Google Authenticator terlalu panjang.";
        if ("AUTH_SSO_OTP_MAX_NOT_VALID".equals(e)) return "Batas percobaan OTP tercapai. Ikuti instruksi APO sebelum mencoba kembali.";
        if ("AUTH_SSO_PIN_NOT_VALID".equals(e)) return "PIN tidak valid.";
        if ("AUTH_SSO_PIN_BLOCKED".equals(e)) return "PIN sedang diblokir oleh server APO.";
        if ("AUTH_SSO_PIN_EXPIRED".equals(e)) return "PIN telah kedaluwarsa.";
        if ("AUTH_SSO_NIK_NOT_VALID".equals(e) || "AUTH_NOT_FOUND".equals(e)) return "NIK tidak valid atau akun tidak ditemukan.";
        if ("AUTH_SSO_NIK_BLOCKED".equals(e)) return "NIK sedang diblokir oleh server APO.";
        if ("AUTH_SSO_NOT_MATCH".equals(e)) return "NIK, PIN, atau OTP tidak cocok.";
        if ("GPS_NOT_ACTIVE".equals(e)) return "GPS harus aktif untuk login APO.";
        if ("GPLAY_VALIDATION_FAILED".equals(e)) return "Validasi Google Play perangkat gagal. Gunakan APO asli jika perangkat memerlukan validasi tambahan.";
        if ("SESSION_EXIST".equals(e)) return m.isEmpty() ? "Masih ada sesi APO aktif." : m;
        if (!m.isEmpty()) return m;
        return "Login gagal (" + code + "). " + (fallback == null ? "" : fallback);
    }

    private void openGoogleAuthenticator() {
        String[] packages = {
                "com.google.android.apps.authenticator2",
                "com.google.android.apps.authenticator"
        };
        for (String pkg : packages) {
            try {
                Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
                if (launch != null) {
                    startActivity(launch);
                    return;
                }
            } catch (Exception ignored) { }
        }
        Toast.makeText(this, "Google Authenticator tidak ditemukan. Buka aplikasi Authenticator secara manual.", Toast.LENGTH_LONG).show();
    }

    private void loadOrders() {
        screen("Pesanan");
        TextView status = text("Memuat pesanan aktif...", 14);
        root.addView(status);
        api.activeShipments(new ApoApiClient.Callback() {
            @Override public void onSuccess(String body) {
                orders.clear();
                collectOrders(body, orders);
                showOrders();
            }

            @Override public void onError(int code, String message) {
                status.setText("Gagal memuat pesanan Lite (" + code + "). " + compact(message));
                root.addView(button("Coba lagi", v -> loadOrders()));
                root.addView(button("Buka Pesanan di APO Asli", v -> openOriginalApo("pesanan")));
                root.addView(button("Keluar", v -> { api.setBearerToken(null); showLogin(); }));
            }
        });
    }

    private void showOrders() {
        screen("Pesanan");
        root.addView(text("Workflow: Packing → Siap Kirim → Konfirmasi", 14));
        if (orders.isEmpty()) {
            root.addView(text("Tidak ada shipment aktif yang terbaca dari respons server.", 14));
        } else {
            for (OrderItem item : orders) {
                root.addView(button(item.label(), v -> {
                    selectedOrder = item;
                    orderStatus = item.status == null || item.status.isEmpty() ? "ACTIVE" : item.status;
                    showOrderDetail();
                }));
            }
        }
        root.addView(button("Refresh", v -> loadOrders()));
        root.addView(button("Chat Teks", v -> showChat()));
        root.addView(button("Buka APO Asli", v -> openOriginalApo("workflow produksi")));
        root.addView(button("Keluar", v -> { api.setBearerToken(null); orders.clear(); showLogin(); }));
    }

    private void showOrderDetail() {
        screen("Detail Pesanan");
        String no = selectedOrder == null ? "-" : selectedOrder.number;
        root.addView(text("Order / Shipment: " + no, 18));
        root.addView(text("Status: " + orderStatus, 15));
        if (selectedOrder != null && selectedOrder.customer != null && !selectedOrder.customer.isEmpty()) {
            root.addView(text("Customer: " + selectedOrder.customer, 14));
        }
        root.addView(text("Perubahan status produksi diteruskan ke APO asli agar autentikasi, lokasi, integritas, dan validasi server tetap berlaku.", 12));
        root.addView(button("Packing", v -> openOriginalApo("Packing " + no)));
        root.addView(button("Siap Kirim", v -> openOriginalApo("Siap Kirim " + no)));
        root.addView(button("Konfirmasi", v -> showConfirmation()));
        root.addView(button("Chat Teks", v -> showChat()));
        root.addView(button("Kembali", v -> showOrders()));
    }

    private void showConfirmation() {
        screen("Konfirmasi");
        String no = selectedOrder == null ? "-" : selectedOrder.number;
        root.addView(text("Shipment: " + no + "\nStatus saat ini: " + orderStatus, 15));
        root.addView(text("Konfirmasi produksi dilakukan oleh APO asli. APO Lite tidak menonaktifkan atau melewati validasi lokasi/integritas/server.", 14));
        root.addView(button("Lanjut Konfirmasi di APO", v -> openOriginalApo("Konfirmasi " + no)));
        root.addView(button("Kembali", v -> showOrderDetail()));
    }

    private void showChat() {
        screen("Chat Teks");
        String no = selectedOrder == null ? "-" : selectedOrder.number;
        root.addView(text("Shipment: " + no, 13));
        TextView log = text("Mode chat Lite hanya teks.\n\n", 15);
        root.addView(log);
        EditText message = input("Tulis pesan...");
        root.addView(message);
        root.addView(button("Kirim", v -> {
            String m = message.getText().toString().trim();
            if (m.isEmpty()) return;
            log.setText(log.getText() + "Anda: " + m + "\n");
            message.setText("");
            Toast.makeText(this, "Kontrak kirim chat belum terverifikasi; pesan belum dikirim ke server.", Toast.LENGTH_SHORT).show();
        }));
        root.addView(button("Buka Chat Produksi di APO", v -> openOriginalApo("chat " + no)));
        root.addView(text("Tanpa telepon, video, voice note, atau attachment di APO Lite.", 12));
        root.addView(button("Kembali", v -> showOrders()));
    }

    private void openOriginalApo(String action) {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(APO_PACKAGE);
            if (launch == null) {
                Toast.makeText(this, "APO asli belum terpasang. Install com.alfamart.apo terlebih dahulu.", Toast.LENGTH_LONG).show();
                return;
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(launch);
            Toast.makeText(this, "Dibuka di APO: " + action, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Tidak dapat membuka APO asli: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String compact(String message) {
        if (message == null) return "";
        String s = message.replace('\n', ' ').replace('\r', ' ').trim();
        return s.length() > 180 ? s.substring(0, 180) + "…" : s;
    }

    private static void collectOrders(String body, List<OrderItem> out) {
        try {
            Object parsed = body.trim().startsWith("[") ? new JSONArray(body) : new JSONObject(body);
            walk(parsed, out);
        } catch (Exception ignored) { }
    }

    private static void walk(Object node, List<OrderItem> out) throws Exception {
        if (node instanceof JSONObject) {
            JSONObject o = (JSONObject) node;
            String number = first(o, "shipmentNumber", "shipmentNo", "shipment_number", "orderNumber", "orderNo");
            if (!number.isEmpty()) {
                String status = first(o, "status", "shipmentStatus", "orderStatus", "state");
                String customer = first(o, "customerName", "customer", "receiverName", "recipientName");
                boolean duplicate = false;
                for (OrderItem x : out) if (number.equals(x.number)) { duplicate = true; break; }
                if (!duplicate) out.add(new OrderItem(number, status, customer));
            }
            JSONArray names = o.names();
            if (names != null) for (int i = 0; i < names.length(); i++) walk(o.opt(names.getString(i)), out);
        } else if (node instanceof JSONArray) {
            JSONArray a = (JSONArray) node;
            for (int i = 0; i < a.length(); i++) walk(a.opt(i), out);
        }
    }

    private static String first(JSONObject o, String... keys) {
        for (String key : keys) {
            Object value = o.opt(key);
            if (value != null && value != JSONObject.NULL && !(value instanceof JSONObject) && !(value instanceof JSONArray)) {
                String s = String.valueOf(value).trim();
                if (!s.isEmpty()) return s;
            }
        }
        return "";
    }

    private static final class OrderItem {
        final String number;
        final String status;
        final String customer;
        OrderItem(String number, String status, String customer) {
            this.number = number;
            this.status = status;
            this.customer = customer;
        }
        String label() {
            String s = number;
            if (status != null && !status.isEmpty()) s += "   •   " + status;
            return s;
        }
    }

    @Override public void onBackPressed() {
        if (api.getBearerToken() == null) showLogin(); else showOrders();
    }
}
