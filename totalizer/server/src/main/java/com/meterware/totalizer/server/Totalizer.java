package com.meterware.totalizer.server;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2005, Russell Gold
 *
 *******************************************************************************************************************/

import com.meterware.totalizer.*;
import com.meterware.totalizer.display.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.Date;
import java.net.Socket;

/**
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 */
public class Totalizer {

    private OutputStreamWriter _writer;
    private Socket _socket;
    private Date _lastUpdate = new Date();

    public static void main( String args[] ) {
        try {
            new Totalizer( 5001, 5002 );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Totalizer( int port, int toteBoardPort ) throws IOException {
        openToteBoardConnection( toteBoardPort );
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread( new TerminalHandler( socket ) ).start();
        }
        // serverSocket.close();  should close when all terminals disconnect
    }


    public void handleRequest( Socket replySocket, byte[] requestBytes ) throws IOException {
        String request = new String( requestBytes );
        StringTokenizer st = new StringTokenizer( request );
        String command = st.nextToken();
        if (command.equalsIgnoreCase( "load" ) ) {
            String fileName = st.nextToken();
            Card card = Track.loadCard(fileName);
            downloadCard( replySocket, card );
        } else if (command.equalsIgnoreCase( "download" ) ) {
            String trackName = st.nextToken();
            Card card = Track.getTrack( trackName ).getCurrentCard();
            downloadCard( replySocket, card );
        } else if (command.equalsIgnoreCase( "bet" ) ) {
            String trackName = st.nextToken();
            Track track = Track.getTrack( trackName );
            String poolName = st.nextToken();
            String horseNum = st.nextToken();
            BettingPool pool = track.getPool( track.getCurrentCard(), poolName );
            String amount = st.nextToken();
            pool.addBet( Integer.parseInt( horseNum )-1, Double.parseDouble( amount ) );
            if (pool.isWinPool() && track.currentRace() == pool.getRace() &&
                ((((new Date().getTime() - _lastUpdate.getTime()) / 1000) >= 20) && track.hasEnoughBets())) {
                this.newFrame( Constants.FMT_ODDS );
                this.sendData( 0, 0, trackName );
                this.sendData( 0, 1, Integer.toString( track.currentRace() ) );
                Race race = track.getCurrentCard().getRace( track.currentRace() );
                for (int i = 1; i <= race.getNumHorses(); i++) {
                    this.sendData( i, 0, Integer.toString( i ) );
                    this.sendData( i, 1, withNBSP( race.getHorseName( i ) ) );
                    double otherBets = track.getPayoutPortion() * (pool.getTotalBets() - pool.getBet( i-1 ));
                    double odds = Math.min( 100.0, otherBets / Math.max( 2, pool.getBet( i-1 ) ) );
                    Fraction fraction = Fraction.toFraction( odds, 0.05, 4 );
                    this.sendData( i, 2, fraction.getNumerator() + "-" + fraction.getDenominator() );
                }
                this.endFrame();
            }
        } else if (command.equalsIgnoreCase( "display" ) ) {
            String trackName = st.nextToken();
            Track track = Track.getTrack( trackName );
            int raceNum = Integer.parseInt( st.nextToken() );
            track.setCurrentRace( raceNum );
            _lastUpdate = new Date();
            Race race = track.getCurrentCard().getRace( raceNum );
            this.newFrame( Constants.FMT_ODDS );
            this.sendData( 0, 0, trackName );
            this.sendData( 0, 1, Integer.toString( raceNum ) );
            for (int i = 1; i <= race.getNumHorses(); i++) {
                this.sendData( i, 0, Integer.toString( i ) );
                this.sendData( i, 1, withNBSP( race.getHorseName( i ) ) );
                Fraction fraction = Fraction.toFraction( race.getInitialOdds( i ) );
                this.sendData( i, 2, fraction.getNumerator() + "-" + fraction.getDenominator() );
            }
            this.endFrame();
        } else if (command.equalsIgnoreCase( "results" ) ) {
            String trackName = st.nextToken();
            Track track = Track.getTrack( trackName );
            int raceNum = Integer.parseInt( st.nextToken() );
            Race race = track.getCurrentCard().getRace( raceNum );
            this.newFrame( Constants.FMT_RESULTS );
            this.sendData( 0, 0, trackName );
            this.sendData( 0, 1, Integer.toString( raceNum ) );
            int win   = Integer.parseInt( st.nextToken() );
            int place = Integer.parseInt( st.nextToken() );
            int show  = Integer.parseInt( st.nextToken() );
            this.sendData( 1, 0, Integer.toString( win ) );
            this.sendData( 1, 1, withNBSP( race.getHorseName( win ) ) );
            this.sendData( 2, 0, Integer.toString( place ) );
            this.sendData( 2, 1, withNBSP( race.getHorseName( place ) ) );
            this.sendData( 3, 0, Integer.toString( show ) );
            this.sendData( 3, 1, withNBSP( race.getHorseName( show ) ) );
            DecimalFormat format = new DecimalFormat( "#,##0.00" );
            BettingPool winPool = track.getPool( track.getCurrentCard(), "WIN-" + raceNum );
            double winExcess = track.getPayoutPortion() * (winPool.getTotalBets() - winPool.getBet( win-1 ));
            double winPayout = 2 * (winExcess + winPool.getBet( win-1 )) / winPool.getBet( win-1 );
            this.sendData( 1, 2, format.format( winPayout ) );
            BettingPool placePool = track.getPool( track.getCurrentCard(), "PLACE-" + raceNum );
            double placeExcess = track.getPayoutPortion() * (placePool.getTotalBets() - placePool.getBet( win-1 ) - placePool.getBet( place-1 )) / 2;
            double placePayout1 = 2 * (placeExcess + placePool.getBet( win-1 )) / placePool.getBet( win-1 );
            double placePayout2 = 2 * (placeExcess + placePool.getBet( place-1 )) / placePool.getBet( place-1 );
            this.sendData( 1, 3, format.format( placePayout1 ) );
            this.sendData( 2, 3, format.format( placePayout2 ) );
            BettingPool showPool = track.getPool( track.getCurrentCard(), "SHOW-" + raceNum );
            double showExcess = track.getPayoutPortion() * (showPool.getTotalBets() - showPool.getBet( win-1 ) - showPool.getBet( place-1 ) - showPool.getBet( show-1 )) / 3;
            double showPayout1 = 2 * (showExcess + showPool.getBet( win-1 )) / showPool.getBet( win-1 );
            double showPayout2 = 2 * (showExcess + showPool.getBet( place-1 )) / showPool.getBet( place-1 );
            double showPayout3 = 2 * (showExcess + showPool.getBet( show-1 )) / showPool.getBet( show-1 );
            this.sendData( 1, 4, format.format( showPayout1 ) );
            this.sendData( 2, 4, format.format( showPayout2 ) );
            this.sendData( 3, 4, format.format( showPayout3 ) );
            this.endFrame();
        }
    }


    private void downloadCard( Socket replySocket, Card card ) throws IOException {
        OutputStream outputStream = replySocket.getOutputStream();
        DataOutputStream dos = new DataOutputStream( outputStream );
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject(card);
        oos.close();
        
        dos.writeInt( baos.size() );
        dos.write(baos.toByteArray());
        dos.flush();
    }


    private String withNBSP( String s ) {
        return s.replace( ' ', (char) 0x0a0 );
    }


    void sendMessage( String message ) throws IOException {
        _writer.write( message.length() + ">" );
        _writer.write( message );
        _writer.flush();
    }


    void endFrame() throws IOException {
        sendMessage("END");
    }


    void sendData( int row, int column, Object value ) throws IOException {
        sendMessage( "DATA " + row + ' ' + column + ' ' + value );
    }


    void newFrame( int format ) throws IOException {
        sendMessage( "NEW " + format );
    }


    void closeToteBoardConnection() throws IOException {
        _writer.close();
        _socket.close();
    }


    void openToteBoardConnection( int port ) throws IOException {
        _socket = new Socket( "localhost", port );
        OutputStream outputStream = _socket.getOutputStream();
        _writer = new OutputStreamWriter( outputStream );
    }
    
    
    private class TerminalHandler implements Runnable {

        private Socket socket;

        public TerminalHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[2048];
                int size;
                while (-1 < (size = inputStream.read( buffer ))) {
                    byte[] request = new byte[ size ];
                    System.arraycopy(buffer, 0, request, 0, size);
                    handleRequest( socket, request );
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
