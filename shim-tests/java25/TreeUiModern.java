import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

public class TreeUiModern {
  static JTree tree;

  static void act(String name) {
    Action a = tree.getActionMap().get(name);
    if (a == null) {
      System.out.println(name + " missing");
      return;
    }
    a.actionPerformed(new ActionEvent(tree, ActionEvent.ACTION_PERFORMED, name));
    System.out.println(name + " -> " + java.util.Arrays.toString(tree.getSelectionRows()) + " lead=" + tree.getLeadSelectionRow()
        + " rows=" + tree.getRowCount());
  }

  static void click(int row, int dx, int count, int mods) {
    Rectangle r = tree.getRowBounds(row);
    tree.dispatchEvent(new MouseEvent(tree, MouseEvent.MOUSE_PRESSED, 0, mods, r.x + dx, r.y + 2, count, false,
        MouseEvent.BUTTON1));
    System.out.println("click " + row + " " + dx + " " + count + " -> " + java.util.Arrays.toString(tree.getSelectionRows())
        + " rows=" + tree.getRowCount());
  }

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        for (int i = 0; i < 4; i++) {
          DefaultMutableTreeNode n = new DefaultMutableTreeNode("n" + i);
          for (int j = 0; j < 3; j++) {
            n.add(new DefaultMutableTreeNode("c" + i + j));
          }
          root.add(n);
        }
        tree = new JTree(root);
        tree.setSize(200, 300);
        tree.setSelectionRow(0);
        String[] names = {"selectNext", "selectNext", "selectChild", "selectChild", "selectChild", "selectNext",
            "selectParent", "selectParent", "selectLast", "selectPrevious", "selectFirst", "selectNextExtendSelection",
            "selectNextExtendSelection", "selectPreviousChangeLead", "toggle", "selectAll", "clearSelection"};
        for (String n : names) {
          act(n);
        }
        System.out.println(tree.getInputMap().get(KeyStroke.getKeyStroke("DOWN")) + " "
            + tree.getInputMap().get(KeyStroke.getKeyStroke("ctrl DOWN")) + " "
            + tree.getInputMap().get(KeyStroke.getKeyStroke("RIGHT")) + " "
            + tree.getInputMap().get(KeyStroke.getKeyStroke("HOME")));
        click(1, -6, 1, 0);
        click(1, -6, 1, 0);
        click(3, -6, 1, 0);
        for (String n : new String[] {"selectFirstChangeLead", "expand", "collapse", "scrollDownChangeSelection",
            "scrollUpExtendSelection", "selectChildChangeLead", "selectParentChangeLead", "moveSelectionToParent"}) {
          act(n);
        }
        tree.collapseRow(0);
        System.out.println(tree.getRowCount());
        tree.expandRow(0);
        tree.setSelectionRow(1);
        KeyEvent ke = new KeyEvent(tree, KeyEvent.KEY_TYPED, 1000, 0, KeyEvent.VK_UNDEFINED, 'c');
        System.out.println("listeners " + tree.getKeyListeners().length + " " + tree.getFocusListeners().length + " "
            + tree.getComponentListeners().length + " " + tree.getTreeSelectionListeners().length);
        tree.setEditable(true);
        tree.startEditingAtPath(tree.getPathForRow(1));
        System.out.println(tree.isEditing());
        tree.cancelEditing();
        System.out.println(tree.isEditing());
      }
    });
  }
}
