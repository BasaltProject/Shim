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

import java.net.MalformedURLException;
import java.net.URL;

public class UrlTest {
  private static String query="var1=val1&var2=val2";
  private static String path="/testpath";
  private static String domain="file://www.readytalk.com";
  private static String file=path + "?" + query;
  private static URL url;
  
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }
  
  private static void setupURL() throws MalformedURLException {
    StringBuilder builder = new StringBuilder();
    builder.append(domain);
    builder.append(file);
    url = new URL(builder.toString());
  }
  
  private static void testGetPath() {
    expect(url.getPath().equals(path));
  }
  
  private static void testGetFile() {
    expect(url.getFile().equals(file));
  }
  
  private static void testGetQuery() {
    expect(url.getQuery().equals(query));
  }

  public static void main(String[] args) throws MalformedURLException {
    setupURL();
    testGetPath();
    testGetFile();
    testGetQuery();
  }

}
