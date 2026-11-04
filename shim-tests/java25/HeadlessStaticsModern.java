import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicIconFactory;

public class HeadlessStaticsModern {
  interface T {
    Object run() throws Throwable;
  }

  static void t(String n, T t) {
    try {
      Object o = t.run();
      System.out.println(n + " -> " + o);
    } catch (Throwable e) {
      System.out.println(n + " !! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  static String icon(Icon ic) {
    return ic.getIconWidth() + "x" + ic.getIconHeight();
  }

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    t("taskbar", () -> Taskbar.isTaskbarSupported());
    t("taskbar.get", () -> Taskbar.getTaskbar());
    t("splash", () -> SplashScreen.getSplashScreen());
    t("buttons", () -> MouseInfo.getNumberOfButtons());
    t("windows", () -> Window.getWindows().length + " " + Window.getOwnerlessWindows().length + " "
        + Frame.getFrames().length);
    t("canvas", () -> new Canvas().getBufferStrategy() + " " + new Canvas().createVolatileImage(4, 4));
    t("panel", () -> new Panel().getAccessibleContext().getClass().getName() + " "
        + new Canvas().getAccessibleContext().getClass().getName());
    t("jframe", () -> JFrame.isDefaultLookAndFeelDecorated());
    t("event", () -> {
      Event e = new Event(null, 1L, Event.KEY_PRESS, 1, 2, 65, Event.CTRL_MASK | Event.SHIFT_MASK);
      e.translate(3, 4);
      return e + " " + e.controlDown() + e.shiftDown() + e.metaDown();
    });
    t("event2", () -> new Event("t", Event.MOUSE_DOWN, "arg"));
    t("icons", () -> icon(BasicIconFactory.getMenuItemCheckIcon()) + " " + icon(BasicIconFactory.getMenuItemArrowIcon())
        + " " + icon(BasicIconFactory.getMenuArrowIcon()) + " " + icon(BasicIconFactory.getCheckBoxIcon()) + " "
        + icon(BasicIconFactory.getRadioButtonIcon()) + " " + icon(BasicIconFactory.getCheckBoxMenuItemIcon()) + " "
        + icon(BasicIconFactory.getRadioButtonMenuItemIcon()) + " " + icon(BasicIconFactory.createEmptyFrameIcon()));
    t("separator", () -> {
      JToolBar.Separator s = new JToolBar.Separator();
      return s.getUI().getClass().getName() + " " + s.getPreferredSize() + " "
          + new JToolBar.Separator(new Dimension(7, 9)).getPreferredSize();
    });
    t("metalcheck", () -> icon(new javax.swing.plaf.metal.MetalCheckBoxIcon()));
    t("desktopicon", () -> {
      JInternalFrame f = new JInternalFrame("Title", true, true, true, true);
      JInternalFrame.JDesktopIcon di = f.getDesktopIcon();
      return di.getUI().getClass().getName() + " " + di.getPreferredSize().height;
    });
  }
}
