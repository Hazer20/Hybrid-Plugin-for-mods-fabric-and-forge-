# DESquadCore

Многомодульное серверное ядро DESquadCore (PaperMC fork-направление) для Minecraft 1.21.8.

## Структура
- `desquad-api` — публичный API (items/npc/gui/economy/dialogue/models/blocks/mobs/modules/performance + events)
- `desquad-engine` — встроенные движки и in-memory реализации
- `desquad-server` — bootstrap, команды, конфиги, интеграция API
- `modules/*` — подключаемые модули (items, npc, economy, gui, blocks, mobs, performance)

## Patch 1.0.2
- Новый `PerformanceService` в API для async-heavy задач
- Новый runtime-модуль `performance`
- Пулы выполнения (`ticks`, `ai`, `items`, `resourcepack`) в `AsyncExecutionEngine`
- Команды производительности: `/desquad perf info`, `/desquad perf stress <count>`
- Расширенные настройки производительности в `desquad.yml` для CPU/RAM/GPU-offload политики

## Сборка
```bash
./gradlew clean build
```

Итоговый jar сервера (`:desquad-server:jar`): `DESquadCore-1.21.8.jar`.
