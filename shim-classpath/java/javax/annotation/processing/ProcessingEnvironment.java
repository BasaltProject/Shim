/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.annotation.processing;

public interface ProcessingEnvironment {
  javax.lang.model.util.Elements getElementUtils();
  javax.annotation.processing.Filer getFiler();
  java.util.Locale getLocale();
  javax.annotation.processing.Messager getMessager();
  java.util.Map<java.lang.String, java.lang.String> getOptions();
  javax.lang.model.SourceVersion getSourceVersion();
  javax.lang.model.util.Types getTypeUtils();
  default boolean isPreviewEnabled() {
    return false;
  }
}
