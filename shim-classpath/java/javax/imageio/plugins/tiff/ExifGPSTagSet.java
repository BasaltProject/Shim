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

public final class ExifGPSTagSet extends TIFFTagSet {
  public static final int TAG_GPS_VERSION_ID = 0;
  public static final java.lang.String GPS_VERSION_2_2 = "2200";
  public static final int TAG_GPS_LATITUDE_REF = 1;
  public static final int TAG_GPS_LATITUDE = 2;
  public static final int TAG_GPS_LONGITUDE_REF = 3;
  public static final int TAG_GPS_LONGITUDE = 4;
  public static final int TAG_GPS_ALTITUDE_REF = 5;
  public static final int TAG_GPS_ALTITUDE = 6;
  public static final int TAG_GPS_TIME_STAMP = 7;
  public static final int TAG_GPS_SATELLITES = 8;
  public static final int TAG_GPS_STATUS = 9;
  public static final int TAG_GPS_MEASURE_MODE = 10;
  public static final int TAG_GPS_DOP = 11;
  public static final int TAG_GPS_SPEED_REF = 12;
  public static final int TAG_GPS_SPEED = 13;
  public static final int TAG_GPS_TRACK_REF = 14;
  public static final int TAG_GPS_TRACK = 15;
  public static final int TAG_GPS_IMG_DIRECTION_REF = 16;
  public static final int TAG_GPS_IMG_DIRECTION = 17;
  public static final int TAG_GPS_MAP_DATUM = 18;
  public static final int TAG_GPS_DEST_LATITUDE_REF = 19;
  public static final int TAG_GPS_DEST_LATITUDE = 20;
  public static final int TAG_GPS_DEST_LONGITUDE_REF = 21;
  public static final int TAG_GPS_DEST_LONGITUDE = 22;
  public static final int TAG_GPS_DEST_BEARING_REF = 23;
  public static final int TAG_GPS_DEST_BEARING = 24;
  public static final int TAG_GPS_DEST_DISTANCE_REF = 25;
  public static final int TAG_GPS_DEST_DISTANCE = 26;
  public static final int TAG_GPS_PROCESSING_METHOD = 27;
  public static final int TAG_GPS_AREA_INFORMATION = 28;
  public static final int TAG_GPS_DATE_STAMP = 29;
  public static final int TAG_GPS_DIFFERENTIAL = 30;
  public static final java.lang.String LATITUDE_REF_NORTH = "N";
  public static final java.lang.String LATITUDE_REF_SOUTH = "S";
  public static final java.lang.String LONGITUDE_REF_EAST = "E";
  public static final java.lang.String LONGITUDE_REF_WEST = "W";
  public static final int ALTITUDE_REF_SEA_LEVEL = 0;
  public static final int ALTITUDE_REF_SEA_LEVEL_REFERENCE = 1;
  public static final java.lang.String STATUS_MEASUREMENT_IN_PROGRESS = "A";
  public static final java.lang.String STATUS_MEASUREMENT_INTEROPERABILITY = "V";
  public static final java.lang.String MEASURE_MODE_2D = "2";
  public static final java.lang.String MEASURE_MODE_3D = "3";
  public static final java.lang.String SPEED_REF_KILOMETERS_PER_HOUR = "K";
  public static final java.lang.String SPEED_REF_MILES_PER_HOUR = "M";
  public static final java.lang.String SPEED_REF_KNOTS = "N";
  public static final java.lang.String DIRECTION_REF_TRUE = "T";
  public static final java.lang.String DIRECTION_REF_MAGNETIC = "M";
  public static final java.lang.String DEST_DISTANCE_REF_KILOMETERS = "K";
  public static final java.lang.String DEST_DISTANCE_REF_MILES = "M";
  public static final java.lang.String DEST_DISTANCE_REF_KNOTS = "N";
  public static final int DIFFERENTIAL_CORRECTION_NONE = 0;
  public static final int DIFFERENTIAL_CORRECTION_APPLIED = 1;

  private static ExifGPSTagSet theInstance;

  private ExifGPSTagSet() {
    super(createTags());
  }

  public static synchronized ExifGPSTagSet getInstance() {
    if (theInstance == null) {
      theInstance = new ExifGPSTagSet();
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
        tag(0, "GPSVersionID", 2, -1),
        tag(1, "GPSLatitudeRef", 4, -1),
        tag(2, "GPSLatitude", 32, -1),
        tag(3, "GPSLongitudeRef", 4, -1),
        tag(4, "GPSLongitude", 32, -1),
        tag(5, "GPSAltitudeRef", 2, -1, "0=Sea level", "1=Sea level reference (negative value)"),
        tag(6, "GPSAltitude", 32, -1),
        tag(7, "GPSTimeStamp", 32, -1),
        tag(8, "GPSSatellites", 4, -1),
        tag(9, "GPSStatus", 4, -1),
        tag(10, "GPSMeasureMode", 4, -1),
        tag(11, "GPSDOP", 32, -1),
        tag(12, "GPSSpeedRef", 4, -1),
        tag(13, "GPSSpeed", 32, -1),
        tag(14, "GPSTrackRef", 4, -1),
        tag(15, "GPSTrack", 32, -1),
        tag(16, "GPSImgDirectionRef", 4, -1),
        tag(17, "GPSImgDirection", 32, -1),
        tag(18, "GPSMapDatum", 4, -1),
        tag(19, "GPSDestLatitudeRef", 4, -1),
        tag(20, "GPSDestLatitude", 32, -1),
        tag(21, "GPSDestLongitudeRef", 4, -1),
        tag(22, "GPSDestLongitude", 32, -1),
        tag(23, "GPSDestBearingRef", 4, -1),
        tag(24, "GPSDestBearing", 32, -1),
        tag(25, "GPSDestDistanceRef", 4, -1),
        tag(26, "GPSDestDistance", 32, -1),
        tag(27, "GPSProcessingMethod", 128, -1),
        tag(28, "GPSAreaInformation", 128, -1),
        tag(29, "GPSDateStamp", 4, -1),
        tag(30, "GPSDifferential", 8, -1, "0=Measurement without differential correction", "1=Differential correction applied"),
    }) {
      tags.add(t);
    }
    return tags;
  }
}
