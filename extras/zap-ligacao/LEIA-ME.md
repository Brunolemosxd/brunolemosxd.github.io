# Zap da Ligação (teste)

App Android separado do OrçaOn. Quando uma ligação termina, aparece uma notificação
"Mandar WhatsApp?" que abre a conversa com o número (mesmo sem ele estar salvo).
A tela do app também lista as ligações recentes com um botão de WhatsApp.

- Pacote: `com.brunolemos.zapligacao`
- Permissões: estado do telefone, histórico de ligações e notificações. Nada sai do celular.
- Não vai para a Play Store do jeito que está: a Google só libera o histórico de ligações para apps de telefone.

## Gerar o APK
Android SDK (platform 36) e Gradle 8.14: `gradle assembleDebug` nesta pasta.
Saída: `app/build/outputs/apk/debug/app-debug.apk`.
