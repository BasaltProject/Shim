/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class DriverManager {
  private static final CopyOnWriteArrayList<DriverInfo> registeredDrivers = new CopyOnWriteArrayList<DriverInfo>();
  private static volatile int loginTimeout = 0;
  private static volatile PrintWriter logWriter = null;
  private static volatile PrintStream logStream = null;
  private static final Object logSync = new Object();
  private static volatile boolean driversInitialized;

  private DriverManager() {
  }

  public static PrintWriter getLogWriter() {
    return logWriter;
  }

  public static void setLogWriter(PrintWriter out) {
    SecurityManagerChecks.check("setLog");
    logStream = null;
    logWriter = out;
  }

  public static Connection getConnection(String url, Properties info) throws SQLException {
    return getConnection(url, info, callerLoader());
  }

  public static Connection getConnection(String url, String user, String password) throws SQLException {
    Properties info = new Properties();
    if (user != null) {
      info.put("user", user);
    }
    if (password != null) {
      info.put("password", password);
    }
    return getConnection(url, info, callerLoader());
  }

  public static Connection getConnection(String url) throws SQLException {
    Properties info = new Properties();
    return getConnection(url, info, callerLoader());
  }

  public static Driver getDriver(String url) throws SQLException {
    println("DriverManager.getDriver(\"" + url + "\")");
    ensureDriversInitialized();
    ClassLoader callerCL = callerLoader();
    for (DriverInfo aDriver : registeredDrivers) {
      if (isDriverAllowed(aDriver.driver, callerCL)) {
        try {
          if (aDriver.driver.acceptsURL(url)) {
            println("getDriver returning " + aDriver.driver.getClass().getName());
            return aDriver.driver;
          }
        } catch (SQLException sqe) {
        }
      } else {
        println("    skipping: " + aDriver.driver.getClass().getName());
      }
    }
    println("getDriver: no suitable driver");
    throw new SQLException("No suitable driver", "08001");
  }

  public static synchronized void registerDriver(Driver driver) throws SQLException {
    registerDriver(driver, null);
  }

  public static synchronized void registerDriver(Driver driver, DriverAction da) throws SQLException {
    if (driver != null) {
      registeredDrivers.addIfAbsent(new DriverInfo(driver, da));
    } else {
      throw new NullPointerException();
    }
    println("registerDriver: " + driver);
  }

  public static synchronized void deregisterDriver(Driver driver) throws SQLException {
    if (driver == null) {
      return;
    }
    println("DriverManager.deregisterDriver: " + driver);
    DriverInfo aDriver = new DriverInfo(driver, null);
    int index = registeredDrivers.indexOf(aDriver);
    if (index >= 0) {
      DriverInfo di = registeredDrivers.get(index);
      if (di.action() != null) {
        di.action().deregister();
      }
      registeredDrivers.remove(di);
    } else {
      println("    couldn't find driver to unload");
    }
  }

  public static Enumeration<Driver> getDrivers() {
    ensureDriversInitialized();
    ClassLoader callerCL = callerLoader();
    List<Driver> result = new ArrayList<Driver>();
    for (DriverInfo aDriver : registeredDrivers) {
      if (isDriverAllowed(aDriver.driver, callerCL)) {
        result.add(aDriver.driver);
      } else {
        println("    skipping: " + aDriver.getClass().getName());
      }
    }
    return java.util.Collections.enumeration(result);
  }

  public static Stream<Driver> drivers() {
    ensureDriversInitialized();
    ClassLoader callerCL = callerLoader();
    return registeredDrivers.stream().filter(aDriver -> isDriverAllowed(aDriver.driver, callerCL))
        .map(di -> di.driver);
  }

  public static void setLoginTimeout(int seconds) {
    loginTimeout = seconds;
  }

  public static int getLoginTimeout() {
    return loginTimeout;
  }

  @Deprecated
  public static void setLogStream(PrintStream out) {
    SecurityManagerChecks.check("setLog");
    logStream = out;
    if (out != null) {
      logWriter = new PrintWriter(out);
    } else {
      logWriter = null;
    }
  }

  @Deprecated
  public static PrintStream getLogStream() {
    return logStream;
  }

  public static void println(String message) {
    synchronized (logSync) {
      if (logWriter != null) {
        logWriter.println(message);
        logWriter.flush();
      }
    }
  }

  private static ClassLoader callerLoader() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return cl != null ? cl : ClassLoader.getSystemClassLoader();
  }

  private static boolean isDriverAllowed(Driver driver, ClassLoader classLoader) {
    boolean result = false;
    if (driver != null) {
      Class<?> aClass = null;
      try {
        aClass = Class.forName(driver.getClass().getName(), true, classLoader);
      } catch (Exception ex) {
        result = false;
      }
      result = (aClass == driver.getClass());
    }
    return result;
  }

  private static void ensureDriversInitialized() {
    if (driversInitialized) {
      return;
    }
    synchronized (DriverManager.class) {
      if (driversInitialized) {
        return;
      }
      String drivers = System.getProperty("jdbc.drivers");
      if (drivers != null && !drivers.isEmpty()) {
        for (String name : drivers.split(":")) {
          try {
            Class.forName(name, true, ClassLoader.getSystemClassLoader());
          } catch (Exception ex) {
            println("DriverManager.Initialize: load failed: " + ex);
          }
        }
      }
      try {
        Iterator<Driver> it = ServiceLoader.load(Driver.class).iterator();
        while (it.hasNext()) {
          try {
            it.next();
          } catch (Throwable t) {
          }
        }
      } catch (Throwable t) {
      }
      driversInitialized = true;
    }
  }

  private static Connection getConnection(String url, Properties info, ClassLoader callerCL) throws SQLException {
    if (callerCL == null) {
      callerCL = callerLoader();
    }
    if (url == null) {
      throw new SQLException("The url cannot be null", "08001");
    }
    println("DriverManager.getConnection(\"" + url + "\")");
    ensureDriversInitialized();
    SQLException reason = null;
    for (DriverInfo aDriver : registeredDrivers) {
      if (isDriverAllowed(aDriver.driver, callerCL)) {
        try {
          println("    trying " + aDriver.driver.getClass().getName());
          Connection con = aDriver.driver.connect(url, info);
          if (con != null) {
            println("getConnection returning " + aDriver.driver.getClass().getName());
            return con;
          }
        } catch (SQLException ex) {
          if (reason == null) {
            reason = ex;
          }
        }
      } else {
        println("    skipping: " + aDriver.getClass().getName());
      }
    }
    if (reason != null) {
      println("getConnection failed: " + reason);
      throw reason;
    }
    println("getConnection: no suitable driver found for " + url);
    throw new SQLException("No suitable driver found for " + url, "08001");
  }

  private static final class SecurityManagerChecks {
    static void check(String name) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPermission(new SQLPermission(name));
      }
    }
  }
}
