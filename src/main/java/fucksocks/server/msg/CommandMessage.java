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

package fucksocks.server.msg;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import fucksocks.common.AddressType;
import fucksocks.common.SocksCommand;
import fucksocks.common.SocksException;
import fucksocks.utils.SocksUtil;

/**
 * The class <code>RequestCommandMessage</code> represents a SOCKS5 command message.
 * 
 * @author Youchao Feng
 * @date Apr 6, 2015 11:10:12 AM
 * @version 1.0
 *
 */
public class CommandMessage implements ReadableMessage, WritableMessage {

  /**
   * Value of CONNECTE command.
   */
  protected static final int CMD_CONNECT = 0x01;

  /**
   * Value of BIND command.
   */
  protected static final int CMD_BIND = 0x02;

  /**
   * Value of UDP ASSOCIATE command.
   */
  protected static final int CMD_UDP_ASSOCIATE = 0x03;

  /**
   * Value of RESERVED field.
   */
  private static final int RESERVED = 0x00;

  /**
   * Version.
   */
  private int version;

  /**
   * IP address of destination.
   */
  private InetAddress inetAddress;

  /**
   * Port of destination.
   */
  private int port;

  /**
   * Host of destination.
   */
  private String host;

  /**
   * SOCKS command.
   */
  private SocksCommand command;

  /**
   * Reserved field.
   */
  private int reserved;

  /**
   * Address type.
   */
  private int addressType;

  /**
   * SOCKS exception in Command message.
   */
  private SocksException socksException;

  @Override
  public int getLength() {
    return getBytes().length;
  }

  @Override
  public byte[] getBytes() {
    byte[] bytes = null;

    switch (addressType) {
      case AddressType.IPV4:
        bytes = new byte[10];
        byte[] ipv4Bytes = inetAddress.getAddress();// todo
        for (int i = 0; i < ipv4Bytes.length; i++) {
          bytes[i + 4] = ipv4Bytes[i];
        }
        bytes[8] = SocksUtil.getFisrtByteFromInt(port);
        bytes[9] = SocksUtil.getSecondByteFromInt(port);
        break;

      case AddressType.IPV6:
        bytes = new byte[22];
        byte[] ipv6Bytes = inetAddress.getAddress();// todo
        for (int i = 0; i < ipv6Bytes.length; i++) {
          bytes[i + 4] = ipv6Bytes[i];
        }
        bytes[20] = SocksUtil.getFisrtByteFromInt(port);
        bytes[21] = SocksUtil.getSecondByteFromInt(port);
        break;

      case AddressType.DOMAINNAME:
        final int hostLength = host.getBytes().length;
        bytes = new byte[7 + hostLength];
        bytes[4] = (byte) hostLength;
        for (int i = 0; i < hostLength; i++) {
          bytes[5 + i] = host.getBytes()[i];
        }
        bytes[5 + hostLength] = SocksUtil.getFisrtByteFromInt(port);
        bytes[6 + hostLength] = SocksUtil.getSecondByteFromInt(port);
        break;

      default:
        break;
    }

    bytes[0] = (byte) version;
    bytes[1] = (byte) command.getValue();
    bytes[2] = RESERVED;
    bytes[3] = (byte) addressType;

    return bytes;
  }

  @Override
  public void read(InputStream inputStream) throws SocksException, IOException {

    version = inputStream.read();
    int cmd = inputStream.read();

    switch (cmd) {
      case CMD_CONNECT:
        command = SocksCommand.CONNECT;
        break;
      case CMD_BIND:
        command = SocksCommand.BIND;
        break;
      case CMD_UDP_ASSOCIATE:
        command = SocksCommand.UDP_ASSOCIATE;
        break;

      default:
        socksException = SocksException.serverReplyException(ServerReply.COMMAND_NOT_SUPPORTED);
    }
    reserved = inputStream.read();
    addressType = inputStream.read();

    if (!AddressType.isSupport(addressType) && socksException == null) {
      socksException = SocksException.serverReplyException(ServerReply.ADDRESS_TYPE_NOT_SUPPORTED);
    }

    // read address
    switch (addressType) {

      case AddressType.IPV4:
        byte[] addressBytes = new byte[4];
        inputStream.read(addressBytes);
        inetAddress = InetAddress.getByAddress(addressBytes);
        break;

      case AddressType.DOMAINNAME:
        int domainLength = inputStream.read();
        byte[] domainBytes = new byte[domainLength];
        for (int i = 0; i < domainBytes.length; i++) {
          domainBytes[i] = (byte) inputStream.read();
        }
        host = new String(domainBytes, Charset.forName("UTF-8"));
        try {
          inetAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
          if (socksException == null) {
            socksException = SocksException.serverReplyException(ServerReply.HOST_UNREACHABLE);
          }
        }
      default:
        // TODO Implement later.
        break;
    }

    // Read port
    byte[] portBytes = new byte[2];
    inputStream.read(portBytes);
    port = SocksUtil.bytesToInt(portBytes);

  }

  /**
   * Returns version.
   * 
   * @return Version.
   */
  public int getVersion() {
    return version;
  }

  /**
   * Sets version.
   * 
   * @param version Version.
   */
  public void setVersion(int version) {
    this.version = version;
  }

  public boolean hasSocksException() {
    return socksException != null;
  }

  public InetAddress getInetAddress() {
    return inetAddress;
  }

  public void setInetAddress(InetAddress inetAddress) {
    this.inetAddress = inetAddress;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public SocksCommand getCommand() {
    return command;
  }

  public void setCommand(SocksCommand command) {
    this.command = command;
  }

  public int getReserved() {
    return reserved;
  }

  public void setReserved(int reserved) {
    this.reserved = reserved;
  }

  public int getAddressType() {
    return addressType;
  }

  public void setAddressType(int addressType) {
    this.addressType = addressType;
  }

  public SocketAddress getSocketAddress() {
    return new InetSocketAddress(inetAddress, port);
  }

  public SocksException getSocksException() {
    return socksException;
  }

  public void setSocksException(SocksException socksException) {
    this.socksException = socksException;
  }

}
