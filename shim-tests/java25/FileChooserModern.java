import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;

public class FileChooserModern {
  static JFileChooser fc;
  static BasicFileChooserUI ui;

  static void settle() throws Exception {
    Thread.sleep(400);
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() { }
    });
  }

  interface Step {
    void run() throws Exception;
  }

  static void edt(final Step step) throws Exception {
    final Throwable[] failure = new Throwable[1];
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          step.run();
        } catch (Throwable t) {
          failure[0] = t;
        }
      }
    });
    if (failure[0] != null) {
      throw new RuntimeException(failure[0]);
    }
  }

  static String listing() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ui.getModel().getSize(); i++) {
      sb.append(((File) ui.getModel().getElementAt(i)).getName()).append(' ');
    }
    return sb.toString();
  }

  static Component find(Component c, Class<?> k) {
    if (k.isInstance(c)) {
      return c;
    }
    if (c instanceof Container) {
      for (Component x : ((Container) c).getComponents()) {
        Component r = find(x, k);
        if (r != null) {
          return r;
        }
      }
    }
    return null;
  }

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    Locale.setDefault(Locale.US);
    File dir = Files.createTempDirectory("fcm").toFile();
    for (String d : new String[] {"Zdir", "adir", ".hid"}) {
      new File(dir, d).mkdir();
    }
    for (String f : new String[] {"b.txt", "A.txt", "a2.txt", "B10.txt", "B9.txt", ".dot", "10.x", "2.x", "_u", "Zfile",
        "c.java"}) {
      new File(dir, f).createNewFile();
    }
    final File root = dir;
    edt(() -> {
      fc = new JFileChooser(root);
      ui = (BasicFileChooserUI) fc.getUI();
    });
    settle();
    edt(() -> System.out.println(fc.getPreferredSize() + " " + fc.getMinimumSize() + " " + ui.getClass().getName()));
    edt(() -> System.out.println(listing()));
    edt(() -> fc.setFileHidingEnabled(false));
    settle();
    edt(() -> System.out.println(listing()));
    edt(() -> {
      fc.setFileHidingEnabled(true);
      fc.addChoosableFileFilter(new FileNameExtensionFilter("Text", "txt"));
      fc.setFileFilter(fc.getChoosableFileFilters()[1]);
    });
    settle();
    edt(() -> System.out.println(listing() + "| " + fc.getFileFilter().getDescription() + " "
        + fc.getChoosableFileFilters().length));
    edt(() -> {
      fc.setFileFilter(fc.getAcceptAllFileFilter());
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    });
    settle();
    edt(() -> System.out.println(listing() + "| " + ui.getFileName().equals(root.getPath())));
    edt(() -> fc.setFileSelectionMode(JFileChooser.FILES_ONLY));
    settle();
    edt(() -> System.out.println(listing()));
    edt(() -> {
      fc.setDialogType(JFileChooser.SAVE_DIALOG);
      System.out.println(ui.getApproveButtonText(fc) + " " + ui.getDialogTitle(fc) + " "
          + ui.getApproveButtonToolTipText(fc));
      fc.setDialogType(JFileChooser.OPEN_DIALOG);
      fc.setApproveButtonText("Go");
      System.out.println(ui.getApproveButtonText(fc) + " " + ui.getDialogTitle(fc) + " "
          + ui.getDefaultButton(fc).getText());
      fc.setApproveButtonText(null);
    });
    final int[] events = new int[2];
    edt(() -> {
      fc.addActionListener(e -> events[e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION) ? 0 : 1]++);
      ui.setFileName("b.txt");
      ui.getApproveSelectionAction().actionPerformed(null);
      System.out.println(fc.getSelectedFile().getName() + " " + events[0] + " " + events[1]);
      ui.setFileName("adir");
      ui.getApproveSelectionAction().actionPerformed(null);
    });
    settle();
    edt(() -> {
      System.out.println(fc.getCurrentDirectory().getName() + " " + events[0] + " " + events[1]);
      ui.getChangeToParentDirectoryAction().actionPerformed(null);
    });
    settle();
    edt(() -> {
      System.out.println(fc.getCurrentDirectory().equals(root));
      ui.getCancelSelectionAction().actionPerformed(null);
      System.out.println(events[0] + " " + events[1]);
      ui.setFileName("*.txt");
      ui.getApproveSelectionAction().actionPerformed(null);
    });
    settle();
    edt(() -> {
      System.out.println(listing() + "| " + fc.getFileFilter().getDescription());
      ui.setFileName("");
      ui.getApproveSelectionAction().actionPerformed(null);
    });
    settle();
    edt(() -> System.out.println(listing()));
    edt(() -> {
      ui.getNewFolderAction().actionPerformed(null);
      System.out.println(fc.getSelectedFile().getName());
      fc.getSelectedFile().delete();
      fc.setFileFilter(fc.getAcceptAllFileFilter());
      fc.rescanCurrentDirectory();
      Container top = (Container) ((Container) fc.getComponent(0)).getComponent(0);
      ((AbstractButton) top.getComponent(top.getComponentCount() - 1)).doClick();
    });
    settle();
    edt(() -> {
      System.out.println(fc.getPreferredSize());
      JTable table = (JTable) find(fc, JTable.class);
      System.out.println(table.getRowCount() + " " + table.getColumnCount() + " " + table.getRowHeight() + " "
          + table.getColumnName(0) + "," + table.getColumnName(1) + "," + table.getColumnName(2) + ","
          + table.getColumnModel().getColumn(0).getHeaderValue() + ","
          + table.getColumnModel().getColumn(2).getHeaderValue());
      System.out.println(table.getValueAt(0, 1) + " " + table.getValueAt(table.getRowCount() - 1, 1) + " "
          + ui.getFileView(fc).getTypeDescription(root) + " "
          + ui.getFileView(fc).getTypeDescription(new File(root, "b.txt")));
      JComponent bottom = (JComponent) fc.getComponent(3);
      JComboBox<?> filterBox = null;
      for (Component c : bottom.getComponents()) {
        if (c instanceof JPanel && find(c, JComboBox.class) != null) {
          filterBox = (JComboBox<?>) find(c, JComboBox.class);
        }
      }
      System.out.println(filterBox.getItemCount() + " "
          + ((javax.swing.filechooser.FileFilter) filterBox.getItemAt(0)).getDescription());
      JComboBox<?> dirBox = (JComboBox<?>) find(fc.getComponent(0), JComboBox.class);
      System.out.println(((File) dirBox.getItemAt(0)).getPath() + " "
          + ((File) dirBox.getSelectedItem()).equals(root.getCanonicalFile()));
      fc.setMultiSelectionEnabled(true);
      fc.setSelectedFiles(new File[] {new File(root, "b.txt"), new File(root, "c.java")});
      System.out.println(ui.getFileName());
      fc.setAccessory(new JLabel("acc"));
      System.out.println(ui.getAccessoryPanel().getComponentCount());
      fc.setControlButtonsAreShown(false);
      fc.setFileView(new javax.swing.filechooser.FileView() {
        public String getName(File f) {
          return "N" + f.getName();
        }
      });
      System.out.println(fc.getName(new File(root, "b.txt")) + " " + fc.getDescription(new File(root, "b.txt")));
    });
    for (File f : root.listFiles()) {
      f.delete();
    }
    root.delete();
    System.exit(0);
  }
}
