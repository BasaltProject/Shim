/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public class ConfigFile extends Configuration {
  private final URI uri;
  private Map<String, AppConfigurationEntry[]> entries;

  public ConfigFile() {
    this.uri = null;
    init();
  }

  public ConfigFile(URI uri) {
    this.uri = uri;
    init();
  }

  private void init() {
    Map<String, AppConfigurationEntry[]> loaded = new HashMap<String, AppConfigurationEntry[]>();
    boolean found = false;
    try {
      if (uri != null) {
        found = load(uri.toURL(), loaded);
      } else {
        String prop = System.getProperty("java.security.auth.login.config");
        if (prop != null) {
          prop = prop.replace("${java.home}", System.getProperty("java.home")).replace("${user.home}",
              System.getProperty("user.home"));
          URL u;
          try {
            u = new URL(prop);
          } catch (java.net.MalformedURLException e) {
            u = new java.io.File(prop).toURI().toURL();
          }
          found = load(u, loaded);
        }
        if (!found) {
          java.io.File home = new java.io.File(System.getProperty("user.home"), ".java.login.config");
          if (home.exists()) {
            found = load(home.toURI().toURL(), loaded);
          }
        }
      }
    } catch (IOException ioe) {
      throw new SecurityException(ioe.toString(), ioe);
    }
    if (!found) {
      throw new SecurityException(new IOException("Unable to locate a login configuration"));
    }
    entries = loaded;
  }

  private boolean load(URL url, Map<String, AppConfigurationEntry[]> into) throws IOException {
    InputStream in;
    try {
      in = url.openStream();
    } catch (IOException e) {
      throw new IOException("Unable to locate a login configuration" + (e.getMessage() == null ? "" : " (" + e.getMessage() + ")"));
    }
    try {
      parse(new BufferedReader(new InputStreamReader(in, "UTF-8")), into);
    } finally {
      in.close();
    }
    return true;
  }

  private static String substitute(String s) throws IOException {
    int i;
    while ((i = s.indexOf("${")) >= 0) {
      int j = s.indexOf('}', i);
      if (j < 0) {
        throw new IOException("Configuration Error:\n\tunbalanced property reference");
      }
      String key = s.substring(i + 2, j);
      String v = System.getProperty(key);
      if (v == null) {
        throw new IOException("Configuration Error:\n\tsystem property " + key + " expanded to empty value");
      }
      s = s.substring(0, i) + v + s.substring(j + 1);
    }
    return s;
  }

  private void parse(BufferedReader r, Map<String, AppConfigurationEntry[]> into) throws IOException {
    StreamTokenizer st = new StreamTokenizer(r);
    st.resetSyntax();
    st.wordChars('a', 'z');
    st.wordChars('A', 'Z');
    st.wordChars('0', '9');
    st.wordChars(':', ':');
    st.wordChars('.', '.');
    st.wordChars('-', '-');
    st.wordChars('_', '_');
    st.wordChars('$', '$');
    st.wordChars(128 + 32, 255);
    st.whitespaceChars(0, ' ');
    st.commentChar('/');
    st.quoteChar('\'');
    st.quoteChar('"');
    st.lowerCaseMode(false);
    st.slashSlashComments(true);
    st.slashStarComments(true);
    while (st.nextToken() != StreamTokenizer.TT_EOF) {
      if (st.ttype != StreamTokenizer.TT_WORD && st.ttype != '"' && st.ttype != '\'') {
        throw error(st, "expected application name");
      }
      String name = st.sval;
      expect(st, '{');
      List<AppConfigurationEntry> list = new ArrayList<AppConfigurationEntry>();
      while (st.nextToken() != '}') {
        if (st.ttype == StreamTokenizer.TT_EOF) {
          throw error(st, "expected '}'");
        }
        if (st.ttype != StreamTokenizer.TT_WORD && st.ttype != '"') {
          throw error(st, "expected login module class name");
        }
        String module = st.sval;
        if (st.nextToken() != StreamTokenizer.TT_WORD) {
          throw error(st, "expected controlFlag");
        }
        AppConfigurationEntry.LoginModuleControlFlag flag;
        String f = st.sval.toUpperCase(java.util.Locale.ENGLISH);
        if (f.equals("REQUIRED")) {
          flag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
        } else if (f.equals("REQUISITE")) {
          flag = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
        } else if (f.equals("SUFFICIENT")) {
          flag = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
        } else if (f.equals("OPTIONAL")) {
          flag = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
        } else {
          throw error(st, "Invalid controlFlag: " + st.sval);
        }
        Map<String, String> opts = new HashMap<String, String>();
        while (st.nextToken() != ';') {
          if (st.ttype != StreamTokenizer.TT_WORD) {
            throw error(st, "expected ';' or option name");
          }
          String key = st.sval;
          expect(st, '=');
          if (st.nextToken() != '"' && st.ttype != StreamTokenizer.TT_WORD && st.ttype != '\'') {
            throw error(st, "expected option value");
          }
          opts.put(key, substitute(st.sval));
        }
        list.add(new AppConfigurationEntry(substitute(module), flag, opts));
      }
      expect(st, ';');
      into.put(name, list.toArray(new AppConfigurationEntry[0]));
    }
  }

  private static void expect(StreamTokenizer st, int c) throws IOException {
    if (st.nextToken() != c) {
      throw error(st, "expected '" + (char) c + "'");
    }
  }

  private static IOException error(StreamTokenizer st, String msg) {
    return new IOException("java.io.IOException: " + msg + ", read end of file/line " + st.lineno());
  }

  @Override
  public AppConfigurationEntry[] getAppConfigurationEntry(String applicationName) {
    AppConfigurationEntry[] e = entries.get(applicationName);
    return e == null ? null : e.clone();
  }

  @Override
  public synchronized void refresh() {
    init();
  }
}
