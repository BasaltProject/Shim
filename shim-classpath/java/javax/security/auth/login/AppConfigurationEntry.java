/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AppConfigurationEntry {
  private final String loginModuleName;
  private final LoginModuleControlFlag controlFlag;
  private final Map<String, ?> options;

  public AppConfigurationEntry(String loginModuleName, LoginModuleControlFlag controlFlag, Map<String, ?> options) {
    if (loginModuleName == null || loginModuleName.isEmpty()
        || (controlFlag != LoginModuleControlFlag.REQUIRED && controlFlag != LoginModuleControlFlag.REQUISITE
        && controlFlag != LoginModuleControlFlag.SUFFICIENT && controlFlag != LoginModuleControlFlag.OPTIONAL)
        || options == null) {
      throw new IllegalArgumentException();
    }
    this.loginModuleName = loginModuleName;
    this.controlFlag = controlFlag;
    this.options = Collections.unmodifiableMap(new HashMap<String, Object>(options));
  }

  public String getLoginModuleName() {
    return loginModuleName;
  }

  public LoginModuleControlFlag getControlFlag() {
    return controlFlag;
  }

  public Map<String, ?> getOptions() {
    return options;
  }

  public static class LoginModuleControlFlag {
    private final String controlFlag;

    public static final LoginModuleControlFlag REQUIRED = new LoginModuleControlFlag("required");
    public static final LoginModuleControlFlag REQUISITE = new LoginModuleControlFlag("requisite");
    public static final LoginModuleControlFlag SUFFICIENT = new LoginModuleControlFlag("sufficient");
    public static final LoginModuleControlFlag OPTIONAL = new LoginModuleControlFlag("optional");

    private LoginModuleControlFlag(String controlFlag) {
      this.controlFlag = controlFlag;
    }

    @Override
    public String toString() {
      return "LoginModuleControlFlag: " + controlFlag;
    }
  }
}
