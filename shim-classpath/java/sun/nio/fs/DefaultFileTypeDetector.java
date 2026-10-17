/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileTypeDetector;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class DefaultFileTypeDetector {
  private DefaultFileTypeDetector() { }

  public static FileTypeDetector create() {
    return new MimeTypesFileTypeDetector();
  }

  private static final class MimeTypesFileTypeDetector extends FileTypeDetector {
    private final Map<String, String> mimeTypeMap = new HashMap<String, String>();
    private volatile boolean loaded;

    private static String getExtension(String name) {
      String ext = "";
      if (name != null && !name.isEmpty()) {
        int dot = name.indexOf('.');
        if ((dot >= 0) && (dot < name.length() - 1)) {
          ext = name.substring(dot + 1);
        }
      }
      return ext;
    }

    private synchronized void loadMimeTypes() {
      if (loaded) {
        return;
      }
      loaded = true;
      String userHome = System.getProperty("user.home");
      if (userHome != null) {
        loadFrom(Paths.get(userHome, ".mime.types"));
      }
      loadFrom(Paths.get("/etc/mime.types"));
    }

    private void loadFrom(Path file) {
      if (!Files.isReadable(file)) {
        return;
      }
      try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
        String line;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.charAt(0) == '#') {
            continue;
          }
          String[] parts = line.split("\\s+");
          if (parts.length < 2) {
            continue;
          }
          for (int i = 1; i < parts.length; i++) {
            String ext = parts[i];
            if (!ext.isEmpty() && !mimeTypeMap.containsKey(ext)) {
              mimeTypeMap.put(ext, parts[0]);
            }
          }
        }
      } catch (IOException ignore) {
      }
    }

    private static final Pattern kTypePattern = Pattern.compile("[^\\s/]+/[^\\s/;]+");

    private static String parse(String s) {
      if (s == null) {
        return null;
      }
      int slash = s.indexOf('/');
      if (slash < 0) {
        return null;
      }
      return s.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public String probeContentType(Path path) throws IOException {
      String result = implProbeContentType(path);
      if (result == null) {
        Path fn = path.getFileName();
        if (fn != null) {
          result = URLConnection.guessContentTypeFromName(fn.toString());
        }
      }
      return parse(result);
    }

    private String implProbeContentType(Path path) {
      Path fn = path.getFileName();
      if (fn == null) {
        return null;
      }
      String ext = getExtension(fn.toString());
      if (ext.isEmpty()) {
        return null;
      }
      loadMimeTypes();
      if (mimeTypeMap.isEmpty()) {
        return null;
      }
      String mimeType;
      do {
        mimeType = mimeTypeMap.get(ext);
        if (mimeType == null) {
          ext = getExtension(ext);
        }
      } while (mimeType == null && !ext.isEmpty());
      return mimeType;
    }
  }
}
