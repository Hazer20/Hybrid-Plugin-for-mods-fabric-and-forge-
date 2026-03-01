# HybridSecurityPrefix (Paper 1.21.8)

Плагин для `Paper 1.21.8`, который объединяет:

- собственный whitelist (без использования встроенного whitelist Paper);
- систему регистрации/входа по паролю;
- систему пользовательских префиксов + платный премиум-префикс;
- GUI-меню для управления префиксами.

## Что делает плагин

### 1) Custom whitelist
- При старте плагин отключает встроенный whitelist Paper (`Bukkit.setWhitelist(false)`).
- Проверка допуска выполняется в `AsyncPlayerPreLoginEvent` по файлу `plugins/HybridSecurityPrefix/whitelist.yml`.

Команды администратора:
- `/cwhitelist add <ник>`
- `/cwhitelist remove <ник>`
- `/cwhitelist list`
- `/cwhitelist reload`

Permission: `hybrid.whitelist.admin` (по умолчанию OP).

### 2) Регистрация и вход по паролю
- Если игрок заходит впервые: нужно `/register <пароль> <пароль>`.
- Для следующих входов: `/login <пароль>`.
- Пока игрок не авторизован — блокируются движение, чат и любые команды кроме `/register` и `/login`.
- Данные хранятся в `plugins/HybridSecurityPrefix/users.yml`.
- Пароль сохраняется в виде `salt + SHA-256 hash`.

### 3) Система префиксов
- Обычный пользовательский префикс: `/prefix set <текст>`.
- Премиум-префикс за валюту: `/prefix premium <текст>`.
- Сброс: `/prefix clear`.
- Меню: `/prefix menu` (или просто `/prefix`).

Данные префиксов: `plugins/HybridSecurityPrefix/prefixes.yml`.

### 4) Платные префиксы
- Для оплаты нужен `Vault` + любой economy-провайдер.
- Цена задается в `config.yml`:

```yml
prefix:
  max-length: 16
  premium-cost: 1000.0
```

Если Vault/экономика не подключены, премиум-команда сообщит об этом игроку.

## Сборка

```bash
mvn clean package
```

Готовый jar появится в `target/`.

## Установка

1. Скопируйте jar в папку `plugins` вашего Paper-сервера.
2. (Опционально) установите Vault + Economy-плагин для платных префиксов.
3. Запустите сервер.
4. Добавьте игроков в `cwhitelist`.

