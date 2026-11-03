/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.sound;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiDeviceTransmitter;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

public final class SoftSequencer implements Sequencer {
  static final class SeqInfo extends MidiDevice.Info {
    SeqInfo() {
      super("Real Time Sequencer", "Oracle Corporation", "Software sequencer", "Version 1.0");
    }
  }

  public static final MidiDevice.Info info = new SeqInfo();

  private static final class Ev {
    final long tick;
    final int track;
    final MidiMessage message;

    Ev(long tick, int track, MidiMessage message) {
      this.tick = tick;
      this.track = track;
      this.message = message;
    }
  }

  private final Object lock = new Object();
  private final List<SeqTransmitter> transmitters = new ArrayList<>();
  private final List<SeqReceiver> receivers = new ArrayList<>();
  private final List<MetaEventListener> metaListeners = new ArrayList<>();
  private final Map<ControllerEventListener, boolean[]> controllerListeners = new HashMap<>();
  private final Set<Integer> muted = new HashSet<>();
  private final Set<Integer> soloed = new HashSet<>();
  private final Map<Track, Integer> recordTracks = new HashMap<>();

  private Sequence sequence;
  private boolean open;
  private boolean running;
  private boolean recording;
  private long tickPos;
  private double mpq = 500000;
  private float tempoFactor = 1.0f;
  private long loopStart;
  private long loopEnd = -1;
  private int loopCount;
  private int loopsDone;
  private Thread player;
  private long segStartNanos;
  private long segStartTick;
  private long openNanos;
  private SyncMode master = SyncMode.INTERNAL_CLOCK;
  private SyncMode slave = SyncMode.NO_SYNC;

  public MidiDevice.Info getDeviceInfo() { return info; }

  public void open() throws MidiUnavailableException {
    synchronized (lock) {
      if (!open) {
        open = true;
        openNanos = System.nanoTime();
      }
    }
  }

  public void close() {
    List<SeqTransmitter> ts;
    List<SeqReceiver> rs;
    synchronized (lock) {
      if (!open) {
        return;
      }
      halt();
      open = false;
      ts = new ArrayList<>(transmitters);
      rs = new ArrayList<>(receivers);
    }
    for (SeqTransmitter t : ts) {
      t.close();
    }
    for (SeqReceiver r : rs) {
      r.close();
    }
  }

  public boolean isOpen() {
    synchronized (lock) {
      return open;
    }
  }

  public long getMicrosecondPosition() {
    synchronized (lock) {
      if (sequence == null) {
        return 0;
      }
      return tickToMicros(currentTick());
    }
  }

  public int getMaxReceivers() { return -1; }

  public int getMaxTransmitters() { return -1; }

  public Receiver getReceiver() throws MidiUnavailableException {
    SeqReceiver r = new SeqReceiver();
    synchronized (lock) {
      receivers.add(r);
    }
    return r;
  }

  public List<Receiver> getReceivers() {
    synchronized (lock) {
      return Collections.unmodifiableList(new ArrayList<Receiver>(receivers));
    }
  }

  public Transmitter getTransmitter() throws MidiUnavailableException {
    SeqTransmitter t = new SeqTransmitter();
    synchronized (lock) {
      transmitters.add(t);
    }
    return t;
  }

  public List<Transmitter> getTransmitters() {
    synchronized (lock) {
      return Collections.unmodifiableList(new ArrayList<Transmitter>(transmitters));
    }
  }

  private void requireOpen() {
    if (!open) {
      throw new IllegalStateException("sequencer not open");
    }
  }

  public void setSequence(Sequence seq) throws InvalidMidiDataException {
    synchronized (lock) {
      halt();
      sequence = seq;
      tickPos = 0;
      muted.clear();
      soloed.clear();
      recordTracks.clear();
      loopStart = 0;
      loopEnd = -1;
      loopCount = 0;
    }
  }

  public void setSequence(InputStream stream) throws IOException, InvalidMidiDataException {
    if (stream == null) {
      setSequence((Sequence) null);
      return;
    }
    setSequence(MidiSystem.getSequence(stream));
  }

  public Sequence getSequence() {
    synchronized (lock) {
      return sequence;
    }
  }

  public void start() {
    synchronized (lock) {
      requireOpen();
      if (running) {
        return;
      }
      if (sequence == null) {
        return;
      }
      running = true;
      loopsDone = 0;
      segStartNanos = System.nanoTime();
      segStartTick = tickPos;
      applyTempoAt(tickPos);
      final Thread t = new Thread(new Runnable() {
        public void run() {
          play();
        }
      }, "Shim Sequencer");
      t.setDaemon(true);
      player = t;
      t.start();
    }
  }

  public void stop() {
    boolean was;
    synchronized (lock) {
      requireOpen();
      was = running;
      halt();
    }
    if (was) {
      silence();
    }
  }

  private void fireEndOfTrack() {
    try {
      MetaMessage eot = new MetaMessage(0x2F, new byte[0], 0);
      for (MetaEventListener l : new ArrayList<>(metaListeners)) {
        l.meta(eot);
      }
    } catch (InvalidMidiDataException e) {
      return;
    }
  }

  private void restoreState() {
    int[] volume = new int[16];
    int[] program = new int[16];
    boolean[] used = new boolean[16];
    Arrays.fill(volume, 100);
    for (Track t : sequence.getTracks()) {
      for (int i = 0; i < t.size(); i++) {
        MidiMessage m = t.get(i).getMessage();
        if (m instanceof ShortMessage) {
          ShortMessage sm = (ShortMessage) m;
          int ch = sm.getChannel();
          used[ch] = true;
          if (sm.getCommand() == ShortMessage.CONTROL_CHANGE && sm.getData1() == 7) {
            volume[ch] = sm.getData2();
          } else if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE) {
            program[ch] = sm.getData1();
          }
        }
      }
    }
    for (int ch = 0; ch < 16; ch++) {
      if (!used[ch]) {
        continue;
      }
      try {
        send(new ShortMessage(ShortMessage.CONTROL_CHANGE, ch, 7, volume[ch]));
        send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, ch, program[ch], 0));
        send(new ShortMessage(ShortMessage.PITCH_BEND, ch, 0, 64));
        send(new ShortMessage(ShortMessage.CONTROL_CHANGE, ch, 64, 0));
      } catch (InvalidMidiDataException e) {
        return;
      }
    }
  }

  private void send(MidiMessage m) {
    for (SeqTransmitter t : new ArrayList<>(transmitters)) {
      Receiver r = t.getReceiver();
      if (r != null) {
        try {
          r.send(m, -1);
        } catch (IllegalStateException e) {
          continue;
        }
      }
    }
  }

  private void silence() {
    List<SeqTransmitter> ts;
    synchronized (lock) {
      ts = new ArrayList<>(transmitters);
    }
    int[] ccs = {123, 64, 121};
    for (int ch = 0; ch < 16; ch++) {
      for (int cc : ccs) {
        try {
          ShortMessage m = new ShortMessage(ShortMessage.CONTROL_CHANGE, ch, cc, 0);
          for (SeqTransmitter t : ts) {
            Receiver r = t.getReceiver();
            if (r != null) {
              r.send(m, -1);
            }
          }
        } catch (InvalidMidiDataException | IllegalStateException e) {
          return;
        }
      }
    }
  }

  private void halt() {
    if (running) {
      tickPos = currentTick();
      running = false;
      lock.notifyAll();
    }
    recording = false;
  }

  public boolean isRunning() {
    synchronized (lock) {
      return running;
    }
  }

  public void startRecording() {
    synchronized (lock) {
      requireOpen();
      recording = true;
    }
    start();
  }

  public void stopRecording() {
    synchronized (lock) {
      requireOpen();
      recording = false;
    }
  }

  public boolean isRecording() {
    synchronized (lock) {
      return recording;
    }
  }

  public void recordEnable(Track track, int channel) {
    synchronized (lock) {
      if (!containsTrack(track)) {
        throw new IllegalArgumentException("Track does not exist in the current sequence");
      }
      recordTracks.put(track, channel);
    }
  }

  public void recordDisable(Track track) {
    synchronized (lock) {
      recordTracks.remove(track);
    }
  }

  private boolean containsTrack(Track track) {
    if (sequence == null) {
      return false;
    }
    for (Track t : sequence.getTracks()) {
      if (t == track) {
        return true;
      }
    }
    return false;
  }

  public float getTempoInBPM() { return (float) (60000000.0 / getTempoInMPQ()); }

  public void setTempoInBPM(float bpm) {
    if (bpm <= 0) {
      bpm = 1.0f;
    }
    setTempoInMPQ((float) (60000000.0 / bpm));
  }

  public float getTempoInMPQ() {
    synchronized (lock) {
      return (float) mpq;
    }
  }

  public void setTempoInMPQ(float newMpq) {
    if (newMpq <= 0) {
      newMpq = 1.0f;
    }
    synchronized (lock) {
      rebase();
      mpq = newMpq;
      lock.notifyAll();
    }
  }

  public void setTempoFactor(float factor) {
    if (factor > 0 && factor != tempoFactor) {
      synchronized (lock) {
        rebase();
        tempoFactor = factor;
        lock.notifyAll();
      }
    }
  }

  public float getTempoFactor() {
    synchronized (lock) {
      return tempoFactor;
    }
  }

  public long getTickLength() {
    synchronized (lock) {
      return sequence == null ? 0 : sequence.getTickLength();
    }
  }

  public long getMicrosecondLength() {
    synchronized (lock) {
      return sequence == null ? 0 : sequence.getMicrosecondLength();
    }
  }

  public long getTickPosition() {
    synchronized (lock) {
      return sequence == null ? 0 : currentTick();
    }
  }

  public void setTickPosition(long tick) {
    if (tick < 0) {
      return;
    }
    synchronized (lock) {
      if (sequence == null) {
        return;
      }
      tickPos = tick;
      if (running) {
        segStartNanos = System.nanoTime();
        segStartTick = tick;
        lock.notifyAll();
      }
      applyTempoAt(tick);
    }
  }

  public void setMicrosecondPosition(long micros) {
    if (micros < 0) {
      return;
    }
    synchronized (lock) {
      if (sequence == null) {
        return;
      }
      setTickPosition(microsToTick(micros));
    }
  }

  public void setMasterSyncMode(SyncMode sync) {
    if (sync != null && sync.equals(SyncMode.INTERNAL_CLOCK)) {
      master = sync;
    }
  }

  public SyncMode getMasterSyncMode() { return master; }

  public SyncMode[] getMasterSyncModes() { return new SyncMode[] {SyncMode.INTERNAL_CLOCK}; }

  public void setSlaveSyncMode(SyncMode sync) {
    if (sync != null && sync.equals(SyncMode.NO_SYNC)) {
      slave = sync;
    }
  }

  public SyncMode getSlaveSyncMode() { return slave; }

  public SyncMode[] getSlaveSyncModes() { return new SyncMode[] {SyncMode.NO_SYNC}; }

  public void setTrackMute(int track, boolean mute) {
    synchronized (lock) {
      if (sequence == null || track < 0 || track >= sequence.getTracks().length) {
        return;
      }
      if (mute) {
        muted.add(track);
      } else {
        muted.remove(track);
      }
    }
  }

  public boolean getTrackMute(int track) {
    synchronized (lock) {
      return muted.contains(track);
    }
  }

  public void setTrackSolo(int track, boolean solo) {
    synchronized (lock) {
      if (sequence == null || track < 0 || track >= sequence.getTracks().length) {
        return;
      }
      if (solo) {
        soloed.add(track);
      } else {
        soloed.remove(track);
      }
    }
  }

  public boolean getTrackSolo(int track) {
    synchronized (lock) {
      return soloed.contains(track);
    }
  }

  public boolean addMetaEventListener(MetaEventListener listener) {
    synchronized (lock) {
      if (!metaListeners.contains(listener)) {
        metaListeners.add(listener);
      }
      return true;
    }
  }

  public void removeMetaEventListener(MetaEventListener listener) {
    synchronized (lock) {
      metaListeners.remove(listener);
    }
  }

  public int[] addControllerEventListener(ControllerEventListener listener, int[] controllers) {
    synchronized (lock) {
      boolean[] set = controllerListeners.get(listener);
      if (set == null) {
        set = new boolean[128];
        controllerListeners.put(listener, set);
      }
      if (controllers != null) {
        for (int c : controllers) {
          if (c >= 0 && c < 128) {
            set[c] = true;
          }
        }
      }
      return listening(set);
    }
  }

  public int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers) {
    synchronized (lock) {
      boolean[] set = controllerListeners.get(listener);
      if (set == null) {
        return new int[0];
      }
      if (controllers == null) {
        controllerListeners.remove(listener);
        return new int[0];
      }
      for (int c : controllers) {
        if (c >= 0 && c < 128) {
          set[c] = false;
        }
      }
      int[] left = listening(set);
      if (left.length == 0) {
        controllerListeners.remove(listener);
      }
      return left;
    }
  }

  private static int[] listening(boolean[] set) {
    int n = 0;
    for (boolean b : set) {
      if (b) {
        n++;
      }
    }
    int[] out = new int[n];
    int k = 0;
    for (int i = 0; i < set.length; i++) {
      if (set[i]) {
        out[k++] = i;
      }
    }
    return out;
  }

  public void setLoopStartPoint(long tick) {
    synchronized (lock) {
      if (tick > getTickLength() || (loopEnd != -1 && tick > loopEnd) || tick < 0) {
        throw new IllegalArgumentException("invalid loop start point: " + tick);
      }
      loopStart = tick;
    }
  }

  public long getLoopStartPoint() {
    synchronized (lock) {
      return loopStart;
    }
  }

  public void setLoopEndPoint(long tick) {
    synchronized (lock) {
      if (tick > getTickLength() || (tick != -1 && loopStart > tick) || tick < -1) {
        throw new IllegalArgumentException("invalid loop end point: " + tick);
      }
      loopEnd = tick;
    }
  }

  public long getLoopEndPoint() {
    synchronized (lock) {
      return loopEnd;
    }
  }

  public void setLoopCount(int count) {
    if (count != LOOP_CONTINUOUSLY && count < 0) {
      throw new IllegalArgumentException("illegal value for loop count: " + count);
    }
    synchronized (lock) {
      loopCount = count;
    }
  }

  public int getLoopCount() {
    synchronized (lock) {
      return loopCount;
    }
  }

  private double microsPerTick() {
    float div = sequence.getDivisionType();
    if (div == Sequence.PPQ) {
      return mpq / sequence.getResolution() / tempoFactor;
    }
    return 1000000.0 / (div * sequence.getResolution()) / tempoFactor;
  }

  private long currentTick() {
    if (!running) {
      return tickPos;
    }
    double elapsedMicros = (System.nanoTime() - segStartNanos) / 1000.0;
    long t = segStartTick + (long) (elapsedMicros / microsPerTick());
    long len = sequence.getTickLength();
    return t > len ? len : t;
  }

  private void rebase() {
    if (running && sequence != null) {
      long t = currentTick();
      segStartTick = t;
      segStartNanos = System.nanoTime();
    }
  }

  private void applyTempoAt(long tick) {
    if (sequence == null || sequence.getDivisionType() != Sequence.PPQ) {
      return;
    }
    long best = -1;
    for (Track t : sequence.getTracks()) {
      for (int i = 0; i < t.size(); i++) {
        MidiEvent e = t.get(i);
        int v = tempoOf(e.getMessage());
        if (v > 0 && e.getTick() <= tick && e.getTick() >= best) {
          best = e.getTick();
          mpq = v;
        }
      }
    }
  }

  private static int tempoOf(MidiMessage m) {
    if (m.getLength() != 6 || m.getStatus() != 0xFF) {
      return -1;
    }
    byte[] b = m.getMessage();
    if (b[1] != 0x51 || b[2] != 0x03) {
      return -1;
    }
    return (b[5] & 0xFF) | ((b[4] & 0xFF) << 8) | ((b[3] & 0xFF) << 16);
  }

  private long tickToMicros(long tick) {
    if (sequence == null) {
      return 0;
    }
    return seqMicros(tick);
  }

  private long seqMicros(long tick) {
    float div = sequence.getDivisionType();
    if (div != Sequence.PPQ) {
      return (long) (1000000.0 * tick / (div * sequence.getResolution()));
    }
    List<long[]> tempos = fileTempos();
    long micros = 0;
    long last = 0;
    long cur = 500000;
    for (long[] t : tempos) {
      long et = Math.min(t[0], tick);
      micros += (et - last) * cur / sequence.getResolution();
      last = et;
      cur = t[1];
    }
    micros += (tick - last) * cur / sequence.getResolution();
    return micros;
  }

  private long microsToTick(long micros) {
    long lo = 0;
    long hi = Math.max(sequence.getTickLength(), 1);
    while (seqMicros(hi) < micros && hi < Long.MAX_VALUE / 4) {
      hi *= 2;
    }
    while (lo < hi) {
      long mid = (lo + hi) >>> 1;
      if (seqMicros(mid) < micros) {
        lo = mid + 1;
      } else {
        hi = mid;
      }
    }
    return lo;
  }

  private List<long[]> fileTempos() {
    List<long[]> out = new ArrayList<>();
    for (Track t : sequence.getTracks()) {
      for (int i = 0; i < t.size(); i++) {
        MidiEvent e = t.get(i);
        int v = tempoOf(e.getMessage());
        if (v > 0) {
          out.add(new long[] {e.getTick(), v});
        }
      }
    }
    Collections.sort(out, new java.util.Comparator<long[]>() {
      public int compare(long[] a, long[] b) {
        return Long.compare(a[0], b[0]);
      }
    });
    return out;
  }

  private List<Ev> snapshot() {
    List<Ev> evs = new ArrayList<>();
    Track[] tracks = sequence.getTracks();
    for (int ti = 0; ti < tracks.length; ti++) {
      Track t = tracks[ti];
      for (int i = 0; i < t.size(); i++) {
        MidiEvent e = t.get(i);
        evs.add(new Ev(e.getTick(), ti, e.getMessage()));
      }
    }
    Collections.sort(evs, new java.util.Comparator<Ev>() {
      public int compare(Ev a, Ev b) {
        return Long.compare(a.tick, b.tick);
      }
    });
    return evs;
  }

  private static int firstAtOrAfter(List<Ev> evs, long tick) {
    int lo = 0;
    int hi = evs.size();
    while (lo < hi) {
      int mid = (lo + hi) >>> 1;
      if (evs.get(mid).tick < tick) {
        lo = mid + 1;
      } else {
        hi = mid;
      }
    }
    return lo;
  }

  private void play() {
    List<Ev> evs;
    int idx;
    synchronized (lock) {
      evs = snapshot();
      idx = firstAtOrAfter(evs, tickPos);
    }
    while (true) {
      Ev ev;
      boolean finished = false;
      synchronized (lock) {
        if (!running || player != Thread.currentThread()) {
          return;
        }
        long len = sequence.getTickLength();
        long endTick = loopEnd == -1 ? len : loopEnd;
        boolean pastLoop = idx >= evs.size() || evs.get(idx).tick > endTick;
        if (pastLoop && (loopCount < 0 || loopsDone < loopCount)) {
          loopsDone++;
          segStartTick = loopStart;
          segStartNanos = System.nanoTime();
          idx = firstAtOrAfter(evs, loopStart);
          applyTempoAt(loopStart);
          continue;
        }
        if (idx >= evs.size()) {
          finished = true;
          ev = null;
        } else {
          ev = evs.get(idx);
          long waitNanos = (long) ((ev.tick - segStartTick) * microsPerTick() * 1000.0)
              - (System.nanoTime() - segStartNanos);
          if (waitNanos > 0) {
            try {
              lock.wait(waitNanos / 1000000L, (int) (waitNanos % 1000000L));
            } catch (InterruptedException e) {
              return;
            }
            continue;
          }
          idx++;
          int t = tempoOf(ev.message);
          if (t > 0 && sequence.getDivisionType() == Sequence.PPQ) {
            segStartTick = ev.tick;
            segStartNanos = System.nanoTime();
            mpq = t;
          }
        }
        if (finished) {
          tickPos = sequence.getTickLength();
          running = false;
          recording = false;
          silence();
          restoreState();
          fireEndOfTrack();
          return;
        }
      }
      dispatch(ev);
    }
  }

  private void dispatch(Ev ev) {
    MidiMessage msg = ev.message;
    if (msg instanceof MetaMessage) {
      if (((MetaMessage) msg).getType() == 0x2F) {
        return;
      }
      List<MetaEventListener> ls;
      synchronized (lock) {
        ls = new ArrayList<>(metaListeners);
      }
      for (MetaEventListener l : ls) {
        l.meta((MetaMessage) msg.clone());
      }
      return;
    }
    boolean audible;
    List<SeqTransmitter> ts;
    List<Map.Entry<ControllerEventListener, boolean[]>> cl = null;
    synchronized (lock) {
      audible = soloed.isEmpty() ? !muted.contains(ev.track) : soloed.contains(ev.track);
      ts = new ArrayList<>(transmitters);
      if (msg instanceof ShortMessage && ((ShortMessage) msg).getCommand() == ShortMessage.CONTROL_CHANGE) {
        cl = new ArrayList<>(controllerListeners.entrySet());
      }
    }
    if (audible) {
      long stamp = -1;
      for (SeqTransmitter t : ts) {
        Receiver r = t.getReceiver();
        if (r != null) {
          try {
            r.send(msg, stamp);
          } catch (IllegalStateException e) {
            continue;
          }
        }
      }
    }
    if (cl != null) {
      ShortMessage sm = (ShortMessage) msg;
      for (Map.Entry<ControllerEventListener, boolean[]> e : cl) {
        if (e.getValue()[sm.getData1()]) {
          e.getKey().controlChange((ShortMessage) sm.clone());
        }
      }
    }
  }

  private final class SeqTransmitter implements MidiDeviceTransmitter {
    private Receiver receiver;

    public MidiDevice getMidiDevice() { return SoftSequencer.this; }

    public void setReceiver(Receiver r) {
      synchronized (lock) {
        receiver = r;
      }
    }

    public Receiver getReceiver() {
      synchronized (lock) {
        return receiver;
      }
    }

    public void close() {
      synchronized (lock) {
        transmitters.remove(this);
      }
    }
  }

  private final class SeqReceiver implements MidiDeviceReceiver {
    private boolean closed;

    public MidiDevice getMidiDevice() { return SoftSequencer.this; }

    public void close() {
      closed = true;
      synchronized (lock) {
        receivers.remove(this);
      }
    }

    public void send(MidiMessage message, long timeStamp) {
      if (closed) {
        throw new IllegalStateException("Receiver is not open");
      }
      synchronized (lock) {
        if (!recording || sequence == null || !(message instanceof ShortMessage)) {
          return;
        }
        long tick = currentTick();
        int ch = ((ShortMessage) message).getChannel();
        for (Map.Entry<Track, Integer> e : recordTracks.entrySet()) {
          if (e.getValue() == -1 || e.getValue() == ch) {
            e.getKey().add(new MidiEvent((MidiMessage) message.clone(), tick));
          }
        }
      }
    }
  }
}
