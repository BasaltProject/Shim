/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.annotation.processing;

public interface Filer {
  javax.tools.JavaFileObject createClassFile(java.lang.CharSequence charSequence, javax.lang.model.element.Element... elements) throws java.io.IOException;
  javax.tools.FileObject createResource(javax.tools.JavaFileManager.Location location, java.lang.CharSequence charSequence, java.lang.CharSequence charSequence2, javax.lang.model.element.Element... elements) throws java.io.IOException;
  javax.tools.JavaFileObject createSourceFile(java.lang.CharSequence charSequence, javax.lang.model.element.Element... elements) throws java.io.IOException;
  javax.tools.FileObject getResource(javax.tools.JavaFileManager.Location location, java.lang.CharSequence charSequence, java.lang.CharSequence charSequence2) throws java.io.IOException;
}
