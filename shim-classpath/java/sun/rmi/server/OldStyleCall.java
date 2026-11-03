/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.UnexpectedException;
import java.rmi.UnmarshalException;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.RemoteCall;
import java.rmi.server.UID;
import sun.rmi.transport.TCPTransport;

final class OldStyleCall implements RemoteCall {
  private final UnicastRef ref;
  private final int opnum;
  private final long hash;
  private Socket socket;
  private DataOutputStream dout;
  private DataInputStream din;
  private MarshalOutputStream out;
  private MarshalInputStream in;

  OldStyleCall(UnicastRef ref, int opnum, long hash) throws RemoteException {
    this.ref = ref;
    this.opnum = opnum;
    this.hash = hash;
  }

  public ObjectOutput getOutputStream() throws IOException {
    if (out == null) {
      try {
        socket = ref.getClientSocketFactory() != null
            ? ref.getClientSocketFactory().createSocket(ref.getHost(), ref.getPort())
            : RMISocketFactory.getSocketFactory().createSocket(ref.getHost(), ref.getPort());
      } catch (java.net.UnknownHostException e) {
        throw new java.rmi.UnknownHostException("Unknown host: " + ref.getHost(), e);
      } catch (java.net.ConnectException e) {
        throw new java.rmi.ConnectException("Connection refused to host: " + ref.getHost(), e);
      } catch (IOException e) {
        throw new java.rmi.ConnectIOException("Exception creating connection to: " + ref.getHost(), e);
      }
      dout = new DataOutputStream(new java.io.BufferedOutputStream(socket.getOutputStream()));
      din = new DataInputStream(new java.io.BufferedInputStream(socket.getInputStream()));
      TCPTransport.clientHandshake(dout, din, socket);
      dout.writeByte(TCPTransport.Call);
      out = new MarshalOutputStream(dout);
      ref.getObjID().write(out);
      out.writeInt(opnum);
      out.writeLong(hash);
    }
    return out;
  }

  public void releaseOutputStream() throws IOException {
    if (out != null) {
      out.flush();
      dout.flush();
    }
  }

  public ObjectInput getInputStream() throws IOException {
    return in;
  }

  public void releaseInputStream() throws IOException {
  }

  public ObjectOutput getResultStream(boolean success) throws IOException, StreamCorruptedException {
    throw new StreamCorruptedException("server side only");
  }

  public void executeCall() throws Exception {
    getOutputStream();
    releaseOutputStream();
    int op = din.readByte();
    if (op != TCPTransport.Return) {
      throw new UnmarshalException("Transport return code invalid");
    }
    in = new MarshalInputStream(din);
    int rtype = in.readByte();
    UID.read(in);
    if (rtype == TCPTransport.ExceptionalReturn) {
      Object ex = in.readObject();
      if (ex instanceof Exception) {
        throw (Exception) ex;
      } else if (ex instanceof Error) {
        throw (Error) ex;
      }
      throw new UnexpectedException("Return value class not found");
    } else if (rtype != TCPTransport.NormalReturn) {
      throw new UnmarshalException("Return type invalid");
    }
  }

  public void done() throws IOException {
    if (socket != null) {
      socket.close();
      socket = null;
    }
  }
}
