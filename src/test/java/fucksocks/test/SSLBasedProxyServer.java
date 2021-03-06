/*
 * Copyright 2015-2025 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package fucksocks.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import fucksocks.common.SSLConfiguration;
import fucksocks.common.methods.NoAuthencationRequiredMethod;
import fucksocks.server.SSLSocksProxyServer;
import fucksocks.server.Socks5Handler;
import fucksocks.server.SocksProxyServer;

/**
 * The class <code>SSLBasedProxyServer</code> is a test class to start a SSL based SOCKS5 proxy server.
 * 
 * @author Youchao Feng
 * @version 1.0
 * @since 1.0
 */
public class SSLBasedProxyServer {

  public static void main(String[] args) throws FileNotFoundException, IOException {
    SSLConfiguration configuration = SSLConfiguration.loadClassPath("/server-ssl.properties");
    SocksProxyServer proxyServer = new SSLSocksProxyServer(Socks5Handler.class, configuration);
    proxyServer.setSupportMethods(new NoAuthencationRequiredMethod());
    try {
      proxyServer.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
