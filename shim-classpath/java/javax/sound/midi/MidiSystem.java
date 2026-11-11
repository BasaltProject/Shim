/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class MidiSystem {
  private MidiSystem() { }

  public static MidiDevice.Info[] getMidiDeviceInfo() {
    return new MidiDevice.Info[] {shim.sound.SoftSynthesizer.info, shim.sound.SoftSequencer.info};
  }

  public static MidiDevice getMidiDevice(MidiDevice.Info info) throws MidiUnavailableException {
    if (info == shim.sound.SoftSynthesizer.info) {
      return new shim.sound.SoftSynthesizer();
    }
    if (info == shim.sound.SoftSequencer.info) {
      return new shim.sound.SoftSequencer();
    }
    throw new IllegalArgumentException("Requested device not installed: " + info);
  }

  public static Receiver getReceiver() throws MidiUnavailableException {
    Synthesizer synth = getSynthesizer();
    synth.open();
    return synth.getReceiver();
  }

  public static Transmitter getTransmitter() throws MidiUnavailableException {
    throw new MidiUnavailableException("MIDI IN receiver not available");
  }

  public static Synthesizer getSynthesizer() throws MidiUnavailableException {
    return new shim.sound.SoftSynthesizer();
  }

  public static Sequencer getSequencer() throws MidiUnavailableException { return getSequencer(true); }

  public static Sequencer getSequencer(boolean connected) throws MidiUnavailableException {
    Sequencer seq = new shim.sound.SoftSequencer();
    if (connected) {
      Receiver rec = getReceiver();
      Transmitter trans = seq.getTransmitter();
      trans.setReceiver(rec);
    }
    return seq;
  }

  public static Soundbank getSoundbank(InputStream stream) throws InvalidMidiDataException, IOException {
    throw new InvalidMidiDataException("cannot get soundbank from stream");
  }

  public static Soundbank getSoundbank(URL url) throws InvalidMidiDataException, IOException {
    throw new InvalidMidiDataException("cannot get soundbank from stream");
  }

  public static Soundbank getSoundbank(File file) throws InvalidMidiDataException, IOException {
    throw new InvalidMidiDataException("cannot get soundbank from stream");
  }

  public static MidiFileFormat getMidiFileFormat(InputStream stream) throws InvalidMidiDataException, IOException {
    if (!stream.markSupported()) {
      throw new IOException("mark/reset not supported");
    }
    stream.mark(1 << 20);
    try {
      return formatOf(parse(stream));
    } finally {
      stream.reset();
    }
  }

  public static MidiFileFormat getMidiFileFormat(URL url) throws InvalidMidiDataException, IOException {
    try (InputStream in = url.openStream()) {
      return formatOf(parse(in));
    }
  }

  public static MidiFileFormat getMidiFileFormat(File file) throws InvalidMidiDataException, IOException {
    try (InputStream in = new FileInputStream(file)) {
      return formatOf(parse(in));
    }
  }

  private static MidiFileFormat formatOf(Object[] parsed) {
    Sequence seq = (Sequence) parsed[0];
    return new MidiFileFormat(((Integer) parsed[1]).intValue(), seq.getDivisionType(), seq.getResolution(),
        MidiFileFormat.UNKNOWN_LENGTH, MidiFileFormat.UNKNOWN_LENGTH);
  }

  public static Sequence getSequence(InputStream stream) throws InvalidMidiDataException, IOException {
    return (Sequence) parse(stream)[0];
  }

  public static Sequence getSequence(URL url) throws InvalidMidiDataException, IOException {
    try (InputStream in = url.openStream()) {
      return getSequence(in);
    }
  }

  public static Sequence getSequence(File file) throws InvalidMidiDataException, IOException {
    try (InputStream in = new java.io.BufferedInputStream(new FileInputStream(file))) {
      return getSequence(in);
    }
  }

  public static int[] getMidiFileTypes() { return new int[] {0, 1}; }

  public static boolean isFileTypeSupported(int fileType) { return fileType == 0 || fileType == 1; }

  public static int[] getMidiFileTypes(Sequence sequence) {
    Track[] tracks = sequence.getTracks();
    if (tracks.length == 1) {
      return new int[] {0, 1};
    }
    return new int[] {1};
  }

  public static boolean isFileTypeSupported(int fileType, Sequence sequence) {
    for (int t : getMidiFileTypes(sequence)) {
      if (t == fileType) {
        return true;
      }
    }
    return false;
  }

  private static Object[] parse(InputStream in) throws InvalidMidiDataException, IOException {
    DataInputStream din = new DataInputStream(in);
    byte[] tag = new byte[4];
    din.readFully(tag);
    if (tag[0] != 'M' || tag[1] != 'T' || tag[2] != 'h' || tag[3] != 'd') {
      throw new InvalidMidiDataException("could not get sequence from input stream");
    }
    int len = din.readInt();
    int type = din.readUnsignedShort();
    int ntracks = din.readUnsignedShort();
    int div = din.readUnsignedShort();
    din.skipBytes(Math.max(0, len - 6));
    float divisionType;
    int resolution;
    if ((div & 0x8000) == 0) {
      divisionType = Sequence.PPQ;
      resolution = div;
    } else {
      int fps = -(byte) (div >> 8);
      divisionType = fps == 29 ? Sequence.SMPTE_30DROP : fps;
      resolution = div & 0xff;
    }
    Sequence seq = new Sequence(divisionType, resolution);
    for (int i = 0; i < ntracks; i++) {
      byte[] mt = new byte[4];
      din.readFully(mt);
      int tl = din.readInt();
      byte[] body = new byte[tl];
      din.readFully(body);
      Track track = seq.createTrack();
      int pos = 0;
      long tick = 0;
      int running = 0;
      while (pos < body.length) {
        long delta = 0;
        int b;
        do {
          b = body[pos++] & 0xff;
          delta = (delta << 7) | (b & 0x7f);
        } while ((b & 0x80) != 0);
        tick += delta;
        int status = body[pos] & 0xff;
        if (status < 0x80) {
          status = running;
        } else {
          pos++;
          if (status < 0xf0) {
            running = status;
          }
        }
        if (status == 0xff) {
          int mtype = body[pos++] & 0xff;
          long ml = 0;
          do {
            b = body[pos++] & 0xff;
            ml = (ml << 7) | (b & 0x7f);
          } while ((b & 0x80) != 0);
          byte[] md = new byte[(int) ml];
          System.arraycopy(body, pos, md, 0, (int) ml);
          pos += (int) ml;
          track.add(new MidiEvent(new MetaMessage(mtype, md, md.length), tick));
        } else if (status == 0xf0 || status == 0xf7) {
          long sl = 0;
          do {
            b = body[pos++] & 0xff;
            sl = (sl << 7) | (b & 0x7f);
          } while ((b & 0x80) != 0);
          byte[] sd = new byte[(int) sl];
          System.arraycopy(body, pos, sd, 0, (int) sl);
          pos += (int) sl;
          track.add(new MidiEvent(new SysexMessage(status, sd, sd.length), tick));
        } else {
          int dl = (status & 0xf0) == 0xc0 || (status & 0xf0) == 0xd0 ? 1 : 2;
          int d1 = body[pos++] & 0xff;
          int d2 = dl == 2 ? body[pos++] & 0xff : 0;
          track.add(new MidiEvent(new ShortMessage(status, d1, d2), tick));
        }
      }
    }
    return new Object[] {seq, Integer.valueOf(type)};
  }

  public static int write(Sequence in, int type, File out) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(out)) {
      return write(in, type, fos);
    }
  }

  public static int write(Sequence in, int type, OutputStream out) throws IOException {
    if (!isFileTypeSupported(type, in)) {
      throw new IllegalArgumentException("Could not write MIDI file");
    }
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    Track[] tracks = in.getTracks();
    bo.write(new byte[] {'M', 'T', 'h', 'd'});
    writeInt(bo, 6);
    bo.write(0);
    bo.write(type);
    bo.write(tracks.length >> 8);
    bo.write(tracks.length);
    int div;
    if (in.getDivisionType() == Sequence.PPQ) {
      div = in.getResolution();
    } else {
      int fps = in.getDivisionType() == Sequence.SMPTE_30DROP ? 29 : (int) in.getDivisionType();
      div = ((-fps & 0xff) << 8) | in.getResolution();
    }
    bo.write(div >> 8);
    bo.write(div);
    for (Track t : tracks) {
      ByteArrayOutputStream tb = new ByteArrayOutputStream();
      long last = 0;
      int running = -1;
      for (int i = 0; i < t.size(); i++) {
        MidiEvent e = t.get(i);
        writeVar(tb, e.getTick() - last);
        last = e.getTick();
        MidiMessage m = e.getMessage();
        byte[] mb = m.getMessage();
        if (m instanceof MetaMessage) {
          tb.write(0xff);
          tb.write(((MetaMessage) m).getType());
          byte[] md = ((MetaMessage) m).getData();
          writeVar(tb, md.length);
          tb.write(md);
          running = -1;
        } else if (m instanceof SysexMessage) {
          tb.write(mb[0] & 0xff);
          writeVar(tb, mb.length - 1);
          tb.write(mb, 1, mb.length - 1);
          running = -1;
        } else {
          int st = mb[0] & 0xff;
          if (st != running) {
            tb.write(st);
          }
          tb.write(mb, 1, mb.length - 1);
          running = st < 0xf0 ? st : -1;
        }
      }
      bo.write(new byte[] {'M', 'T', 'r', 'k'});
      writeInt(bo, tb.size());
      bo.write(tb.toByteArray());
    }
    byte[] all = bo.toByteArray();
    out.write(all);
    out.flush();
    return all.length;
  }

  private static void writeInt(ByteArrayOutputStream o, int v) {
    o.write(v >> 24);
    o.write(v >> 16);
    o.write(v >> 8);
    o.write(v);
  }

  private static void writeVar(ByteArrayOutputStream o, long v) {
    int shift = 63;
    while (shift > 0 && ((v & (0x7fL << shift)) == 0)) {
      shift -= 7;
    }
    while (shift > 0) {
      o.write((int) (((v & (0x7fL << shift)) >> shift) | 0x80));
      shift -= 7;
    }
    o.write((int) (v & 0x7f));
  }
}
