/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.*; import java.net.*; import java.nio.charset.StandardCharsets; import javax.naming.*; import javax.naming.directory.*; import javax.naming.ldap.*; import java.util.*;
public class LdapJndiModern {
 static String attrs(Attributes a) throws NamingException { List<String> l=new ArrayList<>(); NamingEnumeration<? extends Attribute> e=a.getAll(); while(e.hasMore()){ Attribute x=e.next(); List<String> v=new ArrayList<>(); NamingEnumeration<?> ve=x.getAll(); while(ve.hasMore()){ Object o=ve.next(); v.add(o instanceof byte[]?"bytes":String.valueOf(o)); } Collections.sort(v); l.add(x.getID()+"="+v); } Collections.sort(l); return l.toString(); }
 static void run(String n, Callable c){ try{ c.call(); }catch(Throwable e){ System.out.println(n+" EXC "+e.getClass().getName()+": "+e.getMessage()); } }
 interface Callable { void call() throws Exception; }
 static void sr(NamingEnumeration<SearchResult> e) throws Exception { List<String> out=new ArrayList<>(); while(e.hasMore()){ SearchResult r=e.next(); out.add(r.getName()+"|"+r.getClassName()+"|"+r.isRelative()+"|"+attrs(r.getAttributes())+"|"+(r.getObject()==null?"null":r.getObject().getClass().getSimpleName())); } Collections.sort(out); for(String s:out) System.out.println("  "+s); }
 public static void main(String[] a) throws Exception {
  FakeLdap srv=new FakeLdap();
  Hashtable<String,Object> env=new Hashtable<>(); env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory"); env.put(Context.PROVIDER_URL,"ldap://127.0.0.1:"+srv.port()+"/dc=example,dc=com");
  DirContext c=new InitialDirContext(env); System.out.println(c.getClass().getName()+" ["+c.getNameInNamespace()+"]");
  run("attrs",()->System.out.println("attrs "+attrs(c.getAttributes("cn=John Smith,ou=people"))));
  run("attrs2",()->System.out.println("attrs2 "+attrs(c.getAttributes("cn=John Smith,ou=people",new String[]{"mail","sn"}))));
  run("attrsnf",()->System.out.println(attrs(c.getAttributes("cn=Nobody,ou=people"))));
  run("lookup",()->{ Object o=c.lookup("ou=people"); System.out.println("lookup "+o.getClass().getName()+" "+((Context)o).getNameInNamespace()); });
  run("list",()->{ NamingEnumeration<NameClassPair> e=c.list("ou=people"); List<String> o=new ArrayList<>(); while(e.hasMore()){ NameClassPair p=e.next(); o.add(p.getName()+"|"+p.getClassName()+"|"+p.isRelative()+"|"+p.getNameInNamespace()); } Collections.sort(o); System.out.println("list "+o); });
  run("listB",()->{ NamingEnumeration<Binding> e=c.listBindings("ou=people"); List<String> o=new ArrayList<>(); while(e.hasMore()){ Binding p=e.next(); o.add(p.getName()+"|"+p.getClassName()+"|"+p.getObject().getClass().getSimpleName()); } Collections.sort(o); System.out.println("listB "+o); });
  run("search1",()->{ SearchControls sc=new SearchControls(); sc.setSearchScope(SearchControls.SUBTREE_SCOPE); System.out.println("search1"); sr(c.search("","(&(objectClass=person)(mail=*))",sc)); });
  run("search2",()->{ SearchControls sc=new SearchControls(); sc.setSearchScope(SearchControls.ONELEVEL_SCOPE); sc.setReturningAttributes(new String[]{"cn"}); sc.setCountLimit(0); System.out.println("search2"); sr(c.search("ou=people","(|(sn=Smith)(sn=D*))",sc)); });
  run("search3",()->{ SearchControls sc=new SearchControls(); sc.setReturningObjFlag(true); System.out.println("search3"); sr(c.search("ou=people","(cn={0})",new Object[]{"Jane Doe"},sc)); });
  run("search4",()->{ System.out.println("search4"); sr(c.search("ou=people",new BasicAttributes("sn","Smith"))); });
  run("search5",()->{ SearchControls sc=new SearchControls(); sc.setCountLimit(1); sc.setSearchScope(2); System.out.println("search5"); sr(c.search("","(objectClass=*)",sc)); });
  run("search6",()->{ SearchControls sc=new SearchControls(); sc.setSearchScope(2); sc.setTimeLimit(5000); System.out.println("search6"); sr(c.search("","(!(objectClass=person))",sc)); });
  run("search7",()->{ SearchControls sc=new SearchControls(); sc.setSearchScope(2); System.out.println("search7"); sr(c.search("","(cn=J*)",sc)); });
  run("add",()->{ BasicAttributes at=new BasicAttributes(true); at.put("objectClass","person"); at.put("cn","New Guy"); at.put("sn","Guy"); c.createSubcontext("cn=New Guy,ou=people",at); System.out.println("added"); });
  run("addDup",()->{ BasicAttributes at=new BasicAttributes(true); at.put("cn","New Guy"); c.createSubcontext("cn=New Guy,ou=people",at); });
  run("bind",()->{ BasicAttributes at=new BasicAttributes(true); at.put("objectClass","person"); at.put("cn","Bound"); at.put("sn","B"); c.bind("cn=Bound,ou=people","x",at); });
  run("modify",()->{ c.modifyAttributes("cn=John Smith,ou=people",new ModificationItem[]{new ModificationItem(DirContext.ADD_ATTRIBUTE,new BasicAttribute("telephoneNumber","123")),new ModificationItem(DirContext.REPLACE_ATTRIBUTE,new BasicAttribute("sn","Smythe")),new ModificationItem(DirContext.REMOVE_ATTRIBUTE,new BasicAttribute("mail","js@example.com"))}); System.out.println("modified "+attrs(c.getAttributes("cn=John Smith,ou=people"))); });
  run("modify2",()->{ BasicAttributes at=new BasicAttributes(true); at.put("description","hi"); c.modifyAttributes("cn=Jane Doe,ou=people",DirContext.ADD_ATTRIBUTE,at); System.out.println("modified2 "+attrs(c.getAttributes("cn=Jane Doe,ou=people"))); });
  run("rename",()->{ c.rename("cn=Jane Doe,ou=people","cn=Janet Doe,ou=people"); System.out.println("renamed "+attrs(c.getAttributes("cn=Janet Doe,ou=people"))); });
  run("destroy",()->{ c.destroySubcontext("cn=New Guy,ou=people"); System.out.println("destroyed"); });
  run("destroyNf",()->{ c.destroySubcontext("cn=Nope,ou=people"); });
  run("unbind",()->{ c.unbind("cn=Bound,ou=people"); System.out.println("unbound"); });
  run("rebind",()->{ c.rebind("cn=Bound,ou=people","x"); });
  run("auth bad",()->{ Hashtable<String,Object> e2=new Hashtable<>(env); e2.put(Context.SECURITY_AUTHENTICATION,"simple"); e2.put(Context.SECURITY_PRINCIPAL,"cn=admin,dc=example,dc=com"); e2.put(Context.SECURITY_CREDENTIALS,"wrong"); new InitialDirContext(e2); });
  run("auth ok",()->{ Hashtable<String,Object> e2=new Hashtable<>(env); e2.put(Context.SECURITY_AUTHENTICATION,"simple"); e2.put(Context.SECURITY_PRINCIPAL,"cn=admin,dc=example,dc=com"); e2.put(Context.SECURITY_CREDENTIALS,"adminpw"); DirContext d=new InitialDirContext(e2); System.out.println("auth ok "+attrs(d.getAttributes("cn=admin"))); d.close(); });
  run("ldapctx",()->{ LdapContext lc=new InitialLdapContext(env,null); System.out.println("ldapctx "+lc.getClass().getSimpleName()+" "+lc.getRequestControls().length+" "+(lc.getResponseControls()==null)+" "+lc.getConnectControls()); lc.setRequestControls(new Control[]{new PagedResultsControl(2,Control.NONCRITICAL)}); { StringBuilder sb=new StringBuilder(); for(Control k:lc.getRequestControls()) sb.append(k.getID()+"("+k.isCritical()+") "); System.out.println("ctrls "+sb); } SearchControls sc=new SearchControls(); sc.setSearchScope(2); sr(lc.search("","(objectClass=person)",sc)); Control[] rc=lc.getResponseControls(); System.out.println("respctrls "+(rc==null?"null":rc.length)); lc.close(); });
  run("ref",()->{ Hashtable<String,Object> e2=new Hashtable<>(env); e2.put(Context.REFERRAL,"throw"); e2.put("java.naming.ldap.derefAliases","never"); e2.put("com.sun.jndi.ldap.read.timeout","3000"); e2.put("com.sun.jndi.ldap.connect.timeout","3000"); DirContext d=new InitialDirContext(e2); sr(d.search("ou=people","(sn=Smith)",new SearchControls())); d.close(); });
  run("compose",()->System.out.println(c.composeName("cn=a","ou=b")+"|"+c.composeName(new LdapName("cn=a"),new LdapName("ou=b"))));
  run("env",()->System.out.println(c.getEnvironment().get("java.naming.provider.url").toString().replaceAll(":\\d+/","/")));
  c.close(); Thread.sleep(200);
  System.out.println("--- server log"); String[] lines=srv.log.toString().split("\n"); for(String l:lines) System.out.println(l);
 }}
/** In-memory LDAPv3 server speaking just enough BER to exercise a client; logs each request. */
class FakeLdap {
  static final class E { String dn; LinkedHashMap<String,List<String>> a=new LinkedHashMap<>(); }
  final Map<String,E> db=new TreeMap<>(); final ServerSocket ss; final StringBuilder log=new StringBuilder();
  FakeLdap() throws IOException { ss=new ServerSocket(0,50,InetAddress.getLoopbackAddress()); Thread t=new Thread(()->{ try{ while(true){ Socket s=ss.accept(); new Thread(()->serve(s)).start(); } }catch(IOException e){} }); t.setDaemon(true); t.start();
    add("dc=example,dc=com","objectClass","top","objectClass","domain","dc","example");
    add("ou=people,dc=example,dc=com","objectClass","top","objectClass","organizationalUnit","ou","people");
    add("cn=John Smith,ou=people,dc=example,dc=com","objectClass","top","objectClass","person","cn","John Smith","sn","Smith","mail","john@example.com","mail","js@example.com","userPassword","secret");
    add("cn=Jane Doe,ou=people,dc=example,dc=com","objectClass","top","objectClass","person","cn","Jane Doe","sn","Doe","mail","jane@example.com");
    add("cn=admin,dc=example,dc=com","objectClass","top","objectClass","person","cn","admin","sn","Admin","userPassword","adminpw"); }
  void add(String dn,String... kv){ E e=new E(); e.dn=dn; for(int i=0;i<kv.length;i+=2) e.a.computeIfAbsent(kv[i],k->new ArrayList<>()).add(kv[i+1]); db.put(norm(dn),e); }
  static String norm(String dn){ return dn.toLowerCase().replaceAll("\\s*,\\s*",",").replaceAll("\\s*=\\s*","="); }
  int port(){ return ss.getLocalPort(); }
  // BER helpers
  static int[] len(byte[] b,int p){ int l=b[p]&0xff; if(l<0x80) return new int[]{l,p+1}; int n=l&0x7f; l=0; for(int i=0;i<n;i++) l=(l<<8)|(b[p+1+i]&0xff); return new int[]{l,p+1+n}; }
  static byte[] tlv(int tag,byte[]... parts){ ByteArrayOutputStream c=new ByteArrayOutputStream(); for(byte[] x:parts)c.writeBytes(x); byte[] body=c.toByteArray(); ByteArrayOutputStream o=new ByteArrayOutputStream(); o.write(tag); if(body.length<128)o.write(body.length); else if(body.length<256){o.write(0x81);o.write(body.length);} else {o.write(0x82);o.write(body.length>>8);o.write(body.length);} o.writeBytes(body); return o.toByteArray(); }
  static byte[] i(int v){ return tlv(0x02,new byte[]{(byte)v}); } static byte[] os(String s){ return tlv(0x04,s.getBytes(StandardCharsets.UTF_8)); }
  static class R { int tag,start,end; R(int t,int s,int e){tag=t;start=s;end=e;} }
  static List<R> kids(byte[] b,R r){ List<R> l=new ArrayList<>(); int p=r.start; while(p<r.end){ int t=b[p]&0xff; int[] x=len(b,p+1); l.add(new R(t,x[1],x[1]+x[0])); p=x[1]+x[0]; } return l; }
  static String str(byte[] b,R r){ return new String(b,r.start,r.end-r.start,StandardCharsets.UTF_8); }
  static int intv(byte[] b,R r){ int v=0; for(int i=r.start;i<r.end;i++) v=(v<<8)|(b[i]&0xff); return v; }
  void serve(Socket s){ try(s){ InputStream in=s.getInputStream(); OutputStream out=s.getOutputStream(); while(true){ int t=in.read(); if(t<0)return; int l=in.read(); if(l>=0x80){int n=l&0x7f;l=0;for(int k=0;k<n;k++)l=(l<<8)|in.read();} byte[] body=in.readNBytes(l); handle(body,out); } }catch(Exception e){} }
  synchronized void log(String s){ log.append(s).append('\n'); }
  void send(OutputStream o,byte[] m) throws IOException { synchronized(o){ o.write(m); o.flush(); } }
  byte[] result(int tag,int code,String matched,String msg){ return tlv(tag,tlv(0x0a,new byte[]{(byte)code}),os(matched),os(msg)); }
  void handle(byte[] body,OutputStream out) throws IOException {
    R root=new R(0x30,0,body.length); List<R> k=kids(body,root); int id=intv(body,k.get(0)); R op=k.get(1); int tag=op.tag; List<R> o=kids(body,op);
    String ctrls=""; if(k.size()>2 && (k.get(2).tag&0xff)==0xa0){ for(R c:kids(body,k.get(2))){ List<R> cc=kids(body,c); ctrls+=" ctrl="+str(body,cc.get(0))+"("+(cc.size()>1&&cc.get(1).tag==1?(body[cc.get(1).start]!=0):false)+")"; } }
    byte[] msgid=tlv(0x02,new byte[]{(byte)id});
    switch(tag){
      case 0x60: { int ver=intv(body,o.get(0)); String dn=str(body,o.get(1)); R auth=o.get(2); String pw=auth.tag==0x80?str(body,auth):"<sasl>"; log("BIND v"+ver+" dn='"+dn+"' pw='"+pw+"'"+ctrls);
        int code=0; if(!dn.isEmpty()){ E e=db.get(norm(dn)); List<String> p=e==null?null:e.a.get("userPassword"); if(p==null||!p.contains(pw)) code=49; } else if(!pw.isEmpty()) code=49;
        send(out,tlv(0x30,msgid,result(0x61,code,"",code==49?"invalid creds":""))); break; }
      case 0x42: log("UNBIND"); out.close(); break;
      case 0x63: { String base=str(body,o.get(0)); int scope=intv(body,o.get(1)); int deref=intv(body,o.get(2)); int size=intv(body,o.get(3)); int time=intv(body,o.get(4)); boolean typesOnly=body[o.get(5).start]!=0; String filt=filterStr(body,o.get(6)); List<String> want=new ArrayList<>(); for(R a:kids(body,o.get(7))) want.add(str(body,a));
        log("SEARCH base='"+base+"' scope="+scope+" deref="+deref+" size="+size+" time="+time+" typesOnly="+typesOnly+" filter="+filt+" attrs="+want+ctrls);
        E be=db.get(norm(base)); if(be==null&&!base.isEmpty()){ send(out,tlv(0x30,msgid,result(0x65,32,"","no such"))); break; }
        int sent=0; for(E e:db.values()){ String nd=norm(e.dn); String nb=norm(base); boolean in=false; if(scope==0) in=nd.equals(nb); else if(scope==1) in=nd.endsWith(","+nb)&&nd.indexOf(',',0)==nd.length()-nb.length()-1 || (nb.isEmpty()&&!nd.contains(",")); else in=nd.equals(nb)||nd.endsWith(","+nb)||nb.isEmpty();
          if(scope==1&&nd.endsWith(","+nb)){ String rest=nd.substring(0,nd.length()-nb.length()-1); in=!rest.contains(","); }
          if(!in||!match(filt,e)) continue; if(size>0&&sent>=size){ send(out,tlv(0x30,msgid,result(0x65,4,"","size"))); sent=-1; break; }
          ByteArrayOutputStream attrs=new ByteArrayOutputStream(); for(Map.Entry<String,List<String>> a:e.a.entrySet()){ if(!(want.isEmpty()||want.contains("*")||want.stream().anyMatch(w->w.equalsIgnoreCase(a.getKey())))) continue; ByteArrayOutputStream vals=new ByteArrayOutputStream(); if(!typesOnly) for(String v:a.getValue()) vals.writeBytes(os(v)); attrs.writeBytes(tlv(0x30,os(a.getKey()),tlv(0x31,vals.toByteArray()))); }
          send(out,tlv(0x30,msgid,tlv(0x64,os(e.dn),tlv(0x30,attrs.toByteArray())))); sent++; }
        if(sent>=0) send(out,tlv(0x30,msgid,result(0x65,0,"",""))); break; }
      case 0x68: { String dn=str(body,o.get(0)); log("ADD dn='"+dn+"'"+ctrls); E e=new E(); e.dn=dn; StringBuilder sb=new StringBuilder(); for(R a:kids(body,o.get(1))){ List<R> ak=kids(body,a); String name=str(body,ak.get(0)); List<String> vs=new ArrayList<>(); for(R v:kids(body,ak.get(1))) vs.add(str(body,v)); e.a.put(name,vs); sb.append(name+"="+vs+";"); } { String[] parts=sb.toString().split(";"); Arrays.sort(parts); log("  attrs "+String.join(";",parts)); }
        if(db.containsKey(norm(dn))) send(out,tlv(0x30,msgid,result(0x69,68,"","exists"))); else { db.put(norm(dn),e); send(out,tlv(0x30,msgid,result(0x69,0,"",""))); } break; }
      case 0x4a: { String dn=str(body,root.start==0?new R(0,op.start,op.end):new R(0,0,0)); log("DELETE dn='"+dn+"'"+ctrls); if(db.remove(norm(dn))!=null) send(out,tlv(0x30,msgid,result(0x6b,0,"",""))); else send(out,tlv(0x30,msgid,result(0x6b,32,"","nf"))); break; }
      case 0x66: { String dn=str(body,o.get(0)); log("MODIFY dn='"+dn+"'"+ctrls); E e=db.get(norm(dn)); StringBuilder sb=new StringBuilder(); for(R ch:kids(body,o.get(1))){ List<R> c=kids(body,ch); int opn=intv(body,c.get(0)); List<R> pa=kids(body,c.get(1)); String name=str(body,pa.get(0)); List<String> vs=new ArrayList<>(); for(R v:kids(body,pa.get(1))) vs.add(str(body,v)); sb.append("op"+opn+" "+name+"="+vs+";"); if(e!=null){ if(opn==0) e.a.computeIfAbsent(name,x->new ArrayList<>()).addAll(vs); else if(opn==1){ if(vs.isEmpty()) e.a.remove(name); else if(e.a.containsKey(name)) e.a.get(name).removeAll(vs);} else e.a.put(name,vs);} } log("  mods "+sb); send(out,tlv(0x30,msgid,result(0x67,e==null?32:0,"",""))); break; }
      case 0x6c: { String dn=str(body,o.get(0)); String nrdn=str(body,o.get(1)); boolean del=body[o.get(2).start]!=0; log("MODDN dn='"+dn+"' newrdn='"+nrdn+"' delold="+del+(o.size()>3?" newsup='"+str(body,o.get(3))+"'":"")+ctrls); E e=db.remove(norm(dn)); if(e==null){send(out,tlv(0x30,msgid,result(0x6d,32,"","nf")));break;} String parent=o.size()>3?str(body,o.get(3)):dn.substring(dn.indexOf(',')+1); e.dn=nrdn+","+parent; db.put(norm(e.dn),e); send(out,tlv(0x30,msgid,result(0x6d,0,"",""))); break; }
      case 0x6e: { String dn=str(body,o.get(0)); String at=str(body,kids(body,o.get(1)).get(0)); String val=str(body,kids(body,o.get(1)).get(1)); log("COMPARE dn='"+dn+"' "+at+"="+val); E e=db.get(norm(dn)); int code=e==null?32:(e.a.getOrDefault(at,List.of()).contains(val)?6:5); send(out,tlv(0x30,msgid,result(0x6f,code,"",""))); break; }
      default: log("OP "+Integer.toHexString(tag)); send(out,tlv(0x30,msgid,result(0x65,2,"","unsupported")));
    } }
  static String filterStr(byte[] b,R f){ List<R> k=kids(b,new R(0,f.start,f.end)); int t=f.tag&0xff; switch(t){
    case 0xa0: case 0xa1: { StringBuilder s=new StringBuilder("("+(t==0xa0?"&":"|")); for(R c:kids(b,f)) s.append(filterStr(b,c)); return s+")"; }
    case 0xa2: return "(!"+filterStr(b,kids(b,f).get(0))+")";
    case 0xa3: return "("+str(b,k.get(0))+"="+str(b,k.get(1))+")";
    case 0x87: return "("+str(b,f)+"=*)";
    case 0xa4: { StringBuilder s=new StringBuilder("("+str(b,k.get(0))+"="); boolean first=true; for(R x:kids(b,k.get(1))){ int xt=x.tag&0xff; if(xt==0x80) s.append(str(b,x)+"*"); else if(xt==0x81) s.append((first?"*":"")+str(b,x)+"*"); else s.append("*"+str(b,x)); first=false;} return s+")"; }
    case 0xa5: return "("+str(b,k.get(0))+">="+str(b,k.get(1))+")";
    case 0xa6: return "("+str(b,k.get(0))+"<="+str(b,k.get(1))+")";
    case 0xa8: return "("+str(b,k.get(0))+"~="+str(b,k.get(1))+")";
    default: return "(?"+t+")"; } }
  static boolean match(String f,E e){ // evaluate canonical string filter
    f=f.trim(); if(!f.startsWith("(")) return true; String in=f.substring(1,f.length()-1);
    if(in.startsWith("&")||in.startsWith("|")){ boolean and=in.startsWith("&"); for(String sub:split(in.substring(1))){ boolean m=match(sub,e); if(and&&!m) return false; if(!and&&m) return true; } return and; }
    if(in.startsWith("!")) return !match(in.substring(1),e);
    int op=in.indexOf('='); String name=in.substring(0,op); String rel="="; if(name.endsWith(">")||name.endsWith("<")||name.endsWith("~")){ rel=name.substring(name.length()-1)+"="; name=name.substring(0,name.length()-1);} String val=in.substring(op+1);
    List<String> vals=null; for(Map.Entry<String,List<String>> a:e.a.entrySet()) if(a.getKey().equalsIgnoreCase(name)) vals=a.getValue(); if(vals==null) return false;
    if(val.equals("*")) return true; if(val.contains("*")){ StringBuilder re=new StringBuilder(); for(String part:val.toLowerCase().split("\\*",-1)){ if(re.length()>0) re.append(".*"); re.append(java.util.regex.Pattern.quote(part)); } for(String v:vals) if(v.toLowerCase().matches(re.toString())) return true; return false; }
    for(String v:vals){ int c=v.compareToIgnoreCase(val); if(rel.equals("=")&&c==0) return true; if(rel.equals(">=")&&c>=0) return true; if(rel.equals("<=")&&c<=0) return true; if(rel.equals("~=")&&c==0) return true; } return false; }
  static List<String> split(String s){ List<String> out=new ArrayList<>(); int d=0,st=0; for(int i=0;i<s.length();i++){ char c=s.charAt(i); if(c=='(')d++; else if(c==')'){d--; if(d==0){out.add(s.substring(st,i+1)); st=i+1;}} } return out; }
}
