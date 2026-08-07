/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PrinterStateReasons extends java.util.HashMap<javax.print.attribute.standard.PrinterStateReason, javax.print.attribute.standard.Severity> implements javax.print.attribute.PrintServiceAttribute {
  public PrinterStateReasons() {
  }
  public PrinterStateReasons(java.util.Map<javax.print.attribute.standard.PrinterStateReason, javax.print.attribute.standard.Severity> p0) {
    super(p0);
  }
  public PrinterStateReasons(int p0, float p1) {
    super(p0, p1);
  }
  public PrinterStateReasons(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "printer-state-reasons"; }
  public javax.print.attribute.standard.Severity put(javax.print.attribute.standard.PrinterStateReason p0, javax.print.attribute.standard.Severity p1) { if (p0 == null) {
    throw new NullPointerException("reason is null");
  }
  if (p1 == null) {
    throw new NullPointerException("severity is null");
  }
  return super.put(p0, p1); }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return PrinterStateReasons.class; }
  public java.util.Set<javax.print.attribute.standard.PrinterStateReason> printerStateReasonSet(javax.print.attribute.standard.Severity p0) { if (p0 == null) {
    throw new NullPointerException("severity is null");
  }
  return new PrinterStateReasonSet(p0, entrySet()); }

  private class PrinterStateReasonSet extends java.util.AbstractSet<PrinterStateReason> {
    private final Severity mySeverity;
    private final java.util.Set<java.util.Map.Entry<PrinterStateReason, Severity>> myEntrySet;

    PrinterStateReasonSet(Severity severity, java.util.Set<java.util.Map.Entry<PrinterStateReason, Severity>> entrySet) {
      mySeverity = severity;
      myEntrySet = entrySet;
    }

    public int size() {
      int result = 0;
      for (java.util.Map.Entry<PrinterStateReason, Severity> e : myEntrySet) {
        if (e.getValue() == mySeverity) {
          result++;
        }
      }
      return result;
    }

    public java.util.Iterator<PrinterStateReason> iterator() {
      final java.util.Iterator<java.util.Map.Entry<PrinterStateReason, Severity>> it = myEntrySet.iterator();
      return new java.util.Iterator<PrinterStateReason>() {
        private java.util.Map.Entry<PrinterStateReason, Severity> next = advance();

        private java.util.Map.Entry<PrinterStateReason, Severity> advance() {
          while (it.hasNext()) {
            java.util.Map.Entry<PrinterStateReason, Severity> e = it.next();
            if (e.getValue() == mySeverity) {
              return e;
            }
          }
          return null;
        }

        public boolean hasNext() { return next != null; }

        public PrinterStateReason next() {
          if (next == null) {
            throw new java.util.NoSuchElementException();
          }
          PrinterStateReason r = next.getKey();
          next = advance();
          return r;
        }

        public void remove() { throw new UnsupportedOperationException(); }
      };
    }
  }
}
