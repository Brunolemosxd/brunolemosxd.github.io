package com.brunolemos.zapligacao;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int VERDE = 0xFF25D366;
    private LinearLayout raiz;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Zap.criarCanal(this);
        ScrollView rolagem = new ScrollView(this);
        raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        int m = dp(16);
        raiz.setPadding(m, m, m, m);
        rolagem.addView(raiz);
        rolagem.setOnApplyWindowInsetsListener((v, ins) -> {
            int topo, baixo;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets i = ins.getInsets(WindowInsets.Type.systemBars());
                topo = i.top; baixo = i.bottom;
            } else {
                topo = ins.getSystemWindowInsetTop(); baixo = ins.getSystemWindowInsetBottom();
            }
            v.setPadding(0, topo, 0, baixo);
            return ins;
        });
        setContentView(rolagem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        montar();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        montar();
    }

    private boolean naConfig = false;
    private android.window.OnBackInvokedCallback voltarCallback;

    private void abrirConfig(boolean abrir) {
        naConfig = abrir;
        if (Build.VERSION.SDK_INT >= 33) {
            if (abrir && voltarCallback == null) {
                voltarCallback = () -> abrirConfig(false);
                getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                        android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, voltarCallback);
            } else if (!abrir && voltarCallback != null) {
                getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(voltarCallback);
                voltarCallback = null;
            }
        }
        montar();
        ((ScrollView) raiz.getParent()).scrollTo(0, 0);
    }

    @Override
    public void onBackPressed() {
        if (naConfig) abrirConfig(false);
        else super.onBackPressed();
    }

    /** Cabeçalho: título à esquerda e um botão pequeno à direita. */
    private void cabecalho(String titulo, String textoBotao, View.OnClickListener ao) {
        LinearLayout linha = new LinearLayout(this);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.addView(texto(titulo, 24, true), new LinearLayout.LayoutParams(0, -2, 1));
        linha.addView(botao(textoBotao, false, ao), new LinearLayout.LayoutParams(-2, -2));
        raiz.addView(linha);
        espaco(8);
    }

    private void montar() {
        raiz.removeAllViews();
        if (naConfig) montarConfig(); else montarInicio();
    }

    private void montarInicio() {
        cabecalho("Zap da Ligação", "⚙️ Config", v -> abrirConfig(true));

        List<String> faltam = permissoesFaltando();
        if (!faltam.isEmpty()) {
            raiz.addView(cartao("⚠️ Falta permissão",
                    "Toque no botão e escolha \"Permitir\" em tudo. O app só lê o número da última ligação, nada é enviado para fora do celular."));
            raiz.addView(botao("Dar permissões", true, v ->
                    requestPermissions(faltam.toArray(new String[0]), 1)));
            raiz.addView(botao("Abrir configurações do app", false, v -> abrirConfigApp()));
            espaco(16);
        }

        raiz.addView(texto("Ligações recentes", 18, true));
        espaco(4);
        List<Zap.Ligacao> lista = Zap.ultimas(this, 30, 0);
        if (lista.isEmpty()) {
            raiz.addView(texto(faltam.isEmpty() ? "Nenhuma ligação encontrada." : "Dê as permissões para ver as ligações aqui.", 14, false));
        }
        for (Zap.Ligacao l : lista) raiz.addView(linhaLigacao(l));
    }

    private void montarConfig() {
        SharedPreferences p = Zap.prefs(this);
        cabecalho("Configurações", "← Voltar", v -> abrirConfig(false));
        raiz.addView(texto("Quando uma ligação termina, aparece um aviso para mandar WhatsApp para o número.", 14, false));
        espaco(8);

        Switch ligado = chave("Avisar depois das ligações", p.getBoolean("ligado", true));
        ligado.setOnCheckedChangeListener((v, on) -> p.edit().putBoolean("ligado", on).apply());
        raiz.addView(ligado);

        Switch contatos = chave("Avisar também de contatos salvos", p.getBoolean("contatos", true));
        contatos.setOnCheckedChangeListener((v, on) -> p.edit().putBoolean("contatos", on).apply());
        raiz.addView(contatos);

        LinearLayout linhaDdd = new LinearLayout(this);
        linhaDdd.setGravity(Gravity.CENTER_VERTICAL);
        TextView rotulo = texto("Seu DDD (para números sem DDD)", 15, false);
        linhaDdd.addView(rotulo, new LinearLayout.LayoutParams(0, -2, 1));
        EditText ddd = new EditText(this);
        ddd.setInputType(InputType.TYPE_CLASS_NUMBER);
        ddd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        ddd.setText(p.getString("ddd", "51"));
        ddd.setGravity(Gravity.CENTER);
        ddd.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                if (s.length() == 2) p.edit().putString("ddd", s.toString()).apply();
            }
        });
        linhaDdd.addView(ddd, new LinearLayout.LayoutParams(dp(64), -2));
        raiz.addView(linhaDdd);
        espaco(8);

        raiz.addView(texto("Mensagem pronta (ligação que você recebeu ou perdeu)", 15, true));
        raiz.addView(campoMensagem(p, "msgRecebida", Zap.MSG_RECEBIDA));
        raiz.addView(texto("Ligações que você fez abrem sem mensagem. Deixe em branco para nunca usar mensagem pronta.", 13, false));
        espaco(12);

        raiz.addView(botao("Testar aviso com a última ligação", false, v -> testar()));
        raiz.addView(texto("Se o aviso não aparecer depois de uma ligação de verdade: em Configurações do app, ligue \"Início automático\" (Xiaomi) e deixe a bateria \"Sem restrições\".", 13, false));
        raiz.addView(botao("Abrir configurações do app", false, v -> abrirConfigApp()));
    }

    private View linhaLigacao(Zap.Ligacao l) {
        LinearLayout linha = new LinearLayout(this);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.setPadding(0, dp(8), 0, dp(8));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        String nome = TextUtils.isEmpty(l.nome) ? l.numero : l.nome;
        info.addView(texto(nome, 16, true));
        String quando = DateUtils.getRelativeTimeSpanString(l.data, System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS).toString();
        String sub = (TextUtils.isEmpty(l.nome) ? "" : l.numero + " • ") + Zap.tipoTexto(l.tipo) + " • " + quando;
        TextView tSub = texto(sub, 13, false);
        tSub.setAlpha(0.7f);
        info.addView(tSub);
        linha.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        Button zap = botao("WhatsApp", true, v -> abrirZap(l));
        linha.addView(zap, new LinearLayout.LayoutParams(-2, -2));
        return linha;
    }

    private void abrirZap(Zap.Ligacao l) {
        String z = Zap.numeroParaZap(this, l.numero);
        if (z == null) { toast("Esse número não dá para usar no WhatsApp"); return; }
        try { startActivity(Zap.intentWhatsApp(z, Zap.mensagem(this, l.tipo))); }
        catch (Exception e) { toast("Não achei o WhatsApp neste celular"); }
    }

    private void testar() {
        if (!permissoesFaltando().isEmpty()) { toast("Dê as permissões primeiro"); return; }
        List<Zap.Ligacao> l = Zap.ultimas(this, 1, 0);
        if (l.isEmpty()) { toast("Nenhuma ligação no histórico"); return; }
        Zap.avisar(this, l.get(0));
        toast("Aviso enviado. Olhe a barra de notificações.");
    }

    private List<String> permissoesFaltando() {
        List<String> faltam = new ArrayList<>();
        if (!Zap.temPermissao(this, Manifest.permission.READ_PHONE_STATE)) faltam.add(Manifest.permission.READ_PHONE_STATE);
        if (!Zap.temPermissao(this, Manifest.permission.READ_CALL_LOG)) faltam.add(Manifest.permission.READ_CALL_LOG);
        if (Build.VERSION.SDK_INT >= 33 && !Zap.temPermissao(this, Manifest.permission.POST_NOTIFICATIONS))
            faltam.add(Manifest.permission.POST_NOTIFICATIONS);
        return faltam;
    }

    private void abrirConfigApp() {
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName())));
    }

    // ---- pedacinhos da tela ----

    private TextView texto(String s, int sp, boolean negrito) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        if (negrito) t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(2), 0, dp(2));
        return t;
    }

    private View cartao(String titulo, String corpo) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable fundo = new GradientDrawable();
        fundo.setCornerRadius(dp(12));
        fundo.setColor(0x33F4C430);
        c.setBackground(fundo);
        c.addView(texto(titulo, 16, true));
        c.addView(texto(corpo, 14, false));
        return c;
    }

    private Button botao(String s, boolean destaque, View.OnClickListener ao) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setOnClickListener(ao);
        if (destaque) {
            GradientDrawable fundo = new GradientDrawable();
            fundo.setCornerRadius(dp(20));
            fundo.setColor(VERDE);
            b.setBackground(fundo);
            b.setTextColor(Color.WHITE);
            b.setTypeface(Typeface.DEFAULT_BOLD);
            b.setPadding(dp(16), 0, dp(16), 0);
        }
        return b;
    }

    private EditText campoMensagem(SharedPreferences p, String chave, String padrao) {
        EditText e = new EditText(this);
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        e.setText(p.getString(chave, padrao));
        e.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                p.edit().putString(chave, s.toString().trim()).apply();
            }
        });
        return e;
    }

    private Switch chave(String s, boolean on) {
        Switch sw = new Switch(this);
        sw.setText(s);
        sw.setTextSize(15);
        sw.setChecked(on);
        sw.setPadding(0, dp(8), 0, dp(8));
        return sw;
    }

    private void espaco(int alturaDp) {
        View v = new View(this);
        raiz.addView(v, new LinearLayout.LayoutParams(1, dp(alturaDp)));
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
