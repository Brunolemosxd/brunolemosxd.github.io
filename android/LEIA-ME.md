# OrçaOn para Android (TWA)

App Android que abre https://brunolemosxd.github.io em tela cheia (Trusted Web Activity).
Pacote: `com.orcaon.app` (não pode mudar nunca).

## Gerar o APK/AAB
1. Android SDK (platform 36, build-tools 36) e Gradle 8.14.
2. Crie `android/keystore.properties` (NÃO vai para o GitHub):
   ```
   storeFile=/caminho/orcaon-upload.jks
   storePassword=...
   keyAlias=orcaon
   keyPassword=...
   ```
3. `gradle assembleRelease bundleRelease` dentro de `android/`.
   Saída: `app/build/outputs/apk/release/app-release.apk` e `app/build/outputs/bundle/release/app-release.aab`.

## Barra de endereço escondida
O arquivo `/.well-known/assetlinks.json` do site precisa ter a impressão digital (SHA-256) da chave.
Se a Play Store assinar o app com a chave dela (Play App Signing), adicionar também a impressão digital dela nesse arquivo.
O arquivo `.nojekyll` na raiz é necessário para o GitHub Pages publicar a pasta `.well-known`.

## Versões
A cada nova versão da "casca", aumentar `versionCode` (e `versionName`) em `app/build.gradle`.
O conteúdo do app vem do site, então mudanças no `index.html` não precisam de APK novo.
