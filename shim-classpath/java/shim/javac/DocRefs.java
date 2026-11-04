/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.doctree.ParamTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

/** Resolves {@code @see}, {@code {@link}} and {@code @param} references against the code model. */
final class DocRefs {
  private DocRefs() {
  }

  static Element param(Element context, ParamTree tree) {
    String name = tree.getName().getName().toString();
    if (!(context instanceof ExecutableElement) && !(context instanceof TypeElement)) {
      return null;
    }
    if (tree.isTypeParameter()) {
      List<? extends TypeParameterElement> tps = context instanceof ExecutableElement
          ? ((ExecutableElement) context).getTypeParameters() : ((TypeElement) context).getTypeParameters();
      for (TypeParameterElement tp : tps) {
        if (tp.getSimpleName().contentEquals(name)) {
          return tp;
        }
      }
      return null;
    }
    if (context instanceof ExecutableElement) {
      for (VariableElement p : ((ExecutableElement) context).getParameters()) {
        if (p.getSimpleName().contentEquals(name)) {
          return p;
        }
      }
    } else {
      for (Element e : ((TypeElement) context).getEnclosedElements()) {
        if (e.getKind() == ElementKind.RECORD_COMPONENT && e.getSimpleName().contentEquals(name)) {
          return e;
        }
      }
    }
    return null;
  }

  static Element resolve(JavacTaskImpl task, CompilationUnitTree cu, Element context, String sig) {
    if (sig == null) {
      return null;
    }
    sig = sig.trim();
    String member = null;
    String params = null;
    int hash = sig.indexOf('#');
    String cls = sig;
    if (hash >= 0) {
      cls = sig.substring(0, hash);
      member = sig.substring(hash + 1);
    } else if (sig.indexOf('(') >= 0) {
      member = sig;
      cls = "";
    }
    if (member != null) {
      int open = member.indexOf('(');
      if (open >= 0) {
        params = member.substring(open + 1, member.lastIndexOf(')') < 0 ? member.length() : member.lastIndexOf(')'));
        member = member.substring(0, open);
      }
    }
    cls = cls.trim();
    TypeElement type;
    if (cls.isEmpty()) {
      type = enclosingType(context);
    } else {
      type = findType(task, cu, context, stripGenerics(cls));
    }
    if (type == null) {
      return cls.isEmpty() || member == null ? null : null;
    }
    if (member == null) {
      return type;
    }
    return findMember(type, member.trim(), params);
  }

  private static String stripGenerics(String s) {
    StringBuilder sb = new StringBuilder();
    int depth = 0;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '<') {
        depth++;
      } else if (c == '>') {
        depth--;
      } else if (depth == 0 && !Character.isWhitespace(c)) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static TypeElement enclosingType(Element e) {
    for (Element x = e; x != null; x = x.getEnclosingElement()) {
      if (x instanceof TypeElement) {
        return (TypeElement) x;
      }
    }
    return null;
  }

  private static TypeElement findType(JavacTaskImpl task, CompilationUnitTree cu, Element context, String name) {
    javax.lang.model.util.Elements els = task.getElements();
    int dot = name.indexOf('.');
    String first = dot < 0 ? name : name.substring(0, dot);
    String rest = dot < 0 ? "" : name.substring(dot);
    for (TypeElement t = enclosingType(context); t != null; t = enclosingType(t.getEnclosingElement())) {
      if (t.getSimpleName().contentEquals(first)) {
        return nested(t, rest);
      }
      for (Element m : t.getEnclosedElements()) {
        if (m instanceof TypeElement && m.getSimpleName().contentEquals(first)) {
          return nested((TypeElement) m, rest);
        }
      }
    }
    if (cu != null) {
      for (ImportTree imp : cu.getImports()) {
        String q = imp.getQualifiedIdentifier().toString();
        if (imp.isStatic()) {
          continue;
        }
        if (q.endsWith("." + first)) {
          TypeElement t = els.getTypeElement(q);
          if (t != null) {
            return nested(t, rest);
          }
        }
      }
      String pkg = cu.getPackageName() == null ? "" : cu.getPackageName().toString();
      TypeElement same = els.getTypeElement(pkg.isEmpty() ? first : pkg + "." + first);
      if (same != null) {
        return nested(same, rest);
      }
      for (ImportTree imp : cu.getImports()) {
        String q = imp.getQualifiedIdentifier().toString();
        if (!imp.isStatic() && q.endsWith(".*")) {
          TypeElement t = els.getTypeElement(q.substring(0, q.length() - 1) + first);
          if (t != null) {
            return nested(t, rest);
          }
        }
      }
    }
    TypeElement lang = els.getTypeElement("java.lang." + first);
    if (lang != null) {
      return nested(lang, rest);
    }
    return els.getTypeElement(name);
  }

  private static TypeElement nested(TypeElement t, String rest) {
    while (t != null && !rest.isEmpty()) {
      int next = rest.indexOf('.', 1);
      String part = rest.substring(1, next < 0 ? rest.length() : next);
      rest = next < 0 ? "" : rest.substring(next);
      TypeElement found = null;
      for (Element m : t.getEnclosedElements()) {
        if (m instanceof TypeElement && m.getSimpleName().contentEquals(part)) {
          found = (TypeElement) m;
        }
      }
      t = found;
    }
    return t;
  }

  private static Element findMember(TypeElement type, String name, String params) {
    List<Element> candidates = new ArrayList<Element>();
    collect(type, candidates, new java.util.HashSet<TypeElement>());
    if (params == null) {
      for (Element e : candidates) {
        if ((e.getKind() == ElementKind.FIELD || e.getKind() == ElementKind.ENUM_CONSTANT)
            && e.getSimpleName().contentEquals(name)) {
          return e;
        }
      }
    }
    boolean ctor = type.getSimpleName().contentEquals(name);
    for (Element e : candidates) {
      if (!(e instanceof ExecutableElement)) {
        continue;
      }
      boolean isCtor = e.getKind() == ElementKind.CONSTRUCTOR;
      if (isCtor ? (!ctor || e.getEnclosingElement() != type) : !e.getSimpleName().contentEquals(name)) {
        continue;
      }
      if (params == null || matches((ExecutableElement) e, params)) {
        return e;
      }
    }
    return null;
  }

  private static void collect(TypeElement t, List<Element> out, java.util.Set<TypeElement> seen) {
    if (t == null || !seen.add(t)) {
      return;
    }
    out.addAll(t.getEnclosedElements());
    javax.lang.model.type.TypeMirror sup = t.getSuperclass();
    if (sup instanceof javax.lang.model.type.DeclaredType) {
      collect((TypeElement) ((javax.lang.model.type.DeclaredType) sup).asElement(), out, seen);
    }
    for (javax.lang.model.type.TypeMirror i : t.getInterfaces()) {
      if (i instanceof javax.lang.model.type.DeclaredType) {
        collect((TypeElement) ((javax.lang.model.type.DeclaredType) i).asElement(), out, seen);
      }
    }
  }

  private static boolean matches(ExecutableElement m, String params) {
    List<String> want = new ArrayList<String>();
    int depth = 0;
    StringBuilder cur = new StringBuilder();
    for (int i = 0; i < params.length(); i++) {
      char c = params.charAt(i);
      if (c == '<') {
        depth++;
      } else if (c == '>') {
        depth--;
      } else if (c == ',' && depth == 0) {
        want.add(cur.toString().trim());
        cur.setLength(0);
      } else if (depth == 0) {
        cur.append(c);
      }
    }
    if (cur.toString().trim().length() > 0) {
      want.add(cur.toString().trim());
    }
    List<? extends VariableElement> have = m.getParameters();
    if (have.size() != want.size()) {
      return false;
    }
    for (int i = 0; i < have.size(); i++) {
      String h = erasure(have.get(i).asType().toString());
      String w = want.get(i).replace("...", "[]").replaceAll("\\s+", "");
      if (!h.equals(w) && !h.endsWith("." + w)) {
        return false;
      }
    }
    return true;
  }

  private static String erasure(String t) {
    StringBuilder sb = new StringBuilder();
    int depth = 0;
    for (int i = 0; i < t.length(); i++) {
      char c = t.charAt(i);
      if (c == '<') {
        depth++;
      } else if (c == '>') {
        depth--;
      } else if (depth == 0) {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
