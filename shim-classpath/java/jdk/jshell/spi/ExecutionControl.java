/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.spi;

public interface ExecutionControl extends java.lang.AutoCloseable {
  void addToClasspath(java.lang.String string) throws jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException;
  void close();
  java.lang.Object extensionCommand(java.lang.String string, java.lang.Object object) throws jdk.jshell.spi.ExecutionControl.RunException, jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException;
  static jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv executionEnv, java.lang.String string, java.util.Map<java.lang.String, java.lang.String> map) throws java.lang.Throwable {
    throw new UnsupportedOperationException();
  }
  static jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv executionEnv, java.lang.String string) throws java.lang.Throwable {
    throw new UnsupportedOperationException();
  }
  java.lang.String invoke(java.lang.String string, java.lang.String string2) throws jdk.jshell.spi.ExecutionControl.RunException, jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException;
  void load(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] classBytecodess) throws jdk.jshell.spi.ExecutionControl.ClassInstallException, jdk.jshell.spi.ExecutionControl.NotImplementedException, jdk.jshell.spi.ExecutionControl.EngineTerminationException;
  void redefine(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] classBytecodess) throws jdk.jshell.spi.ExecutionControl.ClassInstallException, jdk.jshell.spi.ExecutionControl.NotImplementedException, jdk.jshell.spi.ExecutionControl.EngineTerminationException;
  void stop() throws jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException;
  java.lang.String varValue(java.lang.String string, java.lang.String string2) throws jdk.jshell.spi.ExecutionControl.RunException, jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException;

  public static final class ClassBytecodes implements java.io.Serializable {
    private static final long serialVersionUID = 54506481972415973L;


    public ClassBytecodes(java.lang.String string, byte[] bs) {
    }

    public byte[] bytecodes() {
      throw new UnsupportedOperationException();
    }
    public java.lang.String name() {
      throw new UnsupportedOperationException();
    }
  }

  public static class ClassInstallException extends jdk.jshell.spi.ExecutionControl.ExecutionControlException {
    private static final long serialVersionUID = 1L;


    public ClassInstallException(java.lang.String string, boolean[] flags) {
      super(string);
    }

    public boolean[] installed() {
      throw new UnsupportedOperationException();
    }
  }

  public static class EngineTerminationException extends jdk.jshell.spi.ExecutionControl.ExecutionControlException {
    private static final long serialVersionUID = 1L;


    public EngineTerminationException(java.lang.String string) {
      super(string);
    }

  }

  public static abstract class ExecutionControlException extends java.lang.Exception {
    private static final long serialVersionUID = 1L;


    public ExecutionControlException(java.lang.String string) {
      super(string);
    }

  }

  public static class InternalException extends jdk.jshell.spi.ExecutionControl.ExecutionControlException {
    private static final long serialVersionUID = 1L;


    public InternalException(java.lang.String string) {
      super(string);
    }

  }

  public static class NotImplementedException extends jdk.jshell.spi.ExecutionControl.InternalException {
    private static final long serialVersionUID = 1L;


    public NotImplementedException(java.lang.String string) {
      super(string);
    }

  }

  public static class ResolutionException extends jdk.jshell.spi.ExecutionControl.RunException {
    private static final long serialVersionUID = 1L;


    private final int id;

    public ResolutionException(int id, java.lang.StackTraceElement[] stackTrace) {
      super("resolution exception");
      this.id = id;
      this.setStackTrace(stackTrace);
    }

    public int id() {
      return id;
    }
  }

  public static abstract class RunException extends jdk.jshell.spi.ExecutionControl.ExecutionControlException {
    private static final long serialVersionUID = 1L;


    protected RunException(java.lang.String string) {
      super(string);
    }

  }

  public static class StoppedException extends jdk.jshell.spi.ExecutionControl.RunException {
    private static final long serialVersionUID = 1L;


    public StoppedException() {
      super("stopped by user");
    }

  }

  public static class UserException extends jdk.jshell.spi.ExecutionControl.RunException {
    private static final long serialVersionUID = 1L;


    private final String causeExceptionClass;

    public UserException(java.lang.String message, java.lang.String causeExceptionClass,
        java.lang.StackTraceElement[] stackTrace) {
      super(message);
      this.causeExceptionClass = causeExceptionClass;
      this.setStackTrace(stackTrace);
    }

    public java.lang.String causeExceptionClass() {
      return causeExceptionClass;
    }
  }
}
