/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

import java.io.Serializable;

public class HashDocAttributeSet extends HashAttributeSet implements DocAttributeSet, Serializable {
  private static final long serialVersionUID = -1128534486061432528L;

  public HashDocAttributeSet() { super(DocAttribute.class); }

  public HashDocAttributeSet(DocAttribute attribute) { super(attribute, DocAttribute.class); }

  public HashDocAttributeSet(DocAttribute[] attributes) { super(attributes, DocAttribute.class); }

  public HashDocAttributeSet(DocAttributeSet attributes) { super(attributes, DocAttribute.class); }
}
