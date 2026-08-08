/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

import java.util.Vector;

public class Sequence {
  public static final float PPQ = 0.0f;
  public static final float SMPTE_24 = 24.0f;
  public static final float SMPTE_25 = 25.0f;
  public static final float SMPTE_30DROP = 29.97f;
  public static final float SMPTE_30 = 30.0f;

  protected float divisionType;
  protected int resolution;
  protected Vector<Track> tracks = new Vector<>();

  public Sequence(float divisionType, int resolution) throws InvalidMidiDataException {
    if (divisionType == PPQ) {
      this.divisionType = PPQ;
    } else if (divisionType == SMPTE_24) {
      this.divisionType = SMPTE_24;
    } else if (divisionType == SMPTE_25) {
      this.divisionType = SMPTE_25;
    } else if (divisionType == SMPTE_30DROP) {
      this.divisionType = SMPTE_30DROP;
    } else if (divisionType == SMPTE_30) {
      this.divisionType = SMPTE_30;
    } else {
      throw new InvalidMidiDataException("Unsupported division type: " + divisionType);
    }
    this.resolution = resolution;
  }

  public Sequence(float divisionType, int resolution, int numTracks) throws InvalidMidiDataException {
    this(divisionType, resolution);
    for (int i = 0; i < numTracks; i++) {
      tracks.addElement(new Track());
    }
  }

  public float getDivisionType() { return divisionType; }

  public int getResolution() { return resolution; }

  public Track createTrack() {
    Track track = new Track();
    tracks.addElement(track);
    return track;
  }

  public boolean deleteTrack(Track track) {
    synchronized (tracks) {
      return tracks.removeElement(track);
    }
  }

  public Track[] getTracks() {
    return tracks.toArray(new Track[tracks.size()]);
  }

  public long getMicrosecondLength() {
    return microsecondLength(this, getTickLength());
  }

  static long microsecondLength(Sequence seq, long tick) {
    if (seq.getDivisionType() != PPQ) {
      double framesPerSecond = seq.getDivisionType();
      return (long) (1000000.0 * tick / (framesPerSecond * seq.getResolution()));
    }
    long micros = 0;
    long lastTick = 0;
    int tempo = 500000;
    java.util.List<MidiEvent> tempos = new java.util.ArrayList<>();
    for (Track t : seq.getTracks()) {
      for (int i = 0; i < t.size(); i++) {
        MidiEvent e = t.get(i);
        if (MidiUtils.isMetaTempo(e.getMessage())) {
          tempos.add(e);
        }
      }
    }
    java.util.Collections.sort(tempos, new java.util.Comparator<MidiEvent>() {
      public int compare(MidiEvent a, MidiEvent b) {
        return Long.compare(a.getTick(), b.getTick());
      }
    });
    for (MidiEvent e : tempos) {
      long et = Math.min(e.getTick(), tick);
      micros += (et - lastTick) * (long) tempo / seq.getResolution();
      lastTick = et;
      tempo = MidiUtils.getTempoMPQ(e.getMessage());
    }
    micros += (tick - lastTick) * (long) tempo / seq.getResolution();
    return micros;
  }

  public long getTickLength() {
    long length = 0;
    synchronized (tracks) {
      for (int i = 0; i < tracks.size(); i++) {
        long temp = tracks.elementAt(i).ticks();
        if (temp > length) {
          length = temp;
        }
      }
      return length;
    }
  }

  public Patch[] getPatchList() {
    return new Patch[0];
  }
}
