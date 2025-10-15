/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SQLException extends Exception implements Iterable<Throwable> {
  private static final long serialVersionUID = 2135244094396331484L;

  private String SQLState;
  private int vendorCode;
  private volatile SQLException next;

  public SQLException(String reason, String SQLState, int vendorCode) {
    super(reason);
    this.SQLState = SQLState;
    this.vendorCode = vendorCode;
    logCodeThenTrace();
  }

  public SQLException(String reason, String SQLState) {
    super(reason);
    this.SQLState = SQLState;
    this.vendorCode = 0;
    logTraceThen("SQLException: SQLState(" + SQLState + ")");
  }

  public SQLException(String reason) {
    super(reason);
    this.SQLState = null;
    this.vendorCode = 0;
    logTraceThen(null);
  }

  public SQLException() {
    super();
    this.SQLState = null;
    this.vendorCode = 0;
    logTraceThen(null);
  }

  public SQLException(Throwable cause) {
    super(cause);
    logTraceThen(null);
  }

  public SQLException(String reason, Throwable cause) {
    super(reason, cause);
    logTraceThen(null);
  }

  public SQLException(String reason, String sqlState, Throwable cause) {
    super(reason, cause);
    this.SQLState = sqlState;
    this.vendorCode = 0;
    logTraceThen("SQLState(" + sqlState + ")");
  }

  public SQLException(String reason, String sqlState, int vendorCode, Throwable cause) {
    super(reason, cause);
    this.SQLState = sqlState;
    this.vendorCode = vendorCode;
    logCodeThenTrace();
  }

  private void logCodeThenTrace() {
    if (!(this instanceof SQLWarning) && DriverManager.getLogWriter() != null) {
      DriverManager.println("SQLState(" + SQLState + ") vendor code(" + vendorCode + ")");
      printStackTrace(DriverManager.getLogWriter());
    }
  }

  private void logTraceThen(String trailer) {
    if (!(this instanceof SQLWarning) && DriverManager.getLogWriter() != null) {
      printStackTrace(DriverManager.getLogWriter());
      if (trailer != null) {
        DriverManager.println(trailer);
      }
    }
  }

  public String getSQLState() {
    return SQLState;
  }

  public int getErrorCode() {
    return vendorCode;
  }

  public SQLException getNextException() {
    return next;
  }

  public synchronized void setNextException(SQLException ex) {
    SQLException current = this;
    for (;;) {
      SQLException following = current.next;
      if (following != null) {
        current = following;
        continue;
      }
      current.next = ex;
      return;
    }
  }

  public Iterator<Throwable> iterator() {
    return new Iterator<Throwable>() {
      private SQLException firstException = SQLException.this;
      private SQLException nextException = firstException.getNextException();
      private Throwable cause = firstException.getCause();

      public boolean hasNext() {
        return firstException != null || nextException != null || cause != null;
      }

      public Throwable next() {
        Throwable throwable;
        if (firstException != null) {
          throwable = firstException;
          firstException = null;
        } else if (cause != null) {
          throwable = cause;
          cause = cause.getCause();
        } else if (nextException != null) {
          throwable = nextException;
          cause = nextException.getCause();
          nextException = nextException.getNextException();
        } else {
          throw new NoSuchElementException();
        }
        return throwable;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
