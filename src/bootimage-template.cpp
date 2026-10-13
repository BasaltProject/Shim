/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

const unsigned NAME(BootMask) = (~static_cast<unsigned>(0))
                                / NAME(BytesPerWord);

const unsigned NAME(BootShift) UNUSED = 32 - avian::util::log(NAME(BytesPerWord));

inline unsigned LABEL(codeMapSize)(unsigned codeSize)
{
  return avian::util::ceilingDivide(codeSize, TargetBitsPerWord)
         * TargetBytesPerWord;
}

inline unsigned LABEL(heapMapSize)(unsigned heapSize)
{
  return avian::util::ceilingDivide(heapSize,
                                    TargetBitsPerWord * TargetBytesPerWord)
         * TargetBytesPerWord;
}

inline object LABEL(bootObject)(LABEL(uintptr_t) * heap, unsigned offset)
{
  if (offset) {
    return reinterpret_cast<object>(heap + offset - 1);
  } else {
    return 0;
  }
}
