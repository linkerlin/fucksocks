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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import fucksocks.client.SSLSocks5;
import fucksocks.client.SocksProxy;
import fucksocks.client.SocksSocket;
import fucksocks.common.SSLConfiguration;

/**
 * The class <code>SSLBasedProxyClient</code> is a client to connect a SSL based SOCKS5 proxy
 * server.
 * 
 * @author Youchao Feng
 * @version 1.0
 * @since 1.0
 *
 */
public class SSLBasedProxyClient {

  public static void main(String[] args) {

    InputStream inputStream = null;
    OutputStream outputStream = null;
    Socket socket = null;
    StringBuffer response = null;
    int length = 0;
    byte[] buffer = new byte[2048];

    try {
      SSLConfiguration configuration = SSLConfiguration.loadClassPath("client-ssl.properties");
      SocksProxy proxy = new SSLSocks5(new InetSocketAddress("localhost", 1080), configuration);
      socket = new SocksSocket(proxy, new InetSocketAddress("whois.internic.net", 43));

      inputStream = socket.getInputStream();
      outputStream = socket.getOutputStream();
      PrintWriter printWriter = new PrintWriter(outputStream);
      printWriter.print("domain google.com\r\n"); // query google.com WHOIS.
      printWriter.flush();

      response = new StringBuffer();
      while ((length = inputStream.read(buffer)) > 0) {
        response.append(new String(buffer, 0, length));
      }

      System.out.println(response.toString());

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      ResourceUtil.close(inputStream, outputStream, socket);
    }
  }

}
