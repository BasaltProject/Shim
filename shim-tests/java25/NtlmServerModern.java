/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.security.sasl.*;import javax.security.auth.callback.*;import java.util.*;
public class NtlmServerModern{
 static CallbackHandler h(String tag,String user,String pw,String realm){return cbs->{for(Callback c:cbs){
  if(c instanceof RealmCallback r){System.out.println(tag+" Realm default="+r.getDefaultText()+" prompt="+r.getPrompt());if(realm!=null)r.setText(realm);}
  else if(c instanceof NameCallback n){System.out.println(tag+" Name default="+n.getDefaultName()+" prompt="+n.getPrompt());n.setName(user);}
  else if(c instanceof PasswordCallback p){System.out.println(tag+" Password prompt="+p.getPrompt()+" echo="+p.isEchoOn());p.setPassword(pw.toCharArray());}
  else if(c instanceof AuthorizeCallback a){System.out.println(tag+" Authorize "+a.getAuthenticationID()+" "+a.getAuthorizationID());a.setAuthorized(true);a.setAuthorizedID(a.getAuthorizationID());}
  else System.out.println(tag+" other "+c);}};}
 static void run(String name,String ver,String cpw,String spw){
  try{
   Map<String,Object> sp=new HashMap<>();if(ver!=null)sp.put("com.sun.security.sasl.ntlm.version",ver);sp.put("com.sun.security.sasl.ntlm.domain","DOM");sp.put("com.sun.security.sasl.ntlm.hostname","srv");sp.put("com.sun.security.sasl.ntlm.random",new Random(5));
   Map<String,Object> cp=new HashMap<>();if(ver!=null)cp.put("com.sun.security.sasl.ntlm.version",ver);cp.put("com.sun.security.sasl.ntlm.hostname","cli");cp.put("com.sun.security.sasl.ntlm.random",new Random(6));
   SaslServer s=Sasl.createSaslServer("NTLM","imap","srv",sp,h("S","alice",spw,null));
   System.out.println(name+" server "+(s==null?null:s.getMechanismName()));
   SaslClient c=Sasl.createSaslClient(new String[]{"NTLM"},null,"imap","srv",cp,h("C","alice",cpw,"DOM"));
   byte[] m1=c.evaluateChallenge(new byte[0]);
   byte[] m2=s.evaluateResponse(m1);
   System.out.println(name+" t2 len="+m2.length+" complete="+s.isComplete());
   byte[] m3=c.evaluateChallenge(m2);
   byte[] r=s.evaluateResponse(m3);
   System.out.println(name+" done r="+r+" complete="+s.isComplete()+" authz="+s.getAuthorizationID()+" prop="+s.getNegotiatedProperty(Sasl.QOP));
  }catch(Throwable e){System.out.println(name+" ! "+e+" cause="+e.getCause());}
 }
 public static void main(String[] a)throws Exception{
  for(String v:new String[]{null,"LM","NTLM","LM/NTLM","LMv2","NTLMv2"})run("ok-"+v,v,"pw","pw");
  run("badpw",null,"x","pw");
  try{Sasl.createSaslServer("NTLM","imap","srv",new HashMap<>(),null);}catch(Throwable e){System.out.println("nocbh "+e);}
  try{Map<String,Object> p=new HashMap<>();p.put("com.sun.security.sasl.ntlm.version","XX");Sasl.createSaslServer("NTLM","imap","srv",p,h("S","a","b",null));}catch(Throwable e){System.out.println("badver "+e+" "+e.getCause());}
  SaslServer s=Sasl.createSaslServer("NTLM","imap","srv",new HashMap<>(),h("S","alice","pw",null));
  System.out.println(s.getClass().getName());
  try{s.evaluateResponse(new byte[5]);}catch(Throwable e){System.out.println("short "+e+" "+e.getCause());}
  try{s.getAuthorizationID();}catch(Throwable e){System.out.println("authz-early "+e);}
  try{System.out.println(s.getNegotiatedProperty("x"));}catch(Throwable e){System.out.println("prop-early "+e);}
  try{s.wrap(new byte[1],0,1);}catch(Throwable e){System.out.println("wrap "+e);}
  System.out.println(Arrays.toString(Sasl.createSaslServer("NTLM","imap","srv",new HashMap<>(),h("S","a","b",null)).getClass().getInterfaces()));
 }}
