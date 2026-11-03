/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.timer;

public interface TimerMBean {
  java.lang.Integer addNotification(java.lang.String string, java.lang.String string2, java.lang.Object object, java.util.Date date, long l, long l2, boolean flag) throws java.lang.IllegalArgumentException;
  java.lang.Integer addNotification(java.lang.String string, java.lang.String string2, java.lang.Object object, java.util.Date date, long l, long l2) throws java.lang.IllegalArgumentException;
  java.lang.Integer addNotification(java.lang.String string, java.lang.String string2, java.lang.Object object, java.util.Date date, long l) throws java.lang.IllegalArgumentException;
  java.lang.Integer addNotification(java.lang.String string, java.lang.String string2, java.lang.Object object, java.util.Date date) throws java.lang.IllegalArgumentException;
  java.util.Vector<java.lang.Integer> getAllNotificationIDs();
  java.util.Date getDate(java.lang.Integer integer);
  java.lang.Boolean getFixedRate(java.lang.Integer integer);
  int getNbNotifications();
  java.lang.Long getNbOccurences(java.lang.Integer integer);
  java.util.Vector<java.lang.Integer> getNotificationIDs(java.lang.String string);
  java.lang.String getNotificationMessage(java.lang.Integer integer);
  java.lang.String getNotificationType(java.lang.Integer integer);
  java.lang.Object getNotificationUserData(java.lang.Integer integer);
  java.lang.Long getPeriod(java.lang.Integer integer);
  boolean getSendPastNotifications();
  boolean isActive();
  boolean isEmpty();
  void removeAllNotifications();
  void removeNotification(java.lang.Integer integer) throws javax.management.InstanceNotFoundException;
  void removeNotifications(java.lang.String string) throws javax.management.InstanceNotFoundException;
  void setSendPastNotifications(boolean flag);
  void start();
  void stop();
}
