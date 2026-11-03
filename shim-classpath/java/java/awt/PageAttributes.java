/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.util.Locale;

public final class PageAttributes implements Cloneable {
  public static final class ColorType extends AttributeValue {
    private static final String[] kNames = {"color", "monochrome"};

    public static final ColorType COLOR = new ColorType(0);
    public static final ColorType MONOCHROME = new ColorType(1);

    private ColorType(int type) { super(type, kNames); }

    public String toString() { return kNames[hashCode()]; }

    public int hashCode() { return super.hashCode(); }
  }

  public static final class MediaType extends AttributeValue {
    private static final String[] kNames = {"iso-4a0", "iso-2a0", "iso-a0", "iso-a1", "iso-a2", "iso-a3", "iso-a4", "iso-a5", "iso-a6", "iso-a7", "iso-a8", "iso-a9", "iso-a10", "iso-b0", "iso-b1", "iso-b2", "iso-b3", "iso-b4", "iso-b5", "iso-b6", "iso-b7", "iso-b8", "iso-b9", "iso-b10", "jis-b0", "jis-b1", "jis-b2", "jis-b3", "jis-b4", "jis-b5", "jis-b6", "jis-b7", "jis-b8", "jis-b9", "jis-b10", "iso-c0", "iso-c1", "iso-c2", "iso-c3", "iso-c4", "iso-c5", "iso-c6", "iso-c7", "iso-c8", "iso-c9", "iso-c10", "iso-designated-long", "executive", "folio", "invoice", "ledger", "na-letter", "na-legal", "quarto", "a", "b", "c", "d", "e", "na-10x15-envelope", "na-10x14-envelope", "na-10x13-envelope", "na-9x12-envelope", "na-9x11-envelope", "na-7x9-envelope", "na-6x9-envelope", "na-number-9-envelope", "na-number-10-envelope", "na-number-11-envelope", "na-number-12-envelope", "na-number-14-envelope", "invite-envelope", "italy-envelope", "monarch-envelope", "personal-envelope"};

    public static final MediaType ISO_4A0 = new MediaType(0);
    public static final MediaType ISO_2A0 = new MediaType(1);
    public static final MediaType ISO_A0 = new MediaType(2);
    public static final MediaType ISO_A1 = new MediaType(3);
    public static final MediaType ISO_A2 = new MediaType(4);
    public static final MediaType ISO_A3 = new MediaType(5);
    public static final MediaType ISO_A4 = new MediaType(6);
    public static final MediaType ISO_A5 = new MediaType(7);
    public static final MediaType ISO_A6 = new MediaType(8);
    public static final MediaType ISO_A7 = new MediaType(9);
    public static final MediaType ISO_A8 = new MediaType(10);
    public static final MediaType ISO_A9 = new MediaType(11);
    public static final MediaType ISO_A10 = new MediaType(12);
    public static final MediaType ISO_B0 = new MediaType(13);
    public static final MediaType ISO_B1 = new MediaType(14);
    public static final MediaType ISO_B2 = new MediaType(15);
    public static final MediaType ISO_B3 = new MediaType(16);
    public static final MediaType ISO_B4 = new MediaType(17);
    public static final MediaType ISO_B5 = new MediaType(18);
    public static final MediaType ISO_B6 = new MediaType(19);
    public static final MediaType ISO_B7 = new MediaType(20);
    public static final MediaType ISO_B8 = new MediaType(21);
    public static final MediaType ISO_B9 = new MediaType(22);
    public static final MediaType ISO_B10 = new MediaType(23);
    public static final MediaType JIS_B0 = new MediaType(24);
    public static final MediaType JIS_B1 = new MediaType(25);
    public static final MediaType JIS_B2 = new MediaType(26);
    public static final MediaType JIS_B3 = new MediaType(27);
    public static final MediaType JIS_B4 = new MediaType(28);
    public static final MediaType JIS_B5 = new MediaType(29);
    public static final MediaType JIS_B6 = new MediaType(30);
    public static final MediaType JIS_B7 = new MediaType(31);
    public static final MediaType JIS_B8 = new MediaType(32);
    public static final MediaType JIS_B9 = new MediaType(33);
    public static final MediaType JIS_B10 = new MediaType(34);
    public static final MediaType ISO_C0 = new MediaType(35);
    public static final MediaType ISO_C1 = new MediaType(36);
    public static final MediaType ISO_C2 = new MediaType(37);
    public static final MediaType ISO_C3 = new MediaType(38);
    public static final MediaType ISO_C4 = new MediaType(39);
    public static final MediaType ISO_C5 = new MediaType(40);
    public static final MediaType ISO_C6 = new MediaType(41);
    public static final MediaType ISO_C7 = new MediaType(42);
    public static final MediaType ISO_C8 = new MediaType(43);
    public static final MediaType ISO_C9 = new MediaType(44);
    public static final MediaType ISO_C10 = new MediaType(45);
    public static final MediaType ISO_DESIGNATED_LONG = new MediaType(46);
    public static final MediaType EXECUTIVE = new MediaType(47);
    public static final MediaType FOLIO = new MediaType(48);
    public static final MediaType INVOICE = new MediaType(49);
    public static final MediaType LEDGER = new MediaType(50);
    public static final MediaType NA_LETTER = new MediaType(51);
    public static final MediaType NA_LEGAL = new MediaType(52);
    public static final MediaType QUARTO = new MediaType(53);
    public static final MediaType A = new MediaType(54);
    public static final MediaType B = new MediaType(55);
    public static final MediaType C = new MediaType(56);
    public static final MediaType D = new MediaType(57);
    public static final MediaType E = new MediaType(58);
    public static final MediaType NA_10X15_ENVELOPE = new MediaType(59);
    public static final MediaType NA_10X14_ENVELOPE = new MediaType(60);
    public static final MediaType NA_10X13_ENVELOPE = new MediaType(61);
    public static final MediaType NA_9X12_ENVELOPE = new MediaType(62);
    public static final MediaType NA_9X11_ENVELOPE = new MediaType(63);
    public static final MediaType NA_7X9_ENVELOPE = new MediaType(64);
    public static final MediaType NA_6X9_ENVELOPE = new MediaType(65);
    public static final MediaType NA_NUMBER_9_ENVELOPE = new MediaType(66);
    public static final MediaType NA_NUMBER_10_ENVELOPE = new MediaType(67);
    public static final MediaType NA_NUMBER_11_ENVELOPE = new MediaType(68);
    public static final MediaType NA_NUMBER_12_ENVELOPE = new MediaType(69);
    public static final MediaType NA_NUMBER_14_ENVELOPE = new MediaType(70);
    public static final MediaType INVITE_ENVELOPE = new MediaType(71);
    public static final MediaType ITALY_ENVELOPE = new MediaType(72);
    public static final MediaType MONARCH_ENVELOPE = new MediaType(73);
    public static final MediaType PERSONAL_ENVELOPE = new MediaType(74);
    public static final MediaType A0 = ISO_A0;
    public static final MediaType A1 = ISO_A1;
    public static final MediaType A2 = ISO_A2;
    public static final MediaType A3 = ISO_A3;
    public static final MediaType A4 = ISO_A4;
    public static final MediaType A5 = ISO_A5;
    public static final MediaType A6 = ISO_A6;
    public static final MediaType A7 = ISO_A7;
    public static final MediaType A8 = ISO_A8;
    public static final MediaType A9 = ISO_A9;
    public static final MediaType A10 = ISO_A10;
    public static final MediaType B0 = ISO_B0;
    public static final MediaType B1 = ISO_B1;
    public static final MediaType B2 = ISO_B2;
    public static final MediaType B3 = ISO_B3;
    public static final MediaType B4 = ISO_B4;
    public static final MediaType ISO_B4_ENVELOPE = ISO_B4;
    public static final MediaType B5 = ISO_B5;
    public static final MediaType ISO_B5_ENVELOPE = ISO_B5;
    public static final MediaType B6 = ISO_B6;
    public static final MediaType B7 = ISO_B7;
    public static final MediaType B8 = ISO_B8;
    public static final MediaType B9 = ISO_B9;
    public static final MediaType B10 = ISO_B10;
    public static final MediaType C0 = ISO_C0;
    public static final MediaType ISO_C0_ENVELOPE = ISO_C0;
    public static final MediaType C1 = ISO_C1;
    public static final MediaType ISO_C1_ENVELOPE = ISO_C1;
    public static final MediaType C2 = ISO_C2;
    public static final MediaType ISO_C2_ENVELOPE = ISO_C2;
    public static final MediaType C3 = ISO_C3;
    public static final MediaType ISO_C3_ENVELOPE = ISO_C3;
    public static final MediaType C4 = ISO_C4;
    public static final MediaType ISO_C4_ENVELOPE = ISO_C4;
    public static final MediaType C5 = ISO_C5;
    public static final MediaType ISO_C5_ENVELOPE = ISO_C5;
    public static final MediaType C6 = ISO_C6;
    public static final MediaType ISO_C6_ENVELOPE = ISO_C6;
    public static final MediaType C7 = ISO_C7;
    public static final MediaType ISO_C7_ENVELOPE = ISO_C7;
    public static final MediaType C8 = ISO_C8;
    public static final MediaType ISO_C8_ENVELOPE = ISO_C8;
    public static final MediaType C9 = ISO_C9;
    public static final MediaType ISO_C9_ENVELOPE = ISO_C9;
    public static final MediaType C10 = ISO_C10;
    public static final MediaType ISO_C10_ENVELOPE = ISO_C10;
    public static final MediaType ISO_DESIGNATED_LONG_ENVELOPE = ISO_DESIGNATED_LONG;
    public static final MediaType STATEMENT = INVOICE;
    public static final MediaType TABLOID = LEDGER;
    public static final MediaType LETTER = NA_LETTER;
    public static final MediaType NOTE = NA_LETTER;
    public static final MediaType LEGAL = NA_LEGAL;
    public static final MediaType ENV_10X15 = NA_10X15_ENVELOPE;
    public static final MediaType ENV_10X14 = NA_10X14_ENVELOPE;
    public static final MediaType ENV_10X13 = NA_10X13_ENVELOPE;
    public static final MediaType ENV_9X12 = NA_9X12_ENVELOPE;
    public static final MediaType ENV_9X11 = NA_9X11_ENVELOPE;
    public static final MediaType ENV_7X9 = NA_7X9_ENVELOPE;
    public static final MediaType ENV_6X9 = NA_6X9_ENVELOPE;
    public static final MediaType ENV_9 = NA_NUMBER_9_ENVELOPE;
    public static final MediaType ENV_10 = NA_NUMBER_10_ENVELOPE;
    public static final MediaType ENV_11 = NA_NUMBER_11_ENVELOPE;
    public static final MediaType ENV_12 = NA_NUMBER_12_ENVELOPE;
    public static final MediaType ENV_14 = NA_NUMBER_14_ENVELOPE;
    public static final MediaType ENV_INVITE = INVITE_ENVELOPE;
    public static final MediaType ENV_ITALY = ITALY_ENVELOPE;
    public static final MediaType ENV_MONARCH = MONARCH_ENVELOPE;
    public static final MediaType ENV_PERSONAL = PERSONAL_ENVELOPE;
    public static final MediaType INVITE = INVITE_ENVELOPE;
    public static final MediaType ITALY = ITALY_ENVELOPE;
    public static final MediaType MONARCH = MONARCH_ENVELOPE;
    public static final MediaType PERSONAL = PERSONAL_ENVELOPE;

    private MediaType(int type) { super(type, kNames); }

    public String toString() { return kNames[hashCode()]; }

    public int hashCode() { return super.hashCode(); }
  }

  public static final class OrientationRequestedType extends AttributeValue {
    private static final String[] kNames = {"portrait", "landscape"};

    public static final OrientationRequestedType PORTRAIT = new OrientationRequestedType(0);
    public static final OrientationRequestedType LANDSCAPE = new OrientationRequestedType(1);

    private OrientationRequestedType(int type) { super(type, kNames); }

    public String toString() { return kNames[hashCode()]; }

    public int hashCode() { return super.hashCode(); }
  }

  public static final class OriginType extends AttributeValue {
    private static final String[] kNames = {"physical", "printable"};

    public static final OriginType PHYSICAL = new OriginType(0);
    public static final OriginType PRINTABLE = new OriginType(1);

    private OriginType(int type) { super(type, kNames); }

    public String toString() { return kNames[hashCode()]; }

    public int hashCode() { return super.hashCode(); }
  }

  public static final class PrintQualityType extends AttributeValue {
    private static final String[] kNames = {"high", "normal", "draft"};

    public static final PrintQualityType HIGH = new PrintQualityType(0);
    public static final PrintQualityType NORMAL = new PrintQualityType(1);
    public static final PrintQualityType DRAFT = new PrintQualityType(2);

    private PrintQualityType(int type) { super(type, kNames); }

    public String toString() { return kNames[hashCode()]; }

    public int hashCode() { return super.hashCode(); }
  }


  private ColorType color;
  private MediaType media;
  private OrientationRequestedType orientationRequested;
  private OriginType origin;
  private PrintQualityType printQuality;
  private int[] printerResolution;

  public PageAttributes() {
    setColor(ColorType.MONOCHROME);
    setMediaToDefault();
    setOrientationRequestedToDefault();
    setOrigin(OriginType.PHYSICAL);
    setPrintQualityToDefault();
    setPrinterResolutionToDefault();
  }

  public PageAttributes(PageAttributes obj) {
    set(obj);
  }

  public PageAttributes(ColorType color, MediaType media, OrientationRequestedType orientationRequested,
      OriginType origin, PrintQualityType printQuality, int[] printerResolution) {
    setColor(color);
    setMedia(media);
    setOrientationRequested(orientationRequested);
    setOrigin(origin);
    setPrintQuality(printQuality);
    setPrinterResolution(printerResolution);
  }

  public Object clone() {
    try {
      PageAttributes c = (PageAttributes) super.clone();
      c.printerResolution = printerResolution.clone();
      return c;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  public void set(PageAttributes obj) {
    color = obj.color;
    media = obj.media;
    orientationRequested = obj.orientationRequested;
    origin = obj.origin;
    printQuality = obj.printQuality;
    printerResolution = obj.printerResolution.clone();
  }

  public ColorType getColor() { return color; }

  public void setColor(ColorType color) {
    if (color == null) {
      throw new IllegalArgumentException("Invalid value for attribute color");
    }
    this.color = color;
  }

  public MediaType getMedia() { return media; }

  public void setMedia(MediaType media) {
    if (media == null) {
      throw new IllegalArgumentException("Invalid value for attribute media");
    }
    this.media = media;
  }

  public void setMediaToDefault() {
    String defaultCountry = Locale.getDefault().getCountry();
    if (defaultCountry != null && (defaultCountry.equals(Locale.US.getCountry())
        || defaultCountry.equals(Locale.CANADA.getCountry()))) {
      setMedia(MediaType.NA_LETTER);
    } else {
      setMedia(MediaType.ISO_A4);
    }
  }

  public OrientationRequestedType getOrientationRequested() { return orientationRequested; }

  public void setOrientationRequested(OrientationRequestedType orientationRequested) {
    if (orientationRequested == null) {
      throw new IllegalArgumentException("Invalid value for attribute orientationRequested");
    }
    this.orientationRequested = orientationRequested;
  }

  public void setOrientationRequested(int orientationRequested) {
    switch (orientationRequested) {
      case 3:
        setOrientationRequested(OrientationRequestedType.PORTRAIT);
        break;
      case 4:
        setOrientationRequested(OrientationRequestedType.LANDSCAPE);
        break;
      default:
        setOrientationRequested(null);
        break;
    }
  }

  public void setOrientationRequestedToDefault() { setOrientationRequested(OrientationRequestedType.PORTRAIT); }

  public OriginType getOrigin() { return origin; }

  public void setOrigin(OriginType origin) {
    if (origin == null) {
      throw new IllegalArgumentException("Invalid value for attribute origin");
    }
    this.origin = origin;
  }

  public PrintQualityType getPrintQuality() { return printQuality; }

  public void setPrintQuality(PrintQualityType printQuality) {
    if (printQuality == null) {
      throw new IllegalArgumentException("Invalid value for attribute printQuality");
    }
    this.printQuality = printQuality;
  }

  public void setPrintQuality(int printQuality) {
    switch (printQuality) {
      case 3:
        setPrintQuality(PrintQualityType.DRAFT);
        break;
      case 4:
        setPrintQuality(PrintQualityType.NORMAL);
        break;
      case 5:
        setPrintQuality(PrintQualityType.HIGH);
        break;
      default:
        setPrintQuality(null);
        break;
    }
  }

  public void setPrintQualityToDefault() { setPrintQuality(PrintQualityType.NORMAL); }

  public int[] getPrinterResolution() {
    int[] copy = new int[3];
    copy[0] = printerResolution[0];
    copy[1] = printerResolution[1];
    copy[2] = printerResolution[2];
    return copy;
  }

  public void setPrinterResolution(int[] printerResolution) {
    if (printerResolution == null || printerResolution.length != 3 || printerResolution[0] <= 0
        || printerResolution[1] <= 0 || (printerResolution[2] != 3 && printerResolution[2] != 4)) {
      throw new IllegalArgumentException("Invalid value for attribute printerResolution");
    }
    int[] copy = new int[3];
    copy[0] = printerResolution[0];
    copy[1] = printerResolution[1];
    copy[2] = printerResolution[2];
    this.printerResolution = copy;
  }

  public void setPrinterResolution(int printerResolution) {
    setPrinterResolution(new int[] {printerResolution, printerResolution, 3});
  }

  public void setPrinterResolutionToDefault() { setPrinterResolution(72); }

  public boolean equals(Object obj) {
    if (!(obj instanceof PageAttributes)) {
      return false;
    }
    PageAttributes rhs = (PageAttributes) obj;
    return (color == rhs.color && media == rhs.media && orientationRequested == rhs.orientationRequested
        && origin == rhs.origin && printQuality == rhs.printQuality
        && printerResolution[0] == rhs.printerResolution[0] && printerResolution[1] == rhs.printerResolution[1]
        && printerResolution[2] == rhs.printerResolution[2]);
  }

  public int hashCode() {
    return (color.hashCode() << 31 ^ media.hashCode() << 24 ^ orientationRequested.hashCode() << 23
        ^ origin.hashCode() << 22 ^ printQuality.hashCode() << 20 ^ printerResolution[2] >> 2 << 19
        ^ printerResolution[1] << 10 ^ printerResolution[0]);
  }

  public String toString() {
    return "color=" + getColor() + ",media=" + getMedia() + ",orientation-requested=" + getOrientationRequested()
        + ",origin=" + getOrigin() + ",print-quality=" + getPrintQuality() + ",printer-resolution=["
        + printerResolution[0] + "," + printerResolution[1] + "," + printerResolution[2] + "]";
  }
}
