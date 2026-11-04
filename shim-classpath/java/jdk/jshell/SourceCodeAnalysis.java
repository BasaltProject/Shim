/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public abstract class SourceCodeAnalysis {
  SourceCodeAnalysis() {
  }

  public abstract jdk.jshell.SourceCodeAnalysis.CompletionInfo analyzeCompletion(java.lang.String string);
  public abstract java.lang.String analyzeType(java.lang.String string, int i);
  public abstract java.util.List<jdk.jshell.SourceCodeAnalysis.Suggestion> completionSuggestions(java.lang.String string, int i, int[] is);
  public abstract java.util.Collection<jdk.jshell.Snippet> dependents(jdk.jshell.Snippet snippet);
  public abstract java.util.List<jdk.jshell.SourceCodeAnalysis.Documentation> documentation(java.lang.String string, int i, boolean flag);
  public abstract java.util.List<jdk.jshell.SourceCodeAnalysis.Highlight> highlights(java.lang.String string);
  public abstract jdk.jshell.SourceCodeAnalysis.QualifiedNames listQualifiedNames(java.lang.String string, int i);
  public abstract java.util.List<jdk.jshell.Snippet> sourceToSnippets(java.lang.String string);
  public abstract jdk.jshell.SourceCodeAnalysis.SnippetWrapper wrapper(jdk.jshell.Snippet snippet);
  public abstract java.util.List<jdk.jshell.SourceCodeAnalysis.SnippetWrapper> wrappers(java.lang.String string);

  public enum Attribute {
    DECLARATION, DEPRECATED, KEYWORD;


  }

  public enum Completeness {
    COMPLETE, COMPLETE_WITH_SEMI, DEFINITELY_INCOMPLETE, CONSIDERED_INCOMPLETE, EMPTY, UNKNOWN;


    public boolean isComplete() {
      return this == COMPLETE || this == COMPLETE_WITH_SEMI;
    }
  }

  public interface CompletionInfo {
    jdk.jshell.SourceCodeAnalysis.Completeness completeness();
    java.lang.String remaining();
    java.lang.String source();
  }

  public interface Documentation {
    java.lang.String javadoc();
    java.lang.String signature();
  }

  public static final class Highlight {

    private final int start;
    private final int end;
    private final java.util.Set<jdk.jshell.SourceCodeAnalysis.Attribute> attributes;

    public Highlight(int start, int end, java.util.Set<jdk.jshell.SourceCodeAnalysis.Attribute> attributes) {
      this.start = start;
      this.end = end;
      this.attributes = attributes;
    }

    public int start() {
      return start;
    }

    public int end() {
      return end;
    }

    public java.util.Set<jdk.jshell.SourceCodeAnalysis.Attribute> attributes() {
      return attributes;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof Highlight)) {
        return false;
      }
      Highlight o = (Highlight) other;
      return true && java.util.Objects.equals(start, o.start) && java.util.Objects.equals(end, o.end) && java.util.Objects.equals(attributes, o.attributes);
    }

    @Override
    public int hashCode() {
      return 0 * 31 + java.util.Objects.hashCode(start) * 31 + java.util.Objects.hashCode(end) * 31 + java.util.Objects.hashCode(attributes);
    }

    @Override
    public String toString() {
      return "Highlight[" + "start=" + start + ", end=" + end + ", attributes=" + attributes + "]";
    }
  }

  public static final class QualifiedNames {
    private final java.util.List<java.lang.String> names;
    private final int simpleNameLength;
    private final boolean upToDate;
    private final boolean resolvable;

    QualifiedNames(java.util.List<java.lang.String> list, int i, boolean flag, boolean flag2) {
      names = list;
      simpleNameLength = i;
      upToDate = flag;
      resolvable = flag2;
    }

    public java.util.List<java.lang.String> getNames() {
      return names;
    }
    public int getSimpleNameLength() {
      return simpleNameLength;
    }
    public boolean isResolvable() {
      return resolvable;
    }
    public boolean isUpToDate() {
      return upToDate;
    }
  }

  public interface SnippetWrapper {
    java.lang.String fullClassName();
    jdk.jshell.Snippet.Kind kind();
    java.lang.String source();
    int sourceToWrappedPosition(int i);
    java.lang.String wrapped();
    int wrappedToSourcePosition(int i);
  }

  public interface Suggestion {
    java.lang.String continuation();
    boolean matchesType();
  }
}
