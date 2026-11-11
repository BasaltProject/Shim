/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.UIManager;

public class XmlEncoderParity {
  public static class Bean {
    private String name = "n";
    private List<String> items = new ArrayList<>();
    private Bean child;
    private HashMap<String, Integer> map = new HashMap<>();
    public String getName() { return name; }
    public void setName(String s) { name = s; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> l) { items = l; }
    public Bean getChild() { return child; }
    public void setChild(Bean b) { child = b; }
    public HashMap<String, Integer> getMap() { return map; }
    public void setMap(HashMap<String, Integer> m) { map = m; }
  }

  static void enc(Object o) {
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    try (XMLEncoder e = new XMLEncoder(bo)) {
      e.setExceptionListener(ex -> System.out.println("EX " + ex.getClass().getName() + ": " + ex.getMessage()));
      e.writeObject(o);
    }
    System.out.println(bo.toString().replaceAll("version=\"[^\"]*\"", "version=V"));
  }

  public static void main(String[] a) {
    for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
      System.out.println(i.getName() + " " + i.getClassName());
    }
    Bean b = new Bean();
    b.setName("x");
    b.getItems().add("a");
    b.setChild(b);
    b.getMap().put("k", 1);
    enc(b);
    Object shared = new int[] {3};
    enc(new ArrayList<Object>(Arrays.asList(shared, shared, "s")));
    enc(new Point(1, 2));
    enc(new Dimension(3, 4));
    enc(new Rectangle(1, 2, 3, 4));
    enc(Thread.State.NEW);
    enc(java.net.URI.create("http://x"));
    enc(java.math.BigDecimal.ONE);
    enc(Arrays.asList(1, 2));
  }
}
