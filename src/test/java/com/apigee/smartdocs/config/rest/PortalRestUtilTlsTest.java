/**
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apigee.smartdocs.config.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;

import java.security.Security;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.junit.Test;

/**
 * Regression test for b/585251411.
 *
 * <p>Prior to the fix, {@link PortalRestUtil}'s {@code REQUEST_FACTORY} and
 * {@code APACHE_REQUEST_FACTORY} initializers installed a trust-all
 * {@code TrustManagerFactory} provider ({@code XTrustProvider}) and a
 * trust-all {@code HostnameVerifier} ({@code FakeHostnameVerifier}) on the
 * JVM on every request. That weakened the default TLS settings for every
 * other HTTPS client in the same JVM.
 *
 * <p>These tests assert that building an {@link HttpRequest} from either
 * factory leaves the JVM-wide TLS state unchanged: the default
 * {@link HostnameVerifier} is still the JDK default, and no {@code XTrustJSSE}
 * security provider is registered.
 */
public class PortalRestUtilTlsTest {

  private static final String TRUST_ALL_PROVIDER_NAME = "XTrustJSSE";

  @Test
  public void netHttpTransportRequestLeavesDefaultHostnameVerifierUnchanged()
      throws Exception {
    HostnameVerifier before = HttpsURLConnection.getDefaultHostnameVerifier();
    assertNotNull("JDK must ship a default HostnameVerifier", before);

    HttpRequest request = PortalRestUtil.REQUEST_FACTORY.buildGetRequest(
        new GenericUrl("https://example.invalid/"));
    assertNotNull(request);

    HostnameVerifier after = HttpsURLConnection.getDefaultHostnameVerifier();
    assertSame(
        "PortalRestUtil must not replace the JVM-wide default HostnameVerifier",
        before, after);
    assertTrue(
        "Default HostnameVerifier class name must not contain 'Fake'",
        !after.getClass().getName().contains("Fake"));
  }

  @Test
  public void apacheHttpTransportRequestLeavesDefaultHostnameVerifierUnchanged()
      throws Exception {
    HostnameVerifier before = HttpsURLConnection.getDefaultHostnameVerifier();
    assertNotNull("JDK must ship a default HostnameVerifier", before);

    HttpRequest request = PortalRestUtil.APACHE_REQUEST_FACTORY.buildGetRequest(
        new GenericUrl("https://example.invalid/"));
    assertNotNull(request);

    HostnameVerifier after = HttpsURLConnection.getDefaultHostnameVerifier();
    assertSame(
        "PortalRestUtil must not replace the JVM-wide default HostnameVerifier",
        before, after);
    assertTrue(
        "Default HostnameVerifier class name must not contain 'Fake'",
        !after.getClass().getName().contains("Fake"));
  }

  @Test
  public void trustAllSecurityProviderIsNotInstalled() throws Exception {
    // Touch both factories so any install() side effects would have fired.
    PortalRestUtil.REQUEST_FACTORY.buildGetRequest(
        new GenericUrl("https://example.invalid/"));
    PortalRestUtil.APACHE_REQUEST_FACTORY.buildGetRequest(
        new GenericUrl("https://example.invalid/"));

    assertTrue(
        "Trust-all security provider '" + TRUST_ALL_PROVIDER_NAME
            + "' must not be installed",
        Security.getProvider(TRUST_ALL_PROVIDER_NAME) == null);
  }
}
