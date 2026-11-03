/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.swing;

import javax.swing.UIDefaults;

public final class TextBindings {
  private TextBindings() { }

  private static final Object[] kData = {
    "TextField", new String[] {
        "ctrl pressed A", "select-all", "ctrl pressed BACK_SLASH", "unselect",
        "ctrl pressed BACK_SPACE", "delete-previous-word", "ctrl pressed C", "copy-to-clipboard",
        "ctrl pressed DELETE", "delete-next-word", "ctrl pressed H", "delete-previous",
        "ctrl pressed INSERT", "copy-to-clipboard", "ctrl pressed KP_LEFT", "caret-previous-word",
        "ctrl pressed KP_RIGHT", "caret-next-word", "ctrl pressed LEFT", "caret-previous-word",
        "ctrl pressed RIGHT", "caret-next-word", "ctrl pressed V", "paste-from-clipboard",
        "ctrl pressed X", "cut-to-clipboard", "pressed BACK_SPACE", "delete-previous",
        "pressed COPY", "copy-to-clipboard", "pressed CUT", "cut-to-clipboard", "pressed DELETE", "delete-next",
        "pressed END", "caret-end-line", "pressed ENTER", "notify-field-accept", "pressed HOME", "caret-begin-line",
        "pressed KP_LEFT", "caret-backward", "pressed KP_RIGHT", "caret-forward", "pressed LEFT", "caret-backward",
        "pressed PASTE", "paste-from-clipboard", "pressed RIGHT", "caret-forward",
        "shift ctrl pressed KP_LEFT", "selection-previous-word",
        "shift ctrl pressed KP_RIGHT", "selection-next-word", "shift ctrl pressed LEFT", "selection-previous-word",
        "shift ctrl pressed O", "toggle-componentOrientation", "shift ctrl pressed RIGHT", "selection-next-word",
        "shift pressed BACK_SPACE", "delete-previous", "shift pressed DELETE", "cut-to-clipboard",
        "shift pressed END", "selection-end-line", "shift pressed HOME", "selection-begin-line",
        "shift pressed INSERT", "paste-from-clipboard", "shift pressed KP_LEFT", "selection-backward",
        "shift pressed KP_RIGHT", "selection-forward", "shift pressed LEFT", "selection-backward",
        "shift pressed RIGHT", "selection-forward"
    },
    "TextArea", new String[] {
        "ctrl pressed A", "select-all", "ctrl pressed BACK_SLASH", "unselect",
        "ctrl pressed BACK_SPACE", "delete-previous-word", "ctrl pressed C", "copy-to-clipboard",
        "ctrl pressed DELETE", "delete-next-word", "ctrl pressed END", "caret-end",
        "ctrl pressed H", "delete-previous", "ctrl pressed HOME", "caret-begin",
        "ctrl pressed INSERT", "copy-to-clipboard", "ctrl pressed KP_LEFT", "caret-previous-word",
        "ctrl pressed KP_RIGHT", "caret-next-word", "ctrl pressed LEFT", "caret-previous-word",
        "ctrl pressed RIGHT", "caret-next-word", "ctrl pressed SPACE", "activate-link-action",
        "ctrl pressed T", "next-link-action", "ctrl pressed V", "paste-from-clipboard",
        "ctrl pressed X", "cut-to-clipboard", "pressed BACK_SPACE", "delete-previous",
        "pressed COPY", "copy-to-clipboard", "pressed CUT", "cut-to-clipboard", "pressed DELETE", "delete-next",
        "pressed DOWN", "caret-down", "pressed END", "caret-end-line", "pressed ENTER", "insert-break",
        "pressed HOME", "caret-begin-line", "pressed KP_DOWN", "caret-down", "pressed KP_LEFT", "caret-backward",
        "pressed KP_RIGHT", "caret-forward", "pressed KP_UP", "caret-up", "pressed LEFT", "caret-backward",
        "pressed PAGE_DOWN", "page-down", "pressed PAGE_UP", "page-up", "pressed PASTE", "paste-from-clipboard",
        "pressed RIGHT", "caret-forward", "pressed TAB", "insert-tab", "pressed UP", "caret-up",
        "shift ctrl pressed END", "selection-end", "shift ctrl pressed HOME", "selection-begin",
        "shift ctrl pressed KP_LEFT", "selection-previous-word",
        "shift ctrl pressed KP_RIGHT", "selection-next-word", "shift ctrl pressed LEFT", "selection-previous-word",
        "shift ctrl pressed O", "toggle-componentOrientation",
        "shift ctrl pressed PAGE_DOWN", "selection-page-right", "shift ctrl pressed PAGE_UP", "selection-page-left",
        "shift ctrl pressed RIGHT", "selection-next-word", "shift ctrl pressed T", "previous-link-action",
        "shift pressed BACK_SPACE", "delete-previous", "shift pressed DELETE", "cut-to-clipboard",
        "shift pressed DOWN", "selection-down", "shift pressed END", "selection-end-line",
        "shift pressed HOME", "selection-begin-line", "shift pressed INSERT", "paste-from-clipboard",
        "shift pressed KP_DOWN", "selection-down", "shift pressed KP_LEFT", "selection-backward",
        "shift pressed KP_RIGHT", "selection-forward", "shift pressed KP_UP", "selection-up",
        "shift pressed LEFT", "selection-backward", "shift pressed PAGE_DOWN", "selection-page-down",
        "shift pressed PAGE_UP", "selection-page-up", "shift pressed RIGHT", "selection-forward",
        "shift pressed UP", "selection-up"
    },
    "EditorPane", new String[] {
        "ctrl pressed A", "select-all", "ctrl pressed BACK_SLASH", "unselect",
        "ctrl pressed BACK_SPACE", "delete-previous-word", "ctrl pressed C", "copy-to-clipboard",
        "ctrl pressed DELETE", "delete-next-word", "ctrl pressed END", "caret-end",
        "ctrl pressed H", "delete-previous", "ctrl pressed HOME", "caret-begin",
        "ctrl pressed INSERT", "copy-to-clipboard", "ctrl pressed KP_LEFT", "caret-previous-word",
        "ctrl pressed KP_RIGHT", "caret-next-word", "ctrl pressed LEFT", "caret-previous-word",
        "ctrl pressed RIGHT", "caret-next-word", "ctrl pressed SPACE", "activate-link-action",
        "ctrl pressed T", "next-link-action", "ctrl pressed V", "paste-from-clipboard",
        "ctrl pressed X", "cut-to-clipboard", "pressed BACK_SPACE", "delete-previous",
        "pressed COPY", "copy-to-clipboard", "pressed CUT", "cut-to-clipboard", "pressed DELETE", "delete-next",
        "pressed DOWN", "caret-down", "pressed END", "caret-end-line", "pressed ENTER", "insert-break",
        "pressed HOME", "caret-begin-line", "pressed KP_DOWN", "caret-down", "pressed KP_LEFT", "caret-backward",
        "pressed KP_RIGHT", "caret-forward", "pressed KP_UP", "caret-up", "pressed LEFT", "caret-backward",
        "pressed PAGE_DOWN", "page-down", "pressed PAGE_UP", "page-up", "pressed PASTE", "paste-from-clipboard",
        "pressed RIGHT", "caret-forward", "pressed TAB", "insert-tab", "pressed UP", "caret-up",
        "shift ctrl pressed END", "selection-end", "shift ctrl pressed HOME", "selection-begin",
        "shift ctrl pressed KP_LEFT", "selection-previous-word",
        "shift ctrl pressed KP_RIGHT", "selection-next-word", "shift ctrl pressed LEFT", "selection-previous-word",
        "shift ctrl pressed O", "toggle-componentOrientation",
        "shift ctrl pressed PAGE_DOWN", "selection-page-right", "shift ctrl pressed PAGE_UP", "selection-page-left",
        "shift ctrl pressed RIGHT", "selection-next-word", "shift ctrl pressed T", "previous-link-action",
        "shift pressed BACK_SPACE", "delete-previous", "shift pressed DELETE", "cut-to-clipboard",
        "shift pressed DOWN", "selection-down", "shift pressed END", "selection-end-line",
        "shift pressed HOME", "selection-begin-line", "shift pressed INSERT", "paste-from-clipboard",
        "shift pressed KP_DOWN", "selection-down", "shift pressed KP_LEFT", "selection-backward",
        "shift pressed KP_RIGHT", "selection-forward", "shift pressed KP_UP", "selection-up",
        "shift pressed LEFT", "selection-backward", "shift pressed PAGE_DOWN", "selection-page-down",
        "shift pressed PAGE_UP", "selection-page-up", "shift pressed RIGHT", "selection-forward",
        "shift pressed UP", "selection-up"
    },
    "TextPane", new String[] {
        "ctrl pressed A", "select-all", "ctrl pressed BACK_SLASH", "unselect",
        "ctrl pressed BACK_SPACE", "delete-previous-word", "ctrl pressed C", "copy-to-clipboard",
        "ctrl pressed DELETE", "delete-next-word", "ctrl pressed END", "caret-end",
        "ctrl pressed H", "delete-previous", "ctrl pressed HOME", "caret-begin",
        "ctrl pressed INSERT", "copy-to-clipboard", "ctrl pressed KP_LEFT", "caret-previous-word",
        "ctrl pressed KP_RIGHT", "caret-next-word", "ctrl pressed LEFT", "caret-previous-word",
        "ctrl pressed RIGHT", "caret-next-word", "ctrl pressed SPACE", "activate-link-action",
        "ctrl pressed T", "next-link-action", "ctrl pressed V", "paste-from-clipboard",
        "ctrl pressed X", "cut-to-clipboard", "pressed BACK_SPACE", "delete-previous",
        "pressed COPY", "copy-to-clipboard", "pressed CUT", "cut-to-clipboard", "pressed DELETE", "delete-next",
        "pressed DOWN", "caret-down", "pressed END", "caret-end-line", "pressed ENTER", "insert-break",
        "pressed HOME", "caret-begin-line", "pressed KP_DOWN", "caret-down", "pressed KP_LEFT", "caret-backward",
        "pressed KP_RIGHT", "caret-forward", "pressed KP_UP", "caret-up", "pressed LEFT", "caret-backward",
        "pressed PAGE_DOWN", "page-down", "pressed PAGE_UP", "page-up", "pressed PASTE", "paste-from-clipboard",
        "pressed RIGHT", "caret-forward", "pressed TAB", "insert-tab", "pressed UP", "caret-up",
        "shift ctrl pressed END", "selection-end", "shift ctrl pressed HOME", "selection-begin",
        "shift ctrl pressed KP_LEFT", "selection-previous-word",
        "shift ctrl pressed KP_RIGHT", "selection-next-word", "shift ctrl pressed LEFT", "selection-previous-word",
        "shift ctrl pressed O", "toggle-componentOrientation",
        "shift ctrl pressed PAGE_DOWN", "selection-page-right", "shift ctrl pressed PAGE_UP", "selection-page-left",
        "shift ctrl pressed RIGHT", "selection-next-word", "shift ctrl pressed T", "previous-link-action",
        "shift pressed BACK_SPACE", "delete-previous", "shift pressed DELETE", "cut-to-clipboard",
        "shift pressed DOWN", "selection-down", "shift pressed END", "selection-end-line",
        "shift pressed HOME", "selection-begin-line", "shift pressed INSERT", "paste-from-clipboard",
        "shift pressed KP_DOWN", "selection-down", "shift pressed KP_LEFT", "selection-backward",
        "shift pressed KP_RIGHT", "selection-forward", "shift pressed KP_UP", "selection-up",
        "shift pressed LEFT", "selection-backward", "shift pressed PAGE_DOWN", "selection-page-down",
        "shift pressed PAGE_UP", "selection-page-up", "shift pressed RIGHT", "selection-forward",
        "shift pressed UP", "selection-up"
    },
    "PasswordField", new String[] {
        "ctrl pressed A", "select-all", "ctrl pressed BACK_SLASH", "unselect", "ctrl pressed C", "copy-to-clipboard",
        "ctrl pressed H", "delete-previous", "ctrl pressed INSERT", "copy-to-clipboard",
        "ctrl pressed KP_LEFT", "caret-begin-line", "ctrl pressed KP_RIGHT", "caret-end-line",
        "ctrl pressed LEFT", "caret-begin-line", "ctrl pressed RIGHT", "caret-end-line",
        "ctrl pressed V", "paste-from-clipboard", "ctrl pressed X", "cut-to-clipboard",
        "pressed BACK_SPACE", "delete-previous", "pressed COPY", "copy-to-clipboard",
        "pressed CUT", "cut-to-clipboard", "pressed DELETE", "delete-next", "pressed END", "caret-end-line",
        "pressed ENTER", "notify-field-accept", "pressed HOME", "caret-begin-line",
        "pressed KP_LEFT", "caret-backward", "pressed KP_RIGHT", "caret-forward", "pressed LEFT", "caret-backward",
        "pressed PASTE", "paste-from-clipboard", "pressed RIGHT", "caret-forward",
        "shift ctrl pressed KP_LEFT", "selection-begin-line", "shift ctrl pressed KP_RIGHT", "selection-end-line",
        "shift ctrl pressed LEFT", "selection-begin-line", "shift ctrl pressed O", "toggle-componentOrientation",
        "shift ctrl pressed RIGHT", "selection-end-line", "shift pressed BACK_SPACE", "delete-previous",
        "shift pressed DELETE", "cut-to-clipboard", "shift pressed END", "selection-end-line",
        "shift pressed HOME", "selection-begin-line", "shift pressed INSERT", "paste-from-clipboard",
        "shift pressed KP_LEFT", "selection-backward", "shift pressed KP_RIGHT", "selection-forward",
        "shift pressed LEFT", "selection-backward", "shift pressed RIGHT", "selection-forward"
    },
    "FormattedTextField", new String[] {
        "ctrl pressed A", "select-all", "ctrl pressed BACK_SLASH", "unselect",
        "ctrl pressed BACK_SPACE", "delete-previous-word", "ctrl pressed C", "copy-to-clipboard",
        "ctrl pressed DELETE", "delete-next-word", "ctrl pressed H", "delete-previous",
        "ctrl pressed INSERT", "copy-to-clipboard", "ctrl pressed KP_LEFT", "caret-previous-word",
        "ctrl pressed KP_RIGHT", "caret-next-word", "ctrl pressed LEFT", "caret-previous-word",
        "ctrl pressed RIGHT", "caret-next-word", "ctrl pressed V", "paste-from-clipboard",
        "ctrl pressed X", "cut-to-clipboard", "pressed BACK_SPACE", "delete-previous",
        "pressed COPY", "copy-to-clipboard", "pressed CUT", "cut-to-clipboard", "pressed DELETE", "delete-next",
        "pressed DOWN", "decrement", "pressed END", "caret-end-line", "pressed ENTER", "notify-field-accept",
        "pressed ESCAPE", "reset-field-edit", "pressed HOME", "caret-begin-line", "pressed KP_DOWN", "decrement",
        "pressed KP_LEFT", "caret-backward", "pressed KP_RIGHT", "caret-forward", "pressed KP_UP", "increment",
        "pressed LEFT", "caret-backward", "pressed PASTE", "paste-from-clipboard", "pressed RIGHT", "caret-forward",
        "pressed UP", "increment", "shift ctrl pressed KP_LEFT", "selection-previous-word",
        "shift ctrl pressed KP_RIGHT", "selection-next-word", "shift ctrl pressed LEFT", "selection-previous-word",
        "shift ctrl pressed O", "toggle-componentOrientation", "shift ctrl pressed RIGHT", "selection-next-word",
        "shift pressed BACK_SPACE", "delete-previous", "shift pressed DELETE", "cut-to-clipboard",
        "shift pressed END", "selection-end-line", "shift pressed HOME", "selection-begin-line",
        "shift pressed INSERT", "paste-from-clipboard", "shift pressed KP_LEFT", "selection-backward",
        "shift pressed KP_RIGHT", "selection-forward", "shift pressed LEFT", "selection-backward",
        "shift pressed RIGHT", "selection-forward"
    },
  };

  public static void fill(UIDefaults t) {
    for (int i = 0; i < kData.length; i += 2) {
      t.put((String) kData[i] + ".focusInputMap", new UIDefaults.LazyInputMap((Object[]) kData[i + 1]));
    }
  }
}
