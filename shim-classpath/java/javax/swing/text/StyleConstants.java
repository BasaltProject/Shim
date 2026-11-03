/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;

public class StyleConstants {
  public static final String ComponentElementName = "component";
  public static final String IconElementName = "icon";

  public static final Object NameAttribute = new StyleConstants("name");
  public static final Object ResolveAttribute = new StyleConstants("resolver");
  public static final Object ModelAttribute = new StyleConstants("model");
  public static final Object BidiLevel = new CharacterConstants("bidiLevel");
  public static final Object FontFamily = new FontConstants("family");
  public static final Object Family = FontFamily;
  public static final Object FontSize = new FontConstants("size");
  public static final Object Size = FontSize;
  public static final Object Bold = new FontConstants("bold");
  public static final Object Italic = new FontConstants("italic");
  public static final Object Underline = new CharacterConstants("underline");
  public static final Object StrikeThrough = new CharacterConstants("strikethrough");
  public static final Object Superscript = new CharacterConstants("superscript");
  public static final Object Subscript = new CharacterConstants("subscript");
  public static final Object Foreground = new ColorConstants("foreground");
  public static final Object Background = new ColorConstants("background");
  public static final Object ComponentAttribute = new CharacterConstants("component");
  public static final Object IconAttribute = new CharacterConstants("icon");
  public static final Object ComposedTextAttribute = new StyleConstants("composed text");
  public static final Object FirstLineIndent = new ParagraphConstants("FirstLineIndent");
  public static final Object LeftIndent = new ParagraphConstants("LeftIndent");
  public static final Object RightIndent = new ParagraphConstants("RightIndent");
  public static final Object LineSpacing = new ParagraphConstants("LineSpacing");
  public static final Object SpaceAbove = new ParagraphConstants("SpaceAbove");
  public static final Object SpaceBelow = new ParagraphConstants("SpaceBelow");
  public static final Object Alignment = new ParagraphConstants("Alignment");
  public static final Object TabSet = new ParagraphConstants("TabSet");
  public static final Object Orientation = new ParagraphConstants("Orientation");

  public static final int ALIGN_LEFT = 0;
  public static final int ALIGN_CENTER = 1;
  public static final int ALIGN_RIGHT = 2;
  public static final int ALIGN_JUSTIFIED = 3;

  private final String representation;

  StyleConstants(String representation) { this.representation = representation; }

  StyleConstants() { this(null); }

  public String toString() { return representation; }

  private static boolean flag(AttributeSet a, Object key) {
    Boolean b = (Boolean) a.getAttribute(key);
    return b != null && b.booleanValue();
  }

  private static float number(AttributeSet a, Object key) {
    Float f = (Float) a.getAttribute(key);
    return f != null ? f.floatValue() : 0f;
  }

  public static int getBidiLevel(AttributeSet a) {
    Integer o = (Integer) a.getAttribute(BidiLevel);
    return o != null ? o.intValue() : 0;
  }

  public static void setBidiLevel(MutableAttributeSet a, int o) { a.addAttribute(BidiLevel, Integer.valueOf(o)); }

  public static Component getComponent(AttributeSet a) { return (Component) a.getAttribute(ComponentAttribute); }

  public static void setComponent(MutableAttributeSet a, Component c) {
    a.addAttribute(AbstractDocument.ElementNameAttribute, ComponentElementName);
    a.addAttribute(ComponentAttribute, c);
  }

  public static Icon getIcon(AttributeSet a) { return (Icon) a.getAttribute(IconAttribute); }

  public static void setIcon(MutableAttributeSet a, Icon c) {
    a.addAttribute(AbstractDocument.ElementNameAttribute, IconElementName);
    a.addAttribute(IconAttribute, c);
  }

  public static String getFontFamily(AttributeSet a) {
    String family = (String) a.getAttribute(FontFamily);
    return family != null ? family : "Monospaced";
  }

  public static void setFontFamily(MutableAttributeSet a, String fam) { a.addAttribute(FontFamily, fam); }

  public static int getFontSize(AttributeSet a) {
    Integer size = (Integer) a.getAttribute(FontSize);
    return size != null ? size.intValue() : 12;
  }

  public static void setFontSize(MutableAttributeSet a, int s) { a.addAttribute(FontSize, Integer.valueOf(s)); }

  public static boolean isBold(AttributeSet a) { return flag(a, Bold); }

  public static void setBold(MutableAttributeSet a, boolean b) { a.addAttribute(Bold, Boolean.valueOf(b)); }

  public static boolean isItalic(AttributeSet a) { return flag(a, Italic); }

  public static void setItalic(MutableAttributeSet a, boolean b) { a.addAttribute(Italic, Boolean.valueOf(b)); }

  public static boolean isUnderline(AttributeSet a) { return flag(a, Underline); }

  public static void setUnderline(MutableAttributeSet a, boolean b) { a.addAttribute(Underline, Boolean.valueOf(b)); }

  public static boolean isStrikeThrough(AttributeSet a) { return flag(a, StrikeThrough); }

  public static void setStrikeThrough(MutableAttributeSet a, boolean b) {
    a.addAttribute(StrikeThrough, Boolean.valueOf(b));
  }

  public static boolean isSuperscript(AttributeSet a) { return flag(a, Superscript); }

  public static void setSuperscript(MutableAttributeSet a, boolean b) {
    a.addAttribute(Superscript, Boolean.valueOf(b));
  }

  public static boolean isSubscript(AttributeSet a) { return flag(a, Subscript); }

  public static void setSubscript(MutableAttributeSet a, boolean b) { a.addAttribute(Subscript, Boolean.valueOf(b)); }

  public static Color getForeground(AttributeSet a) {
    Color fg = (Color) a.getAttribute(Foreground);
    return fg != null ? fg : Color.black;
  }

  public static void setForeground(MutableAttributeSet a, Color fg) { a.addAttribute(Foreground, fg); }

  public static Color getBackground(AttributeSet a) {
    Color bg = (Color) a.getAttribute(Background);
    return bg != null ? bg : Color.black;
  }

  public static void setBackground(MutableAttributeSet a, Color fg) { a.addAttribute(Background, fg); }

  public static float getFirstLineIndent(AttributeSet a) { return number(a, FirstLineIndent); }

  public static void setFirstLineIndent(MutableAttributeSet a, float i) { a.addAttribute(FirstLineIndent, Float.valueOf(i)); }

  public static float getRightIndent(AttributeSet a) { return number(a, RightIndent); }

  public static void setRightIndent(MutableAttributeSet a, float i) { a.addAttribute(RightIndent, Float.valueOf(i)); }

  public static float getLeftIndent(AttributeSet a) { return number(a, LeftIndent); }

  public static void setLeftIndent(MutableAttributeSet a, float i) { a.addAttribute(LeftIndent, Float.valueOf(i)); }

  public static float getLineSpacing(AttributeSet a) { return number(a, LineSpacing); }

  public static void setLineSpacing(MutableAttributeSet a, float i) { a.addAttribute(LineSpacing, Float.valueOf(i)); }

  public static float getSpaceAbove(AttributeSet a) { return number(a, SpaceAbove); }

  public static void setSpaceAbove(MutableAttributeSet a, float i) { a.addAttribute(SpaceAbove, Float.valueOf(i)); }

  public static float getSpaceBelow(AttributeSet a) { return number(a, SpaceBelow); }

  public static void setSpaceBelow(MutableAttributeSet a, float i) { a.addAttribute(SpaceBelow, Float.valueOf(i)); }

  public static int getAlignment(AttributeSet a) {
    Integer align = (Integer) a.getAttribute(Alignment);
    return align != null ? align.intValue() : ALIGN_LEFT;
  }

  public static void setAlignment(MutableAttributeSet a, int align) { a.addAttribute(Alignment, Integer.valueOf(align)); }

  public static javax.swing.text.TabSet getTabSet(AttributeSet a) {
    return (javax.swing.text.TabSet) a.getAttribute(TabSet);
  }

  public static void setTabSet(MutableAttributeSet a, javax.swing.text.TabSet tabs) { a.addAttribute(TabSet, tabs); }

  public static final class CharacterConstants extends StyleConstants implements AttributeSet.CharacterAttribute {
    CharacterConstants(String representation) { super(representation); }

    CharacterConstants() { super(); }
  }

  public static final class FontConstants extends StyleConstants
      implements AttributeSet.FontAttribute, AttributeSet.CharacterAttribute {
    FontConstants(String representation) { super(representation); }

    FontConstants() { super(); }
  }

  public static final class ColorConstants extends StyleConstants
      implements AttributeSet.ColorAttribute, AttributeSet.CharacterAttribute {
    ColorConstants(String representation) { super(representation); }

    ColorConstants() { super(); }
  }

  public static final class ParagraphConstants extends StyleConstants implements AttributeSet.ParagraphAttribute {
    ParagraphConstants(String representation) { super(representation); }

    ParagraphConstants() { super(); }
  }
}
