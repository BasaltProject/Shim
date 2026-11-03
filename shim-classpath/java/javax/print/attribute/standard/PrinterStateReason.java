/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class PrinterStateReason extends javax.print.attribute.EnumSyntax implements javax.print.attribute.Attribute {
  public static final javax.print.attribute.standard.PrinterStateReason OTHER = new javax.print.attribute.standard.PrinterStateReason(0);
  public static final javax.print.attribute.standard.PrinterStateReason MEDIA_NEEDED = new javax.print.attribute.standard.PrinterStateReason(1);
  public static final javax.print.attribute.standard.PrinterStateReason MEDIA_JAM = new javax.print.attribute.standard.PrinterStateReason(2);
  public static final javax.print.attribute.standard.PrinterStateReason MOVING_TO_PAUSED = new javax.print.attribute.standard.PrinterStateReason(3);
  public static final javax.print.attribute.standard.PrinterStateReason PAUSED = new javax.print.attribute.standard.PrinterStateReason(4);
  public static final javax.print.attribute.standard.PrinterStateReason SHUTDOWN = new javax.print.attribute.standard.PrinterStateReason(5);
  public static final javax.print.attribute.standard.PrinterStateReason CONNECTING_TO_DEVICE = new javax.print.attribute.standard.PrinterStateReason(6);
  public static final javax.print.attribute.standard.PrinterStateReason TIMED_OUT = new javax.print.attribute.standard.PrinterStateReason(7);
  public static final javax.print.attribute.standard.PrinterStateReason STOPPING = new javax.print.attribute.standard.PrinterStateReason(8);
  public static final javax.print.attribute.standard.PrinterStateReason STOPPED_PARTLY = new javax.print.attribute.standard.PrinterStateReason(9);
  public static final javax.print.attribute.standard.PrinterStateReason TONER_LOW = new javax.print.attribute.standard.PrinterStateReason(10);
  public static final javax.print.attribute.standard.PrinterStateReason TONER_EMPTY = new javax.print.attribute.standard.PrinterStateReason(11);
  public static final javax.print.attribute.standard.PrinterStateReason SPOOL_AREA_FULL = new javax.print.attribute.standard.PrinterStateReason(12);
  public static final javax.print.attribute.standard.PrinterStateReason COVER_OPEN = new javax.print.attribute.standard.PrinterStateReason(13);
  public static final javax.print.attribute.standard.PrinterStateReason INTERLOCK_OPEN = new javax.print.attribute.standard.PrinterStateReason(14);
  public static final javax.print.attribute.standard.PrinterStateReason DOOR_OPEN = new javax.print.attribute.standard.PrinterStateReason(15);
  public static final javax.print.attribute.standard.PrinterStateReason INPUT_TRAY_MISSING = new javax.print.attribute.standard.PrinterStateReason(16);
  public static final javax.print.attribute.standard.PrinterStateReason MEDIA_LOW = new javax.print.attribute.standard.PrinterStateReason(17);
  public static final javax.print.attribute.standard.PrinterStateReason MEDIA_EMPTY = new javax.print.attribute.standard.PrinterStateReason(18);
  public static final javax.print.attribute.standard.PrinterStateReason OUTPUT_TRAY_MISSING = new javax.print.attribute.standard.PrinterStateReason(19);
  public static final javax.print.attribute.standard.PrinterStateReason OUTPUT_AREA_ALMOST_FULL = new javax.print.attribute.standard.PrinterStateReason(20);
  public static final javax.print.attribute.standard.PrinterStateReason OUTPUT_AREA_FULL = new javax.print.attribute.standard.PrinterStateReason(21);
  public static final javax.print.attribute.standard.PrinterStateReason MARKER_SUPPLY_LOW = new javax.print.attribute.standard.PrinterStateReason(22);
  public static final javax.print.attribute.standard.PrinterStateReason MARKER_SUPPLY_EMPTY = new javax.print.attribute.standard.PrinterStateReason(23);
  public static final javax.print.attribute.standard.PrinterStateReason MARKER_WASTE_ALMOST_FULL = new javax.print.attribute.standard.PrinterStateReason(24);
  public static final javax.print.attribute.standard.PrinterStateReason MARKER_WASTE_FULL = new javax.print.attribute.standard.PrinterStateReason(25);
  public static final javax.print.attribute.standard.PrinterStateReason FUSER_OVER_TEMP = new javax.print.attribute.standard.PrinterStateReason(26);
  public static final javax.print.attribute.standard.PrinterStateReason FUSER_UNDER_TEMP = new javax.print.attribute.standard.PrinterStateReason(27);
  public static final javax.print.attribute.standard.PrinterStateReason OPC_NEAR_EOL = new javax.print.attribute.standard.PrinterStateReason(28);
  public static final javax.print.attribute.standard.PrinterStateReason OPC_LIFE_OVER = new javax.print.attribute.standard.PrinterStateReason(29);
  public static final javax.print.attribute.standard.PrinterStateReason DEVELOPER_LOW = new javax.print.attribute.standard.PrinterStateReason(30);
  public static final javax.print.attribute.standard.PrinterStateReason DEVELOPER_EMPTY = new javax.print.attribute.standard.PrinterStateReason(31);
  public static final javax.print.attribute.standard.PrinterStateReason INTERPRETER_RESOURCE_UNAVAILABLE = new javax.print.attribute.standard.PrinterStateReason(32);
  protected PrinterStateReason(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "printer-state-reason"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.PrinterStateReason.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"other", "media-needed", "media-jam", "moving-to-paused", "paused", "shutdown", "connecting-to-device", "timed-out", "stopping", "stopped-partly", "toner-low", "toner-empty", "spool-area-full", "cover-open", "interlock-open", "door-open", "input-tray-missing", "media-low", "media-empty", "output-tray-missing", "output-area-almost-full", "output-area-full", "marker-supply-low", "marker-supply-empty", "marker-waste-almost-full", "marker-waste-full", "fuser-over-temp", "fuser-under-temp", "opc-near-eol", "opc-life-over", "developer-low", "developer-empty", "interpreter-resource-unavailable"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {OTHER, MEDIA_NEEDED, MEDIA_JAM, MOVING_TO_PAUSED, PAUSED, SHUTDOWN, CONNECTING_TO_DEVICE, TIMED_OUT, STOPPING, STOPPED_PARTLY, TONER_LOW, TONER_EMPTY, SPOOL_AREA_FULL, COVER_OPEN, INTERLOCK_OPEN, DOOR_OPEN, INPUT_TRAY_MISSING, MEDIA_LOW, MEDIA_EMPTY, OUTPUT_TRAY_MISSING, OUTPUT_AREA_ALMOST_FULL, OUTPUT_AREA_FULL, MARKER_SUPPLY_LOW, MARKER_SUPPLY_EMPTY, MARKER_WASTE_ALMOST_FULL, MARKER_WASTE_FULL, FUSER_OVER_TEMP, FUSER_UNDER_TEMP, OPC_NEAR_EOL, OPC_LIFE_OVER, DEVELOPER_LOW, DEVELOPER_EMPTY, INTERPRETER_RESOURCE_UNAVAILABLE};
}
