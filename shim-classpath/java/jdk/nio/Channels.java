/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.nio;

public final class Channels {
  private Channels() {
  }

  public static java.nio.channels.SelectableChannel readWriteSelectableChannel(java.io.FileDescriptor fileDescriptor, jdk.nio.Channels.SelectableChannelCloser selectableChannelCloser) {
    throw new UnsupportedOperationException();
  }

  public interface SelectableChannelCloser {
    void implCloseChannel(java.nio.channels.SelectableChannel selectableChannel) throws java.io.IOException;
    void implReleaseChannel(java.nio.channels.SelectableChannel selectableChannel) throws java.io.IOException;
  }
}
