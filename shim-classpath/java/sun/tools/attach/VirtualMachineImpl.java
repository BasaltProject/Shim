/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.tools.attach;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.AttachOperationFailedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.spi.AttachProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class VirtualMachineImpl extends VirtualMachine {
  private static final String PROTOCOL_VERSION = "1";
  private static final int ATTACH_ERROR_BADVERSION = 101;
  private static final int JNI_ENOMEM = -4;
  private static final int JNI_EINVAL = -6;

  private final String pid;
  private final String socketPath;

  VirtualMachineImpl(AttachProvider provider, String vmid) throws AttachNotSupportedException, IOException {
    super(provider, vmid);
    this.pid = vmid;
    if (!new File("/proc/" + vmid).exists()) {
      throw new IOException("non existent JVM pid: " + vmid);
    }
    File socket = new File(tmpdir(), ".java_pid" + vmid);
    if (!socket.exists()) {
      File attachFile = new File("/proc/" + vmid + "/cwd/.attach_pid" + vmid);
      boolean created = false;
      try {
        created = attachFile.createNewFile();
      } catch (IOException e) {
        created = false;
      }
      if (!created) {
        attachFile = new File(tmpdir(), ".attach_pid" + vmid);
        try {
          attachFile.createNewFile();
        } catch (IOException e) {
          throw new AttachNotSupportedException("Unable to open attach file: " + e.getMessage());
        }
      }
      try {
        sendQuitTo(vmid);
        int attempts = 0;
        long delay = 200;
        long timeout = Long.getLong("sun.tools.attach.attachTimeout", 5000L);
        long elapsed = 0;
        while (!socket.exists() && elapsed < timeout) {
          try {
            Thread.sleep(delay);
          } catch (InterruptedException x) {
            break;
          }
          elapsed += delay;
          attempts++;
          if (!new File("/proc/" + vmid).exists()) {
            break;
          }
          delay = Math.min(delay + 100, 500);
        }
        if (!socket.exists()) {
          throw new AttachNotSupportedException(String.format(
              "Unable to open socket file %s: target process %s doesn't respond within %dms or HotSpot VM not loaded",
              socket.getPath(), vmid, elapsed));
        }
      } finally {
        attachFile.delete();
      }
    }
    this.socketPath = socket.getPath();
    connectAndClose();
  }

  private void connectAndClose() throws IOException {
    SocketChannel c = connect();
    c.close();
  }

  private static String tmpdir() {
    return "/tmp";
  }

  private static void sendQuitTo(String pid) throws IOException {
    Process p = new ProcessBuilder("kill", "-3", pid).redirectErrorStream(true).start();
    try {
      p.getInputStream().readAllBytes();
      p.waitFor();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private SocketChannel connect() throws IOException {
    SocketChannel c = SocketChannel.open(StandardProtocolFamily.UNIX);
    try {
      c.connect(UnixDomainSocketAddress.of(Path.of(socketPath)));
    } catch (IOException e) {
      c.close();
      throw e;
    }
    return c;
  }

  public void detach() throws IOException {
  }

  public InputStream execute(String cmd, Object... args) throws AgentLoadException, IOException {
    if (args.length > 3) {
      throw new IllegalArgumentException("Too many arguments");
    }
    ByteArrayOutputStream req = new ByteArrayOutputStream();
    req.write(PROTOCOL_VERSION.getBytes(StandardCharsets.UTF_8));
    req.write(0);
    req.write(cmd.getBytes(StandardCharsets.UTF_8));
    req.write(0);
    for (int i = 0; i < 3; i++) {
      if (i < args.length && args[i] != null) {
        req.write(((String) args[i]).getBytes(StandardCharsets.UTF_8));
      }
      req.write(0);
    }
    SocketChannel ch = connect();
    ByteArrayOutputStream resp = new ByteArrayOutputStream();
    try {
      ByteBuffer out = ByteBuffer.wrap(req.toByteArray());
      while (out.hasRemaining()) {
        ch.write(out);
      }
      ByteBuffer buf = ByteBuffer.allocate(8192);
      while (ch.read(buf) >= 0) {
        buf.flip();
        resp.write(buf.array(), 0, buf.limit());
        buf.clear();
      }
    } finally {
      ch.close();
    }
    byte[] data = resp.toByteArray();
    int nl = 0;
    while (nl < data.length && data[nl] != '\n') {
      nl++;
    }
    int status;
    try {
      status = Integer.parseInt(new String(data, 0, nl, StandardCharsets.UTF_8).trim());
    } catch (NumberFormatException e) {
      throw new IOException("Command failed in target VM");
    }
    int bodyStart = Math.min(nl + 1, data.length);
    if (status != 0) {
      String message = new String(data, bodyStart, data.length - bodyStart, StandardCharsets.UTF_8).trim();
      if (status == ATTACH_ERROR_BADVERSION) {
        throw new IOException("Protocol mismatch with target VM");
      }
      if (cmd.equals("load")) {
        String msg = "Failed to load agent library";
        if (!message.isEmpty()) {
          msg += ": " + message;
        }
        throw new AgentLoadException(msg);
      }
      if (message.isEmpty()) {
        message = "Command failed in target VM";
      }
      throw new AttachOperationFailedException(message);
    }
    return new ByteArrayInputStream(data, bodyStart, data.length - bodyStart);
  }

  private static String readAll(InputStream in) throws IOException {
    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
  }

  public Properties getSystemProperties() throws IOException {
    Properties props = new Properties();
    try (InputStream in = executeQuiet("properties")) {
      props.load(in);
    }
    return props;
  }

  public Properties getAgentProperties() throws IOException {
    Properties props = new Properties();
    try (InputStream in = executeQuiet("agentProperties")) {
      props.load(in);
    }
    return props;
  }

  private InputStream executeQuiet(String cmd, Object... args) throws IOException {
    try {
      return execute(cmd, args);
    } catch (AgentLoadException e) {
      throw new InternalError("Should not get here", e);
    }
  }

  public void loadAgentLibrary(String agentLibrary, String options) throws AgentLoadException,
      AgentInitializationException, IOException {
    loadAgentLibrary(agentLibrary, false, options);
  }

  public void loadAgentPath(String agentLibrary, String options) throws AgentLoadException,
      AgentInitializationException, IOException {
    loadAgentLibrary(agentLibrary, true, options);
  }

  private void loadAgentLibrary(String agentLibrary, boolean isAbsolute, String options) throws AgentLoadException,
      AgentInitializationException, IOException {
    if (agentLibrary == null) {
      throw new NullPointerException("agentLibrary cannot be null");
    }
    String msgPrefix = "return code: ";
    InputStream in = execute("load", agentLibrary, isAbsolute ? "true" : "false", options);
    try {
      String result = readAll(in).trim();
      if (result.isEmpty()) {
        throw new AgentLoadException("Target VM did not respond");
      } else if (result.startsWith(msgPrefix)) {
        int retCode = Integer.parseInt(result.substring(msgPrefix.length()));
        if (retCode != 0) {
          throw new AgentInitializationException("Agent_OnAttach failed", retCode);
        }
      } else {
        throw new AgentLoadException("Failed to load agent library: " + result.replace('\n', ' '));
      }
    } finally {
      in.close();
    }
  }

  public void loadAgent(String agent, String options) throws AgentLoadException, AgentInitializationException,
      IOException {
    if (agent == null) {
      throw new NullPointerException("agent cannot be null");
    }
    String args = agent;
    if (options != null) {
      args = args + "=" + options;
    }
    try {
      loadAgentLibrary("instrument", args);
    } catch (AgentLoadException x) {
      throw new InternalError("instrument library is missing in target VM", x);
    } catch (AgentInitializationException x) {
      int rc = x.returnValue();
      switch (rc) {
        case JNI_ENOMEM:
          throw new AgentLoadException("Insuffient memory");
        case 100:
          throw new AgentLoadException("Agent JAR not found or no Agent-Class attribute");
        case 101:
          throw new AgentLoadException("Unable to add JAR file to system class path");
        case 102:
          throw new AgentLoadException("Agent JAR loaded but agent failed to initialize");
        default:
          throw new AgentLoadException("Failed to load agent - unknown reason: " + rc);
      }
    }
  }

  public void startManagementAgent(Properties agentProperties) throws IOException {
    if (agentProperties == null) {
      throw new NullPointerException("agentProperties cannot be null");
    }
    StringBuilder cmd = new StringBuilder("ManagementAgent.start");
    for (String key : agentProperties.stringPropertyNames()) {
      cmd.append(' ').append(key).append('=').append(agentProperties.getProperty(key));
    }
    try (InputStream in = executeQuiet("jcmd", cmd.toString())) {
      String r = readAll(in);
      if (r.contains("Error") || r.contains("Exception")) {
        throw new IOException(r.trim());
      }
    }
  }

  public String startLocalManagementAgent() throws IOException {
    String key = "com.sun.management.jmxremote.localConnectorAddress";
    Properties agentProps = getAgentProperties();
    String address = (String) agentProps.get(key);
    if (address != null) {
      return address;
    }
    try (InputStream in = executeQuiet("jcmd", "ManagementAgent.start_local")) {
      readAll(in);
    }
    agentProps = getAgentProperties();
    address = (String) agentProps.get(key);
    if (address == null) {
      throw new IOException("Fails to find connector address");
    }
    return address;
  }
}
