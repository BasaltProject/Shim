/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image.renderable;

import java.awt.image.RenderedImage;
import java.io.Serializable;
import java.util.Vector;

public class ParameterBlock implements Cloneable, Serializable {
  protected Vector<Object> sources = new Vector<Object>();
  protected Vector<Object> parameters = new Vector<Object>();

  public ParameterBlock() { }

  public ParameterBlock(Vector<Object> sources) { setSources(sources); }

  public ParameterBlock(Vector<Object> sources, Vector<Object> parameters) {
    setSources(sources);
    setParameters(parameters);
  }

  public Object shallowClone() {
    try {
      return super.clone();
    } catch (Exception e) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public Object clone() {
    ParameterBlock theClone;
    try {
      theClone = (ParameterBlock) super.clone();
    } catch (Exception e) {
      return null;
    }
    if (sources != null) {
      theClone.setSources((Vector<Object>) sources.clone());
    }
    if (parameters != null) {
      theClone.setParameters((Vector<Object>) parameters.clone());
    }
    return theClone;
  }

  public ParameterBlock addSource(Object source) {
    sources.addElement(source);
    return this;
  }

  public Object getSource(int index) { return sources.elementAt(index); }

  public ParameterBlock setSource(Object source, int index) {
    int oldSize = sources.size();
    int newSize = index + 1;
    if (oldSize < newSize) {
      sources.setSize(newSize);
    }
    sources.setElementAt(source, index);
    return this;
  }

  public RenderedImage getRenderedSource(int index) { return (RenderedImage) sources.elementAt(index); }

  public RenderableImage getRenderableSource(int index) { return (RenderableImage) sources.elementAt(index); }

  public int getNumSources() { return sources.size(); }

  public Vector<Object> getSources() { return sources; }

  public void setSources(Vector<Object> sources) { this.sources = sources; }

  public void removeSources() { sources = new Vector<Object>(); }

  public int getNumParameters() { return parameters.size(); }

  public Vector<Object> getParameters() { return parameters; }

  public void setParameters(Vector<Object> parameters) { this.parameters = parameters; }

  public void removeParameters() { parameters = new Vector<Object>(); }

  public ParameterBlock add(Object obj) {
    parameters.addElement(obj);
    return this;
  }

  public ParameterBlock add(byte b) { return add(Byte.valueOf(b)); }

  public ParameterBlock add(char b) { return add(Character.valueOf(b)); }

  public ParameterBlock add(short b) { return add(Short.valueOf(b)); }

  public ParameterBlock add(int b) { return add(Integer.valueOf(b)); }

  public ParameterBlock add(long b) { return add(Long.valueOf(b)); }

  public ParameterBlock add(float b) { return add(Float.valueOf(b)); }

  public ParameterBlock add(double b) { return add(Double.valueOf(b)); }

  public ParameterBlock set(Object obj, int index) {
    int oldSize = parameters.size();
    int newSize = index + 1;
    if (oldSize < newSize) {
      parameters.setSize(newSize);
    }
    parameters.setElementAt(obj, index);
    return this;
  }

  public ParameterBlock set(byte b, int index) { return set(Byte.valueOf(b), index); }

  public ParameterBlock set(char b, int index) { return set(Character.valueOf(b), index); }

  public ParameterBlock set(short b, int index) { return set(Short.valueOf(b), index); }

  public ParameterBlock set(int b, int index) { return set(Integer.valueOf(b), index); }

  public ParameterBlock set(long b, int index) { return set(Long.valueOf(b), index); }

  public ParameterBlock set(float b, int index) { return set(Float.valueOf(b), index); }

  public ParameterBlock set(double b, int index) { return set(Double.valueOf(b), index); }

  public Object getObjectParameter(int index) { return parameters.elementAt(index); }

  public byte getByteParameter(int index) { return ((Byte) parameters.elementAt(index)).byteValue(); }

  public char getCharParameter(int index) { return ((Character) parameters.elementAt(index)).charValue(); }

  public short getShortParameter(int index) { return ((Short) parameters.elementAt(index)).shortValue(); }

  public int getIntParameter(int index) { return ((Integer) parameters.elementAt(index)).intValue(); }

  public long getLongParameter(int index) { return ((Long) parameters.elementAt(index)).longValue(); }

  public float getFloatParameter(int index) { return ((Float) parameters.elementAt(index)).floatValue(); }

  public double getDoubleParameter(int index) { return ((Double) parameters.elementAt(index)).doubleValue(); }

  public Class<?>[] getParamClasses() {
    int numParams = getNumParameters();
    Class<?>[] classes = new Class<?>[numParams];
    for (int i = 0; i < numParams; i++) {
      Object obj = getObjectParameter(i);
      if (obj instanceof Byte) {
        classes[i] = byte.class;
      } else if (obj instanceof Character) {
        classes[i] = char.class;
      } else if (obj instanceof Short) {
        classes[i] = short.class;
      } else if (obj instanceof Integer) {
        classes[i] = int.class;
      } else if (obj instanceof Long) {
        classes[i] = long.class;
      } else if (obj instanceof Float) {
        classes[i] = float.class;
      } else if (obj instanceof Double) {
        classes[i] = double.class;
      } else {
        classes[i] = obj.getClass();
      }
    }
    return classes;
  }
}
