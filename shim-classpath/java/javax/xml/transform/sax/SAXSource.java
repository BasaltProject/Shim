/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform.sax;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class SAXSource implements Source {
  public static final String FEATURE = "http://javax.xml.transform.sax.SAXSource/feature";

  private XMLReader reader;
  private InputSource inputSource;

  public SAXSource() {
  }

  public SAXSource(XMLReader reader, InputSource inputSource) {
    this.reader = reader;
    this.inputSource = inputSource;
  }

  public SAXSource(InputSource inputSource) {
    this.inputSource = inputSource;
  }

  public void setXMLReader(XMLReader reader) {
    this.reader = reader;
  }

  public XMLReader getXMLReader() {
    return reader;
  }

  public void setInputSource(InputSource inputSource) {
    this.inputSource = inputSource;
  }

  public InputSource getInputSource() {
    return inputSource;
  }

  public void setSystemId(String systemId) {
    if (null == inputSource) {
      inputSource = new InputSource(systemId);
    } else {
      inputSource.setSystemId(systemId);
    }
  }

  public String getSystemId() {
    if (inputSource == null) {
      return null;
    }
    return inputSource.getSystemId();
  }

  public static InputSource sourceToInputSource(Source source) {
    if (source instanceof SAXSource) {
      return ((SAXSource) source).getInputSource();
    } else if (source instanceof StreamSource) {
      StreamSource ss = (StreamSource) source;
      InputSource isource = new InputSource(ss.getSystemId());
      isource.setByteStream(ss.getInputStream());
      isource.setCharacterStream(ss.getReader());
      isource.setPublicId(ss.getPublicId());
      return isource;
    }
    return null;
  }

  @Override
  public boolean isEmpty() {
    return inputSource == null && reader == null;
  }
}
