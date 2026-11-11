/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.ldap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.PagedResultsResponseControl;

public final class LdapCtx implements LdapContext {
  private static final String kManageDsaIt = "2.16.840.1.113730.3.4.2";
  private static final Set<String> kBinaryAttrs = new HashSet<String>(Arrays.asList("photo", "personalsignature",
      "audio", "jpegphoto", "javaserializeddata", "thumbnailphoto", "thumbnaillogo", "usercertificate",
      "cacertificate", "certificaterevocationlist", "authorityrevocationlist", "crosscertificatepair",
      "x500uniqueidentifier", "userpassword"));
  private static final String kObjectClassFilter = "(objectClass=*)";

  private final LdapConn conn;
  private final String dn;
  private final Hashtable<Object, Object> env;
  private Control[] reqCtls;
  private Control[] respCtls;
  private boolean closed;

  LdapCtx(LdapConn conn, String dn, Hashtable<Object, Object> env, boolean shared) {
    this.conn = conn;
    this.dn = dn;
    this.env = env;
    if (shared) {
      synchronized (conn) {
        conn.refs++;
      }
    }
    this.reqCtls = isReferralIgnore() ? new Control[] {new BasicControl(kManageDsaIt, false, null)} : null;
  }

  private boolean isReferralIgnore() {
    Object r = env.get(Context.REFERRAL);
    return r == null || "ignore".equals(r);
  }

  private LdapCtx child(String childDn) {
    LdapCtx c = new LdapCtx(conn, childDn, cloneEnv(), true);
    c.reqCtls = reqCtls == null ? null : reqCtls.clone();
    return c;
  }

  @SuppressWarnings("unchecked")
  private Hashtable<Object, Object> cloneEnv() {
    return (Hashtable<Object, Object>) env.clone();
  }

  static List<String> rdns(String dn) {
    List<String> out = new ArrayList<String>();
    StringBuilder cur = new StringBuilder();
    boolean quoted = false;
    for (int i = 0; i < dn.length(); i++) {
      char c = dn.charAt(i);
      if (c == '\\' && i + 1 < dn.length()) {
        cur.append(c).append(dn.charAt(++i));
      } else if (c == '"') {
        quoted = !quoted;
        cur.append(c);
      } else if ((c == ',' || c == ';') && !quoted) {
        out.add(cur.toString().trim());
        cur.setLength(0);
      } else {
        cur.append(c);
      }
    }
    if (cur.toString().trim().length() > 0 || !out.isEmpty()) {
      out.add(cur.toString().trim());
    }
    return out;
  }

  private static String normRdn(String r) {
    int eq = r.indexOf('=');
    return eq < 0 ? r.toLowerCase(Locale.ROOT)
        : (r.substring(0, eq).trim() + "=" + r.substring(eq + 1).trim()).toLowerCase(Locale.ROOT);
  }

  /** Returns the entry name relative to base, or null if the entry is not below it. */
  static String relative(String entry, String base) {
    List<String> e = rdns(entry);
    List<String> b = rdns(base);
    if (e.size() < b.size()) {
      return null;
    }
    int off = e.size() - b.size();
    for (int i = 0; i < b.size(); i++) {
      if (!normRdn(e.get(off + i)).equals(normRdn(b.get(i)))) {
        return null;
      }
    }
    return String.join(",", e.subList(0, off));
  }

  private static String nameText(Name n) {
    if (n instanceof CompositeName) {
      return n.size() == 0 ? "" : n.size() == 1 ? n.get(0) : n.toString();
    }
    return n.toString();
  }

  private String fullDn(String name) {
    String n = name.trim();
    if (n.isEmpty()) {
      return dn;
    }
    return dn.isEmpty() ? n : n + "," + dn;
  }

  private int intEnv(String key, int def) {
    Object v = env.get(key);
    return v == null ? def : Integer.parseInt(v.toString().trim());
  }

  private int derefMode() {
    Object v = env.get("java.naming.ldap.derefAliases");
    if (v == null) {
      return 3;
    }
    switch (v.toString()) {
      case "never":
        return 0;
      case "searching":
        return 1;
      case "finding":
        return 2;
      default:
        return 3;
    }
  }

  private boolean isBinary(String id) {
    String lc = id.toLowerCase(Locale.ROOT);
    if (lc.endsWith(";binary")) {
      return true;
    }
    int semi = lc.indexOf(';');
    String base = semi < 0 ? lc : lc.substring(0, semi);
    if (kBinaryAttrs.contains(base)) {
      return true;
    }
    Object extra = env.get("java.naming.ldap.attributes.binary");
    if (extra != null) {
      for (String a : extra.toString().trim().split("\\s+")) {
        if (a.equalsIgnoreCase(base)) {
          return true;
        }
      }
    }
    return false;
  }

  private List<Control> controls() {
    return reqCtls == null ? null : Arrays.asList(reqCtls);
  }

  private NamingException error(LdapConn.Result r) {
    String text = r.message == null || r.message.isEmpty() ? defaultText(r.code) : r.message;
    String msg = "[LDAP: error code " + r.code + " - " + text + "]";
    switch (r.code) {
      case 3:
        return new javax.naming.TimeLimitExceededException(msg);
      case 4:
        return new javax.naming.SizeLimitExceededException(msg);
      case 11:
        return new javax.naming.LimitExceededException(msg);
      case 7:
      case 8:
      case 13:
      case 48:
        return new javax.naming.AuthenticationNotSupportedException(msg);
      case 10:
        return new javax.naming.PartialResultException(msg);
      case 12:
      case 53:
        return new OperationNotSupportedException(msg);
      case 16:
        return new javax.naming.directory.NoSuchAttributeException(msg);
      case 17:
        return new javax.naming.directory.InvalidAttributeIdentifierException(msg);
      case 18:
        return new javax.naming.directory.InvalidSearchFilterException(msg);
      case 19:
      case 21:
        return new javax.naming.directory.InvalidAttributeValueException(msg);
      case 20:
        return new javax.naming.directory.AttributeInUseException(msg);
      case 32:
        return new javax.naming.NameNotFoundException(msg);
      case 34:
      case 64:
        return new InvalidNameException(msg);
      case 49:
        return new javax.naming.AuthenticationException(msg);
      case 50:
        return new javax.naming.NoPermissionException(msg);
      case 51:
      case 52:
        return new javax.naming.ServiceUnavailableException(msg);
      case 65:
      case 67:
      case 69:
        return new javax.naming.directory.SchemaViolationException(msg);
      case 66:
        return new javax.naming.ContextNotEmptyException(msg);
      case 68:
        return new javax.naming.NameAlreadyBoundException(msg);
      case 2:
        return new javax.naming.CommunicationException(msg);
      default:
        return new NamingException(msg);
    }
  }

  private static String defaultText(int code) {
    switch (code) {
      case 1: return "Operations Error";
      case 2: return "Protocol Error";
      case 3: return "Timelimit Exceeded";
      case 4: return "Sizelimit Exceeded";
      case 10: return "Referral";
      case 16: return "No Such Attribute";
      case 17: return "Undefined Attribute Type";
      case 19: return "Constraint Violation";
      case 20: return "Attribute Or Value Exists";
      case 21: return "Invalid Attribute Syntax";
      case 32: return "No Such Object";
      case 34: return "Invalid DN Syntax";
      case 49: return "Invalid Credentials";
      case 50: return "Insufficient Access Rights";
      case 53: return "Unwilling To Perform";
      case 65: return "Object Class Violation";
      case 66: return "Not Allowed On Non-leaf";
      case 68: return "Entry Already Exists";
      default: return "Other";
    }
  }

  private LdapConn.Result finish(List<LdapConn.Msg> msgs) throws NamingException {
    LdapConn.Msg last = msgs.get(msgs.size() - 1);
    try {
      respCtls = parseControls(last.controls);
      return LdapConn.Result.parse(last.content);
    } catch (IOException e) {
      NamingException ne = new javax.naming.CommunicationException("malformed LDAP response");
      ne.setRootCause(e);
      throw ne;
    }
  }

  private static Control[] parseControls(byte[] block) throws IOException {
    if (block == null) {
      return null;
    }
    List<Control> out = new ArrayList<Control>();
    Ber.Reader r = new Ber.Reader(block);
    while (r.more()) {
      Ber.Reader c = r.seq();
      String id = c.str();
      boolean crit = false;
      byte[] val = null;
      if (c.more() && c.peek() == 0x01) {
        crit = c.raw().length > 0;
      }
      if (c.more()) {
        val = c.raw();
      }
      Control ctl = null;
      if (PagedResultsResponseControl.OID.equals(id)) {
        ctl = new PagedResultsResponseControl(id, crit, val);
      } else if (id.equals(javax.naming.ldap.SortResponseControl.OID)) {
        ctl = new javax.naming.ldap.SortResponseControl(id, crit, val);
      } else {
        ctl = new BasicControl(id, crit, val);
      }
      out.add(ctl);
    }
    return out.isEmpty() ? null : out.toArray(new Control[0]);
  }

  private static <T extends NamingException> T tag(T e, String name) {
    if (e.getRemainingName() == null && !(e instanceof javax.naming.CommunicationException)) {
      try {
        e.setRemainingName(name.isEmpty() ? new CompositeName() : new CompositeName().add(name));
      } catch (InvalidNameException ignore) {
        // keep untagged
      }
    }
    return e;
  }

  @FunctionalInterface
  private interface Body<T> {
    T run() throws NamingException;
  }

  private static <T> T named(String name, Body<T> b) throws NamingException {
    try {
      return b.run();
    } catch (NamingException e) {
      throw tag(e, name);
    }
  }

  private void check(LdapConn.Result r) throws NamingException {
    if (r.code != 0) {
      throw error(r);
    }
  }

  private static final class Entry {
    String dn;
    Attributes attrs;
  }

  private static final class Found {
    final List<Entry> entries = new ArrayList<Entry>();
    NamingException pending;
  }

  private Attributes buildAttrs(Ber.Reader list) throws IOException {
    BasicAttributes out = new BasicAttributes(true);
    while (list.more()) {
      Ber.Reader a = list.seq();
      String type = a.str();
      Ber.Reader vals = a.seq();
      Attribute attr = new BasicAttribute(type);
      boolean bin = isBinary(type);
      while (vals.more()) {
        byte[] raw = vals.raw();
        attr.add(bin ? (Object) raw : new String(raw, StandardCharsets.UTF_8));
      }
      out.put(attr);
    }
    return out;
  }

  private Found search(String base, int scope, int size, int time, boolean typesOnly, String filter,
      List<String> attrs) throws NamingException {
    byte[] f = Filter.encode(filter);
    ByteArrayOutputStream al = new ByteArrayOutputStream();
    for (String a : attrs) {
      al.writeBytes(Ber.str(0x04, a));
    }
    byte[] op = Ber.tlv(0x63, Ber.str(0x04, base), Ber.integer(0x0a, scope), Ber.integer(0x0a, derefMode()),
        Ber.integer(0x02, size), Ber.integer(0x02, time), Ber.bool(typesOnly), f, Ber.tlv(0x30, al.toByteArray()));
    List<LdapConn.Msg> msgs = conn.exchange(op, controls(), 0x65);
    Found found = new Found();
    try {
      for (LdapConn.Msg m : msgs) {
        if (m.tag == 0x64) {
          Ber.Reader r = new Ber.Reader(m.content);
          Entry e = new Entry();
          e.dn = r.str();
          e.attrs = buildAttrs(r.seq());
          found.entries.add(e);
        }
      }
    } catch (IOException e) {
      NamingException ne = new javax.naming.CommunicationException("malformed LDAP response");
      ne.setRootCause(e);
      throw ne;
    }
    LdapConn.Result res = finish(msgs);
    if (res.code == 3 || res.code == 4 || res.code == 11) {
      found.pending = error(res);
    } else {
      check(res);
    }
    return found;
  }

  private static List<String> idList(String[] ids) {
    if (ids == null) {
      return new ArrayList<String>();
    }
    return ids.length == 0 ? new ArrayList<String>(Arrays.asList("1.1")) : new ArrayList<String>(Arrays.asList(ids));
  }

  private static final class Results<T> implements NamingEnumeration<T> {
    private final List<T> items;
    private NamingException pending;
    private int i;

    Results(List<T> items, NamingException pending) {
      this.items = items;
      this.pending = pending;
    }

    @Override
    public T next() throws NamingException {
      if (i < items.size()) {
        return items.get(i++);
      }
      throw new java.util.NoSuchElementException();
    }

    @Override
    public boolean hasMore() throws NamingException {
      if (i < items.size()) {
        return true;
      }
      if (pending != null) {
        NamingException p = pending;
        pending = null;
        throw p;
      }
      return false;
    }

    @Override
    public void close() {
      i = items.size();
      pending = null;
    }

    @Override
    public boolean hasMoreElements() {
      return i < items.size();
    }

    @Override
    public T nextElement() {
      return items.get(i++);
    }
  }

  @Override
  public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
    return named(name, () -> {
      Found f = search(fullDn(name), 0, 0, 0, false, kObjectClassFilter, idList(attrIds));
      return f.entries.isEmpty() ? new BasicAttributes(true) : f.entries.get(0).attrs;
    });
  }

  @Override
  public Attributes getAttributes(String name) throws NamingException {
    return getAttributes(name, null);
  }

  @Override
  public Attributes getAttributes(Name name, String[] attrIds) throws NamingException {
    return getAttributes(nameText(name), attrIds);
  }

  @Override
  public Attributes getAttributes(Name name) throws NamingException {
    return getAttributes(nameText(name), null);
  }

  private Object decodeObject(Attributes a) throws NamingException {
    Attribute data = a.get("javaSerializedData");
    if (data == null || !"true".equalsIgnoreCase(String.valueOf(env.get("com.sun.jndi.ldap.object.trustSerialData")))) {
      return null;
    }
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream((byte[]) data.get()))) {
      return in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      NamingException ne = new NamingException("Cannot deserialize object");
      ne.setRootCause(e);
      throw ne;
    }
  }

  @Override
  public Object lookup(String name) throws NamingException {
    if (name.trim().isEmpty()) {
      return child(dn);
    }
    return named(name, () -> {
      String full = fullDn(name);
      Found f = search(full, 0, 0, 0, false, kObjectClassFilter, new ArrayList<String>());
      if (!f.entries.isEmpty()) {
        Object o = decodeObject(f.entries.get(0).attrs);
        if (o != null) {
          return o;
        }
      }
      return child(full);
    });
  }

  @Override
  public Object lookup(Name name) throws NamingException {
    return lookup(nameText(name));
  }

  @Override
  public Object lookupLink(Name name) throws NamingException {
    return lookup(name);
  }

  @Override
  public Object lookupLink(String name) throws NamingException {
    return lookup(name);
  }

  @Override
  public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
    return named(name, () -> list0(name));
  }

  private NamingEnumeration<NameClassPair> list0(String name) throws NamingException {
    String base = fullDn(name);
    Found f = search(base, 1, 0, 0, false, kObjectClassFilter, new ArrayList<String>(Arrays.asList("objectClass",
        "javaClassName")));
    List<NameClassPair> out = new ArrayList<NameClassPair>();
    for (Entry e : f.entries) {
      String rel = relative(e.dn, base);
      Attribute jc = e.attrs.get("javaClassName");
      String cls = jc != null && jc.size() > 0 ? String.valueOf(jc.get()) : "javax.naming.directory.DirContext";
      NameClassPair p = new NameClassPair(rel == null ? e.dn : rel, cls, rel != null);
      p.setNameInNamespace(e.dn);
      out.add(p);
    }
    return new Results<NameClassPair>(out, f.pending);
  }

  @Override
  public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
    return list(nameText(name));
  }

  @Override
  public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
    return named(name, () -> listBindings0(name));
  }

  private NamingEnumeration<Binding> listBindings0(String name) throws NamingException {
    String base = fullDn(name);
    Found f = search(base, 1, 0, 0, false, kObjectClassFilter, new ArrayList<String>());
    List<Binding> out = new ArrayList<Binding>();
    for (Entry e : f.entries) {
      String rel = relative(e.dn, base);
      Object o = decodeObject(e.attrs);
      if (o == null) {
        o = child(e.dn);
      }
      Binding b = new Binding(rel == null ? e.dn : rel, o.getClass().getName(), o, rel != null);
      b.setNameInNamespace(e.dn);
      out.add(b);
    }
    return new Results<Binding>(out, f.pending);
  }

  @Override
  public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
    return listBindings(nameText(name));
  }

  private NamingEnumeration<SearchResult> run(String name, String filter, SearchControls cons)
      throws NamingException {
    return named(name, () -> run0(name, filter, cons));
  }

  private NamingEnumeration<SearchResult> run0(String name, String filter, SearchControls cons)
      throws NamingException {
    SearchControls sc = cons == null ? new SearchControls() : cons;
    String base = fullDn(name);
    int ms = (int) sc.getTimeLimit();
    boolean typesOnly = "true".equalsIgnoreCase(String.valueOf(env.get("java.naming.ldap.typesOnly")));
    Found f = search(base, sc.getSearchScope(), (int) sc.getCountLimit(), ms > 0 ? ms / 1000 + 1 : 0, typesOnly,
        filter, idList(sc.getReturningAttributes()));
    List<SearchResult> out = new ArrayList<SearchResult>();
    for (Entry e : f.entries) {
      String rel = relative(e.dn, base);
      SearchResult r;
      if (sc.getReturningObjFlag()) {
        Object o = decodeObject(e.attrs);
        if (o == null) {
          o = child(e.dn);
        }
        r = new SearchResult(rel == null ? e.dn : rel, o.getClass().getName(), o, e.attrs, rel != null);
      } else {
        r = new SearchResult(rel == null ? e.dn : rel, null, null, e.attrs, rel != null);
      }
      r.setNameInNamespace(e.dn);
      out.add(r);
    }
    return new Results<SearchResult>(out, f.pending);
  }

  private static String matchFilter(Attributes match) throws NamingException {
    if (match == null || match.size() == 0) {
      return kObjectClassFilter;
    }
    StringBuilder sb = new StringBuilder("(&");
    NamingEnumeration<? extends Attribute> all = match.getAll();
    while (all.hasMore()) {
      Attribute a = all.next();
      if (a.size() == 0) {
        sb.append('(').append(a.getID()).append("=*)");
      }
      for (int i = 0; i < a.size(); i++) {
        sb.append('(').append(a.getID()).append('=');
        StringBuilder v = new StringBuilder();
        Filter.format("{0}", new Object[] {a.get(i)});
        sb.append(Filter.format("{0}", new Object[] {a.get(i)})).append(v).append(')');
      }
    }
    return sb.append(')').toString();
  }

  private static SearchControls matchControls(String[] attrs) {
    return new SearchControls(SearchControls.ONELEVEL_SCOPE, 0, 0, attrs, false, false);
  }

  @Override
  public NamingEnumeration<SearchResult> search(String name, String filter, Object[] args, SearchControls cons)
      throws NamingException {
    return run(name, Filter.format(filter, args), cons);
  }

  @Override
  public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
      throws NamingException {
    return run(name, filter, cons);
  }

  @Override
  public NamingEnumeration<SearchResult> search(String name, Attributes match, String[] attrs)
      throws NamingException {
    return run(name, matchFilter(match), matchControls(attrs));
  }

  @Override
  public NamingEnumeration<SearchResult> search(String name, Attributes match) throws NamingException {
    return run(name, matchFilter(match), matchControls(null));
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, String filter, Object[] args, SearchControls cons)
      throws NamingException {
    return search(nameText(name), filter, args, cons);
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
      throws NamingException {
    return search(nameText(name), filter, cons);
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, Attributes match, String[] attrs)
      throws NamingException {
    return search(nameText(name), match, attrs);
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, Attributes match) throws NamingException {
    return search(nameText(name), match);
  }

  private static byte[] valueBytes(Object v) {
    return v instanceof byte[] ? (byte[]) v : String.valueOf(v).getBytes(StandardCharsets.UTF_8);
  }

  private static byte[] attrTlv(Attribute a) throws NamingException {
    ByteArrayOutputStream vals = new ByteArrayOutputStream();
    for (int i = 0; i < a.size(); i++) {
      vals.writeBytes(Ber.octets(0x04, valueBytes(a.get(i))));
    }
    return Ber.tlv(0x30, Ber.str(0x04, a.getID()), Ber.tlv(0x31, vals.toByteArray()));
  }

  private void add(String target, Attributes attrs) throws NamingException {
    named(target.length() > dn.length() && !dn.isEmpty() ? target.substring(0, target.length() - dn.length() - 1)
        : target, () -> {
      add0(target, attrs);
      return null;
    });
  }

  private void add0(String target, Attributes attrs) throws NamingException {
    ByteArrayOutputStream al = new ByteArrayOutputStream();
    NamingEnumeration<? extends Attribute> all = attrs.getAll();
    while (all.hasMore()) {
      al.writeBytes(attrTlv(all.next()));
    }
    check(finish(conn.exchange(Ber.tlv(0x68, Ber.str(0x04, target), Ber.tlv(0x30, al.toByteArray())), controls(),
        0x69)));
  }

  private static void addUnique(Attribute a, String v) {
    if (!a.contains(v)) {
      a.add(v);
    }
  }

  private Attributes bindAttrs(String target, Object obj, Attributes attrs) throws NamingException {
    BasicAttributes out = new BasicAttributes(true);
    if (attrs != null) {
      NamingEnumeration<? extends Attribute> all = attrs.getAll();
      while (all.hasMore()) {
        out.put((Attribute) all.next().clone());
      }
    }
    if (obj != null) {
      if (!(obj instanceof Serializable)) {
        throw new NamingException("Cannot bind object of type " + obj.getClass().getName());
      }
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ObjectOutputStream oo = new ObjectOutputStream(bytes)) {
        oo.writeObject(obj);
      } catch (IOException e) {
        NamingException ne = new NamingException("Cannot serialize object");
        ne.setRootCause(e);
        throw ne;
      }
      Attribute oc = out.get("objectClass");
      if (oc == null) {
        oc = new BasicAttribute("objectClass");
        oc.add("top");
        oc.add("javaContainer");
        out.put(oc);
      }
      addUnique(oc, "javaObject");
      addUnique(oc, "javaSerializedObject");
      out.put("javaClassName", obj.getClass().getName());
      Attribute names = new BasicAttribute("javaClassNames");
      LinkedHashSet<String> seen = new LinkedHashSet<String>();
      List<Class<?>> chain = new ArrayList<Class<?>>();
      for (Class<?> c = obj.getClass(); c != null; c = c.getSuperclass()) {
        chain.add(c);
        seen.add(c.getName());
      }
      for (Class<?> c : chain) {
        collectInterfaces(c, seen);
      }
      for (String s : seen) {
        names.add(s);
      }
      out.put(names);
      out.put("javaSerializedData", bytes.toByteArray());
    }
    List<String> rd = rdns(target);
    if (!rd.isEmpty() && rd.get(0).indexOf('=') > 0) {
      String r = rd.get(0);
      String type = r.substring(0, r.indexOf('=')).trim();
      if (out.get(type) == null) {
        out.put(type, r.substring(r.indexOf('=') + 1).trim());
      }
    }
    return out;
  }

  private static void collectInterfaces(Class<?> c, Set<String> seen) {
    for (Class<?> i : c.getInterfaces()) {
      seen.add(i.getName());
      collectInterfaces(i, seen);
    }
  }

  @Override
  public void bind(String name, Object obj, Attributes attrs) throws NamingException {
    String target = fullDn(name);
    add(target, bindAttrs(target, obj, attrs));
  }

  @Override
  public void bind(Name name, Object obj, Attributes attrs) throws NamingException {
    bind(nameText(name), obj, attrs);
  }

  @Override
  public void bind(String name, Object obj) throws NamingException {
    bind(name, obj, null);
  }

  @Override
  public void bind(Name name, Object obj) throws NamingException {
    bind(nameText(name), obj, null);
  }

  private static final String[] kObjectAttrs = {"javaClassName", "javaClassNames", "javaSerializedData",
      "javaReferenceAddress", "javaFactory", "javaCodeBase", "javaDoc"};

  @Override
  public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
    String target = fullDn(name);
    Attributes existing = null;
    try {
      existing = getAttributes(name, null);
    } catch (javax.naming.NameNotFoundException e) {
      existing = null;
    }
    Attributes use = attrs;
    if (existing != null && existing.size() > 0) {
      if (use == null) {
        BasicAttributes keep = new BasicAttributes(true);
        NamingEnumeration<? extends Attribute> all = existing.getAll();
        while (all.hasMore()) {
          Attribute a = all.next();
          boolean skip = false;
          for (String s : kObjectAttrs) {
            skip |= s.equalsIgnoreCase(a.getID());
          }
          if (!skip) {
            keep.put(a);
          }
        }
        use = keep;
      }
      unbind(name);
    }
    add(target, bindAttrs(target, obj, use));
  }

  @Override
  public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {
    rebind(nameText(name), obj, attrs);
  }

  @Override
  public void rebind(String name, Object obj) throws NamingException {
    rebind(name, obj, null);
  }

  @Override
  public void rebind(Name name, Object obj) throws NamingException {
    rebind(nameText(name), obj, null);
  }

  private void delete(String target) throws NamingException {
    named(target.length() > dn.length() && !dn.isEmpty() ? target.substring(0, target.length() - dn.length() - 1)
        : target, () -> {
      delete0(target);
      return null;
    });
  }

  private void delete0(String target) throws NamingException {
    check(finish(conn.exchange(Ber.octets(0x4a, target.getBytes(StandardCharsets.UTF_8)), controls(), 0x6b)));
  }

  @Override
  public void unbind(String name) throws NamingException {
    try {
      delete(fullDn(name));
    } catch (javax.naming.NameNotFoundException e) {
      // unbind is idempotent
    }
  }

  @Override
  public void unbind(Name name) throws NamingException {
    unbind(nameText(name));
  }

  @Override
  public void destroySubcontext(String name) throws NamingException {
    delete(fullDn(name));
  }

  @Override
  public void destroySubcontext(Name name) throws NamingException {
    destroySubcontext(nameText(name));
  }

  @Override
  public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
    String target = fullDn(name);
    add(target, bindAttrs(target, null, attrs));
    return child(target);
  }

  @Override
  public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
    return createSubcontext(nameText(name), attrs);
  }

  @Override
  public Context createSubcontext(String name) throws NamingException {
    BasicAttributes attrs = new BasicAttributes(true);
    BasicAttribute oc = new BasicAttribute("objectClass");
    oc.add("top");
    oc.add("javaContainer");
    attrs.put(oc);
    return createSubcontext(name, attrs);
  }

  @Override
  public Context createSubcontext(Name name) throws NamingException {
    return createSubcontext(nameText(name));
  }

  @Override
  public void rename(String oldName, String newName) throws NamingException {
    named(oldName, () -> {
      rename0(oldName, newName);
      return null;
    });
  }

  private void rename0(String oldName, String newName) throws NamingException {
    String from = fullDn(oldName);
    String to = fullDn(newName);
    List<String> t = rdns(to);
    if (t.isEmpty()) {
      throw new InvalidNameException("Empty target name");
    }
    String parent = String.join(",", t.subList(1, t.size()));
    List<String> f = rdns(from);
    String oldParent = String.join(",", f.subList(Math.min(1, f.size()), f.size()));
    boolean sameParent = normRdnList(parent).equals(normRdnList(oldParent));
    byte[] sup = sameParent ? new byte[0] : Ber.str(0x80, parent);
    byte[] op = Ber.tlv(0x6c, Ber.str(0x04, from), Ber.str(0x04, t.get(0)), Ber.bool(true), sup);
    check(finish(conn.exchange(op, controls(), 0x6d)));
  }

  private static String normRdnList(String dn) {
    StringBuilder sb = new StringBuilder();
    for (String r : rdns(dn)) {
      sb.append(normRdn(r)).append(',');
    }
    return sb.toString();
  }

  @Override
  public void rename(Name oldName, Name newName) throws NamingException {
    rename(nameText(oldName), nameText(newName));
  }

  @Override
  public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
    named(name, () -> {
      modify0(name, mods);
      return null;
    });
  }

  private void modify0(String name, ModificationItem[] mods) throws NamingException {
    ByteArrayOutputStream changes = new ByteArrayOutputStream();
    for (ModificationItem m : mods) {
      int op;
      switch (m.getModificationOp()) {
        case DirContext.ADD_ATTRIBUTE:
          op = 0;
          break;
        case DirContext.REMOVE_ATTRIBUTE:
          op = 1;
          break;
        case DirContext.REPLACE_ATTRIBUTE:
          op = 2;
          break;
        default:
          throw new IllegalArgumentException("Invalid modification code " + m.getModificationOp());
      }
      changes.writeBytes(Ber.tlv(0x30, Ber.integer(0x0a, op), attrTlv(m.getAttribute())));
    }
    byte[] msg = Ber.tlv(0x66, Ber.str(0x04, fullDn(name)), Ber.tlv(0x30, changes.toByteArray()));
    check(finish(conn.exchange(msg, controls(), 0x67)));
  }

  @Override
  public void modifyAttributes(String name, int modOp, Attributes attrs) throws NamingException {
    List<ModificationItem> items = new ArrayList<ModificationItem>();
    NamingEnumeration<? extends Attribute> all = attrs.getAll();
    while (all.hasMore()) {
      items.add(new ModificationItem(modOp, all.next()));
    }
    modifyAttributes(name, items.toArray(new ModificationItem[0]));
  }

  @Override
  public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {
    modifyAttributes(nameText(name), mods);
  }

  @Override
  public void modifyAttributes(Name name, int modOp, Attributes attrs) throws NamingException {
    modifyAttributes(nameText(name), modOp, attrs);
  }

  @Override
  public DirContext getSchema(String name) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public DirContext getSchema(Name name) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public DirContext getSchemaClassDefinition(String name) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public DirContext getSchemaClassDefinition(Name name) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public NameParser getNameParser(Name name) throws NamingException {
    return str -> new LdapName(str);
  }

  @Override
  public NameParser getNameParser(String name) throws NamingException {
    return str -> new LdapName(str);
  }

  @Override
  public Name composeName(Name name, Name prefix) throws NamingException {
    Name result = (Name) prefix.clone();
    result.addAll(name);
    return result;
  }

  @Override
  public String composeName(String name, String prefix) throws NamingException {
    return name.isEmpty() ? prefix : prefix.isEmpty() ? name : name + "," + prefix;
  }

  @Override
  public Object addToEnvironment(String propName, Object propVal) throws NamingException {
    return env.put(propName, propVal);
  }

  @Override
  public Object removeFromEnvironment(String propName) throws NamingException {
    return env.remove(propName);
  }

  @Override
  public Hashtable<?, ?> getEnvironment() throws NamingException {
    return cloneEnv();
  }

  @Override
  public void close() throws NamingException {
    if (closed) {
      return;
    }
    closed = true;
    boolean last;
    synchronized (conn) {
      last = --conn.refs <= 0;
    }
    if (last) {
      if (conn.bound) {
        conn.unbind();
      } else {
        conn.close();
      }
    }
  }

  @Override
  public String getNameInNamespace() throws NamingException {
    return dn;
  }

  @Override
  public ExtendedResponse extendedOperation(ExtendedRequest request) throws NamingException {
    byte[] val = request.getEncodedValue();
    byte[] op = Ber.tlv(0x77, Ber.str(0x80, request.getID()), val == null ? new byte[0] : Ber.octets(0x81, val));
    List<LdapConn.Msg> msgs = conn.exchange(op, controls(), 0x78);
    LdapConn.Result res = finish(msgs);
    check(res);
    String id = null;
    byte[] value = null;
    try {
      Ber.Reader r = new Ber.Reader(msgs.get(msgs.size() - 1).content);
      r.intv();
      r.str();
      r.str();
      while (r.more()) {
        int tag = r.peek();
        if (tag == 0xa3) {
          r.raw();
        } else if (tag == 0x8a) {
          id = r.str();
        } else if (tag == 0x8b) {
          value = r.raw();
        } else {
          r.raw();
        }
      }
    } catch (IOException e) {
      throw new javax.naming.CommunicationException("malformed extended response");
    }
    return request.createExtendedResponse(id, value, 0, value == null ? 0 : value.length);
  }

  @Override
  public LdapContext newInstance(Control[] requestControls) throws NamingException {
    LdapCtx c = child(dn);
    c.setRequestControls(requestControls);
    return c;
  }

  @Override
  public void reconnect(Control[] connCtls) throws NamingException {
    if (connCtls == null) {
      env.remove("java.naming.ldap.control.connect");
    } else {
      env.put("java.naming.ldap.control.connect", connCtls.clone());
    }
  }

  @Override
  public Control[] getConnectControls() throws NamingException {
    Object c = env.get("java.naming.ldap.control.connect");
    return c instanceof Control[] ? ((Control[]) c).clone() : null;
  }

  @Override
  public void setRequestControls(Control[] requestControls) throws NamingException {
    List<Control> list = new ArrayList<Control>();
    if (requestControls != null) {
      list.addAll(Arrays.asList(requestControls));
    }
    if (isReferralIgnore()) {
      list.add(new BasicControl(kManageDsaIt, false, null));
    }
    reqCtls = list.isEmpty() ? null : list.toArray(new Control[0]);
  }

  @Override
  public Control[] getRequestControls() throws NamingException {
    return reqCtls == null ? null : reqCtls.clone();
  }

  @Override
  public Control[] getResponseControls() throws NamingException {
    return respCtls == null ? null : respCtls.clone();
  }
}
