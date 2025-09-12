/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

public class Properties extends Hashtable {
  protected Properties defaults;

  public Properties() {
    this(null, 8);
  }

  public Properties(int initialCapacity) {
    this(null, initialCapacity);
  }

  public Properties(Properties defaults) {
    this(defaults, 8);
  }

  public Properties(Properties defaults, int initialCapacity) {
    this.defaults = defaults;
  }

  public void load(InputStream in) throws IOException {
    new InputStreamParser(in).parse(this);
  }
  
  public void load(Reader reader) throws IOException {
    new ReaderParser(reader).parse(this);
  }

  public void store(OutputStream out, String comment) throws IOException {
    PrintStream os = new PrintStream(out);
    if (comment != null) {
      os.println("#" + comment);
    }
    for (Iterator it = entrySet().iterator();
         it.hasNext();) {
      Map.Entry entry = (Map.Entry)it.next();
      os.print(entry.getKey());
      os.print('=');
      os.println(entry.getValue());
    }
    os.flush();
  }

  public void store(Writer writer, String comments) throws IOException {
    PrintWriter pw = (writer instanceof PrintWriter) ? (PrintWriter) writer : new PrintWriter(writer);
    if (comments != null) {
      pw.println("#" + comments);
    }
    for (Iterator it = entrySet().iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry) it.next();
      pw.print(entry.getKey());
      pw.print('=');
      pw.println(entry.getValue());
    }
    pw.flush();
  }

  public String getProperty(String key) {
    Object oval = super.get(key);
    String sval = (oval instanceof String) ? (String) oval : null;
    return ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval;
  }

  public String getProperty(String key, String defaultValue) {
    String val = getProperty(key);
    return (val == null) ? defaultValue : val;
  }

  public Object setProperty(String key, String value) {
    return put(key, value);
  }
  
  public Enumeration<?> propertyNames() {
    Hashtable<Object, Object> h = new Hashtable<Object, Object>();
    enumerate(h);
    return h.keys();
  }

  private void enumerate(Hashtable<Object, Object> h) {
    if (defaults != null) {
      defaults.enumerate(h);
    }
    for (Iterator it = entrySet().iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry) it.next();
      h.put(entry.getKey(), entry.getValue());
    }
  }

  public Set<String> stringPropertyNames() {
    Map<String, String> h = new HashMap<String, String>();
    enumerateStringProperties(h);
    return h.keySet();
  }

  private void enumerateStringProperties(Map<String, String> h) {
    if (defaults != null) {
      defaults.enumerateStringProperties(h);
    }
    for (Iterator it = entrySet().iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry) it.next();
      Object k = entry.getKey();
      Object v = entry.getValue();
      if (k instanceof String && v instanceof String) {
        h.put((String) k, (String) v);
      }
    }
  }

  private abstract static class Parser {
    private StringBuilder key = null;
    private StringBuilder value = null;
    private StringBuilder current = null;

    private void append(int c) {
      if (current == null) {
        if (key == null) {
          current = key = new StringBuilder();
        } else {
          current = value = new StringBuilder();
        }
      }

      current.append((char) c);
    }

    private void finishLine(Map<String, Object> map) {
      if (key != null) {
        map.put(key.toString(),
                (value == null ? "" : value.toString().trim()));
      }

      key = value = current = null;
    }

    abstract int readCharacter() throws IOException;

    char readUtf16() throws IOException {
      char c = 0;
      for (int i = 0; i < 4; ++i) {
        int digit = Character.digit((char)readCharacter(), 16);
        if (digit == -1) throw new IOException("Invalid Unicode escape encountered.");
        c <<= 4;
        c |= digit;
      }   
      return c;
    }

    void parse(Map map)
      throws IOException
    {
      boolean escaped = false;

      int c;
      while ((c = readCharacter()) != -1) {
        if (c == '\\') {
          if (escaped) {
            escaped = false;
            append(c);
          } else {
            escaped = true;
          }
        } else {
          switch (c) {
          case '#':
          case '!':
            if (key == null) {
              while ((c = readCharacter()) != -1 && c != '\n');
            } else {
              append(c);
            }
            break;

          case ' ':
          case '\r':
          case '\t':
            if (escaped || (current != null && value == current)) {
              append(c);
            } else if (key == current) {
              current = null;
            }
            break;

          case ':':
          case '=':
            if (escaped || (current != null && value == current)) {
              append(c);
            } else {
              if (key == null) {
                key = new StringBuilder();
              }
              current = null;
            }
            break;

          case '\n':
            if (escaped) {
              append(c);
            } else {
              finishLine(map);          
            }
            break;
          case 'n':
            if (escaped) {
              append('\n');
            } else {
              append(c);
            }
            break;

          case 'u':
            if (escaped) {
              append(readUtf16());
            } else {
              append(c);
            } break;

          default:
            append(c);
            break;
          }
        
          escaped = false;
        }
      }

      finishLine(map);
    }
  }
  
  static class InputStreamParser extends Parser {
    InputStream in;
    
    
    public InputStreamParser(InputStream in) {
      this.in = in;
    }

    @Override
    int readCharacter() throws IOException {
      return in.read();
    }
  }

  static class ReaderParser extends Parser {
    Reader in;
    
    public ReaderParser(Reader in) {
      this.in = in;
    }

    @Override
    int readCharacter() throws IOException {
      return in.read();
    }
  }

}
