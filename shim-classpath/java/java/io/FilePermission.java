/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.io;

import java.nio.file.Paths;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public final class FilePermission extends Permission implements Serializable {
  private static final long serialVersionUID = 7930732926638008763L;

  private static final int kExecute = 0x1;
  private static final int kWrite = 0x2;
  private static final int kRead = 0x4;
  private static final int kDelete = 0x8;
  private static final int kReadlink = 0x10;
  private static final int kAll = kRead | kWrite | kExecute | kDelete | kReadlink;
  private static final String kAllFiles = "<<ALL FILES>>";

  private transient int mask;
  private transient boolean allFiles;
  private transient boolean directory;
  private transient boolean recursive;
  private transient String cpath;
  private String actions;

  private static String checkName(String path) {
    if (path == null) {
      throw new NullPointerException("name can't be null");
    }
    return path;
  }

  public FilePermission(String path, String actions) {
    super(checkName(path));
    init(parseActions(actions));
  }

  private void init(int mask) {
    if ((mask & kAll) != mask || mask == 0) {
      throw new IllegalArgumentException("invalid actions mask");
    }
    this.mask = mask;
    String path = getName();
    if (path.equals(kAllFiles)) {
      allFiles = true;
      cpath = "";
      return;
    }
    String base = path;
    if (path.equals("-") || path.endsWith("/-") || path.endsWith(File.separator + "-")) {
      recursive = true;
      directory = true;
      base = path.substring(0, path.length() - 1);
    } else if (path.equals("*") || path.endsWith("/*") || path.endsWith(File.separator + "*")) {
      directory = true;
      base = path.substring(0, path.length() - 1);
    }
    String normalized;
    try {
      normalized = Paths.get(base.isEmpty() ? "." : base).toAbsolutePath().normalize().toString();
    } catch (RuntimeException e) {
      normalized = base;
    }
    if (directory && !normalized.endsWith(File.separator)) {
      normalized = normalized + File.separator;
    }
    cpath = normalized;
  }

  private static int parseActions(String actions) {
    if (actions == null || actions.trim().isEmpty()) {
      return 0;
    }
    int mask = 0;
    for (String token : actions.split(",", -1)) {
      String a = token.trim().toLowerCase(Locale.ROOT);
      switch (a) {
        case "read":
          mask |= kRead;
          break;
        case "write":
          mask |= kWrite;
          break;
        case "execute":
          mask |= kExecute;
          break;
        case "delete":
          mask |= kDelete;
          break;
        case "readlink":
          mask |= kReadlink;
          break;
        default:
          throw new IllegalArgumentException("invalid permission: " + actions);
      }
    }
    return mask;
  }

  @Override
  public boolean implies(Permission p) {
    if (!(p instanceof FilePermission)) {
      return false;
    }
    FilePermission that = (FilePermission) p;
    return (mask & that.mask) == that.mask && impliesIgnoreMask(that);
  }

  boolean impliesIgnoreMask(FilePermission that) {
    if (allFiles) {
      return true;
    }
    if (that.allFiles) {
      return false;
    }
    if (directory) {
      if (recursive) {
        return that.cpath.startsWith(cpath) && (that.directory || that.cpath.length() > cpath.length());
      }
      if (that.directory) {
        return !that.recursive && that.cpath.equals(cpath);
      }
      return that.cpath.startsWith(cpath) && that.cpath.indexOf(File.separatorChar, cpath.length()) < 0
          && that.cpath.length() > cpath.length();
    }
    return !that.directory && cpath.equals(that.cpath);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof FilePermission)) {
      return false;
    }
    FilePermission that = (FilePermission) obj;
    return mask == that.mask && allFiles == that.allFiles && directory == that.directory
        && recursive == that.recursive && cpath.equals(that.cpath);
  }

  @Override
  public int hashCode() {
    return allFiles ? kAllFiles.hashCode() : cpath.hashCode();
  }

  int getMask() {
    return mask;
  }

  @Override
  public String getActions() {
    if (actions == null) {
      StringBuilder sb = new StringBuilder();
      String[] names = {"read", "write", "execute", "delete", "readlink"};
      int[] bits = {kRead, kWrite, kExecute, kDelete, kReadlink};
      for (int i = 0; i < bits.length; i++) {
        if ((mask & bits[i]) != 0) {
          if (sb.length() > 0) {
            sb.append(',');
          }
          sb.append(names[i]);
        }
      }
      actions = sb.toString();
    }
    return actions;
  }

  @Override
  public PermissionCollection newPermissionCollection() {
    return new FilePermissionCollection();
  }
}

final class FilePermissionCollection extends PermissionCollection implements Serializable {
  private static final long serialVersionUID = 2202956749081564585L;

  private final List<FilePermission> perms = new ArrayList<FilePermission>();

  @Override
  public void add(Permission permission) {
    if (!(permission instanceof FilePermission)) {
      throw new IllegalArgumentException("invalid permission: " + permission);
    }
    if (isReadOnly()) {
      throw new SecurityException("attempt to add a Permission to a readonly PermissionCollection");
    }
    synchronized (this) {
      perms.add((FilePermission) permission);
    }
  }

  @Override
  public boolean implies(Permission permission) {
    if (!(permission instanceof FilePermission)) {
      return false;
    }
    FilePermission fp = (FilePermission) permission;
    int desired = fp.getMask();
    int effective = 0;
    synchronized (this) {
      for (FilePermission p : perms) {
        if ((effective & desired) != desired && p.impliesIgnoreMask(fp)) {
          effective |= p.getMask();
        }
      }
    }
    return (effective & desired) == desired;
  }

  @Override
  public Enumeration<Permission> elements() {
    synchronized (this) {
      return Collections.<Permission>enumeration(new ArrayList<Permission>(perms));
    }
  }
}
