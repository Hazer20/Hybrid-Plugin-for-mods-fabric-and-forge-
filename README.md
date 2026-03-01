# Minecraft Pack Updater (1.20.4 -> 1.21.8)

Проект содержит Python-скрипт для переписывания datapack и resourcepack,
а также Windows-батник для удобного запуска без ручного ввода длинной команды.

## Быстрый запуск (Windows)

1. Запустите `run_update.bat`.
2. Укажите:
   - путь к папке старого датапака;
   - путь к папке старого ресурс-пака;
   - выходную папку проекта.
3. Батник проверит наличие папок и запустит:

```bash
python update_mc_pack.py --datapack <...> --resourcepack <...> --output <...> --backup --log
```

## Ручной запуск

```bash
python update_mc_pack.py --datapack old_datapack_path --resourcepack old_resourcepack_path --output output_path --backup --log
```

Дополнительно можно использовать `--dry-run` для проверки без записи изменений.
