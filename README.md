# Minecraft Pack Updater (1.20.4 -> 1.21.8)

Проект содержит Python-скрипт для переписывания datapack и resourcepack,
а также Windows-батник для удобного запуска без ручного ввода длинной команды.

## Быстрый запуск (Windows)

1. Запустите `run_update.bat`.
2. При **первом запуске** батник автоматически создаст папки:
   - `packs_input/old_datapack`
   - `packs_input/old_resourcepack`
   - `mc_output`
   - `logs`
3. Положите ваш датапак в `packs_input/old_datapack`, а ресурс-пак в `packs_input/old_resourcepack`.
4. Запустите `run_update.bat` повторно.
5. Батник:
   - проверит пути и базовую структуру;
   - покажет понятные шаги выполнения (`STEP 1/4 ... STEP 4/4`);
   - запустит конвертацию с `--backup --log --ai-assist --ai-min-score 90`.

## ИИ-проверка качества

После конвертации можно включить AI-помощник (в батнике включен по умолчанию):

```bash
python update_mc_pack.py --datapack old_datapack_path --resourcepack old_resourcepack_path --output output_path --backup --log --ai-assist --ai-min-score 90
```

AI-помощник:
- делает дополнительный проход по файлам,
- пытается автоматически исправить остаточные устаревшие токены,
- считает score качества,
- пишет отчет `logs/ai_assurance_report.txt`.

## Логи и прогресс

- Прогресс батника пишется в: `logs/bat_progress.log`
- Логи Python-конвертера пишутся в:
  - `mc_output/logs/changes.log`
  - `mc_output/logs/errors.log`
  - `mc_output/logs/ai_assurance_report.txt` (если включен `--ai-assist`)

## Ручной запуск

```bash
python update_mc_pack.py --datapack old_datapack_path --resourcepack old_resourcepack_path --output output_path --backup --log
```

Дополнительно можно использовать `--dry-run` для проверки без записи изменений.


## Windows console encoding

`run_update.bat` now forces UTF-8 code page (`chcp 65001`) and uses clear ASCII/English prompts to avoid unreadable mojibake text in console/logs.
