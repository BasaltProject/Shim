/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.*;import java.net.URI;import java.nio.file.*;import java.security.*;import java.util.*;import javax.crypto.spec.*;
public class DomainKeyStoreModern{ public static void main(String[] a)throws Exception{
 Path d=Files.createTempDirectory("dks");
 KeyStore ks=KeyStore.getInstance("PKCS12");ks.load(null,null);
 ks.setEntry("k",new KeyStore.SecretKeyEntry(new SecretKeySpec(new byte[16],"AES")),new KeyStore.PasswordProtection("p".toCharArray()));
 try(OutputStream o=Files.newOutputStream(d.resolve("o.p12"))){ks.store(o,"sp".toCharArray());}
 Path cf=d.resolve("c.cfg");
 String[] cfgs={"domain x { keystore k keystoreType=\"PKCS12\"; };","domain x keystoreType=\"PKCS12\" keystoreURI=\""+d.resolve("o.p12").toUri()+"\" { keystore k; };",
  "domain x { keystore k keystoreType=\"PKCS12\" keystoreURI=\""+d.resolve("o.p12").toUri()+"\"; };","domain x { keystore k keystoreType=\"PKCS12\" keystoreURI=\""+d.resolve("o.p12").toUri()+"\" keystorePasswordEnv=\"HOME\"; };",
  "domain x { keystore k keystoreType=\"JKS\" keystoreURI=\""+d.resolve("o.p12").toUri()+"\"; };","domain x { keystore k keystoreType=\"BOGUS\" keystoreURI=\""+d.resolve("o.p12").toUri()+"\"; };","domain x { keystore k keystoreType=\"PKCS12\" keystoreURI=\""+d.resolve("none.p12").toUri()+"\"; };",
  "domain x { keystore k keystoreType=\"PKCS12\" keystoreURI=\""+d.resolve("o.p12").toUri()+"\"; keystore k keystoreType=\"PKCS12\" keystoreURI=\""+d.resolve("o.p12").toUri()+"\";};",
  "domain x { keystore k keystoreType=\"PKCS12\" keystoreURI=\""+d.resolve("o.p12").toUri()+"\" keystoreProviderName=\"SUN\"; };","domain x { keystore k keystoreType=\"PKCS12\" keystoreURI=\""+d.resolve("o.p12").toUri()+"\" keystoreProviderName=\"NOPE\"; };","domain x { };","domain x { keystore k keystoreType=\"PKCS12\" keystoreURI=\""+d.resolve("o.p12").toUri()+"\"; } "};
 int i=0;
 for(String c:cfgs){ i++;Files.writeString(cf,c);
  for(int pw=0;pw<3;pw++){
  Map<String,KeyStore.ProtectionParameter> m=new HashMap<>();
  if(pw==1)m.put("x:k",new KeyStore.PasswordProtection("sp".toCharArray()));
  if(pw==2)m.put("x:k",new KeyStore.PasswordProtection("zz".toCharArray()));
  try{KeyStore k=KeyStore.getInstance("DKS");k.load(new DomainLoadStoreParameter(cf.toUri(),m));System.out.println(i+"/"+pw+" ok "+Collections.list(k.aliases()));}catch(Throwable e){System.out.println(i+"/"+pw+" "+e.getClass().getName()+": "+e.getMessage().replaceAll("[^ \"]*dks[0-9]*","TMP")+(e.getCause()!=null?" / "+e.getCause().getClass().getName()+": "+e.getCause().getMessage():""));}
 }}
 Files.writeString(cf,cfgs[1]);
 Map<String,KeyStore.ProtectionParameter> m=Map.of("x:k",new KeyStore.PasswordProtection("sp".toCharArray()));
 KeyStore k=KeyStore.getInstance("DKS");k.load(new DomainLoadStoreParameter(cf.toUri(),m));
 System.out.println(k.getCertificateAlias(null)+" "+k.getCertificateChain("x k")+" "+k.getCreationDate("x k")+" "+(k.getCreationDate("k k")!=null)+" "+k.getCertificate("k"));
 for(String s:new String[]{"k","k k","k  k"," k","k k k"}){try{System.out.println("["+s+"] "+k.containsAlias(s)+" "+k.isKeyEntry(s)+" "+k.getKey(s,"p".toCharArray()).getAlgorithm());}catch(Exception e){System.out.println("["+s+"] "+e);}}
 try{k.setKeyEntry("k w",new SecretKeySpec(new byte[16],"AES"),"p".toCharArray(),null);System.out.println(k.size());}catch(Exception e){System.out.println(e);}
 try{k.setKeyEntry("nospace",new SecretKeySpec(new byte[16],"AES"),"p".toCharArray(),null);}catch(Exception e){System.out.println(e);}
 try{k.setKeyEntry("zz w",new SecretKeySpec(new byte[16],"AES"),"p".toCharArray(),null);}catch(Exception e){System.out.println(e);}
 try{k.store(new DomainLoadStoreParameter(cf.toUri(),m));}catch(Exception e){System.out.println(e);}
 try{k.store(new DomainLoadStoreParameter(cf.toUri(),Map.of()));}catch(Exception e){System.out.println(e);}
 try{k.store(null);}catch(Exception e){System.out.println(e);}
 try{k.load(null);}catch(Exception e){System.out.println(e);}
 try{KeyStore.getInstance("DKS").load(null);System.out.println("ld null ok");}catch(Exception e){System.out.println(e);}
 try{KeyStore.getInstance("DKS").load(new KeyStore.LoadStoreParameter(){public KeyStore.ProtectionParameter getProtectionParameter(){return null;}});}catch(Exception e){System.out.println(e);}
 try{KeyStore.getInstance("DKS").store(null,null);}catch(Exception e){System.out.println(e);}
 try{System.out.println(KeyStore.getInstance("DKS").size());}catch(Exception e){System.out.println(e);}
}}
