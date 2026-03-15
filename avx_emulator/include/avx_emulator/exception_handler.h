#pragma once

#include "avx_emulator/common.h"

namespace avxemu {

class ExceptionHandler {
 public:
  static ExceptionHandler& Instance();

  bool Install();
  void Uninstall();

 private:
  static LONG CALLBACK VectoredHandler(EXCEPTION_POINTERS* exceptionInfo);
  PVOID handle_ = nullptr;
};

}  // namespace avxemu
