package com.meterware.io;
/********************************************************************************************************************
 * $Id: SocketServerAccess.java,v 1.5 2005/02/03 11:42:19 russgold Exp $
 *
 * Copyright (c) 2004-2005, Russell Gold
 *
 *******************************************************************************************************************/
import java.io.*;
import java.net.Socket;
import java.util.Iterator;

/**
 *
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 **/
public class SocketServerAccess {

    private OutputStream _outputStream;
    private InputStream  _inputStream;
    private boolean      _active;
    private Socket       _socket;
    private ClientMessageHandler _handler;


    public static SocketServerAccess connectToRemoteServer( String host, int port ) throws IOException {
        return new SocketServerAccess( new Socket( host, port ) );
    }


    private SocketServerAccess( Socket socket ) throws IOException {
        _socket = socket;
        _outputStream = new BufferedOutputStream( socket.getOutputStream() );
        _inputStream = new BufferedInputStream( socket.getInputStream() );
    }


    public InputStream getInputStream() {
        return _inputStream;
    }


    public OutputStream getOutputStream() {
        return _outputStream;
    }


    public void disconnect() throws IOException {
        _active = false;
        _outputStream.close();
        _socket.close();
    }


    public void pause() {
        _active = false;
    }


    public void resume() {
        _active = true;
        Thread thread = new Thread( new MessageHandler() );
        thread.setDaemon( true );
        thread.start();
    }

    public ClientMessageHandler getHandler() {
        return _handler;
    }

    public void setHandler( ClientMessageHandler handler ) {
        _handler = handler;
    }

    /**
     * Extracts length-delineated messages from the specified byte array and invokes the handler for each one.
     * @param bytes bytes received
     */
    protected void handleMessages( byte[] bytes ) {
        Iterator mi = new MessageIterator( bytes );
        while (mi.hasNext()) {
            byte[] message = (byte[]) mi.next();
            getHandler().handleMessage( message );
        }
    }

    public void sendRequest( byte[] requestBody ) throws IOException {
        getOutputStream().write( requestBody );
        getOutputStream().flush();
    }


    private class MessageHandler implements Runnable {

        public void run() {
            while (_active) {
                try {
                    try { Thread.sleep( 20 ); } catch( InterruptedException e ) {};
                    int available = getInputStream().available();
                    if (available == 0) continue;
                    byte[] bytes = new byte[ available ];
                    getInputStream().read( bytes );
                    SocketServerAccess.this.handleMessages( bytes );
                } catch (IOException e) {
                    if (_active) e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                }
            }
        }
    }

}
