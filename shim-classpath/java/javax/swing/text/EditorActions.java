/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.awt.ComponentOrientation;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.text.BreakIterator;
import javax.swing.UIManager;

final class EditorActions extends TextAction {
  private static final String[] kNames = {
      "insert-content",
      "delete-previous",
      "delete-next",
      "set-read-only",
      "delete-previous-word",
      "delete-next-word",
      "set-writable",
      "cut-to-clipboard",
      "copy-to-clipboard",
      "paste-from-clipboard",
      "page-up",
      "page-down",
      "selection-page-up",
      "selection-page-down",
      "selection-page-left",
      "selection-page-right",
      "insert-break",
      "beep",
      "caret-forward",
      "caret-backward",
      "selection-forward",
      "selection-backward",
      "caret-up",
      "caret-down",
      "selection-up",
      "selection-down",
      "caret-begin-word",
      "caret-end-word",
      "selection-begin-word",
      "selection-end-word",
      "caret-previous-word",
      "caret-next-word",
      "selection-previous-word",
      "selection-next-word",
      "caret-begin-line",
      "caret-begin-line-and-up",
      "caret-end-line",
      "caret-end-line-and-down",
      "selection-begin-line",
      "selection-end-line",
      "caret-begin-paragraph",
      "caret-end-paragraph",
      "selection-begin-paragraph",
      "selection-end-paragraph",
      "caret-begin",
      "caret-end",
      "selection-begin",
      "selection-end",
      "default-typed",
      "insert-tab",
      "select-word",
      "select-line",
      "select-paragraph",
      "select-all",
      "unselect",
      "toggle-componentOrientation",
      "dump-model"};

  private final String kind;

  private EditorActions(String name) {
    super(name);
    this.kind = name;
  }

  static javax.swing.Action[] all() {
    javax.swing.Action[] out = new javax.swing.Action[kNames.length];
    for (int i = 0; i < out.length; i++) {
      out[i] = create(kNames[i]);
    }
    return out;
  }

  static javax.swing.Action create(String name) {
    switch (name) {
      case "insert-content":
        return new DefaultEditorKit.InsertContentAction();
      case "cut-to-clipboard":
        return new DefaultEditorKit.CutAction();
      case "copy-to-clipboard":
        return new DefaultEditorKit.CopyAction();
      case "paste-from-clipboard":
        return new DefaultEditorKit.PasteAction();
      case "insert-break":
        return new DefaultEditorKit.InsertBreakAction();
      case "beep":
        return new DefaultEditorKit.BeepAction();
      case "default-typed":
        return new DefaultEditorKit.DefaultKeyTypedAction();
      case "insert-tab":
        return new DefaultEditorKit.InsertTabAction();
      default:
        return new EditorActions(name);
    }
  }

  static void feedback(JTextComponent target) {
    UIManager.getLookAndFeel().provideErrorFeedback(target);
  }

  private static Element paragraph(Document doc, int pos) {
    if (doc instanceof AbstractDocument) {
      return ((AbstractDocument) doc).getParagraphElement(pos);
    }
    Element root = doc.getDefaultRootElement();
    return root.getElement(root.getElementIndex(pos));
  }

  private static int lineStart(String t, int dot) {
    int i = t.lastIndexOf('\n', dot - 1);
    return i < 0 ? 0 : i + 1;
  }

  private static int lineEnd(String t, int dot) {
    int i = t.indexOf('\n', dot);
    return i < 0 ? t.length() : i;
  }

  private static int wordStart(String t, int off) {
    BreakIterator it = BreakIterator.getWordInstance();
    it.setText(t);
    if (off >= t.length()) {
      return it.last() == BreakIterator.DONE ? off : it.isBoundary(off) ? off : it.preceding(off);
    }
    return it.isBoundary(off) ? off : it.preceding(off);
  }

  private static int wordEnd(String t, int off) {
    BreakIterator it = BreakIterator.getWordInstance();
    it.setText(t);
    int e = it.following(off);
    return e == BreakIterator.DONE ? t.length() : e;
  }

  private static int nextWord(String t, int off) {
    BreakIterator it = BreakIterator.getWordInstance();
    it.setText(t);
    int e = it.following(off);
    while (e != BreakIterator.DONE && e < t.length() && !Character.isLetterOrDigit(t.charAt(e))) {
      e = it.following(e);
    }
    return e == BreakIterator.DONE ? t.length() : e;
  }

  private static int previousWord(String t, int off) {
    BreakIterator it = BreakIterator.getWordInstance();
    it.setText(t);
    int s = it.preceding(off);
    while (s != BreakIterator.DONE && s > 0 && !Character.isLetterOrDigit(t.charAt(s))) {
      s = it.preceding(s);
    }
    return s == BreakIterator.DONE ? 0 : s;
  }

  private static void remove(JTextComponent target, int a, int b) throws BadLocationException {
    if (b > a) {
      target.getDocument().remove(a, b - a);
    }
  }

  public void actionPerformed(ActionEvent e) {
    JTextComponent target = getTextComponent(e);
    if (target == null) {
      return;
    }
    Caret caret = target.getCaret();
    Document doc = target.getDocument();
    int dot = caret.getDot();
    int len = doc.getLength();
    String text;
    try {
      text = doc.getText(0, len);
    } catch (BadLocationException ex) {
      return;
    }
    boolean select = kind.startsWith("selection-");
    int dest = dot;
    try {
      switch (kind) {
        case "delete-previous": {
          boolean beep = true;
          if (target.isEditable()) {
            int mark = caret.getMark();
            if (dot != mark) {
              doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
              beep = false;
            } else if (dot > 0) {
              int delChars = 1;
              if (dot > 1) {
                char c0 = text.charAt(dot - 2);
                char c1 = text.charAt(dot - 1);
                if (c0 >= '\uD800' && c0 <= '\uDBFF' && c1 >= '\uDC00' && c1 <= '\uDFFF') {
                  delChars = 2;
                }
              }
              doc.remove(dot - delChars, delChars);
              beep = false;
            }
          }
          if (beep) {
            feedback(target);
          }
          return;
        }
        case "delete-next": {
          boolean beep = true;
          if (target.isEditable()) {
            int mark = caret.getMark();
            if (dot != mark) {
              doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
              beep = false;
            } else if (dot < len) {
              int delChars = 1;
              if (dot < len - 1) {
                char c0 = text.charAt(dot);
                char c1 = text.charAt(dot + 1);
                if (c0 >= '\uD800' && c0 <= '\uDBFF' && c1 >= '\uDC00' && c1 <= '\uDFFF') {
                  delChars = 2;
                }
              }
              doc.remove(dot, delChars);
              beep = false;
            }
          }
          if (beep) {
            feedback(target);
          }
          return;
        }
        case "delete-previous-word": {
          if (target.isEditable() && target.isEnabled()) {
            int start = previousWord(text, dot);
            if (dot > start) {
              doc.remove(start, dot - start);
              return;
            }
          }
          feedback(target);
          return;
        }
        case "delete-next-word": {
          if (target.isEditable() && target.isEnabled()) {
            int end = nextWord(text, dot);
            if (end > dot) {
              doc.remove(dot, end - dot);
              return;
            }
          }
          feedback(target);
          return;
        }
        case "set-read-only":
          target.setEditable(false);
          return;
        case "set-writable":
          target.setEditable(true);
          return;
        case "page-up":
        case "page-down":
        case "selection-page-up":
        case "selection-page-down":
        case "selection-page-left":
        case "selection-page-right":
          return;
        case "caret-forward":
        case "selection-forward":
          dest = Math.min(len, dot + 1);
          break;
        case "caret-backward":
        case "selection-backward":
          dest = Math.max(0, dot - 1);
          break;
        case "caret-up":
        case "selection-up": {
          int ls = lineStart(text, dot);
          if (ls == 0) {
            dest = 0;
          } else {
            int prevStart = lineStart(text, ls - 1);
            dest = Math.min(prevStart + (dot - ls), ls - 1);
          }
          break;
        }
        case "caret-down":
        case "selection-down": {
          int le = lineEnd(text, dot);
          if (le >= len) {
            dest = len;
          } else {
            int ls = lineStart(text, dot);
            int nextStart = le + 1;
            dest = Math.min(nextStart + (dot - ls), lineEnd(text, nextStart));
          }
          break;
        }
        case "caret-begin-word":
        case "selection-begin-word":
          dest = wordStart(text, dot);
          break;
        case "caret-end-word":
        case "selection-end-word":
          dest = wordEnd(text, dot);
          break;
        case "caret-previous-word":
        case "selection-previous-word":
          dest = previousWord(text, dot);
          break;
        case "caret-next-word":
        case "selection-next-word":
          dest = nextWord(text, dot);
          break;
        case "caret-begin-line":
        case "selection-begin-line":
          dest = lineStart(text, dot);
          break;
        case "caret-end-line":
        case "selection-end-line":
          dest = lineEnd(text, dot);
          break;
        case "caret-begin-line-and-up": {
          int ls = lineStart(text, dot);
          dest = (dot == ls && ls > 0) ? lineStart(text, ls - 1) : ls;
          break;
        }
        case "caret-end-line-and-down": {
          int le = lineEnd(text, dot);
          dest = (dot == le && le < len) ? lineEnd(text, le + 1) : le;
          break;
        }
        case "caret-begin-paragraph":
        case "selection-begin-paragraph":
          dest = paragraph(doc, dot).getStartOffset();
          break;
        case "caret-end-paragraph":
        case "selection-end-paragraph":
          dest = Math.min(paragraph(doc, dot).getEndOffset() - 1, len);
          break;
        case "caret-begin":
        case "selection-begin":
          dest = 0;
          break;
        case "caret-end":
        case "selection-end":
          dest = len;
          break;
        case "select-word": {
          int s = wordStart(text, dot);
          int en = wordEnd(text, dot);
          target.setCaretPosition(s);
          target.moveCaretPosition(en);
          return;
        }
        case "select-line": {
          target.setCaretPosition(lineStart(text, dot));
          target.moveCaretPosition(lineEnd(text, dot));
          return;
        }
        case "select-paragraph": {
          Element elem = paragraph(doc, dot);
          target.setCaretPosition(elem.getStartOffset());
          target.moveCaretPosition(elem.getEndOffset());
          return;
        }
        case "select-all":
          target.setCaretPosition(0);
          target.moveCaretPosition(len);
          return;
        case "unselect":
          caret.setDot(dot);
          return;
        case "toggle-componentOrientation":
          target.setComponentOrientation(target.getComponentOrientation().isLeftToRight()
              ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
          return;
        case "dump-model":
          if (doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).dump(System.err);
          }
          return;
        default:
          return;
      }
    } catch (BadLocationException ex) {
      feedback(target);
      return;
    }
    if (select) {
      caret.moveDot(dest);
    } else {
      caret.setDot(dest);
    }
  }
}
