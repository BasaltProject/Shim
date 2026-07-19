/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package shim.swing;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.plaf.ActionMapUIResource;

public final class ListKeyActions {
  private ListKeyActions() { }

  public static ActionMap create() {
    ActionMap map = new ActionMapUIResource();
    for (String base : new String[] {"selectPreviousRow", "selectNextRow", "selectPreviousColumn", "selectNextColumn",
        "selectFirstRow", "selectLastRow", "scrollUp", "scrollDown"}) {
      map.put(base, new Act(base));
      map.put(base + "ExtendSelection", new Act(base + "ExtendSelection"));
      map.put(base + "ChangeLead", new Act(base + "ChangeLead"));
    }
    for (String n : new String[] {"selectAll", "clearSelection", "addToSelection", "toggleAndAnchor", "extendTo",
        "moveSelectionTo"}) {
      map.put(n, new Act(n));
    }
    map.put("copy", TransferHandler.getCopyAction());
    map.put("cut", TransferHandler.getCutAction());
    map.put("paste", TransferHandler.getPasteAction());
    return map;
  }

  private static void moveLead(ListSelectionModel sm, int index) {
    if (sm instanceof javax.swing.DefaultListSelectionModel) {
      ((javax.swing.DefaultListSelectionModel) sm).moveLeadSelectionIndex(index);
    }
  }

  private static final class Act extends AbstractAction {
    private final String name;

    Act(String name) {
      super(name);
      this.name = name;
    }

    public boolean isEnabled() {
      return true;
    }

    private int step(JList<?> list, int lead) {
      int size = list.getModel().getSize();
      boolean next = name.startsWith("selectNext");
      if (name.startsWith("selectFirst")) {
        return 0;
      }
      if (name.startsWith("selectLast")) {
        return size - 1;
      }
      if (name.contains("Column") && list.getLayoutOrientation() == JList.VERTICAL) {
        return -1;
      }
      if (name.startsWith("scroll")) {
        int first = list.getFirstVisibleIndex();
        int last = list.getLastVisibleIndex();
        if (first < 0 || last < 0) {
          return -1;
        }
        int page = Math.max(1, last - first + 1);
        if (name.startsWith("scrollUp")) {
          return lead != first ? first : Math.max(0, first - page);
        }
        return lead != last ? last : Math.min(size - 1, last + page);
      }
      if (lead < 0) {
        return size > 0 ? (next ? 0 : size - 1) : -1;
      }
      int target = lead + (next ? 1 : -1);
      return target < 0 || target >= size ? -1 : target;
    }

    public void actionPerformed(ActionEvent e) {
      JList<?> list = (JList<?>) e.getSource();
      ListSelectionModel sm = list.getSelectionModel();
      int size = list.getModel().getSize();
      int lead = sm.getLeadSelectionIndex();
      switch (name) {
        case "selectAll":
          if (size > 0 && list.getSelectionMode() != ListSelectionModel.SINGLE_SELECTION) {
            int anchor = sm.getAnchorSelectionIndex();
            list.setSelectionInterval(0, size - 1);
            sm.setAnchorSelectionIndex(anchor);
            moveLead(sm, lead);
          }
          return;
        case "clearSelection":
          list.clearSelection();
          return;
        case "addToSelection":
          if (lead >= 0 && !list.isSelectedIndex(lead)) {
            int anchor = sm.getAnchorSelectionIndex();
            list.addSelectionInterval(lead, lead);
            sm.setAnchorSelectionIndex(anchor);
          }
          return;
        case "toggleAndAnchor":
          if (lead >= 0) {
            if (list.isSelectedIndex(lead)) {
              list.removeSelectionInterval(lead, lead);
            } else {
              list.addSelectionInterval(lead, lead);
            }
          }
          return;
        case "extendTo":
          change(list, 1, lead);
          return;
        case "moveSelectionTo":
          if (lead >= 0) {
            list.setSelectionInterval(lead, lead);
          }
          return;
        default:
          break;
      }
      int target = step(list, lead);
      if (target < 0) {
        return;
      }
      int type = name.endsWith("ExtendSelection") ? 1 : name.endsWith("ChangeLead") ? 2 : 0;
      change(list, type, target);
    }

    private void change(JList<?> list, int type, int index) {
      ListSelectionModel sm = list.getSelectionModel();
      if (index < 0 || index >= list.getModel().getSize()) {
        return;
      }
      if (type == 1 && list.getSelectionMode() != ListSelectionModel.SINGLE_SELECTION) {
        int anchor = sm.getAnchorSelectionIndex();
        if (anchor == -1) {
          anchor = 0;
        }
        list.setSelectionInterval(anchor, index);
        sm.setAnchorSelectionIndex(anchor);
        moveLead(sm, index);
      } else if (type == 2) {
        moveLead(sm, index);
      } else {
        list.setSelectedIndex(index);
      }
      list.ensureIndexIsVisible(index);
    }
  }
}
