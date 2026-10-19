/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanProperty {
  boolean bound() default true;

  boolean expert() default false;

  boolean hidden() default false;

  boolean preferred() default false;

  boolean required() default false;

  boolean visualUpdate() default false;

  String description() default "";

  String[] enumerationValues() default {};
}
