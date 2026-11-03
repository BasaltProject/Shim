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

public final class ExifTIFFTagSet extends TIFFTagSet {
  public static final int TAG_GPS_INFO_IFD_POINTER = 34853;
  public static final int TAG_INTEROPERABILITY_IFD_POINTER = 40965;
  public static final int TAG_EXIF_VERSION = 36864;
  public static final java.lang.String EXIF_VERSION_2_1 = "0210";
  public static final java.lang.String EXIF_VERSION_2_2 = "0220";
  public static final int TAG_FLASHPIX_VERSION = 40960;
  public static final int TAG_COLOR_SPACE = 40961;
  public static final int COLOR_SPACE_SRGB = 1;
  public static final int COLOR_SPACE_UNCALIBRATED = 65535;
  public static final int TAG_COMPONENTS_CONFIGURATION = 37121;
  public static final int COMPONENTS_CONFIGURATION_DOES_NOT_EXIST = 0;
  public static final int COMPONENTS_CONFIGURATION_Y = 1;
  public static final int COMPONENTS_CONFIGURATION_CB = 2;
  public static final int COMPONENTS_CONFIGURATION_CR = 3;
  public static final int COMPONENTS_CONFIGURATION_R = 4;
  public static final int COMPONENTS_CONFIGURATION_G = 5;
  public static final int COMPONENTS_CONFIGURATION_B = 6;
  public static final int TAG_COMPRESSED_BITS_PER_PIXEL = 37122;
  public static final int TAG_PIXEL_X_DIMENSION = 40962;
  public static final int TAG_PIXEL_Y_DIMENSION = 40963;
  public static final int TAG_MAKER_NOTE = 37500;
  public static final int TAG_MARKER_NOTE = 37500;
  public static final int TAG_USER_COMMENT = 37510;
  public static final int TAG_RELATED_SOUND_FILE = 40964;
  public static final int TAG_DATE_TIME_ORIGINAL = 36867;
  public static final int TAG_DATE_TIME_DIGITIZED = 36868;
  public static final int TAG_SUB_SEC_TIME = 37520;
  public static final int TAG_SUB_SEC_TIME_ORIGINAL = 37521;
  public static final int TAG_SUB_SEC_TIME_DIGITIZED = 37522;
  public static final int TAG_EXPOSURE_TIME = 33434;
  public static final int TAG_F_NUMBER = 33437;
  public static final int TAG_EXPOSURE_PROGRAM = 34850;
  public static final int EXPOSURE_PROGRAM_NOT_DEFINED = 0;
  public static final int EXPOSURE_PROGRAM_MANUAL = 1;
  public static final int EXPOSURE_PROGRAM_NORMAL_PROGRAM = 2;
  public static final int EXPOSURE_PROGRAM_APERTURE_PRIORITY = 3;
  public static final int EXPOSURE_PROGRAM_SHUTTER_PRIORITY = 4;
  public static final int EXPOSURE_PROGRAM_CREATIVE_PROGRAM = 5;
  public static final int EXPOSURE_PROGRAM_ACTION_PROGRAM = 6;
  public static final int EXPOSURE_PROGRAM_PORTRAIT_MODE = 7;
  public static final int EXPOSURE_PROGRAM_LANDSCAPE_MODE = 8;
  public static final int EXPOSURE_PROGRAM_MAX_RESERVED = 255;
  public static final int TAG_SPECTRAL_SENSITIVITY = 34852;
  public static final int TAG_ISO_SPEED_RATINGS = 34855;
  public static final int TAG_OECF = 34856;
  public static final int TAG_SHUTTER_SPEED_VALUE = 37377;
  public static final int TAG_APERTURE_VALUE = 37378;
  public static final int TAG_BRIGHTNESS_VALUE = 37379;
  public static final int TAG_EXPOSURE_BIAS_VALUE = 37380;
  public static final int TAG_MAX_APERTURE_VALUE = 37381;
  public static final int TAG_SUBJECT_DISTANCE = 37382;
  public static final int TAG_METERING_MODE = 37383;
  public static final int METERING_MODE_UNKNOWN = 0;
  public static final int METERING_MODE_AVERAGE = 1;
  public static final int METERING_MODE_CENTER_WEIGHTED_AVERAGE = 2;
  public static final int METERING_MODE_SPOT = 3;
  public static final int METERING_MODE_MULTI_SPOT = 4;
  public static final int METERING_MODE_PATTERN = 5;
  public static final int METERING_MODE_PARTIAL = 6;
  public static final int METERING_MODE_MIN_RESERVED = 7;
  public static final int METERING_MODE_MAX_RESERVED = 254;
  public static final int METERING_MODE_OTHER = 255;
  public static final int TAG_LIGHT_SOURCE = 37384;
  public static final int LIGHT_SOURCE_UNKNOWN = 0;
  public static final int LIGHT_SOURCE_DAYLIGHT = 1;
  public static final int LIGHT_SOURCE_FLUORESCENT = 2;
  public static final int LIGHT_SOURCE_TUNGSTEN = 3;
  public static final int LIGHT_SOURCE_FLASH = 4;
  public static final int LIGHT_SOURCE_FINE_WEATHER = 9;
  public static final int LIGHT_SOURCE_CLOUDY_WEATHER = 10;
  public static final int LIGHT_SOURCE_SHADE = 11;
  public static final int LIGHT_SOURCE_DAYLIGHT_FLUORESCENT = 12;
  public static final int LIGHT_SOURCE_DAY_WHITE_FLUORESCENT = 13;
  public static final int LIGHT_SOURCE_COOL_WHITE_FLUORESCENT = 14;
  public static final int LIGHT_SOURCE_WHITE_FLUORESCENT = 15;
  public static final int LIGHT_SOURCE_STANDARD_LIGHT_A = 17;
  public static final int LIGHT_SOURCE_STANDARD_LIGHT_B = 18;
  public static final int LIGHT_SOURCE_STANDARD_LIGHT_C = 19;
  public static final int LIGHT_SOURCE_D55 = 20;
  public static final int LIGHT_SOURCE_D65 = 21;
  public static final int LIGHT_SOURCE_D75 = 22;
  public static final int LIGHT_SOURCE_D50 = 23;
  public static final int LIGHT_SOURCE_ISO_STUDIO_TUNGSTEN = 24;
  public static final int LIGHT_SOURCE_OTHER = 255;
  public static final int TAG_FLASH = 37385;
  public static final int FLASH_DID_NOT_FIRE = 0;
  public static final int FLASH_FIRED = 1;
  public static final int FLASH_STROBE_RETURN_LIGHT_NOT_DETECTED = 5;
  public static final int FLASH_STROBE_RETURN_LIGHT_DETECTED = 7;
  public static final int FLASH_MASK_FIRED = 1;
  public static final int FLASH_MASK_RETURN_NOT_DETECTED = 4;
  public static final int FLASH_MASK_RETURN_DETECTED = 6;
  public static final int FLASH_MASK_MODE_FLASH_FIRING = 8;
  public static final int FLASH_MASK_MODE_FLASH_SUPPRESSION = 16;
  public static final int FLASH_MASK_MODE_AUTO = 24;
  public static final int FLASH_MASK_FUNCTION_NOT_PRESENT = 32;
  public static final int FLASH_MASK_RED_EYE_REDUCTION = 64;
  public static final int TAG_FOCAL_LENGTH = 37386;
  public static final int TAG_SUBJECT_AREA = 37396;
  public static final int TAG_FLASH_ENERGY = 41483;
  public static final int TAG_SPATIAL_FREQUENCY_RESPONSE = 41484;
  public static final int TAG_FOCAL_PLANE_X_RESOLUTION = 41486;
  public static final int TAG_FOCAL_PLANE_Y_RESOLUTION = 41487;
  public static final int TAG_FOCAL_PLANE_RESOLUTION_UNIT = 41488;
  public static final int FOCAL_PLANE_RESOLUTION_UNIT_NONE = 1;
  public static final int FOCAL_PLANE_RESOLUTION_UNIT_INCH = 2;
  public static final int FOCAL_PLANE_RESOLUTION_UNIT_CENTIMETER = 3;
  public static final int TAG_SUBJECT_LOCATION = 41492;
  public static final int TAG_EXPOSURE_INDEX = 41493;
  public static final int TAG_SENSING_METHOD = 41495;
  public static final int SENSING_METHOD_NOT_DEFINED = 1;
  public static final int SENSING_METHOD_ONE_CHIP_COLOR_AREA_SENSOR = 2;
  public static final int SENSING_METHOD_TWO_CHIP_COLOR_AREA_SENSOR = 3;
  public static final int SENSING_METHOD_THREE_CHIP_COLOR_AREA_SENSOR = 4;
  public static final int SENSING_METHOD_COLOR_SEQUENTIAL_AREA_SENSOR = 5;
  public static final int SENSING_METHOD_TRILINEAR_SENSOR = 7;
  public static final int SENSING_METHOD_COLOR_SEQUENTIAL_LINEAR_SENSOR = 8;
  public static final int TAG_FILE_SOURCE = 41728;
  public static final int FILE_SOURCE_DSC = 3;
  public static final int TAG_SCENE_TYPE = 41729;
  public static final int SCENE_TYPE_DSC = 1;
  public static final int TAG_CFA_PATTERN = 41730;
  public static final int TAG_CUSTOM_RENDERED = 41985;
  public static final int CUSTOM_RENDERED_NORMAL = 0;
  public static final int CUSTOM_RENDERED_CUSTOM = 1;
  public static final int TAG_EXPOSURE_MODE = 41986;
  public static final int EXPOSURE_MODE_AUTO_EXPOSURE = 0;
  public static final int EXPOSURE_MODE_MANUAL_EXPOSURE = 1;
  public static final int EXPOSURE_MODE_AUTO_BRACKET = 2;
  public static final int TAG_WHITE_BALANCE = 41987;
  public static final int WHITE_BALANCE_AUTO = 0;
  public static final int WHITE_BALANCE_MANUAL = 1;
  public static final int TAG_DIGITAL_ZOOM_RATIO = 41988;
  public static final int TAG_FOCAL_LENGTH_IN_35MM_FILM = 41989;
  public static final int TAG_SCENE_CAPTURE_TYPE = 41990;
  public static final int SCENE_CAPTURE_TYPE_STANDARD = 0;
  public static final int SCENE_CAPTURE_TYPE_LANDSCAPE = 1;
  public static final int SCENE_CAPTURE_TYPE_PORTRAIT = 2;
  public static final int SCENE_CAPTURE_TYPE_NIGHT_SCENE = 3;
  public static final int TAG_GAIN_CONTROL = 41991;
  public static final int GAIN_CONTROL_NONE = 0;
  public static final int GAIN_CONTROL_LOW_GAIN_UP = 1;
  public static final int GAIN_CONTROL_HIGH_GAIN_UP = 2;
  public static final int GAIN_CONTROL_LOW_GAIN_DOWN = 3;
  public static final int GAIN_CONTROL_HIGH_GAIN_DOWN = 4;
  public static final int TAG_CONTRAST = 41992;
  public static final int CONTRAST_NORMAL = 0;
  public static final int CONTRAST_SOFT = 1;
  public static final int CONTRAST_HARD = 2;
  public static final int TAG_SATURATION = 41993;
  public static final int SATURATION_NORMAL = 0;
  public static final int SATURATION_LOW = 1;
  public static final int SATURATION_HIGH = 2;
  public static final int TAG_SHARPNESS = 41994;
  public static final int SHARPNESS_NORMAL = 0;
  public static final int SHARPNESS_SOFT = 1;
  public static final int SHARPNESS_HARD = 2;
  public static final int TAG_DEVICE_SETTING_DESCRIPTION = 41995;
  public static final int TAG_SUBJECT_DISTANCE_RANGE = 41996;
  public static final int SUBJECT_DISTANCE_RANGE_UNKNOWN = 0;
  public static final int SUBJECT_DISTANCE_RANGE_MACRO = 1;
  public static final int SUBJECT_DISTANCE_RANGE_CLOSE_VIEW = 2;
  public static final int SUBJECT_DISTANCE_RANGE_DISTANT_VIEW = 3;
  public static final int TAG_IMAGE_UNIQUE_ID = 42016;

  private static ExifTIFFTagSet theInstance;

  private ExifTIFFTagSet() {
    super(createTags());
  }

  public static synchronized ExifTIFFTagSet getInstance() {
    if (theInstance == null) {
      theInstance = new ExifTIFFTagSet();
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
        tag(33434, "ExposureTime", 32, 1),
        tag(33437, "FNumber", 32, 1),
        tag(34850, "ExposureProgram", 8, 1, "0=Not Defined", "1=Manual", "2=Normal Program", "3=Aperture Priority", "4=Shutter Priority", "5=Creative Program", "6=Action Program", "7=Portrait Mode", "8=Landscape Mode"),
        tag(34852, "SpectralSensitivity", 4, -1),
        tag(34855, "ISOSpeedRatings", 8, -1),
        tag(34856, "OECF", 128, -1),
        tag(36864, "ExifVersion", 128, 4),
        tag(36867, "DateTimeOriginal", 4, 20),
        tag(36868, "DateTimeDigitized", 4, 20),
        tag(37121, "ComponentsConfiguration", 128, 4, "0=DoesNotExist", "1=Y", "2=Cb", "3=Cr", "4=R", "5=G", "6=B"),
        tag(37122, "CompressedBitsPerPixel", 32, 1),
        tag(37377, "ShutterSpeedValue", 1024, 1),
        tag(37378, "ApertureValue", 32, 1),
        tag(37379, "BrightnessValue", 1024, 1),
        tag(37380, "ExposureBiasValue", 1024, 1),
        tag(37381, "MaxApertureValue", 32, 1),
        tag(37382, "SubjectDistance", 32, 1),
        tag(37383, "MeteringMode", 8, 1, "0=Unknown", "1=Average", "2=CenterWeightedAverage", "3=Spot", "4=MultiSpot", "5=Pattern", "6=Partial", "255=Other"),
        tag(37384, "LightSource", 8, 1, "0=Unknown", "1=Daylight", "2=Fluorescent", "3=Tungsten", "17=Standard Light A", "18=Standard Light B", "19=Standard Light C", "20=D55", "21=D65", "22=D75", "255=Other"),
        tag(37385, "Flash", 8, 1, "0=Flash Did Not Fire", "1=Flash Fired", "5=Strobe Return Light Not Detected", "7=Strobe Return Light Detected"),
        tag(37386, "FocalLength", 32, 1),
        tag(37396, "SubjectArea", 8, -1),
        tag(37500, "MakerNote", 128, -1),
        tag(37510, "UserComment", 128, -1),
        tag(37520, "SubSecTime", 4, -1),
        tag(37521, "SubSecTimeOriginal", 4, -1),
        tag(37522, "SubSecTimeDigitized", 4, -1),
        tag(40960, "FlashPixVersion", 128, 4),
        tag(40961, "ColorSpace", 8, 1, "1=sRGB", "65535=Uncalibrated"),
        tag(40962, "PixelXDimension", 24, 1),
        tag(40963, "PixelYDimension", 24, 1),
        tag(40964, "RelatedSoundFile", 4, 13),
        pointer(40965, "InteroperabilityIFD", ExifInteroperabilityTagSet.getInstance()),
        tag(41483, "FlashEnergy", 32, 1),
        tag(41484, "SpatialFrequencyResponse", 128, -1),
        tag(41486, "FocalPlaneXResolution", 32, 1),
        tag(41487, "FocalPlaneYResolution", 32, 1),
        tag(41488, "FocalPlaneResolutionUnit", 8, 1, "1=None", "2=Inch", "3=Centimeter"),
        tag(41492, "SubjectLocation", 8, 2),
        tag(41493, "ExposureIndex", 32, 1),
        tag(41495, "SensingMethod", 8, 1, "1=Not Defined", "2=One-chip color area sensor", "3=Two-chip color area sensor", "4=Three-chip color area sensor", "5=Color sequential area sensor", "7=Trilinear sensor", "8=Color sequential linear sensor"),
        tag(41728, "FileSource", 128, 1, "3=DSC"),
        tag(41729, "SceneType", 128, 1, "1=A directly photographed image"),
        tag(41730, "CFAPattern", 128, -1),
        tag(41985, "CustomRendered", 8, 1, "0=Normal process", "1=Custom process"),
        tag(41986, "ExposureMode", 8, 1, "0=Auto exposure", "1=Manual exposure", "2=Auto bracket"),
        tag(41987, "WhiteBalance", 8, 1, "0=Auto white balance", "1=Manual white balance"),
        tag(41988, "DigitalZoomRatio", 32, 1),
        tag(41989, "FocalLengthIn35mmFilm", 8, 1),
        tag(41990, "SceneCaptureType", 8, 1, "0=Standard", "1=Landscape", "2=Portrait", "3=Night scene"),
        tag(41991, "GainControl", 8, 1, "0=None", "1=Low gain up", "2=High gain up", "3=Low gain down", "4=High gain down"),
        tag(41992, "Contrast", 8, 1, "0=Normal", "1=Soft", "2=Hard"),
        tag(41993, "Saturation", 8, 1, "0=Normal", "1=Low saturation", "2=High saturation"),
        tag(41994, "Sharpness", 8, 1, "0=Normal", "1=Soft", "2=Hard"),
        tag(41995, "DeviceSettingDescription", 128, -1),
        tag(41996, "SubjectDistanceRange", 8, 1, "0=unknown", "1=Macro", "2=Close view", "3=Distant view"),
        tag(42016, "ImageUniqueID", 4, 33),
    }) {
      tags.add(t);
    }
    return tags;
  }
}
