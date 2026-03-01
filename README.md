# HybridSecurityPrefix (Paper 1.21.8)

Плагин для `Paper 1.21.8`, который объединяет:

- собственный whitelist (без использования встроенного whitelist Paper);
- систему регистрации/входа по паролю;
- систему пользовательских префиксов + премиум-префикс по праву доступа;
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
- До авторизации игрок неуязвим.
- После успешного входа/регистрации игрок получает эффекты и звуки подтверждения.
- Данные хранятся в `plugins/HybridSecurityPrefix/users.yml`.
- Пароль сохраняется в виде `salt + SHA-256 hash`.

### 3) Система префиксов
- Обычный пользовательский префикс: `/prefix set <текст>`.
- Премиум-префикс: `/prefix premium <текст>`.
- Сброс: `/prefix clear`.
- Меню: `/prefix menu` (или просто `/prefix`).

Данные префиксов: `plugins/HybridSecurityPrefix/prefixes.yml`.

### 4) Премиум-префикс без Vault
- Зависимость от Vault удалена, чтобы сборка проходила без внешнего VaultAPI.
- Доступ к премиум-префиксу выдается правом:
  - `hybrid.prefix.premium`
- Рекомендуемый сценарий «за плату»: продавать/выдавать это право через ваш донат-магазин и permission-плагин.

## Настройки

```yml
prefix:
  max-length: 16
  premium-cost: 1000.0
```

> `premium-cost` оставлен для совместимости, сейчас напрямую не используется без экономики.

## Сборка

```bash
mvn clean package
```

Готовый jar появится в `target/`.

## Установка

1. Скопируйте jar в папку `plugins` вашего Paper-сервера.
2. Запустите сервер.
3. Добавьте игроков в `cwhitelist`.
4. Выдавайте `hybrid.prefix.premium` тем, кому нужен премиум-префикс.
