/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class StackWalker {
  public enum Option {
    RETAIN_CLASS_REFERENCE,
    SHOW_REFLECT_FRAMES,
    SHOW_HIDDEN_FRAMES,
    DROP_METHOD_INFO
  }

  public interface StackFrame {
    String getClassName();
    String getMethodName();
    Class<?> getDeclaringClass();
    MethodType getMethodType();
    String getDescriptor();
    int getByteCodeIndex();
    String getFileName();
    int getLineNumber();
    boolean isNativeMethod();
    StackTraceElement toStackTraceElement();
  }

  private final Set<Option> options;
  private final int estimateDepth;

  private StackWalker(Set<Option> options, int estimateDepth) {
    this.options = options;
    this.estimateDepth = estimateDepth;
  }

  public static StackWalker getInstance() {
    return new StackWalker(Collections.<Option>emptySet(), 0);
  }

  public static StackWalker getInstance(Option option) {
    Objects.requireNonNull(option, "option");
    return new StackWalker(EnumSet.of(option), 0);
  }

  public static StackWalker getInstance(Set<Option> options) {
    Objects.requireNonNull(options, "options");
    return new StackWalker(EnumSet.copyOf(options), 0);
  }

  public static StackWalker getInstance(Set<Option> options, int estimateDepth) {
    Objects.requireNonNull(options, "options");
    if (estimateDepth <= 0) {
      throw new IllegalArgumentException("estimateDepth must be > 0: " + estimateDepth);
    }
    return new StackWalker(EnumSet.copyOf(options), estimateDepth);
  }

  private List<StackFrame> captureFrames() {
    Throwable capture = new Throwable();
    Class<?>[] classes = capture.stackClasses();
    StackTraceElement[] elements = capture.getStackTrace();
    List<StackFrame> frames = new ArrayList<StackFrame>();
    boolean retainClass = options.contains(Option.RETAIN_CLASS_REFERENCE);
    boolean showReflect = options.contains(Option.SHOW_REFLECT_FRAMES);
    boolean showHidden = options.contains(Option.SHOW_HIDDEN_FRAMES);

    boolean passedSelf = false;
    for (int i = 0; i < elements.length; i++) {
      StackTraceElement ste = elements[i];
      String cname = ste.getClassName();
      if (!passedSelf) {
        if (cname.equals("java.lang.StackWalker") || cname.equals("java.lang.Throwable")) {
          continue;
        }
        passedSelf = true;
      }
      if (!showHidden) {
        if (cname.startsWith("java.lang.invoke.LambdaForm") || cname.startsWith("jdk.internal.")) {
          continue;
        }
      }
      if (!showReflect) {
        if (cname.equals("java.lang.reflect.Method") || cname.equals("java.lang.reflect.Constructor")) {
          continue;
        }
      }
      frames.add(new StackFrameImpl(ste, retainClass, classes != null && i < classes.length ? classes[i] : null));
    }
    return frames;
  }

  public <T> T walk(Function<? super Stream<StackFrame>, ? extends T> function) {
    Objects.requireNonNull(function, "function");
    List<StackFrame> frames = captureFrames();
    return function.apply(frames.stream());
  }

  public void forEach(Consumer<? super StackFrame> action) {
    Objects.requireNonNull(action, "action");
    walk(s -> {
      s.forEach(action);
      return null;
    });
  }

  public Class<?> getCallerClass() {
    if (!options.contains(Option.RETAIN_CLASS_REFERENCE)) {
      throw new UnsupportedOperationException("RETAIN_CLASS_REFERENCE option not set");
    }
    Throwable capture = new Throwable();
    Class<?>[] classes = capture.stackClasses();
    StackTraceElement[] elements = capture.getStackTrace();
    int idx = 0;
    while (idx < elements.length &&
           (elements[idx].getClassName().equals("java.lang.Throwable") ||
            elements[idx].getClassName().equals("java.lang.StackWalker"))) {
      idx++;
    }
    int callerIdx = idx + 1;
    if (callerIdx >= elements.length) {
      throw new IllegalCallerException("no caller frame");
    }
    if (classes != null && callerIdx < classes.length) {
      return classes[callerIdx];
    }
    try {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (cl == null) {
        cl = ClassLoader.getSystemClassLoader();
      }
      return Class.forName(elements[callerIdx].getClassName(), false, cl);
    } catch (ClassNotFoundException e) {
      throw new IllegalCallerException("unable to resolve caller class: " + elements[callerIdx].getClassName(), e);
    }
  }

  private static final class StackFrameImpl implements StackFrame {
    private final StackTraceElement ste;
    private final boolean retainClass;
    private Class<?> declaringClass;

    StackFrameImpl(StackTraceElement ste, boolean retainClass, Class<?> declaringClass) {
      this.ste = ste;
      this.retainClass = retainClass;
      this.declaringClass = declaringClass;
    }

    @Override
    public String getClassName() {
      return ste.getClassName();
    }

    @Override
    public String getMethodName() {
      return ste.getMethodName();
    }

    @Override
    public Class<?> getDeclaringClass() {
      if (!retainClass) {
        throw new UnsupportedOperationException("RETAIN_CLASS_REFERENCE option not set");
      }
      if (declaringClass == null) {
        try {
          ClassLoader cl = Thread.currentThread().getContextClassLoader();
          if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
          }
          declaringClass = Class.forName(ste.getClassName(), false, cl);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
      return declaringClass;
    }

    @Override
    public MethodType getMethodType() {
      if (!retainClass) {
        throw new UnsupportedOperationException("RETAIN_CLASS_REFERENCE option not set");
      }
      return MethodType.methodType(void.class);
    }

    @Override
    public String getDescriptor() {
      return "()V";
    }

    @Override
    public int getByteCodeIndex() {
      return -1;
    }

    @Override
    public String getFileName() {
      return ste.getFileName();
    }

    @Override
    public int getLineNumber() {
      return ste.getLineNumber();
    }

    @Override
    public boolean isNativeMethod() {
      return ste.isNativeMethod();
    }

    @Override
    public StackTraceElement toStackTraceElement() {
      return ste;
    }

    @Override
    public String toString() {
      return ste.getClassName() + "." + ste.getMethodName() + "(" +
        (ste.isNativeMethod() ? "Native Method" :
          (ste.getFileName() != null ? ste.getFileName() + ":" + ste.getLineNumber() : "Unknown Source")) + ")";
    }
  }
}
