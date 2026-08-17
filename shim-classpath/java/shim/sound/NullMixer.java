/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.sound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public final class NullMixer implements Mixer {
  static final class MixInfo extends Mixer.Info {
    MixInfo() {
      super("Shim Null Mixer", "Basalt", "Silent software mixer without hardware", "1.0");
    }
  }

  public static final Mixer.Info info = new MixInfo();
  private static final int kBuffer = 32768;

  private static AudioFormat[] formats() {
    List<AudioFormat> out = new ArrayList<>();
    for (int bits : new int[] {8, 16, 24, 32}) {
      for (boolean big : new boolean[] {false, true}) {
        out.add(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, -1f, bits, -1, -1, -1f, big));
        out.add(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, -1f, bits, -1, -1, -1f, big));
      }
    }
    return out.toArray(new AudioFormat[0]);
  }

  private final DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, formats(), 32, -1);
  private final DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, formats(), 32, -1);
  private final DataLine.Info clipInfo = new DataLine.Info(Clip.class, formats(), 32, -1);
  private final List<Line> sources = new ArrayList<>();
  private final List<Line> targets = new ArrayList<>();
  private final List<LineListener> listeners = new ArrayList<>();
  private boolean open;

  public Mixer.Info getMixerInfo() { return info; }

  public Line.Info[] getSourceLineInfo() { return new Line.Info[] {sourceInfo, clipInfo}; }

  public Line.Info[] getTargetLineInfo() { return new Line.Info[] {targetInfo}; }

  public Line.Info[] getSourceLineInfo(Line.Info li) { return filter(getSourceLineInfo(), li); }

  public Line.Info[] getTargetLineInfo(Line.Info li) { return filter(getTargetLineInfo(), li); }

  private static Line.Info[] filter(Line.Info[] all, Line.Info li) {
    List<Line.Info> out = new ArrayList<>();
    for (Line.Info i : all) {
      if (li.matches(i)) {
        out.add(i);
      }
    }
    return out.toArray(new Line.Info[0]);
  }

  public boolean isLineSupported(Line.Info li) {
    return filter(getSourceLineInfo(), li).length > 0 || filter(getTargetLineInfo(), li).length > 0;
  }

  public Line getLine(Line.Info li) throws LineUnavailableException {
    if (li instanceof DataLine.Info) {
      Class<?> c = li.getLineClass();
      if (Clip.class.isAssignableFrom(c) && li.matches(clipInfo)) {
        return track(new NullClip(), sources);
      }
      if (SourceDataLine.class.isAssignableFrom(c) && li.matches(sourceInfo)) {
        return track(new NullSource(), sources);
      }
      if (TargetDataLine.class.isAssignableFrom(c) && li.matches(targetInfo)) {
        return track(new NullTarget(), targets);
      }
    }
    throw new IllegalArgumentException("Line unsupported: " + li);
  }

  private <T extends Line> T track(T l, List<Line> list) {
    synchronized (list) {
      list.add(l);
    }
    return l;
  }

  public int getMaxLines(Line.Info li) { return isLineSupported(li) ? AudioSystemConstants.kUnbounded : 0; }

  public Line[] getSourceLines() {
    synchronized (sources) {
      return sources.toArray(new Line[0]);
    }
  }

  public Line[] getTargetLines() {
    synchronized (targets) {
      return targets.toArray(new Line[0]);
    }
  }

  public void synchronize(Line[] lines, boolean maintainSync) {
    throw new IllegalArgumentException("Synchronization not supported by this mixer.");
  }

  public void unsynchronize(Line[] lines) {
    throw new IllegalArgumentException("Synchronization not supported by this mixer.");
  }

  public boolean isSynchronizationSupported(Line[] lines, boolean maintainSync) { return false; }

  public Line.Info getLineInfo() { return new Line.Info(Mixer.class); }

  public void open() throws LineUnavailableException { open = true; }

  public void close() { open = false; }

  public boolean isOpen() { return open; }

  public Control[] getControls() { return new Control[0]; }

  public boolean isControlSupported(Control.Type type) { return false; }

  public Control getControl(Control.Type type) { throw new IllegalArgumentException("Unsupported control type: " + type); }

  public void addLineListener(LineListener l) {
    synchronized (listeners) {
      listeners.add(l);
    }
  }

  public void removeLineListener(LineListener l) {
    synchronized (listeners) {
      listeners.remove(l);
    }
  }

  private static final class AudioSystemConstants {
    static final int kUnbounded = -1;
  }

  private abstract class BaseLine implements DataLine {
    final List<LineListener> ls = new ArrayList<>();
    AudioFormat format;
    boolean isOpen;
    boolean running;
    int bufferSize;
    long startNanos;
    long accumulatedNanos;
    long writtenFrames;

    abstract DataLine.Info lineInfo();

    public Line.Info getLineInfo() { return lineInfo(); }

    void fire(LineEvent.Type t) {
      List<LineListener> copy;
      synchronized (ls) {
        copy = new ArrayList<>(ls);
      }
      LineEvent e = new LineEvent(this, t, getLongFramePosition());
      for (LineListener l : copy) {
        l.update(e);
      }
    }

    public void addLineListener(LineListener l) {
      synchronized (ls) {
        ls.add(l);
      }
    }

    public void removeLineListener(LineListener l) {
      synchronized (ls) {
        ls.remove(l);
      }
    }

    void doOpen(AudioFormat f, int size) throws LineUnavailableException {
      if (isOpen) {
        throw new IllegalStateException("Line is already open");
      }
      if (f.getSampleRate() <= 0 || f.getChannels() <= 0 || f.getSampleSizeInBits() <= 0) {
        throw new IllegalArgumentException("Invalid format: " + f);
      }
      if (!lineInfo().isFormatSupported(f)) {
        throw new IllegalArgumentException("Line unsupported format: " + f);
      }
      format = f;
      int frameSize = f.getFrameSize() > 0 ? f.getFrameSize() : (f.getSampleSizeInBits() + 7) / 8 * f.getChannels();
      bufferSize = (size > 0 ? size : kBuffer) / frameSize * frameSize;
      isOpen = true;
      fire(LineEvent.Type.OPEN);
    }

    public void open() throws LineUnavailableException {
      throw new IllegalArgumentException("Line cannot be opened without a format");
    }

    public synchronized void close() {
      if (isOpen) {
        stop();
        isOpen = false;
        fire(LineEvent.Type.CLOSE);
      }
    }

    public synchronized boolean isOpen() { return isOpen; }

    public Control[] getControls() { return new Control[0]; }

    public boolean isControlSupported(Control.Type type) { return false; }

    public Control getControl(Control.Type type) { throw new IllegalArgumentException("Unsupported control type: " + type); }

    public synchronized void start() {
      if (isOpen && !running) {
        running = true;
        startNanos = System.nanoTime();
        fire(LineEvent.Type.START);
      }
    }

    public synchronized void stop() {
      if (isOpen && running) {
        accumulatedNanos += System.nanoTime() - startNanos;
        running = false;
        fire(LineEvent.Type.STOP);
      }
    }

    public synchronized boolean isRunning() { return running; }

    public synchronized boolean isActive() { return running; }

    public AudioFormat getFormat() {
      return format != null ? format : new AudioFormat(44100f, 16, 2, true, false);
    }

    public int getBufferSize() { return isOpen ? bufferSize : kBuffer; }

    public float getLevel() { return -1f; }

    synchronized long playedNanos() {
      return accumulatedNanos + (running ? System.nanoTime() - startNanos : 0);
    }

    synchronized long playedFrames() {
      long f = (long) (playedNanos() / 1e9 * format.getSampleRate());
      return Math.min(f, writtenFrames);
    }

    public int getFramePosition() { return (int) getLongFramePosition(); }

    public long getLongFramePosition() { return format == null ? 0 : playedFrames(); }

    public long getMicrosecondPosition() {
      return format == null ? 0 : (long) (getLongFramePosition() * 1e6 / format.getSampleRate());
    }
  }

  private final class NullSource extends BaseLine implements SourceDataLine {
    DataLine.Info lineInfo() { return sourceInfo; }

    public void open(AudioFormat f, int size) throws LineUnavailableException { doOpen(f, size); }

    public void open(AudioFormat f) throws LineUnavailableException { doOpen(f, -1); }

    public synchronized int write(byte[] b, int off, int len) {
      if (!isOpen) {
        return 0;
      }
      int fs = format.getFrameSize() > 0 ? format.getFrameSize() : 1;
      if (len % fs != 0) {
        throw new IllegalArgumentException("illegal request to write non-integral number of frames (" + len
            + " bytes, frameSize = " + fs + " bytes)");
      }
      writtenFrames += len / fs;
      long ahead = writtenFrames - (long) (playedNanos() / 1e9 * format.getSampleRate());
      long limit = bufferSize / fs;
      if (running && ahead > limit) {
        long ns = (long) ((ahead - limit) / format.getSampleRate() * 1e9);
        try {
          wait(ns / 1000000L, (int) (ns % 1000000L));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
      return len;
    }

    public synchronized void drain() {
      if (!isOpen || !running) {
        return;
      }
      long remaining = writtenFrames - (long) (playedNanos() / 1e9 * format.getSampleRate());
      if (remaining > 0) {
        long ns = (long) (remaining / format.getSampleRate() * 1e9);
        try {
          wait(ns / 1000000L, (int) (ns % 1000000L));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }

    public synchronized void flush() { writtenFrames = Math.min(writtenFrames, playedFrames()); }

    public synchronized int available() {
      if (!isOpen) {
        return 0;
      }
      int fs = format.getFrameSize() > 0 ? format.getFrameSize() : 1;
      long ahead = Math.max(0, writtenFrames - (long) (playedNanos() / 1e9 * format.getSampleRate()));
      return (int) Math.max(0, bufferSize - ahead * fs);
    }
  }

  private final class NullTarget extends BaseLine implements TargetDataLine {
    long readFrames;

    DataLine.Info lineInfo() { return targetInfo; }

    public void open(AudioFormat f, int size) throws LineUnavailableException { doOpen(f, size); }

    public void open(AudioFormat f) throws LineUnavailableException { doOpen(f, -1); }

    public synchronized int read(byte[] b, int off, int len) {
      if (!isOpen) {
        return 0;
      }
      int fs = format.getFrameSize() > 0 ? format.getFrameSize() : 1;
      if (len % fs != 0) {
        throw new IllegalArgumentException("illegal request to read non-integral number of frames (" + len
            + " bytes, frameSize = " + fs + " bytes)");
      }
      if (!running) {
        return 0;
      }
      long ns = (long) (len / fs / format.getSampleRate() * 1e9);
      try {
        wait(ns / 1000000L, (int) (ns % 1000000L));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      byte fill = format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED ? (byte) 0x80 : 0;
      java.util.Arrays.fill(b, off, off + len, fill);
      readFrames += len / fs;
      writtenFrames = readFrames;
      return len;
    }

    public synchronized void drain() { }

    public synchronized void flush() { }

    public synchronized int available() { return 0; }
  }

  private final class NullClip extends BaseLine implements Clip {
    int frames;
    int framePos;
    int loopStart;
    int loopEnd = -1;
    int loops;
    long posBaseNanos;

    DataLine.Info lineInfo() { return clipInfo; }

    public void open(AudioFormat f, byte[] data, int off, int len) throws LineUnavailableException {
      doOpen(f, len);
      int fs = f.getFrameSize() > 0 ? f.getFrameSize() : 1;
      frames = len / fs;
      writtenFrames = frames;
    }

    public void open(AudioInputStream stream) throws LineUnavailableException, IOException {
      AudioFormat f = stream.getFormat();
      long len = stream.getFrameLength();
      byte[] data = stream.readAllBytes();
      open(f, data, 0, data.length);
    }

    public void open() throws LineUnavailableException {
      throw new IllegalArgumentException("Illegal call to open() in interface Clip");
    }

    public synchronized int getFrameLength() { return isOpen ? frames : AudioSystemConstants.kUnbounded; }

    public synchronized long getMicrosecondLength() {
      return isOpen ? (long) (frames * 1e6 / format.getSampleRate()) : -1;
    }

    public synchronized void setFramePosition(int pos) {
      if (pos < 0 || pos > frames) {
        framePos = 0;
      } else {
        framePos = pos;
      }
      accumulatedNanos = 0;
      startNanos = System.nanoTime();
    }

    public synchronized void setMicrosecondPosition(long micros) {
      setFramePosition((int) (micros * format.getSampleRate() / 1e6));
    }

    public synchronized void setLoopPoints(int start, int end) {
      if (end != -1 && (end < 0 || end > frames || start > end)) {
        throw new IllegalArgumentException("Loop points out of range");
      }
      if (start < 0 || start > frames) {
        throw new IllegalArgumentException("Loop points out of range");
      }
      loopStart = start;
      loopEnd = end;
    }

    public synchronized void loop(int count) {
      loops = count;
      start();
    }

    synchronized long playedFrames() {
      long played = (long) (playedNanos() / 1e9 * format.getSampleRate());
      long pos = framePos + played;
      int end = loopEnd == -1 ? frames : loopEnd;
      if (loops != 0 && end > loopStart) {
        long span = end - loopStart;
        long left = loops < 0 ? Long.MAX_VALUE : (long) loops * span;
        long beyond = pos - end;
        if (beyond > 0) {
          long wrapped = Math.min(beyond, left);
          pos = end + beyond - wrapped < 0 ? pos : pos - (wrapped / span) * span;
        }
        if (loops < 0 || pos < end + left) {
          return Math.min(pos, frames);
        }
      }
      return Math.min(pos, frames);
    }

    public synchronized boolean isRunning() {
      if (running && playedFrames() >= frames && loops >= 0 && playedNanos() / 1e9 * format.getSampleRate() + framePos
          >= frames + (loops == 0 ? 0 : loops * Math.max(1, (loopEnd == -1 ? frames : loopEnd) - loopStart))) {
        stop();
      }
      return running;
    }

    public synchronized boolean isActive() { return isRunning(); }

    public synchronized void drain() { }

    public synchronized void flush() { }

    public synchronized int available() { return 0; }

    public int getFramePosition() { return (int) getLongFramePosition(); }
  }
}
