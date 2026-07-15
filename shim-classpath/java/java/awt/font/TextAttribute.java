/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

public final class TextAttribute extends java.text.AttributedCharacterIterator.Attribute {
  private static final java.util.Map<String, TextAttribute> instanceMap = new java.util.HashMap<>(29);

  public static final TextAttribute FAMILY = new TextAttribute("family");
  public static final TextAttribute WEIGHT = new TextAttribute("weight");
  public static final java.lang.Float WEIGHT_EXTRA_LIGHT = java.lang.Float.valueOf(Float.intBitsToFloat(1056964608));
  public static final java.lang.Float WEIGHT_LIGHT = java.lang.Float.valueOf(Float.intBitsToFloat(1061158912));
  public static final java.lang.Float WEIGHT_DEMILIGHT = java.lang.Float.valueOf(Float.intBitsToFloat(1063256064));
  public static final java.lang.Float WEIGHT_REGULAR = java.lang.Float.valueOf(Float.intBitsToFloat(1065353216));
  public static final java.lang.Float WEIGHT_SEMIBOLD = java.lang.Float.valueOf(Float.intBitsToFloat(1067450368));
  public static final java.lang.Float WEIGHT_MEDIUM = java.lang.Float.valueOf(Float.intBitsToFloat(1069547520));
  public static final java.lang.Float WEIGHT_DEMIBOLD = java.lang.Float.valueOf(Float.intBitsToFloat(1071644672));
  public static final java.lang.Float WEIGHT_BOLD = java.lang.Float.valueOf(Float.intBitsToFloat(1073741824));
  public static final java.lang.Float WEIGHT_HEAVY = java.lang.Float.valueOf(Float.intBitsToFloat(1074790400));
  public static final java.lang.Float WEIGHT_EXTRABOLD = java.lang.Float.valueOf(Float.intBitsToFloat(1075838976));
  public static final java.lang.Float WEIGHT_ULTRABOLD = java.lang.Float.valueOf(Float.intBitsToFloat(1076887552));
  public static final TextAttribute WIDTH = new TextAttribute("width");
  public static final java.lang.Float WIDTH_CONDENSED = java.lang.Float.valueOf(Float.intBitsToFloat(1061158912));
  public static final java.lang.Float WIDTH_SEMI_CONDENSED = java.lang.Float.valueOf(Float.intBitsToFloat(1063256064));
  public static final java.lang.Float WIDTH_REGULAR = java.lang.Float.valueOf(Float.intBitsToFloat(1065353216));
  public static final java.lang.Float WIDTH_SEMI_EXTENDED = java.lang.Float.valueOf(Float.intBitsToFloat(1067450368));
  public static final java.lang.Float WIDTH_EXTENDED = java.lang.Float.valueOf(Float.intBitsToFloat(1069547520));
  public static final TextAttribute POSTURE = new TextAttribute("posture");
  public static final java.lang.Float POSTURE_REGULAR = java.lang.Float.valueOf(Float.intBitsToFloat(0));
  public static final java.lang.Float POSTURE_OBLIQUE = java.lang.Float.valueOf(Float.intBitsToFloat(1045220557));
  public static final TextAttribute SIZE = new TextAttribute("size");
  public static final TextAttribute TRANSFORM = new TextAttribute("transform");
  public static final TextAttribute SUPERSCRIPT = new TextAttribute("superscript");
  public static final java.lang.Integer SUPERSCRIPT_SUPER = java.lang.Integer.valueOf(1);
  public static final java.lang.Integer SUPERSCRIPT_SUB = java.lang.Integer.valueOf(-1);
  public static final TextAttribute FONT = new TextAttribute("font");
  public static final TextAttribute CHAR_REPLACEMENT = new TextAttribute("char_replacement");
  public static final TextAttribute FOREGROUND = new TextAttribute("foreground");
  public static final TextAttribute BACKGROUND = new TextAttribute("background");
  public static final TextAttribute UNDERLINE = new TextAttribute("underline");
  public static final java.lang.Integer UNDERLINE_ON = java.lang.Integer.valueOf(0);
  public static final TextAttribute STRIKETHROUGH = new TextAttribute("strikethrough");
  public static final java.lang.Boolean STRIKETHROUGH_ON = java.lang.Boolean.valueOf(true);
  public static final TextAttribute RUN_DIRECTION = new TextAttribute("run_direction");
  public static final java.lang.Boolean RUN_DIRECTION_LTR = java.lang.Boolean.valueOf(false);
  public static final java.lang.Boolean RUN_DIRECTION_RTL = java.lang.Boolean.valueOf(true);
  public static final TextAttribute BIDI_EMBEDDING = new TextAttribute("bidi_embedding");
  public static final TextAttribute JUSTIFICATION = new TextAttribute("justification");
  public static final java.lang.Float JUSTIFICATION_FULL = java.lang.Float.valueOf(Float.intBitsToFloat(1065353216));
  public static final java.lang.Float JUSTIFICATION_NONE = java.lang.Float.valueOf(Float.intBitsToFloat(0));
  public static final TextAttribute INPUT_METHOD_HIGHLIGHT = new TextAttribute("input method highlight");
  public static final TextAttribute INPUT_METHOD_UNDERLINE = new TextAttribute("input method underline");
  public static final java.lang.Integer UNDERLINE_LOW_ONE_PIXEL = java.lang.Integer.valueOf(1);
  public static final java.lang.Integer UNDERLINE_LOW_TWO_PIXEL = java.lang.Integer.valueOf(2);
  public static final java.lang.Integer UNDERLINE_LOW_DOTTED = java.lang.Integer.valueOf(3);
  public static final java.lang.Integer UNDERLINE_LOW_GRAY = java.lang.Integer.valueOf(4);
  public static final java.lang.Integer UNDERLINE_LOW_DASHED = java.lang.Integer.valueOf(5);
  public static final TextAttribute SWAP_COLORS = new TextAttribute("swap_colors");
  public static final java.lang.Boolean SWAP_COLORS_ON = java.lang.Boolean.valueOf(true);
  public static final TextAttribute NUMERIC_SHAPING = new TextAttribute("numeric_shaping");
  public static final TextAttribute KERNING = new TextAttribute("kerning");
  public static final java.lang.Integer KERNING_ON = java.lang.Integer.valueOf(1);
  public static final TextAttribute LIGATURES = new TextAttribute("ligatures");
  public static final java.lang.Integer LIGATURES_ON = java.lang.Integer.valueOf(1);
  public static final TextAttribute TRACKING = new TextAttribute("tracking");
  public static final java.lang.Float TRACKING_TIGHT = java.lang.Float.valueOf(Float.intBitsToFloat(-1121724662));
  public static final java.lang.Float TRACKING_LOOSE = java.lang.Float.valueOf(Float.intBitsToFloat(1025758986));
  protected TextAttribute(java.lang.String name) {
    super(name);
    if (this.getClass() == TextAttribute.class) {
      instanceMap.put(name, this);
    }
  }
  protected java.lang.Object readResolve() throws java.io.InvalidObjectException {
    if (this.getClass() != TextAttribute.class) {
      throw new java.io.InvalidObjectException("subclass didn't correctly implement readResolve");
    }
    TextAttribute instance = instanceMap.get(getName());
    if (instance != null) {
      return instance;
    }
    throw new java.io.InvalidObjectException("unknown attribute name");
  }
}
