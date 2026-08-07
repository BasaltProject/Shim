/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class MediaName extends javax.print.attribute.standard.Media implements javax.print.attribute.Attribute {
  public static final javax.print.attribute.standard.MediaName NA_LETTER_WHITE = new javax.print.attribute.standard.MediaName(0);
  public static final javax.print.attribute.standard.MediaName NA_LETTER_TRANSPARENT = new javax.print.attribute.standard.MediaName(1);
  public static final javax.print.attribute.standard.MediaName ISO_A4_WHITE = new javax.print.attribute.standard.MediaName(2);
  public static final javax.print.attribute.standard.MediaName ISO_A4_TRANSPARENT = new javax.print.attribute.standard.MediaName(3);
  protected MediaName(int p0) {
    super(p0);
  }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"na-letter-white", "na-letter-transparent", "iso-a4-white", "iso-a4-transparent"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {NA_LETTER_WHITE, NA_LETTER_TRANSPARENT, ISO_A4_WHITE, ISO_A4_TRANSPARENT};
}
