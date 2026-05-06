/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.MarshalException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnexpectedException;
import java.rmi.UnmarshalException;
import java.rmi.server.ObjID;
import java.rmi.server.Operation;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;
import java.rmi.server.UID;
import sun.rmi.transport.TCPTransport;

public class UnicastRef implements RemoteRef {
  private static final long serialVersionUID = 8258372400816541186L;

  protected String host;
  protected int port;
  protected ObjID objID;
  protected RMIClientSocketFactory csf;
  protected boolean local;
  protected transient boolean useNewFormat;

  public UnicastRef() {
  }

  public UnicastRef(String host, int port, ObjID objID, RMIClientSocketFactory csf) {
    this.host = host;
    this.port = port;
    this.objID = objID;
    this.csf = csf;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public ObjID getObjID() {
    return objID;
  }

  public RMIClientSocketFactory getClientSocketFactory() {
    return csf;
  }

  public void setLocal(boolean local) {
    this.local = local;
  }

  public Object invoke(Remote obj, Method method, Object[] params, long opnum) throws Exception {
    Socket socket = null;
    boolean needsAck = false;
    try {
      try {
        socket = csf != null ? csf.createSocket(host, port) : RMISocketFactory.getSocketFactory().createSocket(host, port);
      } catch (java.net.UnknownHostException e) {
        throw new java.rmi.UnknownHostException("Unknown host: " + host, e);
      } catch (java.net.ConnectException e) {
        throw new ConnectException("Connection refused to host: " + host, e);
      } catch (IOException e) {
        throw new ConnectIOException("Exception creating connection to: " + host, e);
      }
      java.io.DataOutputStream dout = new java.io.DataOutputStream(new java.io.BufferedOutputStream(socket.getOutputStream()));
      java.io.DataInputStream din = new java.io.DataInputStream(new java.io.BufferedInputStream(socket.getInputStream()));
      try {
        TCPTransport.clientHandshake(dout, din, socket);
      } catch (IOException e) {
        throw new ConnectIOException("error during JRMP connection establishment", e);
      }
      try {
        dout.writeByte(TCPTransport.Call);
        sun.rmi.server.MarshalOutputStream out = new sun.rmi.server.MarshalOutputStream(dout);
        objID.write(out);
        out.writeInt(-1);
        out.writeLong(opnum);
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
          marshalValue(types[i], params[i], out);
        }
        out.flush();
        dout.flush();
      } catch (IOException e) {
        throw new MarshalException("error marshalling arguments", e);
      }
      int op;
      try {
        op = din.readByte();
      } catch (IOException e) {
        throw new UnmarshalException("error unmarshalling return header", e);
      }
      if (op != TCPTransport.Return) {
        throw new UnmarshalException("Transport return code invalid");
      }
      UID ackUid;
      Object result = null;
      Object thrownObject = null;
      boolean exceptional = false;
      try {
        sun.rmi.server.MarshalInputStream in = new sun.rmi.server.MarshalInputStream(din);
        int rtype = in.readByte();
        ackUid = UID.read(in);
        if (rtype == TCPTransport.NormalReturn) {
          result = unmarshalValue(method.getReturnType(), in);
        } else if (rtype == TCPTransport.ExceptionalReturn) {
          thrownObject = in.readObject();
          exceptional = true;
        } else {
          throw new UnmarshalException("Return type invalid");
        }
      } catch (IOException e) {
        throw new UnmarshalException("error unmarshalling return", e);
      } catch (ClassNotFoundException e) {
        throw new UnmarshalException("error unmarshalling return", e);
      }
      if (RefReadState.take()) {
        sendAck(dout, ackUid);
      }
      if (!exceptional) {
        return result;
      }
      if (thrownObject instanceof Exception) {
        Exception e = (Exception) thrownObject;
        if (!(e instanceof RuntimeException) && !declared(method, e)) {
          throw new UnexpectedException("unexpected exception", e);
        }
        throw e;
      } else if (thrownObject instanceof Error) {
        throw (Error) thrownObject;
      }
      throw new UnmarshalException("Return value class not found", new ClassCastException(String.valueOf(thrownObject)));
    } finally {
      RefReadState.take();
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException e) {
        }
      }
    }
  }

  private static void sendAck(java.io.DataOutputStream dout, UID uid) {
    try {
      dout.writeByte(TCPTransport.DGCAck);
      uid.write(dout);
      dout.flush();
    } catch (IOException e) {
    }
  }

  private static boolean declared(Method m, Exception e) {
    for (Class<?> c : m.getExceptionTypes()) {
      if (c.isInstance(e)) {
        return true;
      }
    }
    return false;
  }

  public static void marshalValue(Class<?> type, Object value, java.io.ObjectOutput out) throws IOException {
    if (type.isPrimitive()) {
      if (type == int.class) {
        out.writeInt(((Integer) value).intValue());
      } else if (type == boolean.class) {
        out.writeBoolean(((Boolean) value).booleanValue());
      } else if (type == byte.class) {
        out.writeByte(((Byte) value).byteValue());
      } else if (type == char.class) {
        out.writeChar(((Character) value).charValue());
      } else if (type == short.class) {
        out.writeShort(((Short) value).shortValue());
      } else if (type == long.class) {
        out.writeLong(((Long) value).longValue());
      } else if (type == float.class) {
        out.writeFloat(((Float) value).floatValue());
      } else if (type == double.class) {
        out.writeDouble(((Double) value).doubleValue());
      } else {
        throw new Error("Unrecognized primitive type: " + type);
      }
    } else {
      out.writeObject(value);
    }
  }

  public static Object unmarshalValue(Class<?> type, java.io.ObjectInput in) throws IOException, ClassNotFoundException {
    if (type.isPrimitive()) {
      if (type == int.class) {
        return Integer.valueOf(in.readInt());
      } else if (type == boolean.class) {
        return Boolean.valueOf(in.readBoolean());
      } else if (type == byte.class) {
        return Byte.valueOf(in.readByte());
      } else if (type == char.class) {
        return Character.valueOf(in.readChar());
      } else if (type == short.class) {
        return Short.valueOf(in.readShort());
      } else if (type == long.class) {
        return Long.valueOf(in.readLong());
      } else if (type == float.class) {
        return Float.valueOf(in.readFloat());
      } else if (type == double.class) {
        return Double.valueOf(in.readDouble());
      } else if (type == void.class) {
        return null;
      } else {
        throw new Error("Unrecognized primitive type: " + type);
      }
    } else {
      return in.readObject();
    }
  }

  public RemoteCall newCall(RemoteObject obj, Operation[] ops, int opnum, long hash) throws RemoteException {
    return new OldStyleCall(this, opnum, hash);
  }

  public void invoke(RemoteCall call) throws Exception {
    ((OldStyleCall) call).executeCall();
  }

  public void done(RemoteCall call) throws RemoteException {
    try {
      call.done();
    } catch (IOException e) {
      throw new RemoteException("done failed", e);
    }
  }

  public String getRefClass(ObjectOutput out) {
    return "UnicastRef";
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(host);
    out.writeInt(port);
    objID.write(out);
    out.writeBoolean(false);
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    host = in.readUTF();
    port = in.readInt();
    objID = ObjID.read(in);
    boolean resultStream = in.readBoolean();
    if (resultStream) {
      RefReadState.mark();
    }
  }

  public String remoteToString() {
    return Util.unqualifiedName(getClass()) + " [liveRef: [endpoint:[" + host + ":" + port + "](" + (local ? "local" : "remote")
        + "),objID:" + objID + "]]";
  }

  public int remoteHashCode() {
    return objID == null ? super.hashCode() : objID.hashCode();
  }

  public boolean remoteEquals(RemoteRef ref) {
    if (ref instanceof UnicastRef) {
      UnicastRef other = (UnicastRef) ref;
      return port == other.port && host.equals(other.host) && objID.equals(other.objID);
    }
    return false;
  }

  public boolean equals(Object obj) {
    return obj instanceof UnicastRef && remoteEquals((UnicastRef) obj);
  }

  public int hashCode() {
    return remoteHashCode();
  }

  public String toString() {
    return remoteToString();
  }

  static final class RefReadState {
    private static final ThreadLocal<Boolean> needsAck = new ThreadLocal<Boolean>();

    static void mark() {
      needsAck.set(Boolean.TRUE);
    }

    static boolean take() {
      Boolean b = needsAck.get();
      needsAck.remove();
      return b != null && b.booleanValue();
    }
  }
}
