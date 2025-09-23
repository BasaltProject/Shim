/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.annotation;

public class IncompleteAnnotationException extends RuntimeException {
  private static final long serialVersionUID = 8445097402741811912L;

  private Class<? extends Annotation> annotationType;
  private String elementName;

  public IncompleteAnnotationException(Class<? extends Annotation> annotationType, String elementName) {
    super(annotationType.getName() + " missing element " + elementName.toString());
    this.annotationType = annotationType;
    this.elementName = elementName;
  }

  public Class<? extends Annotation> annotationType() {
    return annotationType;
  }

  public String elementName() {
    return elementName;
  }
}
