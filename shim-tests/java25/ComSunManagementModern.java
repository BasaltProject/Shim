/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.management.ManagementFactory;
import com.sun.management.UnixOperatingSystemMXBean;
import com.sun.management.HotSpotDiagnosticMXBean;
public class ComSunManagementModern {
  public static void main(String[] a) throws Exception {
    java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    System.out.println(os instanceof com.sun.management.OperatingSystemMXBean);
    System.out.println(os instanceof UnixOperatingSystemMXBean);
    var o=(com.sun.management.OperatingSystemMXBean)os;
    System.out.println(o.getTotalMemorySize()>0);
    System.out.println(o.getFreeMemorySize()>0);
    System.out.println(o.getCommittedVirtualMemorySize()>0);
    System.out.println(o.getTotalSwapSpaceSize()>=0);
    System.out.println(o.getProcessCpuTime()>0);
    System.out.println(o.getCpuLoad()>=-1);
    System.out.println(o.getProcessCpuLoad()>=-1);
    var u=(UnixOperatingSystemMXBean)os;
    System.out.println(u.getOpenFileDescriptorCount()>0);
    System.out.println(u.getMaxFileDescriptorCount()>0);
    var t=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
    System.out.println(t.isThreadAllocatedMemorySupported()+" "+t.isThreadAllocatedMemoryEnabled());
    System.out.println(t.getCurrentThreadAllocatedBytes()>=0);
    System.out.println(t.getThreadAllocatedBytes(Thread.currentThread().threadId())>=0);
    var h=ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
    System.out.println(h!=null);
    if(h!=null){System.out.println(h.getDiagnosticOptions().size()>=0);
    try{h.getVMOption("NoSuch");}catch(Exception e){System.out.println(e.getClass().getName()+": "+e.getMessage());}
    try{System.out.println(h.getVMOption("MaxHeapSize").getName());}catch(Exception e){System.out.println(e.getClass().getName());}}
    boolean all = !ManagementFactory.getGarbageCollectorMXBeans().isEmpty();
    for (java.lang.management.GarbageCollectorMXBean g: ManagementFactory.getGarbageCollectorMXBeans()) all &= g instanceof com.sun.management.GarbageCollectorMXBean;
    System.out.println(all);
  }
}
