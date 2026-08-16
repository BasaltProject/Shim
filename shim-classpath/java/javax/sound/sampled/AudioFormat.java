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

public class AudioFormat {
  protected Encoding encoding;
  protected float sampleRate;
  protected int sampleSizeInBits;
  protected int channels;
  protected int frameSize;
  protected float frameRate;
  protected boolean bigEndian;
  private HashMap<String, Object> properties;

  public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize,
      float frameRate, boolean bigEndian) {
    this.encoding = encoding;
    this.sampleRate = sampleRate;
    this.sampleSizeInBits = sampleSizeInBits;
    this.channels = channels;
    this.frameSize = frameSize;
    this.frameRate = frameRate;
    this.bigEndian = bigEndian;
    this.properties = null;
  }

  public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize,
      float frameRate, boolean bigEndian, Map<String, Object> properties) {
    this(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    this.properties = new HashMap<>(properties);
  }

  public AudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian) {
    this((signed == true ? Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED), sampleRate, sampleSizeInBits, channels,
        (channels == AudioSystem.NOT_SPECIFIED || sampleSizeInBits == AudioSystem.NOT_SPECIFIED)
            ? AudioSystem.NOT_SPECIFIED : ((sampleSizeInBits + 7) / 8) * channels, sampleRate, bigEndian);
  }

  public Encoding getEncoding() { return encoding; }

  public float getSampleRate() { return sampleRate; }

  public int getSampleSizeInBits() { return sampleSizeInBits; }

  public int getChannels() { return channels; }

  public int getFrameSize() { return frameSize; }

  public float getFrameRate() { return frameRate; }

  public boolean isBigEndian() { return bigEndian; }

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

  public boolean matches(AudioFormat format) {
    if (format.getEncoding().equals(getEncoding())
        && (format.getChannels() == AudioSystem.NOT_SPECIFIED || format.getChannels() == getChannels())
        && (format.getSampleRate() == (float) AudioSystem.NOT_SPECIFIED || format.getSampleRate() == getSampleRate())
        && (format.getSampleSizeInBits() == AudioSystem.NOT_SPECIFIED
            || format.getSampleSizeInBits() == getSampleSizeInBits())
        && (format.getFrameRate() == (float) AudioSystem.NOT_SPECIFIED || format.getFrameRate() == getFrameRate())
        && (format.getFrameSize() == AudioSystem.NOT_SPECIFIED || format.getFrameSize() == getFrameSize())
        && (getSampleSizeInBits() <= 8 || format.isBigEndian() == isBigEndian())) {
      return true;
    }
    return false;
  }

  public String toString() {
    String sampleRate = getSampleRate() == (float) AudioSystem.NOT_SPECIFIED ? "unknown sample rate"
        : "" + getSampleRate() + " Hz";
    String sampleSize = getSampleSizeInBits() == (float) AudioSystem.NOT_SPECIFIED ? "unknown bits per sample"
        : "" + getSampleSizeInBits() + " bit";
    String channels = switchChannels(getChannels());
    String frameSize = getFrameSize() == (float) AudioSystem.NOT_SPECIFIED ? "unknown frame size"
        : "" + getFrameSize() + " bytes/frame";
    String frameRate = "";
    if (Math.abs(getSampleRate() - getFrameRate()) > 0.00001) {
      if (getFrameRate() == (float) AudioSystem.NOT_SPECIFIED) {
        frameRate = ", unknown frame rate";
      } else {
        frameRate = ", " + getFrameRate() + " frames/second";
      }
    }
    String bigEndian = "";
    if ((getEncoding().equals(Encoding.PCM_SIGNED) || getEncoding().equals(Encoding.PCM_UNSIGNED))
        && ((getSampleSizeInBits() > 8) || (getSampleSizeInBits() == AudioSystem.NOT_SPECIFIED))) {
      if (isBigEndian()) {
        bigEndian = ", big-endian";
      } else {
        bigEndian = ", little-endian";
      }
    }
    return String.format("%s %s, %s, %s, %s%s%s", getEncoding(), sampleRate, sampleSize, channels, frameSize,
        frameRate, bigEndian);
  }

  private static String switchChannels(int c) {
    switch (c) {
    case 1:
      return "mono";
    case 2:
      return "stereo";
    case AudioSystem.NOT_SPECIFIED:
      return "unknown number of channels";
    default:
      return "" + c + " channels";
    }
  }

  public static class Encoding {
    public static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED");
    public static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED");
    public static final Encoding PCM_FLOAT = new Encoding("PCM_FLOAT");
    public static final Encoding ULAW = new Encoding("ULAW");
    public static final Encoding ALAW = new Encoding("ALAW");

    private String name;

    public Encoding(String name) {
      this.name = name;
    }

    public final boolean equals(Object obj) {
      if (toString() == null) {
        return (obj != null) && (obj.toString() == null);
      }
      if (obj instanceof Encoding) {
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
  }
}
