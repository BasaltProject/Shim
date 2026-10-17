/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.io.OutputStream;
import java.io.Writer;
import org.w3c.dom.ls.LSOutput;

public class DomLsOutput implements LSOutput {
  private Writer characterStream;
  private OutputStream byteStream;
  private String systemId;
  private String encoding;

  public Writer getCharacterStream() {
    return characterStream;
  }

  public void setCharacterStream(Writer characterStream) {
    this.characterStream = characterStream;
  }

  public OutputStream getByteStream() {
    return byteStream;
  }

  public void setByteStream(OutputStream byteStream) {
    this.byteStream = byteStream;
  }

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
}
