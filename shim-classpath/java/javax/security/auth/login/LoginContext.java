/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;

public class LoginContext {
  private static final String kOther = "other";

  private static final class ModuleInfo {
    final AppConfigurationEntry entry;
    LoginModule module;

    ModuleInfo(AppConfigurationEntry entry) {
      this.entry = entry;
    }
  }

  private Subject subject;
  private boolean subjectProvided;
  private boolean loginSucceeded;
  private CallbackHandler callbackHandler;
  private final Map<String, Object> state = new HashMap<String, Object>();
  private Configuration config;
  private ModuleInfo[] moduleStack;
  private ClassLoader contextClassLoader;
  private int moduleIndex;

  private void init(String name) throws LoginException {
    if (name == null) {
      throw new LoginException("Invalid null input: name");
    }
    if (config == null) {
      config = Configuration.getConfiguration();
    }
    AppConfigurationEntry[] entries = config.getAppConfigurationEntry(name);
    if (entries == null) {
      entries = config.getAppConfigurationEntry(kOther);
      if (entries == null) {
        throw new LoginException("No LoginModules configured for " + name);
      }
    }
    moduleStack = new ModuleInfo[entries.length];
    for (int i = 0; i < entries.length; i++) {
      moduleStack[i] = new ModuleInfo(new AppConfigurationEntry(entries[i].getLoginModuleName(),
          entries[i].getControlFlag(), entries[i].getOptions()));
    }
    contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader == null) {
      contextClassLoader = ClassLoader.getSystemClassLoader();
    }
  }

  public LoginContext(String name) throws LoginException {
    init(name);
  }

  public LoginContext(String name, Subject subject) throws LoginException {
    init(name);
    if (subject == null) {
      throw new LoginException("invalid null Subject provided");
    }
    this.subject = subject;
    subjectProvided = true;
  }

  public LoginContext(String name, CallbackHandler callbackHandler) throws LoginException {
    init(name);
    if (callbackHandler == null) {
      throw new LoginException("invalid null CallbackHandler provided");
    }
    this.callbackHandler = callbackHandler;
  }

  public LoginContext(String name, Subject subject, CallbackHandler callbackHandler) throws LoginException {
    this(name, subject);
    if (callbackHandler == null) {
      throw new LoginException("invalid null CallbackHandler provided");
    }
    this.callbackHandler = callbackHandler;
  }

  public LoginContext(String name, Subject subject, CallbackHandler callbackHandler, Configuration config)
      throws LoginException {
    this.config = config;
    init(name);
    if (subject != null) {
      this.subject = subject;
      subjectProvided = true;
    }
    this.callbackHandler = callbackHandler;
  }

  public void login() throws LoginException {
    loginSucceeded = false;
    if (subject == null) {
      subject = new Subject();
    }
    try {
      invoke("login");
      invoke("commit");
      loginSucceeded = true;
    } catch (LoginException le) {
      try {
        invoke("abort");
      } catch (LoginException le2) {
        throw le;
      }
      throw le;
    }
  }

  public void logout() throws LoginException {
    if (subject == null) {
      throw new LoginException("null subject - logout called before login");
    }
    invoke("logout");
  }

  public Subject getSubject() {
    if (!loginSucceeded && !subjectProvided) {
      return null;
    }
    return subject;
  }

  private void clearState() {
    moduleIndex = 0;
    state.clear();
  }

  private void throwException(LoginException originalError, LoginException le) throws LoginException {
    clearState();
    throw originalError != null ? originalError : le;
  }

  private void invoke(String methodName) throws LoginException {
    LoginException firstError = null;
    LoginException firstRequiredError = null;
    boolean success = false;
    int index = 0;
    for (int i = index; i < moduleStack.length; i++) {
      moduleIndex = i;
      try {
        if (moduleStack[i].module == null) {
          Class<?> c = Class.forName(moduleStack[i].entry.getLoginModuleName(), true, contextClassLoader);
          moduleStack[i].module = (LoginModule) c.getConstructor().newInstance();
          moduleStack[i].module.initialize(subject, callbackHandler, state, moduleStack[i].entry.getOptions());
        }
        Method m = LoginModule.class.getMethod(methodName);
        boolean status = ((Boolean) m.invoke(moduleStack[i].module)).booleanValue();
        if (status) {
          if (!methodName.equals("abort") && !methodName.equals("logout")
              && moduleStack[i].entry.getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT
              && firstRequiredError == null) {
            clearState();
            return;
          }
          success = true;
        }
      } catch (NoSuchMethodException e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        new InstantiationException(moduleStack[i].entry.getLoginModuleName()).printStackTrace(new java.io.PrintWriter(sw));
        throw new LoginException(sw.toString());
      } catch (InstantiationException e) {
        throw new LoginException("unable to instantiate LoginModule: " + e.getMessage());
      } catch (ClassNotFoundException e) {
        throw new LoginException("No LoginModule found for " + e.getMessage());
      } catch (IllegalAccessException e) {
        throw new LoginException("unable to access LoginModule: " + e.getMessage());
      } catch (InvocationTargetException ite) {
        LoginException le;
        if (ite.getCause() instanceof LoginException) {
          le = (LoginException) ite.getCause();
        } else if (ite.getCause() instanceof SecurityException) {
          le = new LoginException("Security Exception");
          le.initCause(new SecurityException());
        } else {
          java.io.StringWriter sw = new java.io.StringWriter();
          ite.getCause().printStackTrace(new java.io.PrintWriter(sw));
          le = new LoginException(sw.toString());
        }
        AppConfigurationEntry.LoginModuleControlFlag flag = moduleStack[i].entry.getControlFlag();
        if (flag == AppConfigurationEntry.LoginModuleControlFlag.REQUISITE) {
          if (methodName.equals("abort") || methodName.equals("logout")) {
            if (firstRequiredError == null) {
              firstRequiredError = le;
            }
          } else {
            throwException(firstRequiredError, le);
          }
        } else if (flag == AppConfigurationEntry.LoginModuleControlFlag.REQUIRED) {
          if (firstRequiredError == null) {
            firstRequiredError = le;
          }
        } else if (firstError == null) {
          firstError = le;
        }
      }
    }
    if (firstRequiredError != null) {
      throwException(firstRequiredError, null);
    } else if (!success && firstError != null) {
      throwException(firstError, null);
    } else if (!success) {
      throwException(new LoginException("Login Failure: all modules ignored"), null);
    }
  }
}
