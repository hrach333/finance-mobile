#!/bin/bash
# Скрипт для запуска pepk.jar с автоматическим вводом паролей

# Пароль keystore
STORE_PASS="finance123"

# Запускаем pepk.jar и передаём пароль через stdin
(echo "$STORE_PASS"; echo "$STORE_PASS") | java -jar pepk.jar \
  --keystore financeapp-release-key.keystore \
  --alias financeapp-key \
  --output pepk_out.zip \
  --encryptionkey=00008cece6539f51c15a88446649bc0ba31f62a783f9d79c0f41bdbd270beb4045fbb584d4292f86d2e5bfa66aa1a7a9d67f167bee4077d037feb2c720fa1cb34e21dcd1 \
  --include-cert

echo "✅ ZIP архив успешно создан: pepk_out.zip"
