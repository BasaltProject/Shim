/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.table;

import java.text.Collator;
import java.util.Comparator;
import javax.swing.DefaultRowSorter;

public class TableRowSorter<M extends TableModel> extends DefaultRowSorter<M, Integer> {
  private static final Comparator<?> kComparable = new Comparator<Object>() {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public int compare(Object o1, Object o2) { return ((Comparable) o1).compareTo(o2); }
  };

  private TableStringConverter tableStringConverter;
  private M tableModel;
  private final ModelWrapper<M, Integer> tableWrapper = new ModelWrapper<M, Integer>() {
    public M getModel() { return tableModel; }

    public int getColumnCount() { return (tableModel == null) ? 0 : tableModel.getColumnCount(); }

    public int getRowCount() { return (tableModel == null) ? 0 : tableModel.getRowCount(); }

    public Object getValueAt(int row, int column) { return tableModel.getValueAt(row, column); }

    public String getStringValueAt(int row, int column) {
      TableStringConverter converter = getStringConverter();
      if (converter != null) {
        String value = converter.toString(tableModel, row, column);
        if (value != null) {
          return value;
        }
        return "";
      }
      return super.getStringValueAt(row, column);
    }

    public Integer getIdentifier(int index) { return index; }
  };

  public TableRowSorter() { this(null); }

  public TableRowSorter(M model) {
    setModel(model);
  }

  public void setModel(M model) {
    tableModel = model;
    setModelWrapper(tableWrapper);
  }

  public void setStringConverter(TableStringConverter stringConverter) {
    this.tableStringConverter = stringConverter;
  }

  public TableStringConverter getStringConverter() { return tableStringConverter; }

  public Comparator<?> getComparator(int column) {
    Comparator<?> comparator = super.getComparator(column);
    if (comparator != null) {
      return comparator;
    }
    Class<?> columnClass = tableModel.getColumnClass(column);
    if (columnClass == String.class) {
      return Collator.getInstance();
    }
    if (Comparable.class.isAssignableFrom(columnClass)) {
      return kComparable;
    }
    return Collator.getInstance();
  }

  protected boolean useToString(int column) {
    Comparator<?> comparator = super.getComparator(column);
    if (comparator != null) {
      return false;
    }
    Class<?> columnClass = tableModel.getColumnClass(column);
    if (columnClass == String.class) {
      return false;
    }
    if (Comparable.class.isAssignableFrom(columnClass)) {
      return false;
    }
    return true;
  }
}
