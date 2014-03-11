package com.meterware.totalizer.display;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2005, Russell Gold
 *
 *******************************************************************************************************************/
import javax.swing.*;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.InputStream;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.Socket;
import java.net.ServerSocket;

/**
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 */
public class ToteBoard implements Runnable {

    private JPanel          _display;
    private ToteBoardLayout _currentLayout;
    private ToteBoardLayout _newLayout;
    private ServerSocket    _serverSocket;


    public static void main( String[] args ) {
        try {
            Frame frame = new Frame();
            frame.addWindowListener(
                    new WindowAdapter() {
                        public void windowClosing( WindowEvent e ) {
                            System.exit( 0 );
                        }
                    }
            );
            frame.setLayout( new GridLayout() );
            ToteBoard toteBoard = new ToteBoard( 5002 );
            frame.add( toteBoard.getPanel() );
            frame.setSize( 500, 300 );
            frame.setVisible( true );
            new Thread( toteBoard ).start();
        } catch( Throwable t ) {
            t.printStackTrace();
        }
    }


    public ToteBoard( int port ) throws IOException {
        _serverSocket = new ServerSocket( port );
        _display = new JPanel() {
            public void paint( Graphics g ) {
                if (_currentLayout == null) {
                    super.paint( g );
                } else {
                    g.setColor( getBackground() );
                    g.fillRect( 0, 0, getWidth(), getHeight() );
                    g.setColor( getForeground() );
                    _currentLayout.draw( g );
                }
            }
        };
    }


    private JPanel getPanel() {
        return _display;
    }


    protected void handleRequest( byte[] bytes ) {
        String request = new String( bytes );
        while (request.length() > 0) {
            int i = request.indexOf( '>' );
            assert i > 0 : "no length indicator sent";
            String lengthString = request.substring( 0, i );
            int length = Integer.parseInt( lengthString );
            handleRequest( request.substring( i+1, i+length+1 ) );
            request = request.substring( i+length+1 );
        }
    }


    private void handleRequest( String request ) {
        StringTokenizer st = new StringTokenizer( request );
        String command = st.nextToken();
        if (command.equalsIgnoreCase( "new" ) ) {
            startNewFrame( _display, Integer.parseInt( st.nextToken() ) );
        } else if (command.equalsIgnoreCase( "data" ) ) {
            int row = Integer.parseInt( st.nextToken() );
            int column = Integer.parseInt( st.nextToken() );
            String value = withoutNBSP( st.nextToken() );
            this.setData( row, column, value );
        } else if (command.equalsIgnoreCase( "end" ) ) {
            displayNewFrame( _display );
        }
    }


    private String withoutNBSP( String s ) {
        return s.replace( (char) 0x0a0, ' ' );
    }


    void startNewFrame( JPanel panel, int format ) {
        _newLayout = new ToteBoardLayout( panel, format == Constants.FMT_ODDS );
    }


    void setData( int i, int j, String value ) {
        _newLayout.setValue( i, j, value );
    }


    void displayNewFrame( JPanel panel ) {
        _currentLayout = _newLayout;
        _newLayout = null;
        _currentLayout.setFont(      new Font( "Monospaced", Font.PLAIN, 18 ) );
        _currentLayout.setTitleFont( new Font( "Monospaced", Font.PLAIN, 24 ) );
        panel.repaint();
    }


    public void run() {
        try {
            Socket socket = _serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[2048];
            int size;
            while (-1 < (size = inputStream.read( buffer ))) {
                byte[] request = new byte[ size ];
                System.arraycopy( buffer, 0, request, 0, size );
                handleRequest( request );
            }
            socket.close();
            _serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
