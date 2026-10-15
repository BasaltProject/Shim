/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ProcessBuilder {
  private final List<String> command;
  private File directory;
  private Map<String, String> environment;
  private boolean redirectErrorStream;
  private Redirect[] redirects;

  public ProcessBuilder(List<String> command) {
    this.command = new ArrayList<String>(Objects.requireNonNull(command, "command"));
  }

  public ProcessBuilder(String... command) {
    this.command = new ArrayList<String>(Arrays.asList(Objects.requireNonNull(command, "command")));
  }

  public ProcessBuilder command(List<String> command) {
    this.command.clear();
    this.command.addAll(Objects.requireNonNull(command, "command"));
    return this;
  }

  public ProcessBuilder command(String... command) {
    this.command.clear();
    this.command.addAll(Arrays.asList(Objects.requireNonNull(command, "command")));
    return this;
  }

  public List<String> command() {
    return command;
  }

  public Map<String, String> environment() {
    if (environment == null) {
      environment = new HashMap<String, String>(System.getenv());
    }
    return environment;
  }

  ProcessBuilder environment(String[] envp) {
    if (envp != null) {
      environment = new HashMap<String, String>();
      for (String env : envp) {
        int idx = env.indexOf('=');
        if (idx > 0) {
          environment.put(env.substring(0, idx), env.substring(idx + 1));
        }
      }
    }
    return this;
  }

  public File directory() {
    return directory;
  }

  public ProcessBuilder directory(File directory) {
    this.directory = directory;
    return this;
  }

  private Redirect[] redirects() {
    if (redirects == null) {
      redirects = new Redirect[] { Redirect.PIPE, Redirect.PIPE, Redirect.PIPE };
    }
    return redirects;
  }

  public ProcessBuilder redirectInput(Redirect source) {
    Objects.requireNonNull(source, "source");
    if (source.type() == Redirect.Type.WRITE || source.type() == Redirect.Type.APPEND) {
      throw new IllegalArgumentException("Redirect invalid for input: " + source);
    }
    redirects()[0] = source;
    return this;
  }

  public ProcessBuilder redirectOutput(Redirect destination) {
    Objects.requireNonNull(destination, "destination");
    if (destination.type() == Redirect.Type.READ) {
      throw new IllegalArgumentException("Redirect invalid for output: " + destination);
    }
    redirects()[1] = destination;
    return this;
  }

  public ProcessBuilder redirectError(Redirect destination) {
    Objects.requireNonNull(destination, "destination");
    if (destination.type() == Redirect.Type.READ) {
      throw new IllegalArgumentException("Redirect invalid for error: " + destination);
    }
    redirects()[2] = destination;
    return this;
  }

  public ProcessBuilder redirectInput(File file) {
    return redirectInput(Redirect.from(file));
  }

  public ProcessBuilder redirectOutput(File file) {
    return redirectOutput(Redirect.to(file));
  }

  public ProcessBuilder redirectError(File file) {
    return redirectError(Redirect.to(file));
  }

  public Redirect redirectInput() {
    return redirects()[0];
  }

  public Redirect redirectOutput() {
    return redirects()[1];
  }

  public Redirect redirectError() {
    return redirects()[2];
  }

  public ProcessBuilder inheritIO() {
    Arrays.fill(redirects(), Redirect.INHERIT);
    return this;
  }

  public boolean redirectErrorStream() {
    return redirectErrorStream;
  }

  public ProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
    this.redirectErrorStream = redirectErrorStream;
    return this;
  }

  public Process start() throws IOException {
    if (command.isEmpty()) {
      throw new IndexOutOfBoundsException("command is empty");
    }
    String[] cmd = command.toArray(new String[command.size()]);
    for (String arg : cmd) {
      if (arg == null) {
        throw new NullPointerException("command element is null");
      }
    }
    return Runtime.getRuntime().exec(cmd);
  }

  public static List<Process> startPipeline(List<ProcessBuilder> builders) throws IOException {
    Objects.requireNonNull(builders, "builders");
    if (builders.isEmpty()) {
      throw new IllegalArgumentException("empty pipeline");
    }
    List<Process> processes = new ArrayList<Process>(builders.size());
    for (ProcessBuilder b : builders) {
      processes.add(b.start());
    }
    return processes;
  }

  public static abstract class Redirect {
    public enum Type {
      PIPE,
      INHERIT,
      READ,
      WRITE,
      APPEND
    }

    public static final Redirect PIPE = new SimpleRedirect(Type.PIPE);
    public static final Redirect INHERIT = new SimpleRedirect(Type.INHERIT);
    public static final Redirect DISCARD = new SimpleRedirect(Type.WRITE);

    public abstract Type type();

    public File file() {
      return null;
    }

    boolean append() {
      return false;
    }

    public static Redirect from(File file) {
      return new FileRedirect(Type.READ, Objects.requireNonNull(file, "file"), false);
    }

    public static Redirect to(File file) {
      return new FileRedirect(Type.WRITE, Objects.requireNonNull(file, "file"), false);
    }

    public static Redirect appendTo(File file) {
      return new FileRedirect(Type.APPEND, Objects.requireNonNull(file, "file"), true);
    }

    private static final class SimpleRedirect extends Redirect {
      private final Type type;

      SimpleRedirect(Type type) {
        this.type = type;
      }

      @Override
      public Type type() {
        return type;
      }

      @Override
      public boolean equals(Object obj) {
        return (obj instanceof SimpleRedirect) && ((SimpleRedirect) obj).type == this.type;
      }

      @Override
      public int hashCode() {
        return type.hashCode();
      }

      @Override
      public String toString() {
        return "Redirect." + type.name();
      }
    }

    private static final class FileRedirect extends Redirect {
      private final Type type;
      private final File file;
      private final boolean append;

      FileRedirect(Type type, File file, boolean append) {
        this.type = type;
        this.file = file;
        this.append = append;
      }

      @Override
      public Type type() {
        return type;
      }

      @Override
      public File file() {
        return file;
      }

      @Override
      boolean append() {
        return append;
      }

      @Override
      public boolean equals(Object obj) {
        if (!(obj instanceof FileRedirect)) return false;
        FileRedirect other = (FileRedirect) obj;
        return this.type == other.type && Objects.equals(this.file, other.file) && this.append == other.append;
      }

      @Override
      public int hashCode() {
        return Objects.hash(type, file, append);
      }

      @Override
      public String toString() {
        return "Redirect[" + type + " " + file + "]";
      }
    }
  }
}
