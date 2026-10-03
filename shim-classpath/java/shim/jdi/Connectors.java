/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdi;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Connectors {
  private Connectors() {
  }

  static final Transport SOCKET = new Transport() {
    public String name() {
      return "dt_socket";
    }
  };

  public static final class Arg
      implements Connector.StringArgument, Connector.IntegerArgument, Connector.BooleanArgument {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String label;
    private final String description;
    private final boolean must;
    private final int kind;
    private String value;

    Arg(String name, String label, String description, String def, boolean must, int kind) {
      this.name = name;
      this.label = label;
      this.description = description;
      this.value = def;
      this.must = must;
      this.kind = kind;
    }

    public String name() {
      return name;
    }

    public String label() {
      return label;
    }

    public String description() {
      return description;
    }

    public boolean mustSpecify() {
      return must;
    }

    public String value() {
      return value;
    }

    public void setValue(String v) {
      if (!isValid(v)) {
        throw new IllegalArgumentException("Illegal argument value: " + v);
      }
      value = v;
    }

    public boolean isValid(String v) {
      if (v == null) {
        return false;
      }
      if (kind == 1) {
        try {
          Integer.parseInt(v);
        } catch (NumberFormatException e) {
          return false;
        }
      }
      if (kind == 2) {
        return v.equals("true") || v.equals("false");
      }
      return true;
    }

    public int intValue() {
      return value == null || value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    public boolean isValid(int i) {
      return i >= min() && i <= max();
    }

    public int max() {
      return Integer.MAX_VALUE;
    }

    public int min() {
      return Integer.MIN_VALUE;
    }

    public void setValue(int i) {
      value = String.valueOf(i);
    }

    public String stringValueOf(int i) {
      return String.valueOf(i);
    }

    public boolean booleanValue() {
      return Boolean.parseBoolean(value);
    }

    public void setValue(boolean b) {
      value = String.valueOf(b);
    }

    public String stringValueOf(boolean b) {
      return String.valueOf(b);
    }

    public String toString() {
      return value;
    }
  }

  private static String str(Map<String, ? extends Connector.Argument> args, String name)
      throws IllegalConnectorArgumentsException {
    Connector.Argument a = args.get(name);
    if (a == null) {
      throw new IllegalConnectorArgumentsException("Argument missing", name);
    }
    return a.value();
  }

  public static final class SocketAttach implements AttachingConnector {
    public String name() {
      return "com.sun.jdi.SocketAttach";
    }

    public String description() {
      return "Attaches by socket to other VMs";
    }

    public Transport transport() {
      return SOCKET;
    }

    public Map<String, Connector.Argument> defaultArguments() {
      Map<String, Connector.Argument> m = new LinkedHashMap<String, Connector.Argument>();
      m.put("hostname", new Arg("hostname", "Host", "Name or IP address of the host where the target VM is", "",
          false, 0));
      m.put("port", new Arg("port", "Port", "Port number where the target VM is listening", "", true, 1));
      m.put("timeout", new Arg("timeout", "Timeout", "Timeout while waiting to attach", "0", false, 1));
      return m;
    }

    public VirtualMachine attach(Map<String, ? extends Connector.Argument> args)
        throws IOException, IllegalConnectorArgumentsException {
      String host = str(args, "hostname");
      String port = str(args, "port");
      String to = args.containsKey("timeout") ? args.get("timeout").value() : "0";
      if (port == null || port.isEmpty()) {
        throw new IllegalConnectorArgumentsException("Argument is required", "port");
      }
      Socket s = new Socket();
      s.connect(new InetSocketAddress(host == null || host.isEmpty() ? "localhost" : host, Integer.parseInt(port)),
          to == null || to.isEmpty() ? 0 : Integer.parseInt(to));
      Wire w = new Wire(s.getInputStream(), s.getOutputStream());
      return new Vm(w).machine();
    }
  }

  public static final class CommandLineLaunch implements LaunchingConnector {
    public String name() {
      return "com.sun.jdi.CommandLineLaunch";
    }

    public String description() {
      return "Launches target VM as a child process";
    }

    public Transport transport() {
      return SOCKET;
    }

    public Map<String, Connector.Argument> defaultArguments() {
      Map<String, Connector.Argument> m = new LinkedHashMap<String, Connector.Argument>();
      m.put("home", new Arg("home", "Home", "Home directory of the runtime", "", false, 0));
      m.put("options", new Arg("options", "Options", "Launched VM options", "", false, 0));
      m.put("main", new Arg("main", "Main", "Main class and arguments", "", true, 0));
      m.put("suspend", new Arg("suspend", "Suspend", "All threads will be suspended before execution of main",
          "true", false, 2));
      m.put("quote", new Arg("quote", "Quote", "Character used to combine arguments", "\"", true, 0));
      m.put("vmexec", new Arg("vmexec", "Launcher", "Name of the runtime launcher executable", "java", true, 0));
      return m;
    }

    public VirtualMachine launch(Map<String, ? extends Connector.Argument> args) throws IOException,
        IllegalConnectorArgumentsException, com.sun.jdi.connect.VMStartException {
      String main = str(args, "main");
      if (main == null || main.isEmpty()) {
        throw new IllegalConnectorArgumentsException("Argument is required", "main");
      }
      String home = str(args, "home");
      String exec = str(args, "vmexec");
      String cmd = null;
      if (home != null && !home.isEmpty()) {
        cmd = home + java.io.File.separator + "bin" + java.io.File.separator + (exec == null || exec.isEmpty()
            ? "java" : exec);
      } else {
        java.util.Optional<String> self = ProcessHandle.current().info().command();
        cmd = self.isPresent() ? self.get() : "java";
      }
      boolean suspend = !"false".equals(args.containsKey("suspend") ? args.get("suspend").value() : "true");
      List<String> command = new ArrayList<String>();
      command.add(cmd);
      command.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=" + (suspend ? "y" : "n") + ",address=0");
      String opts = str(args, "options");
      if (opts != null && !opts.trim().isEmpty()) {
        for (String o : opts.trim().split("\\s+")) {
          command.add(o);
        }
      }
      for (String m : main.trim().split("\\s+")) {
        command.add(m);
      }
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.redirectErrorStream(false);
      final Process p = pb.start();
      java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getErrorStream()));
      String line;
      int port = -1;
      while ((line = r.readLine()) != null) {
        int i = line.indexOf("Listening for transport dt_socket at address:");
        if (i >= 0) {
          port = Integer.parseInt(line.substring(line.lastIndexOf(':') + 1).trim());
          break;
        }
      }
      if (port < 0) {
        p.destroy();
        throw new com.sun.jdi.connect.VMStartException("VM did not report a debugging address", p);
      }
      Thread drain = new Thread(new Runnable() {
        public void run() {
          try {
            java.io.InputStream in = p.getErrorStream();
            byte[] buf = new byte[1024];
            while (in.read(buf) >= 0) {
              // discard
            }
          } catch (IOException e) {
            // process ended
          }
        }
      });
      drain.setDaemon(true);
      drain.start();
      Socket s = new Socket("localhost", port);
      Wire w = new Wire(s.getInputStream(), s.getOutputStream());
      Vm vm = new Vm(w);
      vm.process = p;
      return vm.machine();
    }
  }

  public static final class SocketListen implements ListeningConnector {
    private ServerSocket server;

    public String name() {
      return "com.sun.jdi.SocketListen";
    }

    public String description() {
      return "Accepts socket connections initiated by other VMs";
    }

    public Transport transport() {
      return SOCKET;
    }

    public boolean supportsMultipleConnections() {
      return true;
    }

    public Map<String, Connector.Argument> defaultArguments() {
      Map<String, Connector.Argument> m = new LinkedHashMap<String, Connector.Argument>();
      m.put("port", new Arg("port", "Port", "Port number at which to listen for VM connections", "", false, 1));
      m.put("timeout", new Arg("timeout", "Timeout", "Timeout while waiting to accept a connection", "0", false,
          1));
      m.put("localAddress", new Arg("localAddress", "Local address",
          "Local address that the listener binds to", "", false, 0));
      return m;
    }

    public synchronized String startListening(Map<String, ? extends Connector.Argument> args)
        throws IOException, IllegalConnectorArgumentsException {
      String port = args.containsKey("port") ? args.get("port").value() : "";
      String la = args.containsKey("localAddress") ? args.get("localAddress").value() : "";
      int p = port == null || port.isEmpty() ? 0 : Integer.parseInt(port);
      server = la == null || la.isEmpty() ? new ServerSocket(p)
          : new ServerSocket(p, 50, java.net.InetAddress.getByName(la));
      return (la == null || la.isEmpty() ? java.net.InetAddress.getLocalHost().getHostName() : la) + ":"
          + server.getLocalPort();
    }

    public synchronized void stopListening(Map<String, ? extends Connector.Argument> args) throws IOException,
        IllegalConnectorArgumentsException {
      if (server != null) {
        server.close();
        server = null;
      }
    }

    public VirtualMachine accept(Map<String, ? extends Connector.Argument> args)
        throws IOException, IllegalConnectorArgumentsException {
      ServerSocket ss;
      synchronized (this) {
        ss = server;
      }
      if (ss == null) {
        startListening(args);
        ss = server;
      }
      String to = args.containsKey("timeout") ? args.get("timeout").value() : "0";
      if (to != null && !to.isEmpty()) {
        ss.setSoTimeout(Integer.parseInt(to));
      }
      Socket s;
      try {
        s = ss.accept();
      } catch (java.net.SocketTimeoutException e) {
        throw new TransportTimeoutException("Accept timed out");
      }
      Wire w = new Wire(s.getInputStream(), s.getOutputStream());
      return new Vm(w).machine();
    }
  }

  public static final class Manager implements com.sun.jdi.VirtualMachineManager {
    private final List<AttachingConnector> attaching = new ArrayList<AttachingConnector>();
    private final List<ListeningConnector> listening = new ArrayList<ListeningConnector>();
    private final List<LaunchingConnector> launching = new ArrayList<LaunchingConnector>();

    public Manager() {
      launching.add(new CommandLineLaunch());
      attaching.add(new SocketAttach());
      listening.add(new SocketListen());
    }

    public List<AttachingConnector> attachingConnectors() {
      return java.util.Collections.unmodifiableList(attaching);
    }

    public List<ListeningConnector> listeningConnectors() {
      return java.util.Collections.unmodifiableList(listening);
    }

    public List<LaunchingConnector> launchingConnectors() {
      return java.util.Collections.unmodifiableList(launching);
    }

    public List<Connector> allConnectors() {
      List<Connector> l = new ArrayList<Connector>(launching);
      l.addAll(attaching);
      l.addAll(listening);
      return l;
    }

    public LaunchingConnector defaultConnector() {
      return launching.get(0);
    }

    public List<VirtualMachine> connectedVirtualMachines() {
      return new ArrayList<VirtualMachine>();
    }

    public VirtualMachine createVirtualMachine(com.sun.jdi.connect.spi.Connection c, Process p)
        throws IOException {
      throw new UnsupportedOperationException();
    }

    public VirtualMachine createVirtualMachine(com.sun.jdi.connect.spi.Connection c) throws IOException {
      throw new UnsupportedOperationException();
    }

    public int majorInterfaceVersion() {
      return 25;
    }

    public int minorInterfaceVersion() {
      return 0;
    }
  }
}
