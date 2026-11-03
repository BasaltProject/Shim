/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public class DefaultEditorKit extends javax.swing.text.EditorKit {
  public static final java.lang.String EndOfLineStringProperty = "__EndOfLine__";
  public static final java.lang.String insertContentAction = "insert-content";
  public static final java.lang.String insertBreakAction = "insert-break";
  public static final java.lang.String insertTabAction = "insert-tab";
  public static final java.lang.String deletePrevCharAction = "delete-previous";
  public static final java.lang.String deleteNextCharAction = "delete-next";
  public static final java.lang.String deleteNextWordAction = "delete-next-word";
  public static final java.lang.String deletePrevWordAction = "delete-previous-word";
  public static final java.lang.String readOnlyAction = "set-read-only";
  public static final java.lang.String writableAction = "set-writable";
  public static final java.lang.String cutAction = "cut-to-clipboard";
  public static final java.lang.String copyAction = "copy-to-clipboard";
  public static final java.lang.String pasteAction = "paste-from-clipboard";
  public static final java.lang.String beepAction = "beep";
  public static final java.lang.String pageUpAction = "page-up";
  public static final java.lang.String pageDownAction = "page-down";
  public static final java.lang.String forwardAction = "caret-forward";
  public static final java.lang.String backwardAction = "caret-backward";
  public static final java.lang.String selectionForwardAction = "selection-forward";
  public static final java.lang.String selectionBackwardAction = "selection-backward";
  public static final java.lang.String upAction = "caret-up";
  public static final java.lang.String downAction = "caret-down";
  public static final java.lang.String selectionUpAction = "selection-up";
  public static final java.lang.String selectionDownAction = "selection-down";
  public static final java.lang.String beginWordAction = "caret-begin-word";
  public static final java.lang.String endWordAction = "caret-end-word";
  public static final java.lang.String selectionBeginWordAction = "selection-begin-word";
  public static final java.lang.String selectionEndWordAction = "selection-end-word";
  public static final java.lang.String previousWordAction = "caret-previous-word";
  public static final java.lang.String nextWordAction = "caret-next-word";
  public static final java.lang.String selectionPreviousWordAction = "selection-previous-word";
  public static final java.lang.String selectionNextWordAction = "selection-next-word";
  public static final java.lang.String beginLineAction = "caret-begin-line";
  public static final java.lang.String endLineAction = "caret-end-line";
  public static final java.lang.String beginLineUpAction = "caret-begin-line-and-up";
  public static final java.lang.String endLineDownAction = "caret-end-line-and-down";
  public static final java.lang.String selectionBeginLineAction = "selection-begin-line";
  public static final java.lang.String selectionEndLineAction = "selection-end-line";
  public static final java.lang.String beginParagraphAction = "caret-begin-paragraph";
  public static final java.lang.String endParagraphAction = "caret-end-paragraph";
  public static final java.lang.String selectionBeginParagraphAction = "selection-begin-paragraph";
  public static final java.lang.String selectionEndParagraphAction = "selection-end-paragraph";
  public static final java.lang.String beginAction = "caret-begin";
  public static final java.lang.String endAction = "caret-end";
  public static final java.lang.String selectionBeginAction = "selection-begin";
  public static final java.lang.String selectionEndAction = "selection-end";
  public static final java.lang.String selectWordAction = "select-word";
  public static final java.lang.String selectLineAction = "select-line";
  public static final java.lang.String selectParagraphAction = "select-paragraph";
  public static final java.lang.String selectAllAction = "select-all";
  public static final java.lang.String defaultKeyTypedAction = "default-typed";
  public DefaultEditorKit() { }

  public String getContentType() { return "text/plain"; }

  public ViewFactory getViewFactory() { return null; }

  public javax.swing.Action[] getActions() { return EditorActions.all(); }

  public Caret createCaret() { return null; }

  public Document createDefaultDocument() { return new PlainDocument(); }

  public void read(java.io.InputStream in, Document doc, int pos) throws java.io.IOException, BadLocationException {
    read(new java.io.InputStreamReader(in), doc, pos);
  }

  public void write(java.io.OutputStream out, Document doc, int pos, int len)
      throws java.io.IOException, BadLocationException {
    java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(out);
    write(osw, doc, pos, len);
    osw.flush();
  }

  public void read(java.io.Reader in, Document doc, int pos) throws java.io.IOException, BadLocationException {
    StringBuilder all = new StringBuilder();
    char[] buff = new char[4096];
    int n;
    boolean isCRLF = false;
    boolean isCR = false;
    boolean lastWasCR = false;
    boolean wasEmpty = (doc.getLength() == 0);
    while ((n = in.read(buff, 0, buff.length)) != -1) {
      for (int i = 0; i < n; i++) {
        char c = buff[i];
        if (c == '\r') {
          if (lastWasCR) {
            isCR = true;
            all.append('\n');
          }
          lastWasCR = true;
        } else if (c == '\n') {
          if (lastWasCR) {
            isCRLF = true;
            lastWasCR = false;
          }
          all.append('\n');
        } else {
          if (lastWasCR) {
            isCR = true;
            all.append('\n');
            lastWasCR = false;
          }
          all.append(c);
        }
      }
    }
    if (lastWasCR) {
      all.append('\n');
      isCR = true;
    }
    if (all.length() > 0) {
      doc.insertString(pos, all.toString(), null);
    }
    if (wasEmpty) {
      if (isCRLF) {
        doc.putProperty(EndOfLineStringProperty, "\r\n");
      } else if (isCR) {
        doc.putProperty(EndOfLineStringProperty, "\r");
      } else {
        doc.putProperty(EndOfLineStringProperty, "\n");
      }
    }
  }

  public void write(java.io.Writer out, Document doc, int pos, int len)
      throws java.io.IOException, BadLocationException {
    if ((pos < 0) || ((pos + len) > doc.getLength())) {
      throw new BadLocationException("DefaultEditorKit.write", pos);
    }
    Object eolProperty = doc.getProperty(EndOfLineStringProperty);
    if (eolProperty == null) {
      eolProperty = System.getProperty("line.separator");
    }
    String eol = eolProperty instanceof String ? (String) eolProperty : null;
    String data = doc.getText(pos, len);
    if (eol != null && !eol.equals("\n")) {
      out.write(data.replace("\n", eol));
    } else {
      out.write(data);
    }
    out.flush();
  }

  public static class InsertContentAction extends TextAction {
    public InsertContentAction() { super(insertContentAction); }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      JTextComponent target = getTextComponent(e);
      if ((target != null) && (e != null)) {
        if ((!target.isEditable()) || (!target.isEnabled())) {
          EditorActions.feedback(target);
          return;
        }
        String content = e.getActionCommand();
        if (content != null) {
          target.replaceSelection(content);
        } else {
          EditorActions.feedback(target);
        }
      }
    }
  }

  public static class CutAction extends TextAction {
    public CutAction() { super(cutAction); }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      JTextComponent target = getTextComponent(e);
      if (target != null) {
        target.cut();
      }
    }
  }

  public static class CopyAction extends TextAction {
    public CopyAction() { super(copyAction); }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      JTextComponent target = getTextComponent(e);
      if (target != null) {
        target.copy();
      }
    }
  }

  public static class PasteAction extends TextAction {
    public PasteAction() { super(pasteAction); }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      JTextComponent target = getTextComponent(e);
      if (target != null) {
        target.paste();
      }
    }
  }

  public static class InsertBreakAction extends TextAction {
    public InsertBreakAction() { super(insertBreakAction); }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      JTextComponent target = getTextComponent(e);
      if (target != null) {
        if ((!target.isEditable()) || (!target.isEnabled())) {
          EditorActions.feedback(target);
          return;
        }
        target.replaceSelection("\n");
      }
    }
  }

  public static class BeepAction extends TextAction {
    public BeepAction() { super(beepAction); }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      javax.swing.UIManager.getLookAndFeel().provideErrorFeedback(getTextComponent(e));
    }
  }

  public static class DefaultKeyTypedAction extends TextAction {
    public DefaultKeyTypedAction() { super(defaultKeyTypedAction); }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      JTextComponent target = getTextComponent(e);
      if ((target != null) && (e != null)) {
        if ((!target.isEditable()) || (!target.isEnabled())) {
          return;
        }
        String content = e.getActionCommand();
        int mod = e.getModifiers();
        if ((content != null) && (content.length() > 0)) {
          boolean alt = (mod & java.awt.event.ActionEvent.ALT_MASK) != 0;
          boolean ctrl = (mod & java.awt.event.ActionEvent.CTRL_MASK) != 0;
          boolean isPrintableMask = !(alt ^ ctrl);
          if (isPrintableMask) {
            char c = content.charAt(0);
            if ((c >= 0x20) && (c != 0x7F)) {
              target.replaceSelection(content);
            }
          }
        }
      }
    }
  }

  public static class InsertTabAction extends TextAction {
    public InsertTabAction() { super(insertTabAction); }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      JTextComponent target = getTextComponent(e);
      if (target != null) {
        if ((!target.isEditable()) || (!target.isEnabled())) {
          EditorActions.feedback(target);
          return;
        }
        target.replaceSelection("\t");
      }
    }
  }
}
