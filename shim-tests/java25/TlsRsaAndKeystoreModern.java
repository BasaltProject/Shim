/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.security.*; import java.security.cert.*; import javax.crypto.*;
public class TlsRsaAndKeystoreModern { public static void main(String[] a) throws Exception {
 KeyPairGenerator g=KeyPairGenerator.getInstance("RSA"); g.initialize(1024, new SecureRandom(new byte[]{1})); KeyPair kp=g.generateKeyPair();
 Signature s=Signature.getInstance("MD5andSHA1withRSA"); s.initSign(kp.getPrivate()); s.update("hello".getBytes()); byte[] sig=s.sign();
 s.initVerify(kp.getPublic()); s.update("hello".getBytes()); System.out.println(s.verify(sig)+" "+sig.length+" "+s.getProvider().getName());
 s.initVerify(kp.getPublic()); s.update("hellp".getBytes()); System.out.println(s.verify(sig));
 KeyStore k=KeyStore.getInstance("PKCS12", "SunJSSE"); k.load(null,null); System.out.println(k.getType()+" "+k.getProvider().getName());
 System.out.println(CertStore.getInstance("com.sun.security.IndexedCollection", new CollectionCertStoreParameters()).getType());
 try { CertStore.getInstance("IndexedCollection", new CollectionCertStoreParameters()); System.out.println("found"); } catch(Exception e){ System.out.println(e.getClass().getName()); }
 Cipher c=Cipher.getInstance("AES/CTR/NoPadding"); c.init(Cipher.ENCRYPT_MODE,new javax.crypto.spec.SecretKeySpec(new byte[16],"AES"),new javax.crypto.spec.IvParameterSpec(new byte[16])); System.out.println(java.util.HexFormat.of().formatHex(c.doFinal(new byte[20]))+" "+c.getProvider().getName());
}}
