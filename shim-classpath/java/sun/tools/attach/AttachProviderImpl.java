/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.tools.attach;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AttachProviderImpl extends AttachProvider {
  public AttachProviderImpl() {
  }

  public String name() {
    return "sun";
  }

  public String type() {
    return "socket";
  }

  public VirtualMachine attachVirtualMachine(String vmid) throws AttachNotSupportedException, IOException {
    checkAttachPermission();
    testAttachable(vmid);
    return new VirtualMachineImpl(this, vmid);
  }

  void testAttachable(String id) throws AttachNotSupportedException {
    try {
      int pid = Integer.parseInt(id);
      if (pid < 1) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException x) {
      throw new AttachNotSupportedException("Invalid process identifier: " + id);
    }
  }

  private static void checkAttachPermission() {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(new com.sun.tools.attach.AttachPermission("attachVirtualMachine"));
    }
  }

  public List<VirtualMachineDescriptor> listVirtualMachines() {
    List<VirtualMachineDescriptor> result = new ArrayList<VirtualMachineDescriptor>();
    String user = System.getProperty("user.name");
    File tmp = new File(System.getProperty("java.io.tmpdir", "/tmp"));
    File[] dirs = tmp.listFiles();
    if (dirs == null) {
      return result;
    }
    for (File dir : dirs) {
      if (!dir.isDirectory() || !dir.getName().startsWith("hsperfdata_")) {
        continue;
      }
      File[] files = dir.listFiles();
      if (files == null) {
        continue;
      }
      for (File f : files) {
        String name = f.getName();
        boolean numeric = !name.isEmpty();
        for (int i = 0; i < name.length(); i++) {
          if (!Character.isDigit(name.charAt(i))) {
            numeric = false;
          }
        }
        if (!numeric || !new File("/proc/" + name).exists()) {
          continue;
        }
        result.add(new VirtualMachineDescriptor(this, name, displayName(name)));
      }
    }
    return result;
  }

  private static String displayName(String pid) {
    try {
      byte[] raw = Files.readAllBytes(Path.of("/proc/" + pid + "/cmdline"));
      String[] parts = new String(raw, StandardCharsets.UTF_8).split("\\u0000");
      int i = 1;
      while (i < parts.length) {
        String p = parts[i];
        if (p.equals("-cp") || p.equals("-classpath") || p.equals("--class-path") || p.equals("-p")
            || p.equals("--module-path") || p.equals("--add-modules") || p.equals("--add-opens")
            || p.equals("--add-exports")) {
          i += 2;
        } else if (p.equals("-jar")) {
          StringBuilder b = new StringBuilder(i + 1 < parts.length ? parts[i + 1] : "");
          for (int j = i + 2; j < parts.length; j++) {
            b.append(' ').append(parts[j]);
          }
          return b.toString();
        } else if (p.startsWith("-")) {
          i++;
        } else {
          break;
        }
      }
      StringBuilder b = new StringBuilder();
      for (int j = i; j < parts.length; j++) {
        if (b.length() > 0) {
          b.append(' ');
        }
        b.append(parts[j]);
      }
      return b.toString();
    } catch (IOException e) {
      return pid;
    }
  }
}
