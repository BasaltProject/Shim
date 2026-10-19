/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package jdk.net;

import java.net.SocketOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ExtendedSocketOptions {
  private static final class ExtSocketOption<T> implements SocketOption<T> {
    private final String name;
    private final Class<T> type;

    ExtSocketOption(String name, Class<T> type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public Class<T> type() {
      return type;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public static final SocketOption<Boolean> TCP_QUICKACK = new ExtSocketOption<Boolean>("TCP_QUICKACK", Boolean.class);

  public static final SocketOption<Integer> TCP_KEEPIDLE = new ExtSocketOption<Integer>("TCP_KEEPIDLE", Integer.class);

  public static final SocketOption<Integer> TCP_KEEPINTERVAL =
      new ExtSocketOption<Integer>("TCP_KEEPINTERVAL", Integer.class);

  public static final SocketOption<Integer> TCP_KEEPCOUNT = new ExtSocketOption<Integer>("TCP_KEEPCOUNT", Integer.class);

  public static final SocketOption<Integer> SO_INCOMING_NAPI_ID =
      new ExtSocketOption<Integer>("SO_INCOMING_NAPI_ID", Integer.class);

  public static final SocketOption<UnixDomainPrincipal> SO_PEERCRED =
      new ExtSocketOption<UnixDomainPrincipal>("SO_PEERCRED", UnixDomainPrincipal.class);

  public static final SocketOption<Boolean> IP_DONTFRAGMENT =
      new ExtSocketOption<Boolean>("IP_DONTFRAGMENT", Boolean.class);

  private ExtendedSocketOptions() { }

  static Set<SocketOption<?>> options() {
    Set<SocketOption<?>> options = new HashSet<SocketOption<?>>();
    options.add(TCP_QUICKACK);
    options.add(TCP_KEEPIDLE);
    options.add(TCP_KEEPINTERVAL);
    options.add(TCP_KEEPCOUNT);
    options.add(SO_INCOMING_NAPI_ID);
    options.add(SO_PEERCRED);
    options.add(IP_DONTFRAGMENT);
    return Collections.unmodifiableSet(options);
  }
}
