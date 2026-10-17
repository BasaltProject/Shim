/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.net.IDN;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class SNIHostName extends SNIServerName {
  private final String hostname;

  public SNIHostName(String hostname) {
    super(StandardConstants.SNI_HOST_NAME,
        (hostname = IDN.toASCII(Objects.requireNonNull(hostname, "Server name value of host_name cannot be null"),
            IDN.USE_STD3_ASCII_RULES)).getBytes(StandardCharsets.UTF_8));
    this.hostname = hostname;
    checkHostName();
  }

  public SNIHostName(byte[] encoded) {
    super(StandardConstants.SNI_HOST_NAME, encoded);
    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT);
    try {
      this.hostname = IDN.toASCII(decoder.decode(ByteBuffer.wrap(encoded)).toString(), IDN.USE_STD3_ASCII_RULES);
    } catch (RuntimeException | CharacterCodingException e) {
      throw new IllegalArgumentException("The encoded server name value is invalid", e);
    }
    checkHostName();
  }

  public String getAsciiName() {
    return hostname;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof SNIHostName) {
      return hostname.equalsIgnoreCase(((SNIHostName) other).hostname);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + hostname.toUpperCase(Locale.ENGLISH).hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "type=host_name (0), value=" + hostname;
  }

  public static SNIMatcher createSNIMatcher(String regex) {
    if (regex == null) {
      throw new NullPointerException("The regular expression cannot be null");
    }
    return new SNIHostNameMatcher(regex);
  }

  private void checkHostName() {
    if (hostname.isEmpty()) {
      throw new IllegalArgumentException("Server name value of host_name cannot be empty");
    }
    if (hostname.endsWith(".")) {
      throw new IllegalArgumentException("Server name value of host_name cannot have the trailing dot");
    }
  }

  private static final class SNIHostNameMatcher extends SNIMatcher {
    private final Pattern pattern;

    SNIHostNameMatcher(String regex) {
      super(StandardConstants.SNI_HOST_NAME);
      pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean matches(SNIServerName serverName) {
      if (serverName == null) {
        throw new NullPointerException("The SNIServerName argument cannot be null");
      }
      SNIHostName hostname;
      if (!(serverName instanceof SNIHostName)) {
        if (serverName.getType() != StandardConstants.SNI_HOST_NAME) {
          throw new IllegalArgumentException("The server name type is not host_name");
        }
        try {
          hostname = new SNIHostName(serverName.getEncoded());
        } catch (NullPointerException | IllegalArgumentException e) {
          return false;
        }
      } else {
        hostname = (SNIHostName) serverName;
      }
      String asciiName = hostname.getAsciiName();
      if (pattern.matcher(asciiName).matches()) {
        return true;
      }
      return pattern.matcher(IDN.toUnicode(asciiName)).matches();
    }
  }
}
