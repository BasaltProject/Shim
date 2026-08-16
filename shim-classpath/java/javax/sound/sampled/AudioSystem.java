/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat.Encoding;

public class AudioSystem {
  public static final int NOT_SPECIFIED = -1;

  private AudioSystem() { }

  private static final Mixer sharedMixer = new shim.sound.NullMixer();

  public static Mixer.Info[] getMixerInfo() { return new Mixer.Info[] {shim.sound.NullMixer.info}; }

  public static Mixer getMixer(Mixer.Info info) {
    if (info == null || info == shim.sound.NullMixer.info) {
      return sharedMixer;
    }
    throw new IllegalArgumentException("Mixer not supported: " + info);
  }

  public static Line.Info[] getSourceLineInfo(Line.Info info) { return sharedMixer.getSourceLineInfo(info); }

  public static Line.Info[] getTargetLineInfo(Line.Info info) { return sharedMixer.getTargetLineInfo(info); }

  public static boolean isLineSupported(Line.Info info) { return sharedMixer.isLineSupported(info); }

  public static Line getLine(Line.Info info) throws LineUnavailableException { return sharedMixer.getLine(info); }

  public static Clip getClip() throws LineUnavailableException {
    return (Clip) getLine(new DataLine.Info(Clip.class, new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, -1f, 16, 2,
        4, -1f, false)));
  }

  public static Clip getClip(Mixer.Info mixerInfo) throws LineUnavailableException {
    getMixer(mixerInfo);
    return getClip();
  }

  public static SourceDataLine getSourceDataLine(AudioFormat format) throws LineUnavailableException {
    return (SourceDataLine) getLine(new DataLine.Info(SourceDataLine.class, format));
  }

  public static SourceDataLine getSourceDataLine(AudioFormat format, Mixer.Info mixerinfo)
      throws LineUnavailableException {
    getMixer(mixerinfo);
    return getSourceDataLine(format);
  }

  public static TargetDataLine getTargetDataLine(AudioFormat format) throws LineUnavailableException {
    return (TargetDataLine) getLine(new DataLine.Info(TargetDataLine.class, format));
  }

  public static TargetDataLine getTargetDataLine(AudioFormat format, Mixer.Info mixerinfo)
      throws LineUnavailableException {
    getMixer(mixerinfo);
    return getTargetDataLine(format);
  }

  public static Encoding[] getTargetEncodings(Encoding sourceEncoding) {
    if (Encoding.PCM_SIGNED.equals(sourceEncoding) || Encoding.PCM_UNSIGNED.equals(sourceEncoding)) {
      return new Encoding[] {Encoding.PCM_SIGNED, Encoding.PCM_UNSIGNED, Encoding.ULAW, Encoding.ALAW};
    }
    if (Encoding.ULAW.equals(sourceEncoding) || Encoding.ALAW.equals(sourceEncoding)) {
      return new Encoding[] {Encoding.PCM_SIGNED};
    }
    return new Encoding[0];
  }

  public static Encoding[] getTargetEncodings(AudioFormat sourceFormat) {
    return getTargetEncodings(sourceFormat.getEncoding());
  }

  public static boolean isConversionSupported(Encoding targetEncoding, AudioFormat sourceFormat) {
    for (Encoding e : getTargetEncodings(sourceFormat)) {
      if (e.equals(targetEncoding)) {
        return true;
      }
    }
    return false;
  }

  public static AudioFormat[] getTargetFormats(Encoding targetEncoding, AudioFormat sourceFormat) {
    if (!isConversionSupported(targetEncoding, sourceFormat)) {
      return new AudioFormat[0];
    }
    if (Encoding.PCM_SIGNED.equals(targetEncoding) || Encoding.PCM_UNSIGNED.equals(targetEncoding)) {
      List<AudioFormat> out = new ArrayList<>();
      int[] sizes = {8, 16, 24, 32};
      for (int bits : sizes) {
        for (boolean be : new boolean[] {false, true}) {
          if (bits == 8 && be) {
            continue;
          }
          int fs = bits / 8 * sourceFormat.getChannels();
          out.add(new AudioFormat(targetEncoding, sourceFormat.getSampleRate(), bits, sourceFormat.getChannels(), fs,
              sourceFormat.getSampleRate(), be));
        }
      }
      return out.toArray(new AudioFormat[0]);
    }
    return new AudioFormat[] {new AudioFormat(targetEncoding, sourceFormat.getSampleRate(), 8,
        sourceFormat.getChannels(), sourceFormat.getChannels(), sourceFormat.getSampleRate(), false)};
  }

  public static boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat) {
    if (sourceFormat.matches(targetFormat)) {
      return true;
    }
    if (!isConversionSupported(targetFormat.getEncoding(), sourceFormat)) {
      return false;
    }
    return targetFormat.getChannels() == sourceFormat.getChannels();
  }

  public static AudioInputStream getAudioInputStream(Encoding targetEncoding, AudioInputStream sourceStream) {
    if (sourceStream.getFormat().getEncoding().equals(targetEncoding)) {
      return sourceStream;
    }
    AudioFormat src = sourceStream.getFormat();
    int bits = Encoding.PCM_SIGNED.equals(targetEncoding) || Encoding.PCM_UNSIGNED.equals(targetEncoding)
        ? (src.getSampleSizeInBits() == 8 && (Encoding.ULAW.equals(src.getEncoding()) || Encoding.ALAW.equals(src.getEncoding()))
            ? 16 : src.getSampleSizeInBits()) : 8;
    AudioFormat target = new AudioFormat(targetEncoding, src.getSampleRate(), bits, src.getChannels(),
        bits / 8 * src.getChannels(), src.getSampleRate(), src.isBigEndian() && bits > 8);
    return getAudioInputStream(target, sourceStream);
  }

  public static AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {
    if (sourceStream.getFormat().matches(targetFormat)) {
      return sourceStream;
    }
    if (!isConversionSupported(targetFormat, sourceStream.getFormat())) {
      throw new IllegalArgumentException("Unsupported conversion: " + targetFormat + " from " + sourceStream.getFormat());
    }
    try {
      byte[] data = sourceStream.readAllBytes();
      AudioFormat src = sourceStream.getFormat();
      int channels = src.getChannels();
      int frames = data.length / Math.max(1, src.getFrameSize());
      int n = frames * channels;
      double[] samples = new double[n];
      for (int i = 0; i < n; i++) {
        samples[i] = decode(data, i, src);
      }
      byte[] out = new byte[n * (targetFormat.getSampleSizeInBits() / 8)];
      for (int i = 0; i < n; i++) {
        encode(out, i, samples[i], targetFormat);
      }
      return new AudioInputStream(new ByteArrayInputStream(out), targetFormat, frames);
    } catch (IOException e) {
      throw new IllegalArgumentException(e.toString());
    }
  }

  private static double decode(byte[] d, int i, AudioFormat f) {
    Encoding enc = f.getEncoding();
    if (Encoding.ULAW.equals(enc)) {
      return ulaw(d[i]) / 32768.0;
    }
    if (Encoding.ALAW.equals(enc)) {
      return alaw(d[i]) / 32768.0;
    }
    int bytes = f.getSampleSizeInBits() / 8;
    int off = i * bytes;
    long v = 0;
    for (int b = 0; b < bytes; b++) {
      int idx = f.isBigEndian() ? off + b : off + bytes - 1 - b;
      v = (v << 8) | (d[idx] & 0xff);
    }
    int bits = bytes * 8;
    if (Encoding.PCM_UNSIGNED.equals(enc)) {
      v -= 1L << (bits - 1);
    } else if ((v & (1L << (bits - 1))) != 0) {
      v -= 1L << bits;
    }
    return (double) v / (1L << (bits - 1));
  }

  private static void encode(byte[] out, int i, double s, AudioFormat f) {
    Encoding enc = f.getEncoding();
    if (Encoding.ULAW.equals(enc) || Encoding.ALAW.equals(enc)) {
      int pcm = (int) Math.max(-32768, Math.min(32767, Math.round(s * 32768)));
      out[i] = Encoding.ULAW.equals(enc) ? toUlaw(pcm) : toAlaw(pcm);
      return;
    }
    int bytes = f.getSampleSizeInBits() / 8;
    int bits = bytes * 8;
    long max = (1L << (bits - 1)) - 1;
    long v = Math.round(s * (1L << (bits - 1)));
    v = Math.max(-(max + 1), Math.min(max, v));
    if (Encoding.PCM_UNSIGNED.equals(enc)) {
      v += 1L << (bits - 1);
    }
    int off = i * bytes;
    for (int b = 0; b < bytes; b++) {
      int idx = f.isBigEndian() ? off + bytes - 1 - b : off + b;
      out[idx] = (byte) (v >> (8 * b));
    }
  }

  private static int ulaw(byte b) {
    int u = ~b & 0xff;
    int sign = u & 0x80;
    int exp = (u >> 4) & 7;
    int mant = u & 0x0f;
    int sample = ((mant << 3) + 0x84) << exp;
    sample -= 0x84;
    return sign != 0 ? -sample : sample;
  }

  private static byte toUlaw(int pcm) {
    int sign = (pcm >> 8) & 0x80;
    if (sign != 0) {
      pcm = -pcm;
    }
    if (pcm > 32635) {
      pcm = 32635;
    }
    pcm += 0x84;
    int exp = 7;
    for (int mask = 0x4000; (pcm & mask) == 0 && exp > 0; mask >>= 1) {
      exp--;
    }
    int mant = (pcm >> (exp + 3)) & 0x0f;
    return (byte) ~(sign | (exp << 4) | mant);
  }

  private static int alaw(byte b) {
    int a = (b ^ 0x55) & 0xff;
    int sign = a & 0x80;
    int exp = (a >> 4) & 7;
    int mant = a & 0x0f;
    int sample = exp == 0 ? (mant << 4) + 8 : ((mant << 4) + 0x108) << (exp - 1);
    return sign != 0 ? sample : -sample;
  }

  private static byte toAlaw(int pcm) {
    int sign = (pcm >> 8) & 0x80;
    if (sign == 0) {
      pcm = -pcm - 1;
    }
    if (pcm > 32767) {
      pcm = 32767;
    }
    if (pcm < 0) {
      pcm = -pcm;
    }
    int exp;
    int mant;
    if (pcm < 256) {
      exp = 0;
      mant = pcm >> 4;
    } else {
      exp = 1;
      for (int t = pcm >> 8; t > 1; t >>= 1) {
        exp++;
      }
      mant = (pcm >> (exp + 3)) & 0x0f;
    }
    int a = (sign == 0 ? 0 : 0x80) | (exp << 4) | mant;
    return (byte) (a ^ 0x55);
  }

  public static AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
    if (!stream.markSupported()) {
      throw new IOException("mark/reset not supported");
    }
    stream.mark(1 << 20);
    try {
      return readFormat(stream);
    } finally {
      stream.reset();
    }
  }

  public static AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
    try (InputStream in = new java.io.BufferedInputStream(url.openStream())) {
      return readFormat(in);
    }
  }

  public static AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
    try (InputStream in = new java.io.BufferedInputStream(new FileInputStream(file))) {
      return readFormat(in);
    }
  }

  public static AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
    if (!stream.markSupported()) {
      throw new IOException("mark/reset not supported");
    }
    stream.mark(1 << 20);
    try {
      return open(stream);
    } catch (UnsupportedAudioFileException e) {
      stream.reset();
      throw e;
    }
  }

  public static AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
    InputStream in = new java.io.BufferedInputStream(url.openStream());
    try {
      return open(in);
    } catch (UnsupportedAudioFileException e) {
      in.close();
      throw e;
    }
  }

  public static AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
    InputStream in = new java.io.BufferedInputStream(new FileInputStream(file));
    try {
      return open(in);
    } catch (UnsupportedAudioFileException e) {
      in.close();
      throw e;
    }
  }

  private static AudioFileFormat readFormat(InputStream in) throws UnsupportedAudioFileException, IOException {
    AudioInputStream ais = open(in);
    return new AudioFileFormat(typeOf(in, ais), ais.getFormat(), (int) ais.getFrameLength());
  }

  private static AudioFileFormat.Type lastType;

  private static AudioFileFormat.Type typeOf(InputStream in, AudioInputStream ais) {
    return lastType;
  }

  private static int le32(byte[] b, int o) {
    return (b[o] & 0xff) | ((b[o + 1] & 0xff) << 8) | ((b[o + 2] & 0xff) << 16) | ((b[o + 3] & 0xff) << 24);
  }

  private static int le16(byte[] b, int o) {
    return (b[o] & 0xff) | ((b[o + 1] & 0xff) << 8);
  }

  private static int be32(byte[] b, int o) {
    return ((b[o] & 0xff) << 24) | ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff);
  }

  private static AudioInputStream open(InputStream in) throws UnsupportedAudioFileException, IOException {
    DataInputStream din = new DataInputStream(in);
    byte[] head = new byte[12];
    int n = din.readNBytes(head, 0, 12);
    if (n >= 12 && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F' && head[8] == 'W'
        && head[9] == 'A' && head[10] == 'V' && head[11] == 'E') {
      lastType = AudioFileFormat.Type.WAVE;
      AudioFormat fmt = null;
      while (true) {
        byte[] ch = new byte[8];
        if (din.readNBytes(ch, 0, 8) < 8) {
          throw new UnsupportedAudioFileException("Not a valid WAV file");
        }
        int size = le32(ch, 4);
        if (ch[0] == 'f' && ch[1] == 'm' && ch[2] == 't' && ch[3] == ' ') {
          byte[] f = new byte[size];
          din.readFully(f);
          if ((size & 1) != 0) {
            din.skipBytes(1);
          }
          int tag = le16(f, 0);
          int channels = le16(f, 2);
          int rate = le32(f, 4);
          int bits = le16(f, 14);
          if (tag == 0xfffe && size >= 26) {
            tag = le16(f, 24);
          }
          Encoding enc;
          switch (tag) {
          case 1:
            enc = bits <= 8 ? Encoding.PCM_UNSIGNED : Encoding.PCM_SIGNED;
            break;
          case 3:
            enc = Encoding.PCM_FLOAT;
            break;
          case 6:
            enc = Encoding.ALAW;
            break;
          case 7:
            enc = Encoding.ULAW;
            break;
          default:
            throw new UnsupportedAudioFileException("Unsupported WAV format tag " + tag);
          }
          fmt = new AudioFormat(enc, rate, bits, channels, ((bits + 7) / 8) * channels, rate, false);
        } else if (ch[0] == 'd' && ch[1] == 'a' && ch[2] == 't' && ch[3] == 'a') {
          if (fmt == null) {
            throw new UnsupportedAudioFileException("data chunk before fmt chunk");
          }
          long frames = size / Math.max(1, fmt.getFrameSize());
          return new AudioInputStream(in, fmt, frames);
        } else {
          din.skipBytes(size + (size & 1));
        }
      }
    }
    if (n >= 4 && head[0] == '.' && head[1] == 's' && head[2] == 'n' && head[3] == 'd') {
      lastType = AudioFileFormat.Type.AU;
      byte[] rest = new byte[12];
      din.readFully(rest);
      int off = be32(head, 4);
      int len = be32(head, 8);
      int enc = be32(rest, 0);
      int rate = be32(rest, 4);
      int ch = be32(rest, 8);
      din.skipBytes(Math.max(0, off - 24));
      AudioFormat f;
      switch (enc) {
      case 1:
        f = new AudioFormat(Encoding.ULAW, rate, 8, ch, ch, rate, true);
        break;
      case 2:
        f = new AudioFormat(Encoding.PCM_SIGNED, rate, 8, ch, ch, rate, true);
        break;
      case 3:
        f = new AudioFormat(Encoding.PCM_SIGNED, rate, 16, ch, 2 * ch, rate, true);
        break;
      case 4:
        f = new AudioFormat(Encoding.PCM_SIGNED, rate, 24, ch, 3 * ch, rate, true);
        break;
      case 5:
        f = new AudioFormat(Encoding.PCM_SIGNED, rate, 32, ch, 4 * ch, rate, true);
        break;
      case 27:
        f = new AudioFormat(Encoding.ALAW, rate, 8, ch, ch, rate, true);
        break;
      default:
        throw new UnsupportedAudioFileException("Unsupported AU encoding " + enc);
      }
      long frames = len == -1 ? NOT_SPECIFIED : len / f.getFrameSize();
      return new AudioInputStream(in, f, frames);
    }
    throw new UnsupportedAudioFileException("Stream of unsupported format");
  }

  public static AudioFileFormat.Type[] getAudioFileTypes() {
    return new AudioFileFormat.Type[] {AudioFileFormat.Type.WAVE, AudioFileFormat.Type.AU};
  }

  public static boolean isFileTypeSupported(AudioFileFormat.Type fileType) {
    return AudioFileFormat.Type.WAVE.equals(fileType) || AudioFileFormat.Type.AU.equals(fileType);
  }

  public static AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream stream) {
    Encoding e = stream.getFormat().getEncoding();
    if (Encoding.PCM_SIGNED.equals(e) || Encoding.PCM_UNSIGNED.equals(e) || Encoding.ULAW.equals(e)
        || Encoding.ALAW.equals(e)) {
      return getAudioFileTypes();
    }
    return new AudioFileFormat.Type[0];
  }

  public static boolean isFileTypeSupported(AudioFileFormat.Type fileType, AudioInputStream stream) {
    for (AudioFileFormat.Type t : getAudioFileTypes(stream)) {
      if (t.equals(fileType)) {
        return true;
      }
    }
    return false;
  }

  public static int write(AudioInputStream stream, AudioFileFormat.Type fileType, OutputStream out) throws IOException {
    if (!isFileTypeSupported(fileType, stream)) {
      throw new IllegalArgumentException("could not write audio file: file type not supported: " + fileType);
    }
    AudioFormat f = stream.getFormat();
    byte[] data = stream.readAllBytes();
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    if (AudioFileFormat.Type.WAVE.equals(fileType)) {
      int tag;
      int bits = f.getSampleSizeInBits();
      if (Encoding.ULAW.equals(f.getEncoding())) {
        tag = 7;
      } else if (Encoding.ALAW.equals(f.getEncoding())) {
        tag = 6;
      } else {
        tag = 1;
        if (Encoding.PCM_SIGNED.equals(f.getEncoding()) && bits == 8) {
          for (int i = 0; i < data.length; i++) {
            data[i] += 128;
          }
        }
        if (bits > 8 && f.isBigEndian()) {
          int bytes = bits / 8;
          for (int i = 0; i + bytes <= data.length; i += bytes) {
            for (int a = 0, b = bytes - 1; a < b; a++, b--) {
              byte t = data[i + a];
              data[i + a] = data[i + b];
              data[i + b] = t;
            }
          }
        }
      }
      int channels = f.getChannels();
      int rate = (int) f.getSampleRate();
      int blockAlign = f.getFrameSize();
      writeLe(bo, "RIFF".getBytes());
      writeLe32(bo, 36 + data.length);
      writeLe(bo, "WAVEfmt ".getBytes());
      writeLe32(bo, 16);
      writeLe16(bo, tag);
      writeLe16(bo, channels);
      writeLe32(bo, rate);
      writeLe32(bo, rate * blockAlign);
      writeLe16(bo, blockAlign);
      writeLe16(bo, bits);
      writeLe(bo, "data".getBytes());
      writeLe32(bo, data.length);
      bo.write(data);
    } else {
      int enc;
      int bits = f.getSampleSizeInBits();
      if (Encoding.ULAW.equals(f.getEncoding())) {
        enc = 1;
      } else if (Encoding.ALAW.equals(f.getEncoding())) {
        enc = 27;
      } else {
        enc = bits == 8 ? 2 : bits == 16 ? 3 : bits == 24 ? 4 : 5;
        if (bits > 8 && !f.isBigEndian()) {
          int bytes = bits / 8;
          for (int i = 0; i + bytes <= data.length; i += bytes) {
            for (int a = 0, b = bytes - 1; a < b; a++, b--) {
              byte t = data[i + a];
              data[i + a] = data[i + b];
              data[i + b] = t;
            }
          }
        }
        if (Encoding.PCM_UNSIGNED.equals(f.getEncoding()) && bits == 8) {
          for (int i = 0; i < data.length; i++) {
            data[i] -= 128;
          }
        }
      }
      bo.write(new byte[] {'.', 's', 'n', 'd'});
      writeBe32(bo, 24);
      writeBe32(bo, data.length);
      writeBe32(bo, enc);
      writeBe32(bo, (int) f.getSampleRate());
      writeBe32(bo, f.getChannels());
      bo.write(data);
    }
    byte[] all = bo.toByteArray();
    out.write(all);
    out.flush();
    return all.length;
  }

  public static int write(AudioInputStream stream, AudioFileFormat.Type fileType, File out) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(out)) {
      return write(stream, fileType, fos);
    }
  }

  private static void writeLe(ByteArrayOutputStream o, byte[] b) throws IOException {
    o.write(b);
  }

  private static void writeLe32(ByteArrayOutputStream o, int v) {
    o.write(v);
    o.write(v >> 8);
    o.write(v >> 16);
    o.write(v >> 24);
  }

  private static void writeLe16(ByteArrayOutputStream o, int v) {
    o.write(v);
    o.write(v >> 8);
  }

  private static void writeBe32(ByteArrayOutputStream o, int v) {
    o.write(v >> 24);
    o.write(v >> 16);
    o.write(v >> 8);
    o.write(v);
  }
}
