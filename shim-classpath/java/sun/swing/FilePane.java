/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicDirectoryModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class FilePane extends JPanel implements PropertyChangeListener {
  public static final String ACTION_APPROVE_SELECTION = "approveSelection";
  public static final String ACTION_CANCEL = "cancelSelection";
  public static final String ACTION_EDIT_FILE_NAME = "editFileName";
  public static final String ACTION_REFRESH = "refresh";
  public static final String ACTION_CHANGE_TO_PARENT_DIRECTORY = "Go Up";
  public static final String ACTION_NEW_FOLDER = "New Folder";
  public static final String ACTION_VIEW_LIST = "viewTypeList";
  public static final String ACTION_VIEW_DETAILS = "viewTypeDetails";
  public static final int VIEWTYPE_LIST = 0;
  public static final int VIEWTYPE_DETAILS = 1;
  private static final int VIEWTYPE_COUNT = 2;

  public interface FileChooserUIAccessor {
    JFileChooser getFileChooser();

    BasicDirectoryModel getModel();

    JPanel createList();

    JPanel createDetailsView();

    boolean isDirectorySelected();

    File getDirectory();

    Action getChangeToParentDirectoryAction();

    Action getApproveSelectionAction();

    Action getNewFolderAction();

    MouseListener createDoubleClickListener(JList<?> list);

    ListSelectionListener createListSelectionListener();
  }

  private final FileChooserUIAccessor accessor;
  private final Action[] viewActions = new Action[VIEWTYPE_COUNT];
  private final Action refreshAction;
  private final Action editAction;
  private JPanel[] viewPanels = new JPanel[VIEWTYPE_COUNT];
  private JList<Object> list;
  private JTable detailsTable;
  private int viewType = -1;
  private boolean readOnly;
  private boolean syncing;
  private ListSelectionListener selectionListener;
  private JPopupMenu contextMenu;
  private final Locale locale;

  public FilePane(FileChooserUIAccessor accessor) {
    super(new BorderLayout());
    this.accessor = accessor;
    this.locale = getFileChooser().getLocale();
    viewActions[VIEWTYPE_LIST] = new ViewTypeAction(VIEWTYPE_LIST);
    viewActions[VIEWTYPE_DETAILS] = new ViewTypeAction(VIEWTYPE_DETAILS);
    refreshAction = new AbstractAction(UIManager.getString("FileChooser.refreshActionLabelText", locale)) {
      public void actionPerformed(ActionEvent e) {
        getFileChooser().rescanCurrentDirectory();
      }
    };
    editAction = new AbstractAction(ACTION_EDIT_FILE_NAME) {
      public void actionPerformed(ActionEvent e) { }
    };
    readOnly = Boolean.TRUE.equals(UIManager.get("FileChooser.readOnly"));
    ActionMap map = super.getActionMap();
    map.put(ACTION_APPROVE_SELECTION, accessor.getApproveSelectionAction());
    map.put(ACTION_REFRESH, refreshAction);
    map.put(ACTION_EDIT_FILE_NAME, editAction);
    map.put(ACTION_CHANGE_TO_PARENT_DIRECTORY, accessor.getChangeToParentDirectoryAction());
    map.put(ACTION_NEW_FOLDER, accessor.getNewFolderAction());
    map.put(ACTION_VIEW_LIST, viewActions[VIEWTYPE_LIST]);
    map.put(ACTION_VIEW_DETAILS, viewActions[VIEWTYPE_DETAILS]);
    setPreferredSize(new Dimension(405, 135));
    getFileChooser().addPropertyChangeListener(this);
    setViewType(VIEWTYPE_LIST);
  }

  private JFileChooser getFileChooser() {
    return accessor.getFileChooser();
  }

  public int getViewType() {
    return viewType;
  }

  public void setViewType(int viewType) {
    if (viewType == this.viewType) {
      return;
    }
    int oldValue = this.viewType;
    this.viewType = viewType;
    JPanel panel = viewPanels[viewType];
    if (panel == null) {
      panel = viewType == VIEWTYPE_LIST ? accessor.createList() : accessor.createDetailsView();
      if (panel == null) {
        panel = viewType == VIEWTYPE_LIST ? createList() : createDetailsView();
      }
      viewPanels[viewType] = panel;
    }
    removeAll();
    add(panel, BorderLayout.CENTER);
    revalidate();
    repaint();
    updateSelectionFromChooser();
    firePropertyChange("viewType", oldValue, viewType);
  }

  public Action getViewTypeAction(int viewType) {
    return viewActions[viewType];
  }

  public Action[] getActions() {
    return new Action[] {viewActions[VIEWTYPE_LIST], viewActions[VIEWTYPE_DETAILS], refreshAction,
        accessor.getNewFolderAction()};
  }

  public Action getApproveSelectionAction() {
    return accessor.getApproveSelectionAction();
  }

  public JPopupMenu getContextMenu() {
    if (contextMenu == null) {
      contextMenu = new JPopupMenu();
      JMenu viewMenu = new JMenu(UIManager.getString("FileChooser.viewMenuLabelText", locale));
      for (int i = 0; i < VIEWTYPE_COUNT; i++) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(viewActions[i]);
        item.setSelected(i == viewType);
        viewMenu.add(item);
      }
      contextMenu.add(viewMenu);
      contextMenu.addSeparator();
      contextMenu.add(new JMenuItem(refreshAction));
      if (!readOnly) {
        contextMenu.add(new JMenuItem(accessor.getNewFolderAction()));
      }
    }
    return contextMenu;
  }

  public void addActionsToMenu(JPopupMenu menu) {
    for (Action action : getActions()) {
      if (action != null) {
        menu.add(new JCheckBoxMenuItem(action));
      }
    }
  }

  public JPanel createList() {
    JPanel p = new JPanel(new BorderLayout());
    list = new JList<Object>() {
      public int getNextMatch(String prefix, int startIndex, javax.swing.text.Position.Bias bias) {
        return super.getNextMatch(prefix, startIndex, bias);
      }
    };
    list.setModel((javax.swing.ListModel<Object>) (javax.swing.ListModel<?>) accessor.getModel());
    list.setLayoutOrientation(JList.VERTICAL_WRAP);
    list.setVisibleRowCount(0);
    list.setCellRenderer(new FileRenderer());
    list.setSelectionMode(getFileChooser().isMultiSelectionEnabled() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        : ListSelectionModel.SINGLE_SELECTION);
    selectionListener = accessor.createListSelectionListener();
    list.addListSelectionListener(selectionListener);
    list.addMouseListener(accessor.createDoubleClickListener(list));
    list.addMouseListener(new ContextMenuTrigger());
    list.setBackground(UIManager.getColor("List.background"));
    JScrollPane scroller = new JScrollPane(list);
    p.add(scroller, BorderLayout.CENTER);
    return p;
  }

  public JPanel createDetailsView() {
    JPanel p = new JPanel(new BorderLayout());
    detailsTable = new JTable(new FileTableModel());
    detailsTable.setShowGrid(false);
    detailsTable.setIntercellSpacing(new Dimension(1, 0));
    detailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    detailsTable.setRowHeight(Math.max(detailsTable.getFont().getSize() + 4, 17));
    detailsTable.setSelectionMode(getFileChooser().isMultiSelectionEnabled()
        ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
    detailsTable.setColumnSelectionAllowed(false);
    int[] widths = {271, 75, 130};
    TableColumn nameColumn = detailsTable.getColumnModel().getColumn(0);
    nameColumn.setCellRenderer(new DetailsNameRenderer());
    detailsTable.getColumnModel().getColumn(1).setCellRenderer(new DetailsSizeRenderer());
    detailsTable.getColumnModel().getColumn(2).setCellRenderer(new DetailsDateRenderer());
    String[] headerKeys = {"FileChooser.fileNameHeaderText", "FileChooser.fileSizeHeaderText",
        "FileChooser.fileDateHeaderText"};
    for (int i = 0; i < widths.length; i++) {
      detailsTable.getColumnModel().getColumn(i).setHeaderValue(UIManager.getString(headerKeys[i], locale));
      detailsTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
      detailsTable.getColumnModel().getColumn(i).setWidth(widths[i]);
    }
    detailsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          detailsSelectionChanged();
        }
      }
    });
    detailsTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
          int row = detailsTable.rowAtPoint(e.getPoint());
          if (row >= 0) {
            File f = (File) accessor.getModel().getElementAt(row);
            if (getFileChooser().isTraversable(f)) {
              getFileChooser().setCurrentDirectory(f);
            } else {
              getFileChooser().approveSelection();
            }
          }
        }
      }
    });
    detailsTable.addMouseListener(new ContextMenuTrigger());
    detailsTable.setBackground(UIManager.getColor("Table.background"));
    JScrollPane scroller = new JScrollPane(detailsTable);
    scroller.getViewport().setBackground(detailsTable.getBackground());
    p.add(scroller, BorderLayout.CENTER);
    return p;
  }

  private void detailsSelectionChanged() {
    if (syncing) {
      return;
    }
    JFileChooser chooser = getFileChooser();
    int[] rows = detailsTable.getSelectedRows();
    if (rows.length == 0) {
      return;
    }
    java.util.ArrayList<File> picked = new java.util.ArrayList<File>();
    for (int row : rows) {
      File f = (File) accessor.getModel().getElementAt(row);
      boolean dir = f.isDirectory();
      if ((chooser.isFileSelectionEnabled() && !dir) || (chooser.isDirectorySelectionEnabled() && dir)) {
        picked.add(f);
      }
    }
    syncing = true;
    try {
      if (chooser.isMultiSelectionEnabled()) {
        chooser.setSelectedFiles(picked.isEmpty() ? null : picked.toArray(new File[0]));
      } else if (!picked.isEmpty()) {
        chooser.setSelectedFile(picked.get(0));
      } else {
        chooser.setSelectedFile(null);
      }
    } finally {
      syncing = false;
    }
  }

  private void updateSelectionFromChooser() {
    if (syncing) {
      return;
    }
    JFileChooser chooser = getFileChooser();
    File[] files = chooser.isMultiSelectionEnabled() ? chooser.getSelectedFiles()
        : chooser.getSelectedFile() == null ? null : new File[] {chooser.getSelectedFile()};
    if (viewType == VIEWTYPE_LIST && list != null) {
      list.removeListSelectionListener(selectionListener);
      list.clearSelection();
      if (files != null) {
        for (File f : files) {
          int index = accessor.getModel().indexOf(f);
          if (index >= 0) {
            list.addSelectionInterval(index, index);
          }
        }
      }
      list.addListSelectionListener(selectionListener);
    } else if (viewType == VIEWTYPE_DETAILS && detailsTable != null && files != null) {
      syncing = true;
      try {
        detailsTable.clearSelection();
        for (File f : files) {
          int index = accessor.getModel().indexOf(f);
          if (index >= 0) {
            detailsTable.addRowSelectionInterval(index, index);
          }
        }
      } finally {
        syncing = false;
      }
    }
  }

  public void propertyChange(PropertyChangeEvent e) {
    String prop = e.getPropertyName();
    if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)
        || JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(prop)) {
      updateSelectionFromChooser();
    } else if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
      clearSelection();
    } else if (JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY.equals(prop)) {
      int mode = getFileChooser().isMultiSelectionEnabled() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
          : ListSelectionModel.SINGLE_SELECTION;
      if (list != null) {
        list.setSelectionMode(mode);
      }
      if (detailsTable != null) {
        detailsTable.setSelectionMode(mode);
      }
    } else if (JFileChooser.FILE_HIDING_CHANGED_PROPERTY.equals(prop)
        || JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(prop)) {
      clearSelection();
    }
  }

  public void clearSelection() {
    if (list != null) {
      list.clearSelection();
    }
    if (detailsTable != null) {
      detailsTable.clearSelection();
    }
  }

  public void ensureIndexIsVisible(int i) {
    if (i < 0) {
      return;
    }
    if (list != null) {
      list.ensureIndexIsVisible(i);
    }
    if (detailsTable != null) {
      Rectangle r = detailsTable.getCellRect(i, 0, true);
      detailsTable.scrollRectToVisible(r);
    }
  }

  public void rescanCurrentDirectory() {
    accessor.getModel().validateFileCache();
  }

  public void uninstallUI() {
    getFileChooser().removePropertyChangeListener(this);
    if (list != null && selectionListener != null) {
      list.removeListSelectionListener(selectionListener);
    }
  }

  public boolean isFileNameChanged() {
    return false;
  }

  private String sizeText(long length) {
    double value = length / 1024.0;
    String unitKey = "FileChooser.kiloByteString";
    String unit = "KB";
    if (value >= 1024) {
      value /= 1024;
      unit = "MB";
      if (value >= 1024) {
        value /= 1024;
        unit = "GB";
      }
    }
    String fallback = "{0} " + unit;
    String pattern = UIManager.getString(unitKey, locale);
    return MessageFormat.format(pattern != null && unit.equals("KB") ? pattern : fallback,
        new DecimalFormat("0.0", java.text.DecimalFormatSymbols.getInstance(locale)).format(value));
  }

  private final class ContextMenuTrigger extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      maybeShow(e);
    }

    public void mouseReleased(MouseEvent e) {
      maybeShow(e);
    }

    private void maybeShow(MouseEvent e) {
      if (e.isPopupTrigger()) {
        JPopupMenu menu = getContextMenu();
        menu.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  private final class ViewTypeAction extends AbstractAction {
    private final int type;

    ViewTypeAction(int type) {
      super(UIManager.getString(type == VIEWTYPE_LIST ? "FileChooser.listViewActionLabelText"
          : "FileChooser.detailsViewActionLabelText", locale));
      this.type = type;
      putValue(Action.ACTION_COMMAND_KEY, type == VIEWTYPE_LIST ? ACTION_VIEW_LIST : ACTION_VIEW_DETAILS);
    }

    public void actionPerformed(ActionEvent e) {
      setViewType(type);
    }
  }

  public class FileRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      File file = (File) value;
      JFileChooser chooser = getFileChooser();
      setText(chooser.getName(file));
      setIcon(chooser.getIcon(file));
      return this;
    }
  }

  private final class DetailsNameRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      File file = (File) value;
      JFileChooser chooser = getFileChooser();
      setText(chooser.getName(file));
      setIcon(chooser.getIcon(file));
      return this;
    }
  }

  private final class DetailsSizeRenderer extends DefaultTableCellRenderer {
    DetailsSizeRenderer() {
      setHorizontalAlignment(SwingConstants.RIGHT);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
      super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
      setText(value instanceof Long ? sizeText(((Long) value).longValue()) : "");
      return this;
    }
  }

  private final class DetailsDateRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
      super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
      setText(value instanceof Date ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale)
          .format((Date) value) : "");
      return this;
    }
  }

  private final class FileTableModel extends AbstractTableModel {
    FileTableModel() {
      accessor.getModel().addListDataListener(new javax.swing.event.ListDataListener() {
        public void contentsChanged(javax.swing.event.ListDataEvent e) {
          fireTableDataChanged();
        }

        public void intervalAdded(javax.swing.event.ListDataEvent e) {
          fireTableDataChanged();
        }

        public void intervalRemoved(javax.swing.event.ListDataEvent e) {
          fireTableDataChanged();
        }
      });
    }

    public int getRowCount() {
      return accessor.getModel().getSize();
    }

    public int getColumnCount() {
      return 3;
    }

    public Object getValueAt(int row, int column) {
      File f = (File) accessor.getModel().getElementAt(row);
      switch (column) {
        case 0:
          return f;
        case 1:
          return f.isDirectory() ? null : Long.valueOf(f.length());
        default:
          return new Date(f.lastModified());
      }
    }

    public boolean isCellEditable(int row, int column) {
      return false;
    }
  }
}
