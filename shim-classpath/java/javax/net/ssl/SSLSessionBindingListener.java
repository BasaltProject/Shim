/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.util.EventListener;

public interface SSLSessionBindingListener extends EventListener {
  void valueBound(SSLSessionBindingEvent event);

  void valueUnbound(SSLSessionBindingEvent event);
}
