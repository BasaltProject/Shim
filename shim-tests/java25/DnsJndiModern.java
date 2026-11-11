/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.naming.*; import javax.naming.directory.*; import java.net.*; import java.util.*; import java.io.*;
public class DnsJndiModern {
 static void name(ByteArrayOutputStream o,String n){ if(n.length()>0&&!n.equals(".")) for(String l:n.split("\\.")){o.write(l.length());o.writeBytes(l.getBytes());} o.write(0);} 
 static byte[] rr(String n,int type,int ttl,byte[] data){ ByteArrayOutputStream o=new ByteArrayOutputStream(); name(o,n); o.write(type>>8);o.write(type);o.write(0);o.write(1);o.write(ttl>>24);o.write(ttl>>16);o.write(ttl>>8);o.write(ttl);o.write(data.length>>8);o.write(data.length);o.writeBytes(data); return o.toByteArray(); }
 static byte[] nm(String n){ByteArrayOutputStream o=new ByteArrayOutputStream();name(o,n);return o.toByteArray();}
 static List<byte[]> answers(String qn,int qt){ List<byte[]> a=new ArrayList<>(); qn=qn.toLowerCase();
  if(qn.equals("host.example.com")&&(qt==1||qt==255)) { a.add(rr(qn,1,300,new byte[]{1,2,3,4})); a.add(rr(qn,1,300,new byte[]{5,6,7,8})); }
  if(qn.equals("host.example.com")&&(qt==28||qt==255)) a.add(rr(qn,28,300,new byte[]{0x20,0x01,0xd,(byte)0xb8,0,0,0,0,0,0,0,0,0,0,0,1}));
  if(qn.equals("example.com")&&(qt==15||qt==255)){ ByteArrayOutputStream o=new ByteArrayOutputStream(); o.write(0);o.write(10);o.writeBytes(nm("mail.example.com")); a.add(rr(qn,15,300,o.toByteArray())); o=new ByteArrayOutputStream(); o.write(0);o.write(20);o.writeBytes(nm("mail2.example.com")); a.add(rr(qn,15,300,o.toByteArray())); }
  if(qn.equals("example.com")&&(qt==16||qt==255)){ ByteArrayOutputStream o=new ByteArrayOutputStream(); byte[] t1="v=spf1 -all".getBytes(); o.write(t1.length);o.writeBytes(t1); byte[] t2="second".getBytes(); o.write(t2.length);o.writeBytes(t2); a.add(rr(qn,16,300,o.toByteArray())); }
  if(qn.equals("example.com")&&(qt==2||qt==255)) a.add(rr(qn,2,300,nm("ns1.example.com")));
  if(qn.equals("www.example.com")&&(qt==5||qt==255||qt==1)) a.add(rr(qn,5,300,nm("host.example.com")));
  if(qn.equals("_sip._tcp.example.com")&&(qt==33||qt==255)){ ByteArrayOutputStream o=new ByteArrayOutputStream(); o.write(0);o.write(5);o.write(0);o.write(6);o.write(0x13);o.write(0xc4);o.writeBytes(nm("sip.example.com")); a.add(rr(qn,33,300,o.toByteArray())); }
  if(qn.equals("4.3.2.1.in-addr.arpa")&&(qt==12||qt==255)) a.add(rr(qn,12,300,nm("host.example.com")));
  if(qn.equals("example.com")&&(qt==6||qt==255)){ ByteArrayOutputStream o=new ByteArrayOutputStream(); o.writeBytes(nm("ns1.example.com")); o.writeBytes(nm("admin.example.com")); for(int v:new int[]{2024010101,3600,600,86400,300}){o.write(v>>24);o.write(v>>16);o.write(v>>8);o.write(v);} a.add(rr(qn,6,300,o.toByteArray())); }
  return a; }
 public static void main(String[] x) throws Exception {
  DatagramSocket ds=new DatagramSocket(0,InetAddress.getLoopbackAddress()); int port=ds.getLocalPort();
  Thread t=new Thread(()->{ try{ byte[] buf=new byte[512]; while(true){ DatagramPacket p=new DatagramPacket(buf,512); ds.receive(p); byte[] q=Arrays.copyOf(p.getData(),p.getLength());
    int i=12; StringBuilder qn=new StringBuilder(); while(q[i]!=0){int l=q[i++]&0xff; if(qn.length()>0)qn.append('.'); qn.append(new String(q,i,l)); i+=l;} i++; int qt=((q[i]&0xff)<<8)|(q[i+1]&0xff);
    List<byte[]> ans=answers(qn.toString(),qt); System.out.println("  Q "+qn+" "+qt+" flags="+Integer.toHexString(((q[2]&0xff)<<8)|(q[3]&0xff))+" arcount="+q[11]); ByteArrayOutputStream o=new ByteArrayOutputStream(); o.write(q[0]);o.write(q[1]); int flags=0x8400|(ans.isEmpty()?3:0); o.write(flags>>8);o.write(flags); o.write(0);o.write(1); o.write(0);o.write(ans.size());o.write(0);o.write(0);o.write(0);o.write(0); o.write(q,12,i+4-12); for(byte[] r:ans)o.writeBytes(r);
    byte[] out=o.toByteArray(); ds.send(new DatagramPacket(out,out.length,p.getSocketAddress())); } }catch(Exception e){} }); t.setDaemon(true); t.start();
  Hashtable<String,String> env=new Hashtable<>(); env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.dns.DnsContextFactory"); env.put(Context.PROVIDER_URL,"dns://127.0.0.1:"+port+"/example.com"); env.put("com.sun.jndi.dns.timeout.initial","500"); env.put("com.sun.jndi.dns.timeout.retries","1");
  DirContext c=new InitialDirContext(env);
  System.out.println(c.getClass().getName()+" "+c.getNameInNamespace()+"|"+c.getEnvironment().get(Context.PROVIDER_URL).toString().replaceAll(":\\d+/","/"));
  String[][] qs={{"host","A"},{"host","AAAA"},{"","MX"},{"","TXT"},{"","NS"},{"www","CNAME"},{"_sip._tcp","SRV"},{"","SOA"},{"host","*"},{"host","A","AAAA"},{"nosuch","A"},{"host","BOGUS"},{"host","IN A"},{"host","TXT"}};
  for(String[] q:qs){ try{ Attributes at=c.getAttributes(q[0], Arrays.copyOfRange(q,1,q.length)); List<String> ids=new ArrayList<>(); NamingEnumeration<? extends Attribute> e=at.getAll(); StringBuilder sb=new StringBuilder(); while(e.hasMore()){ Attribute a=e.next(); List<String> vs=new ArrayList<>(); NamingEnumeration<?> ve=a.getAll(); while(ve.hasMore()) vs.add(String.valueOf(ve.next())); Collections.sort(vs); sb.append(a.getID()+"="+vs+" "); }
    System.out.println(Arrays.toString(q)+" -> "+at.size()+" "+(sb.toString().isEmpty()?"":sb)); }catch(Throwable e){System.out.println(Arrays.toString(q)+" EXC "+e.getClass().getName()+": "+e.getMessage());} }
  try{ Attributes at=c.getAttributes("host"); System.out.println("all "+at.size()); }catch(Throwable e){System.out.println("all EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ Attributes at=c.getAttributes("www"); System.out.println("allwww "+at.size()+" "+at); }catch(Throwable e){System.out.println("allwww EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ Object o=c.lookup("host"); System.out.println("lookup "+o.getClass().getName()); }catch(Throwable e){System.out.println("lookup EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ Attributes at=c.getAttributes("4.3.2.1.in-addr.arpa",new String[]{"PTR"}); System.out.println("ptr "+at); }catch(Throwable e){System.out.println("ptr EXC "+e);}
  try{ System.out.println(c.getSchema("")); }catch(Throwable e){System.out.println("schema EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ c.bind("a","b"); }catch(Throwable e){System.out.println("bind EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ NamingEnumeration<?> en=c.search("host",new BasicAttributes("A",true)); int n=0; while(en.hasMore()){en.next();n++;} System.out.println("search "+n); }catch(Throwable e){System.out.println("search EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ Attributes at=c.getAttributes("HOST.EXAMPLE.COM.",new String[]{"A"}); System.out.println("abs "+at.size()); }catch(Throwable e){System.out.println("abs EXC "+e.getClass().getName()+": "+e.getMessage());}

  String[] names={"nosuch","a.b","","host.example.com.","host.example.com","example.com.","www.example.com"};
  for(String n:names){ try{ Attributes at=c.getAttributes(n,new String[]{"A"}); System.out.println("A["+n+"] ok "+at.size()); }catch(NamingException e){System.out.println("A["+n+"] EXC "+e.getClass().getSimpleName()+" "+e.getMessage()+" rem="+e.getRemainingName()+" res="+e.getResolvedName()+" cause="+e.getRootCause()); }}
  for(String n:new String[]{"host","","example.com.","nosuch"}){ try{ Object o=c.lookup(n); System.out.println("lookup["+n+"] "+o.getClass().getName()+" "+((Context)o).getNameInNamespace()); }catch(NamingException e){System.out.println("lookup["+n+"] EXC "+e.getClass().getSimpleName()+" "+e.getMessage()+" rem="+e.getRemainingName()); }}
  try{ System.out.println(c.composeName("a","b.c")+" "+c.composeName("a","")+" "+c.composeName("","x.y.")); }catch(Throwable e){System.out.println("compose EXC "+e);} 
  try{ System.out.println(c.getNameParser("").parse("a.b.c")+" size="+c.getNameParser("").parse("a.b.c").size()+" "+c.getNameParser("").parse("a.b.c").get(0)); }catch(Throwable e){System.out.println("parse EXC "+e);} 
  try{ Name nm=c.getNameParser("").parse("a.b.c"); System.out.println(nm.getClass().getName()); }catch(Throwable e){}
  try{ NamingEnumeration<?> en=c.list("host"); System.out.println("list ok"); }catch(Throwable e){System.out.println("list EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ c.lookup(new javax.naming.ldap.LdapName("cn=x")); }catch(Throwable e){System.out.println("ldapname EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ c.getAttributes(new CompositeName("host"),new String[]{"A"}); System.out.println("composite ok"); }catch(Throwable e){System.out.println("composite EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ System.out.println(c.addToEnvironment("com.sun.jndi.dns.recursion","false")); System.out.println(c.getEnvironment().get("com.sun.jndi.dns.recursion")); }catch(Throwable e){System.out.println("env EXC "+e);} 
  try{ System.out.println(c.getAttributes("host",new String[]{"A"}).size()); }catch(Throwable e){System.out.println("A2 EXC "+e);} 
  try{ InitialContext ic=new InitialContext(); Object o=ic.lookup("dns://127.0.0.1:"+port+"/host.example.com"); System.out.println("urllookup "+o.getClass().getName()); Attributes at=((DirContext)new InitialDirContext().lookup("dns://127.0.0.1:"+port+"/example.com")).getAttributes("host",new String[]{"A"}); System.out.println("urlattrs "+at.size()); }catch(Throwable e){System.out.println("url EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ Attributes at=new InitialDirContext().getAttributes("dns://127.0.0.1:"+port+"/host.example.com",new String[]{"A"}); System.out.println("urlA "+at.size()); }catch(Throwable e){System.out.println("urlA EXC "+e.getClass().getName()+": "+e.getMessage());}
  try{ Attributes at=new InitialDirContext().getAttributes("dns://127.0.0.1:"+port+"/example.com",new String[]{"MX"}); System.out.println("urlMX "+at.size()); }catch(Throwable e){System.out.println("urlMX EXC "+e.getClass().getName()+": "+e.getMessage());}
  c.close();
 }}
