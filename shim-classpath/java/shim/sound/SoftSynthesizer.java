/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.sound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.midi.Instrument;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.midi.VoiceStatus;

public final class SoftSynthesizer implements Synthesizer {
  static final class SynthInfo extends MidiDevice.Info {
    SynthInfo() {
      super("Gervill", "OpenJDK", "Software MIDI Synthesizer", "1.0");
    }
  }

  public static final MidiDevice.Info info = new SynthInfo();

  private static final int kMaxPolyphony = 64;
  private static final long kLatencyMicros = 120000;

  private final SoftChannel[] channels = new SoftChannel[16];
  private final VoiceStatus[] voices = new VoiceStatus[kMaxPolyphony];
  private final List<Receiver> receivers = new ArrayList<>();
  private boolean open;
  private long openNanos;

  public SoftSynthesizer() {
    for (int i = 0; i < channels.length; i++) {
      channels[i] = new SoftChannel(this, i);
    }
    for (int i = 0; i < voices.length; i++) {
      voices[i] = new VoiceStatus();
    }
  }

  synchronized void voiceOn(int channel, int bank, int program, int note, int velocity) {
    VoiceStatus slot = null;
    for (VoiceStatus v : voices) {
      if (!v.active) {
        slot = v;
        break;
      }
    }
    if (slot == null) {
      slot = voices[0];
    }
    slot.active = true;
    slot.channel = channel;
    slot.bank = bank;
    slot.program = program;
    slot.note = note;
    slot.volume = velocity;
  }

  synchronized void voiceOff(int channel, int note) {
    for (VoiceStatus v : voices) {
      if (v.active && v.channel == channel && v.note == note) {
        v.active = false;
        v.volume = 0;
        return;
      }
    }
  }

  synchronized void voicesOff(int channel) {
    for (VoiceStatus v : voices) {
      if (v.active && v.channel == channel) {
        v.active = false;
        v.volume = 0;
      }
    }
  }

  public synchronized MidiDevice.Info getDeviceInfo() { return info; }

  public synchronized void open() throws MidiUnavailableException {
    if (!open) {
      open = true;
      openNanos = System.nanoTime();
    }
  }

  public synchronized void close() {
    if (open) {
      open = false;
      for (SoftChannel c : channels) {
        c.allSoundOff();
      }
      for (Receiver r : new ArrayList<>(receivers)) {
        r.close();
      }
      receivers.clear();
    }
  }

  public synchronized boolean isOpen() { return open; }

  public long getMicrosecondPosition() {
    synchronized (this) {
      if (!open) {
        return 0;
      }
      return (System.nanoTime() - openNanos) / 1000;
    }
  }

  public int getMaxReceivers() { return -1; }

  public int getMaxTransmitters() { return 0; }

  public int getMaxPolyphony() { return kMaxPolyphony; }

  public long getLatency() { return kLatencyMicros; }

  public MidiChannel[] getChannels() { return channels.clone(); }

  public synchronized VoiceStatus[] getVoiceStatus() {
    VoiceStatus[] out = new VoiceStatus[voices.length];
    for (int i = 0; i < out.length; i++) {
      VoiceStatus v = new VoiceStatus();
      v.active = voices[i].active;
      v.channel = voices[i].channel;
      v.bank = voices[i].bank;
      v.program = voices[i].program;
      v.note = voices[i].note;
      v.volume = voices[i].volume;
      out[i] = v;
    }
    return out;
  }

  public boolean isSoundbankSupported(Soundbank soundbank) { return false; }

  public boolean loadInstrument(Instrument instrument) {
    if (instrument == null) {
      throw new IllegalArgumentException("Unsupported instrument: " + instrument);
    }
    return false;
  }

  public void unloadInstrument(Instrument instrument) {
    if (instrument == null) {
      throw new IllegalArgumentException("Unsupported instrument: " + instrument);
    }
  }

  public boolean remapInstrument(Instrument from, Instrument to) {
    if (from == null || to == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public Soundbank getDefaultSoundbank() { return null; }

  public Instrument[] getAvailableInstruments() { return new Instrument[0]; }

  public Instrument[] getLoadedInstruments() { return new Instrument[0]; }

  public boolean loadAllInstruments(Soundbank soundbank) { return false; }

  public void unloadAllInstruments(Soundbank soundbank) { }

  public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) { return false; }

  public void unloadInstruments(Soundbank soundbank, Patch[] patchList) { }

  public Receiver getReceiver() throws MidiUnavailableException {
    SynthReceiver r = new SynthReceiver();
    synchronized (this) {
      receivers.add(r);
    }
    return r;
  }

  public synchronized List<Receiver> getReceivers() { return Collections.unmodifiableList(new ArrayList<>(receivers)); }

  public Transmitter getTransmitter() throws MidiUnavailableException {
    throw new MidiUnavailableException("MIDI OUT transmitter not supported");
  }

  public List<Transmitter> getTransmitters() { return Collections.emptyList(); }

  private final class SynthReceiver implements MidiDeviceReceiver {
    private boolean closed;

    public MidiDevice getMidiDevice() { return SoftSynthesizer.this; }

    public void close() {
      closed = true;
      synchronized (SoftSynthesizer.this) {
        receivers.remove(this);
      }
    }

    public void send(MidiMessage message, long timeStamp) {
      if (closed) {
        throw new IllegalStateException("Receiver is not open");
      }
      if (message instanceof ShortMessage) {
        ShortMessage sm = (ShortMessage) message;
        SoftChannel ch = channels[sm.getChannel()];
        switch (sm.getCommand()) {
          case ShortMessage.NOTE_ON:
            ch.noteOn(sm.getData1(), sm.getData2());
            break;
          case ShortMessage.NOTE_OFF:
            ch.noteOff(sm.getData1(), sm.getData2());
            break;
          case ShortMessage.POLY_PRESSURE:
            ch.setPolyPressure(sm.getData1(), sm.getData2());
            break;
          case ShortMessage.CONTROL_CHANGE:
            ch.controlChange(sm.getData1(), sm.getData2());
            break;
          case ShortMessage.PROGRAM_CHANGE:
            ch.programChange(sm.getData1());
            break;
          case ShortMessage.CHANNEL_PRESSURE:
            ch.setChannelPressure(sm.getData1());
            break;
          case ShortMessage.PITCH_BEND:
            ch.setPitchBend(sm.getData1() + sm.getData2() * 128);
            break;
          default:
        }
      } else if (message instanceof SysexMessage) {
        byte[] d = message.getMessage();
        if (d.length >= 6 && (d[0] & 0xff) == 0xF0 && d[1] == 0x7E && d[3] == 0x09) {
          for (SoftChannel c : channels) {
            c.allSoundOff();
            c.resetAllControllers();
            c.programChange(0);
          }
        }
      } else if (message instanceof MetaMessage) {
        return;
      }
    }
  }
}
