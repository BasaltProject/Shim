/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import com.sun.net.httpserver.Authenticator;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;

public class HttpServerEdgeModern {
  static String raw(int port, String req, int bodyTimeout) throws Exception {
    try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
      s.setSoTimeout(bodyTimeout);
      s.getOutputStream().write(req.getBytes(StandardCharsets.ISO_8859_1));
      s.getOutputStream().flush();
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      try {
        int n;
        while ((n = s.getInputStream().read(buf)) > 0) {
          b.write(buf, 0, n);
        }
      } catch (SocketTimeoutException e) {
        b.write("<timeout>".getBytes());
      }
      return b.toString("ISO-8859-1").replaceAll("Date: [^\r]*", "Date: <date>").replace("\r", "\\r").replace("\n", "\\n");
    }
  }

  static void t(String n, Callable<Object> c) {
    try {
      System.out.println(n + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(n + " ! " + e.getClass().getName() + (e instanceof NullPointerException ? "" : ": " + e.getMessage()));
    }
  }

  public static void main(String[] a) throws Exception {
    headers();
    server();
    System.exit(0);
  }

  static void headers() throws Exception { 
  for (String k: new String[]{"a b","a:b","","a\rb","a\0b","é","a\tb"," a","a ","a(b)","a,b","a\u007fb"}) { String kk=k; t("key["+kk.replace("\r","\\r").replace("\0","\\0").replace("\t","\\t").replace("\u007f","DEL")+"]", ()->{ Headers h=new Headers(); h.add(kk,"v"); return h.keySet(); }); }
  for (String v: new String[]{"a\rb","a\nb","a\r\n b","a\r\nb","a\0b","é","a\tb","  padded  ","","a\u007fb","a\u0001b"}) { String vv=v; t("val["+vv.replace("\r","\\r").replace("\n","\\n").replace("\0","\\0").replace("\t","\\t").replace("\u007f","DEL").replace("\u0001","SOH")+"]", ()->{ Headers h=new Headers(); h.add("k",vv); return h.get("k"); }); }
  t("put-list-null", ()->new Headers().put("k", null)); t("put-list-with-null", ()->{ Headers h=new Headers(); h.put("k", Arrays.asList("a", null)); return h.get("k"); });
  t("put-list-bad", ()->{ Headers h=new Headers(); h.put("k", List.of("a\nb")); return h.get("k"); });
  t("put-returns-old", ()->{ Headers h=new Headers(); h.put("k", new ArrayList<>(List.of("1"))); return h.put("K", new ArrayList<>(List.of("2"))); });
  t("get-nonstring", ()->new Headers().get(5)); t("get-null", ()->new Headers().get(null)); t("containsKey-null", ()->new Headers().containsKey(null)); t("containsKey-nonstring", ()->new Headers().containsKey(5));
  t("getFirst-null", ()->new Headers().getFirst(null)); t("remove-null", ()->new Headers().remove(null)); t("set-null", ()->{ new Headers().set(null,"x"); return 1; });
  t("set-replaces", ()->{ Headers h=new Headers(); h.add("k","1"); h.add("k","2"); h.set("K","3"); return h; });
  t("add-null-value-list", ()->{ Headers h=new Headers(); h.add("k",null); h.add("k","x"); return h.get("k"); });
  t("containsValue", ()->{ Headers h=new Headers(); h.add("k","1"); return h.containsValue(List.of("1")); });
  t("values", ()->{ Headers h=new Headers(); h.add("k","1"); return h.values(); });
  t("entrySet", ()->{ Headers h=new Headers(); h.add("k","1"); return h.entrySet(); });
  t("entry-setValue", ()->{ Headers h=new Headers(); h.add("k","1"); Map.Entry<String,List<String>> e=h.entrySet().iterator().next(); e.setValue(List.of("z")); return h; });
  t("keySet-remove", ()->{ Headers h=new Headers(); h.add("k","1"); h.keySet().remove("K"); return h; });
  t("replaceAll", ()->{ Headers h=new Headers(); h.add("k","1"); h.replaceAll((k,v)->List.of("r")); return h; });
  t("replaceAll-bad", ()->{ Headers h=new Headers(); h.add("k","1"); h.replaceAll((k,v)->List.of("r\n")); return h; });
  t("copy-ctor", ()->{ Map<String,List<String>> m=new HashMap<>(); m.put("abc",List.of("1")); m.put("ABC", List.of("2")); return new TreeMap<>(new Headers(m)); });
  t("copy-ctor-bad", ()->{ Map<String,List<String>> m=new HashMap<>(); m.put("a\n",List.of("1")); return new Headers(m); });
  t("clear", ()->{ Headers h=new Headers(); h.add("k","1"); h.clear(); return h.size(); });
  t("equals-map", ()->{ Headers h=new Headers(); h.add("K","1"); return h.equals(Map.of("K", List.of("1"))); });
  t("of-null", ()->Headers.of((String[])null)); t("of-null-elem", ()->Headers.of("a",null)); t("of-dup", ()->new TreeMap<>(Headers.of("a","1","A","2")));
  t("of-empty", ()->Headers.of()); t("of-map-null", ()->Headers.of((Map<String,List<String>>)null));
  t("of-unmod-set", ()->{ Headers.of("a","1").set("a","2"); return 1; }); t("of-unmod-remove", ()->Headers.of("a","1").remove("a")); t("of-unmod-clear", ()->{ Headers.of("a","1").clear(); return 1; });
  t("of-unmod-put", ()->Headers.of("a","1").put("b", List.of("1"))); t("of-unmod-get-list-add", ()->Headers.of("a","1").get("a").add("x"));
  t("of-getFirst", ()->Headers.of("a","1","a","2").getFirst("a")+" "+Headers.of("A","1","a","2").get("a"));
  t("httpprincipal", ()->{ HttpPrincipal p=new HttpPrincipal("u","r"); return p+" "+p.getName()+" "+p.getUsername()+" "+p.getRealm()+" "+p.equals(new HttpPrincipal("u","r"))+" "+(p.hashCode()==new HttpPrincipal("u","r").hashCode()); });
  t("httpprincipal-null", ()->new HttpPrincipal(null,"r")); t("httpprincipal-null2", ()->new HttpPrincipal("u",null));
  t("basic-realm", ()->new BasicAuthenticator("r"){ public boolean checkCredentials(String u,String p){return false;} }.getRealm());
  t("basic-null", ()->new BasicAuthenticator(null){ public boolean checkCredentials(String u,String p){return false;} });
  t("basic-charset-null", ()->new BasicAuthenticator("r",null){ public boolean checkCredentials(String u,String p){return false;} });
  t("failure-code", ()->new Authenticator.Failure(403).getResponseCode()); t("failure-bad", ()->new Authenticator.Failure(99)); t("retry-bad", ()->new Authenticator.Retry(0)); t("success-null", ()->new Authenticator.Success(null));
  t("handlers-of-null", ()->HttpHandlers.of(200, null, "x")); t("handlers-of-code", ()->HttpHandlers.of(50, Headers.of(), "x")); t("handlers-of-code2", ()->HttpHandlers.of(1000, Headers.of(), "x")); t("handlers-of-body-null", ()->HttpHandlers.of(200, Headers.of(), null));
  t("handlers-orelse-null", ()->HttpHandlers.handleOrElse(null, ex->{}, ex->{}));
  t("filter-chain", ()->new Filter.Chain(List.of(), ex->{}));
  t("filter-before-null", ()->Filter.beforeHandler(null, ex->{})); t("filter-desc", ()->Filter.beforeHandler("d", ex->{}).description()+" "+Filter.afterHandler("e", ex->{}).description()+" "+Filter.adaptRequest("f", r->r).description());
  t("request-with", ()->{ Request r=new Request(){ public java.net.URI getRequestURI(){return java.net.URI.create("/x");} public String getRequestMethod(){return "GET";} public Headers getRequestHeaders(){ return Headers.of("a","1"); } }; Request w=r.with("Extra", List.of("v")); return w.getRequestHeaders()+" "+w.getRequestMethod()+" "+w.getRequestURI()+" "+r.getRequestHeaders(); });
  t("request-with-dup", ()->{ Request r=new Request(){ public java.net.URI getRequestURI(){return java.net.URI.create("/x");} public String getRequestMethod(){return "GET";} public Headers getRequestHeaders(){ return Headers.of("a","1"); } }; return r.with("A", List.of("v")); });
   }

  static void server() throws Exception { HttpServer s = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(),0),0); int port=s.getAddress().getPort();
  s.createContext("/hello", ex->{ byte[] b="hello".getBytes(); ex.sendResponseHeaders(200,b.length); try(OutputStream o=ex.getResponseBody()){o.write(b);} });
  s.createContext("/echo", ex->{ byte[] body=ex.getRequestBody().readAllBytes(); ex.sendResponseHeaders(200, body.length); try(OutputStream o=ex.getResponseBody()){o.write(body);} });
  s.createContext("/chunky", ex->{ ex.sendResponseHeaders(200,0); try(OutputStream o=ex.getResponseBody()){ o.write("abc".getBytes()); o.flush(); o.write(new byte[0]); o.write("de".getBytes()); } });
  
  s.createContext("/lowmem", ex->{ ex.sendResponseHeaders(200, 0); ex.getResponseBody().close(); });
  s.createContext("/emptychunk", ex->{ ex.sendResponseHeaders(200, 0); ex.close(); });
  s.createContext("/closeonly", ex->{ ex.close(); });
  s.createContext("/ro", ex->{ ex.getResponseHeaders().add("Content-Length","3"); ex.getResponseHeaders().add("Transfer-Encoding", "x"); ex.getResponseHeaders().add("Date","custom"); ex.getResponseHeaders().add("Connection","keep-alive"); ex.sendResponseHeaders(200, 2); ex.getResponseBody().write("ok".getBytes()); ex.close(); });
  
  
  s.createContext("/read", ex->{ InputStream in=ex.getRequestBody(); int c1=in.read(); byte[] rest=in.readAllBytes(); int eof=in.read(); in.close(); String m=c1+" "+rest.length+" "+eof+" "+in.available(); byte[] b=m.getBytes(); ex.sendResponseHeaders(200,b.length); try(OutputStream o=ex.getResponseBody()){o.write(b);} });
  s.start();
  String[][] cases={
   {"no-root-404","GET /nothing HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"two-tokens","GET /hello\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"four-tokens","GET /hello HTTP/1.1 extra\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"bad-uri","GET /he llo HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"star-uri","OPTIONS * HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"no-slash","GET hello HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"header-no-colon","GET /hello HTTP/1.1\r\nHost x\r\nConnection: close\r\n\r\n"},
   {"header-fold","GET /hello HTTP/1.1\r\nHost: x\r\nX-Fold: a\r\n b\r\nConnection: close\r\n\r\n"},
   {"header-empty-value","GET /hello HTTP/1.1\r\nHost: x\r\nX-Empty:\r\nConnection: close\r\n\r\n"},
   {"leading-crlf","\r\nGET /hello HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"http11-default-keepalive-then-close","GET /hello HTTP/1.1\r\nHost: x\r\n\r\nGET /hello HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"chunky","GET /chunky HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"lowmem","GET /lowmem HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"emptychunk","GET /emptychunk HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"closeonly","GET /closeonly HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"ro","GET /ro HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"read-fixed","POST /read HTTP/1.1\r\nHost: x\r\nContent-Length: 5\r\nConnection: close\r\n\r\nabcde"},
   {"read-chunked","POST /read HTTP/1.1\r\nHost: x\r\nTransfer-Encoding: chunked\r\nConnection: close\r\n\r\n3\r\nabc\r\n2;ext=1\r\nde\r\n0\r\nTrailer: x\r\n\r\n"},
   {"read-chunked-badsize","POST /read HTTP/1.1\r\nHost: x\r\nTransfer-Encoding: chunked\r\nConnection: close\r\n\r\nzz\r\nabc\r\n0\r\n\r\n"},
   {"read-neg-length","POST /read HTTP/1.1\r\nHost: x\r\nContent-Length: -1\r\nConnection: close\r\n\r\nabc"},
   {"te-and-cl","POST /echo HTTP/1.1\r\nHost: x\r\nContent-Length: 3\r\nTransfer-Encoding: chunked\r\nConnection: close\r\n\r\n3\r\nabc\r\n0\r\n\r\n"},
   {"te-unknown","POST /echo HTTP/1.1\r\nHost: x\r\nTransfer-Encoding: gzip\r\nConnection: close\r\n\r\n"},
   {"expect-other","POST /echo HTTP/1.1\r\nHost: x\r\nExpect: something\r\nContent-Length: 2\r\nConnection: close\r\n\r\nab"},
   {"method-trace","TRACE /hello HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"dup-content-length","POST /echo HTTP/1.1\r\nHost: x\r\nContent-Length: 2\r\nContent-Length: 3\r\nConnection: close\r\n\r\nabc"},
   {"percent-path","GET /hel%6co HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
   {"fragment","GET /hello#frag HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n"},
  };
  for (String[] c: cases) { String name=c[0]; t(name, ()->raw(port, c[1], 1500)); }
  System.exit(0);   }
}
