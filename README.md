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
   - запустит полную конвертацию с `--backup --log`.

## Логи и прогресс

- Прогресс батника пишется в: `logs/bat_progress.log`
- Логи Python-конвертера пишутся в:
  - `mc_output/logs/changes.log`
  - `mc_output/logs/errors.log`

## Ручной запуск

```bash
python update_mc_pack.py --datapack old_datapack_path --resourcepack old_resourcepack_path --output output_path --backup --log
```

Дополнительно можно использовать `--dry-run` для проверки без записи изменений.
