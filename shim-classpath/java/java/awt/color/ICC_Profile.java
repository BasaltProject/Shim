/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.color;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ICC_Profile implements java.io.Serializable {
  public static final int CLASS_INPUT = 0;
  public static final int CLASS_DISPLAY = 1;
  public static final int CLASS_OUTPUT = 2;
  public static final int CLASS_DEVICELINK = 3;
  public static final int CLASS_COLORSPACECONVERSION = 4;
  public static final int CLASS_ABSTRACT = 5;
  public static final int CLASS_NAMEDCOLOR = 6;
  public static final int icSigXYZData = 1482250784;
  public static final int icSigLabData = 1281450528;
  public static final int icSigLuvData = 1282766368;
  public static final int icSigYCbCrData = 1497588338;
  public static final int icSigYxyData = 1501067552;
  public static final int icSigRgbData = 1380401696;
  public static final int icSigGrayData = 1196573017;
  public static final int icSigHsvData = 1213421088;
  public static final int icSigHlsData = 1212961568;
  public static final int icSigCmykData = 1129142603;
  public static final int icSigCmyData = 1129142560;
  public static final int icSigSpace2CLR = 843271250;
  public static final int icSigSpace3CLR = 860048466;
  public static final int icSigSpace4CLR = 876825682;
  public static final int icSigSpace5CLR = 893602898;
  public static final int icSigSpace6CLR = 910380114;
  public static final int icSigSpace7CLR = 927157330;
  public static final int icSigSpace8CLR = 943934546;
  public static final int icSigSpace9CLR = 960711762;
  public static final int icSigSpaceACLR = 1094929490;
  public static final int icSigSpaceBCLR = 1111706706;
  public static final int icSigSpaceCCLR = 1128483922;
  public static final int icSigSpaceDCLR = 1145261138;
  public static final int icSigSpaceECLR = 1162038354;
  public static final int icSigSpaceFCLR = 1178815570;
  public static final int icSigInputClass = 1935896178;
  public static final int icSigDisplayClass = 1835955314;
  public static final int icSigOutputClass = 1886549106;
  public static final int icSigLinkClass = 1818848875;
  public static final int icSigAbstractClass = 1633842036;
  public static final int icSigColorSpaceClass = 1936744803;
  public static final int icSigNamedColorClass = 1852662636;
  public static final int icPerceptual = 0;
  public static final int icRelativeColorimetric = 1;
  public static final int icMediaRelativeColorimetric = 1;
  public static final int icSaturation = 2;
  public static final int icAbsoluteColorimetric = 3;
  public static final int icICCAbsoluteColorimetric = 3;
  public static final int icSigHead = 1751474532;
  public static final int icSigAToB0Tag = 1093812784;
  public static final int icSigAToB1Tag = 1093812785;
  public static final int icSigAToB2Tag = 1093812786;
  public static final int icSigBlueColorantTag = 1649957210;
  public static final int icSigBlueMatrixColumnTag = 1649957210;
  public static final int icSigBlueTRCTag = 1649693251;
  public static final int icSigBToA0Tag = 1110589744;
  public static final int icSigBToA1Tag = 1110589745;
  public static final int icSigBToA2Tag = 1110589746;
  public static final int icSigCalibrationDateTimeTag = 1667329140;
  public static final int icSigCharTargetTag = 1952543335;
  public static final int icSigCopyrightTag = 1668313716;
  public static final int icSigCrdInfoTag = 1668441193;
  public static final int icSigDeviceMfgDescTag = 1684893284;
  public static final int icSigDeviceModelDescTag = 1684890724;
  public static final int icSigDeviceSettingsTag = 1684371059;
  public static final int icSigGamutTag = 1734438260;
  public static final int icSigGrayTRCTag = 1800688195;
  public static final int icSigGreenColorantTag = 1733843290;
  public static final int icSigGreenMatrixColumnTag = 1733843290;
  public static final int icSigGreenTRCTag = 1733579331;
  public static final int icSigLuminanceTag = 1819635049;
  public static final int icSigMeasurementTag = 1835360627;
  public static final int icSigMediaBlackPointTag = 1651208308;
  public static final int icSigMediaWhitePointTag = 2004119668;
  public static final int icSigNamedColor2Tag = 1852009522;
  public static final int icSigOutputResponseTag = 1919251312;
  public static final int icSigPreview0Tag = 1886545200;
  public static final int icSigPreview1Tag = 1886545201;
  public static final int icSigPreview2Tag = 1886545202;
  public static final int icSigProfileDescriptionTag = 1684370275;
  public static final int icSigProfileSequenceDescTag = 1886610801;
  public static final int icSigPs2CRD0Tag = 1886610480;
  public static final int icSigPs2CRD1Tag = 1886610481;
  public static final int icSigPs2CRD2Tag = 1886610482;
  public static final int icSigPs2CRD3Tag = 1886610483;
  public static final int icSigPs2CSATag = 1886597747;
  public static final int icSigPs2RenderingIntentTag = 1886597737;
  public static final int icSigRedColorantTag = 1918392666;
  public static final int icSigRedMatrixColumnTag = 1918392666;
  public static final int icSigRedTRCTag = 1918128707;
  public static final int icSigScreeningDescTag = 1935897188;
  public static final int icSigScreeningTag = 1935897198;
  public static final int icSigTechnologyTag = 1952801640;
  public static final int icSigUcrBgTag = 1650877472;
  public static final int icSigViewingCondDescTag = 1987405156;
  public static final int icSigViewingConditionsTag = 1986618743;
  public static final int icSigChromaticityTag = 1667789421;
  public static final int icSigChromaticAdaptationTag = 1667785060;
  public static final int icSigColorantOrderTag = 1668051567;
  public static final int icSigColorantTableTag = 1668051572;
  public static final int icHdrSize = 0;
  public static final int icHdrCmmId = 4;
  public static final int icHdrVersion = 8;
  public static final int icHdrDeviceClass = 12;
  public static final int icHdrColorSpace = 16;
  public static final int icHdrPcs = 20;
  public static final int icHdrDate = 24;
  public static final int icHdrMagic = 36;
  public static final int icHdrPlatform = 40;
  public static final int icHdrFlags = 44;
  public static final int icHdrManufacturer = 48;
  public static final int icHdrModel = 52;
  public static final int icHdrAttributes = 56;
  public static final int icHdrRenderingIntent = 64;
  public static final int icHdrIlluminant = 68;
  public static final int icHdrCreator = 80;
  public static final int icHdrProfileID = 84;
  public static final int icTagType = 0;
  public static final int icTagReserved = 4;
  public static final int icCurveCount = 8;
  public static final int icCurveData = 12;
  public static final int icXYZNumberX = 8;

  static final int kSrgb = 0;
  static final int kLinearRgb = 1;
  static final int kGray = 2;
  static final int kXyz = 3;
  static final int kPycc = 4;

  private byte[] data;
  final int kind;

  ICC_Profile(byte[] data, int kind) {
    this.data = data;
    this.kind = kind;
  }

  private static byte[] header(int colorSpaceSig, int pcsSig, int profileClass, String desc) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] h = new byte[128];
    putInt(h, 8, 0x02100000);
    putInt(h, 12, profileClass);
    putInt(h, 16, colorSpaceSig);
    putInt(h, 20, pcsSig);
    putInt(h, 36, 0x61637370);
    byte[] d = desc.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    putInt(h, 0, 128 + 4 + 12 + d.length);
    out.write(h, 0, h.length);
    byte[] tags = new byte[4 + 12];
    putInt(tags, 0, 1);
    putInt(tags, 4, icSigProfileDescriptionTag);
    putInt(tags, 8, 128 + 16);
    putInt(tags, 12, d.length);
    out.write(tags, 0, tags.length);
    out.write(d, 0, d.length);
    return out.toByteArray();
  }

  private static void putInt(byte[] b, int off, int v) {
    b[off] = (byte) (v >>> 24);
    b[off + 1] = (byte) (v >>> 16);
    b[off + 2] = (byte) (v >>> 8);
    b[off + 3] = (byte) v;
  }

  private static int getInt(byte[] b, int off) {
    return (b[off] & 0xff) << 24 | (b[off + 1] & 0xff) << 16 | (b[off + 2] & 0xff) << 8 | (b[off + 3] & 0xff);
  }

  public static ICC_Profile getInstance(byte[] data) {
    if (data == null || data.length < 128 || getInt(data, 36) != 0x61637370) {
      throw new IllegalArgumentException("Invalid ICC Profile Data");
    }
    int sig = getInt(data, 16);
    int cls = getInt(data, 12);
    if (cls != icSigInputClass && cls != icSigDisplayClass && cls != icSigOutputClass
        && cls != icSigColorSpaceClass && cls != icSigAbstractClass && cls != icSigLinkClass && cls != icSigNamedColorClass) {
      throw new IllegalArgumentException("Invalid ICC Profile Data");
    }
    if (sig == icSigRgbData) {
      return new ICC_ProfileRGB(data, kSrgb);
    } else if (sig == icSigGrayData) {
      return new ICC_ProfileGray(data, kGray);
    }
    return new ICC_Profile(data, sig == icSigXYZData ? kXyz : kPycc);
  }

  public static ICC_Profile getInstance(int cspace) {
    switch (cspace) {
    case ColorSpace.CS_sRGB:
      return new ICC_ProfileRGB(header(icSigRgbData, icSigXYZData, icSigDisplayClass, "sRGB IEC61966-2.1"), kSrgb);
    case ColorSpace.CS_LINEAR_RGB:
      return new ICC_ProfileRGB(header(icSigRgbData, icSigXYZData, icSigDisplayClass, "Linear RGB"), kLinearRgb);
    case ColorSpace.CS_GRAY:
      return new ICC_ProfileGray(header(icSigGrayData, icSigXYZData, icSigDisplayClass, "Linear Gray"), kGray);
    case ColorSpace.CS_CIEXYZ:
      return new ICC_Profile(header(icSigXYZData, icSigXYZData, icSigColorSpaceClass, "CIEXYZ"), kXyz);
    case ColorSpace.CS_PYCC:
      return new ICC_Profile(header(icSigYCbCrData, icSigXYZData, icSigColorSpaceClass, "PhotoYCC"), kPycc);
    default:
      throw new IllegalArgumentException("Unknown color space");
    }
  }

  public static ICC_Profile getInstance(String fileName) throws IOException {
    try (FileInputStream in = new FileInputStream(fileName)) {
      return getInstance(in);
    }
  }

  public static ICC_Profile getInstance(InputStream s) throws IOException {
    return getInstance(s.readAllBytes());
  }

  public int getMajorVersion() {
    return data[8];
  }

  public int getMinorVersion() {
    return (data[9] & 0xf0) >> 4;
  }

  public int getProfileClass() {
    int sig = getInt(data, 12);
    if (sig == icSigInputClass) {
      return CLASS_INPUT;
    } else if (sig == icSigDisplayClass) {
      return CLASS_DISPLAY;
    } else if (sig == icSigOutputClass) {
      return CLASS_OUTPUT;
    } else if (sig == icSigLinkClass) {
      return CLASS_DEVICELINK;
    } else if (sig == icSigColorSpaceClass) {
      return CLASS_COLORSPACECONVERSION;
    } else if (sig == icSigAbstractClass) {
      return CLASS_ABSTRACT;
    } else if (sig == icSigNamedColorClass) {
      return CLASS_NAMEDCOLOR;
    }
    throw new IllegalArgumentException("Unknown profile class");
  }

  public int getColorSpaceType() {
    return csType(getInt(data, 16));
  }

  public int getPCSType() {
    return csType(getInt(data, 20));
  }

  private static int csType(int sig) {
    switch (sig) {
    case icSigXYZData:
      return ColorSpace.TYPE_XYZ;
    case icSigLabData:
      return ColorSpace.TYPE_Lab;
    case icSigLuvData:
      return ColorSpace.TYPE_Luv;
    case icSigYCbCrData:
      return ColorSpace.TYPE_YCbCr;
    case icSigYxyData:
      return ColorSpace.TYPE_Yxy;
    case icSigRgbData:
      return ColorSpace.TYPE_RGB;
    case icSigGrayData:
      return ColorSpace.TYPE_GRAY;
    case icSigHsvData:
      return ColorSpace.TYPE_HSV;
    case icSigHlsData:
      return ColorSpace.TYPE_HLS;
    case icSigCmykData:
      return ColorSpace.TYPE_CMYK;
    case icSigCmyData:
      return ColorSpace.TYPE_CMY;
    default:
      for (int i = 0; i < 9; i++) {
        if (sig == icSigSpace2CLR + (i << 24)) {
          return ColorSpace.TYPE_2CLR + i;
        }
      }
      for (int i = 0; i < 6; i++) {
        if (sig == icSigSpaceACLR + (i << 24)) {
          return ColorSpace.TYPE_ACLR + i;
        }
      }
      throw new IllegalArgumentException("Unknown color space");
    }
  }

  public int getNumComponents() {
    switch (getColorSpaceType()) {
    case ColorSpace.TYPE_GRAY:
      return 1;
    case ColorSpace.TYPE_CMYK:
    case ColorSpace.TYPE_4CLR:
      return 4;
    case ColorSpace.TYPE_2CLR:
      return 2;
    case ColorSpace.TYPE_5CLR:
      return 5;
    case ColorSpace.TYPE_6CLR:
      return 6;
    case ColorSpace.TYPE_7CLR:
      return 7;
    case ColorSpace.TYPE_8CLR:
      return 8;
    case ColorSpace.TYPE_9CLR:
      return 9;
    case ColorSpace.TYPE_ACLR:
      return 10;
    case ColorSpace.TYPE_BCLR:
      return 11;
    case ColorSpace.TYPE_CCLR:
      return 12;
    case ColorSpace.TYPE_DCLR:
      return 13;
    case ColorSpace.TYPE_ECLR:
      return 14;
    case ColorSpace.TYPE_FCLR:
      return 15;
    default:
      return 3;
    }
  }

  public byte[] getData() {
    return data.clone();
  }

  public byte[] getData(int tagSignature) {
    int count = getInt(data, 128);
    for (int i = 0; i < count; i++) {
      int base = 132 + i * 12;
      if (base + 12 <= data.length && getInt(data, base) == tagSignature) {
        int off = getInt(data, base + 4);
        int size = getInt(data, base + 8);
        byte[] out = new byte[size];
        System.arraycopy(data, off, out, 0, size);
        return out;
      }
    }
    return null;
  }

  public void setData(int tagSignature, byte[] tagData) {
    if (tagSignature == icSigHead) {
      if (tagData.length != 128) {
        throw new IllegalArgumentException("Invalid ICC Profile Data");
      }
      System.arraycopy(tagData, 0, data, 0, 128);
      return;
    }
    throw new IllegalArgumentException("Cannot set tag data for tag " + Integer.toHexString(tagSignature));
  }

  public void write(String fileName) throws IOException {
    try (FileOutputStream out = new FileOutputStream(fileName)) {
      write(out);
    }
  }

  public void write(OutputStream s) throws IOException {
    s.write(data);
  }

  protected Object readResolve() throws java.io.ObjectStreamException {
    return getInstance(data);
  }
}
