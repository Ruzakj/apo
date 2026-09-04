package com.ric.apolite;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private LinearLayout root;
    private TextView title;
    private String orderStatus = "NEW";

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
        EditText user = input("User / NIK");
        EditText pass = input("Password");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(user);
        root.addView(pass);
        root.addView(button("Masuk", v -> {
            if (user.getText().toString().trim().isEmpty() || pass.getText().toString().isEmpty()) {
                Toast.makeText(this, "Isi user dan password", Toast.LENGTH_SHORT).show();
                return;
            }
            // Backend authentication intentionally not hardcoded here.
            showOrders();
        }));
        root.addView(text("Backend APO belum dihubungkan pada build shell ini.", 12));
    }

    private void showOrders() {
        screen("Pesanan");
        root.addView(text("Workflow yang dipertahankan: Packing → Siap Kirim → Konfirmasi", 14));
        root.addView(button("Order #TEST-001   •   " + orderStatus, v -> showOrderDetail()));
        root.addView(button("Chat", v -> showChat()));
        root.addView(button("Keluar", v -> showLogin()));
    }

    private void showOrderDetail() {
        screen("Detail Pesanan");
        root.addView(text("Order #TEST-001", 18));
        root.addView(text("Status: " + orderStatus, 15));
        root.addView(text("2 item • pelanggan uji • data lokal shell", 14));
        root.addView(button("Packing", v -> {
            orderStatus = "PACKING";
            Toast.makeText(this, "Status lokal: PACKING", Toast.LENGTH_SHORT).show();
            showOrderDetail();
        }));
        root.addView(button("Siap Kirim", v -> {
            orderStatus = "READY_TO_SHIP";
            Toast.makeText(this, "Status lokal: READY_TO_SHIP", Toast.LENGTH_SHORT).show();
            showOrderDetail();
        }));
        root.addView(button("Konfirmasi", v -> showConfirmation()));
        root.addView(button("Chat Pesanan", v -> showChat()));
        root.addView(button("Kembali", v -> showOrders()));
    }

    private void showConfirmation() {
        screen("Konfirmasi");
        root.addView(text("Konfirmasi produksi akan dihubungkan hanya ke endpoint resmi/terotorisasi dan tetap mengikuti validasi server.", 14));
        root.addView(text("Order: #TEST-001\nStatus saat ini: " + orderStatus, 15));
        root.addView(button("Simulasi Konfirmasi Lokal", v -> {
            orderStatus = "CONFIRMED_LOCAL";
            Toast.makeText(this, "Simulasi lokal selesai", Toast.LENGTH_SHORT).show();
            showOrders();
        }));
        root.addView(button("Kembali", v -> showOrderDetail()));
    }

    private void showChat() {
        screen("Chat");
        TextView log = text("Pelanggan: Halo, pesanan saya bagaimana?\n\nAPO Lite: ", 15);
        root.addView(log);
        EditText message = input("Tulis pesan...");
        root.addView(message);
        root.addView(button("Kirim", v -> {
            String m = message.getText().toString().trim();
            if (m.isEmpty()) return;
            log.setText(log.getText() + m + "\n");
            message.setText("");
        }));
        root.addView(text("Pure text chat: tanpa telepon, video, voice note, atau attachment.", 12));
        root.addView(button("Kembali", v -> showOrders()));
    }

    @Override public void onBackPressed() {
        showOrders();
    }
}
