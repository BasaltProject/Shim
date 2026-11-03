/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.FileChooserUI;

public class JFileChooser extends JComponent implements Accessible {
  public static final int OPEN_DIALOG = 0;
  public static final int SAVE_DIALOG = 1;
  public static final int CUSTOM_DIALOG = 2;
  public static final int CANCEL_OPTION = 1;
  public static final int APPROVE_OPTION = 0;
  public static final int ERROR_OPTION = -1;
  public static final int FILES_ONLY = 0;
  public static final int DIRECTORIES_ONLY = 1;
  public static final int FILES_AND_DIRECTORIES = 2;
  public static final java.lang.String CANCEL_SELECTION = "CancelSelection";
  public static final java.lang.String APPROVE_SELECTION = "ApproveSelection";
  public static final java.lang.String APPROVE_BUTTON_TEXT_CHANGED_PROPERTY = "ApproveButtonTextChangedProperty";
  public static final java.lang.String APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY = "ApproveButtonToolTipTextChangedProperty";
  public static final java.lang.String APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY = "ApproveButtonMnemonicChangedProperty";
  public static final java.lang.String CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY = "ControlButtonsAreShownChangedProperty";
  public static final java.lang.String DIRECTORY_CHANGED_PROPERTY = "directoryChanged";
  public static final java.lang.String SELECTED_FILE_CHANGED_PROPERTY = "SelectedFileChangedProperty";
  public static final java.lang.String SELECTED_FILES_CHANGED_PROPERTY = "SelectedFilesChangedProperty";
  public static final java.lang.String MULTI_SELECTION_ENABLED_CHANGED_PROPERTY = "MultiSelectionEnabledChangedProperty";
  public static final java.lang.String FILE_SYSTEM_VIEW_CHANGED_PROPERTY = "FileSystemViewChanged";
  public static final java.lang.String FILE_VIEW_CHANGED_PROPERTY = "fileViewChanged";
  public static final java.lang.String FILE_HIDING_CHANGED_PROPERTY = "FileHidingChanged";
  public static final java.lang.String FILE_FILTER_CHANGED_PROPERTY = "fileFilterChanged";
  public static final java.lang.String FILE_SELECTION_MODE_CHANGED_PROPERTY = "fileSelectionChanged";
  public static final java.lang.String ACCESSORY_CHANGED_PROPERTY = "AccessoryChangedProperty";
  public static final java.lang.String ACCEPT_ALL_FILE_FILTER_USED_CHANGED_PROPERTY = "acceptAllFileFilterUsedChanged";
  public static final java.lang.String DIALOG_TITLE_CHANGED_PROPERTY = "DialogTitleChangedProperty";
  public static final java.lang.String DIALOG_TYPE_CHANGED_PROPERTY = "DialogTypeChangedProperty";
  public static final java.lang.String CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY = "ChoosableFileFilterChangedProperty";

  protected AccessibleContext accessibleContext = null;
  private FileSystemView fileSystemView = null;
  private JComponent accessory = null;
  private int fileSelectionMode = FILES_ONLY;
  private boolean multiSelectionEnabled = false;
  private boolean useFileHiding = true;
  private boolean dragEnabled = false;
  private FileFilter fileFilter = null;
  private FileView fileView = null;
  private boolean controlsShown = true;
  private String approveButtonToolTipText = null;
  private int approveButtonMnemonic = 0;
  private Vector<FileFilter> filters = new Vector<FileFilter>(5);
  private String approveButtonText = null;
  private String dialogTitle = null;
  private int dialogType = OPEN_DIALOG;
  private int returnValue = ERROR_OPTION;
  private boolean useAcceptAllFileFilter = true;
  private File currentDirectory = null;
  private File selectedFile = null;
  private File[] selectedFiles;
  private final FileFilter acceptAll = new FileFilter() {
    public boolean accept(File f) { return true; }

    public String getDescription() { return UIManager.getString("FileChooser.acceptAllFileFilterText"); }
  };

  public JFileChooser() { this((File) null, (FileSystemView) null); }

  public JFileChooser(String currentDirectoryPath) { this(currentDirectoryPath, (FileSystemView) null); }

  public JFileChooser(File currentDirectory) { this(currentDirectory, (FileSystemView) null); }

  public JFileChooser(FileSystemView fsv) { this((File) null, fsv); }

  public JFileChooser(File currentDirectory, FileSystemView fsv) {
    setup(fsv);
    setCurrentDirectory(currentDirectory);
  }

  public JFileChooser(String currentDirectoryPath, FileSystemView fsv) {
    setup(fsv);
    if (currentDirectoryPath == null) {
      setCurrentDirectory(null);
    } else {
      setCurrentDirectory(fileSystemView.createFileObject(currentDirectoryPath));
    }
  }

  protected void setup(FileSystemView view) {
    if (view == null) {
      view = FileSystemView.getFileSystemView();
    }
    setFileSystemView(view);
    updateUI();
    if (isAcceptAllFileFilterUsed()) {
      addChoosableFileFilter(getAcceptAllFileFilter());
      setFileFilter(getAcceptAllFileFilter());
    }
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
  }

  public void setDragEnabled(boolean b) {
    if (b && java.awt.GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    dragEnabled = b;
  }

  public boolean getDragEnabled() { return dragEnabled; }

  public File getSelectedFile() { return selectedFile; }

  public void setSelectedFile(File file) {
    File oldValue = selectedFile;
    selectedFile = file;
    if (selectedFile != null) {
      if (file.isAbsolute() && !getFileSystemView().isParent(getCurrentDirectory(), selectedFile)) {
        setCurrentDirectory(selectedFile.getParentFile());
      }
      if (!isMultiSelectionEnabled() || selectedFiles == null || selectedFiles.length == 1) {
        ensureFileIsVisible(selectedFile);
      }
    }
    firePropertyChange(SELECTED_FILE_CHANGED_PROPERTY, oldValue, selectedFile);
  }

  public File[] getSelectedFiles() {
    if (selectedFiles == null) {
      return new File[0];
    } else {
      return selectedFiles.clone();
    }
  }

  public void setSelectedFiles(File[] selectedFiles) {
    File[] oldValue = this.selectedFiles;
    if (selectedFiles == null || selectedFiles.length == 0) {
      selectedFiles = null;
      this.selectedFiles = null;
      setSelectedFile(null);
    } else {
      this.selectedFiles = selectedFiles.clone();
      setSelectedFile(this.selectedFiles[0]);
    }
    firePropertyChange(SELECTED_FILES_CHANGED_PROPERTY, oldValue, selectedFiles);
  }

  public File getCurrentDirectory() { return currentDirectory; }

  public void setCurrentDirectory(File dir) {
    File oldValue = currentDirectory;
    if (dir != null && !dir.exists()) {
      dir = currentDirectory;
    }
    if (dir == null) {
      dir = getFileSystemView().getDefaultDirectory();
    }
    if (currentDirectory != null) {
      if (this.currentDirectory.equals(dir)) {
        return;
      }
    }
    File prev = null;
    while (!isTraversable(dir) && prev != dir) {
      prev = dir;
      dir = getFileSystemView().getParentDirectory(dir);
    }
    currentDirectory = dir;
    firePropertyChange(DIRECTORY_CHANGED_PROPERTY, oldValue, currentDirectory);
  }

  public void changeToParentDirectory() {
    selectedFile = null;
    File oldValue = getCurrentDirectory();
    setCurrentDirectory(getFileSystemView().getParentDirectory(oldValue));
  }

  public void rescanCurrentDirectory() {
    FileChooserUI ui = getUI();
    if (ui != null) {
      ui.rescanCurrentDirectory(this);
    }
    firePropertyChange(DIRECTORY_CHANGED_PROPERTY, null, null);
  }

  public void ensureFileIsVisible(File f) {
    FileChooserUI ui = getUI();
    if (ui != null) {
      ui.ensureFileIsVisible(this, f);
    }
  }

  public int showOpenDialog(Component parent) throws HeadlessException {
    setDialogType(OPEN_DIALOG);
    return showDialog(parent, null);
  }

  public int showSaveDialog(Component parent) throws HeadlessException {
    setDialogType(SAVE_DIALOG);
    return showDialog(parent, null);
  }

  public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
    if (approveButtonText != null) {
      setApproveButtonText(approveButtonText);
      setDialogType(CUSTOM_DIALOG);
    }
    createDialog(parent);
    return returnValue;
  }

  protected JDialog createDialog(Component parent) throws HeadlessException { throw new HeadlessException(); }

  public boolean getControlButtonsAreShown() { return controlsShown; }

  public void setControlButtonsAreShown(boolean b) {
    if (controlsShown == b) {
      return;
    }
    boolean oldValue = controlsShown;
    controlsShown = b;
    firePropertyChange(CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY, oldValue, controlsShown);
  }

  public int getDialogType() { return dialogType; }

  public void setDialogType(int dialogType) {
    if (this.dialogType == dialogType) {
      return;
    }
    if (!(dialogType == OPEN_DIALOG || dialogType == SAVE_DIALOG || dialogType == CUSTOM_DIALOG)) {
      throw new IllegalArgumentException("Incorrect Dialog Type: " + dialogType);
    }
    int oldValue = this.dialogType;
    this.dialogType = dialogType;
    if (dialogType == OPEN_DIALOG || dialogType == SAVE_DIALOG) {
      setApproveButtonText(null);
    }
    firePropertyChange(DIALOG_TYPE_CHANGED_PROPERTY, oldValue, dialogType);
  }

  public void setDialogTitle(String dialogTitle) {
    String oldValue = this.dialogTitle;
    this.dialogTitle = dialogTitle;
    firePropertyChange(DIALOG_TITLE_CHANGED_PROPERTY, oldValue, dialogTitle);
  }

  public String getDialogTitle() { return dialogTitle; }

  public void setApproveButtonToolTipText(String toolTipText) {
    if (approveButtonToolTipText == toolTipText) {
      return;
    }
    String oldValue = approveButtonToolTipText;
    approveButtonToolTipText = toolTipText;
    firePropertyChange(APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY, oldValue, approveButtonToolTipText);
  }

  public String getApproveButtonToolTipText() { return approveButtonToolTipText; }

  public int getApproveButtonMnemonic() { return approveButtonMnemonic; }

  public void setApproveButtonMnemonic(int mnemonic) {
    if (approveButtonMnemonic == mnemonic) {
      return;
    }
    int oldValue = approveButtonMnemonic;
    approveButtonMnemonic = mnemonic;
    firePropertyChange(APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY, oldValue, approveButtonMnemonic);
  }

  public void setApproveButtonMnemonic(char mnemonic) {
    int vk = (int) mnemonic;
    if (vk >= 'a' && vk <= 'z') {
      vk -= ('a' - 'A');
    }
    setApproveButtonMnemonic(vk);
  }

  public void setApproveButtonText(String approveButtonText) {
    if (this.approveButtonText == approveButtonText) {
      return;
    }
    String oldValue = this.approveButtonText;
    this.approveButtonText = approveButtonText;
    firePropertyChange(APPROVE_BUTTON_TEXT_CHANGED_PROPERTY, oldValue, approveButtonText);
  }

  public String getApproveButtonText() { return approveButtonText; }

  public FileFilter[] getChoosableFileFilters() {
    FileFilter[] filterArray = new FileFilter[filters.size()];
    filters.copyInto(filterArray);
    return filterArray;
  }

  public void addChoosableFileFilter(FileFilter filter) {
    if (filter != null && !filters.contains(filter)) {
      FileFilter[] oldValue = getChoosableFileFilters();
      filters.addElement(filter);
      firePropertyChange(CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY, oldValue, getChoosableFileFilters());
      if (fileFilter == null && filters.size() == 1) {
        setFileFilter(filter);
      }
    }
  }

  public boolean removeChoosableFileFilter(FileFilter f) {
    int index = filters.indexOf(f);
    if (index >= 0) {
      if (getFileFilter() == f) {
        FileFilter aaff = getAcceptAllFileFilter();
        if (isAcceptAllFileFilterUsed() && (aaff != f)) {
          setFileFilter(aaff);
        } else if (index > 0) {
          setFileFilter(filters.get(0));
        } else if (filters.size() > 1) {
          setFileFilter(filters.get(1));
        } else {
          setFileFilter(null);
        }
      }
      FileFilter[] oldValue = getChoosableFileFilters();
      filters.removeElement(f);
      firePropertyChange(CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY, oldValue, getChoosableFileFilters());
      return true;
    } else {
      return false;
    }
  }

  public void resetChoosableFileFilters() {
    FileFilter[] oldValue = getChoosableFileFilters();
    setFileFilter(null);
    filters.removeAllElements();
    if (isAcceptAllFileFilterUsed()) {
      addChoosableFileFilter(getAcceptAllFileFilter());
    }
    firePropertyChange(CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY, oldValue, getChoosableFileFilters());
  }

  public FileFilter getAcceptAllFileFilter() {
    FileFilter filter = null;
    if (getUI() != null) {
      filter = getUI().getAcceptAllFileFilter(this);
    }
    return filter != null ? filter : acceptAll;
  }

  public boolean isAcceptAllFileFilterUsed() { return useAcceptAllFileFilter; }

  public void setAcceptAllFileFilterUsed(boolean b) {
    boolean oldValue = useAcceptAllFileFilter;
    useAcceptAllFileFilter = b;
    if (!b) {
      removeChoosableFileFilter(getAcceptAllFileFilter());
    } else {
      removeChoosableFileFilter(getAcceptAllFileFilter());
      addChoosableFileFilter(getAcceptAllFileFilter());
    }
    firePropertyChange(ACCEPT_ALL_FILE_FILTER_USED_CHANGED_PROPERTY, oldValue, useAcceptAllFileFilter);
  }

  public JComponent getAccessory() { return accessory; }

  public void setAccessory(JComponent newAccessory) {
    JComponent oldValue = accessory;
    accessory = newAccessory;
    firePropertyChange(ACCESSORY_CHANGED_PROPERTY, oldValue, accessory);
  }

  public void setFileSelectionMode(int mode) {
    if (fileSelectionMode == mode) {
      return;
    }
    if ((mode == FILES_ONLY) || (mode == DIRECTORIES_ONLY) || (mode == FILES_AND_DIRECTORIES)) {
      int oldValue = fileSelectionMode;
      fileSelectionMode = mode;
      firePropertyChange(FILE_SELECTION_MODE_CHANGED_PROPERTY, oldValue, fileSelectionMode);
    } else {
      throw new IllegalArgumentException("Incorrect Mode for file selection: " + mode);
    }
  }

  public int getFileSelectionMode() { return fileSelectionMode; }

  public boolean isFileSelectionEnabled() {
    return ((fileSelectionMode == FILES_ONLY) || (fileSelectionMode == FILES_AND_DIRECTORIES));
  }

  public boolean isDirectorySelectionEnabled() {
    return ((fileSelectionMode == DIRECTORIES_ONLY) || (fileSelectionMode == FILES_AND_DIRECTORIES));
  }

  public void setMultiSelectionEnabled(boolean b) {
    if (multiSelectionEnabled == b) {
      return;
    }
    boolean oldValue = multiSelectionEnabled;
    multiSelectionEnabled = b;
    firePropertyChange(MULTI_SELECTION_ENABLED_CHANGED_PROPERTY, oldValue, multiSelectionEnabled);
  }

  public boolean isMultiSelectionEnabled() { return multiSelectionEnabled; }

  public boolean isFileHidingEnabled() { return useFileHiding; }

  public void setFileHidingEnabled(boolean b) {
    boolean oldValue = useFileHiding;
    useFileHiding = b;
    firePropertyChange(FILE_HIDING_CHANGED_PROPERTY, oldValue, useFileHiding);
  }

  public void setFileFilter(FileFilter filter) {
    FileFilter oldValue = fileFilter;
    fileFilter = filter;
    if (filter != null) {
      if (isMultiSelectionEnabled() && selectedFiles != null && selectedFiles.length > 0) {
        Vector<File> fList = new Vector<File>();
        boolean failed = false;
        for (File file : selectedFiles) {
          if (filter.accept(file)) {
            fList.add(file);
          } else {
            failed = true;
          }
        }
        if (failed) {
          setSelectedFiles((fList.size() == 0) ? null : fList.toArray(new File[fList.size()]));
        }
      } else if (selectedFile != null && !filter.accept(selectedFile)) {
        setSelectedFile(null);
      }
    }
    firePropertyChange(FILE_FILTER_CHANGED_PROPERTY, oldValue, fileFilter);
    if (filter != null && !filters.contains(filter)) {
      addChoosableFileFilter(filter);
    }
  }

  public FileFilter getFileFilter() { return fileFilter; }

  public void setFileView(FileView fileView) {
    FileView oldValue = this.fileView;
    this.fileView = fileView;
    firePropertyChange(FILE_VIEW_CHANGED_PROPERTY, oldValue, fileView);
  }

  public FileView getFileView() { return fileView; }

  public String getName(File f) {
    String filename = null;
    if (f != null) {
      if (getFileView() != null) {
        filename = getFileView().getName(f);
      }
      if (filename == null) {
        filename = f.getName();
        if (filename.length() == 0) {
          filename = f.getPath();
        }
      }
    }
    return filename;
  }

  public String getDescription(File f) {
    String description = null;
    if (f != null) {
      if (getFileView() != null) {
        description = getFileView().getDescription(f);
      }
      if (description == null) {
        description = f.getName();
      }
    }
    return description;
  }

  public String getTypeDescription(File f) {
    String typeDescription = null;
    if (f != null) {
      if (getFileView() != null) {
        typeDescription = getFileView().getTypeDescription(f);
      }
      if (typeDescription == null) {
        typeDescription = getFileSystemView().getSystemTypeDescription(f);
      }
      if (typeDescription == null) {
        typeDescription = f.isDirectory() ? "Directory" : "Generic File";
      }
    }
    return typeDescription;
  }

  public Icon getIcon(File f) {
    Icon icon = null;
    if (f != null) {
      if (getFileView() != null) {
        icon = getFileView().getIcon(f);
      }
      if (icon == null) {
        icon = getFileSystemView().getSystemIcon(f);
      }
    }
    return icon;
  }

  public boolean isTraversable(File f) {
    Boolean traversable = null;
    if (f != null) {
      if (getFileView() != null) {
        traversable = getFileView().isTraversable(f);
      }
      if (traversable == null) {
        traversable = getFileSystemView().isTraversable(f);
      }
    }
    return (traversable != null && traversable.booleanValue());
  }

  public boolean accept(File f) {
    boolean shown = true;
    if (f != null && fileFilter != null) {
      shown = fileFilter.accept(f);
    }
    return shown;
  }

  public void setFileSystemView(FileSystemView fsv) {
    FileSystemView oldValue = fileSystemView;
    fileSystemView = fsv;
    firePropertyChange(FILE_SYSTEM_VIEW_CHANGED_PROPERTY, oldValue, fileSystemView);
  }

  public FileSystemView getFileSystemView() { return fileSystemView; }

  public void approveSelection() {
    returnValue = APPROVE_OPTION;
    fireActionPerformed(APPROVE_SELECTION);
  }

  public void cancelSelection() {
    returnValue = CANCEL_OPTION;
    fireActionPerformed(CANCEL_SELECTION);
  }

  public void addActionListener(ActionListener l) { listenerList.add(ActionListener.class, l); }

  public void removeActionListener(ActionListener l) { listenerList.remove(ActionListener.class, l); }

  public ActionListener[] getActionListeners() { return listenerList.getListeners(ActionListener.class); }

  protected void fireActionPerformed(String command) {
    Object[] listeners = listenerList.getListenerList();
    long mostRecentEventTime = EventQueue.getMostRecentEventTime();
    int modifiers = 0;
    java.awt.AWTEvent currentEvent = EventQueue.getCurrentEvent();
    if (currentEvent instanceof java.awt.event.InputEvent) {
      modifiers = ((java.awt.event.InputEvent) currentEvent).getModifiers();
    } else if (currentEvent instanceof ActionEvent) {
      modifiers = ((ActionEvent) currentEvent).getModifiers();
    }
    ActionEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ActionListener.class) {
        if (e == null) {
          e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command, mostRecentEventTime, modifiers);
        }
        ((ActionListener) listeners[i + 1]).actionPerformed(e);
      }
    }
  }

  public void updateUI() {
    setUI((FileChooserUI) UIManager.getUI(this));
    if (fileSystemView == null) {
      setFileSystemView(FileSystemView.getFileSystemView());
    }
  }

  public String getUIClassID() { return "FileChooserUI"; }

  public FileChooserUI getUI() { return (FileChooserUI) ui; }

  protected String paramString() {
    String approveButtonTextString = (approveButtonText != null ? approveButtonText : "");
    String dialogTitleString = (dialogTitle != null ? dialogTitle : "");
    String dialogTypeString;
    if (dialogType == OPEN_DIALOG) {
      dialogTypeString = "OPEN_DIALOG";
    } else if (dialogType == SAVE_DIALOG) {
      dialogTypeString = "SAVE_DIALOG";
    } else if (dialogType == CUSTOM_DIALOG) {
      dialogTypeString = "CUSTOM_DIALOG";
    } else {
      dialogTypeString = "";
    }
    String returnValueString;
    if (returnValue == CANCEL_OPTION) {
      returnValueString = "CANCEL_OPTION";
    } else if (returnValue == APPROVE_OPTION) {
      returnValueString = "APPROVE_OPTION";
    } else if (returnValue == ERROR_OPTION) {
      returnValueString = "ERROR_OPTION";
    } else {
      returnValueString = "";
    }
    String useFileHidingString = useFileHiding ? "true" : "false";
    String fileSelectionModeString;
    if (fileSelectionMode == FILES_ONLY) {
      fileSelectionModeString = "FILES_ONLY";
    } else if (fileSelectionMode == DIRECTORIES_ONLY) {
      fileSelectionModeString = "DIRECTORIES_ONLY";
    } else if (fileSelectionMode == FILES_AND_DIRECTORIES) {
      fileSelectionModeString = "FILES_AND_DIRECTORIES";
    } else {
      fileSelectionModeString = "";
    }
    return super.paramString() + ",approveButtonText=" + approveButtonTextString + ",currentDirectory="
        + (currentDirectory != null ? currentDirectory.toString() : "") + ",dialogTitle=" + dialogTitleString
        + ",dialogType=" + dialogTypeString + ",fileSelectionMode=" + fileSelectionModeString + ",returnValue="
        + returnValueString + ",selectedFile=" + (selectedFile != null ? selectedFile.toString() : "")
        + ",useFileHiding=" + useFileHidingString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJFileChooser();
    }
    return accessibleContext;
  }

  protected class AccessibleJFileChooser extends AccessibleJComponent {
    protected AccessibleJFileChooser() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.FILE_CHOOSER; }
  }
}
