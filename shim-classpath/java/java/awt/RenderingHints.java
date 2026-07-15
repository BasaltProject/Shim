/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RenderingHints implements Map<Object, Object>, Cloneable {
  HashMap<Object, Object> hintmap = new HashMap<>(7);

  public abstract static class Key {
    private static final Key[] identitymap = new Key[256];
    private final int privatekey;

    private String description;

    private static synchronized void recordIdentity(Key k) {
      int id = k.privatekey;
      if (id < 0 || id >= identitymap.length) {
        id &= 255;
      }
      identitymap[id] = k;
    }

    protected Key(int privatekey) {
      this.privatekey = privatekey;
      recordIdentity(this);
    }

    public abstract boolean isCompatibleValue(Object val);

    protected final int intKey() {
      return privatekey;
    }

    public final int hashCode() {
      return super.hashCode();
    }

    public final boolean equals(Object o) {
      return this == o;
    }
  }

  private static final class SunKey extends Key {
    private final String desc;
    private final int count;
    private final Object[] values;

    SunKey(int id, String desc, int count) {
      super(id);
      this.desc = desc;
      this.count = count;
      this.values = new Object[count];
    }

    Object val(int i, String name) {
      Object v = new HintValue(this, i, name);
      values[i] = v;
      return v;
    }

    public boolean isCompatibleValue(Object val) {
      HintValue hv = null;
      if (((hv = val instanceof HintValue ? (HintValue) val : null) != null)) {
        return hv.key == this;
      }
      return false;
    }

    public String toString() {
      return desc;
    }
  }

  private static final class HintValue {
    final Key key;
    final int index;

    private final String name;

    HintValue(Key key, int index, String name) {
      this.key = key;
      this.index = index;
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }

  private static final class FloatKey extends Key {
    private final String desc;

    FloatKey(int id, String desc) {
      super(id);
      this.desc = desc;
    }

    public boolean isCompatibleValue(Object val) {
      Integer i = null;
      return ((i = val instanceof Integer ? (Integer) val : null) != null) && i >= 100 && i <= 250;
    }

    public String toString() {
      return desc;
    }
  }

  public static final Key KEY_ANTIALIASING = new SunKey(1, "Global antialiasing hint", 3);
  public static final Object VALUE_ANTIALIAS_ON = ((SunKey) KEY_ANTIALIASING).val(0, "Antialiased rendering mode");
  public static final Object VALUE_ANTIALIAS_OFF = ((SunKey) KEY_ANTIALIASING).val(1, "Nonantialiased rendering mode");
  public static final Object VALUE_ANTIALIAS_DEFAULT = ((SunKey) KEY_ANTIALIASING).val(2, "Default antialiasing rendering mode");
  public static final Key KEY_RENDERING = new SunKey(2, "Rendering hint", 3);
  public static final Object VALUE_RENDER_SPEED = ((SunKey) KEY_RENDERING).val(0, "Fastest rendering methods");
  public static final Object VALUE_RENDER_QUALITY = ((SunKey) KEY_RENDERING).val(1, "Highest quality rendering methods");
  public static final Object VALUE_RENDER_DEFAULT = ((SunKey) KEY_RENDERING).val(2, "Default rendering methods");
  public static final Key KEY_DITHERING = new SunKey(3, "Dithering hint", 3);
  public static final Object VALUE_DITHER_DISABLE = ((SunKey) KEY_DITHERING).val(0, "Nondithered rendering mode");
  public static final Object VALUE_DITHER_ENABLE = ((SunKey) KEY_DITHERING).val(1, "Dithered rendering mode");
  public static final Object VALUE_DITHER_DEFAULT = ((SunKey) KEY_DITHERING).val(2, "Default dithering mode");
  public static final Key KEY_TEXT_ANTIALIASING = new SunKey(4, "Text-specific antialiasing hint", 8);
  public static final Object VALUE_TEXT_ANTIALIAS_ON = ((SunKey) KEY_TEXT_ANTIALIASING).val(0, "Antialiased text mode");
  public static final Object VALUE_TEXT_ANTIALIAS_OFF = ((SunKey) KEY_TEXT_ANTIALIASING).val(1, "Nonantialiased text mode");
  public static final Object VALUE_TEXT_ANTIALIAS_DEFAULT = ((SunKey) KEY_TEXT_ANTIALIASING).val(2, "Default antialiasing text mode");
  public static final Object VALUE_TEXT_ANTIALIAS_GASP = ((SunKey) KEY_TEXT_ANTIALIASING).val(3, "gasp antialiasing text mode");
  public static final Object VALUE_TEXT_ANTIALIAS_LCD_HRGB = ((SunKey) KEY_TEXT_ANTIALIASING).val(4, "LCD HRGB antialiasing text mode");
  public static final Object VALUE_TEXT_ANTIALIAS_LCD_HBGR = ((SunKey) KEY_TEXT_ANTIALIASING).val(5, "LCD HBGR antialiasing text mode");
  public static final Object VALUE_TEXT_ANTIALIAS_LCD_VRGB = ((SunKey) KEY_TEXT_ANTIALIASING).val(6, "LCD VRGB antialiasing text mode");
  public static final Object VALUE_TEXT_ANTIALIAS_LCD_VBGR = ((SunKey) KEY_TEXT_ANTIALIASING).val(7, "LCD VBGR antialiasing text mode");
  public static final Key KEY_FRACTIONALMETRICS = new SunKey(5, "Fractional metrics hint", 3);
  public static final Object VALUE_FRACTIONALMETRICS_OFF = ((SunKey) KEY_FRACTIONALMETRICS).val(0, "Integer text metrics mode");
  public static final Object VALUE_FRACTIONALMETRICS_ON = ((SunKey) KEY_FRACTIONALMETRICS).val(1, "Fractional text metrics mode");
  public static final Object VALUE_FRACTIONALMETRICS_DEFAULT = ((SunKey) KEY_FRACTIONALMETRICS).val(2, "Default fractional text metrics mode");
  public static final Key KEY_INTERPOLATION = new SunKey(6, "Image interpolation method", 3);
  public static final Object VALUE_INTERPOLATION_NEAREST_NEIGHBOR = ((SunKey) KEY_INTERPOLATION).val(0, "Nearest Neighbor image interpolation mode");
  public static final Object VALUE_INTERPOLATION_BILINEAR = ((SunKey) KEY_INTERPOLATION).val(1, "Bilinear image interpolation mode");
  public static final Object VALUE_INTERPOLATION_BICUBIC = ((SunKey) KEY_INTERPOLATION).val(2, "Bicubic image interpolation mode");
  public static final Key KEY_ALPHA_INTERPOLATION = new SunKey(7, "Alpha blending interpolation method", 3);
  public static final Object VALUE_ALPHA_INTERPOLATION_SPEED = ((SunKey) KEY_ALPHA_INTERPOLATION).val(0, "Fastest alpha blending methods");
  public static final Object VALUE_ALPHA_INTERPOLATION_QUALITY = ((SunKey) KEY_ALPHA_INTERPOLATION).val(1, "Highest quality alpha blending methods");
  public static final Object VALUE_ALPHA_INTERPOLATION_DEFAULT = ((SunKey) KEY_ALPHA_INTERPOLATION).val(2, "Default alpha blending methods");
  public static final Key KEY_COLOR_RENDERING = new SunKey(8, "Color rendering quality", 3);
  public static final Object VALUE_COLOR_RENDER_SPEED = ((SunKey) KEY_COLOR_RENDERING).val(0, "Fastest color rendering mode");
  public static final Object VALUE_COLOR_RENDER_QUALITY = ((SunKey) KEY_COLOR_RENDERING).val(1, "Highest quality color rendering mode");
  public static final Object VALUE_COLOR_RENDER_DEFAULT = ((SunKey) KEY_COLOR_RENDERING).val(2, "Default color rendering mode");
  public static final Key KEY_STROKE_CONTROL = new SunKey(9, "Stroke normalization control", 3);
  public static final Object VALUE_STROKE_DEFAULT = ((SunKey) KEY_STROKE_CONTROL).val(0, "Default stroke normalization");
  public static final Object VALUE_STROKE_NORMALIZE = ((SunKey) KEY_STROKE_CONTROL).val(1, "Normalize strokes for consistent rendering");
  public static final Object VALUE_STROKE_PURE = ((SunKey) KEY_STROKE_CONTROL).val(2, "Pure stroke conversion for accurate paths");
  public static final Key KEY_TEXT_LCD_CONTRAST = new FloatKey(100, "Text LCD contrast control");
  public static final Key KEY_RESOLUTION_VARIANT = new SunKey(10, "Global image resolution variant", 4);
  public static final Object VALUE_RESOLUTION_VARIANT_DEFAULT = ((SunKey) KEY_RESOLUTION_VARIANT).val(0, "Choose image resolutions based on a defaultheuristic");
  public static final Object VALUE_RESOLUTION_VARIANT_BASE = ((SunKey) KEY_RESOLUTION_VARIANT).val(1, "Use only the standard resolution of an image");
  public static final Object VALUE_RESOLUTION_VARIANT_SIZE_FIT = ((SunKey) KEY_RESOLUTION_VARIANT).val(2, "Choose image resolutions based on the DPIof the screen and transformin the Graphics2D context");
  public static final Object VALUE_RESOLUTION_VARIANT_DPI_FIT = ((SunKey) KEY_RESOLUTION_VARIANT).val(3, "Choose image resolutions based only on the DPI of the screen");

  public RenderingHints(Map<Key, ?> init) {
    if (init != null) {
      hintmap.putAll(init);
    }
  }

  public RenderingHints(Key key, Object value) {
    hintmap.put(key, value);
  }

  public int size() { return hintmap.size(); }

  public boolean isEmpty() { return hintmap.isEmpty(); }

  public boolean containsKey(Object key) {
    return hintmap.containsKey((Key) key);
  }

  public boolean containsValue(Object value) { return hintmap.containsValue(value); }

  public Object get(Object key) { return hintmap.get(key); }

  public Object put(Object key, Object value) {
    if (!((Key) key).isCompatibleValue(value)) {
      throw new IllegalArgumentException(value + " incompatible with " + key);
    }
    return hintmap.put((Key) key, value);
  }

  public void add(RenderingHints hints) { hintmap.putAll(hints.hintmap); }

  public void clear() { hintmap.clear(); }

  public Object remove(Object key) { return hintmap.remove((Key) key); }

  public void putAll(Map<?, ?> m) {
    if (RenderingHints.class.isInstance(m)) {
      for (Map.Entry<?, ?> entry : m.entrySet()) {
        put(entry.getKey(), entry.getValue());
      }
    } else {
      for (Map.Entry<?, ?> entry : m.entrySet()) {
        put(entry.getKey(), entry.getValue());
      }
    }
  }

  public Set<Object> keySet() { return hintmap.keySet(); }

  public Collection<Object> values() { return hintmap.values(); }

  public Set<Map.Entry<Object, Object>> entrySet() { return java.util.Collections.unmodifiableMap(hintmap).entrySet(); }

  public boolean equals(Object o) {
    RenderingHints h = null;
    if (((h = o instanceof RenderingHints ? (RenderingHints) o : null) != null)) {
      return hintmap.equals(h.hintmap);
    }
    Map<?, ?> m = null;
    if (((m = o instanceof Map<?, ?> ? (Map<?, ?>) o : null) != null)) {
      return hintmap.equals(m);
    }
    return false;
  }

  public int hashCode() { return hintmap.hashCode(); }

  @SuppressWarnings("unchecked")
  public Object clone() {
    RenderingHints rh = new RenderingHints(null);
    rh.hintmap = (HashMap<Object, Object>) hintmap.clone();
    return rh;
  }

  public String toString() {
    if (hintmap == null) {
      return getClass().getName() + "@" + Integer.toHexString(hashCode()) + " (0 hints)";
    }
    return hintmap.toString();
  }
}
