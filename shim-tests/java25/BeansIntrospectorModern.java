/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.BeanProperty;
import java.beans.ConstructorProperties;
import java.beans.EventSetDescriptor;
import java.beans.FeatureDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.JavaBean;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.beans.SimpleBeanInfo;
import java.beans.Transient;
import java.beans.VetoableChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;
import java.util.TooManyListenersException;
import java.util.TreeMap;

public class BeansIntrospectorModern {
  public static class Simple {
    public int getA() { return 0; }
    public void setA(int a) { }
    public boolean isFlag() { return true; }
    public void setFlag(boolean f) { }
    public String getName() { return null; }
    public void setName(String n) { }
    public String getRO() { return "ro"; }
    public void setWO(String w) { }
    public static int getStatic() { return 1; }
    public static void setStatic(int x) { }
    protected int getProt() { return 1; }
    int getPkg() { return 1; }
    public int get() { return 1; }
    public void set(int x) { }
    public boolean is() { return true; }
  }

  public static class Names {
    public int getURL() { return 1; }
    public void setURL(int u) { }
    public int getXYz() { return 1; }
    public int getX() { return 1; }
    public int getabc() { return 1; }
    public int getaBc() { return 1; }
    public void setABc(int x) { }
    public int get1x() { return 1; }
    public int getUPPER() { return 1; }
    public int getIO() { return 1; }
    public int getA() { return 1; }
    public int getAb() { return 1; }
    public int getAB() { return 1; }
    public int getaB() { return 1; }
    public int get_under() { return 1; }
    public int getQ$() { return 1; }
  }

  public static class Bools {
    public boolean getBool() { return true; }
    public boolean isBool() { return true; }
    public void setBool(boolean b) { }
    public Boolean getWrapped() { return true; }
    public Boolean isWrapped() { return true; }
    public int isInt() { return 1; }
    public void setInt(int i) { }
    public boolean isOnlyIs() { return true; }
    public boolean getOnlyGet() { return true; }
    public void setOnlyGet(boolean b) { }
    public Boolean getBoxed() { return null; }
    public void setBoxed(Boolean b) { }
    public void setBoxed(boolean b) { }
    public boolean isPrim() { return true; }
    public void setPrim(Boolean b) { }
  }

  public static class Indexed {
    public String[] getArr() { return null; }
    public void setArr(String[] a) { }
    public String getArr(int i) { return null; }
    public void setArr(int i, String s) { }
    public int getOnlyIdx(int i) { return 0; }
    public void setOnlyWrite(int i, int v) { }
    public int[] getMixed() { return null; }
    public int getMixed(int i) { return 0; }
    public void setMixed(int i, int v) { }
    public String getOtherType(int i) { return null; }
    public void setOtherType(int i, Integer v) { }
    public Object[] getObjs() { return null; }
    public void setObjs(String[] x) { }
    public String getObjs(int i) { return null; }
    public boolean isBoolIdx(int i) { return true; }
    public boolean[] getBoolIdx() { return null; }
  }

  public static class Overloads {
    public int getX() { return 1; }
    public void setX(int x) { }
    public void setX(String s) { }
    public void setX(long l) { }
    public String getY() { return null; }
    public void setY(Object o) { }
    public void setY(int i) { }
    public String getM() { return null; }
    public void setM(int i) { }
    public void setOnly(int i) { }
    public void setOnly(String s) { }
    public Number getN() { return 1; }
    public void setN(Integer i) { }
    public void setN(Number n) { }
    public void setN(Object o) { }
  }

  public static class Base<T> {
    public T getValue() { return null; }
    public void setValue(T t) { }
    public Number getNum() { return 1; }
    public int getBaseOnly() { return 1; }
    public void setBaseOnly(int x) { }
    public String getOver() { return "base"; }
    public void setOver(String s) { }
  }

  public static class Sub extends Base<String> {
    @Override public String getValue() { return "x"; }
    @Override public void setValue(String s) { }
    @Override public Integer getNum() { return 1; }
    @Override public String getOver() { return "sub"; }
    public int getSubOnly() { return 2; }
  }

  public interface HasProps {
    int getIf();
    void setIf(int x);
    default String getDef() { return "d"; }
    static int getSt() { return 1; }
    boolean isDefFlag();
  }

  public abstract static class AbstractImpl implements HasProps {
    public int getIf() { return 1; }
    public abstract int getAbs();
  }

  public static class Impl extends AbstractImpl {
    public void setIf(int x) { }
    public int getAbs() { return 0; }
    public boolean isDefFlag() { return true; }
  }

  static class PkgPrivate {
    public int getPub() { return 1; }
    public void setPub(int x) { }
  }

  public static class PkgChild extends PkgPrivate {
    public int getOwn() { return 1; }
  }

  public interface FooListener extends EventListener {
    void fooHappened(EventObject e);
    void barHappened(EventObject e, int x);
  }

  public interface BazListener extends EventListener {
    void baz(EventObject e);
  }

  public interface BadListener extends EventListener {
    void bad(String notAnEvent);
  }

  public static class Events {
    public void addFooListener(FooListener l) { }
    public void removeFooListener(FooListener l) { }
    public FooListener[] getFooListeners() { return null; }
    public void addBazListener(BazListener l) throws TooManyListenersException { }
    public void removeBazListener(BazListener l) { }
    public void addBadListener(BadListener l) { }
    public void removeBadListener(BadListener l) { }
    public void addLonely(FooListener l) { }
    public void addNoRemoveListener(BazListener l) { }
    public void removeNoAddListener(BazListener l) { }
  }

  public static class Bound {
    private final java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
    public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }
    public int getA() { return 0; }
    public void setA(int a) { }
    public int getRo() { return 0; }
  }

  public static class Constrained {
    public void addVetoableChangeListener(VetoableChangeListener l) { }
    public void removeVetoableChangeListener(VetoableChangeListener l) { }
    public void addPropertyChangeListener(PropertyChangeListener l) { }
    public void removePropertyChangeListener(PropertyChangeListener l) { }
    public int getA() { return 0; }
    public void setA(int a) throws PropertyVetoException { }
    public int getB() { return 0; }
    public void setB(int b) { }
  }

  @JavaBean(description = "annotated bean", defaultProperty = "second", defaultEventSet = "fooListener")
  public static class Annotated {
    @BeanProperty(bound = false, expert = true, hidden = true, preferred = true, required = true,
        visualUpdate = true, description = "the first", enumerationValues = {"java.lang.Integer.MAX_VALUE", "java.lang.Integer.MIN_VALUE"})
    public int getFirst() { return 0; }
    public void setFirst(int f) { }
    public int getSecond() { return 0; }
    public void setSecond(int f) { }
    @Transient public int getTrans() { return 0; }
    public void setTrans(int t) { }
    @BeanProperty(description = "setter desc") public void setOnSetter(int t) { }
    public int getOnSetter() { return 0; }
    public void addFooListener(FooListener l) { }
    public void removeFooListener(FooListener l) { }
    @ConstructorProperties({"a"}) public Annotated(int a) { }
    public Annotated() { }
    @BeanProperty(bound = true) public int getB() { return 0; }
    @BeanProperty(description = "idx") public String[] getIdx() { return null; }
    public void setIdx(String[] x) { }
    public String getIdx(int i) { return null; }
    public void setIdx(int i, String s) { }
  }

  public static class WithExplicit {
    public int getA() { return 0; }
    public void setA(int a) { }
    public int getB() { return 0; }
    public int getC() { return 0; }
  }

  public static class WithExplicitBeanInfo extends SimpleBeanInfo {
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
      try {
        PropertyDescriptor a = new PropertyDescriptor("a", WithExplicit.class);
        a.setDisplayName("The A");
        a.setShortDescription("explicit a");
        a.setExpert(true);
        return new PropertyDescriptor[] {a};
      } catch (IntrospectionException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
      BeanDescriptor d = new BeanDescriptor(WithExplicit.class);
      d.setDisplayName("Explicit Bean");
      return d;
    }

    @Override
    public int getDefaultPropertyIndex() {
      return 0;
    }
  }

  public static class Inner {
    public Inner() { }
    public Inner(int x) { }
    public static class Nested {
      public int getN() { return 0; }
    }
  }

  public enum Color { RED, GREEN }

  public record Rec(int x, String name) { }

  public static class Covariant {
    public Object getThing() { return null; }
  }

  public static class CovariantSub extends Covariant {
    @Override public String getThing() { return null; }
    public void setThing(String s) { }
  }

  public static class Generic<K, V extends Number> {
    public K getKey() { return null; }
    public void setKey(K k) { }
    public V getVal() { return null; }
    public List<K> getKeys() { return null; }
    public void setKeys(List<K> k) { }
    public K[] getKArr() { return null; }
  }

  public static class Throwing {
    public int getA() throws Exception { return 0; }
    public void setA(int a) throws java.io.IOException { }
    public int getB() throws RuntimeException { return 0; }
  }

  public static class Synchronized {
    public synchronized int getA() { return 0; }
    public final void setA(int a) { }
    public native int getN();
    public strictfp double getD() { return 0; }
  }

  public static class WriteOnlyOverloads {
    public void setA(int x) { }
    public void setA(String s) { }
    public void setB(String s) { }
    public void setB(Integer i) { }
    public void setC(Object o) { }
    public void setC(String s) { }
    public void setD(long l) { }
    public void setD(int i) { }
    public void setE(Number n) { }
    public void setE(Integer i) { }
    public void setF(List<?> l) { }
    public void setF(ArrayList<?> l) { }
    public void setG(int i) { }
    public void setG(Integer i) { }
    public void setH(boolean b) { }
    public void setH(Boolean b) { }
  }

  public static class Fluent {
    public Fluent setA(int a) { return this; }
    public int getA() { return 0; }
    public Object setB(int b) { return null; }
    public int getB() { return 0; }
    public void setC(int c) { }
    public Fluent setD(int d) { return this; }
  }

  public static class Boolish {
    public boolean isA() { return true; }
    public boolean getA() { return false; }
    public Boolean getB() { return null; }
    public boolean isB() { return false; }
    public int getT() { return 0; }
    public boolean isT() { return true; }
    public void setT(boolean t) { }
    public void setT(int t) { }
    public boolean isU() { return true; }
    public void setU(String s) { }
  }

  private static class Priv {
    public int getX() { return 1; }
    public void setX(int x) { }
  }

  public static class Container {
    public List<String> getList() { return null; }
    public void setList(List<String> l) { }
    public java.util.Map<String, Integer> getMap() { return null; }
    public int[][] getMatrix() { return null; }
    public void setMatrix(int[][] m) { }
    public char getCh() { return 'x'; }
    public void setCh(char c) { }
    public byte[] getBytes() { return null; }
    public void setBytes(byte[] b) { }
    public void setBytes(int i, byte b) { }
    public byte getBytes(int i) { return 0; }
  }

  public static class Duplicate {
    public int getA() { return 0; }
    public void setA(int a) { }
    public int getb() { return 0; }
    public int getB() { return 0; }
  }

  public static class PcBase {
    public void addPropertyChangeListener(PropertyChangeListener l) { }
    public void removePropertyChangeListener(PropertyChangeListener l) { }
    public int getP() { return 0; }
    public void setP(int p) { }
  }

  public static class PcSub extends PcBase {
    public int getQ() { return 0; }
    public void setQ(int q) { }
  }

  public static class NoPc {
    public int getP() { return 0; }
    public void setP(int p) { }
  }

  public static class PcSub2 extends NoPc {
    public void addPropertyChangeListener(PropertyChangeListener l) { }
    public void removePropertyChangeListener(PropertyChangeListener l) { }
    public int getQ() { return 0; }
  }

  public static class OnlyAdd {
    public void addPropertyChangeListener(PropertyChangeListener l) { }
    public int getP() { return 0; }
  }

  public static class NamedPc {
    public void addPropertyChangeListener(String name, PropertyChangeListener l) { }
    public void removePropertyChangeListener(String name, PropertyChangeListener l) { }
    public int getP() { return 0; }
  }

  public static class SubEvents extends Events {
    public void addQuxListener(BazListener l) { }
    public void removeQuxListener(BazListener l) { }
  }

  public static class Casing {
    public int getX() { return 0; }
    public int getx() { return 0; }
    public int getURL() { return 0; }
    public int getUrl() { return 0; }
    public void setY(int y) { }
    public void sety(int y) { }
    public int getY() { return 0; }
  }

  public interface IA { int getA(); }

  public interface IB extends IA { void setA(int a); int getB(); }

  public interface IC { int getA(); String getC(); }

  public abstract static class Diamond implements IB, IC { }

  public static class BaseSetter {
    public void setM(String s) { }
    public void setN(Number n) { }
  }

  public static class SubGetter extends BaseSetter {
    public int getM() { return 0; }
    public Integer getN() { return 0; }
  }

  public static class Varargs {
    public int getV(int... xs) { return 0; }
    public void setW(int... xs) { }
    public void setZ(String[] zs) { }
    public String[] getZ() { return null; }
  }

  public abstract static class AbsBase {
    public abstract int getZ();
    public abstract void setZ(int z);
    public abstract int getOnlyAbs();
  }

  public abstract static class AbsMid extends AbsBase {
    public int getMidOwn() { return 0; }
  }

  public static class AbsLeaf extends AbsMid {
    public int getZ() { return 0; }
    public void setZ(int z) { }
    public int getOnlyAbs() { return 0; }
  }

  public interface IDef { default int getD() { return 0; } default void setD(int d) { } int getAbstractOne(); }

  public interface IDef2 extends IDef { default int getE() { return 0; } }

  public static class UsesDef implements IDef2 { public int getAbstractOne() { return 0; } }

  public static class SplitPc extends NoPc {
    public void addPropertyChangeListener(PropertyChangeListener l) { }
    public void removePropertyChangeListener(PropertyChangeListener l) { }
    public void setP(int p) { }
    public void setR(int r) { }
  }

  public static class IndexOnlyBase {
    public String getItem(int i) { return null; }
  }

  public static class IndexSub extends IndexOnlyBase {
    public void setItem(int i, String s) { }
    public String[] getItem() { return null; }
  }

  public static class AnnA {
    @BeanProperty(bound = true) public int getA() { return 0; }
    public void setA(int a) { }
    @BeanProperty(bound = true) public int getB() { return 0; }
    @BeanProperty public int getC() { return 0; }
    public void setC(int c) { }
    public void addPropertyChangeListener(PropertyChangeListener l) { }
    public void removePropertyChangeListener(PropertyChangeListener l) { }
    @BeanProperty(bound = false) public int getD() { return 0; }
    public void setD(int d) { }
    public int getE() { return 0; }
    @BeanProperty(description = "on setter", hidden = true) public void setE(int e) { }
    @BeanProperty(expert = true) public int getF() { return 0; }
    @BeanProperty(hidden = true) public void setF(int f) { }
    public int getG() { return 0; }
    @Transient(false) public void setG(int g) { }
    @Transient public int getH() { return 0; }
    @Transient(false) public void setH(int h) { }
    @BeanProperty(preferred = true, description = "pref") public int getI() { return 0; }
    @BeanProperty(required = true) public int getJ() { return 0; }
    @BeanProperty(visualUpdate = true) public int getK() { return 0; }
    @BeanProperty(enumerationValues = {"java.lang.Integer.MAX_VALUE"}) public int getL() { return 0; }
    public void setL(int l) { }
    @BeanProperty(description = "idx") public int[] getM() { return null; }
    public void setM(int[] m) { }
    @BeanProperty(description = "idx element", expert = true) public int getM(int i) { return 0; }
    public void setM(int i, int v) { }
    @BeanProperty(description = "event-only") public void addFooListener(FooListener l) { }
    public void removeFooListener(FooListener l) { }
  }

  @JavaBean public static class JbEmpty { public int getA() { return 0; } }

  @JavaBean(description = "desc only") public static class JbDesc { public int getA() { return 0; } }

  @JavaBean(defaultProperty = "a") public static class JbDefProp {
    public int getA() { return 0; }
    public int getB() { return 0; }
  }

  @JavaBean(defaultProperty = "nope") public static class JbBadDefProp { public int getA() { return 0; } }

  @JavaBean(defaultEventSet = "foo") public static class JbDefEvent {
    public void addFooListener(FooListener l) { }
    public void removeFooListener(FooListener l) { }
    public void addBazListener(BazListener l) { }
    public void removeBazListener(BazListener l) { }
  }

  @JavaBean(defaultEventSet = "baz", defaultProperty = "b") public static class JbBoth {
    public void addFooListener(FooListener l) { }
    public void removeFooListener(FooListener l) { }
    public void addBazListener(BazListener l) { }
    public void removeBazListener(BazListener l) { }
    public int getA() { return 0; }
    public int getB() { return 0; }
    public int getC() { return 0; }
  }

  public static class ExplicitSubOwner extends WithExplicit {
    public int getD() { return 0; }
  }

  public static class EvDup {
    public void addFooListener(FooListener l) { }
    public void removeFooListener(FooListener l) { }
    public void addAlphaListener(FooListener l) { }
    public void removeAlphaListener(FooListener l) { }
    public void addZetaListener(BazListener l) { }
    public void removeZetaListener(BazListener l) { }
  }

  public static class EvMismatch {
    public void addFooListener(FooListener l) { }
    public void removeFooListener(BazListener l) { }
    public void addBazListener(BazListener l) { }
    public int removeBazListener(BazListener l) { return 0; }
    public void addPlainListener(Object l) { }
    public void removePlainListener(Object l) { }
  }

  public static class EvGetter {
    public void addFooListener(FooListener l) { }
    public void removeFooListener(FooListener l) { }
    public java.util.List<FooListener> getFooListeners() { return null; }
    public BazListener[] getBazListeners() { return null; }
    public void addBazListener(BazListener l) { }
    public void removeBazListener(BazListener l) { }
  }

  static void p(String s) {
    System.out.println(s);
  }

  static String sig(Method m) {
    if (m == null) {
      return "-";
    }
    boolean library = m.getDeclaringClass().getName().indexOf('.') >= 0;
    StringBuilder sb = new StringBuilder((library ? "" : m.getDeclaringClass().getSimpleName() + ".") + m.getName() + "(");
    Class<?>[] types = m.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      sb.append(i > 0 ? "," : "").append(types[i].getSimpleName());
    }
    return sb.append(")").toString();
  }

  static String attrs(FeatureDescriptor f) {
    TreeMap<String, Object> map = new TreeMap<>();
    Enumeration<String> e = f.attributeNames();
    while (e.hasMoreElements()) {
      String n = e.nextElement();
      Object v = f.getValue(n);
      map.put(n, v instanceof Object[] ? Arrays.deepToString((Object[]) v) : v);
    }
    return map.toString();
  }

  static String desc(FeatureDescriptor f) {
    return "n=" + f.getName() + " dn=" + f.getDisplayName() + " sd=" + f.getShortDescription() + " ex="
        + f.isExpert() + " hi=" + f.isHidden() + " pr=" + f.isPreferred() + " at=" + attrs(f);
  }

  static void dump(Class<?> c, int flags, Class<?> stop) {
    StringBuilder sb = new StringBuilder("## " + c.getName().replace("BeansIntrospectorModern$", "") + " flags=" + flags
        + " stop=" + (stop == null ? "-" : stop.getSimpleName()) + "\n");
    try {
      BeanInfo bi = stop != null ? Introspector.getBeanInfo(c, stop, flags) : Introspector.getBeanInfo(c, flags);
      BeanDescriptor bd = bi.getBeanDescriptor();
      sb.append("bean " + (bd == null ? "null" : bd.getBeanClass().getSimpleName() + " cust=" + bd.getCustomizerClass()
          + " " + desc(bd)) + "\n");
      PropertyDescriptor[] ps = bi.getPropertyDescriptors();
      sb.append("defprop=" + bi.getDefaultPropertyIndex() + " defevent=" + bi.getDefaultEventIndex() + " nprops="
          + (ps == null ? -1 : ps.length) + "\n");
      if (ps != null) {
        for (PropertyDescriptor pd : ps) {
          sb.append("  prop " + pd.getName() + " type="
              + (pd.getPropertyType() == null ? "null" : pd.getPropertyType().getTypeName()) + " r=" + sig(pd.getReadMethod())
              + " w=" + sig(pd.getWriteMethod()) + " bound=" + pd.isBound() + " constr=" + pd.isConstrained()
              + " ed=" + pd.getPropertyEditorClass() + " cls=" + pd.getClass().getSimpleName() + " " + desc(pd));
          if (pd instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor ip = (IndexedPropertyDescriptor) pd;
            sb.append(" itype=" + ip.getIndexedPropertyType() + " ir=" + sig(ip.getIndexedReadMethod()) + " iw="
                + sig(ip.getIndexedWriteMethod()));
          }
          sb.append("\n");
        }
      }
      EventSetDescriptor[] es = bi.getEventSetDescriptors();
      if (es != null) {
        for (EventSetDescriptor e : es) {
          List<String> lm = new ArrayList<>();
          for (Method m : e.getListenerMethods()) {
            lm.add(sig(m));
          }
          Collections.sort(lm);
          sb.append("  event " + e.getName() + " lt=" + e.getListenerType().getSimpleName() + " add="
              + sig(e.getAddListenerMethod()) + " rem=" + sig(e.getRemoveListenerMethod()) + " get="
              + sig(e.getGetListenerMethod()) + " uni=" + e.isUnicast() + " def=" + e.isInDefaultEventSet() + " lm="
              + lm + " " + desc(e) + "\n");
        }
      }
      MethodDescriptor[] ms = bi.getMethodDescriptors();
      List<String> mm = new ArrayList<>();
      if (ms != null) {
        for (MethodDescriptor d : ms) {
          mm.add(sig(d.getMethod()) + Arrays.toString(d.getMethod().getParameterTypes()));
        }
      }
      Collections.sort(mm);
      sb.append("  methods(" + mm.size() + ") " + mm + "\n");
    } catch (Throwable t) {
      sb.append("ERR " + t.getClass().getName() + ": " + t.getMessage() + "\n");
    }
    System.out.print(sb);
  }

  public static void main(String[] args) throws Exception {
    Class<?>[] beans = {Simple.class, Names.class, Bools.class, Indexed.class, Overloads.class, Base.class,
        Sub.class, HasProps.class, AbstractImpl.class, Impl.class, PkgPrivate.class, PkgChild.class, Events.class,
        Bound.class, Constrained.class, Annotated.class, WithExplicit.class, Inner.class, Inner.Nested.class,
        Color.class, Rec.class, Covariant.class, CovariantSub.class, Generic.class, Throwing.class,
        Synchronized.class, WriteOnlyOverloads.class, Fluent.class, Boolish.class, Priv.class, Container.class,
        Duplicate.class, Runnable.class, new Object() { public int getAnon() { return 1; } }.getClass(),
        int[].class, String[].class, PcBase.class, PcSub.class, NoPc.class, PcSub2.class, OnlyAdd.class,
        NamedPc.class, SubEvents.class, Casing.class, IA.class, IB.class, IC.class, Diamond.class, BaseSetter.class,
        SubGetter.class, Varargs.class, AbsBase.class, AbsMid.class, AbsLeaf.class, IDef.class, IDef2.class,
        UsesDef.class, SplitPc.class, IndexOnlyBase.class, IndexSub.class, AnnA.class, JbEmpty.class, JbDesc.class,
        JbDefProp.class, JbBadDefProp.class, JbDefEvent.class, JbBoth.class, ExplicitSubOwner.class, EvDup.class,
        EvMismatch.class, EvGetter.class};
    for (Class<?> c : beans) {
      dump(c, Introspector.USE_ALL_BEANINFO, null);
    }
    dump(Sub.class, Introspector.IGNORE_ALL_BEANINFO, Base.class);
    dump(Sub.class, Introspector.IGNORE_IMMEDIATE_BEANINFO, Object.class);
    dump(Impl.class, Introspector.USE_ALL_BEANINFO, AbstractImpl.class);
    dump(WithExplicit.class, Introspector.IGNORE_ALL_BEANINFO, null);
    dump(WithExplicit.class, Introspector.IGNORE_IMMEDIATE_BEANINFO, null);

    String[] jdk = {"java.lang.Object", "java.lang.String", "java.lang.Thread", "java.lang.Class",
        "java.lang.Throwable", "java.lang.Exception", "java.lang.StackTraceElement", "java.lang.Integer",
        "java.lang.StringBuilder", "java.lang.Runtime", "java.lang.ProcessBuilder", "java.lang.ThreadGroup",
        "java.util.ArrayList", "java.util.HashMap", "java.util.Date", "java.util.Locale", "java.util.Properties",
        "java.util.Calendar", "java.util.GregorianCalendar", "java.util.Random", "java.util.BitSet",
        "java.util.TimeZone", "java.util.SimpleTimeZone", "java.util.Currency", "java.util.UUID",
        "java.util.StringTokenizer", "java.util.Timer", "java.util.Vector", "java.util.Stack",
        "java.util.LinkedList", "java.util.TreeMap", "java.util.IdentityHashMap", "java.util.PriorityQueue",
        "java.util.Scanner", "java.util.Formatter", "java.util.Optional", "java.util.Base64",
        "java.io.File", "java.io.PrintStream", "java.io.StringReader", "java.io.ByteArrayOutputStream",
        "java.io.ObjectOutputStream", "java.io.IOException", "java.io.FileReader", "java.io.BufferedReader",
        "java.net.URL", "java.net.URI", "java.net.InetAddress", "java.net.InetSocketAddress", "java.net.Socket",
        "java.net.ServerSocket", "java.net.DatagramSocket", "java.net.HttpURLConnection", "java.net.URLClassLoader",
        "java.net.Proxy", "java.net.NetworkInterface", "java.net.CookieManager",
        "java.text.SimpleDateFormat", "java.text.DecimalFormat", "java.text.DecimalFormatSymbols",
        "java.text.MessageFormat", "java.text.Collator", "java.text.ChoiceFormat", "java.text.NumberFormat",
        "java.time.LocalDate", "java.time.Duration", "java.time.Instant", "java.time.ZoneId", "java.time.Period",
        "java.math.BigDecimal", "java.math.BigInteger", "java.math.MathContext",
        "java.util.concurrent.ThreadPoolExecutor", "java.util.concurrent.ConcurrentHashMap",
        "java.util.concurrent.atomic.AtomicInteger", "java.util.concurrent.ForkJoinPool",
        "java.util.concurrent.CompletableFuture", "java.util.concurrent.CountDownLatch",
        "java.util.concurrent.locks.ReentrantLock", "java.util.concurrent.Semaphore",
        "java.util.logging.Logger", "java.util.logging.Handler", "java.util.logging.ConsoleHandler",
        "java.util.logging.LogRecord", "java.util.logging.FileHandler", "java.util.logging.SimpleFormatter",
        "java.util.zip.ZipEntry", "java.util.zip.Deflater", "java.util.zip.CRC32", "java.util.jar.Manifest",
        "java.util.jar.Attributes", "java.util.regex.Pattern", "java.security.SecureRandom",
        "java.security.KeyPairGenerator", "java.security.MessageDigest", "java.security.Provider",
        "java.nio.ByteBuffer", "java.nio.charset.Charset", "javax.net.ssl.SSLParameters",
        "javax.net.ssl.SSLContext", "javax.security.auth.Subject", "java.sql.Timestamp", "java.sql.Date",
        "java.lang.management.ManagementFactory", "java.lang.ref.WeakReference", "java.lang.reflect.Proxy",
        "javax.management.ObjectName", "java.util.EventObject", "java.beans.PropertyChangeEvent",
        "java.beans.PropertyChangeSupport", "java.beans.PropertyChangeListenerProxy",
        "java.lang.Character", "java.lang.Boolean", "java.lang.Enum", "java.util.EnumMap", "java.util.Collections",
        "java.util.AbstractList", "java.lang.ClassLoader", "java.lang.Package", "java.lang.Module",
        "java.lang.StringBuffer", "java.lang.ThreadLocal", "java.lang.InheritableThreadLocal", "java.util.Arrays"};
    for (String name : jdk) {
      try {
        dump(Class.forName(name), Introspector.USE_ALL_BEANINFO, null);
      } catch (ClassNotFoundException e) {
        p("## " + name + " missing");
      }
    }

    p("--- utility");
    String[] decap = {"", "a", "A", "AB", "Ab", "aB", "ABc", "URL", "Url", "x1", "X1", "Éa", "foo", "Foo", "FOO", "fOO"};
    for (String s : decap) {
      p("decap[" + s + "] = [" + Introspector.decapitalize(s) + "]");
    }
    try {
      Introspector.decapitalize(null);
    } catch (Exception e) {
      p("decap-null " + e.getClass().getName());
    }
    p("beaninfo-path " + Arrays.toString(Introspector.getBeanInfoSearchPath()));
    Introspector.setBeanInfoSearchPath(new String[] {"com.example.beaninfo"});
    p("beaninfo-path2 " + Arrays.toString(Introspector.getBeanInfoSearchPath()));
    Introspector.setBeanInfoSearchPath(new String[0]);
    p("beaninfo-path3 " + Arrays.toString(Introspector.getBeanInfoSearchPath()));
    Introspector.setBeanInfoSearchPath(new String[] {"sun.beans.infos"});
    Introspector.flushCaches();
    Introspector.flushFromCaches(Simple.class);
    try {
      Introspector.getBeanInfo(Simple.class, String.class);
    } catch (IntrospectionException e) {
      p("bad-stop " + e.getMessage());
    }
    try {
      Introspector.getBeanInfo(null);
    } catch (Exception e) {
      p("null-class " + e.getClass().getName());
    }
    BeanInfo cached1 = Introspector.getBeanInfo(Simple.class);
    BeanInfo cached2 = Introspector.getBeanInfo(Simple.class);
    p("cached-same " + (cached1 == cached2));
    Introspector.flushFromCaches(Simple.class);
    p("flushed-different " + (cached1 != Introspector.getBeanInfo(Simple.class)));

    p("--- descriptors");
    PropertyDescriptor pd = new PropertyDescriptor("a", Simple.class);
    p(pd.getName() + " " + sig(pd.getReadMethod()) + " " + sig(pd.getWriteMethod()) + " " + pd.getPropertyType());
    PropertyDescriptor pd2 = new PropertyDescriptor("flag", Simple.class);
    p(pd2.getName() + " " + sig(pd2.getReadMethod()) + " " + sig(pd2.getWriteMethod()));
    PropertyDescriptor pd3 = new PropertyDescriptor("ro", Simple.class, "getRO", null);
    p(pd3.getName() + " " + sig(pd3.getReadMethod()) + " " + sig(pd3.getWriteMethod()));
    try {
      new PropertyDescriptor("nope", Simple.class);
    } catch (IntrospectionException e) {
      p("pd-nope " + e.getMessage());
    }
    try {
      new PropertyDescriptor("", Simple.class);
    } catch (IntrospectionException e) {
      p("pd-empty " + e.getMessage());
    }
    try {
      new PropertyDescriptor(null, Simple.class);
    } catch (IntrospectionException e) {
      p("pd-null " + e.getMessage());
    }
    try {
      new PropertyDescriptor("name", Simple.class, "getName", "setNope");
    } catch (IntrospectionException e) {
      p("pd-nowrite " + e.getMessage());
    }
    PropertyDescriptor pd4 = new PropertyDescriptor("a", Simple.class.getMethod("getA"), Simple.class.getMethod("setA", int.class));
    p("pd-methods " + pd4.getPropertyType() + " " + pd4.equals(pd) + " " + (pd4.hashCode() == pd.hashCode()));
    pd4.setBound(true);
    pd4.setConstrained(true);
    pd4.setPropertyEditorClass(String.class);
    pd4.setDisplayName("Disp");
    pd4.setValue("k", "v");
    p(pd4.isBound() + " " + pd4.isConstrained() + " " + pd4.getPropertyEditorClass() + " " + pd4.getDisplayName() + " "
        + pd4.getValue("k") + " " + attrs(pd4));
    p("pd-equals-after " + pd4.equals(pd));
    p("feature-defaults " + new FeatureDescriptor().getName() + " " + new FeatureDescriptor().getDisplayName() + " "
        + new FeatureDescriptor().getShortDescription());
    FeatureDescriptor fd = new FeatureDescriptor();
    fd.setName("nm");
    p("feature-name " + fd.getName() + " " + fd.getDisplayName() + " " + fd.getShortDescription());
    fd.setDisplayName("dn");
    fd.setShortDescription("sd");
    fd.setValue("x", 1);
    try {
      fd.setValue("x", null);
    } catch (NullPointerException e) {
      p("feature-null-value");
    }
    p("feature " + fd.getDisplayName() + " " + fd.getShortDescription() + " " + attrs(fd) + " " + fd);
    try {
      fd.setValue(null, 1);
    } catch (NullPointerException e) {
      p("feature-null-attr");
    }
    BeanDescriptor bd = new BeanDescriptor(Simple.class);
    p("bd " + bd.getName() + " " + bd.getBeanClass().getSimpleName() + " " + bd.getCustomizerClass());
    BeanDescriptor bd2 = new BeanDescriptor(Simple.class, String.class);
    p("bd2 " + bd2.getCustomizerClass());
    IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor("arr", Indexed.class);
    p("ipd " + ipd.getPropertyType() + " " + ipd.getIndexedPropertyType() + " " + sig(ipd.getReadMethod()) + " "
        + sig(ipd.getWriteMethod()) + " " + sig(ipd.getIndexedReadMethod()) + " " + sig(ipd.getIndexedWriteMethod()));
    try {
      new IndexedPropertyDescriptor("mixed", Indexed.class, "getMixed", null, "getMixed", "setMixed");
      p("ipd-mixed ok");
    } catch (IntrospectionException e) {
      p("ipd-mixed " + e.getMessage());
    }
    EventSetDescriptor esd = new EventSetDescriptor(Events.class, "foo", FooListener.class, new String[] {"fooHappened"}, "addFooListener",
        "removeFooListener");
    p("esd " + esd.getName() + " " + sig(esd.getAddListenerMethod()) + " " + sig(esd.getRemoveListenerMethod()) + " "
        + esd.getListenerMethods().length + " " + esd.isUnicast() + " " + esd.isInDefaultEventSet());
    try {
      new EventSetDescriptor(Events.class, "foo", FooListener.class, new String[] {"nope"}, "addFooListener",
          "removeFooListener");
    } catch (IntrospectionException e) {
      p("esd-bad " + e.getMessage());
    }
    MethodDescriptor md = new MethodDescriptor(Simple.class.getMethod("getA"));
    p("md " + md.getName() + " " + sig(md.getMethod()) + " " + md.getParameterDescriptors());
    p("tostring-pd " + new PropertyDescriptor("a", Simple.class));
    p("tostring-pd-bound " + pd4);
    p("tostring-ipd " + ipd);
    p("tostring-esd " + esd);
    p("tostring-md " + md);
    p("tostring-bd " + bd2);
    p("tostring-pard " + new java.beans.ParameterDescriptor());
    java.beans.ParameterDescriptor pard = new java.beans.ParameterDescriptor();
    pard.setName("par");
    pard.setExpert(true);
    pard.setHidden(true);
    pard.setPreferred(true);
    p("tostring-pard-set " + pard);
    MethodDescriptor md2 = new MethodDescriptor(Simple.class.getMethod("setA", int.class), new java.beans.ParameterDescriptor[] {pard});
    p("tostring-md2 " + md2 + " " + md2.getParameterDescriptors().length + " " + md2.getMethod().getName());
    PropertyDescriptor pdr = new PropertyDescriptor("ro", Simple.class, "getRO", null);
    pdr.setBound(true);
    pdr.setPreferred(true);
    p("tostring-pdr " + pdr);
    p("pd-type-write-only " + new PropertyDescriptor("wo", Simple.class, null, "setWO").getPropertyType());
    PropertyDescriptor pdm = new PropertyDescriptor("a", null, null);
    p("pd-null-methods " + pdm.getPropertyType() + " " + pdm.getReadMethod());
    p("simple-bean-info " + new SimpleBeanInfo().getBeanDescriptor() + " " + new SimpleBeanInfo().getDefaultPropertyIndex()
        + " " + new SimpleBeanInfo().getAdditionalBeanInfo() + " " + new SimpleBeanInfo().getIcon(1) + " "
        + new SimpleBeanInfo().getPropertyDescriptors());
  }
}
