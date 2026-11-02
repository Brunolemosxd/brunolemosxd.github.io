package com.orcaon.app;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.androidbrowserhelper.trusted.TwaLauncher;

// Tela que abre o OrçaOn em tela cheia (igual ao modelo oficial do Google, Bubblewrap).
public class LauncherActivity extends com.google.androidbrowserhelper.trusted.LauncherActivity {
    private static final String CHROME = "com.android.chrome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No Android 8.0 e anteriores, fixar a orientação com o fundo transparente fecha o app.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    // Abre sempre pelo Chrome quando ele está instalado: o navegador da Samsung não compartilha
    // o PDF como arquivo e escurece as cores do app no modo escuro do celular.
    @Override
    protected TwaLauncher createTwaLauncher() {
        if (chromeDisponivel()) return new TwaLauncher(this, CHROME);
        return super.createTwaLauncher();
    }

    private boolean chromeDisponivel() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(CHROME, 0);
            return info.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
