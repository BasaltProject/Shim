/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*; import java.lang.reflect.*; import java.sql.*; import java.util.*; import java.sql.Date; import java.time.*; import javax.sql.*;
public class SqlModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { o = String.valueOf(b.get()); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + t.getMessage(); } System.out.println(n + "=" + o); }
  @SuppressWarnings("unchecked")
  static <T> T proxy(Class<T> c) { return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[]{c}, (pr, m, args) -> { if (m.isDefault()) return InvocationHandler.invokeDefault(pr, m, args); if (m.getName().equals("toString")) return "proxy"; if (m.getName().equals("hashCode")) return 1; if (m.getName().equals("equals")) return pr == args[0]; throw new UnsupportedOperationException("abstract " + m.getName()); }); }
  static Object dflt(Class<?> t) { if (t == int.class) return 0; if (t == long.class) return 0L; if (t == boolean.class) return false; if (t == String.class) return "x"; if (t == String[].class) return new String[0]; if (t == int[].class) return new int[0]; if (t == Class.class) return Object.class; if (t == Object.class) return "o"; if (t == SQLType.class) return JDBCType.INTEGER; if (t == ShardingKey.class) return null; return null; }
  static void defaults(Class<?> c) throws Exception {
    Object pr = proxy(c);
    List<Method> ms = new ArrayList<>(Arrays.asList(c.getMethods()));
    ms.sort(Comparator.comparing(m -> m.getName() + Arrays.toString(m.getParameterTypes()) + m.getDeclaringClass().getName()));
    for (Method m : ms) {
      if (!m.isDefault()) continue;
      Object[] args = new Object[m.getParameterCount()];
      for (int i = 0; i < args.length; i++) args[i] = dflt(m.getParameterTypes()[i]);
      String r;
      try { Object v = m.invoke(pr, args); r = String.valueOf(v); } catch (InvocationTargetException e) { r = "!" + e.getCause().getClass().getName() + ":" + e.getCause().getMessage(); }
      System.out.println("default " + c.getSimpleName() + "." + m.getName() + Arrays.toString(m.getParameterTypes()).replace("class ", "").replace("interface ", "") + "=" + r);
    }
  }
  public static void main(String[] a) throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"));
    for (Class<?> c : new Class<?>[]{CallableStatement.class, Connection.class, DatabaseMetaData.class, PreparedStatement.class, ResultSet.class, SQLInput.class, SQLOutput.class, Statement.class, DataSource.class, ConnectionPoolDataSource.class, XADataSource.class, CommonDataSource.class, javax.xml.transform.Source.class}) defaults(c);
    Statement st = proxy(Statement.class);
    for (String s : new String[]{"it's", "", "a''b", "x\\y"}) { String s2 = s; p("lit " + s, () -> st.enquoteLiteral(s2)); p("nlit " + s, () -> st.enquoteNCharLiteral(s2)); }
    for (String s : new String[]{"abc", "a_b1", "1abc", "has space", "\"quoted\"", "", "x".repeat(128), "x".repeat(129), "a\"b", "é", "a-b", "\"a\"\"b\""}) { String s2 = s; p("ident " + s2.length() + s2.substring(0, Math.min(5, s2.length())), () -> st.enquoteIdentifier(s2, false) + "|" + st.enquoteIdentifier(s2, true) + "|" + st.isSimpleIdentifier(s2)); }
    p("ident null", () -> { try { st.enquoteIdentifier(null, false); return "no"; } catch (NullPointerException e) { return "NPE"; } });
    p("lit null", () -> { try { st.enquoteLiteral(null); return "no"; } catch (NullPointerException e) { return "NPE"; } });
    p("sqlexception", () -> { SQLException e = new SQLException("r", "S1", 7); return e.getMessage() + "|" + e.getSQLState() + "|" + e.getErrorCode() + "|" + e.getNextException() + "|" + e.getCause(); });
    p("sqlexception cause", () -> { SQLException e = new SQLException(new RuntimeException("c")); return e.getMessage() + "|" + e.getSQLState() + "|" + e.getErrorCode() + "|" + e.getCause().getMessage(); });
    p("sqlexception defaults", () -> { SQLException e = new SQLException(); return e.getMessage() + "|" + e.getSQLState() + "|" + e.getErrorCode(); });
    p("sqlexception 2", () -> { SQLException e = new SQLException("r", "st"); SQLException e2 = new SQLException("r", "st", new Error("e")); SQLException e3 = new SQLException("r", "st", 5, new Error("e")); return e.getErrorCode() + "|" + e2.getCause().getMessage() + e2.getSQLState() + e2.getErrorCode() + "|" + e3.getCause().getMessage() + e3.getErrorCode(); });
    p("chain", () -> { SQLException e1 = new SQLException("1"); SQLException e2 = new SQLException("2", new RuntimeException("c2")); SQLException e3 = new SQLException("3"); e1.setNextException(e2); e1.setNextException(e3); StringBuilder sb = new StringBuilder(); for (Throwable t : e1) sb.append(t.getMessage()).append(','); sb.append(e1.getNextException().getMessage()).append(e2.getNextException().getMessage()).append(e3.getNextException()); return sb; });
    p("chain cause first", () -> { SQLException e1 = new SQLException("1", new RuntimeException("c1", new Error("c1b"))); e1.setNextException(new SQLException("2", new IllegalStateException("c2"))); StringBuilder sb = new StringBuilder(); for (Throwable t : e1) sb.append(t.getMessage()).append(','); return sb; });
    p("iterator end", () -> { Iterator<Throwable> it = new SQLException("x").iterator(); it.next(); try { it.next(); return "no"; } catch (NoSuchElementException e) { return "NSEE"; } });
    p("iterator remove", () -> { Iterator<Throwable> it = new SQLException("x").iterator(); it.remove(); return "no"; });
    p("warning", () -> { SQLWarning w = new SQLWarning("w", "01000", 3); w.setNextWarning(new SQLWarning("w2")); return w.getMessage() + w.getSQLState() + w.getErrorCode() + w.getNextWarning().getMessage() + (w.getNextWarning().getNextWarning()); });
    p("warning bad", () -> { SQLWarning w = new SQLWarning("w"); w.setNextException(new SQLException("plain")); return w.getNextWarning(); });
    p("subclasses", () -> { StringBuilder sb = new StringBuilder(); SQLException[] es = {new SQLDataException("d", "s", 1), new SQLFeatureNotSupportedException("f"), new SQLIntegrityConstraintViolationException("i", "s"), new SQLInvalidAuthorizationSpecException(), new SQLNonTransientConnectionException(new Error("c")), new SQLNonTransientException("n", new Error("e")), new SQLRecoverableException("r", "s", new Error("e")), new SQLSyntaxErrorException("s", "s", 2, new Error("e")), new SQLTimeoutException("t"), new SQLTransactionRollbackException("tr"), new SQLTransientConnectionException("tc"), new SQLTransientException("t")}; for (SQLException e : es) sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(':').append(e.getSQLState()).append(':').append(e.getErrorCode()).append(':').append(e.getCause() == null ? "-" : e.getCause().getMessage()).append(':').append(e.getClass().getSuperclass().getSimpleName()).append(';'); return sb; });
    p("batch", () -> { BatchUpdateException b = new BatchUpdateException("m", "s", 4, new int[]{1, 2, -2}); return b.getMessage() + b.getSQLState() + b.getErrorCode() + Arrays.toString(b.getUpdateCounts()) + Arrays.toString(b.getLargeUpdateCounts()); });
    p("batch long", () -> { BatchUpdateException b = new BatchUpdateException("m", "s", 4, new long[]{1, 2, 1L << 33}, new Error("c")); return Arrays.toString(b.getUpdateCounts()) + Arrays.toString(b.getLargeUpdateCounts()) + b.getCause().getMessage(); });
    p("batch defaults", () -> { BatchUpdateException b = new BatchUpdateException(); BatchUpdateException c = new BatchUpdateException(new Error("e")); BatchUpdateException d = new BatchUpdateException(new int[]{3}, new Error("e")); return b.getUpdateCounts() + "|" + b.getLargeUpdateCounts() + "|" + c.getMessage() + c.getUpdateCounts() + "|" + Arrays.toString(d.getUpdateCounts()); });
    p("batch copy", () -> { int[] x = {1, 2}; BatchUpdateException b = new BatchUpdateException(x); x[0] = 9; b.getUpdateCounts()[1] = 8; return Arrays.toString(b.getUpdateCounts()); });
    p("truncation", () -> { DataTruncation d = new DataTruncation(3, true, false, 10, 5); DataTruncation r = new DataTruncation(1, false, true, 7, 6, new Error("c")); return d.getMessage() + d.getSQLState() + d.getErrorCode() + d.getIndex() + d.getParameter() + d.getRead() + d.getDataSize() + d.getTransferSize() + "|" + r.getMessage() + r.getSQLState() + r.getCause().getMessage() + r.getRead(); });
    p("client info", () -> { Map<String, ClientInfoStatus> m = new HashMap<>(); m.put("k", ClientInfoStatus.REASON_VALUE_INVALID); SQLClientInfoException e = new SQLClientInfoException("msg", "st", 9, m, new Error("c")); return e.getMessage() + e.getSQLState() + e.getErrorCode() + e.getFailedProperties() + e.getCause().getMessage() + (e.getFailedProperties() == m) + new SQLClientInfoException().getFailedProperties(); });
    p("client info 2", () -> { Map<String, ClientInfoStatus> m = new HashMap<>(); SQLClientInfoException x1 = new SQLClientInfoException(m, new Error("e")); SQLClientInfoException x2 = new SQLClientInfoException("r", m); SQLClientInfoException x3 = new SQLClientInfoException("r", "s", m); return x1.getMessage() + "|" + x2.getMessage() + "|" + x3.getSQLState(); });
    p("enums", () -> Arrays.toString(ClientInfoStatus.values()) + Arrays.toString(PseudoColumnUsage.values()) + Arrays.toString(RowIdLifetime.values()));
    p("jdbctype", () -> { StringBuilder sb = new StringBuilder(); for (JDBCType t : JDBCType.values()) sb.append(t).append('=').append(t.getVendorTypeNumber()).append(',').append(t.getName()).append(',').append(t.getVendor()).append(';'); return sb; });
    p("jdbctype valueOf", () -> JDBCType.valueOf(Types.VARCHAR) + "|" + JDBCType.valueOf(2014) + "|" + JDBCType.valueOf("DATE") + "|" + ((SQLType) JDBCType.BIT).getName());
    p("jdbctype bad", () -> JDBCType.valueOf(12345));
    p("types", () -> Types.BIT + "," + Types.ARRAY + "," + Types.TIMESTAMP_WITH_TIMEZONE + "," + Types.REF_CURSOR + "," + Types.NVARCHAR);
    p("driver property", () -> { DriverPropertyInfo i = new DriverPropertyInfo("n", "v"); return i.name + i.value + i.description + i.required + i.choices; });
    p("permission", () -> { SQLPermission pm = new SQLPermission("setLog"); return pm.getName() + pm.getActions() + pm.implies(new SQLPermission("setLog")) + pm.implies(new SQLPermission("other")) + new SQLPermission("a.*", "x").getActions(); });
    p("date", () -> { Date d = Date.valueOf("2024-03-05"); return d + "|" + d.toLocalDate() + "|" + Date.valueOf(LocalDate.of(1999, 12, 31)) + "|" + new Date(0) + "|" + new Date(100, 1, 29) + "|" + d.getTime(); });
    p("date bad", () -> { StringBuilder sb = new StringBuilder(); for (String s : new String[]{null, "2024-3-5", "2024/03/05", "x", "2024-13-01", "2024-00-10", "2024-01-32", "2024-01-", "-01-01", "20240-01-01", "2024-01-01 10:00:00"}) { try { sb.append(Date.valueOf(s)).append(';'); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(';'); } } return sb; });
    p("date unsupported", () -> { StringBuilder sb = new StringBuilder(); Date d = new Date(0); try { d.getHours(); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } try { d.setMinutes(1); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } try { d.toInstant(); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } return sb; });
    p("time", () -> { Time t = Time.valueOf("13:05:09"); return t + "|" + t.toLocalTime() + "|" + Time.valueOf(LocalTime.of(1, 2, 3, 999)) + "|" + new Time(3600000L * 5) + "|" + new Time(25, 61, 61) + "|" + t.getTime(); });
    p("time bad", () -> { StringBuilder sb = new StringBuilder(); for (String s : new String[]{null, "1:2", "x", "12:00", "12:00:", ":1:1", "aa:bb:cc", "25:00:00", "12:00:00.5", "1:2:3"}) { try { sb.append(Time.valueOf(s)).append(';'); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(';'); } } return sb; });
    p("time unsupported", () -> { StringBuilder sb = new StringBuilder(); Time t = new Time(0); try { t.getYear(); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } try { t.getDate(); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } try { t.setMonth(1); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } try { t.toInstant(); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } try { t.getDay(); } catch (Exception e) { sb.append(e.getClass().getSimpleName()); } return sb; });
    p("timestamp", () -> { Timestamp t = Timestamp.valueOf("2024-03-05 13:05:09.123456789"); return t + "|" + t.getNanos() + "|" + t.getTime() + "|" + t.toLocalDateTime() + "|" + t.toInstant() + "|" + Timestamp.valueOf(LocalDateTime.of(1990, 1, 2, 3, 4, 5, 6000)) + "|" + new Timestamp(1709654828123L) + "|" + new Timestamp(-1L) + "|" + new Timestamp(-1500L) + "|" + Timestamp.from(Instant.ofEpochSecond(86400, 5)); });
    p("timestamp strings", () -> { StringBuilder sb = new StringBuilder(); for (String s : new String[]{"2024-03-05 13:05:09", "2024-3-5 1:2:3", "2024-03-05 13:05:09.1", "2024-03-05 13:05:09.100", "2024-03-05 13:05:09.000000001", " 2024-03-05 13:05:09.5 ", "0099-03-05 13:05:09"}) { try { sb.append(Timestamp.valueOf(s)).append(';'); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(';'); } } return sb; });
    p("timestamp bad", () -> { StringBuilder sb = new StringBuilder(); for (String s : new String[]{null, "", "x", "2024-03-05", "2024-03-05 ", "2024-03-05 13", "2024-03-05 13:05", "2024-03-05 13:05:09.", "2024-03-05 13:05:09.1234567890", "2024-03-05 13:05:09.x", "2024-13-05 13:05:09", "2024-03-32 13:05:09", "2024-03-05T13:05:09", "2024-03-05 24:00:00", "2024-03-05 13:60:00", "2024-03-05 13:05:60"}) { try { sb.append(Timestamp.valueOf(s)).append(';'); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(';'); } } return sb; });
    p("timestamp compare", () -> { Timestamp a1 = new Timestamp(1000), b1 = new Timestamp(1000); b1.setNanos(1); Timestamp c1 = new Timestamp(2000); java.util.Date d = new java.util.Date(1000); return a1.compareTo(b1) + "," + b1.compareTo(a1) + "," + a1.compareTo(c1) + "," + a1.equals(b1) + "," + a1.equals(new Timestamp(1000)) + "," + a1.before(c1) + "," + a1.after(c1) + "," + a1.compareTo(d) + "," + a1.equals((Object) d) + "," + d.equals(a1) + "," + (a1.hashCode() == new Timestamp(1000).hashCode()); });
    p("timestamp nanos", () -> { Timestamp t = new Timestamp(0); t.setNanos(999999999); StringBuilder sb = new StringBuilder(); sb.append(t.getTime()).append(',').append(t); try { t.setNanos(1000000000); } catch (Exception e) { sb.append(',').append(e.getMessage()); } try { t.setNanos(-1); } catch (Exception e) { sb.append(',').append(e.getClass().getSimpleName()); } t.setTime(12345); sb.append(',').append(t.getNanos()).append(',').append(t.getTime()); t.setTime(-12345); sb.append(',').append(t.getNanos()).append(',').append(t.getTime()); return sb; });
    p("timestamp ctor", () -> new Timestamp(124, 2, 5, 13, 5, 9, 42) + "|" + new Timestamp(124, 2, 5, 13, 5, 9, 0));
    p("timestamp bad ctor", () -> new Timestamp(124, 2, 5, 13, 5, 9, -1));
    p("timestamp from huge", () -> Timestamp.from(Instant.MAX));
    p("timestamp instant", () -> { Timestamp t = new Timestamp(1709654828123L); return t.toInstant() + "|" + Date.from(t.toInstant()).getTime() + "|" + t.toInstant().toEpochMilli(); });
    p("driver manager empty", () -> { try { DriverManager.getConnection("jdbc:none:x"); return "no"; } catch (SQLException e) { return e.getMessage() + "|" + e.getSQLState() + "|" + e.getErrorCode(); } });
    p("driver manager null", () -> { try { DriverManager.getConnection(null); return "no"; } catch (SQLException e) { return e.getMessage() + "|" + e.getSQLState(); } });
    p("driver manager getDriver", () -> { try { DriverManager.getDriver("jdbc:none:x"); return "no"; } catch (SQLException e) { return e.getMessage() + "|" + e.getSQLState(); } });
    p("driver manager timeout", () -> { DriverManager.setLoginTimeout(7); return DriverManager.getLoginTimeout(); });
    p("driver manager null register", () -> { DriverManager.registerDriver(null); return "no"; });
    p("driver manager deregister null", () -> { DriverManager.deregisterDriver(null); return "ok"; });
    class FakeConn { }
    final java.io.StringWriter log = new java.io.StringWriter();
    p("driver manager flow", () -> {
      DriverManager.setLogWriter(new java.io.PrintWriter(log));
      final int[] dereg = {0};
      Driver d = new Driver() {
        public Connection connect(String url, Properties info) throws SQLException { if (!acceptsURL(url)) return null; if (url.endsWith("fail")) throw new SQLException("boom " + info.getProperty("user") + info.getProperty("password")); return proxy(Connection.class); }
        public boolean acceptsURL(String url) { return url.startsWith("jdbc:fake:"); }
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
        public int getMajorVersion() { return 1; } public int getMinorVersion() { return 2; } public boolean jdbcCompliant() { return false; }
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }
      };
      DriverManager.registerDriver(d, () -> dereg[0]++);
      DriverManager.registerDriver(d);
      StringBuilder sb = new StringBuilder();
      sb.append(DriverManager.getConnection("jdbc:fake:ok", "u", "p") != null).append(',');
      sb.append(DriverManager.getConnection("jdbc:fake:ok") != null).append(',');
      try { DriverManager.getConnection("jdbc:fake:fail", "usr", "pw"); } catch (SQLException e) { sb.append(e.getMessage()).append(','); }
      try { DriverManager.getConnection("jdbc:other:x"); } catch (SQLException e) { sb.append(e.getMessage()).append(','); }
      sb.append(DriverManager.getDriver("jdbc:fake:z") == d).append(',');
      sb.append(Collections.list(DriverManager.getDrivers()).size()).append(',').append(DriverManager.drivers().count()).append(',');
      DriverManager.deregisterDriver(d);
      sb.append(dereg[0]).append(',').append(Collections.list(DriverManager.getDrivers()).size()).append(',');
      DriverManager.deregisterDriver(d);
      DriverManager.setLogWriter(null);
      sb.append(DriverManager.getLogWriter());
      return sb;
    });
    p("driver manager log", () -> log.toString().replaceAll("\\n\\tat [^\\n]*", "").replaceAll("SqlModern\\$[0-9]+@[0-9a-f]+", "Drv").replaceAll("SqlModern\\$[0-9]+", "Drv"));
    p("log writer exception", () -> { java.io.StringWriter w = new java.io.StringWriter(); DriverManager.setLogWriter(new java.io.PrintWriter(w)); new SQLException("logged", "S", 3); new SQLWarning("quiet"); DriverManager.setLogWriter(null); String s = w.toString(); return s.substring(0, s.indexOf('\n')) + "|" + s.contains("java.sql.SQLException: logged") + "|" + s.contains("quiet"); });
    p("log ctors", () -> { StringWriter w = new StringWriter(); DriverManager.setLogWriter(new PrintWriter(w)); Error c = new Error("c"); Runnable[] rs = { () -> new SQLException("m"), () -> new SQLException("m", "S"), () -> new SQLException("m", "S", 3), () -> new SQLException(), () -> new SQLException(new Error("c")), () -> new SQLException("m", new Error("c")), () -> new SQLException("m", "S", new Error("c")), () -> new SQLException("m", "S", 4, new Error("c")), () -> new SQLWarning(), () -> new SQLWarning("w"), () -> new SQLWarning("w", "S"), () -> new SQLWarning("w", "S", 2), () -> new SQLWarning(c), () -> new SQLWarning("w", c), () -> new SQLWarning("w", "S", c), () -> new SQLWarning("w", "S", 2, c), () -> new BatchUpdateException(new int[0]), () -> new DataTruncation(1, true, true, 1, 1) }; StringBuilder sb = new StringBuilder(); for (Runnable r : rs) { w.getBuffer().setLength(0); r.run(); sb.append(w.toString().replaceAll("\\n\\tat [^\\n]*", "").replaceAll("\\n\\t\\.\\.\\. [0-9]+ more", "").replace("\n", "|")).append(';'); } DriverManager.setLogWriter(null); return sb; });
    p("events", () -> { PooledConnection pc = proxy(PooledConnection.class); ConnectionEvent ce = new ConnectionEvent(pc, new SQLException("x")); StatementEvent se = new StatementEvent(pc, proxy(PreparedStatement.class), new SQLException("y")); RowSetEvent re = new RowSetEvent(proxy(RowSet.class)); return (ce.getSource() == pc) + "|" + ce.getSQLException().getMessage() + "|" + new ConnectionEvent(pc).getSQLException() + "|" + se.getSQLException().getMessage() + "|" + (se.getStatement() != null) + "|" + (re.getSource() != null) + "|" + new StatementEvent(pc, null).getSQLException(); });
    p("xa", () -> { javax.transaction.xa.XAException x = new javax.transaction.xa.XAException(javax.transaction.xa.XAException.XAER_NOTA); return x.errorCode + "|" + x.getMessage() + "|" + new javax.transaction.xa.XAException("m").getMessage() + "|" + javax.transaction.xa.XAException.XA_RBTIMEOUT + javax.transaction.xa.XAException.XAER_OUTSIDE; });
    p("statement consts", () -> Statement.CLOSE_CURRENT_RESULT + "," + Statement.SUCCESS_NO_INFO + "," + Statement.EXECUTE_FAILED + "," + ResultSet.TYPE_SCROLL_INSENSITIVE + "," + ResultSet.CONCUR_UPDATABLE + "," + ResultSet.CLOSE_CURSORS_AT_COMMIT + "," + Connection.TRANSACTION_SERIALIZABLE + "," + DatabaseMetaData.columnNullable + "," + ParameterMetaData.parameterModeInOut + "," + java.sql.Array.class.getSimpleName() + DatabaseMetaData.sqlStateSQL);
    p("wrapper", () -> { Wrapper w = proxy(Statement.class); return w.getClass().getInterfaces().length; });
    Runtime.getRuntime().halt(0);
  }
}
