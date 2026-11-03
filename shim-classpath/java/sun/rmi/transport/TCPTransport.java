/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.transport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerError;
import java.rmi.ServerException;
import java.rmi.UnmarshalException;
import java.rmi.server.ExportException;
import java.rmi.server.ObjID;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UID;
import java.util.HashMap;
import java.util.Map;
import sun.rmi.server.MarshalInputStream;
import sun.rmi.server.MarshalOutputStream;
import sun.rmi.server.UnicastRef;

public final class TCPTransport {
  public static final int Magic = 0x4a524d49;
  public static final int Version = 2;
  public static final byte StreamProtocol = 0x4b;
  public static final byte SingleOpProtocol = 0x4c;
  public static final byte MultiplexProtocol = 0x4d;
  public static final byte ProtocolAck = 0x4e;
  public static final byte ProtocolNack = 0x4f;
  public static final byte Call = 0x50;
  public static final byte Return = 0x51;
  public static final byte Ping = 0x52;
  public static final byte PingAck = 0x53;
  public static final byte DGCAck = 0x54;
  public static final int NormalReturn = 1;
  public static final int ExceptionalReturn = 2;

  private TCPTransport() {
  }

  private static final class Listener {
    final ServerSocket socket;
    final Object key;
    int refs;
    volatile boolean closed;

    Listener(ServerSocket socket, Object key) {
      this.socket = socket;
      this.key = key;
    }
  }

  private static final Map<Object, Listener> listeners = new HashMap<Object, Listener>();
  private static final Map<Integer, Listener> byPort = new HashMap<Integer, Listener>();
  private static final ThreadLocal<String> clientHost = new ThreadLocal<String>();
  private static int connectionCount = 0;

  public static String currentClientHost() {
    return clientHost.get();
  }

  public static String localHost() {
    String h = System.getProperty("java.rmi.server.hostname");
    if (h != null && h.length() > 0) {
      return h;
    }
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      return "127.0.0.1";
    }
  }

  public static synchronized int listen(int port, RMIServerSocketFactory ssf, Object[] keyOut) throws RemoteException {
    Object key = ssf != null ? (Object) ssf : (Object) Integer.valueOf(port);
    Listener l = listeners.get(key);
    if (l == null && port != 0 && ssf == null) {
      l = byPort.get(Integer.valueOf(port));
    }
    if (l != null) {
      l.refs++;
      keyOut[0] = l.key;
      return l.socket.getLocalPort();
    }
    ServerSocket ss;
    try {
      ss = ssf != null ? ssf.createServerSocket(port) : RMISocketFactory.getSocketFactory().createServerSocket(port);
    } catch (IOException e) {
      throw new ExportException("Port already in use: " + port, e);
    }
    l = new Listener(ss, key);
    l.refs = 1;
    listeners.put(key, l);
    byPort.put(Integer.valueOf(ss.getLocalPort()), l);
    if (port == 0 && ssf == null) {
      listeners.put(Integer.valueOf(ss.getLocalPort()), l);
    }
    keyOut[0] = l.key;
    final Listener fl = l;
    Thread t = new Thread(new Runnable() {
      public void run() {
        acceptLoop(fl);
      }
    }, "RMI TCP Accept-" + ss.getLocalPort());
    t.setDaemon(false);
    t.start();
    return ss.getLocalPort();
  }

  public static synchronized void release(int port, Object key) {
    Listener l = key == null ? null : listeners.get(key);
    if (l == null && port != 0) {
      l = byPort.get(Integer.valueOf(port));
    }
    if (l == null) {
      return;
    }
    if (--l.refs <= 0) {
      l.closed = true;
      listeners.values().remove(l);
      byPort.values().remove(l);
      try {
        l.socket.close();
      } catch (IOException e) {
      }
    }
  }

  private static void acceptLoop(Listener l) {
    while (!l.closed) {
      try {
        final Socket s = l.socket.accept();
        int n;
        synchronized (TCPTransport.class) {
          n = ++connectionCount;
        }
        Thread t = new Thread(new Runnable() {
          public void run() {
            handleConnection(s);
          }
        }, "RMI TCP Connection(" + n + ")-" + s.getInetAddress().getHostAddress());
        t.setDaemon(true);
        t.start();
      } catch (IOException e) {
        if (l.closed) {
          return;
        }
        try {
          Thread.sleep(10);
        } catch (InterruptedException ie) {
          return;
        }
      }
    }
  }

  public static void clientHandshake(DataOutputStream dout, DataInputStream din, Socket socket) throws IOException {
    dout.writeInt(Magic);
    dout.writeShort(Version);
    dout.writeByte(StreamProtocol);
    dout.flush();
    int ack = din.readByte();
    if (ack != ProtocolAck) {
      throw new IOException("Unsupported protocol acknowledgement " + ack);
    }
    din.readUTF();
    din.readInt();
    dout.writeUTF(socket.getLocalAddress().getHostAddress());
    dout.writeInt(socket.getLocalPort());
    dout.flush();
  }

  private static void handleConnection(Socket socket) {
    try {
      socket.setTcpNoDelay(true);
      DataInputStream din = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
      DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
      int magic = din.readInt();
      int version = din.readShort();
      if (magic != Magic || version != Version) {
        return;
      }
      int protocol = din.readByte();
      String remoteHost = socket.getInetAddress().getHostAddress();
      boolean single = false;
      if (protocol == StreamProtocol) {
        dout.writeByte(ProtocolAck);
        dout.writeUTF(remoteHost);
        dout.writeInt(socket.getPort());
        dout.flush();
        din.readUTF();
        din.readInt();
      } else if (protocol == SingleOpProtocol) {
        single = true;
      } else {
        dout.writeByte(ProtocolNack);
        dout.flush();
        return;
      }
      clientHost.set(remoteHost);
      do {
        int op = din.read();
        if (op < 0) {
          return;
        }
        switch (op) {
          case Call:
            if (!serviceCall(din, dout)) {
              return;
            }
            break;
          case Ping:
            dout.writeByte(PingAck);
            dout.flush();
            break;
          case DGCAck:
            UID.read(din);
            break;
          default:
            return;
        }
      } while (!single);
    } catch (IOException e) {
    } finally {
      clientHost.remove();
      try {
        socket.close();
      } catch (IOException e) {
      }
    }
  }

  private static boolean serviceCall(DataInputStream din, DataOutputStream dout) throws IOException {
    MarshalInputStream in;
    ObjID id;
    int opnum;
    long hash;
    try {
      in = new MarshalInputStream(din);
      id = ObjID.read(in);
      opnum = in.readInt();
      hash = in.readLong();
    } catch (IOException e) {
      return false;
    }
    ObjectTable.Target target = ObjectTable.get(id);
    Object result = null;
    Throwable thrown = null;
    Class<?> returnType = void.class;
    try {
      if (target == null) {
        throw new NoSuchObjectException("no such object in table");
      }
      if (opnum >= 0 && target.oldDispatch != null) {
        result = target.oldDispatch.dispatch(target.impl, opnum, hash, in);
        returnType = result == ObjectTable.OldDispatch.VOID ? void.class : Object.class;
      } else {
        Method m = target.methods.get(Long.valueOf(hash));
        if (m == null) {
          throw new UnmarshalException("unrecognized method hash: method not supported by remote object");
        }
        Class<?>[] types = m.getParameterTypes();
        Object[] args = new Object[types.length];
        try {
          for (int i = 0; i < types.length; i++) {
            args[i] = UnicastRef.unmarshalValue(types[i], in);
          }
        } catch (IOException e) {
          throw new UnmarshalException("error unmarshalling arguments", e);
        } catch (ClassNotFoundException e) {
          throw new UnmarshalException("error unmarshalling arguments", e);
        }
        returnType = m.getReturnType();
        try {
          if (!m.isAccessible()) {
            m.setAccessible(true);
          }
          result = m.invoke(target.impl, args);
        } catch (InvocationTargetException e) {
          throw e.getCause() != null ? e.getCause() : e;
        } catch (IllegalAccessException e) {
          throw new ServerException("method not accessible", e);
        }
      }
    } catch (Throwable t) {
      thrown = t;
    }
    byte[] reply;
    try {
      reply = buildReply(thrown, result, returnType);
    } catch (IOException e) {
      reply = buildReply(new MarshalException("error marshalling return", e), null, void.class);
    }
    dout.writeByte(Return);
    dout.write(reply, 0, reply.length);
    dout.flush();
    return true;
  }

  private static byte[] buildReply(Throwable thrown, Object result, Class<?> returnType) throws IOException {
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    MarshalOutputStream out = new MarshalOutputStream(bo);
    if (thrown != null) {
      Throwable e = thrown;
      if (e instanceof Error) {
        e = new ServerError("Error occurred in server thread", (Error) e);
      } else if (e instanceof RemoteException) {
        e = new ServerException("RemoteException occurred in server thread", (Exception) e);
      }
      out.writeByte(ExceptionalReturn);
      new UID().write(out);
      out.writeObject(e);
    } else {
      out.writeByte(NormalReturn);
      new UID().write(out);
      if (returnType != void.class && result != ObjectTable.OldDispatch.VOID) {
        UnicastRef.marshalValue(returnType, result, out);
      }
    }
    out.flush();
    return bo.toByteArray();
  }
}
