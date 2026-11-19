package com.brunolemos.zapligacao;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/** Funções comuns: ler ligações, montar o número para o WhatsApp e mostrar o aviso. */
final class Zap {

    static final String PREFS = "zap";
    static final String CANAL = "depois_da_ligacao";

    static final class Ligacao {
        long id;
        String numero;
        String nome;
        int tipo;
        long data;
    }

    private Zap() {}

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static boolean temPermissao(Context c, String p) {
        return c.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED;
    }

    /** Lê as últimas ligações (mais nova primeiro). */
    static List<Ligacao> ultimas(Context c, int limite, long desde) {
        List<Ligacao> lista = new ArrayList<>();
        if (!temPermissao(c, Manifest.permission.READ_CALL_LOG)) return lista;
        String[] cols = {CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE, CallLog.Calls.DATE};
        String filtro = desde > 0 ? CallLog.Calls.DATE + " >= ?" : null;
        String[] args = desde > 0 ? new String[]{String.valueOf(desde)} : null;
        try (Cursor cur = c.getContentResolver().query(CallLog.Calls.CONTENT_URI, cols, filtro, args,
                CallLog.Calls.DATE + " DESC")) {
            if (cur == null) return lista;
            while (cur.moveToNext() && lista.size() < limite) {
                Ligacao l = new Ligacao();
                l.id = cur.getLong(0);
                l.numero = cur.getString(1);
                l.nome = cur.getString(2);
                l.tipo = cur.getInt(3);
                l.data = cur.getLong(4);
                if (TextUtils.isEmpty(l.numero) || numeroParaZap(c, l.numero) == null) continue;
                lista.add(l);
            }
        } catch (Exception e) {
            // Sem acesso ou aparelho diferente: devolve o que conseguiu
        }
        return lista;
    }

    static String tipoTexto(int tipo) {
        switch (tipo) {
            case CallLog.Calls.INCOMING_TYPE: return "recebida";
            case CallLog.Calls.OUTGOING_TYPE: return "feita";
            case CallLog.Calls.MISSED_TYPE: return "perdida";
            case CallLog.Calls.REJECTED_TYPE: return "recusada";
            case CallLog.Calls.BLOCKED_TYPE: return "bloqueada";
            default: return "ligação";
        }
    }

    /**
     * Transforma o número da ligação no formato do WhatsApp (55 + DDD + número).
     * Exemplos: "(51) 99702-0357" -> 5551997020357; "015 51 99702-0357" -> 5551997020357;
     * "99702-0357" -> 55 + DDD padrão + número. Devolve null se não der para usar.
     */
    static String numeroParaZap(Context c, String numero) {
        if (numero == null) return null;
        String n = numero.trim();
        boolean internacional = n.startsWith("+");
        String d = n.replaceAll("[^0-9]", "");
        if (d.isEmpty()) return null;
        if (internacional) return d.length() >= 8 ? d : null;
        if (d.startsWith("00")) { // 00 + país (ligação internacional)
            d = d.substring(2);
            return d.length() >= 8 ? d : null;
        }
        if (d.startsWith("55") && (d.length() == 12 || d.length() == 13)) return d;
        while (d.startsWith("0")) d = d.substring(1);
        // 0 + operadora (2 dígitos) + DDD + número
        if (d.length() == 12 || d.length() == 13) d = d.substring(2);
        if (d.length() == 10 || d.length() == 11) return "55" + d;
        if (d.length() == 8 || d.length() == 9) {
            String ddd = prefs(c).getString("ddd", "51");
            return "55" + ddd + d;
        }
        return null; // números curtos (operadora, serviços) não têm WhatsApp
    }

    static final String MSG_RECEBIDA = "Oi, recebi uma ligação sua. Posso ajudar?";

    /** Mensagem pronta só para ligações que você recebeu/perdeu. As que você fez abrem sem texto. */
    static String mensagem(Context c, int tipo) {
        if (tipo == CallLog.Calls.OUTGOING_TYPE) return null;
        return prefs(c).getString("msgRecebida", MSG_RECEBIDA);
    }

    static Intent intentWhatsApp(String numeroZap, String texto) {
        String url = "https://wa.me/" + numeroZap;
        if (!TextUtils.isEmpty(texto)) url += "?text=" + Uri.encode(texto);
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    static void criarCanal(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        NotificationChannel ch = new NotificationChannel(CANAL, "Depois da ligação",
                NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Aviso para mandar WhatsApp quando a ligação termina");
        nm.createNotificationChannel(ch);
    }

    /** Mostra o aviso "Mandar WhatsApp?" para a ligação. */
    static void avisar(Context c, Ligacao l) {
        if (Build.VERSION.SDK_INT >= 33 && !temPermissao(c, Manifest.permission.POST_NOTIFICATIONS)) return;
        String zap = numeroParaZap(c, l.numero);
        if (zap == null) return;
        criarCanal(c);

        int idAviso = (int) (l.id % Integer.MAX_VALUE);
        PendingIntent abrirZap = PendingIntent.getActivity(c, idAviso, intentWhatsApp(zap, mensagem(c, l.tipo)),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent ligar = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + l.numero));
        ligar.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent ligarDeVolta = PendingIntent.getActivity(c, idAviso + 1, ligar,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String quem = TextUtils.isEmpty(l.nome) ? l.numero : l.nome + " • " + l.numero;

        Notification n = new Notification.Builder(c, CANAL)
                .setSmallIcon(R.drawable.ic_notif)
                .setColor(0xFF25D366)
                .setContentTitle("Mandar WhatsApp?")
                .setContentText(quem + " (" + tipoTexto(l.tipo) + ")")
                .setContentIntent(abrirZap)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .addAction(new Notification.Action.Builder(null, "Abrir WhatsApp", abrirZap).build())
                .addAction(new Notification.Action.Builder(null, "Ligar de volta", ligarDeVolta).build())
                .build();

        c.getSystemService(NotificationManager.class).notify(idAviso, n);
    }
}
