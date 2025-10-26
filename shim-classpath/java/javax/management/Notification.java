/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.util.EventObject;

public class Notification extends EventObject {
  private static final long serialVersionUID = -7516092053498031989L;

  private String type;
  private long sequenceNumber;
  private long timeStamp;
  private Object userData;
  private String message = "";
  protected Object source;

  public Notification(String type, Object source, long sequenceNumber) {
    super(source);
    this.source = source;
    this.type = type;
    this.sequenceNumber = sequenceNumber;
    this.timeStamp = System.currentTimeMillis();
  }

  public Notification(String type, Object source, long sequenceNumber, String message) {
    super(source);
    this.source = source;
    this.type = type;
    this.sequenceNumber = sequenceNumber;
    this.timeStamp = System.currentTimeMillis();
    this.message = message;
  }

  public Notification(String type, Object source, long sequenceNumber, long timeStamp) {
    super(source);
    this.source = source;
    this.type = type;
    this.sequenceNumber = sequenceNumber;
    this.timeStamp = timeStamp;
  }

  public Notification(String type, Object source, long sequenceNumber, long timeStamp, String message) {
    super(source);
    this.source = source;
    this.type = type;
    this.sequenceNumber = sequenceNumber;
    this.timeStamp = timeStamp;
    this.message = message;
  }

  @Override
  public Object getSource() {
    return source;
  }

  public void setSource(Object source) {
    super.source = source;
    this.source = source;
  }

  public long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public String getType() {
    return type;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getMessage() {
    return message;
  }

  public Object getUserData() {
    return userData;
  }

  public void setUserData(Object userData) {
    this.userData = userData;
  }

  @Override
  public String toString() {
    return super.toString() + "[type=" + type + "][message=" + message + "]";
  }
}
