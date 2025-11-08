/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.math.*;
import java.net.*;
import java.net.spi.*;
import java.security.*;
import java.security.cert.*;
import java.text.*;
import java.text.spi.*;
import java.util.*;
import java.util.spi.*;
import javax.security.auth.x500.X500PrivateCredential;

public class LegacySpiModern {

static class A {
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static void run() throws Exception {

    LocaleServiceProvider lsp = new LocaleServiceProvider() { public Locale[] getAvailableLocales() { return new Locale[] {Locale.ENGLISH, Locale.FRANCE}; } };
    t("supported", () -> lsp.isSupportedLocale(Locale.ENGLISH) + " " + lsp.isSupportedLocale(Locale.FRANCE) + " " + lsp.isSupportedLocale(Locale.GERMAN) + " " + lsp.isSupportedLocale(Locale.forLanguageTag("fr-FR-u-nu-arab")));
    CurrencyNameProvider cnp = new CurrencyNameProvider() { public Locale[] getAvailableLocales() { return new Locale[0]; } public String getSymbol(String c, Locale l) { return "$" + c; } };
    t("currency", () -> cnp.getSymbol("USD", Locale.US) + " " + cnp.getDisplayName("USD", Locale.US));
    LocaleNameProvider lnp = new LocaleNameProvider() { public Locale[] getAvailableLocales() { return new Locale[0]; } public String getDisplayLanguage(String c, Locale l) { return "L" + c; } public String getDisplayCountry(String c, Locale l) { return "C" + c; } public String getDisplayVariant(String c, Locale l) { return "V" + c; } };
    t("localeName", () -> lnp.getDisplayLanguage("en", Locale.US) + lnp.getDisplayCountry("US", Locale.US) + lnp.getDisplayVariant("x", Locale.US) + " " + lnp.getDisplayScript("Latn", Locale.US) + " " + lnp.getDisplayUnicodeExtensionKey("ca", Locale.US) + " " + lnp.getDisplayUnicodeExtensionType("gregory", "ca", Locale.US));
    TimeZoneNameProvider tz = new TimeZoneNameProvider() { public Locale[] getAvailableLocales() { return new Locale[0]; } public String getDisplayName(String id, boolean dst, int style, Locale l) { return id + dst + style; } };
    t("tz", () -> tz.getDisplayName("UTC", true, 1, Locale.US) + " " + tz.getGenericDisplayName("UTC", 1, Locale.US));
    NumberFormatProvider nfp = new NumberFormatProvider() { public Locale[] getAvailableLocales() { return new Locale[0]; } public NumberFormat getCurrencyInstance(Locale l) { return null; } public NumberFormat getIntegerInstance(Locale l) { return null; } public NumberFormat getNumberInstance(Locale l) { return null; } public NumberFormat getPercentInstance(Locale l) { return null; } };
    t("nfp", () -> nfp.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT));
    t("styles", () -> Arrays.toString(NumberFormat.Style.values()));
    ToolProvider tp = new ToolProvider() { public String name() { return "demo"; } public int run(PrintWriter out, PrintWriter err, String... args) { out.println("out:" + String.join(",", args)); err.println("err"); return args.length; } };
    t("tool", () -> { ByteArrayOutputStream o = new ByteArrayOutputStream(), e = new ByteArrayOutputStream(); int r = tp.run(new PrintStream(o), new PrintStream(e), "a", "b"); return r + " " + o.toString().trim() + " " + e.toString().trim() + " " + tp.description() + " " + tp.name(); });
    t("toolFind", () -> ToolProvider.findFirst("nothing-like-this"));
    t("toolNull", () -> ToolProvider.findFirst(null));
    t("lookup", () -> InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4 | InetAddressResolver.LookupPolicy.IPV6_FIRST));
    t("lookupOk", () -> InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4 | InetAddressResolver.LookupPolicy.IPV6 | InetAddressResolver.LookupPolicy.IPV4_FIRST).characteristics());
    t("lookupBad", () -> InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4_FIRST));
    t("lookupBad2", () -> InetAddressResolver.LookupPolicy.of(0));
    t("lookupBad3", () -> InetAddressResolver.LookupPolicy.of(15));
    t("lookupBad4", () -> InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV6 | InetAddressResolver.LookupPolicy.IPV4_FIRST));
    t("uristore", () -> new URICertStoreParameters(URI.create("http://x/y")) + " " + new URICertStoreParameters(URI.create("http://x/y")).equals(new URICertStoreParameters(URI.create("http://x/y"))) + " " + new URICertStoreParameters(URI.create("http://x/y")).hashCode() + " " + new URICertStoreParameters(URI.create("http://x/y")).getURI());
    t("uristoreNull", () -> new URICertStoreParameters(null));
    t("ldap", () -> new LDAPCertStoreParameters() + " " + new LDAPCertStoreParameters("h").getPort() + " " + new LDAPCertStoreParameters("h", 636).getServerName());
    t("ldapNull", () -> new LDAPCertStoreParameters(null));
    t("domain", () -> { DomainLoadStoreParameter p = new DomainLoadStoreParameter(URI.create("file:/x"), Map.of("a", new KeyStore.PasswordProtection(new char[0]))); return p.getConfiguration() + " " + p.getProtectionParams().keySet() + " " + p.getProtectionParameter(); });
    t("domainNull", () -> new DomainLoadStoreParameter(null, Map.of()));
    t("x500Bad", () -> new X500PrivateCredential(null, null));
    t("x500Bad2", () -> new X500PrivateCredential(null, null, "a"));
    t("rbp", () -> { ResourceBundleProvider p = new AbstractResourceBundleProvider() { }; return p.getBundle("nope.Nothing", Locale.ENGLISH); });
    t("rbpBad", () -> new AbstractResourceBundleProvider("java.nothing") { });
    t("rbpName", () -> { AbstractResourceBundleProvider p = new AbstractResourceBundleProvider() { String n() { return toBundleName("base.Name", Locale.FRANCE); } public String toString() { return n(); } }; return p; });
    t("collatorProv", () -> new CollatorProvider() { public Locale[] getAvailableLocales() { return new Locale[] {Locale.US}; } public Collator getInstance(Locale l) { return Collator.getInstance(l); } }.isSupportedLocale(Locale.US));
    t("calData", () -> new CalendarDataProvider() { public Locale[] getAvailableLocales() { return new Locale[] {Locale.US}; } public int getFirstDayOfWeek(Locale l) { return 1; } public int getMinimalDaysInFirstWeek(Locale l) { return 1; } }.getFirstDayOfWeek(Locale.US));
  }
}

static class B {
  static class Id extends Identity { Id(String n) { super(n); } Id(String n, IdentityScope s) throws KeyManagementException { super(n, s); } }
  static class Scope extends IdentityScope {
    Map<String, Identity> m = new LinkedHashMap<>();
    Scope(String n) { super(n); }
    Scope(String n, IdentityScope s) throws KeyManagementException { super(n, s); }
    public int size() { return m.size(); }
    public Identity getIdentity(String n) { return m.get(n); }
    public Identity getIdentity(PublicKey k) { for (Identity i : m.values()) if (k.equals(i.getPublicKey())) return i; return null; }
    public void addIdentity(Identity i) throws KeyManagementException { if (m.containsKey(i.getName())) throw new KeyManagementException("dup"); m.put(i.getName(), i); }
    public void removeIdentity(Identity i) throws KeyManagementException { if (m.remove(i.getName()) == null) throw new KeyManagementException("none"); }
    public Enumeration<Identity> identities() { return Collections.enumeration(m.values()); }
  }
  static class Sg extends Signer { Sg(String n) { super(n); } }
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static void run() throws Exception {

    Id i = new Id("alice");
    t("name", () -> i.getName() + "|" + i.getScope() + "|" + i.getPublicKey() + "|" + i.getInfo() + "|" + i + "|" + i.toString(false) + "|" + i.hashCode());
    t("detailed", () -> i.toString(true));
    KeyPair kp = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    t("setKey", () -> { i.setPublicKey(kp.getPublic()); return i.getPublicKey() == kp.getPublic(); });
    t("setInfo", () -> { i.setInfo("hello"); return i.getInfo() + "|" + i.toString(true).replaceAll("[0-9a-f]{20,}", "H"); });
    Scope s = new Scope("sc");
    t("scope", () -> s + "|" + s.size() + "|" + s.getName() + "|" + s.toString().replace("\n", "\\n"));
    t("addScoped", () -> { Id j = new Id("bob", s); return j + "|" + j.getScope() + "|" + s.size() + "|" + s; });
    t("addDup", () -> new Id("bob", s));
    t("equals", () -> i.equals(new Id("alice")) + " " + i.equals(new Id("alice2")) + " " + new Id("bob").equals(s.getIdentity("bob")) + " " + i.equals("x"));
    t("getIdPrincipal", () -> s.getIdentity((Principal) new Id("bob")));
    t("sysScope", () -> IdentityScope.getSystemScope());
    t("signer", () -> { Sg g = new Sg("carol"); String before = g.toString(); g.setKeyPair(kp); return before + "|" + (g.getPrivateKey() == kp.getPrivate()) + "|" + (g.getPublicKey() == kp.getPublic()) + "|" + g; });
    t("signerBad", () -> new Sg("d").getPrivateKey());
    t("signerBad2", () -> { new Sg("d").setKeyPair(new KeyPair(kp.getPublic(), null)); return "ok"; });
    t("signerBad3", () -> { new Sg("d").setKeyPair(null); return "ok"; });
    t("nullname", () -> new Id(null));
    t("setKeyTwice", () -> { Id k = new Id("k"); k.setPublicKey(kp.getPublic()); k.setPublicKey(kp.getPublic()); return "ok"; });
    t("certs", () -> i.certificates().length);
    t("authprov", () -> AuthProvider.class.getSuperclass().getName());
  }
}

  public static void main(String[] args) throws Exception {
    A.run();
    B.run();
  }
}
