/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class PKIXRevocationChecker extends PKIXCertPathChecker {
  private URI ocspResponder;
  private X509Certificate ocspResponderCert;
  private List<Extension> ocspExtensions = Collections.<Extension>emptyList();
  private Map<X509Certificate, byte[]> ocspResponses = Collections.<X509Certificate, byte[]>emptyMap();
  private Set<Option> options = Collections.<Option>emptySet();

  protected PKIXRevocationChecker() {
  }

  public void setOcspResponder(URI uri) {
    this.ocspResponder = uri;
  }

  public URI getOcspResponder() {
    return ocspResponder;
  }

  public void setOcspResponderCert(X509Certificate cert) {
    this.ocspResponderCert = cert;
  }

  public X509Certificate getOcspResponderCert() {
    return ocspResponderCert;
  }

  public void setOcspExtensions(List<Extension> extensions) {
    this.ocspExtensions = (extensions == null) ? Collections.<Extension>emptyList()
        : new ArrayList<Extension>(extensions);
  }

  public List<Extension> getOcspExtensions() {
    return Collections.unmodifiableList(ocspExtensions);
  }

  public void setOcspResponses(Map<X509Certificate, byte[]> responses) {
    if (responses == null) {
      this.ocspResponses = Collections.<X509Certificate, byte[]>emptyMap();
    } else {
      Map<X509Certificate, byte[]> copy = new HashMap<X509Certificate, byte[]>(responses.size());
      for (Map.Entry<X509Certificate, byte[]> e : responses.entrySet()) {
        copy.put(e.getKey(), e.getValue().clone());
      }
      this.ocspResponses = copy;
    }
  }

  public Map<X509Certificate, byte[]> getOcspResponses() {
    Map<X509Certificate, byte[]> copy = new HashMap<X509Certificate, byte[]>(ocspResponses.size());
    for (Map.Entry<X509Certificate, byte[]> e : ocspResponses.entrySet()) {
      copy.put(e.getKey(), e.getValue().clone());
    }
    return copy;
  }

  public void setOptions(Set<Option> options) {
    this.options = (options == null) ? Collections.<Option>emptySet() : new HashSet<Option>(options);
  }

  public Set<Option> getOptions() {
    return Collections.unmodifiableSet(options);
  }

  public abstract List<CertPathValidatorException> getSoftFailExceptions();

  @Override
  public PKIXRevocationChecker clone() {
    PKIXRevocationChecker copy = (PKIXRevocationChecker) super.clone();
    copy.ocspExtensions = new ArrayList<Extension>(ocspExtensions);
    copy.ocspResponses = new HashMap<X509Certificate, byte[]>(ocspResponses);
    for (Map.Entry<X509Certificate, byte[]> entry : copy.ocspResponses.entrySet()) {
      byte[] value = entry.getValue();
      entry.setValue(value.clone());
    }
    copy.options = new HashSet<Option>(options);
    return copy;
  }

  public enum Option {
    ONLY_END_ENTITY,
    PREFER_CRLS,
    NO_FALLBACK,
    SOFT_FAIL
  }
}
