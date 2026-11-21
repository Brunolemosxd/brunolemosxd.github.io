package com.brunolemos.zapligacao;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    // Cores
    private static final int VERDE = 0xFF25D366;
    private static final int VERDE_ESCURO = 0xFF128C7E;
    private static final int VERMELHO = 0xFFF15C6D;
    private static final int AZUL = 0xFF53BDEB;
    private static final int[] CORES_AVATAR = {
            0xFF7E57C2, 0xFF26A69A, 0xFFEF6C00, 0xFF5C6BC0,
            0xFFEC407A, 0xFF8D6E63, 0xFF29B6F6, 0xFF9CCC65};

    private boolean escuro;
    private int corFundo, corCartao, corTexto, corSub, corLinha, corCampo;

    private ScrollView rolagem;
    private LinearLayout raiz;
    private boolean naConfig = false;
    private android.window.OnBackInvokedCallback voltarCallback;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Zap.criarCanal(this);
        definirCores();

        rolagem = new ScrollView(this);
        rolagem.setBackgroundColor(corFundo);
        rolagem.setClipToPadding(false);
        raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(16), dp(8), dp(16), dp(24));
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
        barrasDoSistema();
    }

    private void definirCores() {
        escuro = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        if (escuro) {
            corFundo = 0xFF0E1512; corCartao = 0xFF1A2420; corTexto = 0xFFE9EDEF;
            corSub = 0xFF8C9A94; corLinha = 0xFF26332D; corCampo = 0xFF111A16;
        } else {
            corFundo = 0xFFF2F5F3; corCartao = 0xFFFFFFFF; corTexto = 0xFF14201B;
            corSub = 0xFF66756E; corLinha = 0xFFE3E9E6; corCampo = 0xFFF2F5F3;
        }
    }

    @SuppressWarnings("deprecation")
    private void barrasDoSistema() {
        if (Build.VERSION.SDK_INT < 35) {
            getWindow().setStatusBarColor(corFundo);
            getWindow().setNavigationBarColor(corFundo);
        }
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                int claro = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                c.setSystemBarsAppearance(escuro ? 0 : claro, claro);
            }
        } else if (!escuro) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
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
        rolagem.scrollTo(0, 0);
    }

    @Override
    public void onBackPressed() {
        if (naConfig) abrirConfig(false);
        else super.onBackPressed();
    }

    private void montar() {
        raiz.removeAllViews();
        if (naConfig) montarConfig(); else montarInicio();
    }

    // ================= Tela inicial =================

    private void montarInicio() {
        // Cabeçalho
        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.setPadding(dp(4), dp(12), 0, dp(16));
        LinearLayout titulos = new LinearLayout(this);
        titulos.setOrientation(LinearLayout.VERTICAL);
        titulos.addView(texto("Zap da Ligação", 26, corTexto, true));
        titulos.addView(texto("Toque no verde para chamar no WhatsApp", 14, corSub, false));
        topo.addView(titulos, new LinearLayout.LayoutParams(0, -2, 1));
        topo.addView(botaoIcone(R.drawable.ic_config, corCartao, corTexto, "Configurações",
                v -> abrirConfig(true)));
        raiz.addView(topo);

        // Permissões
        List<String> faltam = permissoesFaltando();
        if (!faltam.isEmpty()) {
            LinearLayout aviso = cartao();
            aviso.setBackground(fundoArredondado(escuro ? 0xFF3A3217 : 0xFFFFF4CC, dp(20)));
            aviso.addView(texto("Falta permissão", 17, corTexto, true));
            TextView d = texto("Toque no botão e escolha \"Permitir\" em tudo. O app só lê o número da última ligação; nada sai do celular.", 14, corTexto, false);
            d.setAlpha(0.85f);
            d.setPadding(0, dp(4), 0, dp(12));
            aviso.addView(d);
            aviso.addView(botaoGrande("Dar permissões", VERDE, Color.WHITE,
                    v -> requestPermissions(faltam.toArray(new String[0]), 1)));
            raiz.addView(aviso, margem(0, 0, 0, 16));
        }

        // Lista
        List<Zap.Ligacao> lista = Zap.ultimas(this, 40, 0);
        if (lista.isEmpty()) {
            LinearLayout vazio = cartao();
            vazio.setGravity(Gravity.CENTER_HORIZONTAL);
            vazio.setPadding(dp(20), dp(32), dp(20), dp(32));
            vazio.addView(icone(R.drawable.ic_entrada, corSub, 40));
            TextView tv = texto(faltam.isEmpty() ? "Nenhuma ligação ainda" : "Dê as permissões para ver suas ligações aqui",
                    15, corSub, false);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, dp(8), 0, 0);
            vazio.addView(tv);
            raiz.addView(vazio);
            return;
        }

        String grupoAtual = null;
        LinearLayout bloco = null;
        for (Zap.Ligacao l : lista) {
            String g = grupo(l.data);
            if (!g.equals(grupoAtual)) {
                TextView sec = tituloSecao(g);
                if (grupoAtual == null) sec.setPadding(dp(8), 0, 0, dp(8));
                grupoAtual = g;
                raiz.addView(sec);
                bloco = new LinearLayout(this);
                bloco.setOrientation(LinearLayout.VERTICAL);
                bloco.setBackground(fundoArredondado(corCartao, dp(20)));
                bloco.setClipToOutline(true);
                raiz.addView(bloco);
            } else {
                View divisor = new View(this);
                divisor.setBackgroundColor(corLinha);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Math.max(1, dp(1) / 2));
                lp.setMargins(dp(76), 0, dp(16), 0);
                bloco.addView(divisor, lp);
            }
            bloco.addView(linhaLigacao(l));
        }
    }

    private View linhaLigacao(Zap.Ligacao l) {
        LinearLayout linha = new LinearLayout(this);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.setPadding(dp(14), dp(12), dp(12), dp(12));

        // Avatar
        boolean temNome = !TextUtils.isEmpty(l.nome) && !l.nome.trim().isEmpty();
        FrameLayout avatar = new FrameLayout(this);
        GradientDrawable circ = new GradientDrawable();
        circ.setShape(GradientDrawable.OVAL);
        if (temNome) {
            circ.setColor(CORES_AVATAR[Math.floorMod(l.nome.hashCode(), CORES_AVATAR.length)]);
            TextView ini = texto(l.nome.trim().substring(0, 1).toUpperCase(), 20, Color.WHITE, true);
            ini.setGravity(Gravity.CENTER);
            avatar.addView(ini, new FrameLayout.LayoutParams(-1, -1));
        } else {
            circ.setColor(escuro ? 0xFF2C3A34 : 0xFFDDE5E1);
            avatar.addView(icone(R.drawable.ic_pessoa, escuro ? 0xFF8C9A94 : 0xFF8FA39A, 26),
                    new FrameLayout.LayoutParams(dp(26), dp(26), Gravity.CENTER));
        }
        avatar.setBackground(circ);
        linha.addView(avatar, new LinearLayout.LayoutParams(dp(48), dp(48)));

        // Textos
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(14), 0, dp(8), 0);
        boolean perdida = l.tipo == CallLog.Calls.MISSED_TYPE || l.tipo == CallLog.Calls.REJECTED_TYPE;
        TextView nome = texto(temNome ? l.nome : formatarNumero(l.numero), 16,
                perdida ? VERMELHO : corTexto, true);
        nome.setSingleLine(true);
        nome.setEllipsize(TextUtils.TruncateAt.END);
        info.addView(nome);

        LinearLayout sub = new LinearLayout(this);
        sub.setGravity(Gravity.CENTER_VERTICAL);
        sub.setPadding(0, dp(2), 0, 0);
        int icTipo = l.tipo == CallLog.Calls.OUTGOING_TYPE ? R.drawable.ic_saida
                : perdida ? R.drawable.ic_perdida : R.drawable.ic_entrada;
        int corTipo = l.tipo == CallLog.Calls.OUTGOING_TYPE ? AZUL : perdida ? VERMELHO : VERDE;
        sub.addView(icone(icTipo, corTipo, 16));
        String quando = new SimpleDateFormat(grupo(l.data).equals("Anteriores") ? "dd/MM, HH:mm" : "HH:mm",
                new Locale("pt", "BR")).format(l.data);
        String detalhe = Zap.tipoTexto(l.tipo) + " • " + quando;
        if (temNome) detalhe = formatarNumero(l.numero) + " • " + detalhe;
        TextView tSub = texto(detalhe, 13, corSub, false);
        tSub.setSingleLine(true);
        tSub.setEllipsize(TextUtils.TruncateAt.END);
        tSub.setPadding(dp(4), 0, 0, 0);
        sub.addView(tSub);
        info.addView(sub);
        linha.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        // Botão verde
        linha.addView(botaoIcone(R.drawable.ic_notif, VERDE, Color.WHITE, "Mandar WhatsApp",
                v -> abrirZap(l)));
        return linha;
    }

    private String grupo(long data) {
        Calendar hoje = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(data);
        if (mesmoDia(c, hoje)) return "Hoje";
        hoje.add(Calendar.DAY_OF_YEAR, -1);
        if (mesmoDia(c, hoje)) return "Ontem";
        return "Anteriores";
    }

    private static boolean mesmoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    /** "51997020357" -> "(51) 99702-0357" para ficar fácil de ler. */
    static String formatarNumero(String n) {
        String d = n.replaceAll("[^0-9]", "");
        if (d.startsWith("55") && (d.length() == 12 || d.length() == 13)) d = d.substring(2);
        if (d.length() == 11) return "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
        if (d.length() == 10) return "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
        return n;
    }

    // ================= Configurações =================

    private void montarConfig() {
        SharedPreferences p = Zap.prefs(this);

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.setPadding(0, dp(12), 0, dp(8));
        topo.addView(botaoIcone(R.drawable.ic_voltar, corCartao, corTexto, "Voltar", v -> abrirConfig(false)));
        TextView t = texto("Configurações", 24, corTexto, true);
        t.setPadding(dp(14), 0, 0, 0);
        topo.addView(t);
        raiz.addView(topo);

        // Avisos
        raiz.addView(tituloSecao("Avisos"));
        LinearLayout avisos = cartao();
        Switch ligado = chave("Avisar depois das ligações",
                "Mostra \"Mandar WhatsApp?\" quando a ligação termina", p.getBoolean("ligado", true), avisos);
        ligado.setOnCheckedChangeListener((v, on) -> p.edit().putBoolean("ligado", on).apply());
        divisor(avisos);
        Switch contatos = chave("Avisar também de contatos salvos",
                "Desligado: só avisa de números que não estão na agenda", p.getBoolean("contatos", true), avisos);
        contatos.setOnCheckedChangeListener((v, on) -> p.edit().putBoolean("contatos", on).apply());
        raiz.addView(avisos);

        // Mensagem
        raiz.addView(tituloSecao("Mensagem pronta"));
        LinearLayout msg = cartao();
        msg.addView(texto("Para ligações que você recebeu ou perdeu", 14, corSub, false));
        EditText campo = campo(p.getString("msgRecebida", Zap.MSG_RECEBIDA), true);
        campo.addTextChangedListener(salvar(s -> p.edit().putString("msgRecebida", s.trim()).apply()));
        msg.addView(campo, margem(0, 8, 0, 8));
        msg.addView(texto("Ligações que você fez abrem sem mensagem. Deixe em branco para nunca usar.", 13, corSub, false));
        raiz.addView(msg);

        // DDD
        raiz.addView(tituloSecao("Seu DDD"));
        LinearLayout dddC = cartao();
        LinearLayout linhaDdd = new LinearLayout(this);
        linhaDdd.setGravity(Gravity.CENTER_VERTICAL);
        linhaDdd.addView(texto("Usado quando o número vem sem DDD", 15, corTexto, false),
                new LinearLayout.LayoutParams(0, -2, 1));
        EditText ddd = campo(p.getString("ddd", "51"), false);
        ddd.setInputType(InputType.TYPE_CLASS_NUMBER);
        ddd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        ddd.setGravity(Gravity.CENTER);
        ddd.setTypeface(Typeface.DEFAULT_BOLD);
        ddd.addTextChangedListener(salvar(s -> { if (s.length() == 2) p.edit().putString("ddd", s).apply(); }));
        linhaDdd.addView(ddd, new LinearLayout.LayoutParams(dp(72), -2));
        dddC.addView(linhaDdd);
        raiz.addView(dddC);

        // Ajuda
        raiz.addView(tituloSecao("O aviso não aparece?"));
        LinearLayout ajuda = cartao();
        TextView dica = texto("No Xiaomi: em Configurações do app, ligue \"Início automático\" e deixe a bateria \"Sem restrições\".",
                14, corTexto, false);
        dica.setPadding(0, 0, 0, dp(12));
        ajuda.addView(dica);
        ajuda.addView(botaoGrande("Testar aviso", VERDE, Color.WHITE, v -> testar()));
        ajuda.addView(new View(this), new LinearLayout.LayoutParams(1, dp(8)));
        ajuda.addView(botaoGrande("Abrir configurações do app", escuro ? 0xFF26332D : 0xFFE6EEEA, corTexto,
                v -> abrirConfigApp()));
        raiz.addView(ajuda);

        TextView rodape = texto("Dica: selecione um número em qualquer app, toque nos ⋮ e escolha \"WhatsApp\".",
                13, corSub, false);
        rodape.setGravity(Gravity.CENTER);
        rodape.setPadding(dp(16), dp(20), dp(16), 0);
        raiz.addView(rodape);
    }

    // ================= Ações =================

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

    // ================= Peças da tela =================

    private TextView texto(String s, int sp, int cor, boolean negrito) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(cor);
        if (negrito) t.setTypeface(sp >= 22 ? Typeface.DEFAULT_BOLD
                : Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return t;
    }

    private TextView tituloSecao(String s) {
        TextView t = texto(s.toUpperCase(new Locale("pt", "BR")), 12, VERDE, true);
        t.setLetterSpacing(0.08f);
        t.setPadding(dp(8), dp(18), 0, dp(8));
        return t;
    }

    private LinearLayout cartao() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        c.setBackground(fundoArredondado(corCartao, dp(20)));
        return c;
    }

    private void divisor(LinearLayout pai) {
        View d = new View(this);
        d.setBackgroundColor(corLinha);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Math.max(1, dp(1) / 2));
        lp.setMargins(0, dp(6), 0, dp(6));
        pai.addView(d, lp);
    }

    private GradientDrawable fundoArredondado(int cor, int raio) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(cor);
        g.setCornerRadius(raio);
        return g;
    }

    private Drawable comToque(Drawable fundo) {
        return new RippleDrawable(ColorStateList.valueOf(0x33FFFFFF), fundo, null);
    }

    private ImageView icone(int res, int cor, int tamanhoDp) {
        ImageView i = new ImageView(this);
        i.setImageResource(res);
        i.setImageTintList(ColorStateList.valueOf(cor));
        i.setLayoutParams(new LinearLayout.LayoutParams(dp(tamanhoDp), dp(tamanhoDp)));
        return i;
    }

    /** Botão redondo só com ícone. */
    private View botaoIcone(int res, int fundo, int corIcone, String descricao, View.OnClickListener ao) {
        FrameLayout f = new FrameLayout(this);
        GradientDrawable c = new GradientDrawable();
        c.setShape(GradientDrawable.OVAL);
        c.setColor(fundo);
        f.setBackground(comToque(c));
        f.setContentDescription(descricao);
        f.setOnClickListener(ao);
        f.addView(icone(res, corIcone, 24), new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER));
        f.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
        return f;
    }

    private TextView botaoGrande(String s, int fundo, int corTxt, View.OnClickListener ao) {
        TextView b = texto(s, 15, corTxt, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(16), dp(14), dp(16), dp(14));
        b.setBackground(comToque(fundoArredondado(fundo, dp(26))));
        b.setOnClickListener(ao);
        return b;
    }

    private Switch chave(String titulo, String descricao, boolean on, LinearLayout pai) {
        LinearLayout linha = new LinearLayout(this);
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.setPadding(0, dp(6), 0, dp(6));
        LinearLayout txt = new LinearLayout(this);
        txt.setOrientation(LinearLayout.VERTICAL);
        txt.addView(texto(titulo, 16, corTexto, true));
        TextView d = texto(descricao, 13, corSub, false);
        d.setPadding(0, dp(2), 0, 0);
        txt.addView(d);
        linha.addView(txt, new LinearLayout.LayoutParams(0, -2, 1));
        Switch sw = new Switch(this);
        sw.setChecked(on);
        int[][] estados = {{android.R.attr.state_checked}, {}};
        sw.setThumbTintList(new ColorStateList(estados, new int[]{VERDE, escuro ? 0xFFB8C4BF : 0xFFFFFFFF}));
        sw.setTrackTintList(new ColorStateList(estados, new int[]{VERDE_ESCURO, escuro ? 0xFF3A4842 : 0xFFC5D0CB}));
        sw.setPadding(dp(12), 0, 0, 0);
        linha.addView(sw);
        linha.setOnClickListener(v -> sw.toggle());
        pai.addView(linha);
        return sw;
    }

    private EditText campo(String valor, boolean variasLinhas) {
        EditText e = new EditText(this);
        e.setText(valor);
        e.setTextColor(corTexto);
        e.setHintTextColor(corSub);
        e.setTextSize(16);
        e.setBackground(fundoArredondado(corCampo, dp(14)));
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        if (variasLinhas) {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            e.setMinLines(2);
            e.setGravity(Gravity.TOP | Gravity.START);
        }
        return e;
    }

    private interface AoMudar { void mudou(String s); }

    private TextWatcher salvar(AoMudar ao) {
        return new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) { ao.mudou(s.toString()); }
        };
    }

    private LinearLayout.LayoutParams margem(int e, int c, int d, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(e), dp(c), dp(d), dp(b));
        return lp;
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
