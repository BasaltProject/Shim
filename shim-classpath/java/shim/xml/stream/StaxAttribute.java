/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

final class StaxAttribute {
  String uri;
  String local;
  String prefix = "";
  String value;
  String type = "CDATA";
  boolean specified = true;
}
