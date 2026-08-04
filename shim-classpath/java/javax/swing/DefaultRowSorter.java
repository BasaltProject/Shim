/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.text.Collator;

public abstract class DefaultRowSorter<M, I> extends RowSorter<M> {
  private ModelWrapper<M, I> wrapper;
  private RowFilter<? super M, ? super I> filter;
  private List<SortKey> keys = Collections.emptyList();
  private int maxKeys = 3;
  private boolean sortsOnUpdates;
  private Comparator<?>[] comparators;
  private boolean[] sortable;
  private int[] viewToModel;
  private int[] modelToView;
  private int knownRowCount;

  public DefaultRowSorter() { }

  protected final void setModelWrapper(ModelWrapper<M, I> modelWrapper) {
    if (modelWrapper == null) {
      throw new IllegalArgumentException("modelWrapper most be non-null");
    }
    ModelWrapper<M, I> last = wrapper;
    wrapper = modelWrapper;
    if (last != null) {
      modelStructureChanged();
    } else {
      knownRowCount = wrapper.getRowCount();
    }
  }

  protected final ModelWrapper<M, I> getModelWrapper() { return wrapper; }

  public final M getModel() { return getModelWrapper().getModel(); }

  public void setSortable(int column, boolean sortable) {
    checkColumn(column);
    if (this.sortable == null) {
      this.sortable = new boolean[getModelWrapper().getColumnCount()];
      Arrays.fill(this.sortable, true);
    }
    this.sortable[column] = sortable;
  }

  public boolean isSortable(int column) {
    checkColumn(column);
    return (sortable == null) ? true : sortable[column];
  }

  public void setSortKeys(List<? extends SortKey> sortKeys) {
    List<SortKey> old = this.keys;
    if (sortKeys != null && sortKeys.size() > 0) {
      int max = getModelWrapper().getColumnCount();
      for (SortKey key : sortKeys) {
        if (key == null || key.getColumn() < 0 || key.getColumn() >= max) {
          throw new IllegalArgumentException("Invalid SortKey");
        }
      }
      this.keys = Collections.unmodifiableList(new ArrayList<SortKey>(sortKeys));
    } else {
      this.keys = Collections.emptyList();
    }
    if (!this.keys.equals(old)) {
      fireSortOrderChanged();
      sort();
    }
  }

  public List<? extends SortKey> getSortKeys() { return keys; }

  public void setMaxSortKeys(int max) {
    if (max < 1) {
      throw new IllegalArgumentException("Invalid max");
    }
    maxKeys = max;
  }

  public int getMaxSortKeys() { return maxKeys; }

  public void setSortsOnUpdates(boolean sortsOnUpdates) { this.sortsOnUpdates = sortsOnUpdates; }

  public boolean getSortsOnUpdates() { return sortsOnUpdates; }

  public void setRowFilter(RowFilter<? super M, ? super I> filter) {
    this.filter = filter;
    sort();
  }

  public RowFilter<? super M, ? super I> getRowFilter() { return filter; }

  public void toggleSortOrder(int column) {
    checkColumn(column);
    if (!isSortable(column)) {
      return;
    }
    List<SortKey> list = new ArrayList<SortKey>(getSortKeys());
    int at = -1;
    for (int i = list.size() - 1; i >= 0; i--) {
      if (list.get(i).getColumn() == column) {
        at = i;
        break;
      }
    }
    if (at == -1) {
      list.add(0, new SortKey(column, SortOrder.ASCENDING));
    } else if (at == 0) {
      SortKey first = list.get(0);
      list.set(0, new SortKey(column, first.getSortOrder() == SortOrder.ASCENDING
          ? SortOrder.DESCENDING : SortOrder.ASCENDING));
    } else {
      list.remove(at);
      list.add(0, new SortKey(column, SortOrder.ASCENDING));
    }
    if (list.size() > getMaxSortKeys()) {
      list = list.subList(0, getMaxSortKeys());
    }
    setSortKeys(list);
  }

  public int convertRowIndexToView(int index) {
    int rows = getModelWrapper().getRowCount();
    if (index < 0 || index >= rows) {
      throw new IndexOutOfBoundsException("Invalid index");
    }
    if (modelToView == null) {
      return index;
    }
    return modelToView[index];
  }

  public int convertRowIndexToModel(int index) {
    if (viewToModel == null) {
      if (index < 0 || index >= getModelWrapper().getRowCount()) {
        throw new IndexOutOfBoundsException("Invalid index");
      }
      return index;
    }
    if (index < 0 || index >= viewToModel.length) {
      throw new IndexOutOfBoundsException("Invalid index");
    }
    return viewToModel[index];
  }

  public int getViewRowCount() {
    if (viewToModel != null) {
      return viewToModel.length;
    }
    return getModelWrapper().getRowCount();
  }

  public int getModelRowCount() { return getModelWrapper().getRowCount(); }

  public void modelStructureChanged() {
    comparators = null;
    sortable = null;
    int[] last = snapshot();
    List<SortKey> kept = new ArrayList<SortKey>();
    int cols = getModelWrapper().getColumnCount();
    for (SortKey k : keys) {
      if (k.getColumn() < cols) {
        kept.add(k);
      }
    }
    if (kept.size() != keys.size()) {
      keys = Collections.unmodifiableList(kept);
      fireSortOrderChanged();
    }
    knownRowCount = getModelWrapper().getRowCount();
    recompute(last);
  }

  public void allRowsChanged() {
    int[] last = snapshot();
    knownRowCount = getModelWrapper().getRowCount();
    recompute(last);
  }

  public void rowsInserted(int firstRow, int endRow) {
    checkRange(firstRow, endRow);
    int rows = getModelWrapper().getRowCount();
    if (endRow >= rows) {
      throw new IndexOutOfBoundsException("Invalid range");
    }
    int[] last = snapshot();
    knownRowCount = rows;
    recompute(last);
  }

  public void rowsDeleted(int firstRow, int endRow) {
    checkRange(firstRow, endRow);
    int rows = getModelWrapper().getRowCount();
    if (firstRow >= knownRowCount || endRow >= knownRowCount) {
      throw new IndexOutOfBoundsException("Invalid range");
    }
    int[] last = snapshot();
    knownRowCount = rows;
    recompute(last);
  }

  public void rowsUpdated(int firstRow, int endRow) { rowsUpdated(firstRow, endRow, -1); }

  public void rowsUpdated(int firstRow, int endRow, int column) {
    checkRange(firstRow, endRow);
    checkColumn(column == -1 ? 0 : column);
    if (endRow >= getModelWrapper().getRowCount()) {
      throw new IndexOutOfBoundsException("Invalid range");
    }
    if (!getSortsOnUpdates() && filter == null) {
      return;
    }
    int[] last = snapshot();
    if (getSortsOnUpdates() || filter != null) {
      recompute(last);
    }
  }

  public void sort() {
    int[] last = snapshot();
    recompute(last);
  }

  public Comparator<?> getComparator(int column) {
    if (comparators != null) {
      return comparators[column];
    }
    return null;
  }

  public void setComparator(int column, Comparator<?> comparator) {
    checkColumn(column);
    if (comparators == null) {
      comparators = new Comparator<?>[getModelWrapper().getColumnCount()];
    }
    comparators[column] = comparator;
  }

  protected boolean useToString(int column) { return (getComparator(column) == null); }

  private int[] snapshot() { return viewToModel == null ? null : viewToModel.clone(); }

  private void checkColumn(int column) {
    if (column < 0 || column >= getModelWrapper().getColumnCount()) {
      throw new IndexOutOfBoundsException("column beyond range of TableModel");
    }
  }

  private void checkRange(int firstRow, int endRow) {
    if (firstRow < 0 || endRow < 0 || firstRow > endRow) {
      throw new IndexOutOfBoundsException("Invalid range");
    }
  }

  private boolean unsorted() {
    for (SortKey k : keys) {
      if (k.getSortOrder() != SortOrder.UNSORTED) {
        return false;
      }
    }
    return true;
  }

  private void recompute(int[] lastViewToModel) {
    int rows = getModelWrapper().getRowCount();
    boolean noSort = unsorted();
    if (noSort && filter == null) {
      if (viewToModel == null) {
        return;
      }
      viewToModel = null;
      modelToView = null;
      fireRowSorterChanged(lastViewToModel);
      return;
    }
    List<Integer> included = new ArrayList<Integer>(rows);
    for (int i = 0; i < rows; i++) {
      if (filter == null || filter.include(new EntryImpl(i))) {
        included.add(i);
      }
    }
    if (!noSort) {
      final List<SortKey> active = new ArrayList<SortKey>();
      for (SortKey k : keys) {
        if (k.getSortOrder() != SortOrder.UNSORTED) {
          active.add(k);
        }
      }
      Collections.sort(included, new Comparator<Integer>() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        public int compare(Integer a, Integer b) {
          for (SortKey k : active) {
            int col = k.getColumn();
            Object v1 = getModelWrapper().getValueAt(a, col);
            Object v2 = getModelWrapper().getValueAt(b, col);
            int result;
            if (v1 == null) {
              result = (v2 == null) ? 0 : -1;
            } else if (v2 == null) {
              result = 1;
            } else if (useToString(col)) {
              Comparator c = getComparator(col);
              Object s1 = getModelWrapper().getStringValueAt(a, col);
              Object s2 = getModelWrapper().getStringValueAt(b, col);
              result = c != null ? c.compare(s1, s2) : Collator.getInstance().compare((String) s1, (String) s2);
            } else {
              Comparator c = getComparator(col);
              result = c != null ? c.compare(v1, v2) : ((Comparable) v1).compareTo(v2);
            }
            if (k.getSortOrder() == SortOrder.DESCENDING) {
              result = -result;
            }
            if (result != 0) {
              return result;
            }
          }
          return a - b;
        }
      });
    }
    viewToModel = new int[included.size()];
    modelToView = new int[rows];
    Arrays.fill(modelToView, -1);
    for (int v = 0; v < viewToModel.length; v++) {
      viewToModel[v] = included.get(v);
      modelToView[viewToModel[v]] = v;
    }
    fireRowSorterChanged(lastViewToModel);
  }

  protected abstract static class ModelWrapper<M, I> {
    protected ModelWrapper() { }

    public abstract M getModel();

    public abstract int getColumnCount();

    public abstract int getRowCount();

    public abstract Object getValueAt(int row, int column);

    public String getStringValueAt(int row, int column) {
      Object o = getValueAt(row, column);
      if (o == null) {
        return "";
      }
      String string = o.toString();
      if (string == null) {
        return "";
      }
      return string;
    }

    public abstract I getIdentifier(int row);
  }

  private final class EntryImpl extends RowFilter.Entry<M, I> {
    private final int modelIndex;

    EntryImpl(int modelIndex) { this.modelIndex = modelIndex; }

    public M getModel() { return getModelWrapper().getModel(); }

    public int getValueCount() { return getModelWrapper().getColumnCount(); }

    public Object getValue(int index) { return getModelWrapper().getValueAt(modelIndex, index); }

    public String getStringValue(int index) { return getModelWrapper().getStringValueAt(modelIndex, index); }

    public I getIdentifier() { return getModelWrapper().getIdentifier(modelIndex); }
  }
}
