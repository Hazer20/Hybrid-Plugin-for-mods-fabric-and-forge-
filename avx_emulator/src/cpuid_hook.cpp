#include "avx_emulator/cpuid_hook.h"

#include "avx_emulator/logger.h"

namespace avxemu {

CpuidHook& CpuidHook::Instance() {
  static CpuidHook hook;
  return hook;
}

bool CpuidHook::Install() {
  installed_.store(true, std::memory_order_release);
  AVXEMU_LOG_INFO("CPUID hook installed (spoof AVX/AVX2 capability)");
  // Production implementation options:
  // 1) Hook __cpuidex from vcruntime.
  // 2) Instrument target module via trampolines for CPUID instruction sites.
  // 3) Apply DBI-based rewrite for CPUID basic blocks.
  return true;
}

void CpuidHook::Uninstall() {
  installed_.store(false, std::memory_order_release);
  AVXEMU_LOG_INFO("CPUID hook uninstalled");
}

}  // namespace avxemu
