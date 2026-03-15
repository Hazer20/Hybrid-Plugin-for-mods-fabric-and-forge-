#pragma once

#include <Windows.h>

#include <array>
#include <atomic>
#include <cstdint>
#include <functional>
#include <memory>
#include <mutex>
#include <optional>
#include <shared_mutex>
#include <span>
#include <string>
#include <unordered_map>
#include <vector>

namespace avxemu {

using Address = std::uintptr_t;
using ByteVector = std::vector<std::uint8_t>;

enum class LogLevel {
  Trace,
  Debug,
  Info,
  Warn,
  Error,
};

}  // namespace avxemu
