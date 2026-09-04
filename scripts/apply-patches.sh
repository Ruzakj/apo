#!/usr/bin/env bash
set -euo pipefail
ROOT="${1:?decoded APK directory required}"

# QA-only location test UI. This injects a small QA button into APO's resumed Activities
# and opens an isolated coordinate/distance panel inside the same APK.
# It does NOT alter mock/integrity checks and does NOT feed synthetic coordinates into
# production order confirmation/submission flows.

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

    const-string v1, "<!doctype html><html><meta name='viewport' content='width=device-width,initial-scale=1'><style>body{font-family:sans-serif;background:#f6f7f9;color:#171717;padding:22px}h2{margin-bottom:4px}.badge{background:#fff0c2;padding:10px;border-radius:10px;margin:12px 0}input{box-sizing:border-box;width:100%;padding:13px;margin:5px 0 12px;border:1px solid #ccc;border-radius:10px;font-size:16px}button{width:100%;padding:14px;border:0;border-radius:10px;background:#111;color:#fff;font-weight:bold;font-size:16px}.card{background:white;padding:16px;border-radius:14px;margin-top:14px}small{color:#666}</style><body><h2>APO Location QA</h2><small>Internal coordinate/distance test panel</small><div class='badge'><b>QA ONLY</b> — tidak mengubah lokasi konfirmasi pesanan produksi.</div><div class='card'><b>Simulated position</b><input id='a' inputmode='decimal' placeholder='Latitude, contoh -8.133'><input id='b' inputmode='decimal' placeholder='Longitude, contoh 113.224'><b>Target/customer point</b><input id='c' inputmode='decimal' placeholder='Latitude target'><input id='d' inputmode='decimal' placeholder='Longitude target'><button onclick='calc()'>TEST DISTANCE</button><p id='r'>Belum dihitung.</p></div><script>function calc(){let a=+document.getElementById('a').value,b=+document.getElementById('b').value,c=+document.getElementById('c').value,d=+document.getElementById('d').value;if(![a,b,c,d].every(Number.isFinite)){r.innerText='Koordinat tidak valid';return}let q=Math.PI/180,x=(c-a)*q,y=(d-b)*q,z=Math.sin(x/2)**2+Math.cos(a*q)*Math.cos(c*q)*Math.sin(y/2)**2,m=6371000*2*Math.atan2(Math.sqrt(z),Math.sqrt(1-z));r.innerHTML='<b>'+m.toFixed(1)+' meter</b><br>Simulated: '+a+', '+b+'<br>Target: '+c+', '+d}</script></body></html>"
    const-string v2, "text/html"
    const-string v3, "UTF-8"
    const/4 v4, 0x0
    invoke-virtual {v0, v1, v2, v3, v4}, Landroid/webkit/WebView;->loadDataWithBaseURL(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    invoke-virtual {p0, v0}, Landroid/app/Activity;->setContentView(Landroid/view/View;)V
    return-void
.end method
SMALI

cat > "$SMALI_ROOT/com/alfamart/apo/qa/QaInitProvider.smali" <<'SMALI'
.class public Lcom/alfamart/apo/qa/QaInitProvider;
.super Landroid/content/ContentProvider;
.source "QaInitProvider.java"

.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Landroid/content/ContentProvider;-><init>()V
    return-void
.end method

.method public onCreate()Z
    .locals 3
    invoke-virtual {p0}, Landroid/content/ContentProvider;->getContext()Landroid/content/Context;
    move-result-object v0
    if-eqz v0, :done
    invoke-virtual {v0}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
    move-result-object v0
    instance-of v1, v0, Landroid/app/Application;
    if-eqz v1, :done
    check-cast v0, Landroid/app/Application;
    new-instance v1, Lcom/alfamart/apo/qa/QaLifecycleCallbacks;
    invoke-direct {v1}, Lcom/alfamart/apo/qa/QaLifecycleCallbacks;-><init>()V
    invoke-virtual {v0, v1}, Landroid/app/Application;->registerActivityLifecycleCallbacks(Landroid/app/Application$ActivityLifecycleCallbacks;)V
    :done
    const/4 v2, 0x1
    return v2
.end method

.method public query(Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public getType(Landroid/net/Uri;)Ljava/lang/String;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public insert(Landroid/net/Uri;Landroid/content/ContentValues;)Landroid/net/Uri;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public delete(Landroid/net/Uri;Ljava/lang/String;[Ljava/lang/String;)I
    .locals 1
    const/4 v0, 0x0
    return v0
.end method

.method public update(Landroid/net/Uri;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;)I
    .locals 1
    const/4 v0, 0x0
    return v0
.end method
SMALI

cat > "$SMALI_ROOT/com/alfamart/apo/qa/QaLifecycleCallbacks.smali" <<'SMALI'
.class public Lcom/alfamart/apo/qa/QaLifecycleCallbacks;
.super Ljava/lang/Object;
.implements Landroid/app/Application$ActivityLifecycleCallbacks;
.implements Landroid/view/View$OnClickListener;
.source "QaLifecycleCallbacks.java"

.field private current:Landroid/app/Activity;

.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public onActivityCreated(Landroid/app/Activity;Landroid/os/Bundle;)V
    .locals 0
    return-void
.end method

.method public onActivityStarted(Landroid/app/Activity;)V
    .locals 0
    return-void
.end method

.method public onActivityResumed(Landroid/app/Activity;)V
    .locals 6
    instance-of v0, p1, Lcom/alfamart/apo/qa/LocationQaActivity;
    if-nez v0, :done

    iput-object p1, p0, Lcom/alfamart/apo/qa/QaLifecycleCallbacks;->current:Landroid/app/Activity;
    invoke-virtual {p1}, Landroid/app/Activity;->getWindow()Landroid/view/Window;
    move-result-object v0
    invoke-virtual {v0}, Landroid/view/Window;->getDecorView()Landroid/view/View;
    move-result-object v1

    const-string v2, "APO_QA_FLOAT_BUTTON"
    invoke-virtual {v1, v2}, Landroid/view/View;->findViewWithTag(Ljava/lang/Object;)Landroid/view/View;
    move-result-object v3
    if-nez v3, :done

    instance-of v3, v1, Landroid/view/ViewGroup;
    if-eqz v3, :done
    check-cast v1, Landroid/view/ViewGroup;

    new-instance v3, Landroid/widget/Button;
    invoke-direct {v3, p1}, Landroid/widget/Button;-><init>(Landroid/content/Context;)V
    const-string v4, "QA"
    invoke-virtual {v3, v4}, Landroid/widget/Button;->setText(Ljava/lang/CharSequence;)V
    invoke-virtual {v3, v2}, Landroid/widget/Button;->setTag(Ljava/lang/Object;)V
    const/4 v4, 0x0
    invoke-virtual {v3, v4}, Landroid/widget/Button;->setAllCaps(Z)V
    invoke-virtual {v3, p0}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    new-instance v4, Landroid/widget/FrameLayout$LayoutParams;
    const/4 v5, -0x2
    invoke-direct {v4, v5, v5}, Landroid/widget/FrameLayout$LayoutParams;-><init>(II)V
    const/16 v5, 0x35
    iput v5, v4, Landroid/widget/FrameLayout$LayoutParams;->gravity:I
    const/16 v5, 0x18
    iput v5, v4, Landroid/widget/FrameLayout$LayoutParams;->rightMargin:I
    const/16 v5, 0x60
    iput v5, v4, Landroid/widget/FrameLayout$LayoutParams;->topMargin:I
    invoke-virtual {v1, v3, v4}, Landroid/view/ViewGroup;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    :done
    return-void
.end method

.method public onActivityPaused(Landroid/app/Activity;)V
    .locals 0
    return-void
.end method

.method public onActivityStopped(Landroid/app/Activity;)V
    .locals 0
    return-void
.end method

.method public onActivitySaveInstanceState(Landroid/app/Activity;Landroid/os/Bundle;)V
    .locals 0
    return-void
.end method

.method public onActivityDestroyed(Landroid/app/Activity;)V
    .locals 1
    iget-object v0, p0, Lcom/alfamart/apo/qa/QaLifecycleCallbacks;->current:Landroid/app/Activity;
    if-ne v0, p1, :done
    const/4 v0, 0x0
    iput-object v0, p0, Lcom/alfamart/apo/qa/QaLifecycleCallbacks;->current:Landroid/app/Activity;
    :done
    return-void
.end method

.method public onClick(Landroid/view/View;)V
    .locals 3
    iget-object v0, p0, Lcom/alfamart/apo/qa/QaLifecycleCallbacks;->current:Landroid/app/Activity;
    if-eqz v0, :done
    new-instance v1, Landroid/content/Intent;
    const-class v2, Lcom/alfamart/apo/qa/LocationQaActivity;
    invoke-direct {v1, v0, v2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    invoke-virtual {v0, v1}, Landroid/app/Activity;->startActivity(Landroid/content/Intent;)V
    :done
    return-void
.end method
SMALI

python3 - "$ROOT/AndroidManifest.xml" <<'PY'
import sys, xml.etree.ElementTree as ET
p=sys.argv[1]
A='http://schemas.android.com/apk/res/android'
ET.register_namespace('android', A)
t=ET.parse(p)
root=t.getroot()
app=root.find('application')
name='{%s}name'%A
exported='{%s}exported'%A
label='{%s}label'%A
theme='{%s}theme'%A
authorities='{%s}authorities'%A
init_order='{%s}initOrder'%A

# Remove any previous QA Activity declaration, including the old separate launcher entry.
for node in list(app.findall('activity')):
    if node.get(name)=='com.alfamart.apo.qa.LocationQaActivity':
        app.remove(node)

act=ET.Element('activity', {
    name:'com.alfamart.apo.qa.LocationQaActivity',
    exported:'false',
    label:'APO Location QA',
    theme:'@android:style/Theme.Material.Light.NoActionBar'
})
app.append(act)

# Auto-init QA overlay without replacing APO's existing Application class.
for node in list(app.findall('provider')):
    if node.get(name)=='com.alfamart.apo.qa.QaInitProvider':
        app.remove(node)
provider=ET.Element('provider', {
    name:'com.alfamart.apo.qa.QaInitProvider',
    authorities:'com.alfamart.apo.qa.init',
    exported:'false',
    init_order:'100'
})
app.append(provider)

t.write(p, encoding='utf-8', xml_declaration=True)
PY

echo "Integrated QA button + internal location test panel injected into APO"
