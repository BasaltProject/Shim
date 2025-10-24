/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.datatype;

import java.text.MessageFormat;

final class DatatypeMessages {
  private DatatypeMessages() {
  }

  static String invalidField(Object value, String field) {
    return MessageFormat.format("Invalid value {0} for {1} field.", value, field);
  }

  static String invalidFraction(Object value) {
    return MessageFormat.format("Invalid value {0} for fractional second.", value);
  }

  static String invalidLexical(String lexical) {
    return "\"" + lexical + "\" is not a valid representation of an XML Gregorian Calendar value.";
  }

  static String invalidFields(Object[] values) {
    return MessageFormat.format("Year = {0}, Month = {1}, Day = {2}, Hour = {3}, Minute = {4}, Second = {5}, "
        + "fractionalSecond = {6}, Timezone = {7} , is not a valid representation of an XML Gregorian Calendar value.",
        values);
  }
}
