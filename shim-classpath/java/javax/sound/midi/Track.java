/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

import java.util.ArrayList;
import java.util.HashSet;

public final class Track {
  private final ArrayList<MidiEvent> eventsList = new ArrayList<>();
  private final HashSet<MidiEvent> set = new HashSet<>();
  private MidiEvent eotEvent;

  Track() {
    ImmutableEndOfTrack eot = new ImmutableEndOfTrack();
    eotEvent = new MidiEvent(eot, 0);
    eventsList.add(eotEvent);
    set.add(eotEvent);
  }

  public boolean add(MidiEvent event) {
    if (event == null) {
      return false;
    }
    synchronized (eventsList) {
      if (!set.contains(event)) {
        int eventsCount = eventsList.size();
        MidiEvent lastEvent = null;
        if (eventsCount > 0) {
          lastEvent = eventsList.get(eventsCount - 1);
        }
        if (lastEvent != eotEvent) {
          if (lastEvent != null) {
            eotEvent.setTick(lastEvent.getTick());
          } else {
            eotEvent.setTick(0);
          }
          eventsList.add(eotEvent);
          set.add(eotEvent);
          eventsCount = eventsList.size();
        }
        if (MidiUtils.isMetaEndOfTrack(event.getMessage())) {
          if (event.getTick() > eotEvent.getTick()) {
            eotEvent.setTick(event.getTick());
          }
          return true;
        }
        set.add(event);
        int i = eventsCount;
        for (; i > 0; i--) {
          if (event.getTick() >= (eventsList.get(i - 1)).getTick()) {
            break;
          }
        }
        if (i == eventsCount) {
          eventsList.set(eventsCount - 1, event);
          if (eotEvent.getTick() < event.getTick()) {
            eotEvent.setTick(event.getTick());
          }
          eventsList.add(eotEvent);
        } else {
          eventsList.add(i, event);
        }
        return true;
      }
    }
    return false;
  }

  public boolean remove(MidiEvent event) {
    synchronized (eventsList) {
      if (set.remove(event)) {
        int i = eventsList.indexOf(event);
        if (i >= 0) {
          eventsList.remove(i);
          return true;
        }
      }
    }
    return false;
  }

  public MidiEvent get(int index) throws ArrayIndexOutOfBoundsException {
    try {
      synchronized (eventsList) {
        return eventsList.get(index);
      }
    } catch (IndexOutOfBoundsException ioobe) {
      throw new ArrayIndexOutOfBoundsException(ioobe.getMessage());
    }
  }

  public int size() {
    synchronized (eventsList) {
      return eventsList.size();
    }
  }

  public long ticks() {
    long ret = 0;
    synchronized (eventsList) {
      if (eventsList.size() > 0) {
        ret = (eventsList.get(eventsList.size() - 1)).getTick();
      }
    }
    return ret;
  }

  private static final class ImmutableEndOfTrack extends MetaMessage {
    private ImmutableEndOfTrack() {
      super(new byte[3]);
      data[0] = (byte) 0xFF;
      data[1] = (byte) 0x2F;
      data[2] = (byte) 0;
    }

    public void setMessage(int type, byte[] data, int length) throws InvalidMidiDataException {
      throw new InvalidMidiDataException("Not allowed to change the end of track message");
    }
  }
}
