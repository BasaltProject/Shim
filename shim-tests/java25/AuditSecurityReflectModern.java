/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.io.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class AuditSecurityReflectModern {
    static void p(Object s) { System.out.println(s); }
    static String hex(byte[] b) { return HexFormat.of().formatHex(b); }

    public static void main(String[] args) throws Throwable {
        security();
        reflect();
        serial();
    }

    static void security() throws Exception {
        byte[] msg = "The quick brown fox".getBytes();
        for (String alg : new String[]{"MD2", "MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512", "SHA-512/224", "SHA-512/256", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512"}) {
            MessageDigest md = MessageDigest.getInstance(alg);
            md.update(msg, 0, 5); MessageDigest cl = (MessageDigest) md.clone(); md.update(msg, 5, msg.length - 5);
            p(alg + " " + hex(md.digest()) + " " + md.getDigestLength() + " " + hex(cl.digest()).length());
        }
        for (String alg : new String[]{"HmacMD5", "HmacSHA1", "HmacSHA256", "HmacSHA512", "HmacSHA3-256"}) { Mac m = Mac.getInstance(alg); m.init(new SecretKeySpec("key".getBytes(), alg)); p(alg + " " + hex(m.doFinal(msg))); }
        byte[] key = new byte[32]; for (int i = 0; i < 32; i++) key[i] = (byte) i; byte[] iv = new byte[16]; for (int i = 0; i < 16; i++) iv[i] = (byte) (i * 3);
        String[] modes = {"AES/ECB/PKCS5Padding", "AES/CBC/PKCS5Padding", "AES/CBC/NoPadding", "AES/CTR/NoPadding", "AES/CFB/NoPadding", "AES/CFB8/NoPadding", "AES/OFB/NoPadding", "AES/GCM/NoPadding", "AES/CCM/NoPadding", "AES/KW/NoPadding", "ChaCha20-Poly1305", "DESede/CBC/PKCS5Padding", "Blowfish/CBC/PKCS5Padding", "RC4"};
        for (String mode : modes) {
            try {
                String a = mode.split("/")[0];
                int kl = a.equals("DESede") ? 24 : a.equals("Blowfish") ? 16 : 32;
                SecretKey k = new SecretKeySpec(Arrays.copyOf(key, kl), a.equals("ChaCha20-Poly1305") ? "ChaCha20" : a);
                Cipher c = Cipher.getInstance(mode);
                byte[] data = mode.contains("NoPadding") && !mode.contains("GCM") && !mode.contains("CTR") && !mode.contains("CFB") && !mode.contains("OFB") ? Arrays.copyOf(msg, 16) : msg;
                if (mode.contains("KW")) data = Arrays.copyOf(msg, 16);
                if (mode.contains("ECB") || mode.contains("RC4") || mode.contains("KW")) c.init(Cipher.ENCRYPT_MODE, k);
                else if (mode.contains("GCM")) c.init(Cipher.ENCRYPT_MODE, k, new GCMParameterSpec(128, Arrays.copyOf(iv, 12)));
                else if (mode.contains("CCM")) c.init(Cipher.ENCRYPT_MODE, k, new GCMParameterSpec(128, Arrays.copyOf(iv, 12)));
                else if (mode.contains("ChaCha")) c.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(Arrays.copyOf(iv, 12)));
                else c.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(Arrays.copyOf(iv, a.equals("AES") ? 16 : 8)));
                if (mode.contains("GCM") || mode.contains("ChaCha")) c.updateAAD("aad".getBytes());
                byte[] enc = c.doFinal(data);
                Cipher d = Cipher.getInstance(mode); d.init(Cipher.DECRYPT_MODE, k, c.getParameters() == null ? null : c.getParameters());
                if (mode.contains("GCM") || mode.contains("ChaCha")) d.updateAAD("aad".getBytes());
                p(mode + " " + hex(enc) + " " + Arrays.equals(d.doFinal(enc), data) + " " + c.getBlockSize() + " " + c.getOutputSize(7) + " " + c.getAlgorithm());
            } catch (Exception e) { p(mode + " " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
        }
        try { Cipher g = Cipher.getInstance("AES/GCM/NoPadding"); g.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, new byte[12])); g.doFinal(new byte[20]); } catch (AEADBadTagException e) { p("bad tag " + e.getMessage()); }
        try { Cipher g = Cipher.getInstance("AES/CBC/PKCS5Padding"); g.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv)); g.doFinal(new byte[16]); } catch (BadPaddingException e) { p("bad pad " + e.getMessage()); }
        try { Cipher.getInstance("AES/CBC/PKCS5Padding").init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[5], "AES")); } catch (InvalidKeyException e) { p("ik " + e.getMessage()); }
        SecretKeyFactory sf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        p(hex(sf.generateSecret(new PBEKeySpec("pw".toCharArray(), "salt".getBytes(), 1000, 256)).getEncoded()));
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG"); sr.setSeed(42); byte[] rb = new byte[8]; sr.nextBytes(rb); p("sha1prng " + hex(rb) + " " + sr.nextInt(1000) + " " + sr.getAlgorithm());
        SecureRandom dr = SecureRandom.getInstance("DRBG"); p("drbg " + dr.getAlgorithm() + " " + dr.getParameters());
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA"); kpg.initialize(1024, deterministic(1));
        KeyPair rsa = kpg.generateKeyPair();
        KeyPairGenerator ec = KeyPairGenerator.getInstance("EC"); ec.initialize(new ECGenParameterSpec("secp256r1"), deterministic(2)); KeyPair eck = ec.generateKeyPair();
        KeyPairGenerator ed = KeyPairGenerator.getInstance("Ed25519"); KeyPair edk = ed.generateKeyPair();
        for (String sa : new String[]{"SHA256withRSA", "SHA512withRSA", "SHA1withRSA", "RSASSA-PSS", "SHA256withECDSA", "SHA384withECDSA", "Ed25519"}) {
            try {
                KeyPair kp = sa.contains("RSA") || sa.contains("PSS") ? rsa : sa.contains("ECDSA") ? eck : edk;
                Signature s = Signature.getInstance(sa);
                if (sa.equals("RSASSA-PSS")) s.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
                s.initSign(kp.getPrivate()); s.update(msg); byte[] sig = s.sign();
                Signature v = Signature.getInstance(sa); if (sa.equals("RSASSA-PSS")) v.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
                v.initVerify(kp.getPublic()); v.update(msg); boolean ok = v.verify(sig); v.initVerify(kp.getPublic()); v.update(msg, 0, 3);
                p(sa + " " + ok + " " + v.verify(sig) + " " + (sa.contains("RSA") && !sa.contains("PSS") ? hex(sig).length() + " " + hex(sig).substring(0, 0) : sig.length > 0));
            } catch (Exception e) { p(sa + " " + e); }
        }
        p(rsa.getPublic().getAlgorithm() + rsa.getPublic().getFormat() + rsa.getPrivate().getFormat() + ((java.security.interfaces.RSAPublicKey) rsa.getPublic()).getPublicExponent() + " " + ((java.security.interfaces.RSAPublicKey) rsa.getPublic()).getModulus().bitLength() + " " + eck.getPublic().getEncoded().length + " " + edk.getPublic().getEncoded().length + " " + hex(rsa.getPublic().getEncoded()).substring(0, 20));
        KeyFactory kf = KeyFactory.getInstance("RSA"); PublicKey pk2 = kf.generatePublic(new X509EncodedKeySpec(rsa.getPublic().getEncoded())); p(pk2.equals(rsa.getPublic()) + " " + kf.getKeySpec(rsa.getPrivate(), RSAPrivateCrtKeySpec.class).getPublicExponent());
        Cipher rc = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"); rc.init(Cipher.ENCRYPT_MODE, rsa.getPublic()); byte[] re = rc.doFinal("hi".getBytes()); rc.init(Cipher.DECRYPT_MODE, rsa.getPrivate()); p("oaep " + re.length + " " + new String(rc.doFinal(re)));
        Cipher r1 = Cipher.getInstance("RSA/ECB/PKCS1Padding"); r1.init(Cipher.ENCRYPT_MODE, rsa.getPublic()); r1.init(Cipher.DECRYPT_MODE, rsa.getPrivate()); try { r1.doFinal(new byte[128]); } catch (GeneralSecurityException e) { p("rsa " + e.getClass().getSimpleName()); }
        KeyAgreement ka = KeyAgreement.getInstance("ECDH"); KeyPair eck2 = ec.generateKeyPair(); ka.init(eck.getPrivate()); ka.doPhase(eck2.getPublic(), true); byte[] s1 = ka.generateSecret(); KeyAgreement kb = KeyAgreement.getInstance("ECDH"); kb.init(eck2.getPrivate()); kb.doPhase(eck.getPublic(), true); p("ecdh " + Arrays.equals(s1, kb.generateSecret()) + " " + s1.length);
        KeyAgreement x = KeyAgreement.getInstance("X25519"); KeyPair xa = KeyPairGenerator.getInstance("X25519").generateKeyPair(), xb = KeyPairGenerator.getInstance("X25519").generateKeyPair(); x.init(xa.getPrivate()); x.doPhase(xb.getPublic(), true); p("x25519 " + x.generateSecret().length);
        try {
        KeyStore ks = KeyStore.getInstance("PKCS12"); ks.load(null, null); 
        ByteArrayOutputStream bo = new ByteArrayOutputStream(); ks.store(bo, "storepw".toCharArray()); KeyStore k2 = KeyStore.getInstance("PKCS12"); k2.load(new ByteArrayInputStream(bo.toByteArray()), "storepw".toCharArray());
        p("ks " + Collections.list(k2.aliases()) + " " + k2.size() + " " + k2.getType() + " " + k2.isKeyEntry("sec"));
        try { KeyStore.getInstance("PKCS12").load(new ByteArrayInputStream(bo.toByteArray()), "wrong".toCharArray()); } catch (IOException e) { p("ks " + e.getMessage()); }
        } catch (Exception e) { p("ks-secret " + e.getClass().getName() + " " + e.getMessage()); }
        KeyStore jk = KeyStore.getInstance("JKS"); jk.load(null, null); p("jks " + jk.size() + " " + jk.getProvider().getName());
        p(Base64.getEncoder().encodeToString(msg) + " " + Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[]{(byte) 0xfb, (byte) 0xff}) + " " + Base64.getMimeEncoder(10, "|".getBytes()).encodeToString(new byte[30]) + " " + new String(Base64.getDecoder().decode("SGk=")) + " " + UUID.nameUUIDFromBytes("x".getBytes()) + " " + hex(KeyGenerator.getInstance("AES").generateKey().getEncoded()).length());
    }

    static SecureRandom deterministic(long seed) throws Exception {
        SecureRandom s = SecureRandom.getInstance("SHA1PRNG"); s.setSeed(seed); return s;
    }

    public interface Greeter { String hi(String n); default String twice(String n) { return hi(n) + hi(n); } }
    public static class Base<T extends Comparable<T>> { protected T v; private int secret = 5; public static final int K = 9; public T get() { return v; } protected void hook() {} }
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) @interface Tag { String value() default "d"; int[] n() default {1, 2}; Class<?> c() default Object.class; }
    @Tag(value = "x", n = 3) public static class Impl extends Base<String> implements Greeter, Serializable {
        public volatile int counter; transient String tr = "t"; public static long sl = 7L;
        public Impl() {} private Impl(int a, String... b) { counter = a + b.length; }
        public String hi(String n) { return "hi " + n; }
        @Tag("m") @Deprecated public synchronized <U extends Number & Comparable<U>> List<? super U> gen(Map<String, ? extends U>[] a, U u) throws IOException { return null; }
        private static int priv(int x) { return x * 2; }
        public void varargs(String... s) {}
    }
    record Rec(int a, String b) implements Serializable {}
    sealed interface Shape permits Circ, Sq {}
    record Circ(double r) implements Shape {}
    record Sq(double s) implements Shape {}
    enum E { A { void f() {} }, B; void f() {} }

    static void reflect() throws Throwable {
        Class<?> c = Impl.class;
        p(Modifier.toString(c.getModifiers()) + " " + c.getName() + " " + c.getSimpleName() + " " + c.getCanonicalName() + " " + c.getSuperclass() + " " + c.getGenericSuperclass() + " " + Arrays.toString(c.getInterfaces()) + " " + c.getEnclosingClass() + " " + c.getNestHost() + " " + c.isMemberClass() + " " + c.getPackageName() + "|");
        List<String> ms = new ArrayList<>(); for (Method m : c.getDeclaredMethods()) ms.add(m.toGenericString() + " " + m.isSynthetic() + m.isBridge() + m.isVarArgs() + m.isDefault() + " " + Arrays.toString(m.getDeclaredAnnotations())); Collections.sort(ms); ms.forEach(AuditSecurityReflectModern::p);
        List<String> pm = new ArrayList<>(); for (Method m : c.getMethods()) pm.add(m.getDeclaringClass().getSimpleName() + "." + m.getName()); Collections.sort(pm); p(pm);
        List<String> fs = new ArrayList<>(); for (Field f : c.getDeclaredFields()) fs.add(Modifier.toString(f.getModifiers()) + " " + f.getGenericType() + " " + f.getName()); Collections.sort(fs); p(fs);
        List<String> fp = new ArrayList<>(); for (Field f : c.getFields()) fp.add(f.getDeclaringClass().getSimpleName() + "." + f.getName()); Collections.sort(fp); p(fp);
        for (Constructor<?> k : c.getDeclaredConstructors()) p(k.toGenericString() + " " + k.isVarArgs() + " " + k.getParameterCount());
        Method g = c.getMethod("gen", Map[].class, Number.class);
        p(Arrays.toString(g.getGenericParameterTypes()) + " " + g.getGenericReturnType() + " " + Arrays.toString(g.getTypeParameters()) + " " + Arrays.toString(g.getTypeParameters()[0].getBounds()) + " " + Arrays.toString(g.getExceptionTypes()) + " " + g.getAnnotation(Tag.class).value() + Arrays.toString(g.getAnnotation(Tag.class).n()) + g.getAnnotation(Tag.class).c());
        p(c.getAnnotation(Tag.class) + " " + c.getAnnotation(Tag.class).annotationType());
        Constructor<?> pc = c.getDeclaredConstructor(int.class, String[].class); pc.setAccessible(true); Impl im = (Impl) pc.newInstance(1, new String[]{"a", "b"}); p(im.counter);
        Method pr = c.getDeclaredMethod("priv", int.class); pr.setAccessible(true); p(pr.invoke(null, 21));
        Field sf = Base.class.getDeclaredField("secret"); sf.setAccessible(true); sf.setInt(im, 10); p(sf.get(im));
        try { Base.class.getField("K").set(null, 1); } catch (IllegalAccessException e) { p("IAE " + e.getMessage()); }
        try { c.getMethod("hi", String.class).invoke(im, 5); } catch (IllegalArgumentException e) { p("IArg " + e.getMessage()); }
        try { c.getMethod("hi", String.class).invoke(null, "x"); } catch (NullPointerException e) { p("NPE"); }
        try { Method m = Objects.class.getMethod("requireNonNull", Object.class); m.invoke(null, new Object[]{null}); } catch (InvocationTargetException e) { p("ITE " + e.getCause().getClass().getName()); }
        Rec r = new Rec(1, "z"); p(Arrays.toString(Rec.class.getRecordComponents()) + " " + Rec.class.isRecord() + " " + Rec.class.getRecordComponents()[1].getAccessor().invoke(r) + " " + r + " " + r.hashCode() + " " + r.equals(new Rec(1, "z")));
        p(Arrays.toString(Shape.class.getPermittedSubclasses()) + " " + Shape.class.isSealed() + " " + E.A.getClass() + " " + E.A.getDeclaringClass() + " " + E.A.getClass().isEnum() + " " + E.class.isEnum() + " " + Arrays.toString(E.class.getEnumConstants()) + " " + Modifier.toString(E.class.getModifiers()) + " " + Modifier.toString(E.A.getClass().getModifiers()));
        Greeter px = (Greeter) Proxy.newProxyInstance(Greeter.class.getClassLoader(), new Class<?>[]{Greeter.class}, (pxy, m, a) -> m.isDefault() ? InvocationHandler.invokeDefault(pxy, m, a) : m.getName() + Arrays.toString(a));
        p(px.hi("q") + " " + px.twice("w") + " " + Proxy.isProxyClass(px.getClass()) + " " + px.getClass().getSimpleName().startsWith("$Proxy") + " " + px.getClass().getModifiers());
        MethodHandles.Lookup L = MethodHandles.lookup();
        MethodHandle h1 = L.findVirtual(String.class, "concat", MethodType.methodType(String.class, String.class)), h2 = L.findStatic(AuditSecurityReflectModern.class, "sq", MethodType.methodType(int.class, int.class)), h3 = L.findConstructor(StringBuilder.class, MethodType.methodType(void.class, String.class));
        p((String) h1.invokeExact("a", "b") + (int) h2.invokeExact(7) + h3.invoke("sb") + " " + h1.type() + " " + MethodHandles.insertArguments(h1, 1, "!").invoke("hey") + " " + MethodHandles.filterReturnValue(h2, MethodHandles.identity(int.class)).invoke(3) + " " + MethodHandles.dropArguments(h2, 0, String.class).invoke("x", 4) + " " + MethodHandles.guardWithTest(MethodHandles.constant(boolean.class, true), h2, h2).invoke(2) + " " + h1.asSpreader(Object[].class, 2).invoke((Object) new Object[]{"s", "p"}) + " " + MethodHandles.arrayElementGetter(int[].class).invoke(new int[]{4, 5}, 1) + " " + h2.asType(MethodType.methodType(Object.class, Object.class)).invoke((Object) 5) + " " + MethodHandles.catchException(MethodHandles.throwException(int.class, RuntimeException.class), RuntimeException.class, MethodHandles.dropArguments(MethodHandles.constant(int.class, -1), 0, RuntimeException.class)).invoke(new RuntimeException()));
        try { h2.invokeExact((Object) 7); } catch (WrongMethodTypeException e) { p("WMT " + e.getMessage()); }
        VarHandle vh = L.findVarHandle(Impl.class, "counter", int.class); p(vh.getAndAdd(im, 5) + " " + vh.compareAndSet(im, 8, 1) + " " + vh.get(im) + " " + vh.varType() + " " + vh.accessModeType(VarHandle.AccessMode.COMPARE_AND_SET));
        VarHandle ah = MethodHandles.arrayElementVarHandle(long[].class); long[] la = new long[3]; ah.setVolatile(la, 1, 5L); p(ah.getAndBitwiseOr(la, 1, 2L) + " " + la[1]);
        MethodHandleInfo mi = L.revealDirect(h1); p(mi.getName() + " " + mi.getReferenceKind() + " " + mi.getDeclaringClass() + " " + mi.getMethodType());
        CallSite cs = new MutableCallSite(MethodType.methodType(int.class)); cs.setTarget(MethodHandles.constant(int.class, 4)); p((int) cs.dynamicInvoker().invokeExact());
        p(Class.forName("[[Ljava.lang.String;").getComponentType() + " " + int[].class.getName() + " " + String[][].class.getTypeName() + " " + Array.newInstance(int.class, 2, 3).getClass().getSimpleName() + " " + Integer.TYPE + " " + void.class.isPrimitive() + " " + Class.forName("java.util.Map$Entry").getSimpleName() + " " + Impl.class.getClassLoader().getClass().getSimpleName().isEmpty() + " " + Object.class.getModule().getName() + " " + Impl.class.getModule().isNamed());
        p(Arrays.toString(Class.forName("java.util.ArrayList").getDeclaredConstructors().length > 0 ? new int[]{Class.forName("java.util.ArrayList").getConstructors().length} : null) + " " + Thread.State.class.getEnumConstants().length + " " + Character.UnicodeBlock.of('é') + " " + StackWalker.getInstance().walk(s -> s.map(f -> f.getMethodName()).limit(2).toList()));
    }
    public static int sq(int x) { return x * x; }

    static class Ser implements Serializable {
        private static final long serialVersionUID = 3L;
        int a = 1; String s = "str"; transient int t = 9; int[] arr = {1, 2}; List<String> l = new ArrayList<>(List.of("x", "y")); Map<String, Integer> m = new TreeMap<>(Map.of("k", 1)); Ser self; Date d = new Date(0); E e = E.B; Rec r = null;
        private void writeObject(ObjectOutputStream o) throws IOException { o.defaultWriteObject(); o.writeInt(77); }
        private void readObject(ObjectInputStream i) throws IOException, ClassNotFoundException { i.defaultReadObject(); t = i.readInt(); }
    }
    static class Ext implements Externalizable { int q = 4; public Ext() {} public void writeExternal(ObjectOutput o) throws IOException { o.writeInt(q * 2); } public void readExternal(ObjectInput i) throws IOException { q = i.readInt(); } }

    static void serial() throws Exception {
        Ser s = new Ser(); s.self = s;
        ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (ObjectOutputStream o = new ObjectOutputStream(bo)) { o.writeObject(s); o.writeObject(new Ext()); o.writeObject(new Rec(3, "r")); o.writeObject(new Object[]{1, "two", 3.0, 'c', null, new int[]{1}, List.of(1, 2), Optional.class, java.time.LocalDate.of(2020, 1, 2), new java.math.BigDecimal("1.5"), Locale.FRANCE, E.A, new StringBuilder("sb"), UUID.fromString("00000000-0000-0001-0000-000000000002"), java.util.concurrent.TimeUnit.DAYS}); }
        byte[] b = bo.toByteArray(); p(b.length + " " + hex(Arrays.copyOf(b, 40)) + " " + Arrays.hashCode(b));
        try (ObjectInputStream i = new ObjectInputStream(new ByteArrayInputStream(b))) {
            Ser r = (Ser) i.readObject(); p(r.a + r.s + r.t + Arrays.toString(r.arr) + r.l + r.m + (r.self == r) + r.d.getTime() + r.e); p(((Ext) i.readObject()).q); p(i.readObject()); p(Arrays.deepToString((Object[]) i.readObject()));
        }
        ObjectStreamClass osc = ObjectStreamClass.lookup(Ser.class); p(osc.getSerialVersionUID() + " " + osc.getFields().length + " " + ObjectStreamClass.lookup(Ext.class).getSerialVersionUID() + " " + ObjectStreamClass.lookup(Rec.class).getSerialVersionUID() + " " + Arrays.toString(osc.getFields()));
        try (ObjectInputStream i = new ObjectInputStream(new ByteArrayInputStream(b))) { i.setObjectInputFilter(ObjectInputFilter.Config.createFilter("maxdepth=1;!*")); i.readObject(); } catch (InvalidClassException e) { p("filtered " + e.getMessage()); }
        try { new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(new Object()); } catch (NotSerializableException e) { p("NSE " + e.getMessage()); }
        try { new ObjectInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4})); } catch (StreamCorruptedException e) { p("SCE " + e.getMessage()); }
    }
}
