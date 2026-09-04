#!/usr/bin/env bash
set -euo pipefail
ROOT="${1:?decoded APK directory required}"

# This QA panel is intentionally isolated from APO's production location/order flow.
# It provides coordinate + distance testing inside the rebuilt APK without changing
# mock/integrity checks or submitting synthetic coordinates to production.

SMALI_ROOT=$(find "$ROOT" -maxdepth 1 -type d -name 'smali*' | sort | head -n1)
mkdir -p "$SMALI_ROOT/com/alfamart/apo/qa"

cat > "$SMALI_ROOT/com/alfamart/apo/qa/LocationQaActivity.smali" <<'SMALI'
.class public Lcom/alfamart/apo/qa/LocationQaActivity;
.super Landroid/app/Activity;
.source "LocationQaActivity.java"

.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Landroid/app/Activity;-><init>()V
    return-void
.end method

.method protected onCreate(Landroid/os/Bundle;)V
    .locals 5
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    new-instance v0, Landroid/webkit/WebView;
    invoke-direct {v0, p0}, Landroid/webkit/WebView;-><init>(Landroid/content/Context;)V
    invoke-virtual {v0}, Landroid/webkit/WebView;->getSettings()Landroid/webkit/WebSettings;
    move-result-object v1
    const/4 v2, 0x1
    invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V

    const-string v1, "<!doctype html><html><meta name='viewport' content='width=device-width,initial-scale=1'><style>body{font-family:sans-serif;background:#f6f7f9;color:#171717;padding:22px}h2{margin-bottom:4px}.badge{background:#fff0c2;padding:10px;border-radius:10px;margin:12px 0}input{box-sizing:border-box;width:100%;padding:13px;margin:5px 0 12px;border:1px solid #ccc;border-radius:10px;font-size:16px}button{width:100%;padding:14px;border:0;border-radius:10px;background:#111;color:#fff;font-weight:bold;font-size:16px}.card{background:white;padding:16px;border-radius:14px;margin-top:14px}small{color:#666}</style><body><h2>APO Location QA</h2><small>Isolated coordinate/distance simulator</small><div class='badge'><b>QA ONLY</b> — tidak mengubah lokasi order produksi.</div><div class='card'><b>Simulated position</b><input id='a' placeholder='Latitude, contoh -8.133'><input id='b' placeholder='Longitude, contoh 113.224'><b>Target/customer point</b><input id='c' placeholder='Latitude target'><input id='d' placeholder='Longitude target'><button onclick='calc()'>TEST DISTANCE</button><p id='r'>Belum dihitung.</p></div><script>function calc(){let a=+document.getElementById('a').value,b=+document.getElementById('b').value,c=+document.getElementById('c').value,d=+document.getElementById('d').value;if(![a,b,c,d].every(Number.isFinite)){r.innerText='Koordinat tidak valid';return}let q=Math.PI/180,x=(c-a)*q,y=(d-b)*q,z=Math.sin(x/2)**2+Math.cos(a*q)*Math.cos(c*q)*Math.sin(y/2)**2,m=6371000*2*Math.atan2(Math.sqrt(z),Math.sqrt(1-z));r.innerHTML='<b>'+m.toFixed(1)+' meter</b><br>Simulated: '+a+', '+b+'<br>Target: '+c+', '+d}</script></body></html>"
    const-string v2, "text/html"
    const-string v3, "UTF-8"
    const/4 v4, 0x0
    invoke-virtual {v0, v1, v2, v3, v4}, Landroid/webkit/WebView;->loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    invoke-virtual {p0, v0}, Landroid/app/Activity;->setContentView(Landroid/view/View;)V
    return-void
.end method
SMALI

python3 - "$ROOT/AndroidManifest.xml" <<'PY'
import sys, xml.etree.ElementTree as ET
p=sys.argv[1]
A='http://schemas.android.com/apk/res/android'
ET.register_namespace('android', A)
t=ET.parse(p); root=t.getroot(); app=root.find('application')
name='{%s}name'%A
for a in app.findall('activity'):
    if a.get(name)=='com.alfamart.apo.qa.LocationQaActivity':
        t.write(p, encoding='utf-8', xml_declaration=True); raise SystemExit
act=ET.Element('activity', {
    name:'com.alfamart.apo.qa.LocationQaActivity',
    '{%s}exported'%A:'true',
    '{%s}label'%A:'APO Location QA',
    '{%s}theme'%A:'@android:style/Theme.Material.Light.NoActionBar'
})
f=ET.SubElement(act,'intent-filter')
ET.SubElement(f,'action',{name:'android.intent.action.MAIN'})
ET.SubElement(f,'category',{name:'android.intent.category.LAUNCHER'})
app.append(act)
t.write(p, encoding='utf-8', xml_declaration=True)
PY

echo "QA location panel injected into decoded APK"
