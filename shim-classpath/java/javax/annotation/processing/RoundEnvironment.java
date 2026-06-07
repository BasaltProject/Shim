/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.annotation.processing;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public interface RoundEnvironment {
  boolean processingOver();

  boolean errorRaised();

  Set<? extends Element> getRootElements();

  Set<? extends Element> getElementsAnnotatedWith(TypeElement a);

  default Set<? extends Element> getElementsAnnotatedWithAny(TypeElement... annotations) {
    Set<Element> result = new LinkedHashSet<Element>();
    for (TypeElement annotation : annotations) {
      result.addAll(getElementsAnnotatedWith(annotation));
    }
    return Collections.unmodifiableSet(result);
  }

  Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a);

  default Set<? extends Element> getElementsAnnotatedWithAny(Set<Class<? extends Annotation>> annotations) {
    Set<Element> result = new LinkedHashSet<Element>();
    for (Class<? extends Annotation> annotation : annotations) {
      result.addAll(getElementsAnnotatedWith(annotation));
    }
    return Collections.unmodifiableSet(result);
  }
}
