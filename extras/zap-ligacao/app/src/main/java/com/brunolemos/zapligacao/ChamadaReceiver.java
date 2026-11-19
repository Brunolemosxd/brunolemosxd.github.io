package com.brunolemos.zapligacao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.List;

/**
 * O Android avisa este receptor quando o telefone toca, atende e desliga.
 * Quando a ligação termina, esperamos o app Telefone gravar no histórico,
 * lemos a última ligação e mostramos o aviso "Mandar WhatsApp?".
 */
public class ChamadaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {
        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) return;
        String estado = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        SharedPreferences p = Zap.prefs(c);

        if (!TelephonyManager.EXTRA_STATE_IDLE.equals(estado)) {
            // Tocando ou em ligação: marca o início (o Android manda cada aviso 2 vezes)
            if (!p.getBoolean("emLigacao", false)) {
                p.edit().putBoolean("emLigacao", true)
                        .putLong("inicio", System.currentTimeMillis()).commit();
            }
            return;
        }

        // Desligou
        if (!p.getBoolean("emLigacao", false)) return;
        long inicio = p.getLong("inicio", System.currentTimeMillis());
        p.edit().putBoolean("emLigacao", false).commit();
        if (!p.getBoolean("ligado", true)) return;

        final PendingResult resultado = goAsync();
        HandlerThread t = new HandlerThread("zap");
        t.start();
        Handler h = new Handler(t.getLooper());
        final Context app = c.getApplicationContext();
        Runnable[] tentativa = new Runnable[1];
        final int[] vezes = {0};
        tentativa[0] = () -> {
            Zap.Ligacao l = procurar(app, inicio);
            if (l == null && ++vezes[0] < 3) {
                h.postDelayed(tentativa[0], 2000); // histórico ainda não gravou
                return;
            }
            try {
                if (l != null && deveAvisar(app, l)) {
                    Zap.prefs(app).edit().putLong("ultimoId", l.id).commit();
                    Zap.avisar(app, l);
                }
            } finally {
                resultado.finish();
                t.quitSafely();
            }
        };
        h.postDelayed(tentativa[0], 1500);
    }

    private static Zap.Ligacao procurar(Context c, long inicio) {
        // Margem de 1 minuto: o relógio do histórico pode ser um pouco diferente
        List<Zap.Ligacao> l = Zap.ultimas(c, 1, inicio - 60_000);
        return l.isEmpty() ? null : l.get(0);
    }

    private static boolean deveAvisar(Context c, Zap.Ligacao l) {
        SharedPreferences p = Zap.prefs(c);
        if (p.getLong("ultimoId", -1) == l.id) return false;
        if (l.tipo == CallLog.Calls.BLOCKED_TYPE) return false;
        if (!p.getBoolean("contatos", true) && !TextUtils.isEmpty(l.nome)) return false;
        return true;
    }
}
