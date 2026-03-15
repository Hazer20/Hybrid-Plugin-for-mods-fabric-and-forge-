#include "avx_emulator/exception_handler.h"

#include "avx_emulator/logger.h"
#include "avx_emulator/translation_engine.h"

namespace avxemu {

ExceptionHandler& ExceptionHandler::Instance() {
  static ExceptionHandler handler;
  return handler;
}

LONG CALLBACK ExceptionHandler::VectoredHandler(EXCEPTION_POINTERS* exceptionInfo) {
  if (!exceptionInfo || !exceptionInfo->ExceptionRecord || !exceptionInfo->ContextRecord) {
    return EXCEPTION_CONTINUE_SEARCH;
  }

  if (exceptionInfo->ExceptionRecord->ExceptionCode != EXCEPTION_ILLEGAL_INSTRUCTION) {
    return EXCEPTION_CONTINUE_SEARCH;
  }

  if (TranslationEngine::Instance().HandleIllegalInstruction(exceptionInfo->ContextRecord)) {
    return EXCEPTION_CONTINUE_EXECUTION;
  }

  return EXCEPTION_CONTINUE_SEARCH;
}

bool ExceptionHandler::Install() {
  if (handle_) {
    return true;
  }

  handle_ = AddVectoredExceptionHandler(1, &ExceptionHandler::VectoredHandler);
  if (!handle_) {
    AVXEMU_LOG_ERROR("Failed to install vectored exception handler");
    return false;
  }

  AVXEMU_LOG_INFO("Vectored exception handler installed");
  return true;
}

void ExceptionHandler::Uninstall() {
  if (!handle_) {
    return;
  }

  RemoveVectoredExceptionHandler(handle_);
  handle_ = nullptr;
  AVXEMU_LOG_INFO("Vectored exception handler removed");
}

}  // namespace avxemu
