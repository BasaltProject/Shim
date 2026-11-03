/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

public class JTextPane extends JEditorPane {
  public JTextPane() {
    super();
    EditorKit editorKit = createDefaultEditorKit();
    String contentType = editorKit.getContentType();
    if (contentType != null && getEditorKitClassNameForContentType(contentType)
        .equals("javax.swing.JEditorPane$PlainEditorKit")) {
      setEditorKitForContentType(contentType, editorKit);
    }
    setEditorKit(editorKit);
  }

  public JTextPane(StyledDocument doc) {
    this();
    setStyledDocument(doc);
  }

  public String getUIClassID() { return "TextPaneUI"; }

  public void setDocument(Document doc) {
    if (doc instanceof StyledDocument) {
      super.setDocument(doc);
    } else {
      throw new IllegalArgumentException("Model must be StyledDocument");
    }
  }

  public void setStyledDocument(StyledDocument doc) { super.setDocument(doc); }

  public StyledDocument getStyledDocument() { return (StyledDocument) getDocument(); }

  public void replaceSelection(String content) { replaceSelection(content, true); }

  private void replaceSelection(String content, boolean checkEditable) {
    if (checkEditable && !isEditable()) {
      UIManager.getLookAndFeel().provideErrorFeedback(JTextPane.this);
      return;
    }
    Document doc = getStyledDocument();
    if (doc != null) {
      try {
        Caret caret = getCaret();
        int p0 = Math.min(caret.getDot(), caret.getMark());
        int p1 = Math.max(caret.getDot(), caret.getMark());
        AttributeSet attr = getInputAttributes().copyAttributes();
        if (doc instanceof AbstractDocument) {
          ((AbstractDocument) doc).replace(p0, p1 - p0, content, attr);
        } else {
          if (p0 != p1) {
            doc.remove(p0, p1 - p0);
          }
          if (content != null && content.length() > 0) {
            doc.insertString(p0, content, attr);
          }
        }
      } catch (BadLocationException e) {
        UIManager.getLookAndFeel().provideErrorFeedback(JTextPane.this);
      }
    }
  }

  public void insertComponent(Component c) {
    MutableAttributeSet inputAttributes = getInputAttributes();
    inputAttributes.removeAttributes(inputAttributes);
    StyleConstants.setComponent(inputAttributes, c);
    replaceSelection(" ", false);
    inputAttributes.removeAttributes(inputAttributes);
  }

  public void insertIcon(Icon g) {
    MutableAttributeSet inputAttributes = getInputAttributes();
    inputAttributes.removeAttributes(inputAttributes);
    StyleConstants.setIcon(inputAttributes, g);
    replaceSelection(" ", false);
    inputAttributes.removeAttributes(inputAttributes);
  }

  public Style addStyle(String nm, Style parent) {
    StyledDocument doc = getStyledDocument();
    return doc.addStyle(nm, parent);
  }

  public void removeStyle(String nm) {
    StyledDocument doc = getStyledDocument();
    doc.removeStyle(nm);
  }

  public Style getStyle(String nm) {
    StyledDocument doc = getStyledDocument();
    return doc.getStyle(nm);
  }

  public void setLogicalStyle(Style s) {
    StyledDocument doc = getStyledDocument();
    doc.setLogicalStyle(getCaretPosition(), s);
  }

  public Style getLogicalStyle() {
    StyledDocument doc = getStyledDocument();
    return doc.getLogicalStyle(getCaretPosition());
  }

  public AttributeSet getCharacterAttributes() {
    StyledDocument doc = getStyledDocument();
    Element run = doc.getCharacterElement(getCaretPosition());
    if (run != null) {
      return run.getAttributes();
    }
    return null;
  }

  public void setCharacterAttributes(AttributeSet attr, boolean replace) {
    int p0 = getSelectionStart();
    int p1 = getSelectionEnd();
    if (p0 != p1) {
      StyledDocument doc = getStyledDocument();
      doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
    } else {
      MutableAttributeSet inputAttributes = getInputAttributes();
      if (replace) {
        inputAttributes.removeAttributes(inputAttributes);
      }
      inputAttributes.addAttributes(attr);
    }
  }

  public AttributeSet getParagraphAttributes() {
    StyledDocument doc = getStyledDocument();
    Element paragraph = doc.getParagraphElement(getCaretPosition());
    if (paragraph != null) {
      return paragraph.getAttributes();
    }
    return null;
  }

  public void setParagraphAttributes(AttributeSet attr, boolean replace) {
    int p0 = getSelectionStart();
    int p1 = getSelectionEnd();
    StyledDocument doc = getStyledDocument();
    doc.setParagraphAttributes(p0, p1 - p0, attr, replace);
  }

  public MutableAttributeSet getInputAttributes() { return getStyledEditorKit().getInputAttributes(); }

  protected final StyledEditorKit getStyledEditorKit() { return (StyledEditorKit) getEditorKit(); }

  protected EditorKit createDefaultEditorKit() { return new StyledEditorKit(); }

  public final void setEditorKit(EditorKit kit) {
    if (kit instanceof StyledEditorKit) {
      super.setEditorKit(kit);
    } else {
      throw new IllegalArgumentException("Must be StyledEditorKit");
    }
  }

  protected String paramString() { return super.paramString(); }
}
