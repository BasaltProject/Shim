/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.net.*; import jdk.net.*; import java.nio.channels.*;
public class ExtendedSocketOptionsModern { public static void main(String[] a) throws Exception {
 try (ServerSocket ss=new ServerSocket(0); Socket s=new Socket("127.0.0.1",ss.getLocalPort())) {
  System.out.println(s.supportedOptions().stream().map(Object::toString).sorted().toList());
  System.out.println(ss.supportedOptions().stream().map(Object::toString).sorted().toList());
  try { s.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, 77); System.out.println(s.getOption(ExtendedSocketOptions.TCP_KEEPIDLE)); } catch(Throwable e){ System.out.println("E "+e); }
  try { s.setOption(ExtendedSocketOptions.TCP_KEEPINTERVAL, 11); System.out.println(s.getOption(ExtendedSocketOptions.TCP_KEEPINTERVAL)); } catch(Throwable e){ System.out.println("E "+e); }
  try { s.setOption(ExtendedSocketOptions.TCP_KEEPCOUNT, 5); System.out.println(s.getOption(ExtendedSocketOptions.TCP_KEEPCOUNT)); } catch(Throwable e){ System.out.println("E "+e); }
  try { s.setOption(ExtendedSocketOptions.TCP_QUICKACK, true); System.out.println(s.getOption(ExtendedSocketOptions.TCP_QUICKACK)); } catch(Throwable e){ System.out.println("E "+e); }
  try { System.out.println(s.getOption(ExtendedSocketOptions.SO_INCOMING_NAPI_ID)); } catch(Throwable e){ System.out.println("E "+e); }
  try { s.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, -1); } catch(Throwable e){ System.out.println("E "+e); }
  try { s.setOption(StandardSocketOptions.TCP_NODELAY, true); System.out.println(s.getOption(StandardSocketOptions.TCP_NODELAY)); s.setOption(StandardSocketOptions.SO_LINGER,5); System.out.println(s.getOption(StandardSocketOptions.SO_LINGER)); System.out.println(s.getOption(StandardSocketOptions.IP_TOS)); } catch(Throwable e){ System.out.println("E "+e); }
  SocketChannel c=SocketChannel.open(new InetSocketAddress("127.0.0.1",ss.getLocalPort()));
  System.out.println(c.supportedOptions().stream().map(Object::toString).sorted().toList());
  try { c.setOption(ExtendedSocketOptions.TCP_KEEPCOUNT, 9); System.out.println(c.getOption(ExtendedSocketOptions.TCP_KEEPCOUNT)); } catch(Throwable e){ System.out.println("E "+e); }
  DatagramSocket d=new DatagramSocket(); System.out.println(d.supportedOptions().stream().map(Object::toString).sorted().toList());
  try { System.out.println(Sockets.class.getName()); } catch(Throwable e){}
 }}}
