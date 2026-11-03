/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FileChooserUI;

public class BasicFileChooserUI extends FileChooserUI {
  protected Icon directoryIcon;
  protected Icon fileIcon;
  protected Icon computerIcon;
  protected Icon hardDriveIcon;
  protected Icon floppyDriveIcon;
  protected Icon newFolderIcon;
  protected Icon upFolderIcon;
  protected Icon homeFolderIcon;
  protected Icon listViewIcon;
  protected Icon detailsViewIcon;
  protected Icon viewMenuIcon;
  protected int saveButtonMnemonic;
  protected int openButtonMnemonic;
  protected int cancelButtonMnemonic;
  protected int updateButtonMnemonic;
  protected int helpButtonMnemonic;
  protected int directoryOpenButtonMnemonic;
  protected String saveButtonText;
  protected String openButtonText;
  protected String cancelButtonText;
  protected String updateButtonText;
  protected String helpButtonText;
  protected String directoryOpenButtonText;
  protected String saveButtonToolTipText;
  protected String openButtonToolTipText;
  protected String cancelButtonToolTipText;
  protected String updateButtonToolTipText;
  protected String helpButtonToolTipText;
  protected String directoryOpenButtonToolTipText;

  private JFileChooser filechooser;
  private BasicDirectoryModel model;
  private String filename;
  private String dirname;
  private File directory;
  private boolean directorySelected;
  private JPanel accessoryPanel;
  private final Action approveSelectionAction = new ApproveSelectionAction();
  private final Action cancelSelectionAction = new CancelSelectionAction();
  private final Action updateAction = new UpdateAction();
  private final Action newFolderAction = new NewFolderAction();
  private final Action goHomeAction = new GoHomeAction();
  private final Action changeToParentDirectoryAction = new ChangeToParentDirectoryAction();
  private final BasicFileView fileView = new BasicFileView();
  private final AcceptAllFileFilter acceptAll = new AcceptAllFileFilter();
  private PropertyChangeListener propertyListener;
  private GlobFilter globFilter;
  private FileFilter filterBeforeGlob;
  private static final String kNewFolderErrorSeparator = "FileChooser.newFolderErrorSeparator";
  private String newFolderErrorText;
  private String fileDescriptionText;
  private String directoryDescriptionText;

  public BasicFileChooserUI(JFileChooser b) { }

  public static ComponentUI createUI(JComponent c) {
    return new BasicFileChooserUI((JFileChooser) c);
  }

  public void installUI(JComponent c) {
    accessoryPanel = new JPanel(new java.awt.BorderLayout());
    filechooser = (JFileChooser) c;
    createModel();
    clearIconCache();
    installDefaults(filechooser);
    installComponents(filechooser);
    installListeners(filechooser);
    filechooser.applyComponentOrientation(filechooser.getComponentOrientation());
  }

  public void uninstallUI(JComponent c) {
    uninstallListeners(filechooser);
    uninstallComponents(filechooser);
    uninstallDefaults(filechooser);
    if (accessoryPanel != null) {
      accessoryPanel.removeAll();
    }
    accessoryPanel = null;
    getFileChooser().removeAll();
  }

  public void installComponents(JFileChooser fc) { }

  public void uninstallComponents(JFileChooser fc) { }

  protected void installListeners(JFileChooser fc) {
    propertyListener = createPropertyChangeListener(fc);
    if (propertyListener != null) {
      fc.addPropertyChangeListener(propertyListener);
    }
    fc.addPropertyChangeListener(model);
    InputMap inputMap = (InputMap) UIManager.get("FileChooser.ancestorInputMap");
    SwingUtilities.replaceUIInputMap(fc, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
    javax.swing.ActionMap actionMap = fileChooserActions();
    SwingUtilities.replaceUIActionMap(fc, actionMap);
  }

  javax.swing.ActionMap fileChooserActions() {
    javax.swing.ActionMap map = new javax.swing.plaf.ActionMapUIResource();
    map.put("approveSelection", getApproveSelectionAction());
    map.put("cancelSelection", getCancelSelectionAction());
    map.put("Go Up", getChangeToParentDirectoryAction());
    return map;
  }

  protected void uninstallListeners(JFileChooser fc) {
    if (propertyListener != null) {
      fc.removePropertyChangeListener(propertyListener);
    }
    fc.removePropertyChangeListener(model);
    SwingUtilities.replaceUIInputMap(fc, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
    SwingUtilities.replaceUIActionMap(fc, null);
    propertyListener = null;
  }

  protected void installDefaults(JFileChooser fc) {
    installIcons(fc);
    installStrings(fc);
  }

  protected void installIcons(JFileChooser fc) {
    directoryIcon = UIManager.getIcon("FileView.directoryIcon");
    fileIcon = UIManager.getIcon("FileView.fileIcon");
    computerIcon = UIManager.getIcon("FileView.computerIcon");
    hardDriveIcon = UIManager.getIcon("FileView.hardDriveIcon");
    floppyDriveIcon = UIManager.getIcon("FileView.floppyDriveIcon");
    newFolderIcon = UIManager.getIcon("FileChooser.newFolderIcon");
    upFolderIcon = UIManager.getIcon("FileChooser.upFolderIcon");
    homeFolderIcon = UIManager.getIcon("FileChooser.homeFolderIcon");
    detailsViewIcon = UIManager.getIcon("FileChooser.detailsViewIcon");
    listViewIcon = UIManager.getIcon("FileChooser.listViewIcon");
    viewMenuIcon = UIManager.getIcon("FileChooser.viewMenuIcon");
  }

  protected void installStrings(JFileChooser fc) {
    Locale l = fc.getLocale();
    newFolderErrorText = UIManager.getString("FileChooser.newFolderErrorText", l);
    fileDescriptionText = UIManager.getString("FileChooser.fileDescriptionText", l);
    directoryDescriptionText = UIManager.getString("FileChooser.directoryDescriptionText", l);
    saveButtonText = UIManager.getString("FileChooser.saveButtonText", l);
    openButtonText = UIManager.getString("FileChooser.openButtonText", l);
    saveButtonMnemonic = mnemonic("FileChooser.saveButtonMnemonic", l);
    openButtonMnemonic = mnemonic("FileChooser.openButtonMnemonic", l);
    cancelButtonText = UIManager.getString("FileChooser.cancelButtonText", l);
    cancelButtonMnemonic = mnemonic("FileChooser.cancelButtonMnemonic", l);
    updateButtonText = UIManager.getString("FileChooser.updateButtonText", l);
    updateButtonMnemonic = mnemonic("FileChooser.updateButtonMnemonic", l);
    helpButtonText = UIManager.getString("FileChooser.helpButtonText", l);
    helpButtonMnemonic = mnemonic("FileChooser.helpButtonMnemonic", l);
    directoryOpenButtonText = UIManager.getString("FileChooser.directoryOpenButtonText", l);
    directoryOpenButtonMnemonic = mnemonic("FileChooser.directoryOpenButtonMnemonic", l);
    saveButtonToolTipText = UIManager.getString("FileChooser.saveButtonToolTipText", l);
    openButtonToolTipText = UIManager.getString("FileChooser.openButtonToolTipText", l);
    cancelButtonToolTipText = UIManager.getString("FileChooser.cancelButtonToolTipText", l);
    updateButtonToolTipText = UIManager.getString("FileChooser.updateButtonToolTipText", l);
    helpButtonToolTipText = UIManager.getString("FileChooser.helpButtonToolTipText", l);
    directoryOpenButtonToolTipText = UIManager.getString("FileChooser.directoryOpenButtonToolTipText", l);
  }

  private static int mnemonic(String key, Locale l) {
    Object v = UIManager.get(key, l);
    if (v instanceof Integer) {
      return ((Integer) v).intValue();
    }
    if (v instanceof String && ((String) v).length() > 0) {
      try {
        return Integer.parseInt((String) v);
      } catch (NumberFormatException e) {
        return Character.toUpperCase(((String) v).charAt(0));
      }
    }
    return 0;
  }

  protected void uninstallDefaults(JFileChooser fc) {
    uninstallIcons(fc);
    uninstallStrings(fc);
  }

  protected void uninstallIcons(JFileChooser fc) {
    directoryIcon = null;
    fileIcon = null;
    computerIcon = null;
    hardDriveIcon = null;
    floppyDriveIcon = null;
    newFolderIcon = null;
    upFolderIcon = null;
    homeFolderIcon = null;
    detailsViewIcon = null;
    listViewIcon = null;
    viewMenuIcon = null;
  }

  protected void uninstallStrings(JFileChooser fc) {
    saveButtonText = null;
    openButtonText = null;
    cancelButtonText = null;
    updateButtonText = null;
    helpButtonText = null;
    directoryOpenButtonText = null;
    saveButtonToolTipText = null;
    openButtonToolTipText = null;
    cancelButtonToolTipText = null;
    updateButtonToolTipText = null;
    helpButtonToolTipText = null;
    directoryOpenButtonToolTipText = null;
  }

  protected void createModel() {
    if (model != null) {
      model.invalidateFileCache();
    }
    model = new BasicDirectoryModel(getFileChooser());
  }

  public BasicDirectoryModel getModel() { return model; }

  public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) { return null; }

  public String getFileName() { return filename; }

  public String getDirectoryName() { return dirname; }

  public void setFileName(String filename) { this.filename = filename; }

  public void setDirectoryName(String dirname) { this.dirname = dirname; }

  public void rescanCurrentDirectory(JFileChooser fc) { }

  public void ensureFileIsVisible(JFileChooser fc, File f) { }

  public JFileChooser getFileChooser() { return filechooser; }

  public JPanel getAccessoryPanel() { return accessoryPanel; }

  protected JButton getApproveButton(JFileChooser fc) { return null; }

  public JButton getDefaultButton(JFileChooser fc) { return getApproveButton(fc); }

  public String getApproveButtonToolTipText(JFileChooser fc) {
    String tooltipText = fc.getApproveButtonToolTipText();
    if (tooltipText != null) {
      return tooltipText;
    }
    return fc.getDialogType() == JFileChooser.SAVE_DIALOG ? saveButtonToolTipText : openButtonToolTipText;
  }

  public int getApproveButtonMnemonic(JFileChooser fc) {
    int mn = fc.getApproveButtonMnemonic();
    if (mn > 0) {
      return mn;
    }
    return fc.getDialogType() == JFileChooser.SAVE_DIALOG ? saveButtonMnemonic : openButtonMnemonic;
  }

  public String getApproveButtonText(JFileChooser fc) {
    String buttonText = fc.getApproveButtonText();
    if (buttonText != null) {
      return buttonText;
    }
    return fc.getDialogType() == JFileChooser.SAVE_DIALOG ? saveButtonText : openButtonText;
  }

  public String getDialogTitle(JFileChooser fc) {
    String title = fc.getDialogTitle();
    if (title != null) {
      return title;
    }
    int type = fc.getDialogType();
    if (type == JFileChooser.OPEN_DIALOG) {
      return UIManager.getString("FileChooser.openDialogTitleText", fc.getLocale());
    }
    if (type == JFileChooser.SAVE_DIALOG) {
      return UIManager.getString("FileChooser.saveDialogTitleText", fc.getLocale());
    }
    return getApproveButtonText(fc);
  }

  public FileFilter getAcceptAllFileFilter(JFileChooser fc) { return acceptAll; }

  public FileView getFileView(JFileChooser fc) { return fileView; }

  public void clearIconCache() { fileView.clearIconCache(); }

  protected void setDirectory(File f) { directory = f; }

  protected File getDirectory() { return directory; }

  protected boolean isDirectorySelected() { return directorySelected; }

  protected void setDirectorySelected(boolean b) { directorySelected = b; }

  public Action getApproveSelectionAction() { return approveSelectionAction; }

  public Action getCancelSelectionAction() { return cancelSelectionAction; }

  public Action getChangeToParentDirectoryAction() { return changeToParentDirectoryAction; }

  public Action getNewFolderAction() { return newFolderAction; }

  public Action getGoHomeAction() { return goHomeAction; }

  public Action getUpdateAction() { return updateAction; }

  public ListSelectionListener createListSelectionListener(JFileChooser fc) { return new SelectionListener(); }

  protected MouseListener createDoubleClickListener(JFileChooser fc, JList<?> list) {
    return new DoubleClickListener(list);
  }

  private void changeDirectory(File dir) {
    JFileChooser fc = getFileChooser();
    if (dir != null && fc.getFileSystemView().isLink(dir)) {
      try {
        File target = fc.getFileSystemView().getLinkLocation(dir);
        if (target != null) {
          dir = target;
        }
      } catch (java.io.FileNotFoundException ignored) {
        dir = fc.getCurrentDirectory();
      }
    }
    fc.setCurrentDirectory(dir);
    if (fc.getFileSelectionMode() == JFileChooser.FILES_AND_DIRECTORIES && fc.getFileSystemView().isFileSystem(dir)) {
      setFileName(dir.getAbsolutePath());
    }
    if (dir != null && fc.getFileSelectionMode() == JFileChooser.FILES_ONLY) {
      fc.setSelectedFile(null);
    }
  }

  private static boolean isGlobPattern(String filename) {
    return File.separatorChar == '/' && (filename.indexOf('*') >= 0 || filename.indexOf('?') >= 0
        || filename.indexOf('[') >= 0);
  }

  private void resetGlobFilter() {
    if (globFilter != null) {
      JFileChooser fc = getFileChooser();
      FileFilter previous = filterBeforeGlob;
      fc.removeChoosableFileFilter(globFilter);
      globFilter = null;
      filterBeforeGlob = null;
      if (previous != null) {
        fc.setFileFilter(previous);
      }
    }
  }

  private static final class GlobFilter extends FileFilter {
    private Pattern pattern;
    private String description;

    void setPattern(String globPattern) {
      StringBuilder regex = new StringBuilder();
      for (int i = 0; i < globPattern.length(); i++) {
        char ch = globPattern.charAt(i);
        if (ch == '*') {
          regex.append(".*");
        } else if (ch == '?') {
          regex.append('.');
        } else if (ch == '[') {
          int close = globPattern.indexOf(']', i);
          if (close < 0) {
            regex.append("\\[");
          } else {
            regex.append(globPattern, i, close + 1);
            i = close;
          }
        } else if ("\\.^$|(){}+".indexOf(ch) >= 0) {
          regex.append('\\').append(ch);
        } else {
          regex.append(ch);
        }
      }
      pattern = Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
      description = globPattern;
    }

    public boolean accept(File f) {
      return f == null || f.isDirectory() || pattern.matcher(f.getName()).matches();
    }

    public String getDescription() {
      return description;
    }
  }

  protected class SelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting()) {
        return;
      }
      JFileChooser chooser = getFileChooser();
      FileSystemView fsv = chooser.getFileSystemView();
      JList<?> list = (JList<?>) e.getSource();
      int fsm = chooser.getFileSelectionMode();
      boolean useSetDirectory = (fsm == JFileChooser.FILES_ONLY) || (fsm == JFileChooser.DIRECTORIES_ONLY)
          ? false : true;
      if (chooser.isMultiSelectionEnabled()) {
        File[] files = null;
        Object[] objects = list.getSelectedValues();
        if (objects != null) {
          if (objects.length == 1 && ((File) objects[0]).isDirectory() && chooser.isTraversable(((File) objects[0]))
              && (useSetDirectory || !fsv.isFileSystem(((File) objects[0])))) {
            setDirectorySelected(true);
            setDirectory(((File) objects[0]));
          } else {
            ArrayList<File> fList = new ArrayList<File>(objects.length);
            for (Object object : objects) {
              File f = (File) object;
              boolean isDir = f.isDirectory();
              if ((chooser.isFileSelectionEnabled() && !isDir)
                  || (chooser.isDirectorySelectionEnabled() && fsv.isFileSystem(f) && isDir)) {
                fList.add(f);
              }
            }
            if (fList.size() > 0) {
              files = fList.toArray(new File[fList.size()]);
            }
            setDirectorySelected(false);
          }
        }
        chooser.setSelectedFiles(files);
      } else {
        File file = (File) list.getSelectedValue();
        if (file != null && file.isDirectory() && chooser.isTraversable(file)
            && (useSetDirectory || !fsv.isFileSystem(file))) {
          setDirectorySelected(true);
          setDirectory(file);
          if (useSetDirectory) {
            chooser.setSelectedFile(null);
          }
        } else {
          setDirectorySelected(false);
          if (file != null) {
            chooser.setSelectedFile(file);
          }
        }
      }
    }
  }

  private class DoubleClickListener extends MouseAdapter {
    private final JList<?> list;

    DoubleClickListener(JList<?> list) {
      this.list = list;
    }

    public void mouseClicked(MouseEvent e) {
      if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 2) {
        return;
      }
      int index = list.locationToIndex(e.getPoint());
      if (index < 0) {
        return;
      }
      File f = (File) list.getModel().getElementAt(index);
      if (getFileChooser().isTraversable(f)) {
        list.clearSelection();
        changeDirectory(f);
      } else {
        getFileChooser().approveSelection();
      }
    }
  }

  protected class ApproveSelectionAction extends AbstractAction {
    protected ApproveSelectionAction() {
      super("approveSelection");
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser chooser = getFileChooser();
      if (isDirectorySelected()) {
        File dir = getDirectory();
        if (dir != null) {
          if (chooser.isTraversable(dir)) {
            setDirectory(null);
            changeDirectory(dir);
          } else {
            chooser.approveSelection();
          }
          return;
        }
      }
      String name = getFileName();
      if (name != null) {
        int end = name.length();
        while (end > 0 && name.charAt(end - 1) <= ' ') {
          end--;
        }
        name = name.substring(0, end);
      }
      if (name == null || name.length() == 0) {
        resetGlobFilter();
        return;
      }
      FileSystemView fs = chooser.getFileSystemView();
      File dir = chooser.getCurrentDirectory();
      if (File.separatorChar == '/') {
        if (name.startsWith("~/")) {
          name = System.getProperty("user.home") + name.substring(1);
        } else if (name.equals("~")) {
          name = System.getProperty("user.home");
        }
      }
      File selectedFile = null;
      File[] selectedFiles = null;
      if (chooser.isMultiSelectionEnabled() && name.length() > 1 && name.charAt(0) == '"'
          && name.charAt(name.length() - 1) == '"') {
        List<File> picked = new ArrayList<File>();
        for (String part : name.substring(1, name.length() - 1).split("\" \"")) {
          File file = fs.createFileObject(part);
          if (!file.isAbsolute()) {
            file = fs.getChild(dir, part);
          }
          picked.add(file);
        }
        if (!picked.isEmpty()) {
          selectedFiles = picked.toArray(new File[picked.size()]);
        }
        resetGlobFilter();
      } else {
        selectedFile = fs.createFileObject(name);
        if (!selectedFile.isAbsolute()) {
          selectedFile = fs.getChild(dir, name);
        }
        if (!selectedFile.exists() && isGlobPattern(name)) {
          changeDirectory(selectedFile.getParentFile());
          if (globFilter == null) {
            globFilter = new GlobFilter();
          }
          globFilter.setPattern(selectedFile.getName());
          if (!(chooser.getFileFilter() instanceof GlobFilter)) {
            filterBeforeGlob = chooser.getFileFilter();
          }
          chooser.setFileFilter(null);
          chooser.setFileFilter(globFilter);
          return;
        }
        resetGlobFilter();
        boolean isDir = selectedFile.isDirectory();
        boolean isTraversable = chooser.isTraversable(selectedFile);
        boolean dirSelection = chooser.isDirectorySelectionEnabled();
        boolean fileSelection = chooser.isFileSelectionEnabled();
        boolean control = e != null
            && (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != 0;
        if (isDir && isTraversable && (control || !dirSelection)) {
          changeDirectory(selectedFile);
          return;
        } else if ((isDir || !fileSelection) && (!isDir || !dirSelection) && (!dirSelection || selectedFile.exists())) {
          selectedFile = null;
        }
      }
      if (selectedFiles != null || selectedFile != null) {
        if (selectedFiles != null || chooser.isMultiSelectionEnabled()) {
          if (selectedFiles == null) {
            selectedFiles = new File[] {selectedFile};
          }
          chooser.setSelectedFiles(selectedFiles);
          chooser.setSelectedFiles(selectedFiles);
        } else {
          chooser.setSelectedFile(selectedFile);
        }
        chooser.approveSelection();
      } else {
        if (chooser.isMultiSelectionEnabled()) {
          chooser.setSelectedFiles(null);
        } else {
          chooser.setSelectedFile(null);
        }
        chooser.cancelSelection();
      }
    }
  }

  protected class CancelSelectionAction extends AbstractAction {
    protected CancelSelectionAction() {
      super("cancelSelection");
    }

    public void actionPerformed(ActionEvent e) {
      getFileChooser().cancelSelection();
    }
  }

  protected class UpdateAction extends AbstractAction {
    protected UpdateAction() {
      super("Refresh");
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser fc = getFileChooser();
      fc.rescanCurrentDirectory();
    }
  }

  protected class NewFolderAction extends AbstractAction {
    protected NewFolderAction() {
      super("New Folder");
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser fc = getFileChooser();
      File currentDirectory = fc.getCurrentDirectory();
      if (!currentDirectory.exists()) {
        JOptionPane.showMessageDialog(fc, newFolderErrorText, newFolderErrorText, JOptionPane.ERROR_MESSAGE);
        return;
      }
      File newFolder;
      try {
        newFolder = fc.getFileSystemView().createNewFolder(currentDirectory);
        if (fc.isMultiSelectionEnabled()) {
          fc.setSelectedFiles(new File[] {newFolder});
        } else {
          fc.setSelectedFile(newFolder);
        }
      } catch (IOException exc) {
        JOptionPane.showMessageDialog(fc, newFolderErrorText + UIManager.getString(kNewFolderErrorSeparator)
            + exc.getMessage(), newFolderErrorText, JOptionPane.ERROR_MESSAGE);
        return;
      }
      fc.rescanCurrentDirectory();
    }
  }

  protected class GoHomeAction extends AbstractAction {
    protected GoHomeAction() {
      super("Go Home");
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser fc = getFileChooser();
      changeDirectory(fc.getFileSystemView().getHomeDirectory());
    }
  }

  protected class ChangeToParentDirectoryAction extends AbstractAction {
    protected ChangeToParentDirectoryAction() {
      super("Go Up");
      putValue(Action.ACTION_COMMAND_KEY, "Go Up");
    }

    public void actionPerformed(ActionEvent e) {
      getFileChooser().changeToParentDirectory();
    }
  }

  protected class AcceptAllFileFilter extends FileFilter {
    public AcceptAllFileFilter() { }

    public boolean accept(File f) {
      return true;
    }

    public String getDescription() {
      return UIManager.getString("FileChooser.acceptAllFileFilterText");
    }
  }

  protected class BasicFileView extends FileView {
    protected Map<File, Icon> iconCache = new HashMap<File, Icon>();

    public BasicFileView() { }

    public void clearIconCache() {
      iconCache = new HashMap<File, Icon>();
    }

    public String getName(File f) {
      return f == null ? null : getFileChooser().getFileSystemView().getSystemDisplayName(f);
    }

    public void setName(File f, String name) { }

    public String getDescription(File f) {
      return f.getName();
    }

    public String getTypeDescription(File f) {
      String type = getFileChooser().getFileSystemView().getSystemTypeDescription(f);
      if (type == null) {
        type = f.isDirectory() ? directoryDescriptionText : fileDescriptionText;
      }
      return type;
    }

    public Icon getCachedIcon(File f) {
      return iconCache.get(f);
    }

    public void cacheIcon(File f, Icon i) {
      if (f == null || i == null) {
        return;
      }
      iconCache.put(f, i);
    }

    public Icon getIcon(File f) {
      Icon icon = getCachedIcon(f);
      if (icon != null) {
        return icon;
      }
      icon = fileIcon;
      if (f != null) {
        FileSystemView fsv = getFileChooser().getFileSystemView();
        if (fsv.isFloppyDrive(f)) {
          icon = floppyDriveIcon;
        } else if (fsv.isDrive(f)) {
          icon = hardDriveIcon;
        } else if (fsv.isComputerNode(f)) {
          icon = computerIcon;
        } else if (f.isDirectory()) {
          icon = directoryIcon;
        }
      }
      cacheIcon(f, icon);
      return icon;
    }

    public Boolean isHidden(File f) {
      String name = f.getName();
      return Boolean.valueOf(name != null && name.startsWith("."));
    }
  }
}
