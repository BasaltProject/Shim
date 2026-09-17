/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public abstract class Snippet {
  final String id;
  final Kind kind;
  final SubKind subKind;
  final String source;
  String name;
  String typeName;
  String wrapperClass;
  String wrapperSource;
  int wrapperOffset;
  String signature;
  String paramTypes;
  int ordinal;
  int unitPos;

  Snippet(String id, Kind kind, SubKind subKind, String source) {
    this.id = id;
    this.kind = kind;
    this.subKind = subKind;
    this.source = source;
  }

  public String id() {
    return id;
  }

  public Kind kind() {
    return kind;
  }

  public String source() {
    return source;
  }

  public SubKind subKind() {
    return subKind;
  }

  public String toString() {
    return getClass().getSimpleName() + ":" + id + ":" + source;
  }

  public enum Kind {
    IMPORT(true), TYPE_DECL(true), METHOD(true), VAR(true), EXPRESSION(false), STATEMENT(false), ERRONEOUS(false);

    private final boolean persistent;

    Kind(boolean persistent) {
      this.persistent = persistent;
    }

    public boolean isPersistent() {
      return persistent;
    }
  }

  public enum Status {
    VALID(true, true), RECOVERABLE_DEFINED(true, true), RECOVERABLE_NOT_DEFINED(true, false), DROPPED(false, false),
    OVERWRITTEN(false, false), REJECTED(false, false), NONEXISTENT(false, false);

    private final boolean active;
    private final boolean defined;

    Status(boolean active, boolean defined) {
      this.active = active;
      this.defined = defined;
    }

    public boolean isActive() {
      return active;
    }

    public boolean isDefined() {
      return defined;
    }
  }

  public enum SubKind {
    SINGLE_TYPE_IMPORT_SUBKIND(Kind.IMPORT), TYPE_IMPORT_ON_DEMAND_SUBKIND(Kind.IMPORT),
    SINGLE_STATIC_IMPORT_SUBKIND(Kind.IMPORT), STATIC_IMPORT_ON_DEMAND_SUBKIND(Kind.IMPORT),
    MODULE_IMPORT_SUBKIND(Kind.IMPORT), CLASS_SUBKIND(Kind.TYPE_DECL), INTERFACE_SUBKIND(Kind.TYPE_DECL),
    ENUM_SUBKIND(Kind.TYPE_DECL), RECORD_SUBKIND(Kind.TYPE_DECL), ANNOTATION_TYPE_SUBKIND(Kind.TYPE_DECL),
    METHOD_SUBKIND(Kind.METHOD, true, false), VAR_DECLARATION_SUBKIND(Kind.VAR, true, false),
    VAR_DECLARATION_WITH_INITIALIZER_SUBKIND(Kind.VAR, true, true),
    TEMP_VAR_EXPRESSION_SUBKIND(Kind.VAR, true, true), VAR_VALUE_SUBKIND(Kind.EXPRESSION, true, true),
    ASSIGNMENT_SUBKIND(Kind.EXPRESSION, true, true), OTHER_EXPRESSION_SUBKIND(Kind.EXPRESSION, true, true),
    STATEMENT_SUBKIND(Kind.STATEMENT, true, true), UNKNOWN_SUBKIND(Kind.ERRONEOUS, false, true);

    private final Kind kind;
    private final boolean hasValue;
    private final boolean executable;

    SubKind(Kind kind) {
      this(kind, false, false);
    }

    SubKind(Kind kind, boolean hasValue, boolean executable) {
      this.kind = kind;
      this.hasValue = hasValue;
      this.executable = executable;
    }

    public boolean hasValue() {
      return this == VAR_VALUE_SUBKIND || this == ASSIGNMENT_SUBKIND || this == OTHER_EXPRESSION_SUBKIND
          || this == TEMP_VAR_EXPRESSION_SUBKIND;
    }

    public boolean isExecutable() {
      return this == VAR_DECLARATION_WITH_INITIALIZER_SUBKIND || this == TEMP_VAR_EXPRESSION_SUBKIND
          || this == VAR_VALUE_SUBKIND || this == ASSIGNMENT_SUBKIND || this == OTHER_EXPRESSION_SUBKIND
          || this == STATEMENT_SUBKIND;
    }

    public Kind kind() {
      return kind;
    }
  }
}
