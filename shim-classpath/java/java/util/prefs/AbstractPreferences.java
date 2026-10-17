/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.prefs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

public abstract class AbstractPreferences extends Preferences {
  private final String name;
  private final String absolutePath;
  final AbstractPreferences parent;
  private final AbstractPreferences root;
  protected boolean newNode = false;
  private final Map<String, AbstractPreferences> kidCache = new HashMap<String, AbstractPreferences>();
  private boolean removed = false;
  private final ArrayList<PreferenceChangeListener> prefListeners = new ArrayList<PreferenceChangeListener>();
  private final ArrayList<NodeChangeListener> nodeListeners = new ArrayList<NodeChangeListener>();
  protected final Object lock = new Object();
  private static final ArrayList<EventObject2> eventQueue = new ArrayList<EventObject2>();
  private static Thread eventDispatchThread;

  protected AbstractPreferences(AbstractPreferences parent, String name) {
    if (parent == null) {
      if (!name.equals("")) {
        throw new IllegalArgumentException("Root name '" + name + "' must be \"\"");
      }
      this.absolutePath = "/";
      root = this;
    } else {
      if (name.indexOf('/') != -1) {
        throw new IllegalArgumentException("Name '" + name + "' contains '/'");
      }
      if (name.equals("")) {
        throw new IllegalArgumentException("Illegal name: empty string");
      }
      root = parent.root;
      absolutePath = (parent == root ? "/" + name : parent.absolutePath() + "/" + name);
    }
    this.name = name;
    this.parent = parent;
  }

  public void put(String key, String value) {
    if (key == null || value == null) {
      throw new NullPointerException();
    }
    if (key.length() > MAX_KEY_LENGTH) {
      throw new IllegalArgumentException("Key too long: " + key);
    }
    if (value.length() > MAX_VALUE_LENGTH) {
      throw new IllegalArgumentException("Value too long: " + value);
    }
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      putSpi(key, value);
      enqueuePreferenceChangeEvent(key, value);
    }
  }

  public String get(String key, String def) {
    if (key == null) {
      throw new NullPointerException("Null key");
    }
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      String result = null;
      try {
        result = getSpi(key);
      } catch (Exception e) {
      }
      return result == null ? def : result;
    }
  }

  public void remove(String key) {
    java.util.Objects.requireNonNull(key, "Specified key cannot be null");
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      removeSpi(key);
      enqueuePreferenceChangeEvent(key, null);
    }
  }

  public void clear() throws BackingStoreException {
    synchronized (lock) {
      String[] keys = keys();
      for (int i = 0; i < keys.length; i++) {
        remove(keys[i]);
      }
    }
  }

  public void putInt(String key, int value) {
    put(key, Integer.toString(value));
  }

  public int getInt(String key, int def) {
    int result = def;
    try {
      String value = get(key, null);
      if (value != null) {
        result = Integer.parseInt(value);
      }
    } catch (NumberFormatException e) {
    }
    return result;
  }

  public void putLong(String key, long value) {
    put(key, Long.toString(value));
  }

  public long getLong(String key, long def) {
    long result = def;
    try {
      String value = get(key, null);
      if (value != null) {
        result = Long.parseLong(value);
      }
    } catch (NumberFormatException e) {
    }
    return result;
  }

  public void putBoolean(String key, boolean value) {
    put(key, String.valueOf(value));
  }

  public boolean getBoolean(String key, boolean def) {
    boolean result = def;
    String value = get(key, null);
    if (value != null) {
      if (value.equalsIgnoreCase("true")) {
        result = true;
      } else if (value.equalsIgnoreCase("false")) {
        result = false;
      }
    }
    return result;
  }

  public void putFloat(String key, float value) {
    put(key, Float.toString(value));
  }

  public float getFloat(String key, float def) {
    float result = def;
    try {
      String value = get(key, null);
      if (value != null) {
        result = Float.parseFloat(value);
      }
    } catch (NumberFormatException e) {
    }
    return result;
  }

  public void putDouble(String key, double value) {
    put(key, Double.toString(value));
  }

  public double getDouble(String key, double def) {
    double result = def;
    try {
      String value = get(key, null);
      if (value != null) {
        result = Double.parseDouble(value);
      }
    } catch (NumberFormatException e) {
    }
    return result;
  }

  public void putByteArray(String key, byte[] value) {
    put(key, Base64.getEncoder().encodeToString(value));
  }

  public byte[] getByteArray(String key, byte[] def) {
    byte[] result = def;
    String value = get(key, null);
    try {
      if (value != null) {
        result = Base64.getDecoder().decode(value);
      }
    } catch (RuntimeException e) {
    }
    return result;
  }

  public String[] keys() throws BackingStoreException {
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      return keysSpi();
    }
  }

  public String[] childrenNames() throws BackingStoreException {
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      TreeSet<String> s = new TreeSet<String>(kidCache.keySet());
      for (String kid : childrenNamesSpi()) {
        s.add(kid);
      }
      return s.toArray(new String[s.size()]);
    }
  }

  protected final AbstractPreferences[] cachedChildren() {
    return kidCache.values().toArray(new AbstractPreferences[kidCache.size()]);
  }

  public Preferences parent() {
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      return parent;
    }
  }

  public Preferences node(String path) {
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      if (path.equals("")) {
        return this;
      }
      if (path.equals("/")) {
        return root;
      }
      if (path.charAt(0) != '/') {
        return node(new StringTokenizer(path, "/", true));
      }
    }
    return root.node(new StringTokenizer(path.substring(1), "/", true));
  }

  private Preferences node(StringTokenizer path) {
    String token = path.nextToken();
    if (token.equals("/")) {
      throw new IllegalArgumentException("Consecutive slashes in path");
    }
    synchronized (lock) {
      AbstractPreferences child = kidCache.get(token);
      if (child == null) {
        if (token.length() > MAX_NAME_LENGTH) {
          throw new IllegalArgumentException("Node name " + token + " too long");
        }
        child = childSpi(token);
        if (child.newNode) {
          enqueueNodeAddedEvent(child);
        }
        kidCache.put(token, child);
      }
      if (!path.hasMoreTokens()) {
        return child;
      }
      path.nextToken();
      if (!path.hasMoreTokens()) {
        throw new IllegalArgumentException("Path ends with slash");
      }
      return child.node(path);
    }
  }

  public boolean nodeExists(String path) throws BackingStoreException {
    synchronized (lock) {
      if (path.equals("")) {
        return !removed;
      }
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      if (path.equals("/")) {
        return true;
      }
      if (path.charAt(0) != '/') {
        return nodeExists(new StringTokenizer(path, "/", true));
      }
    }
    return root.nodeExists(new StringTokenizer(path.substring(1), "/", true));
  }

  private boolean nodeExists(StringTokenizer path) throws BackingStoreException {
    String token = path.nextToken();
    if (token.equals("/")) {
      throw new IllegalArgumentException("Consecutive slashes in path");
    }
    synchronized (lock) {
      AbstractPreferences child = kidCache.get(token);
      if (child == null) {
        child = getChild(token);
      }
      if (child == null) {
        return false;
      }
      if (child.removed) {
        return false;
      }
      if (!path.hasMoreTokens()) {
        return true;
      }
      path.nextToken();
      if (!path.hasMoreTokens()) {
        throw new IllegalArgumentException("Path ends with slash");
      }
      return child.nodeExists(path);
    }
  }

  public void removeNode() throws BackingStoreException {
    if (this == root) {
      throw new UnsupportedOperationException("Can't remove the root!");
    }
    synchronized (parent.lock) {
      removeNode2();
      parent.kidCache.remove(name);
    }
  }

  private void removeNode2() throws BackingStoreException {
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node already removed.");
      }
      String[] kidNames = childrenNamesSpi();
      for (int i = 0; i < kidNames.length; i++) {
        if (!kidCache.containsKey(kidNames[i])) {
          kidCache.put(kidNames[i], childSpi(kidNames[i]));
        }
      }
      for (java.util.Iterator<AbstractPreferences> i = kidCache.values().iterator(); i.hasNext();) {
        try {
          i.next().removeNode2();
          i.remove();
        } catch (BackingStoreException x) {
        }
      }
      removeNodeSpi();
      removed = true;
      parent.enqueueNodeRemovedEvent(this);
    }
  }

  public String name() {
    return name;
  }

  public String absolutePath() {
    return absolutePath;
  }

  public boolean isUserNode() {
    return this.root == Preferences.userRoot();
  }

  public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
    if (pcl == null) {
      throw new NullPointerException("Change listener is null.");
    }
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      prefListeners.add(pcl);
    }
    startEventDispatchThreadIfNecessary();
  }

  public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      if (!prefListeners.contains(pcl)) {
        throw new IllegalArgumentException("Listener not registered.");
      }
      prefListeners.remove(pcl);
    }
  }

  public void addNodeChangeListener(NodeChangeListener ncl) {
    if (ncl == null) {
      throw new NullPointerException("Change listener is null.");
    }
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      nodeListeners.add(ncl);
    }
    startEventDispatchThreadIfNecessary();
  }

  public void removeNodeChangeListener(NodeChangeListener ncl) {
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed.");
      }
      if (!nodeListeners.contains(ncl)) {
        throw new IllegalArgumentException("Listener not registered.");
      }
      nodeListeners.remove(ncl);
    }
  }

  protected abstract void putSpi(String key, String value);

  protected abstract String getSpi(String key);

  protected abstract void removeSpi(String key);

  protected abstract void removeNodeSpi() throws BackingStoreException;

  protected abstract String[] keysSpi() throws BackingStoreException;

  protected abstract String[] childrenNamesSpi() throws BackingStoreException;

  protected AbstractPreferences getChild(String nodeName) throws BackingStoreException {
    synchronized (lock) {
      String[] kidNames = childrenNames();
      for (int i = 0; i < kidNames.length; i++) {
        if (kidNames[i].equals(nodeName)) {
          return childSpi(kidNames[i]);
        }
      }
    }
    return null;
  }

  protected abstract AbstractPreferences childSpi(String name);

  public String toString() {
    return (this.isUserNode() ? "User" : "System") + " Preference Node: " + this.absolutePath();
  }

  public void sync() throws BackingStoreException {
    sync2();
  }

  private void sync2() throws BackingStoreException {
    AbstractPreferences[] cachedKids;
    synchronized (lock) {
      if (removed) {
        throw new IllegalStateException("Node has been removed");
      }
      syncSpi();
      cachedKids = cachedChildren();
    }
    for (int i = 0; i < cachedKids.length; i++) {
      cachedKids[i].sync2();
    }
  }

  protected abstract void syncSpi() throws BackingStoreException;

  public void flush() throws BackingStoreException {
    flush2();
  }

  private void flush2() throws BackingStoreException {
    AbstractPreferences[] cachedKids;
    synchronized (lock) {
      flushSpi();
      cachedKids = cachedChildren();
    }
    for (int i = 0; i < cachedKids.length; i++) {
      cachedKids[i].flush2();
    }
  }

  protected abstract void flushSpi() throws BackingStoreException;

  boolean isRemovedForExport() {
    return isRemoved();
  }

  Object lockObject() {
    return lock;
  }

  protected boolean isRemoved() {
    synchronized (lock) {
      return removed;
    }
  }

  private static final class EventObject2 {
    final java.util.EventObject event;
    final boolean isPreference;
    final boolean added;

    EventObject2(java.util.EventObject event, boolean isPreference, boolean added) {
      this.event = event;
      this.isPreference = isPreference;
      this.added = added;
    }
  }

  private static synchronized void startEventDispatchThreadIfNecessary() {
    if (eventDispatchThread == null) {
      eventDispatchThread = new EventDispatchThread();
      eventDispatchThread.setDaemon(true);
      eventDispatchThread.start();
    }
  }

  PreferenceChangeListener[] prefListeners() {
    synchronized (lock) {
      return prefListeners.toArray(new PreferenceChangeListener[prefListeners.size()]);
    }
  }

  NodeChangeListener[] nodeListeners() {
    synchronized (lock) {
      return nodeListeners.toArray(new NodeChangeListener[nodeListeners.size()]);
    }
  }

  private void enqueuePreferenceChangeEvent(String key, String newValue) {
    if (prefListeners.size() != 0) {
      synchronized (eventQueue) {
        eventQueue.add(new EventObject2(new PreferenceChangeEvent(this, key, newValue), true, false));
        eventQueue.notify();
      }
    }
  }

  private void enqueueNodeAddedEvent(Preferences child) {
    if (nodeListeners.size() != 0) {
      synchronized (eventQueue) {
        eventQueue.add(new EventObject2(new NodeChangeEvent(this, child), false, true));
        eventQueue.notify();
      }
    }
  }

  private void enqueueNodeRemovedEvent(Preferences child) {
    if (nodeListeners.size() != 0) {
      synchronized (eventQueue) {
        eventQueue.add(new EventObject2(new NodeChangeEvent(this, child), false, false));
        eventQueue.notify();
      }
    }
  }

  public void exportNode(OutputStream os) throws IOException, BackingStoreException {
    PreferencesXml.export(os, this, false);
  }

  public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
    PreferencesXml.export(os, this, true);
  }

  private static class EventDispatchThread extends Thread {
    public void run() {
      while (true) {
        EventObject2 pending = null;
        AbstractPreferences src = null;
        synchronized (eventQueue) {
          while (eventQueue.isEmpty()) {
            try {
              eventQueue.wait();
            } catch (InterruptedException e) {
              return;
            }
          }
          pending = eventQueue.remove(0);
          src = (AbstractPreferences) pending.event.getSource();
        }
        if (pending.isPreference) {
          PreferenceChangeEvent pce = (PreferenceChangeEvent) pending.event;
          for (PreferenceChangeListener l : src.prefListeners()) {
            l.preferenceChange(pce);
          }
        } else {
          NodeChangeEvent nce = (NodeChangeEvent) pending.event;
          for (NodeChangeListener l : src.nodeListeners()) {
            if (pending.added) {
              l.childAdded(nce);
            } else {
              l.childRemoved(nce);
            }
          }
        }
      }
    }
  }
}
