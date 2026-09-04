#!/usr/bin/env bash
set -euo pipefail
ROOT="${1:?decoded APK directory required}"

# QA-only integrated location test UI.
# Does not modify APO production location, mock/integrity checks, or order confirmation.
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
    .locals 8
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    new-instance v0, Landroid/widget/ScrollView;
    invoke-direct {v0, p0}, Landroid/widget/ScrollView;-><init>(Landroid/content/Context;)V
    new-instance v1, Landroid/widget/LinearLayout;
    invoke-direct {v1, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V
    const/4 v2, 0x1
    invoke-virtual {v1, v2}, Landroid/widget/LinearLayout;->setOrientation(I)V
    const/16 v2, 0x20
    invoke-virtual {v1, v2, v2, v2, v2}, Landroid/widget/LinearLayout;->setPadding(IIII)V

    new-instance v3, Landroid/widget/TextView;
    invoke-direct {v3, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V
    const-string v4, "APO Location QA"
    invoke-virtual {v3, v4}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    const/high16 v4, 0x41c00000    # 24.0f
    invoke-virtual {v3, v4}, Landroid/widget/TextView;->setTextSize(F)V
    invoke-virtual {v1, v3}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    new-instance v3, Landroid/widget/TextView;
    invoke-direct {v3, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V
    const-string v4, "QA internal aktif. Panel ini untuk menguji koordinat dan jarak tanpa mengubah lokasi atau konfirmasi order produksi."
    invoke-virtual {v3, v4}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    invoke-virtual {v1, v3}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    new-instance v3, Landroid/widget/EditText;
    invoke-direct {v3, p0}, Landroid/widget/EditText;-><init>(Landroid/content/Context;)V
    const-string v4, "Simulated latitude"
    invoke-virtual {v3, v4}, Landroid/widget/EditText;->setHint(Ljava/lang/CharSequence;)V
    const/16 v4, 0x2002
    invoke-virtual {v3, v4}, Landroid/widget/EditText;->setInputType(I)V
    const-string v5, "qa_sim_lat"
    invoke-virtual {v3, v5}, Landroid/widget/EditText;->setTag(Ljava/lang/Object;)V
    invoke-virtual {v1, v3}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    new-instance v3, Landroid/widget/EditText;
    invoke-direct {v3, p0}, Landroid/widget/EditText;-><init>(Landroid/content/Context;)V
    const-string v5, "Simulated longitude"
    invoke-virtual {v3, v5}, Landroid/widget/EditText;->setHint(Ljava/lang/CharSequence;)V
    invoke-virtual {v3, v4}, Landroid/widget/EditText;->setInputType(I)V
    const-string v5, "qa_sim_lon"
    invoke-virtual {v3, v5}, Landroid/widget/EditText;->setTag(Ljava/lang/Object;)V
    invoke-virtual {v1, v3}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    new-instance v3, Landroid/widget/EditText;
    invoke-direct {v3, p0}, Landroid/widget/EditText;-><init>(Landroid/content/Context;)V
    const-string v5, "Target latitude"
    invoke-virtual {v3, v5}, Landroid/widget/EditText;->setHint(Ljava/lang/CharSequence;)V
    invoke-virtual {v3, v4}, Landroid/widget/EditText;->setInputType(I)V
    const-string v5, "qa_target_lat"
    invoke-virtual {v3, v5}, Landroid/widget/EditText;->setTag(Ljava/lang/Object;)V
    invoke-virtual {v1, v3}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    new-instance v3, Landroid/widget/EditText;
    invoke-direct {v3, p0}, Landroid/widget/EditText;-><init>(Landroid/content/Context;)V
    const-string v5, "Target longitude"
    invoke-virtual {v3, v5}, Landroid/widget/EditText;->setHint(Ljava/lang/CharSequence;)V
    invoke-virtual {v3, v4}, Landroid/widget/EditText;->setInputType(I)V
    const-string v5, "qa_target_lon"
    invoke-virtual {v3, v5}, Landroid/widget/EditText;->setTag(Ljava/lang/Object;)V
    invoke-virtual {v1, v3}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    new-instance v3, Landroid/widget/Button;
    invoke-direct {v3, p0}, Landroid/widget/Button;-><init>(Landroid/content/Context;)V
    const-string v4, "TEST DISTANCE"
    invoke-virtual {v3, v4}, Landroid/widget/Button;->setText(Ljava/lang/CharSequence;)V
    new-instance v4, Lcom/alfamart/apo/qa/LocationQaActivity$1;
    invoke-direct {v4, p0, v1}, Lcom/alfamart/apo/qa/LocationQaActivity$1;-><init>(Lcom/alfamart/apo/qa/LocationQaActivity;Landroid/view/View;)V
    invoke-virtual {v3, v4}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V
    invoke-virtual {v1, v3}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    new-instance v3, Landroid/widget/TextView;
    invoke-direct {v3, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V
    const-string v4, "Masukkan empat koordinat lalu tekan TEST DISTANCE."
    invoke-virtual {v3, v4}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    const-string v4, "qa_result"
    invoke-virtual {v3, v4}, Landroid/widget/TextView;->setTag(Ljava/lang/Object;)V
    invoke-virtual {v1, v3}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    invoke-virtual {v0, v1}, Landroid/widget/ScrollView;->addView(Landroid/view/View;)V
    invoke-virtual {p0, v0}, Landroid/app/Activity;->setContentView(Landroid/view/View;)V
    return-void
.end method
SMALI

cat > "$SMALI_ROOT/com/alfamart/apo/qa/LocationQaActivity\$1.smali" <<'SMALI'
.class Lcom/alfamart/apo/qa/LocationQaActivity$1;
.super Ljava/lang/Object;
.implements Landroid/view/View$OnClickListener;

.field final synthetic a:Lcom/alfamart/apo/qa/LocationQaActivity;
.field final synthetic root:Landroid/view/View;

.method constructor <init>(Lcom/alfamart/apo/qa/LocationQaActivity;Landroid/view/View;)V
    .locals 0
    iput-object p1, p0, Lcom/alfamart/apo/qa/LocationQaActivity$1;->a:Lcom/alfamart/apo/qa/LocationQaActivity;
    iput-object p2, p0, Lcom/alfamart/apo/qa/LocationQaActivity$1;->root:Landroid/view/View;
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public onClick(Landroid/view/View;)V
    .locals 14
    :try_start
    iget-object v0, p0, Lcom/alfamart/apo/qa/LocationQaActivity$1;->root:Landroid/view/View;
    const-string v1, "qa_sim_lat"
    invoke-virtual {v0, v1}, Landroid/view/View;->findViewWithTag(Ljava/lang/Object;)Landroid/view/View;
    move-result-object v2
    check-cast v2, Landroid/widget/EditText;
    const-string v1, "qa_sim_lon"
    invoke-virtual {v0, v1}, Landroid/view/View;->findViewWithTag(Ljava/lang/Object;)Landroid/view/View;
    move-result-object v3
    check-cast v3, Landroid/widget/EditText;
    const-string v1, "qa_target_lat"
    invoke-virtual {v0, v1}, Landroid/view/View;->findViewWithTag(Ljava/lang/Object;)Landroid/view/View;
    move-result-object v4
    check-cast v4, Landroid/widget/EditText;
    const-string v1, "qa_target_lon"
    invoke-virtual {v0, v1}, Landroid/view/View;->findViewWithTag(Ljava/lang/Object;)Landroid/view/View;
    move-result-object v5
    check-cast v5, Landroid/widget/EditText;

    invoke-virtual {v2}, Landroid/widget/EditText;->getText()Landroid/text/Editable;
    move-result-object v6
    invoke-interface {v6}, Landroid/text/Editable;->toString()Ljava/lang/String;
    move-result-object v6
    invoke-static {v6}, Ljava/lang/Double;->parseDouble(Ljava/lang/String;)D
    move-result-wide v6
    invoke-virtual {v3}, Landroid/widget/EditText;->getText()Landroid/text/Editable;
    move-result-object v2
    invoke-interface {v2}, Landroid/text/Editable;->toString()Ljava/lang/String;
    move-result-object v2
    invoke-static {v2}, Ljava/lang/Double;->parseDouble(Ljava/lang/String;)D
    move-result-wide v8
    invoke-virtual {v4}, Landroid/widget/EditText;->getText()Landroid/text/Editable;
    move-result-object v2
    invoke-interface {v2}, Landroid/text/Editable;->toString()Ljava/lang/String;
    move-result-object v2
    invoke-static {v2}, Ljava/lang/Double;->parseDouble(Ljava/lang/String;)D
    move-result-wide v10
    invoke-virtual {v5}, Landroid/widget/EditText;->getText()Landroid/text/Editable;
    move-result-object v2
    invoke-interface {v2}, Landroid/text/Editable;->toString()Ljava/lang/String;
    move-result-object v2
    invoke-static {v2}, Ljava/lang/Double;->parseDouble(Ljava/lang/String;)D
    move-result-wide v12

    new-instance v2, Landroid/location/Location;
    const-string v3, "qa-a"
    invoke-direct {v2, v3}, Landroid/location/Location;-><init>(Ljava/lang/String;)V
    invoke-virtual {v2, v6, v7}, Landroid/location/Location;->setLatitude(D)V
    invoke-virtual {v2, v8, v9}, Landroid/location/Location;->setLongitude(D)V
    new-instance v3, Landroid/location/Location;
    const-string v4, "qa-b"
    invoke-direct {v3, v4}, Landroid/location/Location;-><init>(Ljava/lang/String;)V
    invoke-virtual {v3, v10, v11}, Landroid/location/Location;->setLatitude(D)V
    invoke-virtual {v3, v12, v13}, Landroid/location/Location;->setLongitude(D)V
    invoke-virtual {v2, v3}, Landroid/location/Location;->distanceTo(Landroid/location/Location;)F
    move-result v2

    const-string v1, "qa_result"
    invoke-virtual {v0, v1}, Landroid/view/View;->findViewWithTag(Ljava/lang/Object;)Landroid/view/View;
    move-result-object v0
    check-cast v0, Landroid/widget/TextView;
    new-instance v1, Ljava/lang/StringBuilder;
    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V
    const-string v3, "Jarak QA: "
    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;
    const-string v2, " meter"
    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    :try_end
    .catch Ljava/lang/Exception; {:try_start .. :try_end} :bad
    return-void
    :bad
    iget-object v0, p0, Lcom/alfamart/apo/qa/LocationQaActivity$1;->root:Landroid/view/View;
    const-string v1, "qa_result"
    invoke-virtual {v0, v1}, Landroid/view/View;->findViewWithTag(Ljava/lang/Object;)Landroid/view/View;
    move-result-object v0
    check-cast v0, Landroid/widget/TextView;
    const-string v1, "Koordinat tidak valid. Gunakan angka latitude/longitude."
    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
    return-void
.end method
SMALI

cat > "$SMALI_ROOT/com/alfamart/apo/qa/QaInitProvider.smali" <<'SMALI'
.class public Lcom/alfamart/apo/qa/QaInitProvider;
.super Landroid/content/ContentProvider;

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
.field private current:Landroid/app/Activity;
.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
.method public onActivityCreated(Landroid/app/Activity;Landroid/os/Bundle;)V .locals 0 return-void .end method
.method public onActivityStarted(Landroid/app/Activity;)V .locals 0 return-void .end method
.method public onActivityPaused(Landroid/app/Activity;)V .locals 0 return-void .end method
.method public onActivityStopped(Landroid/app/Activity;)V .locals 0 return-void .end method
.method public onActivitySaveInstanceState(Landroid/app/Activity;Landroid/os/Bundle;)V .locals 0 return-void .end method
.method public onActivityDestroyed(Landroid/app/Activity;)V .locals 0 return-void .end method
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
    invoke-virtual {v3, p0}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V
    invoke-virtual {v1, v3}, Landroid/view/ViewGroup;->addView(Landroid/view/View;)V
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
p=sys.argv[1]; A='http://schemas.android.com/apk/res/android'; ET.register_namespace('android',A)
t=ET.parse(p); app=t.getroot().find('application'); k=lambda x:'{%s}%s'%(A,x)
for tag, cls in [('activity','com.alfamart.apo.qa.LocationQaActivity'),('provider','com.alfamart.apo.qa.QaInitProvider')]:
    for n in list(app.findall(tag)):
        if n.get(k('name'))==cls: app.remove(n)
app.append(ET.Element('activity',{k('name'):'com.alfamart.apo.qa.LocationQaActivity',k('exported'):'false',k('label'):'APO Location QA',k('theme'):'@android:style/Theme.Material.Light.NoActionBar'}))
app.append(ET.Element('provider',{k('name'):'com.alfamart.apo.qa.QaInitProvider',k('authorities'):'com.alfamart.apo.qa.init',k('exported'):'false',k('initOrder'):'100'}))
t.write(p,encoding='utf-8',xml_declaration=True)
PY

echo "Stable integrated QA location/distance panel injected"
