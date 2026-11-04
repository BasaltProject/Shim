import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class KeymapsModern {
  static void act(JComponent c, String name) {
    Action a = c.getActionMap().get(name);
    a.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, name));
  }

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        JSlider s = new JSlider(0, 100, 40);
        StringBuilder sb = new StringBuilder();
        for (String n : new String[] {"positiveUnitIncrement", "positiveBlockIncrement", "negativeUnitIncrement",
            "negativeBlockIncrement", "maxScroll", "minScroll"}) {
          act(s, n);
          sb.append(s.getValue()).append(' ');
        }
        s.setInverted(true);
        act(s, "positiveUnitIncrement");
        sb.append(s.getValue());
        System.out.println(sb);
        JScrollBar b = new JScrollBar(JScrollBar.VERTICAL, 10, 20, 0, 200);
        sb = new StringBuilder();
        for (String n : new String[] {"positiveUnitIncrement", "positiveBlockIncrement", "negativeUnitIncrement",
            "maxScroll", "negativeBlockIncrement", "minScroll"}) {
          act(b, n);
          sb.append(b.getValue()).append(' ');
        }
        System.out.println(sb);
        JSpinner sp = new JSpinner(new SpinnerNumberModel(5, 0, 7, 1));
        act(sp, "increment");
        act(sp, "increment");
        act(sp, "increment");
        System.out.println(sp.getValue());
        act(sp, "decrement");
        System.out.println(sp.getValue());
        JList<String> list = new JList<String>(new String[] {"a", "b", "c", "d", "e", "f"});
        sb = new StringBuilder();
        for (String n : new String[] {"selectNextRow", "selectNextRow", "selectNextRowExtendSelection",
            "selectNextRowExtendSelection", "selectPreviousRowChangeLead", "toggleAndAnchor", "selectAll",
            "selectLastRow", "selectPreviousRow", "selectFirstRowExtendSelection", "clearSelection",
            "selectNextRowChangeLead", "addToSelection", "extendTo", "selectNextColumn", "moveSelectionTo",
            "selectLastRowChangeLead", "selectPreviousRowExtendSelection"}) {
          act(list, n);
          sb.append(n.substring(0, Math.min(14, n.length()))).append('=')
              .append(java.util.Arrays.toString(list.getSelectedIndices())).append(list.getLeadSelectionIndex())
              .append(list.getAnchorSelectionIndex()).append(' ');
        }
        System.out.println(sb);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        act(list, "selectNextRowExtendSelection");
        act(list, "selectAll");
        System.out.println(java.util.Arrays.toString(list.getSelectedIndices()));
        Object[] lk = list.getActionMap().allKeys();
        java.util.Arrays.sort(lk);
        System.out.println(java.util.Arrays.toString(lk) + " " + list.getInputMap().allKeys().length);
        JPanel big = new JPanel();
        big.setPreferredSize(new Dimension(500, 600));
        JScrollPane pane = new JScrollPane(big);
        pane.setSize(120, 100);
        pane.doLayout();
        pane.getViewport().setSize(100, 80);
        big.setSize(500, 600);
        sb = new StringBuilder();
        for (String n : new String[] {"unitScrollDown", "scrollDown", "unitScrollRight", "scrollEnd", "scrollUp",
            "unitScrollUp", "scrollHome"}) {
          act(pane, n);
          sb.append(pane.getViewport().getViewPosition().x).append(',')
              .append(pane.getViewport().getViewPosition().y).append(' ');
        }
        System.out.println(sb);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), new JPanel());
        split.setSize(200, 100);
        split.doLayout();
        ((JPanel) split.getLeftComponent()).setMinimumSize(new Dimension(10, 10));
        split.setDividerLocation(50);
        sb = new StringBuilder();
        for (String n : new String[] {"positiveIncrement", "positiveIncrement", "negativeIncrement", "selectMax",
            "selectMin"}) {
          act(split, n);
          sb.append(split.getDividerLocation()).append(' ');
        }
        System.out.println(sb);
        for (JComponent c : new JComponent[] {s, b, sp, pane, split}) {
          Object[] keys = c.getActionMap().allKeys();
          java.util.Arrays.sort(keys);
          System.out.println(java.util.Arrays.toString(keys));
          for (int cond : new int[] {JComponent.WHEN_FOCUSED, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT}) {
            KeyStroke[] ks = c.getInputMap(cond).allKeys();
            System.out.println(ks == null ? 0 : ks.length);
          }
        }
        System.out.println(s.getInputMap().get(KeyStroke.getKeyStroke("ctrl PAGE_UP")) + " "
            + b.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(KeyStroke.getKeyStroke("END")));
      }
    });
  }
}
