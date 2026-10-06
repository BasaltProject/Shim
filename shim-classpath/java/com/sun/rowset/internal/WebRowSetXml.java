/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset.internal;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.WebRowSet;
import com.sun.rowset.CachedRowSetImpl;

final class WebRowSetXml {
  private WebRowSetXml() {
  }

  static final class Node {
    final String name;
    final StringBuilder text = new StringBuilder();
    final List<Node> children = new ArrayList<Node>();
    boolean isNull;

    Node(String name) {
      this.name = name;
    }

    Node child(String n) {
      for (Node c : children) {
        if (c.name.equals(n)) {
          return c;
        }
      }
      return null;
    }

    String textOf(String n) {
      Node c = child(n);
      if (c == null || c.child("null") != null) {
        return null;
      }
      return c.text.toString();
    }
  }

  static String escape(String s) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '<':
          b.append("&lt;");
          break;
        case '>':
          b.append("&gt;");
          break;
        case '&':
          b.append("&amp;");
          break;
        case '"':
          b.append("&quot;");
          break;
        case '\'':
          b.append("&apos;");
          break;
        default:
          b.append(c);
      }
    }
    return b.toString();
  }

  static String unescape(String s) {
    if (s.indexOf('&') < 0) {
      return s;
    }
    return s.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&");
  }

  static Node parse(Reader r) throws SQLException {
    try {
      StringBuilder all = new StringBuilder();
      char[] buf = new char[4096];
      int n;
      while ((n = r.read(buf)) > 0) {
        all.append(buf, 0, n);
      }
      String s = all.toString();
      int i = 0;
      Node root = null;
      java.util.ArrayDeque<Node> stack = new java.util.ArrayDeque<Node>();
      while (i < s.length()) {
        int lt = s.indexOf('<', i);
        if (lt < 0) {
          break;
        }
        if (!stack.isEmpty() && lt > i) {
          stack.peek().text.append(unescape(s.substring(i, lt)));
        }
        if (s.startsWith("<?", lt)) {
          i = s.indexOf("?>", lt) + 2;
        } else if (s.startsWith("<!--", lt)) {
          i = s.indexOf("-->", lt) + 3;
        } else if (s.startsWith("</", lt)) {
          int gt = s.indexOf('>', lt);
          stack.pop();
          i = gt + 1;
        } else {
          int gt = s.indexOf('>', lt);
          String tag = s.substring(lt + 1, gt);
          boolean selfClose = tag.endsWith("/");
          if (selfClose) {
            tag = tag.substring(0, tag.length() - 1);
          }
          int sp = tag.indexOf(' ');
          String name = (sp < 0 ? tag : tag.substring(0, sp)).trim();
          Node node = new Node(name);
          if (stack.isEmpty()) {
            root = node;
          } else {
            stack.peek().children.add(node);
          }
          if (!selfClose) {
            stack.push(node);
          }
          i = gt + 1;
        }
      }
      if (root == null) {
        throw new SQLException("Invalid WebRowSet XML document");
      }
      return root;
    } catch (IOException e) {
      throw new SQLException(e.getMessage());
    } catch (RuntimeException e) {
      throw new SQLException("Invalid WebRowSet XML document: " + e);
    }
  }

  static void write(WebRowSet rs, Writer w) throws SQLException {
    try {
      CachedRowSetImpl crs = (CachedRowSetImpl) rs;
      RowSetMetaDataImpl md = (RowSetMetaDataImpl) crs.getMetaData();
      w.write("<?xml version=\"1.0\"?>\n");
      w.write("<webRowSet xmlns=\"http://java.sun.com/xml/ns/jdbc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/jdbc http://java.sun.com/xml/ns/jdbc/webrowset.xsd\">\n");
      w.write("  <properties>\n");
      prop(w, "command", crs.getCommand());
      prop(w, "concurrency", Integer.toString(crs.getConcurrency()));
      prop(w, "datasource", crs.getDataSourceName());
      prop(w, "escape-processing", Boolean.toString(crs.getEscapeProcessing()));
      prop(w, "fetch-direction", Integer.toString(crs.getFetchDirection()));
      prop(w, "fetch-size", Integer.toString(crs.getFetchSize()));
      prop(w, "isolation-level", Integer.toString(crs.getTransactionIsolation()));
      w.write("    <key-columns>\n");
      for (int k : crs.getKeyColumns()) {
        w.write("      <column>" + k + "</column>\n");
      }
      w.write("    </key-columns>\n");
      w.write("    <map>\n");
      w.write("    </map>\n");
      prop(w, "max-field-size", Integer.toString(crs.getMaxFieldSize()));
      prop(w, "max-rows", Integer.toString(crs.getMaxRows()));
      prop(w, "query-timeout", Integer.toString(crs.getQueryTimeout()));
      prop(w, "read-only", Boolean.toString(crs.isReadOnly()));
      prop(w, "rowset-type", typeName(crs.getType()));
      prop(w, "show-deleted", Boolean.toString(crs.getShowDeleted()));
      prop(w, "table-name", crs.getTableName());
      prop(w, "url", crs.getUrl());
      w.write("    <sync-provider>\n");
      w.write("      <sync-provider-name>" + crs.getSyncProvider().getProviderID() + "</sync-provider-name>\n");
      w.write("      <sync-provider-vendor>" + crs.getSyncProvider().getVendor() + "</sync-provider-vendor>\n");
      w.write("      <sync-provider-version>" + crs.getSyncProvider().getVersion() + "</sync-provider-version>\n");
      w.write("      <sync-provider-grade>" + crs.getSyncProvider().getProviderGrade() + "</sync-provider-grade>\n");
      w.write("      <data-source-lock>" + crs.getSyncProvider().getDataSourceLock() + "</data-source-lock>\n");
      w.write("    </sync-provider>\n");
      w.write("  </properties>\n");
      int n = md == null ? 0 : md.getColumnCount();
      w.write("  <metadata>\n");
      w.write("    <column-count>" + n + "</column-count>\n");
      for (int i = 1; i <= n; i++) {
        w.write("    <column-definition>\n");
        w.write("      <column-index>" + i + "</column-index>\n");
        w.write("      <auto-increment>" + md.isAutoIncrement(i) + "</auto-increment>\n");
        w.write("      <case-sensitive>" + md.isCaseSensitive(i) + "</case-sensitive>\n");
        w.write("      <currency>" + md.isCurrency(i) + "</currency>\n");
        w.write("      <nullable>" + md.isNullable(i) + "</nullable>\n");
        w.write("      <signed>" + md.isSigned(i) + "</signed>\n");
        w.write("      <searchable>" + md.isSearchable(i) + "</searchable>\n");
        w.write("      <column-display-size>" + md.getColumnDisplaySize(i) + "</column-display-size>\n");
        w.write("      <column-label>" + escape(String.valueOf(md.getColumnLabel(i))) + "</column-label>\n");
        w.write("      <column-name>" + escape(String.valueOf(md.getColumnName(i))) + "</column-name>\n");
        w.write("      <schema-name>" + escape(md.getSchemaName(i)) + "</schema-name>\n");
        w.write("      <column-precision>" + md.getPrecision(i) + "</column-precision>\n");
        w.write("      <column-scale>" + md.getScale(i) + "</column-scale>\n");
        w.write("      <table-name>" + escape(md.getTableName(i)) + "</table-name>\n");
        w.write("      <catalog-name>" + escape(md.getCatalogName(i)) + "</catalog-name>\n");
        w.write("      <column-type>" + md.getColumnType(i) + "</column-type>\n");
        w.write("      <column-type-name>" + escape(String.valueOf(md.getColumnTypeName(i))) + "</column-type-name>\n");
        w.write("    </column-definition>\n");
      }
      w.write("  </metadata>\n");
      w.write("  <data>\n");
      for (CachedRowSetImpl.Row r : crs.rowsView()) {
        if (r.deleted && r.inserted) {
          continue;
        }
        String tag = r.inserted ? "insertRow" : r.deleted ? "deleteRow" : r.updated ? "modifyRow" : "currentRow";
        w.write("    <" + tag + ">\n");
        for (int i = 0; i < n; i++) {
          Object v = (r.deleted || r.updated) && !r.inserted ? r.orig[i] : r.cur[i];
          w.write("      <columnValue>" + value(v) + "</columnValue>\n");
          if (r.updated && !r.inserted && !r.deleted && r.changed.get(i)) {
            w.write("      <updateValue>" + value(r.cur[i]) + "</updateValue>\n");
          }
        }
        w.write("    </" + tag + ">\n");
      }
      w.write("  </data>\n");
      w.write("</webRowSet>\n");
      w.flush();
    } catch (IOException e) {
      throw new SQLException(e.getMessage());
    }
  }

  private static String value(Object v) {
    if (v == null) {
      return "<null/>";
    }
    if (v instanceof byte[]) {
      byte[] b = (byte[]) v;
      StringBuilder sb = new StringBuilder();
      for (byte x : b) {
        sb.append(Character.forDigit((x >> 4) & 0xf, 16)).append(Character.forDigit(x & 0xf, 16));
      }
      return sb.toString();
    }
    return escape(v.toString());
  }

  private static void prop(Writer w, String name, String value) throws IOException {
    if (value == null) {
      w.write("    <" + name + "><null/></" + name + ">\n");
    } else {
      w.write("    <" + name + ">" + escape(value) + "</" + name + ">\n");
    }
  }

  private static String typeName(int t) {
    switch (t) {
      case java.sql.ResultSet.TYPE_FORWARD_ONLY:
        return "ResultSet.TYPE_FORWARD_ONLY";
      case java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE:
        return "ResultSet.TYPE_SCROLL_INSENSITIVE";
      default:
        return "ResultSet.TYPE_SCROLL_SENSITIVE";
    }
  }

  static Object parseValue(String text, int sqlType) throws SQLException {
    try {
      switch (sqlType) {
        case Types.BIT:
        case Types.BOOLEAN:
          return Boolean.valueOf(text);
        case Types.TINYINT:
          return Byte.valueOf(text);
        case Types.SMALLINT:
          return Short.valueOf(text);
        case Types.INTEGER:
          return Integer.valueOf(text);
        case Types.BIGINT:
          return Long.valueOf(text);
        case Types.REAL:
          return Float.valueOf(text);
        case Types.FLOAT:
        case Types.DOUBLE:
          return Double.valueOf(text);
        case Types.NUMERIC:
        case Types.DECIMAL:
          return new java.math.BigDecimal(text);
        case Types.DATE:
          return java.sql.Date.valueOf(text);
        case Types.TIME:
          return java.sql.Time.valueOf(text);
        case Types.TIMESTAMP:
          return java.sql.Timestamp.valueOf(text);
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
          byte[] b = new byte[text.length() / 2];
          for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(text.substring(2 * i, 2 * i + 2), 16);
          }
          return b;
        default:
          return text;
      }
    } catch (RuntimeException e) {
      throw new SQLException("Invalid value in WebRowSet XML: " + text);
    }
  }

  static void read(WebRowSet rs, Reader r) throws SQLException {
    Node root = parse(r);
    CachedRowSetImpl crs = (CachedRowSetImpl) rs;
    Node props = root.child("properties");
    Node meta = root.child("metadata");
    Node data = root.child("data");
    if (props != null) {
      String v = props.textOf("command");
      if (v != null && v.length() > 0) {
        crs.setCommand(v);
      }
      v = props.textOf("concurrency");
      if (v != null) {
        crs.setConcurrency(Integer.parseInt(v));
      }
      v = props.textOf("datasource");
      if (v != null && v.length() > 0) {
        crs.setDataSourceName(v);
      }
      v = props.textOf("escape-processing");
      if (v != null) {
        crs.setEscapeProcessing(Boolean.parseBoolean(v));
      }
      v = props.textOf("fetch-direction");
      if (v != null) {
        crs.setFetchDirection(Integer.parseInt(v));
      }
      v = props.textOf("isolation-level");
      if (v != null) {
        crs.setTransactionIsolation(Integer.parseInt(v));
      }
      v = props.textOf("max-field-size");
      if (v != null) {
        crs.setMaxFieldSize(Integer.parseInt(v));
      }
      v = props.textOf("max-rows");
      if (v != null) {
        crs.setMaxRows(Integer.parseInt(v));
      }
      v = props.textOf("query-timeout");
      if (v != null) {
        crs.setQueryTimeout(Integer.parseInt(v));
      }
      v = props.textOf("read-only");
      if (v != null) {
        crs.setReadOnly(Boolean.parseBoolean(v));
      }
      v = props.textOf("show-deleted");
      if (v != null) {
        crs.setShowDeleted(Boolean.parseBoolean(v));
      }
      v = props.textOf("table-name");
      if (v != null && v.length() > 0) {
        crs.setTableName(v);
      }
      v = props.textOf("url");
      if (v != null && v.length() > 0) {
        crs.setUrl(v);
      }
      Node keys = props.child("key-columns");
      if (keys != null && !keys.children.isEmpty()) {
        int[] k = new int[keys.children.size()];
        for (int i = 0; i < k.length; i++) {
          k[i] = Integer.parseInt(keys.children.get(i).text.toString().trim());
        }
        crs.setKeyColumns(k);
      }
    }
    RowSetMetaDataImpl md = new RowSetMetaDataImpl();
    if (meta != null) {
      md.setColumnCount(Integer.parseInt(meta.textOf("column-count").trim()));
      for (Node cd : meta.children) {
        if (!cd.name.equals("column-definition")) {
          continue;
        }
        int i = Integer.parseInt(cd.textOf("column-index").trim());
        md.setAutoIncrement(i, Boolean.parseBoolean(cd.textOf("auto-increment")));
        md.setCaseSensitive(i, Boolean.parseBoolean(cd.textOf("case-sensitive")));
        md.setCurrency(i, Boolean.parseBoolean(cd.textOf("currency")));
        md.setNullable(i, Integer.parseInt(cd.textOf("nullable").trim()));
        md.setSigned(i, Boolean.parseBoolean(cd.textOf("signed")));
        md.setSearchable(i, Boolean.parseBoolean(cd.textOf("searchable")));
        md.setColumnDisplaySize(i, Integer.parseInt(cd.textOf("column-display-size").trim()));
        md.setColumnLabel(i, cd.textOf("column-label"));
        md.setColumnName(i, cd.textOf("column-name"));
        md.setSchemaName(i, cd.textOf("schema-name"));
        md.setPrecision(i, Integer.parseInt(cd.textOf("column-precision").trim()));
        md.setScale(i, Integer.parseInt(cd.textOf("column-scale").trim()));
        md.setTableName(i, cd.textOf("table-name"));
        md.setCatalogName(i, cd.textOf("catalog-name"));
        md.setColumnType(i, Integer.parseInt(cd.textOf("column-type").trim()));
        md.setColumnTypeName(i, cd.textOf("column-type-name"));
      }
    }
    crs.setMetaData(md);
    List<Object[]> rowsValues = new ArrayList<Object[]>();
    List<String> kinds = new ArrayList<String>();
    List<Object[]> updates = new ArrayList<Object[]>();
    int cols = md.getColumnCount();
    if (data != null) {
      for (Node row : data.children) {
        Object[] vals = new Object[cols];
        Object[] upd = new Object[cols];
        boolean[] hasUpd = new boolean[cols];
        int ci = 0;
        int ui = -1;
        for (Node c : row.children) {
          if (c.name.equals("columnValue")) {
            ui = ci;
            vals[ci] = c.child("null") != null ? null : parseValue(c.text.toString(), md.getColumnType(ci + 1));
            ci++;
          } else if (c.name.equals("updateValue") && ui >= 0) {
            upd[ui] = c.child("null") != null ? null : parseValue(c.text.toString(), md.getColumnType(ui + 1));
            hasUpd[ui] = true;
          }
        }
        rowsValues.add(vals);
        kinds.add(row.name);
        Object[] u = new Object[cols + 1];
        u[0] = hasUpd;
        System.arraycopy(upd, 0, u, 1, cols);
        updates.add(u);
      }
    }
    crs.loadRows(rowsValues, kinds, updates);
  }
}
