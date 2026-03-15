# AVX Emulator (AVX/AVX2 → SSE4.2 Dynamic Binary Translator)

Проект реализует production-ориентированный каркас динамического бинарного переводчика для x86-64 Windows-игр: перехватывает `EXCEPTION_ILLEGAL_INSTRUCTION`, декодирует AVX/AVX2 инструкции через **Zydis**, транслирует в SSE последовательности и генерирует исполняемый машинный код через **asmjit**.

## Архитектура

```text
Game.exe
  ↓
loader.exe (CreateProcess + DLL Injection)
  ↓
avx_emulator.dll
  ↓
VEH (0xC000001D) + TranslationEngine
  ↓
InstructionDecoder (Zydis) + AVX→SSE Translator
  ↓
JitCodegen (asmjit) + CodeCache
  ↓
SSE runtime execution
```

### Ключевые подсистемы
- `ExceptionHandler`: устанавливает vectored exception handler и передаёт управление в переводчик.
- `TranslationEngine`: fast-path через `CodeCache`, block translation, hot-hit счётчики.
- `InstructionDecoder`: полное декодирование инструкций и операндов через Zydis.
- `AvxToSseTranslator`: таблица трансляций AVX/AVX2 → SSE4.2 (split 256-bit на 2×128-bit).
- `JitCodegen`: JIT-компиляция блока в исполняемый код.
- `MemoryManager`: executable-heap под JIT блоки.
- `ThreadManager`: thread-local статистика и подготовка к многопоточному сценарию.
- `CpuidHook`: интерфейс под подмену CPUID (AVX/AVX2 capability spoofing).

## Реализованные трансляции

### FLOAT
- `vaddps`, `vsubps`, `vmulps`, `vdivps`, `vmaxps`, `vminps`

### LOGIC
- `vandps`, `vorps`, `vxorps`

### MEMORY (register-to-register в текущей версии)
- `vmovaps`, `vmovups`, `vmovdqa`, `vmovdqu`

### INTEGER
- `vpaddd`, `vpsubd`, `vpmulld`

Принцип:
```asm
; AVX
vaddps ymm0, ymm1, ymm2

; SSE split
movaps xmm0, xmm1
movaps xmm8, xmm9
addps xmm0, xmm2
addps xmm8, xmm10
```

## Структура

- `CMakeLists.txt` — корневой build.
- `avx_emulator/CMakeLists.txt` — DLL + loader targets, FetchContent зависимостей.
- `avx_emulator/include/avx_emulator/*.h` — публичные интерфейсы.
- `avx_emulator/src/*.cpp` — реализации модулей.
- `avx_emulator/*.cpp` — compatibility wrappers с ожидаемыми именами файлов.

---

## Подробная инструкция

### 1) Установка зависимостей (Zydis и asmjit)

Вариант A (рекомендуется): через CMake FetchContent (уже настроено, вручную ставить не нужно).

Вариант B: вручную через vcpkg:
```powershell
vcpkg install zydis:x64-windows
vcpkg install asmjit:x64-windows
```
И затем передать toolchain в CMake (`-DCMAKE_TOOLCHAIN_FILE=.../vcpkg.cmake`).

### 2) Сборка проекта

```powershell
cmake -S . -B build -G "Visual Studio 17 2022" -A x64
cmake --build build --config Release
```

### 3) Сборка DLL

После команды выше DLL будет:

`build/avx_emulator/Release/avx_emulator.dll`

(точный путь может отличаться в зависимости от генератора).

### 4) Сборка loader.exe

Тот же build генерирует:

`build/avx_emulator/Release/loader.exe`

### 5) Внедрение DLL в игру

`loader.exe` делает это автоматически:
1. `CreateProcess(..., CREATE_SUSPENDED)`
2. `VirtualAllocEx + WriteProcessMemory` с путём DLL
3. `CreateRemoteThread(LoadLibraryW)`
4. `ResumeThread` у main-thread игры

### 6) Запуск игры через loader

```powershell
loader.exe "D:\Games\Game\Game.exe" "D:\Tools\avx_emulator.dll" "-arg1" "-arg2"
```

### 7) Включение debug-логов

Перед запуском установить переменную:
```powershell
set AVXEMU_DEBUG=1
```

Дополнительно:
```powershell
set AVXEMU_HOT_THRESHOLD=16
set AVXEMU_BLOCK_LEN=96
```

Лог-файл по умолчанию: `avx_emulator.log`.

### 8) Тестирование перевода AVX

1. Собрать небольшой тестовый бинарь с AVX-инструкциями (`/arch:AVX` или `/arch:AVX2`).
2. Запускать его через `loader.exe` на машине без AVX.
3. Проверить:
   - что процесс не падает на `0xC000001D`;
   - что лог фиксирует translate/cache-hit;
   - что результаты вычислений совпадают с эталоном.
4. Для микробенчей использовать блоки с высокой повторяемостью для проверки эффективности `CodeCache`.

### 9) Добавление новых AVX инструкций

1. Открыть `avx_emulator/src/avx_to_sse_translator.cpp`.
2. Добавить `case ZYDIS_MNEMONIC_...` в нужную группу (float/logic/int/memory).
3. Реализовать split-логику `YMM_low + YMM_high`.
4. Если инструкция затрагивает маскирование/перестановку/шанфлы — добавить специализированный emitter helper.
5. Добавить тест-кейс и профиль производительности.

### 10) Оптимизация производительности

- Увеличить размер code-cache (`codeCacheSize`) под реальные workload.
- Агрессивно использовать block translation (вместо поинструкционного fallback).
- Поддерживать больше memory-операндов без fallback.
- Ввести inline-cache на уровне RIP + сигнатуры байт.
- Добавить tiered translation (быстрый baseline + оптимизированный hot tier).
- Уменьшать exception overhead:
  - проставлять trampolines для hot sites,
  - избегать повторных VEH на уже известных адресах.
- Добавить lock striping или sharded-cache для многопоточной нагрузки.

## Важные ограничения текущего baseline

- CPUID spoofing в каркасе реализован как интерфейс и точка расширения; production-перехват CPUID в игре требует низкоуровневого trampolining/rewriter.
- Memory-формы некоторых `vmov*` пока ограничены регистровым путём и должны быть расширены для полного покрытия.
- Для строгой корректности необходимо полноценное сохранение/восстановление состояния SIMD-контекста и корректное продвижение `RIP` на длину всего translated-block.

## Лицензирование и этика

Использование reverse-engineering/инжекции должно соответствовать лицензии игры, локальному законодательству и anti-cheat требованиям.
