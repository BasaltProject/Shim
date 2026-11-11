/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.security.sasl.*; import javax.security.auth.callback.*; import java.util.*; import java.io.*;
public class NtlmClientModern { static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x));return s.toString();}
 static byte[] type2(boolean unicode, byte[] chal){ ByteArrayOutputStream o=new ByteArrayOutputStream(); try{ o.write("NTLMSSP\0".getBytes()); o.write(new byte[]{2,0,0,0}); o.write(new byte[]{0,0,0,0,0,0,0,0}); int flags=0x00000200|(unicode?1:2)|0x00008000|0x00080000; o.write(new byte[]{(byte)flags,(byte)(flags>>8),(byte)(flags>>16),(byte)(flags>>24)}); o.write(chal);}catch(Exception e){} return o.toByteArray(); }
 public static void main(String[] a) throws Exception {
  CallbackHandler h=cbs->{for(Callback c:cbs){ if(c instanceof NameCallback n)n.setName("user"); else if(c instanceof PasswordCallback p)p.setPassword("SecREt01".toCharArray()); else if(c instanceof javax.security.sasl.RealmCallback r)r.setText("DOMAIN"); else throw new UnsupportedCallbackException(c);}};
  for(String ver:new String[]{null,"LM","NTLM","LMv2","NTLMv2","LM/NTLM","Bogus"}){
  Map<String,Object> props=new HashMap<>(); props.put("com.sun.security.sasl.ntlm.domain","DOMAIN"); props.put("com.sun.security.sasl.ntlm.hostname","HOST"); props.put("com.sun.security.sasl.ntlm.random",new Random(42)); if(ver!=null)props.put("com.sun.security.sasl.ntlm.version",ver);
  try{
  SaslClient c=Sasl.createSaslClient(new String[]{"NTLM"},null,"ldap","srv",props,h);
  System.out.println(ver+" "+c.getMechanismName()+" "+c.hasInitialResponse()+" "+(c.getClass().getName()));
  byte[] t1=c.evaluateChallenge(new byte[0]); System.out.println(hex(t1)); System.out.println(c.getMechanismName()+" "+c.hasInitialResponse()+" "+Arrays.toString(Sasl.getSaslClientFactories().hasMoreElements()?new String[0]:null));
  byte[] t3=c.evaluateChallenge(type2(true,new byte[]{1,2,3,4,5,6,7,8})); String h3=hex(t3).replaceAll("[0-9a-f]{32}(0101000000000000)[0-9a-f]{16}","X$1Y"); System.out.println(ver+" "+h3+" "+c.isComplete()); try{c.evaluateChallenge(new byte[0]);}catch(Throwable e){System.out.println(e);}
  }catch(Throwable t){System.out.println(ver+" EXC "+t);} }
 }}
