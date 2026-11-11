/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
import javax.smartcardio.*;
import java.security.*;
import java.util.*;
public class SmartcardNoService {
  static void t(String n, java.util.concurrent.Callable<Object> c) {
    try { System.out.println(n + " -> " + c.call()); } catch (Throwable e) {
      System.out.println(n + " !! " + e + " cause=" + e.getCause() + (e.getCause()!=null? " cc="+e.getCause().getCause():"")); }
  }
  public static void main(String[] a) throws Exception {
    System.out.println(TerminalFactory.getDefaultType());
    t("def", () -> TerminalFactory.getDefault());
    t("provs", () -> Arrays.toString(Security.getProviders()).length()>0 ? Security.getProvider("SunPCSC") : null);
    t("pcsc", () -> TerminalFactory.getInstance("PC/SC", null));
    t("pcsc2", () -> TerminalFactory.getInstance("PC/SC", null, "SunPCSC"));
    t("pcsc3", () -> TerminalFactory.getInstance("PC/SC", null, Security.getProvider("SunPCSC")));
    t("bad", () -> TerminalFactory.getInstance("X", null));
    t("badp", () -> TerminalFactory.getInstance("PC/SC", null, "Nope"));
    t("terms", () -> TerminalFactory.getDefault().terminals().list());
    t("terms2", () -> TerminalFactory.getDefault().terminals().list(CardTerminals.State.CARD_PRESENT));
    t("terms3", () -> TerminalFactory.getDefault().terminals().getTerminal("x"));
    t("terms4", () -> TerminalFactory.getDefault().terminals().waitForChange(10));
    t("type", () -> TerminalFactory.getDefault().getType() + TerminalFactory.getDefault().getProvider());
    t("ctl", () -> TerminalFactory.getDefault().getProvider().getInfo()+TerminalFactory.getDefault().getProvider().getVersionStr() + Collections.list(TerminalFactory.getDefault().getProvider().keys()));
    t("apdu", () -> new CommandAPDU(0,0xa4,4,0,new byte[]{1,2,3}));
    t("apdu2", () -> new CommandAPDU(new byte[]{0,1,2,3}).getNe());
    t("apdu3", () -> Arrays.toString(new CommandAPDU(0,1,2,3,300).getBytes()));
    t("apdu4", () -> new CommandAPDU(new byte[]{0,1}));
    t("apdu5", () -> new ResponseAPDU(new byte[]{1,(byte)0x90,0}).getSW());
    t("atr", () -> new ATR(new byte[]{0x3b,0x00}));
    t("perm", () -> new CardPermission("*","connect"));
    t("perm2", () -> new CardPermission("*","bad"));
    t("perm3", () -> new CardPermission("*","connect").getActions());
    t("ex", () -> new CardException("a", new Exception("b")).getCause());
  }
}
