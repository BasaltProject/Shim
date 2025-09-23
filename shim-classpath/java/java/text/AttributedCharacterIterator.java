/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface AttributedCharacterIterator extends CharacterIterator {
  class Attribute implements Serializable {
    private static final long serialVersionUID = -9142742483513960612L;

    private String name;
    private static final Map<String, Attribute> instanceMap = new HashMap<String, Attribute>(7);

    protected Attribute(String name) {
      this.name = name;
      if (this.getClass() == Attribute.class) {
        instanceMap.put(name, this);
      }
    }

    public final boolean equals(Object obj) {
      return super.equals(obj);
    }

    public final int hashCode() {
      return super.hashCode();
    }

    public String toString() {
      return getClass().getName() + "(" + name + ")";
    }

    protected String getName() {
      return name;
    }

    protected Object readResolve() throws InvalidObjectException {
      if (this.getClass() != Attribute.class) {
        throw new InvalidObjectException("subclass didn't correctly implement readResolve");
      }
      Attribute instance = instanceMap.get(getName());
      if (instance != null) {
        return instance;
      }
      throw new InvalidObjectException("unknown attribute name");
    }

    public static final Attribute LANGUAGE = new Attribute("language");
    public static final Attribute READING = new Attribute("reading");
    public static final Attribute INPUT_METHOD_SEGMENT = new Attribute("input_method_segment");
  }

  int getRunStart();

  int getRunStart(Attribute attribute);

  int getRunStart(Set<? extends Attribute> attributes);

  int getRunLimit();

  int getRunLimit(Attribute attribute);

  int getRunLimit(Set<? extends Attribute> attributes);

  Map<Attribute, Object> getAttributes();

  Object getAttribute(Attribute attribute);

  Set<Attribute> getAllAttributeKeys();
}
