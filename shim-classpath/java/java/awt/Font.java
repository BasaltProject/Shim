/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import shim.awt.FontFace;
import shim.awt.Fonts;
import shim.awt.SimpleGlyphVector;
import shim.awt.SimpleLineMetrics;

public class Font implements java.io.Serializable {
  public static final String DIALOG = "Dialog";
  public static final String DIALOG_INPUT = "DialogInput";
  public static final String SANS_SERIF = "SansSerif";
  public static final String SERIF = "Serif";
  public static final String MONOSPACED = "Monospaced";
  public static final int PLAIN = 0;
  public static final int BOLD = 1;
  public static final int ITALIC = 2;
  public static final int ROMAN_BASELINE = 0;
  public static final int CENTER_BASELINE = 1;
  public static final int HANGING_BASELINE = 2;
  public static final int TRUETYPE_FONT = 0;
  public static final int TYPE1_FONT = 1;
  public static final int LAYOUT_LEFT_TO_RIGHT = 0;
  public static final int LAYOUT_RIGHT_TO_LEFT = 1;
  public static final int LAYOUT_NO_START_CONTEXT = 2;
  public static final int LAYOUT_NO_LIMIT_CONTEXT = 4;

  protected String name;
  protected int style;
  protected int size;
  protected float pointSize;

  private AffineTransform fontTransform;
  private Map<TextAttribute, Object> extra;
  private transient String psName;

  public Font(String name, int style, int size) {
    this.name = (name != null) ? name : "Default";
    this.style = (style & ~0x03) == 0 ? style : 0;
    this.size = size;
    this.pointSize = size;
  }

  private Font(String name, int style, float sizePts) {
    this.name = (name != null) ? name : "Default";
    this.style = (style & ~0x03) == 0 ? style : 0;
    this.size = (int) (sizePts + 0.5);
    this.pointSize = sizePts;
  }

  protected Font(Font font) {
    this.name = font.name;
    this.style = font.style;
    this.size = font.size;
    this.pointSize = font.pointSize;
    this.fontTransform = font.fontTransform;
    this.extra = font.extra;
  }

  public Font(Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
    String family = DIALOG;
    int st = PLAIN;
    float sz = 12f;
    if (attributes != null) {
      Object f = attributes.get(TextAttribute.FONT);
      Font base = null;
      if (((base = f instanceof Font ? (Font) f : null) != null)) {
        family = base.name;
        st = base.style;
        sz = base.pointSize;
      }
      Object fam = attributes.get(TextAttribute.FAMILY);
      String s = null;
      if (((s = fam instanceof String ? (String) fam : null) != null)) {
        family = s;
      }
      Object wt = attributes.get(TextAttribute.WEIGHT);
      Number n = null;
      if (((n = wt instanceof Number ? (Number) wt : null) != null) && n.floatValue() >= TextAttribute.WEIGHT_BOLD.floatValue()) {
        st |= BOLD;
      }
      Object po = attributes.get(TextAttribute.POSTURE);
      Number n2 = null;
      if (((n2 = po instanceof Number ? (Number) po : null) != null) && n2.floatValue() >= TextAttribute.POSTURE_OBLIQUE.floatValue()) {
        st |= ITALIC;
      }
      Object sizeObj = attributes.get(TextAttribute.SIZE);
      Number n3 = null;
      if (((n3 = sizeObj instanceof Number ? (Number) sizeObj : null) != null)) {
        sz = n3.floatValue();
      }
      Object tr = attributes.get(TextAttribute.TRANSFORM);
      AffineTransform at = null;
      if (((at = tr instanceof AffineTransform ? (AffineTransform) tr : null) != null) && !at.isIdentity()) {
        fontTransform = new AffineTransform(at);
      }
    }
    this.name = family;
    this.style = st;
    this.pointSize = sz;
    this.size = (int) (sz + 0.5);
  }

  private FontFace face() {
    return Fonts.face(name, style);
  }

  public Map<TextAttribute, ?> getAttributes() {
    HashMap<TextAttribute, Object> m = new HashMap<>();
    m.put(TextAttribute.FAMILY, getFamily());
    m.put(TextAttribute.WEIGHT, isBold() ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
    m.put(TextAttribute.POSTURE, isItalic() ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
    m.put(TextAttribute.SIZE, pointSize);
    m.put(TextAttribute.TRANSFORM, fontTransform == null ? new java.awt.font.TransformAttribute(new AffineTransform())
        : new java.awt.font.TransformAttribute(fontTransform));
    m.put(TextAttribute.SUPERSCRIPT, 0);
    m.put(TextAttribute.WIDTH, TextAttribute.WIDTH_REGULAR);
    return m;
  }

  public AttributedCharacterIterator.Attribute[] getAvailableAttributes() {
    return new AttributedCharacterIterator.Attribute[] {TextAttribute.FAMILY, TextAttribute.WEIGHT,
        TextAttribute.WIDTH, TextAttribute.POSTURE, TextAttribute.SIZE, TextAttribute.TRANSFORM,
        TextAttribute.SUPERSCRIPT, TextAttribute.CHAR_REPLACEMENT, TextAttribute.FOREGROUND,
        TextAttribute.BACKGROUND, TextAttribute.UNDERLINE, TextAttribute.STRIKETHROUGH, TextAttribute.RUN_DIRECTION,
        TextAttribute.BIDI_EMBEDDING, TextAttribute.JUSTIFICATION, TextAttribute.INPUT_METHOD_HIGHLIGHT,
        TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.SWAP_COLORS, TextAttribute.NUMERIC_SHAPING,
        TextAttribute.KERNING, TextAttribute.LIGATURES, TextAttribute.TRACKING};
  }

  public static Font createFont(int fontFormat, InputStream fontStream) throws FontFormatException, IOException {
    if (fontFormat != TRUETYPE_FONT && fontFormat != TYPE1_FONT) {
      throw new IllegalArgumentException("font format not recognized");
    }
    byte[] data = fontStream.readAllBytes();
    FontFace face = shim.awt.TrueTypeFace.parse(data, 0);
    String family = face.family();
    Fonts.register(family, face);
    return new Font(family, PLAIN, 1f);
  }

  public static Font[] createFonts(InputStream fontStream) throws FontFormatException, IOException {
    return new Font[] {createFont(TRUETYPE_FONT, fontStream)};
  }

  public static Font createFont(int fontFormat, File fontFile) throws FontFormatException, IOException {
    if (fontFormat != TRUETYPE_FONT && fontFormat != TYPE1_FONT) {
      throw new IllegalArgumentException("font format not recognized");
    }
    try (java.io.FileInputStream in = new java.io.FileInputStream(fontFile)) {
      return createFont(fontFormat, in);
    }
  }

  public static Font[] createFonts(File fontFile) throws FontFormatException, IOException {
    return new Font[] {createFont(TRUETYPE_FONT, fontFile)};
  }

  public boolean hasLayoutAttributes() {
    return false;
  }

  public static Font getFont(Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
    Object f = attributes.get(TextAttribute.FONT);
    Font font = null;
    if (((font = f instanceof Font ? (Font) f : null) != null)) {
      return font;
    }
    return new Font(attributes);
  }

  public static boolean textRequiresLayout(char[] chars, int start, int end) {
    return false;
  }

  public Font deriveFont(int style, float size) {
    Font f = new Font(name, style, size);
    f.fontTransform = fontTransform;
    return f;
  }

  public Font deriveFont(float size) {
    Font f = new Font(name, style, size);
    f.fontTransform = fontTransform;
    return f;
  }

  public Font deriveFont(AffineTransform trans) {
    if (trans == null) {
      throw new IllegalArgumentException("transform must not be null");
    }
    Font f = new Font(name, style, pointSize);
    f.fontTransform = trans.isIdentity() ? null : new AffineTransform(trans);
    return f;
  }

  public Font deriveFont(int style, AffineTransform trans) {
    if (trans == null) {
      throw new IllegalArgumentException("transform must not be null");
    }
    Font f = new Font(name, style, pointSize);
    f.fontTransform = trans.isIdentity() ? null : new AffineTransform(trans);
    return f;
  }

  public Font deriveFont(int style) {
    Font f = new Font(name, style, pointSize);
    f.fontTransform = fontTransform;
    return f;
  }

  public Font deriveFont(Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
    if (attributes == null) {
      return this;
    }
    HashMap<AttributedCharacterIterator.Attribute, Object> merged = new HashMap<>(getAttributes());
    merged.putAll(attributes);
    Font f = new Font(merged);
    if (!attributes.containsKey(TextAttribute.FAMILY)) {
      f.name = name;
    }
    return f;
  }

  public String getFamily() {
    return getFamily(Locale.getDefault());
  }

  public String getFamily(Locale l) {
    if (l == null) {
      throw new NullPointerException("null locale doesn't mean default");
    }
    String logical = Fonts.logicalFamily(name);
    if (logical != null) {
      return logical;
    }
    if (Fonts.isKnown(name)) {
      return face().family();
    }
    return DIALOG;
  }

  public String getName() {
    return name;
  }

  public String getFontName() {
    return getFontName(Locale.getDefault());
  }

  public String getFontName(Locale l) {
    if (l == null) {
      throw new NullPointerException("null locale doesn't mean default");
    }
    String sys = Fonts.fullName(name, style);
    if (sys != null) {
      return sys;
    }
    String fam = getFamily(l);
    boolean logical = Fonts.logicalFamily(name) != null || !Fonts.isKnown(name);
    if (!logical) {
      return fam + (style == PLAIN ? "" : isBold() && isItalic() ? " Bold Italic" : isBold() ? " Bold" : " Italic");
    }
    return fam + "." + styleName();
  }

  private String styleName() {
    return isBold() ? (isItalic() ? "bolditalic" : "bold") : (isItalic() ? "italic" : "plain");
  }

  public String getPSName() {
    if (psName == null) {
      psName = Fonts.postScriptName(name, style);
    }
    if (psName == null) {
      psName = getFontName(Locale.ENGLISH).replace(' ', '-');
    }
    return psName;
  }

  public int getStyle() {
    return style;
  }

  public int getSize() {
    return size;
  }

  public float getSize2D() {
    return pointSize;
  }

  public boolean isPlain() {
    return style == 0;
  }

  public boolean isBold() {
    return (style & BOLD) != 0;
  }

  public boolean isItalic() {
    return (style & ITALIC) != 0;
  }

  public boolean isTransformed() {
    return fontTransform != null;
  }

  public AffineTransform getTransform() {
    if (fontTransform != null) {
      return new AffineTransform(fontTransform);
    }
    return new AffineTransform();
  }

  public static Font getFont(String nm) {
    return getFont(nm, null);
  }

  public static Font getFont(String nm, Font font) {
    String str = null;
    try {
      str = System.getProperty(nm);
    } catch (SecurityException e) {
      // fall through
    }
    if (str == null) {
      return font;
    }
    return decode(str);
  }

  public static Font decode(String str) {
    String fontName = str;
    String styleName = "";
    int fontSize = 12;
    int fontStyle = Font.PLAIN;
    if (str == null) {
      return new Font(DIALOG, fontStyle, fontSize);
    }
    int lastHyphen = str.lastIndexOf('-');
    int lastSpace = str.lastIndexOf(' ');
    char sepChar = (lastHyphen > lastSpace) ? '-' : ' ';
    int sizeIndex = str.lastIndexOf(sepChar);
    int styleIndex = str.lastIndexOf(sepChar, sizeIndex - 1);
    int strlen = str.length();
    if (sizeIndex > 0 && sizeIndex + 1 < strlen) {
      try {
        fontSize = Integer.valueOf(str.substring(sizeIndex + 1)).intValue();
        if (fontSize <= 0) {
          fontSize = 12;
        }
      } catch (NumberFormatException e) {
        styleIndex = sizeIndex;
        sizeIndex = strlen;
        if (str.charAt(sizeIndex - 1) == sepChar) {
          sizeIndex--;
        }
      }
    }
    if (styleIndex >= 0 && styleIndex + 1 < strlen) {
      styleName = str.substring(styleIndex + 1, sizeIndex);
      styleName = styleName.toLowerCase(Locale.ENGLISH);
      if (styleName.equals("bolditalic")) {
        fontStyle = Font.BOLD | Font.ITALIC;
      } else if (styleName.equals("italic")) {
        fontStyle = Font.ITALIC;
      } else if (styleName.equals("bold")) {
        fontStyle = Font.BOLD;
      } else if (styleName.equals("plain")) {
        fontStyle = Font.PLAIN;
      } else {
        styleIndex = sizeIndex;
        if (str.charAt(styleIndex - 1) == sepChar) {
          styleIndex--;
        }
      }
      fontName = str.substring(0, styleIndex);
    } else {
      int fontEnd = strlen;
      if (styleIndex > 0) {
        fontEnd = styleIndex;
      } else if (sizeIndex > 0) {
        fontEnd = sizeIndex;
      }
      if (fontEnd > 0 && str.charAt(fontEnd - 1) == sepChar) {
        fontEnd--;
      }
      fontName = str.substring(0, fontEnd);
    }
    return new Font(fontName, fontStyle, fontSize);
  }

  public int hashCode() {
    return name.hashCode() ^ style ^ size;
  }

  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    Font font2 = null;
    if (((font2 = obj instanceof Font ? (Font) obj : null) != null)) {
      return size == font2.size && style == font2.style && pointSize == font2.pointSize && name.equals(font2.name)
          && java.util.Objects.equals(fontTransform, font2.fontTransform);
    }
    return false;
  }

  public String toString() {
    String strStyle;
    if (isBold()) {
      strStyle = isItalic() ? "bolditalic" : "bold";
    } else {
      strStyle = isItalic() ? "italic" : "plain";
    }
    return getClass().getName() + "[family=" + getFamily() + ",name=" + name + ",style=" + strStyle + ",size=" + size
        + "]";
  }

  public int getNumGlyphs() {
    return face().numGlyphs();
  }

  public int getMissingGlyphCode() {
    return face().missingGlyph();
  }

  public byte getBaselineFor(char c) {
    return ROMAN_BASELINE;
  }

  public float getItalicAngle() {
    return isItalic() ? -0.2f : 0f;
  }

  public boolean hasUniformLineMetrics() {
    return false;
  }

  public boolean canDisplay(char c) {
    return face().canDisplay(c);
  }

  public boolean canDisplay(int codePoint) {
    if (!Character.isValidCodePoint(codePoint)) {
      throw new IllegalArgumentException("invalid code point: " + Integer.toHexString(codePoint));
    }
    return face().canDisplay(codePoint);
  }

  public int canDisplayUpTo(String str) {
    return canDisplayUpTo(str.toCharArray(), 0, str.length());
  }

  public int canDisplayUpTo(char[] text, int start, int limit) {
    for (int i = start; i < limit; i++) {
      if (!canDisplay(text[i])) {
        return i;
      }
    }
    return -1;
  }

  public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
    int index = 0;
    for (char c = iter.setIndex(start); iter.getIndex() < limit; c = iter.next()) {
      if (!canDisplay(c)) {
        return iter.getIndex();
      }
      index++;
    }
    return -1;
  }

  public LineMetrics getLineMetrics(String str, FontRenderContext frc) {
    return new SimpleLineMetrics(face(), pointSize, str.length());
  }

  public LineMetrics getLineMetrics(String str, int beginIndex, int limit, FontRenderContext frc) {
    return new SimpleLineMetrics(face(), pointSize, limit - beginIndex);
  }

  public LineMetrics getLineMetrics(char[] chars, int beginIndex, int limit, FontRenderContext frc) {
    return new SimpleLineMetrics(face(), pointSize, limit - beginIndex);
  }

  public LineMetrics getLineMetrics(CharacterIterator ci, int beginIndex, int limit, FontRenderContext frc) {
    return new SimpleLineMetrics(face(), pointSize, limit - beginIndex);
  }

  private Rectangle2D bounds(String s, FontRenderContext frc) {
    FontFace f = face();
    boolean fm = frc != null && frc.usesFractionalMetrics();
    float width = 0;
    for (int i = 0; i < s.length();) {
      int cp = s.codePointAt(i);
      width += f.pixelAdvance(cp, pointSize, fm, frc == null || !frc.isAntiAliased());
      i += Character.charCount(cp);
    }
    float asc = f.ascent() * pointSize;
    return new Rectangle2D.Float(0, -asc, width, asc + (f.descent() + f.leading()) * pointSize);
  }

  public Rectangle2D getStringBounds(String str, FontRenderContext frc) {
    if (str == null) {
      throw new NullPointerException("str is null");
    }
    return bounds(str, frc);
  }

  public Rectangle2D getStringBounds(String str, int beginIndex, int limit, FontRenderContext frc) {
    if (beginIndex < 0) {
      throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
    }
    if (limit > str.length()) {
      throw new IndexOutOfBoundsException("limit: " + limit);
    }
    if (beginIndex > limit) {
      throw new IndexOutOfBoundsException("range length: " + (limit - beginIndex));
    }
    return bounds(str.substring(beginIndex, limit), frc);
  }

  public Rectangle2D getStringBounds(char[] chars, int beginIndex, int limit, FontRenderContext frc) {
    if (beginIndex < 0) {
      throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
    }
    if (limit > chars.length) {
      throw new IndexOutOfBoundsException("limit: " + limit);
    }
    if (beginIndex > limit) {
      throw new IndexOutOfBoundsException("range length: " + (limit - beginIndex));
    }
    return bounds(new String(chars, beginIndex, limit - beginIndex), frc);
  }

  public Rectangle2D getStringBounds(CharacterIterator ci, int beginIndex, int limit, FontRenderContext frc) {
    int start = ci.getBeginIndex();
    int end = ci.getEndIndex();
    if (beginIndex < start) {
      throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
    }
    if (limit > end) {
      throw new IndexOutOfBoundsException("limit: " + limit);
    }
    if (beginIndex > limit) {
      throw new IndexOutOfBoundsException("range length: " + (limit - beginIndex));
    }
    StringBuilder sb = new StringBuilder();
    for (char c = ci.setIndex(beginIndex); ci.getIndex() < limit; c = ci.next()) {
      sb.append(c);
    }
    return bounds(sb.toString(), frc);
  }

  public Rectangle2D getMaxCharBounds(FontRenderContext frc) {
    return bounds("W", frc);
  }

  public GlyphVector createGlyphVector(FontRenderContext frc, String str) {
    int[] cps = str.codePoints().toArray();
    int[] idx = new int[cps.length];
    int pos = 0;
    for (int i = 0; i < cps.length; i++) {
      idx[i] = pos;
      pos += Character.charCount(cps[i]);
    }
    return new SimpleGlyphVector(this, frc, cps, idx);
  }

  public GlyphVector createGlyphVector(FontRenderContext frc, CharacterIterator ci) {
    StringBuilder sb = new StringBuilder();
    for (char c = ci.first(); c != CharacterIterator.DONE; c = ci.next()) {
      sb.append(c);
    }
    return createGlyphVector(frc, sb.toString());
  }

  public GlyphVector createGlyphVector(FontRenderContext frc, int[] glyphCodes) {
    int[] idx = new int[glyphCodes.length];
    for (int i = 0; i < idx.length; i++) {
      idx[i] = i;
    }
    return new SimpleGlyphVector(this, frc, glyphCodes.clone(), idx);
  }

  public GlyphVector createGlyphVector(FontRenderContext frc, char[] chars) {
    return createGlyphVector(frc, new String(chars));
  }

  public GlyphVector layoutGlyphVector(FontRenderContext frc, char[] text, int start, int limit, int flags) {
    return createGlyphVector(frc, new String(text, start, limit - start));
  }
}
