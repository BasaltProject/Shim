/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.smartcardio;

public class PCSCException extends Exception {
  private static final long serialVersionUID = 4181137171979130432L;

  final int code;

  PCSCException(int code) {
    super(toText(code));
    this.code = code;
  }

  PCSCException(String message) {
    super(message);
    this.code = -1;
  }

  private static final String[] kLow = {"F_INTERNAL_ERROR", "E_CANCELLED", "E_INVALID_HANDLE", "E_INVALID_PARAMETER",
      "E_INVALID_TARGET", "E_NO_MEMORY", "F_WAITED_TOO_LONG", "E_INSUFFICIENT_BUFFER", "E_UNKNOWN_READER", "E_TIMEOUT",
      "E_SHARING_VIOLATION", "E_NO_SMARTCARD", "E_UNKNOWN_CARD", "E_CANT_DISPOSE", "E_PROTO_MISMATCH", "E_NOT_READY",
      "E_INVALID_VALUE", "E_SYSTEM_CANCELLED", "F_COMM_ERROR", "F_UNKNOWN_ERROR", "E_INVALID_ATR", "E_NOT_TRANSACTED",
      "E_READER_UNAVAILABLE", "P_SHUTDOWN", "E_PCI_TOO_SMALL", "E_READER_UNSUPPORTED", "E_DUPLICATE_READER",
      "E_CARD_UNSUPPORTED", "E_NO_SERVICE", "E_SERVICE_STOPPED", "E_UNEXPECTED", "E_ICC_INSTALLATION",
      "E_ICC_CREATEORDER", "E_UNSUPPORTED_FEATURE", "E_DIR_NOT_FOUND", "E_FILE_NOT_FOUND", "E_NO_DIR", "E_NO_FILE",
      "E_NO_ACCESS", "E_WRITE_TOO_MANY", "E_BAD_SEEK", "E_INVALID_CHV", "E_UNKNOWN_RES_MNG", "E_NO_SUCH_CERTIFICATE",
      "E_CERTIFICATE_UNAVAILABLE", "E_NO_READERS_AVAILABLE", "E_COMM_DATA_LOST", "E_NO_KEY_CONTAINER",
      "E_SERVER_TOO_BUSY"};

  private static final String[] kWarn = {"W_UNSUPPORTED_CARD", "W_UNRESPONSIVE_CARD", "W_UNPOWERED_CARD",
      "W_RESET_CARD", "W_REMOVED_CARD", "W_SECURITY_VIOLATION", "W_WRONG_CHV", "W_CHV_BLOCKED", "W_EOF",
      "W_CANCELLED_BY_USER", "W_CARD_NOT_AUTHENTICATED"};

  private static String toText(int code) {
    int low = code - 0x80100001;
    if (low >= 0 && low < kLow.length) {
      return "SCARD_" + kLow[low];
    }
    int warn = code - 0x80100065;
    if (warn >= 0 && warn < kWarn.length) {
      return "SCARD_" + kWarn[warn];
    }
    return "Unknown error 0x" + Integer.toHexString(code);
  }
}
