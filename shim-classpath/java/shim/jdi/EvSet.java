/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdi;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;

public final class EvSet extends AbstractSet<Event> implements EventSet {
  private final Vm vm;
  private final List<Event> events;
  private final int policy;

  EvSet(Vm vm, List<Event> events, int policy) {
    this.vm = vm;
    this.events = events;
    this.policy = policy;
  }

  public VirtualMachine virtualMachine() {
    return vm.proxy;
  }

  public int suspendPolicy() {
    return policy == 0 ? com.sun.jdi.request.EventRequest.SUSPEND_NONE
        : policy == 1 ? com.sun.jdi.request.EventRequest.SUSPEND_EVENT_THREAD
        : com.sun.jdi.request.EventRequest.SUSPEND_ALL;
  }

  public void resume() {
    if (policy == 2) {
      vm.proxy.resume();
    } else if (policy == 1) {
      for (Event e : events) {
        if (e instanceof com.sun.jdi.event.LocatableEvent) {
          ((com.sun.jdi.event.LocatableEvent) e).thread().resume();
          return;
        }
        if (e instanceof com.sun.jdi.event.ThreadStartEvent) {
          ((com.sun.jdi.event.ThreadStartEvent) e).thread().resume();
          return;
        }
      }
    }
  }

  public Iterator<Event> iterator() {
    return eventIterator();
  }

  public EventIterator eventIterator() {
    final Iterator<Event> it = events.iterator();
    return new EventIterator() {
      public boolean hasNext() {
        return it.hasNext();
      }

      public Event next() {
        return it.next();
      }

      public Event nextEvent() {
        return it.next();
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public int size() {
    return events.size();
  }

  public String toString() {
    return "event set, policy=" + policy + ", count = " + events.size() + " = " + events;
  }
}
