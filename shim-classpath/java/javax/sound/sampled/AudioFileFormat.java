/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AudioFileFormat {
  private Type type;
  private int byteLength = AudioSystem.NOT_SPECIFIED;
  private AudioFormat format;
  private int frameLength = AudioSystem.NOT_SPECIFIED;
  private HashMap<String, Object> properties;

  protected AudioFileFormat(Type type, int byteLength, AudioFormat format, int frameLength) {
    this.type = type;
    this.byteLength = byteLength;
    this.format = format;
    this.frameLength = frameLength;
    this.properties = null;
  }

  public AudioFileFormat(Type type, AudioFormat format, int frameLength) {
    this(type, AudioSystem.NOT_SPECIFIED, format, frameLength);
  }

  public AudioFileFormat(Type type, AudioFormat format, int frameLength, Map<String, Object> properties) {
    this(type, AudioSystem.NOT_SPECIFIED, format, frameLength);
    this.properties = new HashMap<>(properties);
  }

  public Type getType() { return type; }

  public int getByteLength() { return byteLength; }

  public AudioFormat getFormat() { return format; }

  public int getFrameLength() { return frameLength; }

  public Map<String, Object> properties() {
    Map<String, Object> ret;
    if (properties == null) {
      ret = new HashMap<>(0);
    } else {
      ret = (Map<String, Object>) properties.clone();
    }
    return Collections.unmodifiableMap(ret);
  }

  public Object getProperty(String key) {
    if (properties == null) {
      return null;
    }
    return properties.get(key);
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    if (type != null) {
      buf.append(type.toString() + " (." + type.getExtension() + ") file");
    } else {
      buf.append("unknown file format");
    }
    if (byteLength != AudioSystem.NOT_SPECIFIED) {
      buf.append(", byte length: " + byteLength);
    }
    buf.append(", data format: " + format);
    if (frameLength != AudioSystem.NOT_SPECIFIED) {
      buf.append(", frame length: " + frameLength);
    }
    return new String(buf);
  }

  public static class Type {
    public static final Type WAVE = new Type("WAVE", "wav");
    public static final Type AU = new Type("AU", "au");
    public static final Type AIFF = new Type("AIFF", "aif");
    public static final Type AIFC = new Type("AIFF-C", "aifc");
    public static final Type SND = new Type("SND", "snd");

    private final String name;
    private final String extension;

    public Type(String name, String extension) {
      this.name = name;
      this.extension = extension;
    }

    public final boolean equals(Object obj) {
      if (toString() == null) {
        return (obj != null) && (obj.toString() == null);
      }
      if (obj instanceof Type) {
        return toString().equals(obj.toString());
      }
      return false;
    }

    public final int hashCode() {
      if (toString() == null) {
        return 0;
      }
      return toString().hashCode();
    }

    public final String toString() { return name; }

    public String getExtension() { return extension; }
  }
}
