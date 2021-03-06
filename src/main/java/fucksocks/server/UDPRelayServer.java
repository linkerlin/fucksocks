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

package fucksocks.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fucksocks.common.Socks5DatagramPacketHandler;

/**
 * The class <code>UDPRelayServer</code> represents a UDP relay server.
 * <p>
 * The UDP relay server will receive datagram packets from a client and transmit them to the
 * specified server. It will also receive datagram packets from other UDP servers and send them to
 * client. UDP relay server must need to know the client's IP address and port to find out where the
 * datagram packet from, because UDP is not long connection protocol.
 * </p>
 * 
 * @author Youchao Feng
 * @date Apr 22, 2015 12:54:50 AM
 * @version 1.0
 *
 */
public class UDPRelayServer implements Runnable {

  /**
   * Logger that subclasses also can use.
   */
  protected static final Logger logger = LoggerFactory.getLogger(UDPRelayServer.class);

  /**
   * SOCKS5 datagram packet handle.
   */
  private Socks5DatagramPacketHandler datagramPacketHandler = new Socks5DatagramPacketHandler();

  /**
   * UDP server.
   */
  private DatagramSocket server;

  /**
   * Buffer size.
   */
  private int bufferSize = 1024 * 1024 * 5;

  /**
   * Running thread.
   */
  private Thread thread;

  /**
   * A status flag.
   */
  private boolean running = false;

  /**
   * Client's IP address.
   */
  private InetAddress clientAddresss;

  /**
   * Client's port.
   */
  private int clientPort;

  /**
   * Constructs a {@link UDPRelayServer} instance.
   */
  public UDPRelayServer() {}

  /**
   * Constructs a {@link UDPRelayServer} instance with client's IP address and port. The UDP relay
   * server will use client's IP and port to find out where the datagram packet from.
   * 
   * @param clientInetAddress Client's IP address.
   * @param clientPort Client's port.
   */
  public UDPRelayServer(InetAddress clientInetAddress, int clientPort) {
    this(new InetSocketAddress(clientInetAddress, clientPort));
  }

  public UDPRelayServer(SocketAddress clientSocketAddress) {
    if (clientSocketAddress instanceof InetSocketAddress) {
      clientAddresss = ((InetSocketAddress) clientSocketAddress).getAddress();
      clientPort = ((InetSocketAddress) clientSocketAddress).getPort();
    } else {
      throw new IllegalArgumentException("Only support java.net.InetSocketAddress");
    }
  }

  /**
   * Starts a UDP relay server.
   * 
   * @return Server bind socket address.
   * @throws SocketException If a SOCKS protocol error occurred.
   */
  public SocketAddress start() throws SocketException {
    running = true;
    server = new DatagramSocket();
    SocketAddress socketAddress = server.getLocalSocketAddress();
    thread = new Thread(this);
    thread.start();
    return socketAddress;
  }

  /**
   * Stop the UDP relay server.
   */
  public void stop() {
    if (running) {
      running = false;
      thread.interrupt();
      if (!server.isClosed()) {
        server.close();
      }
    }
  }

  @Override
  public void run() {
    try {
      byte[] recvBuf = new byte[bufferSize];
      DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
      while (running) {
        server.receive(packet);
        if (isFromClient(packet)) {
          datagramPacketHandler.decapsulate(packet);
          server.send(packet);
        } else {
          packet =
              datagramPacketHandler.encapsulate(packet, new InetSocketAddress(clientAddresss,
                  clientPort));
          server.send(packet);
        }

      }
    } catch (IOException e) {
      if (e.getMessage().equalsIgnoreCase("Socket closed") && !running) {
        logger.debug("UDP relay server stoped");
      } else {
        logger.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Returns <code>true</code> if the the datagram packet from client.
   * 
   * @param packet Datagram packet the UDP server received.
   * @return If the datagram packet is sent from client, it will return <code>true</code>.
   */
  protected boolean isFromClient(DatagramPacket packet) {

    if (packet.getPort() == clientPort && clientAddresss.equals(packet.getAddress())) {
      return true;
    }
    // client is in local.
    else if (packet.getPort() == clientPort && clientAddresss.getHostAddress().startsWith("127.")) {
      return true;
    }
    return false;
  }

  /**
   * Return UDP server.
   * 
   * @return UDP server.
   */
  public DatagramSocket getServer() {
    return server;
  }

  /**
   * Sets UDP server.
   * 
   * @param server UDP server.
   */
  public void setServer(DatagramSocket server) {
    this.server = server;
  }

  /**
   * Returns buffer size.
   * 
   * @return Buffer size.
   */
  public int getBufferSize() {
    return bufferSize;
  }

  /**
   * Sets buffer size.
   * 
   * @param bufferSize Buffer size.
   */
  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  /**
   * Returns datagram packet handler.
   * 
   * @return the instance of {@link Socks5DatagramPacketHandler}.
   */
  public Socks5DatagramPacketHandler getDatagramPacketHandler() {
    return datagramPacketHandler;
  }

  /**
   * Sets datagram packet handler.
   * 
   * @param datagramPacketHandler Datagram packet handler.
   */
  public void setDatagramPacketHandler(Socks5DatagramPacketHandler datagramPacketHandler) {
    this.datagramPacketHandler = datagramPacketHandler;
  }

  /**
   * Returns client's IP address.
   * 
   * @return clinet's IP address.
   */
  public InetAddress getClientAddresss() {
    return clientAddresss;
  }

  /**
   * Sets client's IP address.
   * 
   * @param clientAddresss client's IP address.
   */
  public void setClientAddresss(InetAddress clientAddresss) {
    this.clientAddresss = clientAddresss;
  }

  /**
   * Returns client's port.
   * 
   * @return client's port.
   */
  public int getClientPort() {
    return clientPort;
  }

  /**
   * Sets client's port.
   * 
   * @param clientPort client's port.
   */
  public void setClientPort(int clientPort) {
    this.clientPort = clientPort;
  }

  /**
   * Return <code>true</code> if the UDP relay server is running.
   * 
   * @return If the UDP relay server is running, it will return <code>true</code>.
   */
  public boolean isRunning() {
    return running;
  }

}
