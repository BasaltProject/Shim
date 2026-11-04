/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class TreesImpl extends com.sun.source.util.DocTrees {
  protected final JavacTaskImpl task;
  private final com.sun.source.util.DocSourcePositions positions = new com.sun.source.util.DocSourcePositions() {
    public long getStartPosition(CompilationUnitTree cu, com.sun.source.doctree.DocCommentTree c, com.sun.source.doctree.DocTree t) {
      return -1;
    }

    public long getEndPosition(CompilationUnitTree cu, com.sun.source.doctree.DocCommentTree c, com.sun.source.doctree.DocTree t) {
      return -1;
    }

    public long getStartPosition(CompilationUnitTree cu, Tree tree) {
      return tree instanceof N.Base ? ((N.Base) tree).start : -1;
    }

    public long getEndPosition(CompilationUnitTree cu, Tree tree) {
      return tree instanceof N.Base ? ((N.Base) tree).end : -1;
    }
  };

  private static final java.util.Map<JavacTaskImpl, TreesImpl> INSTANCES =
      new java.util.WeakHashMap<JavacTaskImpl, TreesImpl>();

  public static synchronized TreesImpl instance(JavacTaskImpl task) {
    TreesImpl t = INSTANCES.get(task);
    if (t == null) {
      t = new TreesImpl(task);
      INSTANCES.put(task, t);
    }
    return t;
  }

  protected TreesImpl(JavacTaskImpl task) {
    this.task = task;
  }

  private java.text.BreakIterator breakIterator;

  public java.text.BreakIterator getBreakIterator() {
    return breakIterator;
  }

  public String getCharacters(com.sun.source.doctree.EntityTree tree) {
    return DocEntities.characters(tree.getName().toString());
  }

  public javax.lang.model.util.Elements.DocCommentKind getDocCommentKind(TreePath path) {
    CompilationUnitTree cu = path.getCompilationUnit();
    if (!(cu instanceof N.CompilationUnit)) {
      return null;
    }
    String raw = ((N.CompilationUnit) cu).docComments.get(path.getLeaf());
    if (raw == null) {
      return null;
    }
    return raw.startsWith("///") ? javax.lang.model.util.Elements.DocCommentKind.END_OF_LINE
        : javax.lang.model.util.Elements.DocCommentKind.TRADITIONAL;
  }

  public com.sun.source.doctree.DocCommentTree getDocCommentTree(TreePath path) {
    String c = getDocComment(path);
    return c == null ? null : DocParser.parse(c);
  }

  public com.sun.source.doctree.DocCommentTree getDocCommentTree(Element e, String relativePath)
      throws java.io.IOException {
    CompilationUnitTree cu = unitOf(e);
    java.net.URI base = cu instanceof N.CompilationUnit && ((N.CompilationUnit) cu).sourceFile != null
        ? ((N.CompilationUnit) cu).sourceFile.toUri() : null;
    if (base == null) {
      throw new java.io.FileNotFoundException(relativePath);
    }
    java.nio.file.Path dir = java.nio.file.Paths.get(base).getParent();
    return parseFile(new String(java.nio.file.Files.readAllBytes(dir.resolve(relativePath)),
        java.nio.charset.StandardCharsets.UTF_8));
  }

  private static com.sun.source.doctree.DocCommentTree parseFile(String content) {
    String lower = content.toLowerCase(java.util.Locale.ROOT);
    int b = lower.indexOf("<body");
    if (b >= 0) {
      int open = lower.indexOf('>', b);
      int close = lower.lastIndexOf("</body>");
      content = content.substring(open + 1, close < 0 ? content.length() : close);
    }
    return DocParser.parse(content);
  }

  private CompilationUnitTree unitOf(Element e) {
    for (Element x = e; x != null; x = x.getEnclosingElement()) {
      if (x instanceof CSym && ((CSym) x).cu != null) {
        return ((CSym) x).cu;
      }
    }
    if (e instanceof javax.lang.model.element.PackageElement) {
      for (CompilationUnitTree u : task.units()) {
        if (u.getPackage() != null && u.getPackageName().toString().equals(
            ((javax.lang.model.element.PackageElement) e).getQualifiedName().toString())) {
          return u;
        }
      }
    }
    return null;
  }

  public com.sun.source.doctree.DocCommentTree getDocCommentTree(Element e) {
    Tree t = getTree(e);
    CompilationUnitTree cu = null;
    if (e instanceof CSym) {
      cu = ((CSym) e).cu;
    } else if (e instanceof Sym && ((Sym) e).owner instanceof CSym) {
      cu = ((CSym) ((Sym) e).owner).cu;
    }
    if (t == null || !(cu instanceof N.CompilationUnit)) {
      return null;
    }
    String raw = ((N.CompilationUnit) cu).docComments.get(t);
    return raw == null ? null : DocParser.parse(DocText.strip(raw));
  }

  public com.sun.source.doctree.DocCommentTree getDocCommentTree(javax.tools.FileObject fileObject) {
    try {
      return parseFile(fileObject.getCharContent(true).toString());
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public com.sun.source.util.DocTreeFactory getDocTreeFactory() {
    return new DocFactory();
  }

  public com.sun.source.util.DocTreePath getDocTreePath(javax.tools.FileObject fileObject,
      javax.lang.model.element.PackageElement packageElement) {
    com.sun.source.doctree.DocCommentTree doc = getDocCommentTree(fileObject);
    CompilationUnitTree cu = unitOf(packageElement);
    if (cu == null) {
      return null;
    }
    return new com.sun.source.util.DocTreePath(new TreePath(cu), doc);
  }

  public Element getElement(com.sun.source.util.DocTreePath path) {
    com.sun.source.doctree.DocTree leaf = path.getLeaf();
    Element context = getElement(path.getTreePath());
    if (leaf instanceof com.sun.source.doctree.ReferenceTree) {
      return DocRefs.resolve(task, path.getTreePath().getCompilationUnit(), context,
          ((com.sun.source.doctree.ReferenceTree) leaf).getSignature());
    }
    if (leaf instanceof com.sun.source.doctree.LinkTree) {
      return DocRefs.resolve(task, path.getTreePath().getCompilationUnit(), context,
          ((com.sun.source.doctree.LinkTree) leaf).getReference().getSignature());
    }
    if (leaf instanceof com.sun.source.doctree.SeeTree) {
      for (com.sun.source.doctree.DocTree t : ((com.sun.source.doctree.SeeTree) leaf).getReference()) {
        if (t instanceof com.sun.source.doctree.ReferenceTree) {
          return DocRefs.resolve(task, path.getTreePath().getCompilationUnit(), context,
              ((com.sun.source.doctree.ReferenceTree) t).getSignature());
        }
      }
      return null;
    }
    if (leaf instanceof com.sun.source.doctree.ThrowsTree) {
      return DocRefs.resolve(task, path.getTreePath().getCompilationUnit(), context,
          ((com.sun.source.doctree.ThrowsTree) leaf).getExceptionName().getSignature());
    }
    if (leaf instanceof com.sun.source.doctree.IdentifierTree) {
      com.sun.source.util.DocTreePath parent = path.getParentPath();
      if (parent != null && parent.getLeaf() instanceof com.sun.source.doctree.ParamTree) {
        return DocRefs.param(context, (com.sun.source.doctree.ParamTree) parent.getLeaf());
      }
      return null;
    }
    if (leaf instanceof com.sun.source.doctree.DocCommentTree) {
      return context;
    }
    return null;
  }

  public java.util.List<com.sun.source.doctree.DocTree> getFirstSentence(
      java.util.List<? extends com.sun.source.doctree.DocTree> list) {
    return DocParser.firstSentence(new java.util.ArrayList<com.sun.source.doctree.DocTree>(list));
  }

  public TypeMirror getType(com.sun.source.util.DocTreePath path) {
    Element e = getElement(path);
    if (e == null) {
      return null;
    }
    com.sun.source.doctree.DocTree leaf = path.getLeaf();
    String sig = leaf instanceof com.sun.source.doctree.ReferenceTree
        ? ((com.sun.source.doctree.ReferenceTree) leaf).getSignature() : null;
    if (sig != null && sig.indexOf('#') > 0) {
      Element cls = DocRefs.resolve(task, path.getTreePath().getCompilationUnit(), getElement(path.getTreePath()),
          sig.substring(0, sig.indexOf('#')));
      if (cls != null) {
        return task.getTypes().erasure(cls.asType());
      }
    }
    return e.asType();
  }

  public void printMessage(Diagnostic.Kind kind, CharSequence msg, com.sun.source.doctree.DocTree t,
      com.sun.source.doctree.DocCommentTree c, CompilationUnitTree root) {
    if (root instanceof N.CompilationUnit) {
      N.CompilationUnit cu = (N.CompilationUnit) root;
      task.report(kind, cu.sourceFile, task.lineMapOf(cu), -1, -1, -1, "compiler.note.proc.messager",
          msg.toString());
    }
  }

  public void setBreakIterator(java.text.BreakIterator breakIterator) {
    this.breakIterator = breakIterator;
  }

  public com.sun.source.util.DocSourcePositions getSourcePositions() {
    return positions;
  }

  public TreePath getPath(CompilationUnitTree unit, Tree node) {
    return TreePath.getPath(unit, node);
  }

  public String getDocComment(TreePath path) {
    CompilationUnitTree cu = path.getCompilationUnit();
    if (!(cu instanceof N.CompilationUnit)) {
      return null;
    }
    String raw = ((N.CompilationUnit) cu).docComments.get(path.getLeaf());
    return raw == null ? null : DocText.strip(raw);
  }

  static Tree unparen(Tree t) {
    while (t instanceof com.sun.source.tree.ParenthesizedTree) {
      t = ((com.sun.source.tree.ParenthesizedTree) t).getExpression();
    }
    return t;
  }

  static TypeMirror typeOf(Pipeline p, Tree t) {
    if (t == null) {
      return null;
    }
    Object o = p.enter.classOf.get(t);
    if (o instanceof CSym) {
      return ((CSym) o).type();
    }
    Ty ty = p.attr.typeOf(t);
    if (ty != null) {
      return ty;
    }
    Object s = p.enter.symOf.get(t);
    if (s instanceof Sym) {
      return ((Sym) s).asType();
    }
    return null;
  }

  static Element elementOf(Pipeline p, Tree t) {
    t = unparen(t);
    Object s = p.enter.symOf.get(t);
    if (s instanceof Element) {
      return (Element) s;
    }
    s = p.attr.syms.get(t);
    if (s instanceof Element) {
      return (Element) s;
    }
    if (t instanceof ClassTree) {
      return p.enter.classOf.get(t);
    }
    return null;
  }

  public Element getElement(TreePath path) {
    try {
      return elementOf(task.attributed(), path.getLeaf());
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public TypeMirror getLub(CatchTree tree) {
    throw new UnsupportedOperationException();
  }

  public TypeMirror getOriginalType(ErrorType errorType) {
    throw new UnsupportedOperationException();
  }

  public TreePath getPath(Element e, AnnotationMirror a, AnnotationValue v) {
    throw new UnsupportedOperationException();
  }

  public TreePath getPath(Element e, AnnotationMirror a) {
    throw new UnsupportedOperationException();
  }

  public TreePath getPath(Element e) {
    Tree t = getTree(e);
    CompilationUnitTree cu = unitOf(e);
    return t == null || cu == null ? null : TreePath.getPath(cu, t);
  }

  public Scope getScope(TreePath path) {
    throw new UnsupportedOperationException();
  }

  public Tree getTree(Element e, AnnotationMirror a, AnnotationValue v) {
    throw new UnsupportedOperationException();
  }

  public Tree getTree(Element e, AnnotationMirror a) {
    throw new UnsupportedOperationException();
  }

  public Tree getTree(Element e) {
    if (e instanceof CSym) {
      return ((CSym) e).tree;
    }
    if (e instanceof MSym) {
      return ((MSym) e).tree;
    }
    if (e instanceof FSym) {
      return ((FSym) e).tree;
    }
    if (e instanceof VSym) {
      return ((VSym) e).tree;
    }
    return null;
  }

  public MethodTree getTree(ExecutableElement e) {
    return e instanceof MSym ? ((MSym) e).tree : null;
  }

  public ClassTree getTree(TypeElement e) {
    return e instanceof CSym ? ((CSym) e).tree : null;
  }

  public TypeMirror getTypeMirror(TreePath path) {
    try {
      return typeOf(task.attributed(), path.getLeaf());
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public boolean isAccessible(Scope scope, Element member, DeclaredType type) {
    throw new UnsupportedOperationException();
  }

  public boolean isAccessible(Scope scope, TypeElement type) {
    throw new UnsupportedOperationException();
  }

  public void printMessage(Diagnostic.Kind kind, CharSequence msg, Tree t, CompilationUnitTree root) {
    if (root instanceof N.CompilationUnit) {
      N.CompilationUnit cu = (N.CompilationUnit) root;
      long pos = t instanceof N.Base ? ((N.Base) t).pos : -1;
      task.report(kind, cu.sourceFile, task.lineMapOf(cu), pos, positions.getStartPosition(cu, t),
          positions.getEndPosition(cu, t), "compiler.note.proc.messager", msg.toString());
    }
  }
}
