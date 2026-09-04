package com.ric.apolocationqa;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
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

import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_PERMISSIONS = 100;
    private static final double NUDGE = 0.0001;

    private EditText latInput;
    private EditText lonInput;
    private EditText accuracyInput;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
        requestNeededPermissions();
    }

    private View buildUi() {
        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = text("APO Location QA", 26, true);
        root.addView(title);
        TextView subtitle = text("Developer-only mock location using Android's official test-provider mechanism.", 14, false);
        subtitle.setPadding(0, dp(6), 0, dp(18));
        root.addView(subtitle);

        Button dev = button("Open Developer Options");
        dev.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)); }
            catch (Exception e) { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        });
        root.addView(dev);

        TextView hint = text("Select “APO Location QA” under Select mock location app, then return here.", 13, false);
        hint.setPadding(0, dp(8), 0, dp(18));
        root.addView(hint);

        latInput = decimalInput("Latitude", "-8.133500");
        lonInput = decimalInput("Longitude", "113.224800");
        accuracyInput = decimalInput("Accuracy (m)", "3");
        root.addView(latInput);
        root.addView(lonInput);
        root.addView(accuracyInput);

        root.addView(text("Fine adjustment (~11 m per tap)", 13, true));
        LinearLayout ns = horizontal();
        Button north = button("↑ North");
        Button south = button("↓ South");
        north.setOnClickListener(v -> nudgeLat(NUDGE));
        south.setOnClickListener(v -> nudgeLat(-NUDGE));
        ns.addView(north, weight());
        ns.addView(south, weight());
        root.addView(ns);

        LinearLayout ew = horizontal();
        Button west = button("← West");
        Button east = button("East →");
        west.setOnClickListener(v -> nudgeLon(-NUDGE));
        east.setOnClickListener(v -> nudgeLon(NUDGE));
        ew.addView(west, weight());
        ew.addView(east, weight());
        root.addView(ew);

        Button start = button("Start QA Location");
        start.setOnClickListener(v -> startMock());
        root.addView(start);

        Button openMap = button("Preview Coordinate in Maps");
        openMap.setOnClickListener(v -> previewMap());
        root.addView(openMap);

        Button stop = button("Stop & Restore Real GPS");
        stop.setOnClickListener(v -> stopMock());
        root.addView(stop);

        status = text("Status: inactive", 14, true);
        status.setPadding(0, dp(18), 0, dp(8));
        root.addView(status);

        TextView safety = text("This build does not hide mock-location state, alter integrity checks, or bypass anti-mock detection.", 12, false);
        root.addView(safety);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        return scroll;
    }

    private void startMock() {
        Parsed p = parse();
        if (p == null) return;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestNeededPermissions();
            Toast.makeText(this, "Grant location permission first", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MockLocationService.class);
        intent.setAction(MockLocationService.ACTION_START);
        intent.putExtra(MockLocationService.EXTRA_LAT, p.lat);
        intent.putExtra(MockLocationService.EXTRA_LON, p.lon);
        intent.putExtra(MockLocationService.EXTRA_ACCURACY, p.accuracy);
        try {
            startForegroundService(intent);
            status.setText(String.format(Locale.US, "Status: active at %.6f, %.6f", p.lat, p.lon));
        } catch (Exception e) {
            status.setText("Status: could not start. Check Developer Options mock-app selection.");
            Toast.makeText(this, "Mock location permission not available", Toast.LENGTH_LONG).show();
        }
    }

    private void stopMock() {
        Intent intent = new Intent(this, MockLocationService.class);
        intent.setAction(MockLocationService.ACTION_STOP);
        try { startService(intent); } catch (Exception ignored) { stopService(intent); }
        status.setText("Status: inactive — real GPS restored");
    }

    private void previewMap() {
        Parsed p = parse();
        if (p == null) return;
        Uri uri = Uri.parse(String.format(Locale.US, "geo:%.6f,%.6f?q=%.6f,%.6f", p.lat, p.lon, p.lat, p.lon));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try { startActivity(intent); }
        catch (Exception e) { Toast.makeText(this, "No map app available", Toast.LENGTH_SHORT).show(); }
    }

    private Parsed parse() {
        try {
            double lat = Double.parseDouble(latInput.getText().toString().trim().replace(',', '.'));
            double lon = Double.parseDouble(lonInput.getText().toString().trim().replace(',', '.'));
            float acc = Float.parseFloat(accuracyInput.getText().toString().trim().replace(',', '.'));
            if (lat < -90 || lat > 90 || lon < -180 || lon > 180 || acc < 1 || acc > 1000) throw new NumberFormatException();
            return new Parsed(lat, lon, acc);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Check latitude, longitude, and accuracy values", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void nudgeLat(double delta) {
        try {
            double value = Double.parseDouble(latInput.getText().toString().trim().replace(',', '.')) + delta;
            value = Math.max(-90, Math.min(90, value));
            latInput.setText(String.format(Locale.US, "%.6f", value));
        } catch (Exception ignored) {}
    }

    private void nudgeLon(double delta) {
        try {
            double value = Double.parseDouble(lonInput.getText().toString().trim().replace(',', '.')) + delta;
            value = Math.max(-180, Math.min(180, value));
            lonInput.setText(String.format(Locale.US, "%.6f", value));
        } catch (Exception ignored) {}
    }

    private void requestNeededPermissions() {
        java.util.ArrayList<String> permissions = new java.util.ArrayList<>();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!permissions.isEmpty()) requestPermissions(permissions.toArray(new String[0]), REQ_PERMISSIONS);
    }

    private EditText decimalInput(String hint, String value) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value);
        e.setTextSize(18);
        e.setSingleLine(true);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        return e;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setMinHeight(dp(48));
        return b;
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER);
        return l;
    }

    private LinearLayout.LayoutParams weight() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(dp(3), dp(3), dp(3), dp(3));
        return p;
    }

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private static class Parsed {
        final double lat;
        final double lon;
        final float accuracy;
        Parsed(double lat, double lon, float accuracy) {
            this.lat = lat;
            this.lon = lon;
            this.accuracy = accuracy;
        }
    }
}
