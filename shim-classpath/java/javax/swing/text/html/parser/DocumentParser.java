/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html.parser;

import java.io.IOException;
import java.io.Reader;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class DocumentParser extends Parser {
  private HTMLEditorKit.ParserCallback callback;
  private boolean inbody;

  public DocumentParser(DTD dtd) {
    super(dtd);
  }

  public void parse(Reader in, HTMLEditorKit.ParserCallback callback, boolean ignoreCharSet) throws IOException {
    this.callback = callback;
    parse(in);
    callback.handleEndOfLineString("\n");
    try {
      callback.flush();
    } catch (javax.swing.text.BadLocationException e) {
      // the reader reports positions only for inserts
    }
  }

  protected void handleText(char[] text) { callback.handleText(text, getCurrentPos()); }

  protected void handleComment(char[] text) { callback.handleComment(text, getCurrentPos()); }

  protected void handleStartTag(TagElement tag) {
    MutableAttributeSet attributes = getAttributes();
    if (tag.fictional()) {
      attributes.addAttribute(HTMLEditorKit.ParserCallback.IMPLIED, Boolean.TRUE);
    }
    callback.handleStartTag(tag.getHTMLTag(), attributes, getCurrentPos());
  }

  protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException {
    callback.handleSimpleTag(tag.getHTMLTag(), getAttributes(), getCurrentPos());
  }

  protected void handleEndTag(TagElement tag) { callback.handleEndTag(tag.getHTMLTag(), getCurrentPos()); }

  protected void handleError(int ln, String errorMsg) { callback.handleError(errorMsg, getCurrentPos()); }
}
