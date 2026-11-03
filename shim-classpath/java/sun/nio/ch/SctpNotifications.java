/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.PeerAddressChangeNotification;
import com.sun.nio.sctp.SendFailedNotification;
import com.sun.nio.sctp.ShutdownNotification;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

final class SctpNotifications {
  private SctpNotifications() {
  }

  static final class AssocChange extends AssociationChangeNotification {
    private final Association association;
    private final AssociationChangeNotification.AssocChangeEvent event;

    AssocChange(Association association, AssociationChangeNotification.AssocChangeEvent event) {
      this.association = association;
      this.event = event;
    }

    public Association association() {
      return association;
    }

    public AssociationChangeNotification.AssocChangeEvent event() {
      return event;
    }

    public String toString() {
      return super.toString() + " [Association: " + association + ", Event: " + event + "]";
    }
  }

  static final class PeerAddress extends PeerAddressChangeNotification {
    private final Association association;
    private final SocketAddress address;
    private final PeerAddressChangeNotification.AddressChangeEvent event;

    PeerAddress(Association association, SocketAddress address,
        PeerAddressChangeNotification.AddressChangeEvent event) {
      this.association = association;
      this.address = address;
      this.event = event;
    }

    public Association association() {
      return association;
    }

    public SocketAddress address() {
      return address;
    }

    public PeerAddressChangeNotification.AddressChangeEvent event() {
      return event;
    }

    public String toString() {
      return super.toString() + " [Association: " + association + ", Address: " + address + ", Event: " + event + "]";
    }
  }

  static final class SendFailed extends SendFailedNotification {
    private final Association association;
    private final SocketAddress address;
    private final ByteBuffer buffer;
    private final int errorCode;
    private final int streamNumber;

    SendFailed(Association association, SocketAddress address, ByteBuffer buffer, int errorCode, int streamNumber) {
      this.association = association;
      this.address = address;
      this.buffer = buffer;
      this.errorCode = errorCode;
      this.streamNumber = streamNumber;
    }

    public Association association() {
      return association;
    }

    public SocketAddress address() {
      return address;
    }

    public ByteBuffer buffer() {
      return buffer;
    }

    public int errorCode() {
      return errorCode;
    }

    public int streamNumber() {
      return streamNumber;
    }

    public String toString() {
      return super.toString() + " [Association: " + association + ", Address: " + address + ", Buffer: " + buffer
          + ", Error: " + errorCode + ", Stream: " + streamNumber + "]";
    }
  }

  static final class Shutdown extends ShutdownNotification {
    private final Association association;

    Shutdown(Association association) {
      this.association = association;
    }

    public Association association() {
      return association;
    }

    public String toString() {
      return super.toString() + " [Association: " + association + "]";
    }
  }
}
