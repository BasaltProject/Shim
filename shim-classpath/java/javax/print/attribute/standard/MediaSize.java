/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class MediaSize extends javax.print.attribute.Size2DSyntax implements javax.print.attribute.Attribute {
  public MediaSize(int p0, int p1, int p2, javax.print.attribute.standard.MediaSizeName p3) {
    super(p0, p1, p2);
    mediaName = p3;
    register();
  }
  public MediaSize(float p0, float p1, int p2, javax.print.attribute.standard.MediaSizeName p3) {
    super(p0, p1, p2);
    mediaName = p3;
    register();
  }
  public MediaSize(int p0, int p1, int p2) {
    super(p0, p1, p2);
    register();
  }
  public MediaSize(float p0, float p1, int p2) {
    super(p0, p1, p2);
    register();
  }
  public final java.lang.String getName() { return "media-size"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof MediaSize && ((MediaSize) p0).mediaName == mediaName; }
  public static javax.print.attribute.standard.MediaSize getMediaSizeForName(javax.print.attribute.standard.MediaSizeName p0) { return (MediaSize) mediaMap.get(p0); }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return MediaSize.class; }
  public javax.print.attribute.standard.MediaSizeName getMediaSizeName() { return mediaName; }
  public static javax.print.attribute.standard.MediaSizeName findMedia(float p0, float p1, int p2) { MediaSizeName match = MediaSizeName.ISO_A4;
if (p0 <= 0.0f || p1 <= 0.0f || p2 < 1) {
  throw new IllegalArgumentException("args must be +ve values");
}
double ls = p0 * p0 + p1 * p1;
for (int i = 0; i < sizeVector.size(); i++) {
  MediaSize mediaSize = sizeVector.get(i);
  float[] dim = mediaSize.getSize(p2);
  if (p0 == dim[0] && p1 == dim[1]) {
    match = mediaSize.getMediaSizeName();
    break;
  }
  double diffx = p0 - dim[0];
  double diffy = p1 - dim[1];
  double tmp = diffx * diffx + diffy * diffy;
  if (tmp < ls) {
    ls = tmp;
    match = mediaSize.getMediaSizeName();
  }
}
return match; }

  private javax.print.attribute.standard.MediaSizeName mediaName;
  private static final java.util.HashMap<Object, Object> mediaMap = new java.util.HashMap<Object, Object>();
  private static final java.util.Vector<MediaSize> sizeVector = new java.util.Vector<MediaSize>();

  private void register() {
    if (mediaName != null) {
      synchronized (MediaSize.class) {
        if (mediaMap.get(mediaName) == null) {
          mediaMap.put(mediaName, this);
        }
      }
    }
    synchronized (MediaSize.class) {
      sizeVector.add(this);
    }
  }

  static {
    Object forceInit = ISO.A4;
    forceInit = JIS.B5;
    forceInit = NA.LETTER;
    forceInit = Engineering.A;
    forceInit = Other.EXECUTIVE;
  }

  public static final class ISO {
    public static final javax.print.attribute.standard.MediaSize A0 = new javax.print.attribute.standard.MediaSize(841000, 1189000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A0);
    public static final javax.print.attribute.standard.MediaSize A1 = new javax.print.attribute.standard.MediaSize(594000, 841000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A1);
    public static final javax.print.attribute.standard.MediaSize A2 = new javax.print.attribute.standard.MediaSize(420000, 594000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A2);
    public static final javax.print.attribute.standard.MediaSize A3 = new javax.print.attribute.standard.MediaSize(297000, 420000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A3);
    public static final javax.print.attribute.standard.MediaSize A4 = new javax.print.attribute.standard.MediaSize(210000, 297000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A4);
    public static final javax.print.attribute.standard.MediaSize A5 = new javax.print.attribute.standard.MediaSize(148000, 210000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A5);
    public static final javax.print.attribute.standard.MediaSize A6 = new javax.print.attribute.standard.MediaSize(105000, 148000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A6);
    public static final javax.print.attribute.standard.MediaSize A7 = new javax.print.attribute.standard.MediaSize(74000, 105000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A7);
    public static final javax.print.attribute.standard.MediaSize A8 = new javax.print.attribute.standard.MediaSize(52000, 74000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A8);
    public static final javax.print.attribute.standard.MediaSize A9 = new javax.print.attribute.standard.MediaSize(37000, 52000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A9);
    public static final javax.print.attribute.standard.MediaSize A10 = new javax.print.attribute.standard.MediaSize(26000, 37000, 1, javax.print.attribute.standard.MediaSizeName.ISO_A10);
    public static final javax.print.attribute.standard.MediaSize B0 = new javax.print.attribute.standard.MediaSize(1000000, 1414000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B0);
    public static final javax.print.attribute.standard.MediaSize B1 = new javax.print.attribute.standard.MediaSize(707000, 1000000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B1);
    public static final javax.print.attribute.standard.MediaSize B2 = new javax.print.attribute.standard.MediaSize(500000, 707000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B2);
    public static final javax.print.attribute.standard.MediaSize B3 = new javax.print.attribute.standard.MediaSize(353000, 500000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B3);
    public static final javax.print.attribute.standard.MediaSize B4 = new javax.print.attribute.standard.MediaSize(250000, 353000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B4);
    public static final javax.print.attribute.standard.MediaSize B5 = new javax.print.attribute.standard.MediaSize(176000, 250000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B5);
    public static final javax.print.attribute.standard.MediaSize B6 = new javax.print.attribute.standard.MediaSize(125000, 176000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B6);
    public static final javax.print.attribute.standard.MediaSize B7 = new javax.print.attribute.standard.MediaSize(88000, 125000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B7);
    public static final javax.print.attribute.standard.MediaSize B8 = new javax.print.attribute.standard.MediaSize(62000, 88000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B8);
    public static final javax.print.attribute.standard.MediaSize B9 = new javax.print.attribute.standard.MediaSize(44000, 62000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B9);
    public static final javax.print.attribute.standard.MediaSize B10 = new javax.print.attribute.standard.MediaSize(31000, 44000, 1, javax.print.attribute.standard.MediaSizeName.ISO_B10);
    public static final javax.print.attribute.standard.MediaSize C3 = new javax.print.attribute.standard.MediaSize(324000, 458000, 1, javax.print.attribute.standard.MediaSizeName.ISO_C3);
    public static final javax.print.attribute.standard.MediaSize C4 = new javax.print.attribute.standard.MediaSize(229000, 324000, 1, javax.print.attribute.standard.MediaSizeName.ISO_C4);
    public static final javax.print.attribute.standard.MediaSize C5 = new javax.print.attribute.standard.MediaSize(162000, 229000, 1, javax.print.attribute.standard.MediaSizeName.ISO_C5);
    public static final javax.print.attribute.standard.MediaSize C6 = new javax.print.attribute.standard.MediaSize(114000, 162000, 1, javax.print.attribute.standard.MediaSizeName.ISO_C6);
    public static final javax.print.attribute.standard.MediaSize DESIGNATED_LONG = new javax.print.attribute.standard.MediaSize(110000, 220000, 1, javax.print.attribute.standard.MediaSizeName.ISO_DESIGNATED_LONG);
    ISO() {
    }
  }

  public static final class JIS {
    public static final javax.print.attribute.standard.MediaSize B0 = new javax.print.attribute.standard.MediaSize(1030000, 1456000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B0);
    public static final javax.print.attribute.standard.MediaSize B1 = new javax.print.attribute.standard.MediaSize(728000, 1030000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B1);
    public static final javax.print.attribute.standard.MediaSize B2 = new javax.print.attribute.standard.MediaSize(515000, 728000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B2);
    public static final javax.print.attribute.standard.MediaSize B3 = new javax.print.attribute.standard.MediaSize(364000, 515000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B3);
    public static final javax.print.attribute.standard.MediaSize B4 = new javax.print.attribute.standard.MediaSize(257000, 364000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B4);
    public static final javax.print.attribute.standard.MediaSize B5 = new javax.print.attribute.standard.MediaSize(182000, 257000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B5);
    public static final javax.print.attribute.standard.MediaSize B6 = new javax.print.attribute.standard.MediaSize(128000, 182000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B6);
    public static final javax.print.attribute.standard.MediaSize B7 = new javax.print.attribute.standard.MediaSize(91000, 128000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B7);
    public static final javax.print.attribute.standard.MediaSize B8 = new javax.print.attribute.standard.MediaSize(64000, 91000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B8);
    public static final javax.print.attribute.standard.MediaSize B9 = new javax.print.attribute.standard.MediaSize(45000, 64000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B9);
    public static final javax.print.attribute.standard.MediaSize B10 = new javax.print.attribute.standard.MediaSize(32000, 45000, 1, javax.print.attribute.standard.MediaSizeName.JIS_B10);
    public static final javax.print.attribute.standard.MediaSize CHOU_1 = new javax.print.attribute.standard.MediaSize(142000, 332000, 1, null);
    public static final javax.print.attribute.standard.MediaSize CHOU_2 = new javax.print.attribute.standard.MediaSize(119000, 277000, 1, null);
    public static final javax.print.attribute.standard.MediaSize CHOU_3 = new javax.print.attribute.standard.MediaSize(120000, 235000, 1, null);
    public static final javax.print.attribute.standard.MediaSize CHOU_4 = new javax.print.attribute.standard.MediaSize(90000, 205000, 1, null);
    public static final javax.print.attribute.standard.MediaSize CHOU_30 = new javax.print.attribute.standard.MediaSize(92000, 235000, 1, null);
    public static final javax.print.attribute.standard.MediaSize CHOU_40 = new javax.print.attribute.standard.MediaSize(90000, 225000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_0 = new javax.print.attribute.standard.MediaSize(287000, 382000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_1 = new javax.print.attribute.standard.MediaSize(270000, 382000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_2 = new javax.print.attribute.standard.MediaSize(240000, 332000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_3 = new javax.print.attribute.standard.MediaSize(216000, 277000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_4 = new javax.print.attribute.standard.MediaSize(197000, 267000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_5 = new javax.print.attribute.standard.MediaSize(190000, 240000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_6 = new javax.print.attribute.standard.MediaSize(162000, 229000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_7 = new javax.print.attribute.standard.MediaSize(142000, 205000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_8 = new javax.print.attribute.standard.MediaSize(119000, 197000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_20 = new javax.print.attribute.standard.MediaSize(229000, 324000, 1, null);
    public static final javax.print.attribute.standard.MediaSize KAKU_A4 = new javax.print.attribute.standard.MediaSize(228000, 312000, 1, null);
    public static final javax.print.attribute.standard.MediaSize YOU_1 = new javax.print.attribute.standard.MediaSize(120000, 176000, 1, null);
    public static final javax.print.attribute.standard.MediaSize YOU_2 = new javax.print.attribute.standard.MediaSize(114000, 162000, 1, null);
    public static final javax.print.attribute.standard.MediaSize YOU_3 = new javax.print.attribute.standard.MediaSize(98000, 148000, 1, null);
    public static final javax.print.attribute.standard.MediaSize YOU_4 = new javax.print.attribute.standard.MediaSize(105000, 235000, 1, null);
    public static final javax.print.attribute.standard.MediaSize YOU_5 = new javax.print.attribute.standard.MediaSize(95000, 217000, 1, null);
    public static final javax.print.attribute.standard.MediaSize YOU_6 = new javax.print.attribute.standard.MediaSize(98000, 190000, 1, null);
    public static final javax.print.attribute.standard.MediaSize YOU_7 = new javax.print.attribute.standard.MediaSize(92000, 165000, 1, null);
    JIS() {
    }
  }

  public static final class NA {
    public static final javax.print.attribute.standard.MediaSize LETTER = new javax.print.attribute.standard.MediaSize(215900, 279400, 1, javax.print.attribute.standard.MediaSizeName.NA_LETTER);
    public static final javax.print.attribute.standard.MediaSize LEGAL = new javax.print.attribute.standard.MediaSize(215900, 355600, 1, javax.print.attribute.standard.MediaSizeName.NA_LEGAL);
    public static final javax.print.attribute.standard.MediaSize NA_5X7 = new javax.print.attribute.standard.MediaSize(127000, 177800, 1, javax.print.attribute.standard.MediaSizeName.NA_5X7);
    public static final javax.print.attribute.standard.MediaSize NA_8X10 = new javax.print.attribute.standard.MediaSize(203200, 254000, 1, javax.print.attribute.standard.MediaSizeName.NA_8X10);
    public static final javax.print.attribute.standard.MediaSize NA_NUMBER_9_ENVELOPE = new javax.print.attribute.standard.MediaSize(98425, 225425, 1, javax.print.attribute.standard.MediaSizeName.NA_NUMBER_9_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_NUMBER_10_ENVELOPE = new javax.print.attribute.standard.MediaSize(104775, 241300, 1, javax.print.attribute.standard.MediaSizeName.NA_NUMBER_10_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_NUMBER_11_ENVELOPE = new javax.print.attribute.standard.MediaSize(114300, 263525, 1, javax.print.attribute.standard.MediaSizeName.NA_NUMBER_11_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_NUMBER_12_ENVELOPE = new javax.print.attribute.standard.MediaSize(120650, 279400, 1, javax.print.attribute.standard.MediaSizeName.NA_NUMBER_12_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_NUMBER_14_ENVELOPE = new javax.print.attribute.standard.MediaSize(127000, 292100, 1, javax.print.attribute.standard.MediaSizeName.NA_NUMBER_14_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_6X9_ENVELOPE = new javax.print.attribute.standard.MediaSize(152400, 228600, 1, javax.print.attribute.standard.MediaSizeName.NA_6X9_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_7X9_ENVELOPE = new javax.print.attribute.standard.MediaSize(177800, 228600, 1, javax.print.attribute.standard.MediaSizeName.NA_7X9_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_9x11_ENVELOPE = new javax.print.attribute.standard.MediaSize(228600, 279400, 1, javax.print.attribute.standard.MediaSizeName.NA_9X11_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_9x12_ENVELOPE = new javax.print.attribute.standard.MediaSize(228600, 304800, 1, javax.print.attribute.standard.MediaSizeName.NA_9X12_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_10x13_ENVELOPE = new javax.print.attribute.standard.MediaSize(254000, 330200, 1, javax.print.attribute.standard.MediaSizeName.NA_10X13_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_10x14_ENVELOPE = new javax.print.attribute.standard.MediaSize(254000, 355600, 1, javax.print.attribute.standard.MediaSizeName.NA_10X14_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize NA_10X15_ENVELOPE = new javax.print.attribute.standard.MediaSize(254000, 381000, 1, javax.print.attribute.standard.MediaSizeName.NA_10X15_ENVELOPE);
    NA() {
    }
  }

  public static final class Engineering {
    public static final javax.print.attribute.standard.MediaSize A = new javax.print.attribute.standard.MediaSize(215900, 279400, 1, javax.print.attribute.standard.MediaSizeName.A);
    public static final javax.print.attribute.standard.MediaSize B = new javax.print.attribute.standard.MediaSize(279400, 431800, 1, javax.print.attribute.standard.MediaSizeName.B);
    public static final javax.print.attribute.standard.MediaSize C = new javax.print.attribute.standard.MediaSize(431800, 558800, 1, javax.print.attribute.standard.MediaSizeName.C);
    public static final javax.print.attribute.standard.MediaSize D = new javax.print.attribute.standard.MediaSize(558800, 863600, 1, javax.print.attribute.standard.MediaSizeName.D);
    public static final javax.print.attribute.standard.MediaSize E = new javax.print.attribute.standard.MediaSize(863600, 1117600, 1, javax.print.attribute.standard.MediaSizeName.E);
    Engineering() {
    }
  }

  public static final class Other {
    public static final javax.print.attribute.standard.MediaSize EXECUTIVE = new javax.print.attribute.standard.MediaSize(184150, 266700, 1, javax.print.attribute.standard.MediaSizeName.EXECUTIVE);
    public static final javax.print.attribute.standard.MediaSize LEDGER = new javax.print.attribute.standard.MediaSize(279400, 431800, 1, javax.print.attribute.standard.MediaSizeName.LEDGER);
    public static final javax.print.attribute.standard.MediaSize TABLOID = new javax.print.attribute.standard.MediaSize(279400, 431800, 1, javax.print.attribute.standard.MediaSizeName.TABLOID);
    public static final javax.print.attribute.standard.MediaSize INVOICE = new javax.print.attribute.standard.MediaSize(139700, 215900, 1, javax.print.attribute.standard.MediaSizeName.INVOICE);
    public static final javax.print.attribute.standard.MediaSize FOLIO = new javax.print.attribute.standard.MediaSize(215900, 330200, 1, javax.print.attribute.standard.MediaSizeName.FOLIO);
    public static final javax.print.attribute.standard.MediaSize QUARTO = new javax.print.attribute.standard.MediaSize(215900, 275082, 1, javax.print.attribute.standard.MediaSizeName.QUARTO);
    public static final javax.print.attribute.standard.MediaSize ITALY_ENVELOPE = new javax.print.attribute.standard.MediaSize(110000, 230000, 1, javax.print.attribute.standard.MediaSizeName.ITALY_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize MONARCH_ENVELOPE = new javax.print.attribute.standard.MediaSize(98298, 190500, 1, javax.print.attribute.standard.MediaSizeName.MONARCH_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize PERSONAL_ENVELOPE = new javax.print.attribute.standard.MediaSize(92075, 165100, 1, javax.print.attribute.standard.MediaSizeName.PERSONAL_ENVELOPE);
    public static final javax.print.attribute.standard.MediaSize JAPANESE_POSTCARD = new javax.print.attribute.standard.MediaSize(100000, 148000, 1, javax.print.attribute.standard.MediaSizeName.JAPANESE_POSTCARD);
    public static final javax.print.attribute.standard.MediaSize JAPANESE_DOUBLE_POSTCARD = new javax.print.attribute.standard.MediaSize(148000, 200000, 1, javax.print.attribute.standard.MediaSizeName.JAPANESE_DOUBLE_POSTCARD);
    Other() {
    }
  }
}
