/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 16
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.sound.midi.*;
import javax.sound.sampled.*;
import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;

public class DesktopHeadlessModern {
  static long hash(BufferedImage im) {
    long h = 7;
    for (int y = 0; y < im.getHeight(); y++) {
      for (int x = 0; x < im.getWidth(); x++) {
        h = h * 31 + im.getRGB(x, y);
      }
    }
    return h;
  }

  public static void main(String[] args) throws Exception {
    for (int type : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}) {
      BufferedImage im = new BufferedImage(7, 5, type);
      im.setRGB(1, 1, 0xff336699);
      im.setRGB(2, 3, 0x80ff8040);
      System.out.println("type " + type + " " + Integer.toHexString(im.getRGB(1, 1)) + " " + Integer.toHexString(im.getRGB(2, 3))
          + " " + im.getType() + " " + im.getColorModel().getPixelSize() + " " + im.getRaster().getNumBands());
    }

    BufferedImage canvas = new BufferedImage(40, 30, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = canvas.createGraphics();
    g.setColor(Color.RED);
    g.fillRect(2, 3, 10, 8);
    g.setColor(new Color(0, 0, 255, 128));
    g.fillRect(8, 6, 10, 8);
    g.setColor(Color.GREEN);
    g.drawLine(0, 0, 39, 29);
    g.drawRect(20, 2, 10, 10);
    g.dispose();
    System.out.println("canvas " + hash(canvas));

    BufferedImage src = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < 8; y++) {
      for (int x = 0; x < 8; x++) {
        src.setRGB(x, y, (x * 30 << 16) | (y * 30 << 8) | 100);
      }
    }
    for (String format : new String[] {"png", "bmp"}) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      System.out.println("write " + format + " " + ImageIO.write(src, format, out));
      BufferedImage back = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
      System.out.println("read " + format + " " + back.getWidth() + "x" + back.getHeight() + " " + (hash(back) == hash(src)));
    }
    System.out.println("convolve " + hash(new ConvolveOp(new Kernel(3, 3, new float[] {0, 0, 0, 0, 1, 0, 0, 0, 0})).filter(src, null)));
    byte[] inv = new byte[256];
    for (int i = 0; i < 256; i++) {
      inv[i] = (byte) (255 - i);
    }
    System.out.println("lookup " + hash(new LookupOp(new ByteLookupTable(0, inv), null).filter(src, null)));

    Path2D.Double path = new Path2D.Double();
    path.moveTo(0, 0);
    path.lineTo(10, 0);
    path.quadTo(10, 10, 0, 10);
    path.closePath();
    System.out.println("path " + path.contains(3, 3) + " " + path.contains(9, 9) + " " + path.getBounds2D());
    Area area = new Area(new Rectangle(0, 0, 10, 10));
    area.subtract(new Area(new Rectangle(5, 5, 10, 10)));
    System.out.println("area " + area.getBounds() + " " + area.contains(2, 2) + " " + area.contains(7, 7));
    System.out.println("color " + new Color(10, 20, 30).brighter() + " " + Color.decode("#12ab34"));

    Panel panel = new Panel(new BorderLayout(2, 3));
    Canvas north = new Canvas();
    Canvas west = new Canvas();
    Canvas center = new Canvas();
    north.setPreferredSize(new Dimension(50, 20));
    west.setPreferredSize(new Dimension(30, 40));
    panel.add(north, BorderLayout.NORTH);
    panel.add(west, BorderLayout.WEST);
    panel.add(center);
    panel.setSize(200, 100);
    panel.doLayout();
    System.out.println("layout " + north.getBounds() + " " + west.getBounds() + " " + center.getBounds());

    ActionEvent action = new ActionEvent("src", ActionEvent.ACTION_PERFORMED, "cmd", 1234L, ActionEvent.SHIFT_MASK);
    System.out.println("event " + action.getActionCommand() + " " + action.getWhen() + " " + action.getModifiers());
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    DefaultMutableTreeNode child = new DefaultMutableTreeNode("a");
    root.add(child);
    child.add(new DefaultMutableTreeNode("b"));
    root.add(new DefaultMutableTreeNode("c"));
    StringBuilder walk = new StringBuilder();
    for (java.util.Enumeration<?> e = root.preorderEnumeration(); e.hasMoreElements();) {
      walk.append(e.nextElement()).append(' ');
    }
    System.out.println("tree " + walk + root.getLeafCount() + " " + root.getDepth());
    DefaultTableModel table = new DefaultTableModel(new Object[][] {{1, 2}, {3, 4}}, new Object[] {"x", "y"});
    table.addRow(new Object[] {5, 6});
    System.out.println("table " + table.getRowCount() + " " + table.getColumnName(1) + " " + table.getValueAt(2, 0));
    DefaultListModel<String> list = new DefaultListModel<>();
    list.addElement("q");
    list.addElement("r");
    System.out.println("list " + list);
    UndoManager undo = new UndoManager();
    undo.addEdit(new AbstractUndoableEdit() {
      public String getPresentationName() {
        return "foo";
      }
    });
    System.out.println("undo " + undo.canUndo());

    byte[] pcm = new byte[800];
    for (int i = 0; i < 800; i += 2) {
      int v = (int) (Math.sin(i / 20.0) * 20000);
      pcm[i] = (byte) (v >> 8);
      pcm[i + 1] = (byte) v;
    }
    AudioFormat format = new AudioFormat(8000f, 16, 1, true, true);
    ByteArrayOutputStream wav = new ByteArrayOutputStream();
    System.out.println("wav " + AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(pcm), format, 400),
        AudioFileFormat.Type.WAVE, wav));
    AudioInputStream back = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav.toByteArray()));
    System.out.println("wav-read " + back.getFormat() + " " + back.getFrameLength() + " " + back.readAllBytes().length);

    Sequence sequence = new Sequence(Sequence.PPQ, 480);
    Track track = sequence.createTrack();
    track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), 0));
    track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 0), 480));
    ByteArrayOutputStream midi = new ByteArrayOutputStream();
    System.out.println("midi " + MidiSystem.write(sequence, 1, midi));
    Sequence reread = MidiSystem.getSequence(new ByteArrayInputStream(midi.toByteArray()));
    System.out.println("midi-read " + reread.getTracks().length + " " + reread.getTracks()[0].size() + " " + reread.getTickLength());
  }
}
