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

package fucksocks.server.io;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The class <code>SocketPipe</code> represents pipe that can transfer data from one socket to
 * another socket. The tow socket should be connected sockets. If any of the them occurred error the
 * pipe will close all of them.
 *
 * @author Youchao Feng
 * @date Apr 15, 2015 10:46:03 AM
 * @version 1.0
 *
 */
public class SocketPipe implements Pipe {

  /**
   * Logger
   */
  protected static final Logger logger = LoggerFactory.getLogger(SocketPipe.class);

  /**
   * Pipe one.
   */
  private Pipe pipe1;

  /**
   * Pipe tow.
   */
  private Pipe pipe2;

  /**
   * Socket one.
   */
  private Socket socket1;

  /**
   * Socket two.
   */
  private Socket socket2;

  private String name;

  /**
   * flag.
   */
  private boolean running = false;

  private PipeListener listener = new PipeListenerImp();

  /**
   * Constructs SocketPipe instance by tow connected sockets.
   * 
   * @param socket1 A connected socket.
   * @param socket2 Another connected socket.
   * @throws IOException If an I/O error occurred.
   */
  public SocketPipe(Socket socket1, Socket socket2) throws IOException {
    if (socket1.isClosed() || socket2.isClosed()) {
      throw new IllegalArgumentException("socket should be connected");
    }
    this.socket1 = socket1;
    this.socket2 = socket2;
    pipe1 = new StreamPipe(socket1.getInputStream(), socket2.getOutputStream());
    ((StreamPipe) pipe1).setName("OUTPUT_PIPE");
    pipe2 = new StreamPipe(socket2.getInputStream(), socket1.getOutputStream());
    ((StreamPipe) pipe2).setName("INPUT_PIPE");;

    pipe1.addPipeListener(listener);
    pipe2.addPipeListener(listener);
  }

  @Override
  public boolean start() {
    pipe1.start();
    pipe2.start();
    running = true;
    return running;
  }

  @Override
  public boolean stop() {
    if (running) {
      pipe1.stop();
      pipe2.stop();
      logger.debug("Socket pipe stopped");
      if (!pipe1.isRunning() && !pipe2.isRunning()) {
        running = false;
      }
    }
    return running;
  }

  @Override
  public boolean close() {

    pipe2.removePipeListener(listener);
    pipe1.removePipeListener(listener);

    stop();

    try {
      if (socket1 != null && !socket1.isClosed()) {
        socket1.close();
      }
      if (socket2 != null && !socket2.isClosed()) {
        socket2.close();
      }
      logger.debug("Socket pipe closed");
      return true;
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return false;
  }

  @Override
  public int getBufferSize() {
    return 0;
  }

  @Override
  public void setBufferSize(int bufferSize) {
    pipe1.setBufferSize(bufferSize);
    pipe2.setBufferSize(bufferSize);
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void addPipeListener(PipeListener pipeListener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removePipeListener(PipeListener pipeListener) {

  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }



  /**
   * The class <code>PipeListenerImp</code> is a pipe listener.
   * 
   * @author Youchao Feng
   * @date Apr 15, 2015 9:05:45 PM
   * @version 1.0
   *
   */
  private class PipeListenerImp implements PipeListener {

    @Override
    public void onStop(Pipe pipe) {
      StreamPipe streamPipe = (StreamPipe) pipe;
      logger.debug("Pipe[{}] stoped", streamPipe.getName());
      close();
    }

    @Override
    public void onStart(Pipe pipe) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onTransfer(Pipe pipe, byte[] buffer, int bufferLength) {

    }

    @Override
    public void onError(Pipe pipe, Exception exception) {
      logger.info("{} {}", name, exception.getMessage());
    }

  }


}
