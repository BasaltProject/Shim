/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.SocketPermission;
import java.net.URL;
import java.security.*;
import java.util.*;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;

public class SecurityLegacyModern {
static class A1 {
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + t.getMessage(); }
  public static void run() throws Exception {
    System.out.println(AccessController.doPrivileged((PrivilegedAction<String>) () -> "act"));
    System.out.println(AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> "exc"));
    try { AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> { throw new java.io.IOException("io"); }); } catch (Throwable t) { System.out.println("PAE " + fail(t) + " cause=" + t.getCause() + " ex=" + (t instanceof PrivilegedActionException ? ((PrivilegedActionException) t).getException() : null) + " str=" + t); }
    try { AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> { throw new IllegalStateException("rt"); }); } catch (Throwable t) { System.out.println("RT " + fail(t)); }
    AccessControlContext ctx = AccessController.getContext(); System.out.println("ctx " + (ctx != null));
    System.out.println(AccessController.doPrivileged((PrivilegedAction<String>) () -> "ctx-act", ctx));
    System.out.println(AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> "ctx-exc", ctx));
    System.out.println(AccessController.doPrivileged((PrivilegedAction<String>) () -> "perm-act", ctx, new RuntimePermission("x")));
    System.out.println(AccessController.doPrivilegedWithCombiner((PrivilegedAction<String>) () -> "comb"));
    try { AccessController.checkPermission(new RuntimePermission("x")); System.out.println("check ok"); } catch (Throwable t) { System.out.println("check " + fail(t)); }
    try { ctx.checkPermission(new RuntimePermission("x")); System.out.println("ctx check ok"); } catch (Throwable t) { System.out.println("ctx check " + fail(t)); }
    try { System.out.println("policy " + Policy.getPolicy().getClass().getName()); } catch (Throwable t) { System.out.println("policy " + fail(t)); }
    try { Policy.setPolicy(null); System.out.println("setPolicy ok"); } catch (Throwable t) { System.out.println("setPolicy " + fail(t)); }
    try { System.out.println("sm " + System.getSecurityManager()); } catch (Throwable t) { System.out.println("sm " + fail(t)); }
    try { System.setSecurityManager(null); } catch (Throwable t) { System.out.println("setSM " + fail(t)); }
    ProtectionDomain pd = A1.class.getProtectionDomain(); System.out.println("pd " + (pd != null) + " " + (pd == null ? null : pd.getCodeSource() == null));
    System.out.println("pd.implies " + pd.implies(new RuntimePermission("x")));
    PropertyPermissionTest.run();
    GuardedObject go = new GuardedObject("obj", new Guard() { public void checkGuard(Object o) { System.out.println("guard " + o); } }); System.out.println("guarded " + go.getObject());
    try { System.out.println(new URIParameter(new java.net.URI("file:/x")).getURI()); } catch (Throwable t) { System.out.println("uriparam " + fail(t)); }
    try { Signature s = Signature.getInstance("SHA256withRSA"); KeyPairGenerator g = KeyPairGenerator.getInstance("RSA"); g.initialize(1024); KeyPair kp = g.generateKeyPair(); SignedObject so = new SignedObject("payload", kp.getPrivate(), s); System.out.println("signed " + so.getObject() + " " + so.getAlgorithm() + " " + so.verify(kp.getPublic(), Signature.getInstance("SHA256withRSA"))); } catch (Throwable t) { System.out.println("signed " + fail(t)); }
    try { KeyRep kr = new KeyRep(KeyRep.Type.SECRET, "AES", "RAW", new byte[16]); System.out.println("keyrep " + kr.getClass().getSimpleName()); } catch (Throwable t) { System.out.println("keyrep " + fail(t)); }
    UnresolvedPermission up = new UnresolvedPermission("java.io.FilePermission", "/x", "read", null); System.out.println("unresolved " + up + " " + up.getName() + " " + up.getActions() + " " + up.implies(new AllPermission()));
    System.out.println("sec classloader " + SecureClassLoader.class.getSuperclass().getName());
    PKCS12Attribute pa = new PKCS12Attribute("1.2.3", "val"); System.out.println("pkcs12attr " + pa.getName() + " " + pa.getValue() + " " + pa);
    System.out.println(new AllPermission().implies(new RuntimePermission("y")) + " " + new AllPermission());
  }
}
static class PropertyPermissionTest { static void run() { try { java.util.PropertyPermission p = new java.util.PropertyPermission("user.*", "read"); System.out.println("pp " + p + " " + p.getActions() + " " + p.implies(new java.util.PropertyPermission("user.dir", "read")) + " " + p.implies(new java.util.PropertyPermission("user.dir", "write")) + " " + p.newPermissionCollection().getClass().getSimpleName().length()); java.util.PropertyPermission q = new java.util.PropertyPermission("*", "read,write"); System.out.println(q.getActions() + " " + q.equals(new java.util.PropertyPermission("*", "write,read")) + " " + q.hashCode()); try { new java.util.PropertyPermission("x", "exec"); } catch (Throwable t) { System.out.println("bad " + fail(t)); } } catch (Throwable t) { System.out.println("pp " + fail(t)); } } static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + t.getMessage(); } }

static class A2 { public static void run() throws Exception {
  CodeSource cs = new CodeSource(new URL("file:/tmp/x.jar"), (java.security.cert.Certificate[]) null);
  System.out.println(cs + " | " + cs.getLocation() + " | " + cs.getCertificates() + " | " + cs.getCodeSigners() + " | " + cs.hashCode());
  CodeSource cs2 = new CodeSource(new URL("file:/tmp/-"), (java.security.cert.Certificate[]) null);
  System.out.println(cs2.implies(cs) + " " + cs.implies(cs2) + " " + cs.equals(new CodeSource(new URL("file:/tmp/x.jar"), (java.security.cert.Certificate[]) null)) + " " + new CodeSource(null, (java.security.cert.Certificate[]) null) + " " + new CodeSource(null, (java.security.cert.Certificate[]) null).implies(cs));
  System.out.println(new CodeSource(new URL("http://example.com/a/b"), (java.security.cert.Certificate[]) null).implies(new CodeSource(new URL("http://example.com/a/b"), (java.security.cert.Certificate[]) null)));
  Permissions perms = new Permissions(); perms.add(new RuntimePermission("a.*")); perms.add(new java.util.PropertyPermission("p", "read")); perms.add(new AllPermission());
  ProtectionDomain pd = new ProtectionDomain(cs, perms);
  System.out.println(pd.getCodeSource() == cs); System.out.println(pd.getPermissions() == perms); System.out.println(pd.getClassLoader()); System.out.println(Arrays.toString(pd.getPrincipals())); System.out.println(pd.implies(new RuntimePermission("zz")));
  Permissions p2 = new Permissions(); p2.add(new RuntimePermission("a.*"));
  ProtectionDomain pd2 = new ProtectionDomain(cs, p2); System.out.println(pd2.implies(new RuntimePermission("a.b")) + " " + pd2.implies(new RuntimePermission("b.b")));
  
  ProtectionDomain pd3 = new ProtectionDomain(cs, null); System.out.println(pd3.getPermissions() + " " + pd3.implies(new RuntimePermission("x")) + " " + pd3.toString().replace("\n", "\\n"));
  PermissionCollection pc = new RuntimePermission("x").newPermissionCollection(); pc.add(new RuntimePermission("x.*")); System.out.println(pc.toString().replaceAll("@[0-9a-f]+", "@H").replace("\n", "\\n") + pc.implies(new RuntimePermission("x.y")) + pc.implies(new RuntimePermission("xy")) + pc.isReadOnly());
  pc.setReadOnly(); try { pc.add(new RuntimePermission("q")); } catch (Throwable t) { System.out.println(t); }
  try { pc.add(new SecurityPermission("q")); } catch (Throwable t) { System.out.println(t); }
  perms.setReadOnly(); try { perms.add(new RuntimePermission("q")); } catch (Throwable t) { System.out.println(t); }
  System.out.println(Collections.list(perms.elements()).size() + " " + new RuntimePermission("a.b").implies(new RuntimePermission("a.b")) + new RuntimePermission("a.*").implies(new RuntimePermission("a.b.c")) + new RuntimePermission("a.b").implies(new RuntimePermission("a.*")) + new RuntimePermission("*").implies(new RuntimePermission("x")) + new RuntimePermission("exitVM").implies(new RuntimePermission("exitVM.1")) + new RuntimePermission("a.*").implies(new SecurityPermission("a.b")));
  try { new RuntimePermission(null); } catch (Throwable t) { System.out.println(t); } try { new RuntimePermission(""); } catch (Throwable t) { System.out.println(t); }
  System.out.println(new RuntimePermission("a", "act") + " " + new RuntimePermission("a").getActions().isEmpty() + new RuntimePermission("a").equals(new RuntimePermission("a")) + new RuntimePermission("a").equals(new SecurityPermission("a")) + " " + new RuntimePermission("abc").hashCode());
  System.out.println(new java.io.FilePermission("/tmp/-", "read,write").implies(new java.io.FilePermission("/tmp/a/b", "read")) + " " + new java.io.FilePermission("/tmp/*", "read").implies(new java.io.FilePermission("/tmp/a/b", "read")) + " " + new java.io.FilePermission("/tmp/*", "read").implies(new java.io.FilePermission("/tmp/a", "read")) + " " + new java.io.FilePermission("<<ALL FILES>>", "read").implies(new java.io.FilePermission("/x", "read")) + " " + new java.io.FilePermission("/x", "read,write,execute,delete,readlink").getActions() + " " + new java.io.FilePermission("/x", "delete,read").getActions() + " " + new java.io.FilePermission("/x", "read").equals(new java.io.FilePermission("/x", "READ")) + " " + new java.io.FilePermission("/tmp/./a/../b", "read").getName() + " " + new java.io.FilePermission("/tmp/-", "read").getName() + " " + new java.io.FilePermission("/tmp/-", "read"));
  for (String bad : new String[]{"exec2", "", "read,", "read write", ",read", "READ,Write", "read,read"}) { try { System.out.println("fp " + bad + " " + new java.io.FilePermission("/x", bad).getActions()); } catch (Throwable t) { System.out.println("fp " + bad + " " + t); } }
  try { new java.io.FilePermission(null, "read"); } catch (Throwable t) { System.out.println(t); }
  try { new java.io.FilePermission("/x", null); } catch (Throwable t) { System.out.println(t); }
  try { new java.io.FilePermission("", "read"); System.out.println("empty ok"); } catch (Throwable t) { System.out.println(t); }
  for (String bad : new String[]{"exec", "", "read,", "Read , WRITE", "read,read", "readwrite", null}) { try { System.out.println("pp " + bad + " " + new java.util.PropertyPermission("x", bad).getActions()); } catch (Throwable t) { System.out.println("pp " + bad + " " + t); } }
  PermissionCollection ppc = new java.util.PropertyPermission("x", "read").newPermissionCollection(); ppc.add(new java.util.PropertyPermission("a.*", "read")); ppc.add(new java.util.PropertyPermission("a.b", "write")); System.out.println(ppc.implies(new java.util.PropertyPermission("a.b", "read,write")) + " " + ppc.implies(new java.util.PropertyPermission("a.c", "write")) + " " + Collections.list(ppc.elements()).size());
  System.out.println(new SocketPermission("localhost:80", "connect").implies(new SocketPermission("localhost:80", "connect")) + " " + new AllPermission().newPermissionCollection().getClass().getName());
  System.out.println(AccessController.getContext().equals(AccessController.getContext()) + " " + new AccessControlContext(new ProtectionDomain[0]).equals(new AccessControlContext(new ProtectionDomain[0])) + " " + new AccessControlContext(new ProtectionDomain[]{pd}).equals(new AccessControlContext(new ProtectionDomain[]{pd})));
  try { new AccessControlContext(null); } catch (Throwable t) { System.out.println("acc null " + t.getClass().getName()); }
  AccessControlContext acc = new AccessControlContext(new ProtectionDomain[]{pd}); System.out.println(acc.getDomainCombiner());
  try { acc.checkPermission(null); } catch (Throwable t) { System.out.println("check null " + t); }
  System.out.println(new AccessControlException("m").getPermission() + " " + new AccessControlException("m", new RuntimePermission("r")).getPermission());
} }

static class J1 {
  static String fail(Throwable t) { String m = String.valueOf(t.getMessage()); int nl = m.indexOf('\n'); if (nl >= 0) m = m.substring(0, nl); if (t instanceof NullPointerException) m = "<npe>"; return "!" + t.getClass().getName() + ":" + m; }
  static class P implements Principal { final String n; P(String n) { this.n = n; } public String getName() { return n; } public boolean equals(Object o) { return o instanceof P && ((P) o).n.equals(n); } public int hashCode() { return n.hashCode(); } public String toString() { return "P(" + n + ")"; } }
  static class Q extends P { Q(String n) { super(n); } public String toString() { return "Q(" + n + ")"; } }
  public static class Mod implements LoginModule {
    static List<String> log = new ArrayList<String>(); Subject s; Map<String, ?> opts; Map<String, ?> shared; CallbackHandler h;
    public void initialize(Subject subject, CallbackHandler cb, Map<String, ?> sharedState, Map<String, ?> options) { s = subject; opts = options; shared = sharedState; h = cb; log.add("init " + options.get("name") + " cb=" + (cb != null)); }
    public boolean login() throws LoginException { String r = String.valueOf(opts.get("login")); log.add("login " + opts.get("name") + " " + r); if (r.equals("fail")) throw new FailedLoginException("failed " + opts.get("name")); if (r.equals("ignore")) return false; if (r.equals("boom")) throw new IllegalStateException("boom"); if (r.equals("sec")) throw new SecurityException("secret"); return true; }
    public boolean commit() throws LoginException { log.add("commit " + opts.get("name")); if ("fail".equals(opts.get("commit"))) throw new LoginException("commit failed"); s.getPrincipals().add(new P(String.valueOf(opts.get("name")))); return true; }
    public boolean abort() throws LoginException { log.add("abort " + opts.get("name")); return true; }
    public boolean logout() throws LoginException { log.add("logout " + opts.get("name")); s.getPrincipals().removeIf(p -> p.getName().equals(String.valueOf(opts.get("name")))); return true; }
  }
  public static class NoDefault implements LoginModule { public NoDefault(int x) {} public void initialize(Subject s, CallbackHandler c, Map<String, ?> a, Map<String, ?> b) {} public boolean login() { return true; } public boolean commit() { return true; } public boolean abort() { return true; } public boolean logout() { return true; } }
  static Configuration cfg(final Map<String, AppConfigurationEntry[]> m) { return new Configuration() { public AppConfigurationEntry[] getAppConfigurationEntry(String name) { return m.get(name); } }; }
  static AppConfigurationEntry e(AppConfigurationEntry.LoginModuleControlFlag f, String name, String login) { Map<String, Object> o = new HashMap<String, Object>(); o.put("name", name); o.put("login", login); return new AppConfigurationEntry(Mod.class.getName(), f, o); }
  static AppConfigurationEntry ec(AppConfigurationEntry.LoginModuleControlFlag f, String name, String login, String commit) { Map<String, Object> o = new HashMap<String, Object>(); o.put("name", name); o.put("login", login); o.put("commit", commit); return new AppConfigurationEntry(Mod.class.getName(), f, o); }
  static void run(String label, Configuration c, String app) {
    Mod.log.clear(); StringBuilder sb = new StringBuilder(label + ": ");
    try { LoginContext lc = new LoginContext(app, null, null, c); sb.append("created subjectBefore=" + lc.getSubject() + " | "); try { lc.login(); sb.append("login ok subject=" + lc.getSubject().getPrincipals()); } catch (LoginException x) { sb.append("login " + fail(x) + " subject=" + lc.getSubject()); } try { lc.logout(); sb.append(" | logout ok " + lc.getSubject().getPrincipals()); } catch (Throwable x) { sb.append(" | logout " + fail(x)); } } catch (Throwable x) { sb.append("ctor " + fail(x)); }
    sb.append(" || ").append(Mod.log); System.out.println(sb);
  }
  public static void run() throws Exception {
    Subject s = new Subject(); s.getPrincipals().add(new P("a")); s.getPrincipals().add(new Q("b")); s.getPublicCredentials().add("pub"); s.getPrivateCredentials().add("priv");
    System.out.println(new TreeSet<String>(toStr(s.getPrincipals())) + " " + toStr(s.getPrincipals(Q.class)) + " " + toStr(s.getPrincipals(P.class)).size() + " " + s.getPublicCredentials(String.class) + " " + s.getPrivateCredentials(Integer.class) + " " + s.isReadOnly());
    System.out.println(s.toString().replace("\n", "\\n").length() > 10);
    try { s.getPrincipals().add(null); } catch (Throwable t) { System.out.println("null principal " + fail(t)); }
    try { ((Set) s.getPrincipals()).add("str"); } catch (Throwable t) { System.out.println("str principal " + fail(t)); }
    try { s.getPrincipals(null); } catch (Throwable t) { System.out.println("null class " + fail(t)); }
    s.setReadOnly(); System.out.println(s.isReadOnly());
    try { s.getPrincipals().add(new P("c")); } catch (Throwable t) { System.out.println("ro add " + fail(t)); }
    try { s.getPrincipals().remove(new P("a")); } catch (Throwable t) { System.out.println("ro remove " + fail(t)); }
    try { s.getPrincipals().clear(); } catch (Throwable t) { System.out.println("ro clear " + fail(t)); }
    try { Iterator<Principal> it = s.getPrincipals().iterator(); it.next(); it.remove(); } catch (Throwable t) { System.out.println("ro iter remove " + fail(t)); }
    Subject s2 = new Subject(true, new HashSet<Principal>(Arrays.asList(new P("a"), new Q("b"))), new HashSet<Object>(Arrays.asList("pub")), new HashSet<Object>(Arrays.asList("priv"))); System.out.println(s.equals(s2) + " " + (s.hashCode() == s2.hashCode()) + " " + s2.isReadOnly() + " " + s.equals(null) + " " + s.equals("x"));
    try { new Subject(false, null, new HashSet<Object>(), new HashSet<Object>()); } catch (Throwable t) { System.out.println("null set " + fail(t)); }
    try { new Subject(false, (Set) new HashSet<Object>(Arrays.asList("str")), new HashSet<Object>(), new HashSet<Object>()); } catch (Throwable t) { System.out.println("bad principal set " + fail(t)); }
    System.out.println(Subject.current());
    final Subject cs = new Subject();
    System.out.println(Subject.callAs(cs, () -> Subject.current() == cs) + " " + Subject.current());
    try { Subject.callAs(cs, () -> { throw new java.io.IOException("io"); }); } catch (Throwable t) { System.out.println("callAs " + fail(t) + " cause=" + t.getCause()); }
    System.out.println(Subject.doAs(cs, (PrivilegedAction<Boolean>) () -> Subject.current() == cs));
    try { Subject.doAs(cs, (PrivilegedExceptionAction<String>) () -> { throw new java.io.IOException("io2"); }); } catch (Throwable t) { System.out.println("doAs " + fail(t)); }
    // callbacks
    String[] errs = new String[0];
    try { new NameCallback(""); } catch (Throwable t) { System.out.println("name " + fail(t)); }
    NameCallback nc = new NameCallback("user: ", "def"); nc.setName("n"); System.out.println(nc.getPrompt() + nc.getDefaultName() + nc.getName());
    try { new TextOutputCallback(5, "m"); } catch (Throwable t) { System.out.println("tocb " + fail(t)); }
    System.out.println(new TextOutputCallback(TextOutputCallback.WARNING, "hi").getMessageType());
    try { new ChoiceCallback("p", new String[]{"a", "b"}, 2, false); } catch (Throwable t) { System.out.println("choice " + fail(t)); }
    ChoiceCallback cc = new ChoiceCallback("p", new String[]{"a", "b"}, 1, false); cc.setSelectedIndex(0); System.out.println(Arrays.toString(cc.getSelectedIndexes()) + cc.getDefaultChoice() + cc.allowMultipleSelections());
    try { cc.setSelectedIndexes(new int[]{0, 1}); } catch (Throwable t) { System.out.println("multi " + fail(t)); }
    ConfirmationCallback cf = new ConfirmationCallback(ConfirmationCallback.WARNING, ConfirmationCallback.YES_NO_OPTION, ConfirmationCallback.NO); System.out.println(cf.getMessageType() + " " + cf.getOptionType() + " " + cf.getDefaultOption() + " " + cf.getOptions());
    try { new ConfirmationCallback(0, ConfirmationCallback.YES_NO_OPTION, ConfirmationCallback.CANCEL); } catch (Throwable t) { System.out.println("confirm " + fail(t)); }
    try { cf.setSelectedIndex(2); } catch (Throwable t) { System.out.println("confirm sel " + fail(t)); }
    TextInputCallback ti = new TextInputCallback("p", "d"); ti.setText("t"); System.out.println(ti.getPrompt() + ti.getDefaultText() + ti.getText());
    LanguageCallback lc0 = new LanguageCallback(); lc0.setLocale(Locale.FRANCE); System.out.println(lc0.getLocale());
    // entries
    try { new AppConfigurationEntry("", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>()); } catch (Throwable t) { System.out.println("entry " + fail(t)); }
    try { new AppConfigurationEntry("x", null, new HashMap<String, Object>()); } catch (Throwable t) { System.out.println("entry2 " + fail(t)); }
    System.out.println(AppConfigurationEntry.LoginModuleControlFlag.REQUISITE + " " + AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT + " " + AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL + " " + AppConfigurationEntry.LoginModuleControlFlag.REQUIRED);
    try { Configuration.getConfiguration(); } catch (Throwable t) { System.out.println("getConfiguration " + fail(t)); }
    try { new LoginContext("x"); } catch (Throwable t) { System.out.println("LoginContext default " + fail(t)); }
    System.out.println(new AuthPermission("createLoginContext").getName() + " " + new AuthPermission("modifyPrincipals").implies(new AuthPermission("modifyPrincipals")) + " " + new AuthPermission("createLoginContext.*").implies(new AuthPermission("createLoginContext.foo")));
    // login contexts
    AppConfigurationEntry.LoginModuleControlFlag R = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, Q2 = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, S = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, O = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
    Map<String, AppConfigurationEntry[]> m = new HashMap<String, AppConfigurationEntry[]>();
    m.put("ok", new AppConfigurationEntry[]{e(R, "m1", "ok"), e(R, "m2", "ok")});
    m.put("reqfail", new AppConfigurationEntry[]{e(R, "m1", "ok"), e(R, "m2", "fail"), e(R, "m3", "ok")});
    m.put("requisite", new AppConfigurationEntry[]{e(R, "m1", "ok"), e(Q2, "m2", "fail"), e(R, "m3", "ok")});
    m.put("sufficient", new AppConfigurationEntry[]{e(S, "m1", "ok"), e(R, "m2", "ok")});
    m.put("sufficient-after-fail", new AppConfigurationEntry[]{e(R, "m1", "fail"), e(S, "m2", "ok"), e(R, "m3", "ok")});
    m.put("optional-fail", new AppConfigurationEntry[]{e(O, "m1", "fail"), e(R, "m2", "ok")});
    m.put("optional-only-fail", new AppConfigurationEntry[]{e(O, "m1", "fail")});
    m.put("ignored", new AppConfigurationEntry[]{e(R, "m1", "ignore")});
    m.put("boom", new AppConfigurationEntry[]{e(R, "m1", "boom")});
    m.put("sec", new AppConfigurationEntry[]{e(R, "m1", "sec")});
    m.put("commitfail", new AppConfigurationEntry[]{ec(R, "m1", "ok", "ok"), ec(R, "m2", "ok", "fail")});
    m.put("other", new AppConfigurationEntry[]{e(R, "mo", "ok")});
    Map<String, Object> bad = new HashMap<String, Object>(); m.put("missing-class", new AppConfigurationEntry[]{new AppConfigurationEntry("no.such.Module", R, bad)});
    m.put("no-default-ctor", new AppConfigurationEntry[]{new AppConfigurationEntry(NoDefault.class.getName(), R, bad)});
    Configuration c = cfg(m);
    for (String app : new String[]{"ok", "reqfail", "requisite", "sufficient", "sufficient-after-fail", "optional-fail", "optional-only-fail", "ignored", "boom", "sec", "commitfail", "unknown-app", "missing-class", "no-default-ctor"}) run(app, c, app);
    Map<String, AppConfigurationEntry[]> m2 = new HashMap<String, AppConfigurationEntry[]>(); run("no-other", cfg(m2), "x");
    try { new LoginContext(null, null, null, c); } catch (Throwable t) { System.out.println("null name " + fail(t)); }
    try { new LoginContext("ok", (Subject) null, (CallbackHandler) null, c).logout(); } catch (Throwable t) { System.out.println("logout before login " + fail(t)); }
    try { new LoginContext("ok", new Subject(), null, c); System.out.println("subject ctor ok"); } catch (Throwable t) { System.out.println("subject ctor " + fail(t)); }
    Configuration.setConfiguration(c); try { LoginContext l = new LoginContext("ok"); l.login(); System.out.println("global config login " + l.getSubject().getPrincipals()); } catch (Throwable t) { System.out.println("global " + fail(t)); }
    try { new LoginContext("ok", (Subject) null); } catch (Throwable t) { System.out.println("null subject " + fail(t)); }
    try { new LoginContext("ok", (CallbackHandler) null); } catch (Throwable t) { System.out.println("null handler " + fail(t)); }
    System.out.println(new LoginException("m").getMessage() + new CredentialExpiredException("c").getMessage() + (new AccountLockedException() instanceof AccountException) + (new CredentialNotFoundException() instanceof CredentialException) + new AccountNotFoundException("n").getMessage());
  }
  static List<String> toStr(Set<? extends Principal> s) { List<String> l = new ArrayList<String>(); for (Principal p : s) l.add(p.toString()); return l; }
}

static class J2 {
  interface C { void run() throws Exception; }
  static void t(String l, C c) { try { c.run(); System.out.println(l + ": ok"); } catch (Throwable e) { System.out.println(l + ": " + e.getClass().getSimpleName() + ": " + e.getMessage()); } }
  public static void run() {
    t("name null", () -> new NameCallback(null)); t("name empty", () -> new NameCallback("")); t("name2 defnull", () -> new NameCallback("p", null)); t("name2 defempty", () -> new NameCallback("p", "")); t("name2 pnull", () -> new NameCallback(null, "d")); 
    t("pw null", () -> new PasswordCallback(null, true)); t("pw empty", () -> new PasswordCallback("", true));
    t("ti null", () -> new TextInputCallback(null)); t("ti empty", () -> new TextInputCallback("")); t("ti2 dnull", () -> new TextInputCallback("p", null)); t("ti2 dempty", () -> new TextInputCallback("p", "")); t("ti2 pnull", () -> new TextInputCallback(null, "d"));
    t("to type", () -> new TextOutputCallback(3, "m")); t("to neg", () -> new TextOutputCallback(-1, "m")); t("to null", () -> new TextOutputCallback(0, null)); t("to empty", () -> new TextOutputCallback(0, ""));
    t("ch pnull", () -> new ChoiceCallback(null, new String[]{"a"}, 0, false)); t("ch pempty", () -> new ChoiceCallback("", new String[]{"a"}, 0, false)); t("ch cnull", () -> new ChoiceCallback("p", null, 0, false)); t("ch cempty", () -> new ChoiceCallback("p", new String[0], 0, false)); t("ch def neg", () -> new ChoiceCallback("p", new String[]{"a"}, -1, false)); t("ch def big", () -> new ChoiceCallback("p", new String[]{"a"}, 1, false)); t("ch c elem null", () -> new ChoiceCallback("p", new String[]{null}, 0, false)); t("ch c elem empty", () -> new ChoiceCallback("p", new String[]{""}, 0, false));
    ChoiceCallback cc = new ChoiceCallback("p", new String[]{"a", "b"}, 0, true); t("ch multi null", () -> cc.setSelectedIndexes(null)); t("ch sel", () -> { cc.setSelectedIndexes(new int[]{0, 1}); System.out.println(Arrays.toString(cc.getSelectedIndexes())); });
    ChoiceCallback c1 = new ChoiceCallback("p", new String[]{"a", "b"}, 0, false); t("ch single multi", () -> c1.setSelectedIndexes(new int[]{0}));
    t("cf msg", () -> new ConfirmationCallback(3, 0, 0)); t("cf msg neg", () -> new ConfirmationCallback(-1, 0, 0)); t("cf opt", () -> new ConfirmationCallback(0, 3, 0)); t("cf yesno def", () -> new ConfirmationCallback(0, ConfirmationCallback.YES_NO_OPTION, ConfirmationCallback.CANCEL)); t("cf yesnocancel def", () -> new ConfirmationCallback(0, ConfirmationCallback.YES_NO_CANCEL_OPTION, ConfirmationCallback.OK)); t("cf okcancel def", () -> new ConfirmationCallback(0, ConfirmationCallback.OK_CANCEL_OPTION, ConfirmationCallback.YES));
    t("cf opts null", () -> new ConfirmationCallback(0, (String[]) null, 0)); t("cf opts empty", () -> new ConfirmationCallback(0, new String[0], 0)); t("cf opts def", () -> new ConfirmationCallback(0, new String[]{"a"}, 1)); t("cf opts elem", () -> new ConfirmationCallback(0, new String[]{""}, 0)); t("cf opts msg", () -> new ConfirmationCallback(5, new String[]{"a"}, 0));
    t("cf prompt null", () -> new ConfirmationCallback(null, 0, 0, 0)); t("cf prompt empty", () -> new ConfirmationCallback("", 0, 0, 0)); t("cf prompt opts null", () -> new ConfirmationCallback(null, 0, new String[]{"a"}, 0));
    ConfirmationCallback cf = new ConfirmationCallback(0, ConfirmationCallback.YES_NO_OPTION, 0); t("cf sel bad", () -> cf.setSelectedIndex(2)); t("cf sel ok", () -> cf.setSelectedIndex(1));
    ConfirmationCallback cf2 = new ConfirmationCallback(0, new String[]{"x", "y"}, 0); t("cf2 sel bad", () -> cf2.setSelectedIndex(2)); t("cf2 sel neg", () -> cf2.setSelectedIndex(-1));
    ConfirmationCallback cf3 = new ConfirmationCallback(0, ConfirmationCallback.YES_NO_CANCEL_OPTION, 0); t("cf3 sel bad", () -> cf3.setSelectedIndex(3)); ConfirmationCallback cf4 = new ConfirmationCallback(0, ConfirmationCallback.OK_CANCEL_OPTION, ConfirmationCallback.OK); t("cf4 sel bad", () -> cf4.setSelectedIndex(0));
    t("pw", () -> { PasswordCallback p = new PasswordCallback("pw: ", false); p.setPassword("abc".toCharArray()); System.out.println(p.getPrompt() + p.isEchoOn() + new String(p.getPassword())); p.clearPassword(); System.out.println(p.getPassword() == null ? "null" : new String(p.getPassword()).replace('\0', '0')); });
    
    t("ucb", () -> { UnsupportedCallbackException u = new UnsupportedCallbackException(new NameCallback("p"), "msg"); System.out.println(u.getMessage() + " " + u.getCallback().getClass().getSimpleName()); });
  } }

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    A1.run();
    System.out.println("##### A2");
    A2.run();
    System.out.println("##### J1");
    J1.run();
    System.out.println("##### J2");
    J2.run();
  }
}
