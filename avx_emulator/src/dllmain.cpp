#include "avx_emulator/config.h"
#include "avx_emulator/cpuid_hook.h"
#include "avx_emulator/exception_handler.h"
#include "avx_emulator/logger.h"
#include "avx_emulator/translation_engine.h"

BOOL APIENTRY DllMain(HMODULE moduleHandle, DWORD reason, LPVOID reserved) {
  (void)moduleHandle;
  (void)reserved;

  using namespace avxemu;
  if (reason == DLL_PROCESS_ATTACH) {
    DisableThreadLibraryCalls(moduleHandle);

    Config::Instance().ReloadFromEnvironment();
    const auto& cfg = Config::Instance().Get();
    Logger::Instance().Initialize(cfg.logFile, cfg.enableDebugLog);

    AVXEMU_LOG_INFO("AVX Emulator DLL attach");
    TranslationEngine::Instance().Initialize();
    ExceptionHandler::Instance().Install();
    if (cfg.enableCpuidSpoof) {
      CpuidHook::Instance().Install();
    }
  } else if (reason == DLL_PROCESS_DETACH) {
    AVXEMU_LOG_INFO("AVX Emulator DLL detach");
    CpuidHook::Instance().Uninstall();
    ExceptionHandler::Instance().Uninstall();
    Logger::Instance().Shutdown();
  }

  return TRUE;
}
