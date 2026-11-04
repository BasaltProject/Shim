/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto;

import java.io.InputStream;

public class OctetStreamData implements Data {
  private InputStream octetStream;
  private String uri;
  private String mimeType;

  public OctetStreamData(InputStream octetStream) {
    if (octetStream == null) {
      throw new NullPointerException("octetStream is null");
    }
    this.octetStream = octetStream;
  }

  public OctetStreamData(InputStream octetStream, String uri, String mimeType) {
    if (octetStream == null) {
      throw new NullPointerException("octetStream is null");
    }
    this.octetStream = octetStream;
    this.uri = uri;
    this.mimeType = mimeType;
  }

  public InputStream getOctetStream() {
    return octetStream;
  }

  public String getURI() {
    return uri;
  }

  public String getMimeType() {
    return mimeType;
  }
}
