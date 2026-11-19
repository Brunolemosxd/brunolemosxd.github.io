package com.brunolemos.zapligacao;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aparece no menu de texto selecionado ("WhatsApp"): pega o número do texto
 * e abre a conversa no WhatsApp. Não tem tela própria.
 */
public class ZapTextoActivity extends Activity {

    private static final Pattern NUMERO = Pattern.compile("\\+?\\d[\\d\\s().\\-]{6,}\\d");

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        CharSequence texto = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        String zap = null;
        if (texto != null) {
            Matcher m = NUMERO.matcher(texto);
            while (zap == null && m.find()) zap = Zap.numeroParaZap(this, m.group());
        }
        if (zap == null) {
            Toast.makeText(this, "Não achei um número de telefone no texto", Toast.LENGTH_LONG).show();
        } else {
            try {
                startActivity(Zap.intentWhatsApp(zap, null));
            } catch (Exception e) {
                Toast.makeText(this, "Não achei o WhatsApp neste celular", Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }
}
