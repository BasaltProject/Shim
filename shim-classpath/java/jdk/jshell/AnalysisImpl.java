/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Source analysis for the in-process engine. */
class AnalysisImpl extends SourceCodeAnalysis {
  private final JShell js;

  AnalysisImpl(JShell js) {
    this.js = js;
  }

  static final class Info implements CompletionInfo {
    final Completeness c;
    final String source;
    final String remaining;

    Info(Completeness c, String source, String remaining) {
      this.c = c;
      this.source = source;
      this.remaining = remaining;
    }

    public Completeness completeness() {
      return c;
    }

    public String remaining() {
      return remaining;
    }

    public String source() {
      return source;
    }
  }

  private static boolean isWs(char c) {
    return Character.isWhitespace(c);
  }

  public CompletionInfo analyzeCompletion(String input) {
    if (input == null || input.trim().isEmpty()) {
      return new Info(Completeness.EMPTY, input == null ? "" : input, "");
    }
    int n = input.length();
    int i = 0;
    while (i < n && isWs(input.charAt(i))) {
      i++;
    }
    int start = i;
    int depth = 0;
    int headerBrace = -1;
    boolean eqSeen = false;
    boolean lambdaSeen = false;
    int lastTok = -1;
    String firstWord = null;
    while (i < n) {
      char c = input.charAt(i);
      if (c == '/' && i + 1 < n && input.charAt(i + 1) == '/') {
        while (i < n && input.charAt(i) != '\n') {
          i++;
        }
        continue;
      }
      if (c == '/' && i + 1 < n && input.charAt(i + 1) == '*') {
        int e = input.indexOf("*/", i + 2);
        if (e < 0) {
          return new Info(Completeness.DEFINITELY_INCOMPLETE, null, input + "\n");
        }
        i = e + 2;
        continue;
      }
      if (c == '"') {
        if (input.startsWith("\"\"\"", i)) {
          int e = input.indexOf("\"\"\"", i + 3);
          if (e < 0) {
            return new Info(Completeness.DEFINITELY_INCOMPLETE, null, input + "\n");
          }
          i = e + 3;
        } else {
          i++;
          while (i < n && input.charAt(i) != '"') {
            if (input.charAt(i) == '\\') {
              i++;
            }
            if (i < n && input.charAt(i) == '\n') {
              return new Info(Completeness.UNKNOWN, input, "");
            }
            i++;
          }
          if (i >= n) {
            return new Info(Completeness.UNKNOWN, input, "");
          }
          i++;
        }
        lastTok = '"';
        continue;
      }
      if (c == '\'') {
        i++;
        while (i < n && input.charAt(i) != '\'') {
          if (input.charAt(i) == '\\') {
            i++;
          }
          i++;
        }
        if (i >= n) {
          return new Info(Completeness.UNKNOWN, input, "");
        }
        i++;
        lastTok = '\'';
        continue;
      }
      if (isWs(c)) {
        i++;
        continue;
      }
      if (firstWord == null && Character.isJavaIdentifierStart(c)) {
        int e = i;
        while (e < n && Character.isJavaIdentifierPart(input.charAt(e))) {
          e++;
        }
        firstWord = input.substring(i, e);
      }
      if (c == '(' || c == '[' || c == '{') {
        if (c == '{' && depth == 0 && headerBrace < 0) {
          headerBrace = i;
        }
        depth++;
      } else if (c == ')' || c == ']' || c == '}') {
        depth--;
        if (depth < 0) {
          return new Info(Completeness.UNKNOWN, input, "");
        }
        if (c == '}' && depth == 0) {
          String header = input.substring(start, headerBrace < 0 ? i : headerBrace);
          if (braceEnds(header, firstWord) && !continues(input, i + 1, firstWord)) {
            return split(input, start, i + 1, Completeness.COMPLETE);
          }
        }
      } else if (c == ';' && depth == 0) {
        if ("if".equals(firstWord) && continues(input, i + 1, firstWord)) {
          i++;
          continue;
        }
        return split(input, start, i + 1, Completeness.COMPLETE);
      } else if (depth == 0 && c == '=' && headerBrace < 0) {
        eqSeen = true;
      } else if (c == '-' && i + 1 < n && input.charAt(i + 1) == '>') {
        lambdaSeen = true;
      }
      lastTok = c;
      i++;
    }
    if (depth > 0) {
      return new Info(Completeness.DEFINITELY_INCOMPLETE, null, input + "\n");
    }
    String t = input.substring(start).trim();
    if (t.endsWith("+") && false || t.endsWith("+") || t.endsWith("-") || t.endsWith("*") || t.endsWith("/") || t.endsWith("=")
        || t.endsWith(".") || t.endsWith(",") || t.endsWith("&&") || t.endsWith("||") || t.endsWith("?")
        || t.endsWith(":") || t.endsWith("<") || t.endsWith("&") || t.endsWith("|") || t.endsWith("->")) {
      return new Info(Completeness.DEFINITELY_INCOMPLETE, null, input + "\n");
    }
    JShell.Unit u = js.classify(t);
    if (u.kindTag.equals("error")) {
      return new Info(Completeness.UNKNOWN, input, "");
    }
    if (u.kindTag.equals("expr") && !t.endsWith(";")) {
      return new Info(Completeness.COMPLETE, input.substring(start), "");
    }
    return new Info(Completeness.COMPLETE_WITH_SEMI, t + ";", "");
  }

  private static boolean braceEnds(String header, String firstWord) {
    if (firstWord == null) {
      return true;
    }
    if (firstWord.equals("new") || firstWord.equals("return") || firstWord.equals("throw")
        || firstWord.equals("yield")) {
      return false;
    }
    int depth = 0;
    for (int k = 0; k < header.length(); k++) {
      char c = header.charAt(k);
      if (c == '(' || c == '[' || c == '<') {
        depth++;
      } else if (c == ')' || c == ']' || c == '>') {
        depth--;
      } else if (c == '=' && depth <= 0) {
        return false;
      } else if (c == '-' && k + 1 < header.length() && header.charAt(k + 1) == '>' && depth <= 0
          && !firstWord.equals("case")) {
        return false;
      }
    }
    return true;
  }

  private static boolean continues(String in, int from, String firstWord) {
    int i = from;
    while (i < in.length() && isWs(in.charAt(i))) {
      i++;
    }
    for (String k : new String[] {"else", "catch", "finally"}) {
      if (in.startsWith(k, i) && (i + k.length() >= in.length() || !Character.isJavaIdentifierPart(in.charAt(i + k.length())))) {
        return true;
      }
    }
    if ("do".equals(firstWord) && in.startsWith("while", i)) {
      return true;
    }
    return false;
  }

  private Info split(String input, int start, int end, Completeness c) {
    return new Info(c, input.substring(start, end), input.substring(end));
  }

  public List<Snippet> sourceToSnippets(String input) {
    List<Snippet> out = new ArrayList<Snippet>();
    return out;
  }

  public List<SnippetWrapper> wrappers(String input) {
    return Collections.<SnippetWrapper>emptyList();
  }

  public SnippetWrapper wrapper(final Snippet snippet) {
    return new SnippetWrapper() {
      public String fullClassName() {
        return snippet.wrapperClass;
      }

      public Snippet.Kind kind() {
        return snippet.kind;
      }

      public String source() {
        return snippet.source;
      }

      public int sourceToWrappedPosition(int i) {
        return i + snippet.wrapperOffset;
      }

      public String wrapped() {
        return snippet.wrapperSource;
      }

      public int wrappedToSourcePosition(int i) {
        return i - snippet.wrapperOffset;
      }
    };
  }

  private static final String[] DEFAULT_PACKAGES = {"java.lang", "java.io", "java.math", "java.net", "java.nio.file",
      "java.util", "java.util.concurrent", "java.util.function", "java.util.prefs", "java.util.regex",
      "java.util.stream", "java.time", "java.text"};

  private static final java.util.Set<String> KEYWORDS = new java.util.HashSet<String>(java.util.Arrays.asList(
      "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
      "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
      "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
      "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
      "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null"));

  private Class<?> findClass(String name) {
    name = name.replaceAll("<.*>", "").trim();
    if (name.isEmpty()) {
      return null;
    }
    List<String> tries = new ArrayList<String>();
    tries.add(name);
    for (String p : DEFAULT_PACKAGES) {
      tries.add(p + "." + name);
    }
    for (String t : tries) {
      try {
        return Class.forName(t, false, ClassLoader.getSystemClassLoader());
      } catch (Throwable e) {
        // try the next candidate
      }
    }
    return null;
  }

  private Snippet activeNamed(String name) {
    for (Snippet sn : js.snippets) {
      if (js.status(sn).isActive() && name.equals(sn.name) && sn.kind != Snippet.Kind.IMPORT) {
        return sn;
      }
    }
    return null;
  }

  public String analyzeType(String code, int cursor) {
    String t = code.substring(0, Math.min(cursor, code.length())).trim();
    if (t.endsWith(";")) {
      t = t.substring(0, t.length() - 1).trim();
    }
    if (t.isEmpty()) {
      return null;
    }
    if (t.startsWith("\"") && t.endsWith("\"") && t.length() > 1) {
      return "String";
    }
    if (t.equals("true") || t.equals("false")) {
      return "boolean";
    }
    if (t.matches("'(\\\\.|[^'\\\\])+'")) {
      return "char";
    }
    if (t.matches("-?\\d+[lL]")) {
      return "long";
    }
    if (t.matches("-?\\d+")) {
      return "int";
    }
    if (t.matches("-?\\d*\\.\\d+([eE][+-]?\\d+)?[dD]?") || t.matches("-?\\d+[dD]")) {
      return "double";
    }
    if (t.matches("-?\\d*\\.?\\d+[fF]")) {
      return "float";
    }
    if (t.startsWith("new ")) {
      int p = t.indexOf('(');
      if (p > 4) {
        return t.substring(4, p).trim();
      }
    }
    if (Character.isJavaIdentifierStart(t.charAt(0)) && t.matches("[\\w$]+")) {
      Snippet sn = activeNamed(t);
      if (sn instanceof VarSnippet) {
        return ((VarSnippet) sn).typeName();
      }
    }
    return null;
  }

  private static String wordAt(String code, int cursor, int[] bounds) {
    int s = Math.min(cursor, code.length());
    int e = s;
    while (s > 0 && Character.isJavaIdentifierPart(code.charAt(s - 1))) {
      s--;
    }
    while (e < code.length() && Character.isJavaIdentifierPart(code.charAt(e))) {
      e++;
    }
    bounds[0] = s;
    bounds[1] = e;
    return code.substring(s, e);
  }

  private static String qualifierBefore(String code, int wordStart) {
    int p = wordStart;
    while (p > 0 && Character.isWhitespace(code.charAt(p - 1))) {
      p--;
    }
    if (p == 0 || code.charAt(p - 1) != '.') {
      return null;
    }
    int e = p - 1;
    int s = e;
    while (s > 0 && (Character.isJavaIdentifierPart(code.charAt(s - 1)) || code.charAt(s - 1) == '.')) {
      s--;
    }
    return code.substring(s, e);
  }

  private Class<?> qualifierClass(String q) {
    if (q == null || q.isEmpty()) {
      return null;
    }
    Snippet sn = activeNamed(q);
    if (sn instanceof VarSnippet) {
      return findClass(((VarSnippet) sn).typeName());
    }
    String t = analyzeType(q, q.length());
    if (t != null) {
      return findClass(t);
    }
    return findClass(q);
  }

  private static String simple(Class<?> c) {
    return c.getSimpleName().isEmpty() ? c.getName() : c.getSimpleName();
  }

  private static String params(java.lang.reflect.Executable m) {
    StringBuilder sb = new StringBuilder("(");
    Class<?>[] ps = m.getParameterTypes();
    for (int i = 0; i < ps.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(simple(ps[i]));
    }
    return sb.append(')').toString();
  }

  public List<Suggestion> completionSuggestions(String input, int cursor, int[] anchor) {
    int[] bounds = new int[2];
    String word = wordAt(input, cursor, bounds);
    int s = bounds[0];
    String prefix = input.substring(s, Math.min(cursor, input.length()));
    anchor[0] = s;
    java.util.TreeSet<String> names = new java.util.TreeSet<String>();
    String q = qualifierBefore(input, s);
    if (q != null) {
      Class<?> c = qualifierClass(q);
      boolean staticOnly = c != null && activeNamed(q) == null && analyzeType(q, q.length()) == null;
      if (c != null) {
        for (java.lang.reflect.Method m : c.getMethods()) {
          if (m.getName().startsWith(prefix) && (!staticOnly || java.lang.reflect.Modifier.isStatic(m.getModifiers()))) {
            names.add(m.getName() + (m.getParameterTypes().length == 0 ? "()" : "("));
          }
        }
        for (java.lang.reflect.Field f : c.getFields()) {
          if (f.getName().startsWith(prefix) && (!staticOnly || java.lang.reflect.Modifier.isStatic(f.getModifiers()))) {
            names.add(f.getName());
          }
        }
      }
    } else {
      for (Snippet sn : js.snippets) {
        if (js.status(sn).isActive() && sn.name != null && sn.name.startsWith(prefix) && sn.kind != Snippet.Kind.IMPORT) {
          names.add(sn.name);
        }
      }
      for (String k : KEYWORDS) {
        if (!prefix.isEmpty() && k.startsWith(prefix)) {
          names.add(k);
        }
      }
      if (!prefix.isEmpty() && Character.isUpperCase(prefix.charAt(0))) {
        Class<?> c = findClass(prefix);
        if (c != null) {
          names.add(simple(c));
        }
      }
    }
    List<Suggestion> out = new ArrayList<Suggestion>();
    for (final String nm : names) {
      out.add(new Suggestion() {
        public String continuation() {
          return nm;
        }

        public boolean matchesType() {
          return false;
        }
      });
    }
    return out;
  }

  public List<Documentation> documentation(String code, int cursor, boolean computeJavadoc) {
    int[] bounds = new int[2];
    String word = wordAt(code, cursor, bounds);
    List<Documentation> out = new ArrayList<Documentation>();
    if (word.isEmpty()) {
      return out;
    }
    String q = qualifierBefore(code, bounds[0]);
    if (q != null) {
      Class<?> c = qualifierClass(q);
      if (c != null) {
        for (java.lang.reflect.Method m : c.getMethods()) {
          if (m.getName().equals(word)) {
            out.add(doc(simple(m.getReturnType()) + " " + simple(m.getDeclaringClass()) + "." + word + params(m)));
          }
        }
        try {
          java.lang.reflect.Field f = c.getField(word);
          out.add(doc(simple(f.getType()) + " " + simple(f.getDeclaringClass()) + "." + word));
        } catch (NoSuchFieldException e) {
          // not a field
        }
      }
      return out;
    }
    Snippet sn = activeNamed(word);
    if (sn instanceof VarSnippet) {
      out.add(doc(((VarSnippet) sn).typeName() + " " + word));
    } else if (sn instanceof MethodSnippet) {
      out.add(doc(((MethodSnippet) sn).signature() + " " + word));
    } else if (sn != null) {
      out.add(doc(word));
    } else {
      Class<?> c = findClass(word);
      if (c != null) {
        out.add(doc(c.getName()));
      }
    }
    return out;
  }

  private static Documentation doc(final String signature) {
    return new Documentation() {
      public String javadoc() {
        return null;
      }

      public String signature() {
        return signature;
      }
    };
  }

  public List<Highlight> highlights(String code) {
    List<Highlight> out = new ArrayList<Highlight>();
    int n = code.length();
    int i = 0;
    boolean declNext = false;
    while (i < n) {
      char c = code.charAt(i);
      if (c == '/' && i + 1 < n && code.charAt(i + 1) == '/') {
        while (i < n && code.charAt(i) != '\n') {
          i++;
        }
      } else if (c == '/' && i + 1 < n && code.charAt(i + 1) == '*') {
        int e = code.indexOf("*/", i + 2);
        i = e < 0 ? n : e + 2;
      } else if (c == '"' || c == '\'') {
        i++;
        while (i < n && code.charAt(i) != c) {
          i += code.charAt(i) == '\\' ? 2 : 1;
        }
        i++;
      } else if (Character.isJavaIdentifierStart(c)) {
        int s = i;
        while (i < n && Character.isJavaIdentifierPart(code.charAt(i))) {
          i++;
        }
        String w = code.substring(s, i);
        if (KEYWORDS.contains(w)) {
          out.add(new Highlight(s, i, java.util.EnumSet.of(Attribute.KEYWORD)));
          declNext = w.equals("class") || w.equals("interface") || w.equals("enum");
        } else {
          if (declNext) {
            out.add(new Highlight(s, i, java.util.EnumSet.of(Attribute.DECLARATION)));
          }
          declNext = false;
        }
      } else {
        i++;
      }
    }
    return out;
  }

  public QualifiedNames listQualifiedNames(String code, int cursor) {
    int[] bounds = new int[2];
    String word = wordAt(code, cursor, bounds);
    List<String> names = new ArrayList<String>();
    if (!word.isEmpty()) {
      List<String> pkgs = new ArrayList<String>(java.util.Arrays.asList(DEFAULT_PACKAGES));
      pkgs.add("java.awt");
      pkgs.add("java.sql");
      pkgs.add("java.nio");
      pkgs.add("java.nio.channels");
      pkgs.add("javax.swing");
      for (String p : pkgs) {
        try {
          Class.forName(p + "." + word, false, ClassLoader.getSystemClassLoader());
          names.add(p + "." + word);
        } catch (Throwable e) {
          // not in this package
        }
      }
      java.util.Collections.sort(names);
    }
    return new QualifiedNames(names, word.length(), true, !names.isEmpty());
  }

  public Collection<Snippet> dependents(Snippet snippet) {
    return Collections.<Snippet>emptyList();
  }
}
