/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package shim.swing;

import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.plaf.InputMapUIResource;

public final class MetalKeymaps {
  private MetalKeymaps() { }

  private static final String[] kRows = {
      "Button.focusInputMap|SPACE=pressed;released SPACE=released;",
      "CheckBox.focusInputMap|SPACE=pressed;released SPACE=released;",
      "ComboBox.ancestorInputMap|alt DOWN=togglePopup;alt KP_DOWN=togglePopup;alt KP_UP=togglePopup;alt UP=togglePopup;DOWN=selectNext;END=endPassThrough;ENTER=enterPressed;ESCAPE=hidePopup;HOME=homePassThrough;KP_DOWN=selectNext;KP_UP=selectPrevious;PAGE_DOWN=pageDownPassThrough;PAGE_UP=pageUpPassThrough;SPACE=spacePopup;UP=selectPrevious;",
      "Desktop.ancestorInputMap|ctrl alt F6=selectNextFrame;ctrl F10=maximize;ctrl F12=navigateNext;ctrl F4=close;ctrl F5=restore;ctrl F6=selectNextFrame;ctrl F7=move;ctrl F8=resize;ctrl F9=minimize;ctrl TAB=selectNextFrame;DOWN=down;ESCAPE=escape;KP_DOWN=down;KP_LEFT=left;KP_RIGHT=right;KP_UP=up;LEFT=left;RIGHT=right;UP=up;shift ctrl alt F6=selectPreviousFrame;shift ctrl F12=navigatePrevious;shift DOWN=shrinkDown;shift KP_DOWN=shrinkDown;shift KP_LEFT=shrinkLeft;shift KP_RIGHT=shrinkRight;shift KP_UP=shrinkUp;shift LEFT=shrinkLeft;shift RIGHT=shrinkRight;shift UP=shrinkUp;",
      "FileChooser.ancestorInputMap|BACK_SPACE=Go Up;ESCAPE=cancelSelection;F2=editFileName;F5=refresh;",
      "List.focusInputMap|ctrl A=selectAll;ctrl BACK_SLASH=clearSelection;ctrl C=copy;ctrl DOWN=selectNextRowChangeLead;ctrl END=selectLastRowChangeLead;ctrl HOME=selectFirstRowChangeLead;ctrl INSERT=copy;ctrl KP_DOWN=selectNextRowChangeLead;ctrl KP_LEFT=selectPreviousColumnChangeLead;ctrl KP_RIGHT=selectNextColumnChangeLead;ctrl KP_UP=selectPreviousRowChangeLead;ctrl LEFT=selectPreviousColumnChangeLead;ctrl PAGE_DOWN=scrollDownChangeLead;ctrl PAGE_UP=scrollUpChangeLead;ctrl RIGHT=selectNextColumnChangeLead;ctrl SLASH=selectAll;ctrl SPACE=toggleAndAnchor;ctrl UP=selectPreviousRowChangeLead;ctrl V=paste;ctrl X=cut;COPY=copy;CUT=cut;DOWN=selectNextRow;END=selectLastRow;HOME=selectFirstRow;KP_DOWN=selectNextRow;KP_LEFT=selectPreviousColumn;KP_RIGHT=selectNextColumn;KP_UP=selectPreviousRow;LEFT=selectPreviousColumn;PAGE_DOWN=scrollDown;PAGE_UP=scrollUp;PASTE=paste;RIGHT=selectNextColumn;SPACE=addToSelection;UP=selectPreviousRow;shift ctrl DOWN=selectNextRowExtendSelection;shift ctrl END=selectLastRowExtendSelection;shift ctrl HOME=selectFirstRowExtendSelection;shift ctrl KP_DOWN=selectNextRowExtendSelection;shift ctrl KP_LEFT=selectPreviousColumnExtendSelection;shift ctrl KP_RIGHT=selectNextColumnExtendSelection;shift ctrl KP_UP=selectPreviousRowExtendSelection;shift ctrl LEFT=selectPreviousColumnExtendSelection;shift ctrl PAGE_DOWN=scrollDownExtendSelection;shift ctrl PAGE_UP=scrollUpExtendSelection;shift ctrl RIGHT=selectNextColumnExtendSelection;shift ctrl SPACE=moveSelectionTo;shift ctrl UP=selectPreviousRowExtendSelection;shift DELETE=cut;shift DOWN=selectNextRowExtendSelection;shift END=selectLastRowExtendSelection;shift HOME=selectFirstRowExtendSelection;shift INSERT=paste;shift KP_DOWN=selectNextRowExtendSelection;shift KP_LEFT=selectPreviousColumnExtendSelection;shift KP_RIGHT=selectNextColumnExtendSelection;shift KP_UP=selectPreviousRowExtendSelection;shift LEFT=selectPreviousColumnExtendSelection;shift PAGE_DOWN=scrollDownExtendSelection;shift PAGE_UP=scrollUpExtendSelection;shift RIGHT=selectNextColumnExtendSelection;shift SPACE=extendTo;shift UP=selectPreviousRowExtendSelection;",
      "List.focusInputMap.RightToLeft|ctrl KP_LEFT=selectNextColumnChangeLead;ctrl KP_RIGHT=selectPreviousColumnChangeLead;ctrl LEFT=selectNextColumnChangeLead;ctrl RIGHT=selectPreviousColumnChangeLead;KP_LEFT=selectNextColumn;KP_RIGHT=selectPreviousColumn;LEFT=selectNextColumn;RIGHT=selectPreviousColumn;shift ctrl KP_LEFT=selectNextColumnExtendSelection;shift ctrl KP_RIGHT=selectPreviousColumnExtendSelection;shift ctrl LEFT=selectNextColumnExtendSelection;shift ctrl RIGHT=selectPreviousColumnExtendSelection;shift KP_LEFT=selectNextColumnExtendSelection;shift KP_RIGHT=selectPreviousColumnExtendSelection;shift LEFT=selectNextColumnExtendSelection;shift RIGHT=selectPreviousColumnExtendSelection;",
      "RadioButton.focusInputMap|SPACE=pressed;released SPACE=released;",
      "RootPane.ancestorInputMap|CONTEXT_MENU=postPopup;shift F10=postPopup;",
      "ScrollBar.ancestorInputMap|DOWN=positiveUnitIncrement;END=maxScroll;HOME=minScroll;KP_DOWN=positiveUnitIncrement;KP_LEFT=negativeUnitIncrement;KP_RIGHT=positiveUnitIncrement;KP_UP=negativeUnitIncrement;LEFT=negativeUnitIncrement;PAGE_DOWN=positiveBlockIncrement;PAGE_UP=negativeBlockIncrement;RIGHT=positiveUnitIncrement;UP=negativeUnitIncrement;",
      "ScrollBar.ancestorInputMap.RightToLeft|KP_LEFT=positiveUnitIncrement;KP_RIGHT=negativeUnitIncrement;LEFT=positiveUnitIncrement;RIGHT=negativeUnitIncrement;",
      "ScrollPane.ancestorInputMap|ctrl END=scrollEnd;ctrl HOME=scrollHome;ctrl PAGE_DOWN=scrollRight;ctrl PAGE_UP=scrollLeft;DOWN=unitScrollDown;KP_DOWN=unitScrollDown;KP_LEFT=unitScrollLeft;KP_RIGHT=unitScrollRight;KP_UP=unitScrollUp;LEFT=unitScrollLeft;PAGE_DOWN=scrollDown;PAGE_UP=scrollUp;RIGHT=unitScrollRight;UP=unitScrollUp;",
      "ScrollPane.ancestorInputMap.RightToLeft|ctrl PAGE_DOWN=scrollLeft;ctrl PAGE_UP=scrollRight;",
      "Slider.focusInputMap|ctrl PAGE_DOWN=negativeBlockIncrement;ctrl PAGE_UP=positiveBlockIncrement;DOWN=negativeUnitIncrement;END=maxScroll;HOME=minScroll;KP_DOWN=negativeUnitIncrement;KP_LEFT=negativeUnitIncrement;KP_RIGHT=positiveUnitIncrement;KP_UP=positiveUnitIncrement;LEFT=negativeUnitIncrement;PAGE_DOWN=negativeBlockIncrement;PAGE_UP=positiveBlockIncrement;RIGHT=positiveUnitIncrement;UP=positiveUnitIncrement;",
      "Slider.focusInputMap.RightToLeft|KP_LEFT=positiveUnitIncrement;KP_RIGHT=negativeUnitIncrement;LEFT=positiveUnitIncrement;RIGHT=negativeUnitIncrement;",
      "Spinner.ancestorInputMap|DOWN=decrement;KP_DOWN=decrement;KP_UP=increment;UP=increment;",
      "SplitPane.ancestorInputMap|ctrl TAB=focusOutForward;DOWN=positiveIncrement;END=selectMax;F6=toggleFocus;F8=startResize;HOME=selectMin;KP_DOWN=positiveIncrement;KP_LEFT=negativeIncrement;KP_RIGHT=positiveIncrement;KP_UP=negativeIncrement;LEFT=negativeIncrement;RIGHT=positiveIncrement;UP=negativeIncrement;shift ctrl TAB=focusOutBackward;",
      "TabbedPane.ancestorInputMap|ctrl KP_UP=requestFocus;ctrl PAGE_DOWN=navigatePageDown;ctrl PAGE_UP=navigatePageUp;ctrl UP=requestFocus;",
      "TabbedPane.focusInputMap|ctrl DOWN=requestFocusForVisibleComponent;ctrl KP_DOWN=requestFocusForVisibleComponent;DOWN=navigateDown;KP_DOWN=navigateDown;KP_LEFT=navigateLeft;KP_RIGHT=navigateRight;KP_UP=navigateUp;LEFT=navigateLeft;RIGHT=navigateRight;UP=navigateUp;",
      "Table.ancestorInputMap|ctrl A=selectAll;ctrl BACK_SLASH=clearSelection;ctrl C=copy;ctrl DOWN=selectNextRowChangeLead;ctrl END=selectLastRow;ctrl HOME=selectFirstRow;ctrl INSERT=copy;ctrl KP_DOWN=selectNextRowChangeLead;ctrl KP_LEFT=selectPreviousColumnChangeLead;ctrl KP_RIGHT=selectNextColumnChangeLead;ctrl KP_UP=selectPreviousRowChangeLead;ctrl LEFT=selectPreviousColumnChangeLead;ctrl PAGE_DOWN=scrollRightChangeSelection;ctrl PAGE_UP=scrollLeftChangeSelection;ctrl RIGHT=selectNextColumnChangeLead;ctrl SLASH=selectAll;ctrl SPACE=toggleAndAnchor;ctrl UP=selectPreviousRowChangeLead;ctrl V=paste;ctrl X=cut;COPY=copy;CUT=cut;DOWN=selectNextRow;END=selectLastColumn;ENTER=selectNextRowCell;ESCAPE=cancel;F2=startEditing;F8=focusHeader;HOME=selectFirstColumn;KP_DOWN=selectNextRow;KP_LEFT=selectPreviousColumn;KP_RIGHT=selectNextColumn;KP_UP=selectPreviousRow;LEFT=selectPreviousColumn;PAGE_DOWN=scrollDownChangeSelection;PAGE_UP=scrollUpChangeSelection;PASTE=paste;RIGHT=selectNextColumn;SPACE=addToSelection;TAB=selectNextColumnCell;UP=selectPreviousRow;shift ctrl DOWN=selectLastRowExtendSelection;shift ctrl END=selectLastRowExtendSelection;shift ctrl HOME=selectFirstRowExtendSelection;shift ctrl KP_DOWN=selectNextRowExtendSelection;shift ctrl KP_LEFT=selectPreviousColumnExtendSelection;shift ctrl KP_RIGHT=selectNextColumnExtendSelection;shift ctrl KP_UP=selectPreviousRowExtendSelection;shift ctrl LEFT=selectPreviousColumnExtendSelection;shift ctrl PAGE_DOWN=scrollRightExtendSelection;shift ctrl PAGE_UP=scrollLeftExtendSelection;shift ctrl RIGHT=selectNextColumnExtendSelection;shift ctrl SPACE=moveSelectionTo;shift ctrl UP=selectFirstRowExtendSelection;shift DELETE=cut;shift DOWN=selectNextRowExtendSelection;shift END=selectLastColumnExtendSelection;shift ENTER=selectPreviousRowCell;shift HOME=selectFirstColumnExtendSelection;shift INSERT=paste;shift KP_DOWN=selectNextRowExtendSelection;shift KP_LEFT=selectPreviousColumnExtendSelection;shift KP_RIGHT=selectNextColumnExtendSelection;shift KP_UP=selectPreviousRowExtendSelection;shift LEFT=selectPreviousColumnExtendSelection;shift PAGE_DOWN=scrollDownExtendSelection;shift PAGE_UP=scrollUpExtendSelection;shift RIGHT=selectNextColumnExtendSelection;shift SPACE=extendTo;shift TAB=selectPreviousColumnCell;shift UP=selectPreviousRowExtendSelection;",
      "Table.ancestorInputMap.RightToLeft|ctrl KP_LEFT=selectNextColumnChangeLead;ctrl KP_RIGHT=selectPreviousColumnChangeLead;ctrl LEFT=selectNextColumnChangeLead;ctrl PAGE_DOWN=scrollLeftChangeSelection;ctrl PAGE_UP=scrollRightChangeSelection;ctrl RIGHT=selectPreviousColumnChangeLead;KP_LEFT=selectNextColumn;KP_RIGHT=selectPreviousColumn;LEFT=selectNextColumn;RIGHT=selectPreviousColumn;shift ctrl KP_LEFT=selectNextColumnExtendSelection;shift ctrl KP_RIGHT=selectPreviousColumnExtendSelection;shift ctrl LEFT=selectNextColumnExtendSelection;shift ctrl PAGE_DOWN=scrollLeftExtendSelection;shift ctrl PAGE_UP=scrollRightExtendSelection;shift ctrl RIGHT=selectPreviousColumnExtendSelection;shift KP_LEFT=selectNextColumnExtendSelection;shift KP_RIGHT=selectPreviousColumnExtendSelection;shift LEFT=selectNextColumnExtendSelection;shift RIGHT=selectPreviousColumnExtendSelection;",
      "TableHeader.ancestorInputMap|alt KP_LEFT=moveColumnLeft;alt KP_RIGHT=moveColumnRight;alt LEFT=moveColumnLeft;alt RIGHT=moveColumnRight;ESCAPE=focusTable;KP_LEFT=selectColumnToLeft;KP_RIGHT=selectColumnToRight;LEFT=selectColumnToLeft;RIGHT=selectColumnToRight;SPACE=toggleSortOrder;shift alt KP_LEFT=resizeLeft;shift alt KP_RIGHT=resizeRight;shift alt LEFT=resizeLeft;shift alt RIGHT=resizeRight;",
      "ToggleButton.focusInputMap|SPACE=pressed;released SPACE=released;",
      "ToolBar.ancestorInputMap|DOWN=navigateDown;KP_DOWN=navigateDown;KP_LEFT=navigateLeft;KP_RIGHT=navigateRight;KP_UP=navigateUp;LEFT=navigateLeft;RIGHT=navigateRight;UP=navigateUp;",
      "Tree.ancestorInputMap|ESCAPE=cancel;",
      "Tree.focusInputMap|ctrl A=selectAll;ctrl BACK_SLASH=clearSelection;ctrl C=copy;ctrl DOWN=selectNextChangeLead;ctrl END=selectLastChangeLead;ctrl HOME=selectFirstChangeLead;ctrl INSERT=copy;ctrl KP_DOWN=selectNextChangeLead;ctrl KP_LEFT=scrollLeft;ctrl KP_RIGHT=scrollRight;ctrl KP_UP=selectPreviousChangeLead;ctrl LEFT=scrollLeft;ctrl PAGE_DOWN=scrollDownChangeLead;ctrl PAGE_UP=scrollUpChangeLead;ctrl RIGHT=scrollRight;ctrl SLASH=selectAll;ctrl SPACE=toggleAndAnchor;ctrl UP=selectPreviousChangeLead;ctrl V=paste;ctrl X=cut;ADD=expand;COPY=copy;CUT=cut;DOWN=selectNext;END=selectLast;F2=startEditing;HOME=selectFirst;KP_DOWN=selectNext;KP_LEFT=selectParent;KP_RIGHT=selectChild;KP_UP=selectPrevious;LEFT=selectParent;PAGE_DOWN=scrollDownChangeSelection;PAGE_UP=scrollUpChangeSelection;PASTE=paste;RIGHT=selectChild;SPACE=addToSelection;SUBTRACT=collapse;UP=selectPrevious;shift ctrl DOWN=selectNextExtendSelection;shift ctrl END=selectLastExtendSelection;shift ctrl HOME=selectFirstExtendSelection;shift ctrl KP_DOWN=selectNextExtendSelection;shift ctrl KP_UP=selectPreviousExtendSelection;shift ctrl PAGE_DOWN=scrollDownExtendSelection;shift ctrl PAGE_UP=scrollUpExtendSelection;shift ctrl SPACE=moveSelectionTo;shift ctrl UP=selectPreviousExtendSelection;shift DELETE=cut;shift DOWN=selectNextExtendSelection;shift END=selectLastExtendSelection;shift HOME=selectFirstExtendSelection;shift INSERT=paste;shift KP_DOWN=selectNextExtendSelection;shift KP_UP=selectPreviousExtendSelection;shift PAGE_DOWN=scrollDownExtendSelection;shift PAGE_UP=scrollUpExtendSelection;shift SPACE=extendTo;shift UP=selectPreviousExtendSelection;",
      "Tree.focusInputMap.RightToLeft|KP_LEFT=selectChild;KP_RIGHT=selectParent;LEFT=selectChild;RIGHT=selectParent;"
  };

  public static void fill(UIDefaults table) {
    for (final String row : kRows) {
      int bar = row.indexOf('|');
      final String key = row.substring(0, bar);
      final String bindings = row.substring(bar + 1);
      table.put(key, new UIDefaults.LazyValue() {
        public Object createValue(UIDefaults t) {
          InputMap map = new InputMapUIResource();
          for (String pair : bindings.split(";")) {
            int eq = pair.lastIndexOf('=');
            if (eq > 0) {
              KeyStroke ks = KeyStroke.getKeyStroke(pair.substring(0, eq));
              if (ks != null) {
                map.put(ks, pair.substring(eq + 1));
              }
            }
          }
          return map;
        }
      });
    }
  }
}
