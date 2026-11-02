# OrçaOn

App de orçamentos e recibos para prestadores de serviço (pedreiro, eletricista, pintor, freteiro etc.).
Dono: Bruno. Idioma de tudo: português do Brasil. Explicar sempre em linguagem simples, passo a passo.

## Como o projeto é montado
- Site estático hospedado no GitHub Pages: https://brunolemosxd.github.io (repositório `brunolemosxd.github.io`, branch `main`).
- `index.html` é o app inteiro em UM arquivo (HTML + CSS + JS). Dados no `localStorage`; login Google, cotas e backup via Firebase (Auth + Firestore, SDK compat 10.14.1).
- Dados separados por conta no mesmo celular: só a conta ativa fica nas chaves normais (`orcaon_*`, `assinaturaDigital`, `profession`); as outras ficam em `orcaon_cofre_<conta>` e `orcaon_dono_dados` diz de quem são as ativas. A troca acontece em `trocarDadosDaConta()` (no login/saída) e recarrega o app. Chave nova de dados do usuário: começar com `orcaon_`; preferência do aparelho: incluir na lista de exceções de `chaveDaConta()`.
- Outros arquivos: `admin.html` (painel só do Bruno), `politica-privacidade.html`, `termos-de-uso.html`, `excluir-conta.html`, `manifest.json`, `sw.js`, ícones PNG.
- `capa-play-1024x500.png` é só material da Play Store: NÃO vai para o GitHub.
- App Android (TWA) em `android/`: pacote `com.orcaon.app` (não muda nunca). A chave de assinatura (`orcaon-upload.jks` + senha) fica com o Bruno, NUNCA no GitHub. `/.well-known/assetlinks.json` tem a impressão digital da chave (sem ele aparece a barra de endereço) e `.nojekyll` faz o GitHub Pages publicar essa pasta. Ver `android/LEIA-ME.md`.
- PDF/imagem: html2canvas + jsPDF, com quebra de página inteligente. Compartilhamento usa `navigator.share` com painel alternativo (Samsung Internet e navegadores de apps não compartilham arquivo).

## Regras que não podem ser quebradas
1. O nome do app é SEMPRE "OrçaOn" em qualquer texto visível (telas, PDFs, páginas legais, loja, mensagens). Nunca escrever o nome antigo. Por dentro do código também usamos o nome novo: as chaves do localStorage começam com `orcaon_`. O primeiro `<script>` do `index.html` passa sozinho os dados antigos (`orcapro_`) para `orcaon_`. Esse trecho e a leitura de backups antigos (`/^orcapro_/` na restauração) precisam continuar existindo. O projeto Firebase antigo (`orcapro-a70b2`) será trocado por um projeto novo do OrçaOn.
2. Logotipo: "Orça" em branco + "On" em verde `#2BD67B`. Cor de destaque do app: amarelo `#F4C430`. Fundo: `#0B1C2C`.
3. Painel admin: mostrar apenas o CÓDIGO da conta. Nunca nome, e-mail, CPF, CNPJ ou RG de clientes. Admin = e-mail `brunolemosxd@gmail.com` verificado.
4. Segurança e Play Store: sem risco. Nada de código remoto, nada de coletar dados além do declarado, política de privacidade sempre alinhada com o que o app faz (backup Pro, voz, e-mail de sugestões).
5. Plano gratuito: 10 compartilhamentos de orçamento + 10 de recibo + 10 usos da calculadora de material (contador no aparelho, `orcaon_calc_usos`). Pro: R$ 10/mês ou R$ 60/ano (destaque de Black Friday no anual até 30/11/2026, classe `bf-promo` no `index.html`). Verificação do Pro é no servidor (regras do Firestore, `ehPro()` lê o documento `quotas`), nunca só no celular.
6. Backup na nuvem: somente plano Pro (coleção `backups`), apagado junto com a conta.
7. Conta funciona em um celular por vez (documento `sessions`).
8. Textos para o cliente: sem termos técnicos. Botões e mensagens curtas.

## Estado atual (nov/2026)
- Pronto: orçamentos, recibos (vários modelos), clientes (com CPF/CNPJ), perfil em seções que abrem ao tocar, Pix com validação e QR (com QR de exemplo no orçamento), débito, quantidades fracionadas, horas H:MM e dias, ditado por voz, backup Pro, selo de plano e dias restantes, conversão orçamento ⇄ recibo, saudação por horário.
- Feito em nov/2026: convite do Pro ao chegar nos 10 grátis; Meus preços (Perfil > Padrões dos orçamentos) com "Criar meu padrão"; calculadora de material com 17 cálculos por profissão (10 grátis, depois Pro); assinatura recortada rente ao traço, quadros travados ("Editar assinatura") e ajuste de posição por modelo; botão Instalar (PWA); dados separados por conta Google no mesmo celular; tela "Carregando…" no login; preços Pro R$ 10/R$ 60 com Black Friday até 30/11/2026; venda do Pro por Pix; botão Virar Pro balançando; compartilhar envia só o arquivo (Samsung recebia só texto); aviso @media prefers-color-scheme para o Samsung Internet não escurecer.
- Pendente: cobrança automática do Pro (Google Play Billing + Cloud Function, exige plano Blaze do Firebase), publicar na Play Store (aguardando D-U-N-S), restringir a chave de API do Firebase, possível IA de preenchimento (Gemini, só Pro), login com Facebook (exige mudar `googleIdOrca`, regras e política), apagar o projeto Firebase antigo `orcapro-a70b2` e o repositório `Orcapro` (hoje só redireciona para o app).
- Ainda não testado em aparelho real: login com Google no iPhone pelo Safari/Tela de Início; compartilhamento de PDF e cores no Samsung com o APK 1.0.3; compra de teste completa por Pix; valores padrão da calculadora (médias de mercado, confirmar com profissionais).
- D-U-N-S: pedido gratuito na CIAL Dun & Bradstreet em 26/10/2026 (prazo até 30 dias úteis, previsão ~8/12). Contato: Felipe Santos (CIAL). Empresa: MEI Bruno Lemos Webber Inacio, nome comercial FRETE LEMOS, CNPJ 44.226.530/0001-28. Atenção: o CNAE do MEI é de frete; confirmar com contador a venda de assinatura de app.
- Android (APK): versão atual 1.0.3 (versionCode 4). Abre sempre pelo Chrome quando instalado (`createTwaLauncher`). Chave e senha numa pasta privada do Google Drive do Bruno ("OrçaOn - Chave do app Android (NÃO APAGAR)"). Para gerar APK aqui, o ambiente precisa liberar `dl.google.com` (Network access > Allowed domains). No Brasil, desde 30/09/2026, APK de desenvolvedor não verificado instala por um caminho "avançado"; a verificação vem com a conta da Play Console (D-U-N-S).
- iPhone: não instala APK. Instalar pelo Safari > Compartilhar > Adicionar à Tela de Início. App Store exigiria conta Apple (US$ 99/ano, D-U-N-S), Mac e revisão (risco de recusa por ser site).
- Venda do Pro: por Pix (QR e copia e cola em `PIX_PRO`, imagens `pix-mensal.png`/`pix-anual.png`, chave CNPJ do MEI, Nubank PJ, recebedor Bruno Lemos Webber Inacio, cidade Capão da Canoa); o cliente manda o comprovante pelo WhatsApp de suporte (`WHATSAPP_SUPORTE` = 5551991213438) e o Bruno libera o Pro no painel admin (Dias de Pro: 30 mensal, 365 anual).
- Firestore: projeto `orcaon-85c7e`; regras no arquivo `firestore.rules` deste repositório. Elas precisam estar publicadas (Firestore → Regras → Publicar); sem elas, o app não consegue contar os envios nem fazer backup.
- Navegador da Samsung: não compartilha arquivo e escurece cores no modo escuro; por isso o APK prefere o Chrome. Pelo site, o painel de envio baixa o PDF e orienta anexar no WhatsApp.

## Como trabalhar
- Antes de mexer: ler o trecho do código envolvido. Depois de mexer: validar a sintaxe dos scripts e testar com Playwright (Firebase falso, sem rede).
- Não dá para testar aqui: PDF real, regras do Firestore, comportamento real do Samsung Internet. Avisar o Bruno quando algo não foi testado de verdade.
- Mudanças vão em branch/PR para o Bruno aprovar antes de entrar na `main` (o site atualiza sozinho em ~1 minuto).
- Ao entregar: dizer em poucas linhas o que mudou, o que NÃO foi testado e, se houver passo manual do Bruno (Firebase, Play Console), listar com link exato.
- Nome para a loja: usar "OrçaOn". Antes de publicar, conferir disponibilidade na Play Store, domínio e INPI.
