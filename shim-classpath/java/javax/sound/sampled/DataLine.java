/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

public interface DataLine extends javax.sound.sampled.Line {
  void flush();
  void start();
  void stop();
  int available();
  boolean isActive();
  javax.sound.sampled.AudioFormat getFormat();
  float getLevel();
  void drain();
  int getFramePosition();
  long getLongFramePosition();
  long getMicrosecondPosition();
  int getBufferSize();
  boolean isRunning();

  public static class Info extends Line.Info {
    private final AudioFormat[] formats;
    private final int minBufferSize;
    private final int maxBufferSize;

    public Info(Class<?> lineClass, AudioFormat[] formats, int minBufferSize, int maxBufferSize) {
      super(lineClass);
      if (formats == null) {
        this.formats = new AudioFormat[0];
      } else {
        this.formats = java.util.Arrays.copyOf(formats, formats.length);
      }
      this.minBufferSize = minBufferSize;
      this.maxBufferSize = maxBufferSize;
    }

    public Info(Class<?> lineClass, AudioFormat format, int bufferSize) {
      super(lineClass);
      if (format == null) {
        this.formats = new AudioFormat[0];
      } else {
        this.formats = new AudioFormat[] {format};
      }
      this.minBufferSize = bufferSize;
      this.maxBufferSize = bufferSize;
    }

    public Info(Class<?> lineClass, AudioFormat format) { this(lineClass, format, AudioSystem.NOT_SPECIFIED); }

    public AudioFormat[] getFormats() { return java.util.Arrays.copyOf(formats, formats.length); }

    public boolean isFormatSupported(AudioFormat format) {
      for (int i = 0; i < formats.length; i++) {
        if (format.matches(formats[i])) {
          return true;
        }
      }
      return false;
    }

    public int getMinBufferSize() { return minBufferSize; }

    public int getMaxBufferSize() { return maxBufferSize; }

    public boolean matches(Line.Info info) {
      if (!(super.matches(info))) {
        return false;
      }
      Info dataLineInfo = (Info) info;
      if ((getMaxBufferSize() >= 0) && (dataLineInfo.getMaxBufferSize() >= 0)) {
        if (getMaxBufferSize() > dataLineInfo.getMaxBufferSize()) {
          return false;
        }
      }
      if ((getMinBufferSize() >= 0) && (dataLineInfo.getMinBufferSize() >= 0)) {
        if (getMinBufferSize() < dataLineInfo.getMinBufferSize()) {
          return false;
        }
      }
      AudioFormat[] localFormats = getFormats();
      if (localFormats != null) {
        for (int i = 0; i < localFormats.length; i++) {
          if (!(localFormats[i] == null)) {
            if (!(dataLineInfo.isFormatSupported(localFormats[i]))) {
              return false;
            }
          }
        }
      }
      return true;
    }

    public String toString() {
      StringBuilder format = new StringBuilder();
      if ((formats.length == 1) && (formats[0] != null)) {
        format.append(" supporting format " + formats[0]);
      } else if (getFormats().length > 1) {
        format.append(" supporting " + getFormats().length + " audio formats");
      }
      StringBuilder buffers = new StringBuilder();
      if ((minBufferSize != AudioSystem.NOT_SPECIFIED) && (maxBufferSize != AudioSystem.NOT_SPECIFIED)) {
        buffers.append(", and buffers of " + minBufferSize + " to " + maxBufferSize + " bytes");
      } else if ((minBufferSize != AudioSystem.NOT_SPECIFIED) && (minBufferSize > 0)) {
        buffers.append(", and buffers of at least " + minBufferSize + " bytes");
      } else if (maxBufferSize != AudioSystem.NOT_SPECIFIED) {
        buffers.append(", and buffers of up to " + minBufferSize + " bytes");
      }
      return String.valueOf(super.toString() + format + buffers);
    }
  }
}
