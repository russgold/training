package com.meterware.totalizer.bettor;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2005, Russell Gold
 *
 *******************************************************************************************************************/
import com.meterware.io.ClientMessageHandler;
import com.meterware.io.SocketServerAccess;
import com.meterware.totalizer.Card;
import com.meterware.totalizer.Race;

import javax.swing.*;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;

/**
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 */
public class BettorTerminal {

    private SocketServerAccess _serverAccess;
    private JPanel _view;
    private Card _card;
    private int _raceNum;

    private JLabel _trackLabel;
    private JComboBox _raceComboBox;
    private JComboBox _horseComboBox;
    private JComboBox _poolComboBox;
    private JTextField _betAmountField;


    public static void main( String[] args ) {
        try {
            if (args.length == 0) {
                launchTerminal( "localhost", 5001, "Belmont" );
            } else if (args.length == 3) {
                launchTerminal( args[0], Integer.parseInt( args[1] ), args[2] );
            } else {
                System.out.println( "Usage: java -jar BettorTerminal <host-name> <port> <track>" );
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private static void launchTerminal( String hostName, int port, String track ) throws IOException {
        Frame frame = new Frame();
        frame.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                System.exit( 0 );
            }
        } );
        frame.setLayout( new GridLayout() );
        SocketServerAccess serverAccess = SocketServerAccess.connectToRemoteServer(hostName, port);
        BettorTerminal bettorPresenter = new BettorTerminal( track, serverAccess );
        frame.add( bettorPresenter.getPanel() );
        frame.setSize( 400, 150 );
        frame.setVisible( true );
    }


    BettorTerminal( String trackName, SocketServerAccess serverAccess ) {
        _serverAccess = serverAccess;
        _serverAccess.resume();
        _view = new JPanel( new BorderLayout() );
        layoutControls( _view );
        serverAccess.setHandler( new TerminalMessageHandler() );
        sendRequest( ("DOWNLOAD " + trackName) );
    }


    private JPanel getPanel() {
        return _view;
    }


    private void sendRequest( String request ) {
        try {
            _serverAccess.sendRequest( request.getBytes() );
        } catch (IOException e) {
            reportException( e );
        }
    }


    private void reportException( Exception e ) {
        e.printStackTrace();
    }


    private void setCard( Card card ) {
        _card = card;
        this.setTrackName( card.getTrack() );
        int[] openRaces = new int[card.getNumRaces()];
        for (int i = 0; i < openRaces.length; i++) {
            openRaces[i] = (i+1);
        }
        this.setOfferedRaces( openRaces );
        selectRace( openRaces[0] );
    }


    private void selectRace( int raceNum ) {
        _raceNum = raceNum;
        Race race = _card.getRace( raceNum );
        ArrayList<String> horses = new ArrayList<String>();
        for (int i = 0; i < race.getNumHorses(); i++) {
            horses.add( (i+1) + " " + race.getHorse( i+1 ).getName() );
        }
        this.setOfferedHorses( horses.toArray( new String[ horses.size() ] ) );
        this.setOfferedPools( new String[] { "Win", "Place", "Show" } );
    }


    private void layoutControls( JPanel view ) {
        view.add( BorderLayout.NORTH, _trackLabel = new JLabel( "No track specified", JLabel.CENTER ) );
        JPanel contents = new JPanel( new GridLayout( 4, 2 ) );
        view.add( BorderLayout.CENTER, contents );
        contents.add( new JLabel( "Race:", JLabel.RIGHT ) );
        contents.add( _raceComboBox = new JComboBox() );
        contents.add( new JLabel( "Horse:", JLabel.RIGHT ) );
        contents.add( _horseComboBox = new JComboBox() );
        contents.add( new JLabel( "Pool:", JLabel.RIGHT ) );
        contents.add( _poolComboBox = new JComboBox() );
        contents.add( new JLabel( "Bet amount:", JLabel.RIGHT ) );
        contents.add( _betAmountField = new JTextField( "0" ) );
        JButton submitButton = new JButton( "Submit Bet" );
        view.add( BorderLayout.SOUTH, submitButton );

        _raceComboBox.addItemListener( new ItemListener() {
            public void itemStateChanged( ItemEvent e ) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                selectRace( Integer.parseInt( e.getItem().toString() ) );
            }
         } );

        submitButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                submitBet( getSelectedHorse(), getSelectedPool(), getBetAmount() );
            }
        } );
    }


    private int getSelectedHorse() {
        return _horseComboBox.getSelectedIndex() + 1;
    }


    private double getBetAmount() {
        return Double.parseDouble( _betAmountField.getText() );
    }


    private String getSelectedPool() {
        return _poolComboBox.getSelectedItem().toString();
    }


    private void submitBet( int horseNum, String poolName, double betAmount ) {
        sendRequest( "BET " + _card.getTrack() + ' ' + poolName.toUpperCase() + '-' + _raceNum + ' ' + horseNum + ' ' + betAmount );
    }


    private void setOfferedHorses( String[] strings ) {
        _horseComboBox.removeAllItems();
        for (int i = 0; i < strings.length; i++) {
            _horseComboBox.addItem( strings[i] );
        }
    }


    private void setOfferedPools( String[] strings ) {
        _poolComboBox.removeAllItems();
        for (int i = 0; i < strings.length; i++) {
            _poolComboBox.addItem( strings[i] );
        }
    }


    private void setOfferedRaces( int[] openRaces ) {
        _raceComboBox.removeAllItems();
        for (int i = 0; i < openRaces.length; i++) {
            int openRace = openRaces[i];
            _raceComboBox.addItem( new Integer( openRace ) );
        }
    }


    private void setTrackName( String trackName ) {
        _trackLabel.setText( "Welcome to " + trackName );
    }


    class TerminalMessageHandler implements ClientMessageHandler {

        public boolean handleMessage( byte[] bytes ) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
                ObjectInputStream ois = new ObjectInputStream( bais );
                Object result = ois.readObject();
                if (result instanceof Card) {
                    setCard( (Card) result );
                } else {
                    reportException( new RuntimeException( "Received " + result + " instead of a Card" ) );
                }
            } catch (IOException e) {
                reportException( e );
            } catch (ClassNotFoundException e) {
                reportException( e );
            }
            return true;
        }
    }
}
