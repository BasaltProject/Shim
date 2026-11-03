/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDirectoryModel;
import javax.swing.plaf.basic.BasicFileChooserUI;
import sun.swing.FilePane;

public class MetalFileChooserUI extends BasicFileChooserUI {
  private static final Dimension kPreferredSize = new Dimension(500, 326);
  private static final Dimension kMinimumSize = new Dimension(500, 326);
  private static final Dimension kMaximumSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  private static final Dimension kHorizontalStrut = new Dimension(5, 1);
  private static final Dimension kVerticalStrut = new Dimension(1, 5);
  private static final Insets kShrinkwrap = new Insets(0, 0, 0, 0);

  private JLabel lookInLabel;
  private JComboBox<Object> directoryComboBox;
  private DirectoryComboBoxModel directoryComboBoxModel;
  private final Action directoryComboBoxAction = new DirectoryComboBoxAction();
  private FilterComboBoxModel filterComboBoxModel;
  private JTextField fileNameTextField;
  private FilePane filePane;
  private JToggleButton listViewButton;
  private JToggleButton detailsViewButton;
  private JButton approveButton;
  private JButton cancelButton;
  private JPanel buttonPanel;
  private JPanel bottomPanel;
  private JComboBox<Object> filterComboBox;
  private AlignedLabel fileNameLabel;
  private AlignedLabel filterLabel;
  private String lookInLabelText;
  private String saveInLabelText;
  private String fileNameLabelText;
  private String folderNameLabelText;
  private String filesOfTypeLabelText;
  private String upFolderToolTipText;
  private String upFolderAccessibleName;
  private String homeFolderToolTipText;
  private String homeFolderAccessibleName;
  private String newFolderToolTipText;
  private String newFolderAccessibleName;
  private String listViewButtonToolTipText;
  private String listViewButtonAccessibleName;
  private String detailsViewButtonToolTipText;
  private String detailsViewButtonAccessibleName;
  private int lookInLabelMnemonic;
  private int fileNameLabelMnemonic;
  private int folderNameLabelMnemonic;
  private int filesOfTypeLabelMnemonic;
  private ActionMap actionMap;

  public MetalFileChooserUI(JFileChooser filechooser) {
    super(filechooser);
  }

  public static ComponentUI createUI(JComponent c) {
    return new MetalFileChooserUI((JFileChooser) c);
  }

  public void installUI(JComponent c) {
    super.installUI(c);
  }

  public void uninstallUI(JComponent c) {
    c.removePropertyChangeListener(filterComboBoxModel);
    if (approveButton != null) {
      approveButton.removeActionListener(getApproveSelectionAction());
    }
    if (fileNameTextField != null) {
      fileNameTextField.removeActionListener(getApproveSelectionAction());
    }
    if (filePane != null) {
      c.removePropertyChangeListener(filePane);
      filePane.uninstallUI();
      filePane = null;
    }
    super.uninstallUI(c);
  }

  public Dimension getPreferredSize(JComponent c) {
    int prefWidth = kPreferredSize.width;
    Dimension d = c.getLayout().preferredLayoutSize(c);
    if (d != null) {
      return new Dimension(d.width < prefWidth ? prefWidth : d.width,
          d.height < kPreferredSize.height ? kPreferredSize.height : d.height);
    }
    return new Dimension(prefWidth, kPreferredSize.height);
  }

  public Dimension getMinimumSize(JComponent c) {
    return new Dimension(kMinimumSize);
  }

  public Dimension getMaximumSize(JComponent c) {
    return new Dimension(kMaximumSize);
  }

  protected ActionMap createActionMap() {
    ActionMap map = new ActionMapUIResource();
    map.put(FilePane.ACTION_APPROVE_SELECTION, getApproveSelectionAction());
    map.put(FilePane.ACTION_CANCEL, getCancelSelectionAction());
    map.put(FilePane.ACTION_CHANGE_TO_PARENT_DIRECTORY, getChangeToParentDirectoryAction());
    map.put(FilePane.ACTION_NEW_FOLDER, getNewFolderAction());
    map.put(FilePane.ACTION_REFRESH, getUpdateAction());
    if (filePane != null) {
      map.put(FilePane.ACTION_VIEW_LIST, filePane.getViewTypeAction(FilePane.VIEWTYPE_LIST));
      map.put(FilePane.ACTION_VIEW_DETAILS, filePane.getViewTypeAction(FilePane.VIEWTYPE_DETAILS));
      map.put(FilePane.ACTION_EDIT_FILE_NAME, filePane.getActionMap().get(FilePane.ACTION_EDIT_FILE_NAME));
    }
    return map;
  }

  protected ActionMap getActionMap() {
    if (actionMap == null) {
      actionMap = createActionMap();
    }
    return actionMap;
  }

  protected void installListeners(JFileChooser fc) {
    super.installListeners(fc);
    actionMap = null;
    javax.swing.SwingUtilities.replaceUIActionMap(fc, getActionMap());
  }

  public void installComponents(JFileChooser fc) {
    FileSystemView fsv = fc.getFileSystemView();
    fc.setBorder(new EmptyBorder(12, 12, 11, 11));
    fc.setLayout(new BorderLayout(0, 11));
    filePane = new FilePane(new MetalFileChooserUIAccessor());
    fc.addPropertyChangeListener(filePane);

    JPanel topPanel = new JPanel(new BorderLayout(11, 0));
    JPanel topButtonPanel = new JPanel();
    topButtonPanel.setLayout(new BoxLayout(topButtonPanel, BoxLayout.LINE_AXIS));
    topPanel.add(topButtonPanel, BorderLayout.AFTER_LINE_ENDS);
    fc.add(topPanel, BorderLayout.NORTH);

    lookInLabel = new JLabel(lookInLabelText);
    lookInLabel.setDisplayedMnemonic(lookInLabelMnemonic);
    topPanel.add(lookInLabel, BorderLayout.BEFORE_LINE_BEGINS);

    directoryComboBoxModel = createDirectoryComboBoxModel(fc);
    directoryComboBox = new JComboBox<Object>(directoryComboBoxModel) {
      public Dimension getPreferredSize() {
        return new Dimension(150, super.getMinimumSize().height);
      }
    };
    directoryComboBox.getAccessibleContext().setAccessibleDescription(lookInLabelText);
    directoryComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    lookInLabel.setLabelFor(directoryComboBox);
    directoryComboBox.addActionListener(directoryComboBoxAction);
    directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
    directoryComboBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    directoryComboBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
    directoryComboBox.setMaximumRowCount(8);
    topPanel.add(directoryComboBox, BorderLayout.CENTER);

    JButton upFolderButton = new JButton(getChangeToParentDirectoryAction());
    upFolderButton.setText(null);
    upFolderButton.setIcon(upFolderIcon);
    upFolderButton.setToolTipText(upFolderToolTipText);
    upFolderButton.getAccessibleContext().setAccessibleName(upFolderAccessibleName);
    upFolderButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    upFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
    upFolderButton.setMargin(kShrinkwrap);
    topButtonPanel.add(upFolderButton);
    topButtonPanel.add(Box.createRigidArea(kHorizontalStrut));

    JButton homeButton = new JButton(homeFolderIcon);
    homeButton.setToolTipText(homeFolderToolTipText);
    homeButton.getAccessibleContext().setAccessibleName(homeFolderAccessibleName);
    homeButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    homeButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
    homeButton.setMargin(kShrinkwrap);
    homeButton.addActionListener(getGoHomeAction());
    topButtonPanel.add(homeButton);
    topButtonPanel.add(Box.createRigidArea(kHorizontalStrut));

    if (!Boolean.TRUE.equals(UIManager.get("FileChooser.readOnly"))) {
      JButton newFolderButton = new JButton(getNewFolderAction());
      newFolderButton.setText(null);
      newFolderButton.setIcon(newFolderIcon);
      newFolderButton.setToolTipText(newFolderToolTipText);
      newFolderButton.getAccessibleContext().setAccessibleName(newFolderAccessibleName);
      newFolderButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      newFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
      newFolderButton.setMargin(kShrinkwrap);
      topButtonPanel.add(newFolderButton);
      topButtonPanel.add(Box.createRigidArea(kHorizontalStrut));
    }

    ButtonGroup viewButtonGroup = new ButtonGroup();
    listViewButton = createViewToggle(listViewIcon, listViewButtonToolTipText, listViewButtonAccessibleName,
        FilePane.VIEWTYPE_LIST);
    topButtonPanel.add(listViewButton);
    viewButtonGroup.add(listViewButton);
    detailsViewButton = createViewToggle(detailsViewIcon, detailsViewButtonToolTipText,
        detailsViewButtonAccessibleName, FilePane.VIEWTYPE_DETAILS);
    topButtonPanel.add(detailsViewButton);
    viewButtonGroup.add(detailsViewButton);
    filePane.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if ("viewType".equals(e.getPropertyName())) {
          int viewType = filePane.getViewType();
          if (viewType == FilePane.VIEWTYPE_LIST) {
            listViewButton.setSelected(true);
          } else if (viewType == FilePane.VIEWTYPE_DETAILS) {
            detailsViewButton.setSelected(true);
          }
        }
      }
    });

    fc.add(getAccessoryPanel(), BorderLayout.EAST);
    JComponent accessory = fc.getAccessory();
    if (accessory != null) {
      getAccessoryPanel().add(accessory);
    }
    filePane.setPreferredSize(new Dimension(405, 135));
    fc.add(filePane, BorderLayout.CENTER);

    JPanel bottom = getBottomPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
    fc.add(bottom, BorderLayout.SOUTH);

    JPanel fileNamePanel = new JPanel();
    fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.LINE_AXIS));
    bottom.add(fileNamePanel);
    fileNameLabel = new AlignedLabel(fileNameLabelText);
    fileNameLabel.setDisplayedMnemonic(fileNameLabelMnemonic);
    fileNamePanel.add(fileNameLabel);
    fileNameTextField = new JTextField(35) {
      public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
      }
    };
    fileNamePanel.add(fileNameTextField);
    fileNameLabel.setLabelFor(fileNameTextField);
    fileNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent e) {
        if (!getFileChooser().isMultiSelectionEnabled()) {
          filePane.clearSelection();
        }
      }
    });
    if (fc.isMultiSelectionEnabled()) {
      setFileName(fileNameString(fc.getSelectedFiles()));
    } else {
      setFileName(fileNameString(fc.getSelectedFile()));
    }

    bottom.add(Box.createRigidArea(kVerticalStrut));
    JPanel filterPanel = new JPanel();
    filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.LINE_AXIS));
    filterLabel = new AlignedLabel(filesOfTypeLabelText);
    filterLabel.setDisplayedMnemonic(filesOfTypeLabelMnemonic);
    filterPanel.add(filterLabel);
    filterComboBoxModel = createFilterComboBoxModel();
    fc.addPropertyChangeListener(filterComboBoxModel);
    filterComboBox = new JComboBox<Object>(filterComboBoxModel);
    filterLabel.setLabelFor(filterComboBox);
    filterComboBox.setRenderer(createFilterComboBoxRenderer());
    filterPanel.add(filterComboBox);
    bottom.add(filterPanel);

    bottom.add(getButtonPanel());
    getButtonPanel().setLayout(new ButtonAreaLayout());
    approveButton = new JButton(getApproveButtonText(fc));
    approveButton.addActionListener(getApproveSelectionAction());
    approveButton.setToolTipText(getApproveButtonToolTipText(fc));
    getButtonPanel().add(approveButton);
    cancelButton = new JButton(cancelButtonText);
    cancelButton.setToolTipText(cancelButtonToolTipText);
    cancelButton.addActionListener(getCancelSelectionAction());
    getButtonPanel().add(cancelButton);
    if (!fc.getControlButtonsAreShown()) {
      removeControlButtons();
    }
    groupLabels(new AlignedLabel[] {fileNameLabel, filterLabel});
    fileNameTextField.addActionListener(getApproveSelectionAction());
    updateFileNameLabel();
    updateApproveButtonProperties(fc);
  }

  private JToggleButton createViewToggle(Icon icon, String tip, String accessibleName, final int viewType) {
    JToggleButton button = new JToggleButton(icon);
    button.setToolTipText(tip);
    button.getAccessibleContext().setAccessibleName(accessibleName);
    button.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    button.setAlignmentY(JComponent.CENTER_ALIGNMENT);
    button.setMargin(kShrinkwrap);
    button.setSelected(viewType == FilePane.VIEWTYPE_LIST);
    button.addActionListener(filePane.getViewTypeAction(viewType));
    return button;
  }

  protected JPanel getButtonPanel() {
    if (buttonPanel == null) {
      buttonPanel = new JPanel();
    }
    return buttonPanel;
  }

  protected JPanel getBottomPanel() {
    if (bottomPanel == null) {
      bottomPanel = new JPanel();
    }
    return bottomPanel;
  }

  protected void addControlButtons() {
    if (getButtonPanel() != null) {
      getButtonPanel().add(approveButton);
      getButtonPanel().add(cancelButton);
    }
  }

  protected void removeControlButtons() {
    if (getButtonPanel() != null) {
      getButtonPanel().remove(approveButton);
      getButtonPanel().remove(cancelButton);
    }
  }

  public void uninstallComponents(JFileChooser fc) {
    fc.removeAll();
    bottomPanel = null;
    buttonPanel = null;
  }

  protected void installStrings(JFileChooser fc) {
    super.installStrings(fc);
    java.util.Locale l = fc.getLocale();
    lookInLabelText = UIManager.getString("FileChooser.lookInLabelText", l);
    saveInLabelText = UIManager.getString("FileChooser.saveInLabelText", l);
    fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText", l);
    folderNameLabelText = UIManager.getString("FileChooser.folderNameLabelText", l);
    filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText", l);
    upFolderToolTipText = UIManager.getString("FileChooser.upFolderToolTipText", l);
    upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName", l);
    homeFolderToolTipText = UIManager.getString("FileChooser.homeFolderToolTipText", l);
    homeFolderAccessibleName = UIManager.getString("FileChooser.homeFolderAccessibleName", l);
    newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText", l);
    newFolderAccessibleName = UIManager.getString("FileChooser.newFolderAccessibleName", l);
    listViewButtonToolTipText = UIManager.getString("FileChooser.listViewButtonToolTipText", l);
    listViewButtonAccessibleName = UIManager.getString("FileChooser.listViewButtonAccessibleName", l);
    detailsViewButtonToolTipText = UIManager.getString("FileChooser.detailsViewButtonToolTipText", l);
    detailsViewButtonAccessibleName = UIManager.getString("FileChooser.detailsViewButtonAccessibleName", l);
    lookInLabelMnemonic = mnemonicOf("FileChooser.lookInLabelMnemonic", l);
    fileNameLabelMnemonic = mnemonicOf("FileChooser.fileNameLabelMnemonic", l);
    folderNameLabelMnemonic = mnemonicOf("FileChooser.folderNameLabelMnemonic", l);
    filesOfTypeLabelMnemonic = mnemonicOf("FileChooser.filesOfTypeLabelMnemonic", l);
  }

  private static int mnemonicOf(String key, java.util.Locale l) {
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

  private static String fileNameString(File file) {
    return file == null ? null : file.getPath();
  }

  private String fileNameString(File[] files) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; files != null && i < files.length; i++) {
      if (i > 0) {
        buf.append(' ');
      }
      if (files.length > 1) {
        buf.append('"');
      }
      buf.append(files[i].getName());
      if (files.length > 1) {
        buf.append('"');
      }
    }
    return buf.toString();
  }

  private void updateFileNameLabel() {
    if (fileNameLabel != null) {
      if (getFileChooser().getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
        fileNameLabel.setText(folderNameLabelText);
        fileNameLabel.setDisplayedMnemonic(folderNameLabelMnemonic);
      } else {
        fileNameLabel.setText(fileNameLabelText);
        fileNameLabel.setDisplayedMnemonic(fileNameLabelMnemonic);
      }
    }
  }

  private void updateApproveButtonProperties(JFileChooser fc) {
    if (approveButton != null) {
      approveButton.setText(getApproveButtonText(fc));
      approveButton.setToolTipText(getApproveButtonToolTipText(fc));
      approveButton.setMnemonic(getApproveButtonMnemonic(fc));
    }
  }

  public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) {
    return new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        String s = e.getPropertyName();
        JFileChooser chooser = getFileChooser();
        if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
          File f = (File) e.getNewValue();
          if (f != null) {
            setFileName(fileNameString(f));
          }
        } else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
          File[] files = (File[]) e.getNewValue();
          if (files != null && files.length > 0) {
            if (files.length > 1 || !files[0].isDirectory()) {
              setFileName(fileNameString(files));
            }
          }
        } else if (s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
          directoryComboBoxModel.addItem(chooser.getCurrentDirectory());
          File dir = chooser.getCurrentDirectory();
          if (dir != null && chooser.isDirectorySelectionEnabled() && !chooser.isFileSelectionEnabled()) {
            setFileName(chooser.getFileSystemView().isFileSystem(dir) ? dir.getPath() : null);
          }
        } else if (s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
          updateFileNameLabel();
          if (chooser.isDirectorySelectionEnabled() && !chooser.isFileSelectionEnabled()) {
            File dir = chooser.getCurrentDirectory();
            setFileName(dir != null && chooser.getFileSystemView().isFileSystem(dir) ? dir.getPath() : null);
          }
        } else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY)
            || s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)
            || s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)
            || s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
          updateApproveButtonProperties(chooser);
          if (s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY) && lookInLabel != null) {
            lookInLabel.setText(chooser.getDialogType() == JFileChooser.SAVE_DIALOG ? saveInLabelText
                : lookInLabelText);
          }
        } else if (s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
          if (chooser.getControlButtonsAreShown()) {
            addControlButtons();
          } else {
            removeControlButtons();
          }
        } else if (s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
          getAccessoryPanel().removeAll();
          JComponent accessory = (JComponent) e.getNewValue();
          if (accessory != null) {
            getAccessoryPanel().add(accessory, BorderLayout.CENTER);
          }
        } else if (s.equals(JFileChooser.FILE_SYSTEM_VIEW_CHANGED_PROPERTY)) {
          clearIconCache();
        } else if (s.equals("componentOrientation")) {
          chooser.applyComponentOrientation((java.awt.ComponentOrientation) e.getNewValue());
        }
      }
    };
  }

  public ListSelectionListener createListSelectionListener(JFileChooser fc) {
    return new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        MetalFileChooserUI.this.valueChanged(e);
      }
    };
  }

  public void valueChanged(ListSelectionEvent e) {
    JFileChooser fc = getFileChooser();
    File f = fc.getSelectedFile();
    if (!e.getValueIsAdjusting() && f != null && !fc.isTraversable(f)) {
      setFileName(fileNameString(f));
    }
  }

  public void setFileName(String filename) {
    if (fileNameTextField != null) {
      fileNameTextField.setText(filename);
    }
  }

  public String getFileName() {
    return fileNameTextField != null ? fileNameTextField.getText() : null;
  }

  public void setDirectoryName(String dirname) { }

  public String getDirectoryName() {
    return null;
  }

  public void ensureFileIsVisible(JFileChooser fc, File f) {
    if (filePane != null) {
      filePane.ensureIndexIsVisible(getModel().indexOf(f));
    }
  }

  public void rescanCurrentDirectory(JFileChooser fc) {
    if (filePane != null) {
      filePane.rescanCurrentDirectory();
    }
  }

  protected JPanel createList(JFileChooser fc) {
    return filePane.createList();
  }

  protected JPanel createDetailsView(JFileChooser fc) {
    return filePane.createDetailsView();
  }

  protected void setDirectorySelected(boolean directorySelected) {
    super.setDirectorySelected(directorySelected);
    JFileChooser chooser = getFileChooser();
    if (approveButton == null) {
      return;
    }
    if (directorySelected) {
      approveButton.setText(directoryOpenButtonText);
      approveButton.setToolTipText(directoryOpenButtonToolTipText);
      approveButton.setMnemonic(directoryOpenButtonMnemonic);
    } else {
      updateApproveButtonProperties(chooser);
    }
  }

  protected JButton getApproveButton(JFileChooser fc) {
    return approveButton;
  }

  protected FilterComboBoxRenderer createFilterComboBoxRenderer() {
    return new FilterComboBoxRenderer();
  }

  protected DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser fc) {
    return new DirectoryComboBoxRenderer();
  }

  protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc) {
    return new DirectoryComboBoxModel();
  }

  protected FilterComboBoxModel createFilterComboBoxModel() {
    return new FilterComboBoxModel();
  }

  private static void groupLabels(AlignedLabel[] group) {
    for (AlignedLabel label : group) {
      label.group = group;
    }
  }

  private static class AlignedLabel extends JLabel {
    private AlignedLabel[] group;

    AlignedLabel(String text) {
      super(text);
      setAlignmentX(JComponent.LEFT_ALIGNMENT);
    }

    public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      return new Dimension(maxPreferredWidth() + 11, d.height);
    }

    private int maxPreferredWidth() {
      int max = super.getPreferredSize().width;
      if (group != null) {
        for (AlignedLabel label : group) {
          max = Math.max(max, label.ownWidth());
        }
      }
      return max;
    }

    private int ownWidth() {
      return super.getPreferredSize().width;
    }
  }

  private static final class ButtonAreaLayout implements LayoutManager {
    private final int hGap = 5;
    private final int topMargin = 17;

    public void addLayoutComponent(String string, Component comp) { }

    public void layoutContainer(Container container) {
      Component[] children = container.getComponents();
      if (children == null || children.length == 0) {
        return;
      }
      int numChildren = children.length;
      Dimension[] sizes = new Dimension[numChildren];
      Insets insets = container.getInsets();
      int yLocation = insets.top + topMargin;
      int maxWidth = 0;
      for (int i = 0; i < numChildren; i++) {
        sizes[i] = children[i].getPreferredSize();
        maxWidth = Math.max(maxWidth, sizes[i].width);
      }
      int xLocation;
      int xOffset;
      if (container.getComponentOrientation().isLeftToRight()) {
        xLocation = container.getSize().width - insets.left - maxWidth;
        xOffset = hGap + maxWidth;
      } else {
        xLocation = insets.left;
        xOffset = -(hGap + maxWidth);
      }
      for (int i = numChildren - 1; i >= 0; i--) {
        children[i].setBounds(xLocation, yLocation, maxWidth, sizes[i].height);
        xLocation -= xOffset;
      }
    }

    public Dimension minimumLayoutSize(Container c) {
      if (c == null) {
        return new Dimension(0, 0);
      }
      Component[] children = c.getComponents();
      if (children == null || children.length == 0) {
        return new Dimension(0, 0);
      }
      Insets cInsets = c.getInsets();
      int maxWidth = 0;
      int maxHeight = 0;
      for (Component child : children) {
        Dimension d = child.getPreferredSize();
        maxWidth = Math.max(maxWidth, d.width);
        maxHeight = Math.max(maxHeight, d.height);
      }
      return new Dimension(children.length * maxWidth + (children.length - 1) * hGap + cInsets.left + cInsets.right,
          maxHeight + cInsets.top + cInsets.bottom + topMargin);
    }

    public Dimension preferredLayoutSize(Container c) {
      return minimumLayoutSize(c);
    }

    public void removeLayoutComponent(Component c) { }
  }

  private final class MetalFileChooserUIAccessor implements FilePane.FileChooserUIAccessor {
    public JFileChooser getFileChooser() {
      return MetalFileChooserUI.this.getFileChooser();
    }

    public BasicDirectoryModel getModel() {
      return MetalFileChooserUI.this.getModel();
    }

    public JPanel createList() {
      return null;
    }

    public JPanel createDetailsView() {
      return null;
    }

    public boolean isDirectorySelected() {
      return MetalFileChooserUI.this.isDirectorySelected();
    }

    public File getDirectory() {
      return MetalFileChooserUI.this.getDirectory();
    }

    public Action getChangeToParentDirectoryAction() {
      return MetalFileChooserUI.this.getChangeToParentDirectoryAction();
    }

    public Action getApproveSelectionAction() {
      return MetalFileChooserUI.this.getApproveSelectionAction();
    }

    public Action getNewFolderAction() {
      return MetalFileChooserUI.this.getNewFolderAction();
    }

    public java.awt.event.MouseListener createDoubleClickListener(JList<?> list) {
      return MetalFileChooserUI.this.createDoubleClickListener(getFileChooser(), list);
    }

    public ListSelectionListener createListSelectionListener() {
      return MetalFileChooserUI.this.createListSelectionListener(getFileChooser());
    }
  }

  protected class DirectoryComboBoxRenderer extends DefaultListCellRenderer {
    private final IndentIcon ii = new IndentIcon();

    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value == null) {
        setText("");
        return this;
      }
      File directory = (File) value;
      setText(getFileChooser().getName(directory));
      ii.icon = getFileChooser().getIcon(directory);
      ii.depth = directoryComboBoxModel.getDepth(index);
      setIcon(ii);
      return this;
    }
  }

  private static final class IndentIcon implements Icon {
    Icon icon;
    int depth;

    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (c.getComponentOrientation().isLeftToRight()) {
        icon.paintIcon(c, g, x + depth * 10, y);
      } else {
        icon.paintIcon(c, g, x, y);
      }
    }

    public int getIconWidth() {
      return icon.getIconWidth() + depth * 10;
    }

    public int getIconHeight() {
      return icon.getIconHeight();
    }
  }

  private final class DirectoryComboBoxAction extends javax.swing.AbstractAction {
    DirectoryComboBoxAction() {
      super("DirectoryComboBoxAction");
    }

    public void actionPerformed(ActionEvent e) {
      Object selected = directoryComboBox.getSelectedItem();
      if (selected instanceof File) {
        File f = (File) selected;
        if (!f.equals(getFileChooser().getCurrentDirectory())) {
          getFileChooser().setCurrentDirectory(f);
        }
      }
    }
  }

  protected class DirectoryComboBoxModel extends AbstractListModel<Object> implements ComboBoxModel<Object> {
    private final Vector<File> directories = new Vector<File>();
    private int[] depths;
    private File selectedDirectory;
    private final JFileChooser chooser = getFileChooser();
    private final FileSystemView fsv = chooser.getFileSystemView();

    public DirectoryComboBoxModel() {
      File dir = getFileChooser().getCurrentDirectory();
      if (dir != null) {
        addItem(dir);
      }
    }

    private void addItem(File directory) {
      if (directory == null) {
        return;
      }
      directories.clear();
      File[] baseFolders = fsv.getRoots();
      for (File base : baseFolders) {
        directories.add(base);
      }
      File canonical = directory;
      try {
        canonical = fsv.createFileObject(directory.getCanonicalPath());
      } catch (java.io.IOException ignored) {
        canonical = directory;
      }
      List<File> path = new ArrayList<File>(10);
      File f = canonical;
      do {
        path.add(f);
      } while ((f = f.getParentFile()) != null);
      int pathCount = path.size();
      for (int i = 0; i < pathCount; i++) {
        f = path.get(i);
        if (directories.contains(f)) {
          int topIndex = directories.indexOf(f);
          for (int j = i - 1; j >= 0; j--) {
            directories.insertElementAt(path.get(j), topIndex + i - j);
          }
          break;
        }
      }
      calculateDepths();
      setSelectedItem(canonical);
    }

    private void calculateDepths() {
      int size = directories.size();
      depths = new int[size];
      for (int i = 0; i < size; i++) {
        File dir = directories.get(i);
        File parent = dir.getParentFile();
        depths[i] = 0;
        if (parent != null) {
          for (int j = i - 1; j >= 0; j--) {
            if (parent.equals(directories.get(j))) {
              depths[i] = depths[j] + 1;
              break;
            }
          }
        }
      }
    }

    public int getDepth(int i) {
      return (depths != null && i >= 0 && i < depths.length) ? depths[i] : 0;
    }

    public void setSelectedItem(Object selectedDirectory) {
      this.selectedDirectory = (File) selectedDirectory;
      fireContentsChanged(this, -1, -1);
    }

    public Object getSelectedItem() {
      return selectedDirectory;
    }

    public int getSize() {
      return directories.size();
    }

    public Object getElementAt(int index) {
      return directories.elementAt(index);
    }
  }

  protected class FilterComboBoxRenderer extends DefaultListCellRenderer {
    public FilterComboBoxRenderer() { }

    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value != null && value instanceof FileFilter) {
        setText(((FileFilter) value).getDescription());
      }
      return this;
    }
  }

  protected class FilterComboBoxModel extends AbstractListModel<Object>
      implements ComboBoxModel<Object>, PropertyChangeListener {
    protected FileFilter[] filters;

    protected FilterComboBoxModel() {
      filters = getFileChooser().getChoosableFileFilters();
    }

    public void propertyChange(PropertyChangeEvent e) {
      String prop = e.getPropertyName();
      if (prop == JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
        filters = (FileFilter[]) e.getNewValue();
        fireContentsChanged(this, -1, -1);
      } else if (prop == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
        fireContentsChanged(this, -1, -1);
      }
    }

    public void setSelectedItem(Object filter) {
      if (filter != null) {
        getFileChooser().setFileFilter((FileFilter) filter);
        fireContentsChanged(this, -1, -1);
      }
    }

    public Object getSelectedItem() {
      FileFilter currentFilter = getFileChooser().getFileFilter();
      boolean found = false;
      if (currentFilter != null) {
        for (FileFilter filter : filters) {
          if (filter == currentFilter) {
            found = true;
          }
        }
        if (!found) {
          getFileChooser().addChoosableFileFilter(currentFilter);
        }
      }
      return getFileChooser().getFileFilter();
    }

    public int getSize() {
      return filters != null ? filters.length : 0;
    }

    public Object getElementAt(int index) {
      if (index > getSize() - 1) {
        return getFileChooser().getFileFilter();
      }
      return filters != null ? filters[index] : null;
    }
  }
}
