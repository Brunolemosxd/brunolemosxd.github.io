package com.orcaon.app;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

// Tela que abre o OrçaOn em tela cheia (igual ao modelo oficial do Google, Bubblewrap).
public class LauncherActivity extends com.google.androidbrowserhelper.trusted.LauncherActivity {
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
}
