#include "avx_emulator/logger.h"

#include <chrono>
#include <iomanip>
#include <iostream>
#include <sstream>

namespace avxemu {

namespace {
const char* LevelToString(LogLevel level) {
  switch (level) {
    case LogLevel::Trace:
      return "TRACE";
    case LogLevel::Debug:
      return "DEBUG";
    case LogLevel::Info:
      return "INFO";
    case LogLevel::Warn:
      return "WARN";
    case LogLevel::Error:
      return "ERROR";
  }
  return "UNKNOWN";
}
}  // namespace

Logger& Logger::Instance() {
  static Logger logger;
  return logger;
}

void Logger::Initialize(std::wstring_view filePath, bool debugMode) {
  std::scoped_lock lock(mutex_);
  debugMode_ = debugMode;
  file_.open(std::string(filePath.begin(), filePath.end()), std::ios::out | std::ios::trunc);
}

void Logger::Shutdown() {
  std::scoped_lock lock(mutex_);
  if (file_.is_open()) {
    file_.flush();
    file_.close();
  }
}

void Logger::Log(LogLevel level, std::string_view message) {
  if (level == LogLevel::Debug && !debugMode_) {
    return;
  }

  const auto now = std::chrono::system_clock::now();
  const auto tt = std::chrono::system_clock::to_time_t(now);
  std::tm tm{};
  localtime_s(&tm, &tt);

  std::ostringstream line;
  line << std::put_time(&tm, "%H:%M:%S") << " [" << LevelToString(level) << "] " << message << "\n";

  std::scoped_lock lock(mutex_);
  std::cout << line.str();
  if (file_.is_open()) {
    file_ << line.str();
    file_.flush();
  }
}

}  // namespace avxemu
