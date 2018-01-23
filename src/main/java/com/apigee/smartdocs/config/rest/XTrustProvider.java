/**
 * Copyright 2017 Google Inc.
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

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivilegedAction;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager; 
  
public final class XTrustProvider extends java.security.Provider
{ 
    private final static String NAME = "XTrustJSSE"; 
    private final static String INFO =
        "XTrust JSSE Provider (implements trust factory with truststore validation disabled)"; 
    private final static double VERSION = 1.0D; 
     
    public XTrustProvider()
   { 
       super(NAME, VERSION, INFO); 
        
       AccessController.doPrivileged(new PrivilegedAction<Object>()
      { 
         public Object run()
         { 
                 put("TrustManagerFactory." + TrustManagerFactoryImpl.getAlgorithm(),  
                                               TrustManagerFactoryImpl.class.getName()); 
                 return null; 
             } 
       }); 
    } 
     
    public static void install()
   { 
       if(Security.getProvider(NAME) == null)
      { 
          Security.insertProviderAt(new XTrustProvider(), 2); 
          Security.setProperty("ssl.TrustManagerFactory.algorithm",TrustManagerFactoryImpl.getAlgorithm()); 
       } 
    } 
     
    public final static class TrustManagerFactoryImpl extends TrustManagerFactorySpi
   { 
       public TrustManagerFactoryImpl() { } 
       public static String getAlgorithm() { return "XTrust509"; } 
       protected void engineInit(KeyStore keystore) throws KeyStoreException { } 
       protected void engineInit(ManagerFactoryParameters mgrparams)
         throws InvalidAlgorithmParameterException
      { 
          throw new InvalidAlgorithmParameterException(
              XTrustProvider.NAME + " does not use ManagerFactoryParameters"); 
       } 
        
       protected TrustManager[] engineGetTrustManagers()
      { 
            return new TrustManager[] { new X509TrustManager()
         { 
             public X509Certificate[] getAcceptedIssuers() { return null; } 
             public boolean verify(String arg0, SSLSession arg1) { return true;	}
             public void checkClientTrusted(X509Certificate[] certs, String authType) { } 
             public void checkServerTrusted(X509Certificate[] certs, String authType) { } 
            }}; 
        } 
    } 
 }