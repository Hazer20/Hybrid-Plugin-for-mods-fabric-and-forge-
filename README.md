# FracturedUniverse (Paper 1.21.8)

Гибридная система **плагин + datapack** для сезонного ARG-ивента:
**«Нарушение структуры вселенной»**.

Автор: **Hazer_2_0**

## Что реализовано
- Глобальный BossBar: **«Состояние вселенной: X%»**, скрыт до старта.
- Фазовая деградация стабильности мира (100 → 0).
- Замедленная деградация целостности: по умолчанию 1% каждые 1200 сек (настраивается в config).
- Разломы трёх типов: малый / великий / живой.
- Видимый портал-разлом (рамка), безопасные телепорты без лавы/опасных блоков, возврат обратно через разлом.
- Починка разлома командой `/universe fissure repair`.
- Нестабильность игрока после разломов + «Стабилизация организма».
- Портальные механики:
  - `/universe portals destroy`
  - `/universe portals wait`
- ARG система на **30 событий**:
  - `/universe arg start <номер>`
  - `/universe arg random`
  - `/universe arg stop`
  - `/universe arg list`
- Событие падения звезды.
- Пространственный коллапс чанков раз в **9 часов** (с восстановлением).
- Финал сезона:
  - `/universe final`
  - спавн босса **The Fractured Architect**
  - сообщение победы: «Структура реальности восстановлена».
- Datapack контент: sky/events/particles/portals/models.
- Автообнаружение кастомных моделей в `datapack/data/fractured_universe/models`.


## Память и запуск на ПК с 8 ГБ RAM (важно)
Если сервер падает с ошибкой нехватки **native memory** (не Java heap), не завышайте `-Xmx`.

Рекомендованный профиль для 8 ГБ RAM:
- `-Xms2G -Xmx4G`
- лимиты non-heap: `MaxMetaspaceSize=384M`, `MaxDirectMemorySize=512M`, `ReservedCodeCacheSize=256M`
- умеренный стек: `-Xss512k`

Готовые скрипты в репозитории:
- `run-paper-8gb.bat` (Windows)
- `run-paper-8gb.sh` (Linux)

Почему так: при слишком большом `-Xmx` (например 8G на системе с 8G RAM) JVM и Paper/Folia упираются в адресное пространство/нативную память (threads, direct buffers, metaspace, code cache), даже если heap не переполнен.

## Сборка
```bash
mvn clean package
```
Артефакт: `target/FracturedUniverse-1.0.jar`

## Установка
1. Скопируйте JAR в папку `plugins/`.
2. Запустите сервер, чтобы создалась папка `plugins/FracturedUniverse/`.
3. Скопируйте папку `src/main/resources/datapack` в `<ваш_мир>/datapacks/fractured_universe/`.
4. Выполните `/reload confirm` или перезапустите сервер.
5. Проверьте работу:
   - `/datapack list`
   - `/universe debug`

## Память плагина
- `миры.предзагрузка_на_старте: false` (по умолчанию) — не грузит кастомные миры заранее, чтобы снизить пиковое потребление памяти.

## Базовые команды
- `/universe start`
- `/universe stop`
- `/universe phase <0-4>`
- `/universe starfall`
- `/universe fissure <small|great|living|disable|enable|repair>`
- `/universe portals <destroy|wait>`
- `/universe arg <start|random|stop|list>`
- `/universe final`
- `/universe lore add <текст>`

## Структура datapack
```text
data/fractured_universe
  functions/
  structures/
  particles/
  events/
  bosses/
  sky/
  models/

assets/fractured_universe
  textures/sky/
  textures/block/
  textures/item/
  models/block/
  models/item/
```
