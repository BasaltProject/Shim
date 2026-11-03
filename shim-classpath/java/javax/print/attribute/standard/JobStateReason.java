/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class JobStateReason extends javax.print.attribute.EnumSyntax implements javax.print.attribute.Attribute {
  public static final javax.print.attribute.standard.JobStateReason JOB_INCOMING = new javax.print.attribute.standard.JobStateReason(0);
  public static final javax.print.attribute.standard.JobStateReason JOB_DATA_INSUFFICIENT = new javax.print.attribute.standard.JobStateReason(1);
  public static final javax.print.attribute.standard.JobStateReason DOCUMENT_ACCESS_ERROR = new javax.print.attribute.standard.JobStateReason(2);
  public static final javax.print.attribute.standard.JobStateReason SUBMISSION_INTERRUPTED = new javax.print.attribute.standard.JobStateReason(3);
  public static final javax.print.attribute.standard.JobStateReason JOB_OUTGOING = new javax.print.attribute.standard.JobStateReason(4);
  public static final javax.print.attribute.standard.JobStateReason JOB_HOLD_UNTIL_SPECIFIED = new javax.print.attribute.standard.JobStateReason(5);
  public static final javax.print.attribute.standard.JobStateReason RESOURCES_ARE_NOT_READY = new javax.print.attribute.standard.JobStateReason(6);
  public static final javax.print.attribute.standard.JobStateReason PRINTER_STOPPED_PARTLY = new javax.print.attribute.standard.JobStateReason(7);
  public static final javax.print.attribute.standard.JobStateReason PRINTER_STOPPED = new javax.print.attribute.standard.JobStateReason(8);
  public static final javax.print.attribute.standard.JobStateReason JOB_INTERPRETING = new javax.print.attribute.standard.JobStateReason(9);
  public static final javax.print.attribute.standard.JobStateReason JOB_QUEUED = new javax.print.attribute.standard.JobStateReason(10);
  public static final javax.print.attribute.standard.JobStateReason JOB_TRANSFORMING = new javax.print.attribute.standard.JobStateReason(11);
  public static final javax.print.attribute.standard.JobStateReason JOB_QUEUED_FOR_MARKER = new javax.print.attribute.standard.JobStateReason(12);
  public static final javax.print.attribute.standard.JobStateReason JOB_PRINTING = new javax.print.attribute.standard.JobStateReason(13);
  public static final javax.print.attribute.standard.JobStateReason JOB_CANCELED_BY_USER = new javax.print.attribute.standard.JobStateReason(14);
  public static final javax.print.attribute.standard.JobStateReason JOB_CANCELED_BY_OPERATOR = new javax.print.attribute.standard.JobStateReason(15);
  public static final javax.print.attribute.standard.JobStateReason JOB_CANCELED_AT_DEVICE = new javax.print.attribute.standard.JobStateReason(16);
  public static final javax.print.attribute.standard.JobStateReason ABORTED_BY_SYSTEM = new javax.print.attribute.standard.JobStateReason(17);
  public static final javax.print.attribute.standard.JobStateReason UNSUPPORTED_COMPRESSION = new javax.print.attribute.standard.JobStateReason(18);
  public static final javax.print.attribute.standard.JobStateReason COMPRESSION_ERROR = new javax.print.attribute.standard.JobStateReason(19);
  public static final javax.print.attribute.standard.JobStateReason UNSUPPORTED_DOCUMENT_FORMAT = new javax.print.attribute.standard.JobStateReason(20);
  public static final javax.print.attribute.standard.JobStateReason DOCUMENT_FORMAT_ERROR = new javax.print.attribute.standard.JobStateReason(21);
  public static final javax.print.attribute.standard.JobStateReason PROCESSING_TO_STOP_POINT = new javax.print.attribute.standard.JobStateReason(22);
  public static final javax.print.attribute.standard.JobStateReason SERVICE_OFF_LINE = new javax.print.attribute.standard.JobStateReason(23);
  public static final javax.print.attribute.standard.JobStateReason JOB_COMPLETED_SUCCESSFULLY = new javax.print.attribute.standard.JobStateReason(24);
  public static final javax.print.attribute.standard.JobStateReason JOB_COMPLETED_WITH_WARNINGS = new javax.print.attribute.standard.JobStateReason(25);
  public static final javax.print.attribute.standard.JobStateReason JOB_COMPLETED_WITH_ERRORS = new javax.print.attribute.standard.JobStateReason(26);
  public static final javax.print.attribute.standard.JobStateReason JOB_RESTARTABLE = new javax.print.attribute.standard.JobStateReason(27);
  public static final javax.print.attribute.standard.JobStateReason QUEUED_IN_DEVICE = new javax.print.attribute.standard.JobStateReason(28);
  protected JobStateReason(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "job-state-reason"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.JobStateReason.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"job-incoming", "job-data-insufficient", "document-access-error", "submission-interrupted", "job-outgoing", "job-hold-until-specified", "resources-are-not-ready", "printer-stopped-partly", "printer-stopped", "job-interpreting", "job-queued", "job-transforming", "job-queued-for-marker", "job-printing", "job-canceled-by-user", "job-canceled-by-operator", "job-canceled-at-device", "aborted-by-system", "unsupported-compression", "compression-error", "unsupported-document-format", "document-format-error", "processing-to-stop-point", "service-off-line", "job-completed-successfully", "job-completed-with-warnings", "job-completed-with-errors", "job-restartable", "queued-in-device"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {JOB_INCOMING, JOB_DATA_INSUFFICIENT, DOCUMENT_ACCESS_ERROR, SUBMISSION_INTERRUPTED, JOB_OUTGOING, JOB_HOLD_UNTIL_SPECIFIED, RESOURCES_ARE_NOT_READY, PRINTER_STOPPED_PARTLY, PRINTER_STOPPED, JOB_INTERPRETING, JOB_QUEUED, JOB_TRANSFORMING, JOB_QUEUED_FOR_MARKER, JOB_PRINTING, JOB_CANCELED_BY_USER, JOB_CANCELED_BY_OPERATOR, JOB_CANCELED_AT_DEVICE, ABORTED_BY_SYSTEM, UNSUPPORTED_COMPRESSION, COMPRESSION_ERROR, UNSUPPORTED_DOCUMENT_FORMAT, DOCUMENT_FORMAT_ERROR, PROCESSING_TO_STOP_POINT, SERVICE_OFF_LINE, JOB_COMPLETED_SUCCESSFULLY, JOB_COMPLETED_WITH_WARNINGS, JOB_COMPLETED_WITH_ERRORS, JOB_RESTARTABLE, QUEUED_IN_DEVICE};
}
