# DESquadCore

DESquadCore — модульная серверная платформа (ядро + встроенные подсистемы) для экосистемы Minecraft 1.21.8 (Paper/Bukkit/Spigot API-совместимость).

## Важно
Этот репозиторий предоставляет самостоятельный **проект ядра DESquadCore** с интеграцией модулей и тик-рантайма.
Полноценный production-fork PaperMC как отдельный upstream-движок требует синхронизации с официальными Paper sources/patches и отдельного процесса сборки.

## Структура
- `desquad-api` — API (items/npc/gui/economy/dialogue/models/blocks/mobs/modules/performance/events)
- `desquad-engine` — engine-реализации + async execution + tick loop coordinator
- `desquad-server` — bootstrap, команды, ресурсы, конфиги
- `modules/*` — подключаемые модули (`items`, `mobs`, `npc`, `economy`, `gui`, `blocks`, `performance`)

## Patch 1.0.3
- Добавлен серверный координатор тик-цикла `DesquadTickLoop` + `ServerTickRuntime`
- Инициализация тик-подсистем кастомных блоков и кастомных мобов при старте
- Добавлен root-task `buildServerJar` для сборки итогового `DESquadCore-1.21.8.jar` в `build/libs`
- Расширен `/desquad info` диагностикой тик-подсистем
- Обновлен `desquad.yml` секцией `tick-loop`

## Сборка
```bash
./gradlew clean buildServerJar
```

Итоговый файл:
- `build/libs/DESquadCore-1.21.8.jar`

## Запуск
```bash
java -Xms2G -Xmx4G -XX:+UseG1GC -jar DESquadCore-1.21.8.jar nogui
```
