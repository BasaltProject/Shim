/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth.callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

@Deprecated
public class TextCallbackHandler implements CallbackHandler {
  public TextCallbackHandler() {
  }

  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    ConfirmationCallback confirmation = null;
    for (int i = 0; i < callbacks.length; i++) {
      if (callbacks[i] instanceof TextOutputCallback) {
        TextOutputCallback tc = (TextOutputCallback) callbacks[i];
        String text;
        switch (tc.getMessageType()) {
          case TextOutputCallback.INFORMATION:
            text = "";
            break;
          case TextOutputCallback.WARNING:
            text = "Warning: ";
            break;
          case TextOutputCallback.ERROR:
            text = "Error: ";
            break;
          default:
            throw new UnsupportedCallbackException(callbacks[i], "Unrecognized message type");
        }
        String message = tc.getMessage();
        if (message != null) {
          text += message;
        }
        if (text != null) {
          System.err.println(text);
        }
      } else if (callbacks[i] instanceof NameCallback) {
        NameCallback nc = (NameCallback) callbacks[i];
        if (nc.getDefaultName() == null) {
          System.err.print(nc.getPrompt());
        } else {
          System.err.print(nc.getPrompt() + " [" + nc.getDefaultName() + "] ");
        }
        System.err.flush();
        String result = readLine();
        if (result.equals("")) {
          result = nc.getDefaultName();
        }
        nc.setName(result);
      } else if (callbacks[i] instanceof PasswordCallback) {
        PasswordCallback pc = (PasswordCallback) callbacks[i];
        System.err.print(pc.getPrompt());
        System.err.flush();
        pc.setPassword(readLine().toCharArray());
      } else if (callbacks[i] instanceof ConfirmationCallback) {
        confirmation = (ConfirmationCallback) callbacks[i];
      } else {
        throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
      }
    }
    if (confirmation != null) {
      System.err.println(confirmation.getPrompt() == null ? "" : confirmation.getPrompt());
      String[] options = confirmation.getOptions();
      for (int k = 0; options != null && k < options.length; k++) {
        System.err.println(k + ". " + options[k]);
      }
      System.err.flush();
      String line = readLine();
      try {
        confirmation.setSelectedIndex(Integer.parseInt(line.trim()));
      } catch (NumberFormatException e) {
        confirmation.setSelectedIndex(confirmation.getDefaultOption());
      }
    }
  }

  private String readLine() throws IOException {
    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
    String result = r.readLine();
    if (result == null) {
      throw new IOException("Cannot read from System.in");
    }
    return result;
  }
}
