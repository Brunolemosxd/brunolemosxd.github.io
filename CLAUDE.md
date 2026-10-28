# OrçaOn

App de orçamentos e recibos para prestadores de serviço (pedreiro, eletricista, pintor, freteiro etc.).
Dono: Bruno. Idioma de tudo: português do Brasil. Explicar sempre em linguagem simples, passo a passo.

## Como o projeto é montado
- Site estático hospedado no GitHub Pages: https://brunolemosxd.github.io (repositório `brunolemosxd.github.io`, branch `main`).
- `index.html` é o app inteiro em UM arquivo (HTML + CSS + JS). Dados no `localStorage`; login Google, cotas e backup via Firebase (Auth + Firestore, SDK compat 10.14.1).
- Outros arquivos: `admin.html` (painel só do Bruno), `politica-privacidade.html`, `termos-de-uso.html`, `excluir-conta.html`, `manifest.json`, `sw.js`, ícones PNG.
- `capa-play-1024x500.png` é só material da Play Store: NÃO vai para o GitHub.
- PDF/imagem: html2canvas + jsPDF, com quebra de página inteligente. Compartilhamento usa `navigator.share` com painel alternativo (Samsung Internet e navegadores de apps não compartilham arquivo).

## Regras que não podem ser quebradas
1. O nome do app é SEMPRE "OrçaOn" em qualquer texto visível (telas, PDFs, páginas legais, loja, mensagens). Nunca escrever o nome antigo. Por dentro do código também usamos o nome novo: as chaves do localStorage começam com `orcaon_`. O primeiro `<script>` do `index.html` passa sozinho os dados antigos (`orcapro_`) para `orcaon_`. Esse trecho e a leitura de backups antigos (`/^orcapro_/` na restauração) precisam continuar existindo. O projeto Firebase antigo (`orcapro-a70b2`) será trocado por um projeto novo do OrçaOn.
2. Logotipo: "Orça" em branco + "On" em verde `#2BD67B`. Cor de destaque do app: amarelo `#F4C430`. Fundo: `#0B1C2C`.
3. Painel admin: mostrar apenas o CÓDIGO da conta. Nunca nome, e-mail, CPF, CNPJ ou RG de clientes. Admin = e-mail `brunolemosxd@gmail.com` verificado.
4. Segurança e Play Store: sem risco. Nada de código remoto, nada de coletar dados além do declarado, política de privacidade sempre alinhada com o que o app faz (backup Pro, voz, e-mail de sugestões).
5. Plano gratuito: 10 compartilhamentos de orçamento + 10 de recibo. Pro: R$ 7/mês ou R$ 50/ano. Verificação do Pro é no servidor (regras do Firestore, `ehPro()` lê o documento `quotas`), nunca só no celular.
6. Backup na nuvem: somente plano Pro (coleção `backups`), apagado junto com a conta.
7. Conta funciona em um celular por vez (documento `sessions`).
8. Textos para o cliente: sem termos técnicos. Botões e mensagens curtas.

## Estado atual (out/2026)
- Pronto: orçamentos, recibos (vários modelos), clientes (com CPF/CNPJ), perfil, Pix com validação e QR, débito, quantidades fracionadas, horas H:MM e dias, ditado por voz em campos de texto, backup Pro, selo de plano e dias restantes, conversão orçamento ⇄ recibo, saudação por horário.
- Pendente: cobrança do Pro (Google Play Billing + Cloud Function, exige plano Blaze do Firebase), empacotar como TWA e publicar na Play Store (aguardando D-U-N-S da D&B para conta de organização do MEI), número real do WhatsApp de suporte (hoje é um placeholder), restringir a chave de API do Firebase e domínios autorizados, possível IA de preenchimento (Gemini, só Pro, chave no servidor, com limite mensal).
- Firestore: regras no arquivo `firestore.rules` deste repositório. Elas precisam estar publicadas no projeto `orcaon-85c7e` (Firestore → Regras → Publicar); sem elas, o app não consegue contar os envios nem fazer backup.

## Como trabalhar
- Antes de mexer: ler o trecho do código envolvido. Depois de mexer: validar a sintaxe dos scripts e testar com Playwright (Firebase falso, sem rede).
- Não dá para testar aqui: PDF real, regras do Firestore, comportamento real do Samsung Internet. Avisar o Bruno quando algo não foi testado de verdade.
- Mudanças vão em branch/PR para o Bruno aprovar antes de entrar na `main` (o site atualiza sozinho em ~1 minuto).
- Ao entregar: dizer em poucas linhas o que mudou, o que NÃO foi testado e, se houver passo manual do Bruno (Firebase, Play Console), listar com link exato.
- Nome para a loja: usar "OrçaOn". Antes de publicar, conferir disponibilidade na Play Store, domínio e INPI.
