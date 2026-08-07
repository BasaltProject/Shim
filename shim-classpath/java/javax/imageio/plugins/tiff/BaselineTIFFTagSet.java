/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.plugins.tiff;

import java.util.ArrayList;
import java.util.List;

public final class BaselineTIFFTagSet extends TIFFTagSet {
  public static final int TAG_NEW_SUBFILE_TYPE = 254;
  public static final int NEW_SUBFILE_TYPE_REDUCED_RESOLUTION = 1;
  public static final int NEW_SUBFILE_TYPE_SINGLE_PAGE = 2;
  public static final int NEW_SUBFILE_TYPE_TRANSPARENCY = 4;
  public static final int TAG_SUBFILE_TYPE = 255;
  public static final int SUBFILE_TYPE_FULL_RESOLUTION = 1;
  public static final int SUBFILE_TYPE_REDUCED_RESOLUTION = 2;
  public static final int SUBFILE_TYPE_SINGLE_PAGE = 3;
  public static final int TAG_IMAGE_WIDTH = 256;
  public static final int TAG_IMAGE_LENGTH = 257;
  public static final int TAG_BITS_PER_SAMPLE = 258;
  public static final int TAG_COMPRESSION = 259;
  public static final int COMPRESSION_NONE = 1;
  public static final int COMPRESSION_CCITT_RLE = 2;
  public static final int COMPRESSION_CCITT_T_4 = 3;
  public static final int COMPRESSION_CCITT_T_6 = 4;
  public static final int COMPRESSION_LZW = 5;
  public static final int COMPRESSION_OLD_JPEG = 6;
  public static final int COMPRESSION_JPEG = 7;
  public static final int COMPRESSION_ZLIB = 8;
  public static final int COMPRESSION_PACKBITS = 32773;
  public static final int COMPRESSION_DEFLATE = 32946;
  public static final int TAG_PHOTOMETRIC_INTERPRETATION = 262;
  public static final int PHOTOMETRIC_INTERPRETATION_WHITE_IS_ZERO = 0;
  public static final int PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO = 1;
  public static final int PHOTOMETRIC_INTERPRETATION_RGB = 2;
  public static final int PHOTOMETRIC_INTERPRETATION_PALETTE_COLOR = 3;
  public static final int PHOTOMETRIC_INTERPRETATION_TRANSPARENCY_MASK = 4;
  public static final int PHOTOMETRIC_INTERPRETATION_CMYK = 5;
  public static final int PHOTOMETRIC_INTERPRETATION_Y_CB_CR = 6;
  public static final int PHOTOMETRIC_INTERPRETATION_CIELAB = 8;
  public static final int PHOTOMETRIC_INTERPRETATION_ICCLAB = 9;
  public static final int TAG_THRESHHOLDING = 263;
  public static final int THRESHHOLDING_NONE = 1;
  public static final int THRESHHOLDING_ORDERED_DITHER = 2;
  public static final int THRESHHOLDING_RANDOMIZED_DITHER = 3;
  public static final int TAG_CELL_WIDTH = 264;
  public static final int TAG_CELL_LENGTH = 265;
  public static final int TAG_FILL_ORDER = 266;
  public static final int FILL_ORDER_LEFT_TO_RIGHT = 1;
  public static final int FILL_ORDER_RIGHT_TO_LEFT = 2;
  public static final int TAG_DOCUMENT_NAME = 269;
  public static final int TAG_IMAGE_DESCRIPTION = 270;
  public static final int TAG_MAKE = 271;
  public static final int TAG_MODEL = 272;
  public static final int TAG_STRIP_OFFSETS = 273;
  public static final int TAG_ORIENTATION = 274;
  public static final int ORIENTATION_ROW_0_TOP_COLUMN_0_LEFT = 1;
  public static final int ORIENTATION_ROW_0_TOP_COLUMN_0_RIGHT = 2;
  public static final int ORIENTATION_ROW_0_BOTTOM_COLUMN_0_RIGHT = 3;
  public static final int ORIENTATION_ROW_0_BOTTOM_COLUMN_0_LEFT = 4;
  public static final int ORIENTATION_ROW_0_LEFT_COLUMN_0_TOP = 5;
  public static final int ORIENTATION_ROW_0_RIGHT_COLUMN_0_TOP = 6;
  public static final int ORIENTATION_ROW_0_RIGHT_COLUMN_0_BOTTOM = 7;
  public static final int ORIENTATION_ROW_0_LEFT_COLUMN_0_BOTTOM = 8;
  public static final int TAG_SAMPLES_PER_PIXEL = 277;
  public static final int TAG_ROWS_PER_STRIP = 278;
  public static final int TAG_STRIP_BYTE_COUNTS = 279;
  public static final int TAG_MIN_SAMPLE_VALUE = 280;
  public static final int TAG_MAX_SAMPLE_VALUE = 281;
  public static final int TAG_X_RESOLUTION = 282;
  public static final int TAG_Y_RESOLUTION = 283;
  public static final int TAG_PLANAR_CONFIGURATION = 284;
  public static final int PLANAR_CONFIGURATION_CHUNKY = 1;
  public static final int PLANAR_CONFIGURATION_PLANAR = 2;
  public static final int TAG_PAGE_NAME = 285;
  public static final int TAG_X_POSITION = 286;
  public static final int TAG_Y_POSITION = 287;
  public static final int TAG_FREE_OFFSETS = 288;
  public static final int TAG_FREE_BYTE_COUNTS = 289;
  public static final int TAG_GRAY_RESPONSE_UNIT = 290;
  public static final int GRAY_RESPONSE_UNIT_TENTHS = 1;
  public static final int GRAY_RESPONSE_UNIT_HUNDREDTHS = 2;
  public static final int GRAY_RESPONSE_UNIT_THOUSANDTHS = 3;
  public static final int GRAY_RESPONSE_UNIT_TEN_THOUSANDTHS = 4;
  public static final int GRAY_RESPONSE_UNIT_HUNDRED_THOUSANDTHS = 5;
  public static final int TAG_GRAY_RESPONSE_CURVE = 291;
  public static final int TAG_T4_OPTIONS = 292;
  public static final int T4_OPTIONS_2D_CODING = 1;
  public static final int T4_OPTIONS_UNCOMPRESSED = 2;
  public static final int T4_OPTIONS_EOL_BYTE_ALIGNED = 4;
  public static final int TAG_T6_OPTIONS = 293;
  public static final int T6_OPTIONS_UNCOMPRESSED = 2;
  public static final int TAG_RESOLUTION_UNIT = 296;
  public static final int RESOLUTION_UNIT_NONE = 1;
  public static final int RESOLUTION_UNIT_INCH = 2;
  public static final int RESOLUTION_UNIT_CENTIMETER = 3;
  public static final int TAG_PAGE_NUMBER = 297;
  public static final int TAG_TRANSFER_FUNCTION = 301;
  public static final int TAG_SOFTWARE = 305;
  public static final int TAG_DATE_TIME = 306;
  public static final int TAG_ARTIST = 315;
  public static final int TAG_HOST_COMPUTER = 316;
  public static final int TAG_PREDICTOR = 317;
  public static final int PREDICTOR_NONE = 1;
  public static final int PREDICTOR_HORIZONTAL_DIFFERENCING = 2;
  public static final int TAG_WHITE_POINT = 318;
  public static final int TAG_PRIMARY_CHROMATICITES = 319;
  public static final int TAG_COLOR_MAP = 320;
  public static final int TAG_HALFTONE_HINTS = 321;
  public static final int TAG_TILE_WIDTH = 322;
  public static final int TAG_TILE_LENGTH = 323;
  public static final int TAG_TILE_OFFSETS = 324;
  public static final int TAG_TILE_BYTE_COUNTS = 325;
  public static final int TAG_INK_SET = 332;
  public static final int INK_SET_CMYK = 1;
  public static final int INK_SET_NOT_CMYK = 2;
  public static final int TAG_INK_NAMES = 333;
  public static final int TAG_NUMBER_OF_INKS = 334;
  public static final int TAG_DOT_RANGE = 336;
  public static final int TAG_TARGET_PRINTER = 337;
  public static final int TAG_EXTRA_SAMPLES = 338;
  public static final int EXTRA_SAMPLES_UNSPECIFIED = 0;
  public static final int EXTRA_SAMPLES_ASSOCIATED_ALPHA = 1;
  public static final int EXTRA_SAMPLES_UNASSOCIATED_ALPHA = 2;
  public static final int TAG_SAMPLE_FORMAT = 339;
  public static final int SAMPLE_FORMAT_UNSIGNED_INTEGER = 1;
  public static final int SAMPLE_FORMAT_SIGNED_INTEGER = 2;
  public static final int SAMPLE_FORMAT_FLOATING_POINT = 3;
  public static final int SAMPLE_FORMAT_UNDEFINED = 4;
  public static final int TAG_S_MIN_SAMPLE_VALUE = 340;
  public static final int TAG_S_MAX_SAMPLE_VALUE = 341;
  public static final int TAG_TRANSFER_RANGE = 342;
  public static final int TAG_JPEG_TABLES = 347;
  public static final int TAG_JPEG_PROC = 512;
  public static final int JPEG_PROC_BASELINE = 1;
  public static final int JPEG_PROC_LOSSLESS = 14;
  public static final int TAG_JPEG_INTERCHANGE_FORMAT = 513;
  public static final int TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = 514;
  public static final int TAG_JPEG_RESTART_INTERVAL = 515;
  public static final int TAG_JPEG_LOSSLESS_PREDICTORS = 517;
  public static final int TAG_JPEG_POINT_TRANSFORMS = 518;
  public static final int TAG_JPEG_Q_TABLES = 519;
  public static final int TAG_JPEG_DC_TABLES = 520;
  public static final int TAG_JPEG_AC_TABLES = 521;
  public static final int TAG_Y_CB_CR_COEFFICIENTS = 529;
  public static final int TAG_Y_CB_CR_SUBSAMPLING = 530;
  public static final int TAG_Y_CB_CR_POSITIONING = 531;
  public static final int Y_CB_CR_POSITIONING_CENTERED = 1;
  public static final int Y_CB_CR_POSITIONING_COSITED = 2;
  public static final int TAG_REFERENCE_BLACK_WHITE = 532;
  public static final int TAG_COPYRIGHT = 33432;
  public static final int TAG_ICC_PROFILE = 34675;

  private static BaselineTIFFTagSet theInstance;

  private BaselineTIFFTagSet() {
    super(createTags());
  }

  public static synchronized BaselineTIFFTagSet getInstance() {
    if (theInstance == null) {
      theInstance = new BaselineTIFFTagSet();
    }
    return theInstance;
  }

  private static TIFFTag tag(int number, String name, int types, int count, String... valueNames) {
    final String[] vn = valueNames;
    TIFFTag t = new TIFFTag(name, number, types, count) {
      {
        for (String v : vn) {
          int eq = v.indexOf('=');
          addValueName(Integer.parseInt(v.substring(0, eq)), v.substring(eq + 1));
        }
      }
    };
    return t;
  }

  private static TIFFTag pointer(int number, String name, TIFFTagSet child) {
    return new TIFFTag(name, number, child);
  }

  private static List<TIFFTag> createTags() {
    List<TIFFTag> tags = new ArrayList<TIFFTag>();
    for (TIFFTag t : new TIFFTag[] {
        tag(254, "NewSubfileType", 16, 1, "0=Default", "1=ReducedResolution", "2=SinglePage", "3=SinglePage+ReducedResolution", "4=Transparency", "5=Transparency+ReducedResolution", "6=Transparency+SinglePage", "7=Transparency+SinglePage+ReducedResolution"),
        tag(255, "SubfileType", 8, 1, "1=FullResolution", "2=ReducedResolution", "3=SinglePage"),
        tag(256, "ImageWidth", 24, 1),
        tag(257, "ImageLength", 24, 1),
        tag(258, "BitsPerSample", 8, -1),
        tag(259, "Compression", 8, 1, "1=Uncompressed", "2=CCITT RLE", "3=CCITT T.4", "4=CCITT T.6", "5=LZW", "6=Old JPEG", "7=JPEG", "8=ZLib", "32773=PackBits", "32946=Deflate"),
        tag(262, "PhotometricInterpretation", 8, 1, "0=WhiteIsZero", "1=BlackIsZero", "2=RGB", "3=Palette Color", "4=Transparency Mask", "5=CMYK", "6=YCbCr", "8=CIELAB", "9=ICCLAB"),
        tag(263, "Threshholding", 8, 1, "1=None", "2=OrderedDither", "3=RandomizedDither"),
        tag(264, "CellWidth", 8, 1),
        tag(265, "CellLength", 8, 1),
        tag(266, "FillOrder", 8, 1, "1=LeftToRight", "2=RightToLeft"),
        tag(269, "DocumentName", 4, -1),
        tag(270, "ImageDescription", 4, -1),
        tag(271, "Make", 4, -1),
        tag(272, "Model", 4, -1),
        tag(273, "StripOffsets", 24, -1),
        tag(274, "Orientation", 8, 1, "1=Row 0=Top, Column 0=Left", "2=Row 0=Top, Column 0=Right", "3=Row 0=Bottom, Column 0=Right", "4=Row 0=Bottom, Column 0=Left", "5=Row 0=Left, Column 0=Top", "6=Row 0=Right, Column 0=Top", "7=Row 0=Right, Column 0=Bottom"),
        tag(277, "SamplesPerPixel", 8, 1),
        tag(278, "RowsPerStrip", 24, 1),
        tag(279, "StripByteCounts", 24, -1),
        tag(280, "MinSampleValue", 8, -1),
        tag(281, "MaxSampleValue", 8, -1),
        tag(282, "XResolution", 32, 1),
        tag(283, "YResolution", 32, 1),
        tag(284, "PlanarConfiguration", 8, 1, "1=Chunky", "2=Planar"),
        tag(285, "PageName", 4, -1),
        tag(286, "XPosition", 32, 1),
        tag(287, "YPosition", 32, 1),
        tag(288, "FreeOffsets", 16, -1),
        tag(289, "FreeByteCounts", 16, -1),
        tag(290, "GrayResponseUnit", 8, 1, "1=Tenths", "2=Hundredths", "3=Thousandths", "4=Ten-Thousandths", "5=Hundred-Thousandths"),
        tag(291, "GrayResponseCurve", 8, -1),
        tag(292, "T4Options", 16, 1, "0=Default 1DCoding", "1=2DCoding", "2=Uncompressed", "3=2DCoding+Uncompressed", "4=EOLByteAligned", "5=2DCoding+EOLByteAligned", "6=Uncompressed+EOLByteAligned", "7=2DCoding+Uncompressed+EOLByteAligned"),
        tag(293, "T6Options", 16, 1, "0=Default", "2=Uncompressed"),
        tag(296, "ResolutionUnit", 8, 1, "1=None", "2=Inch", "3=Centimeter"),
        tag(297, "PageNumber", 8, -1),
        tag(301, "TransferFunction", 8, -1),
        tag(305, "Software", 4, -1),
        tag(306, "DateTime", 4, 20),
        tag(315, "Artist", 4, -1),
        tag(316, "HostComputer", 4, -1),
        tag(317, "Predictor", 8, 1, "1=None", "2=Horizontal Differencing"),
        tag(318, "WhitePoint", 32, 2),
        tag(319, "PrimaryChromaticities", 32, 6),
        tag(320, "ColorMap", 8, -1),
        tag(321, "HalftoneHints", 8, 2),
        tag(322, "TileWidth", 24, 1),
        tag(323, "TileLength", 24, 1),
        tag(324, "TileOffsets", 16, -1),
        tag(325, "TileByteCounts", 24, -1),
        tag(332, "InkSet", 8, 1, "1=CMYK", "2=Not CMYK"),
        tag(333, "InkNames", 4, -1),
        tag(334, "NumberOfInks", 8, 1),
        tag(336, "DotRange", 10, -1),
        tag(337, "TargetPrinter", 4, -1),
        tag(338, "ExtraSamples", 8, -1, "0=Unspecified", "1=Associated Alpha", "2=Unassociated Alpha"),
        tag(339, "SampleFormat", 8, -1, "1=Unsigned Integer", "2=Signed Integer", "3=Floating Point", "4=Undefined"),
        tag(340, "SMinSampleValue", 8058, -1),
        tag(341, "SMaxSampleValue", 8058, -1),
        tag(342, "TransferRange", 8, 6),
        tag(347, "JPEGTables", 128, -1),
        tag(512, "JPEGProc", 8, 1, "1=Baseline sequential process", "14=Lossless process with Huffman coding"),
        tag(513, "JPEGInterchangeFormat", 16, 1),
        tag(514, "JPEGInterchangeFormatLength", 16, 1),
        tag(515, "JPEGRestartInterval", 8, 1),
        tag(517, "JPEGLosslessPredictors", 8, -1, "1=A", "2=B", "3=C", "4=A+B-C", "5=A+((B-C)/2)", "6=B+((A-C)/2)", "7=(A+B)/2"),
        tag(518, "JPEGPointTransforms", 8, -1),
        tag(519, "JPEGQTables", 16, -1),
        tag(520, "JPEGDCTables", 16, -1),
        tag(521, "JPEGACTables", 16, -1),
        tag(529, "YCbCrCoefficients", 32, 3),
        tag(530, "YCbCrSubSampling", 8, 2),
        tag(531, "YCbCrPositioning", 8, 1, "1=Centered", "2=Cosited"),
        tag(532, "ReferenceBlackWhite", 32, -1),
        tag(33432, "Copyright", 4, -1),
        tag(34675, "ICC Profile", 128, -1),
    }) {
      tags.add(t);
    }
    return tags;
  }
}
