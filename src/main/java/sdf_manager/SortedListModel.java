/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author charbda
 */
class SortedListModel extends javax.swing.AbstractListModel {

  // Define a SortedSet
  private java.util.SortedSet model;

  public SortedListModel() {
    // Create a TreeSet
    // Store it in SortedSet variable
    model = new java.util.TreeSet();

  }

  // ListModel methods
  public int getSize() {
    // Return the model size
    return model.size();
  }

  public Object getElementAt(int index) {
    // Return the appropriate element

    return model.toArray()[index];
  }

  // Other methods
  public void add(Object element) {
    if (model.add(element)) {
      fireContentsChanged(this, 0, getSize());
    }
  }

  public void addAll(Object elements[]) {
    Collection c = java.util.Arrays.asList(elements);
    model.addAll(c);
    fireContentsChanged(this, 0, getSize());
  }

  public void clear() {
    model.clear();
    fireContentsChanged(this, 0, getSize());
  }

  public boolean contains(Object element) {
    return model.contains(element);
  }

  public Object firstElement() {
    // Return the appropriate element
    return model.first();
  }

  public Iterator iterator() {
    return model.iterator();
  }

  public Object lastElement() {
    // Return the appropriate element
    return model.last();
  }

  public boolean removeElement(Object element) {
    boolean removed = model.remove(element);
    if (removed) {
      fireContentsChanged(this, 0, getSize());
    }
    return removed;
  }
}

class ComboBoxSorterModel extends javax.swing.DefaultComboBoxModel {
    @Override
    public void addElement(Object object) {
        int size = getSize();
        int lstcmp = -99;
        for(int i = 0;i<size;i++) {
            lstcmp = ((Comparable)getElementAt(i)).compareTo((Comparable)object);
            if (lstcmp > 0) {
                insertElementAt(object, i);
                return;
            } else if (lstcmp == 0) return;
        }
        super.addElement(object);
    }
}


