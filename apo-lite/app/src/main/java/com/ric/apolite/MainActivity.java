package com.ric.apolite;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
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

public class MainActivity extends Activity {
    private LinearLayout root;
    private TextView title;
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
        title = text(name, 24);
        title.setGravity(Gravity.START);
        root.addView(title);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void showLogin() {
        screen("APO Lite");
        root.addView(text("Minimal operational client", 14));
        EditText user = input("NIK");
        EditText pass = input("PIN");
        pass.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        root.addView(user);
        root.addView(pass);
        TextView status = text("Siap terhubung ke APO gateway.", 12);
        root.addView(status);
        Button login = button("Masuk", v -> {
            String nik = user.getText().toString().trim();
            String pin = pass.getText().toString().trim();
            if (nik.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Isi NIK dan PIN", Toast.LENGTH_SHORT).show();
                return;
            }
            login.setEnabled(false);
            status.setText("Menghubungkan...");
            api.login(nik, pin, new ApoApiClient.Callback() {
                @Override public void onSuccess(String body) {
                    login.setEnabled(true);
                    String token = ApoApiClient.findToken(body);
                    if (token == null || token.isEmpty()) {
                        status.setText("Login mendapat respons, tetapi token belum dikenali. Kontrak auth perlu dipetakan lagi.");
                        return;
                    }
                    api.setBearerToken(token);
                    Toast.makeText(MainActivity.this, "Login berhasil", Toast.LENGTH_SHORT).show();
                    loadOrders();
                }

                @Override public void onError(int code, String message) {
                    login.setEnabled(true);
                    status.setText("Login gagal (" + code + "). " + compact(message));
                }
            });
        });
        root.addView(login);
        root.addView(text("APO Lite tidak menyimpan PIN. Token hanya berada di memori proses aplikasi pada build ini.", 12));
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
                status.setText("Gagal memuat pesanan (" + code + "). " + compact(message));
                root.addView(button("Coba lagi", v -> loadOrders()));
                root.addView(button("Keluar", v -> { api.setBearerToken(null); showLogin(); }));
            }
        });
    }

    private void showOrders() {
        screen("Pesanan");
        root.addView(text("Packing → Siap Kirim → Konfirmasi", 14));
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
        root.addView(button("Chat", v -> showChat()));
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
        root.addView(button("Packing", v -> {
            orderStatus = "PACKING_LOCAL";
            Toast.makeText(this, "UI Packing siap. Endpoint write belum diaktifkan sampai kontraknya terverifikasi.", Toast.LENGTH_LONG).show();
            showOrderDetail();
        }));
        root.addView(button("Siap Kirim", v -> {
            orderStatus = "READY_TO_SHIP_LOCAL";
            Toast.makeText(this, "UI Siap Kirim siap. Endpoint write belum diaktifkan sampai kontraknya terverifikasi.", Toast.LENGTH_LONG).show();
            showOrderDetail();
        }));
        root.addView(button("Konfirmasi", v -> showConfirmation()));
        root.addView(button("Chat Pesanan", v -> showChat()));
        root.addView(button("Kembali", v -> showOrders()));
    }

    private void showConfirmation() {
        screen("Konfirmasi");
        String no = selectedOrder == null ? "-" : selectedOrder.number;
        root.addView(text("Shipment: " + no + "\nStatus saat ini: " + orderStatus, 15));
        root.addView(text("Konfirmasi produksi belum dikirim dari Lite sampai endpoint dan payload resminya selesai dipetakan. Validasi server/lokasi tetap dipertahankan.", 14));
        root.addView(button("Validasi UI", v -> Toast.makeText(this, "Flow UI konfirmasi OK", Toast.LENGTH_SHORT).show()));
        root.addView(button("Kembali", v -> showOrderDetail()));
    }

    private void showChat() {
        screen("Chat");
        TextView log = text("Pure text chat\n\n", 15);
        root.addView(log);
        EditText message = input("Tulis pesan...");
        root.addView(message);
        root.addView(button("Kirim", v -> {
            String m = message.getText().toString().trim();
            if (m.isEmpty()) return;
            log.setText(log.getText() + "Anda: " + m + "\n");
            message.setText("");
            Toast.makeText(this, "Endpoint kirim chat belum diaktifkan sampai contract channel/message selesai dipetakan.", Toast.LENGTH_SHORT).show();
        }));
        root.addView(text("Tanpa telepon, video, voice note, atau attachment.", 12));
        root.addView(button("Kembali", v -> showOrders()));
    }

    private static String compact(String message) {
        if (message == null) return "";
        String s = message.replace('\n', ' ').replace('\r', ' ').trim();
        return s.length() > 180 ? s.substring(0, 180) + "…" : s;
    }

    private static void collectOrders(String body, List<OrderItem> out) {
        try {
            Object root = body.trim().startsWith("[") ? new JSONArray(body) : new JSONObject(body);
            walk(root, out);
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
