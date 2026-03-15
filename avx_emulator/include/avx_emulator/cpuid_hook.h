#pragma once

#include "avx_emulator/common.h"

namespace avxemu {

class CpuidHook {
 public:
  static CpuidHook& Instance();

  bool Install();
  void Uninstall();

 private:
  CpuidHook() = default;
  std::atomic<bool> installed_{false};
};

}  // namespace avxemu
