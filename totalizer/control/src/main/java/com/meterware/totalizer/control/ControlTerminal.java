package com.meterware.totalizer.control;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2005, Russell Gold
 *
 *******************************************************************************************************************/
import com.meterware.io.SocketServerAccess;
import com.meterware.io.ClientMessageHandler;
import com.meterware.totalizer.Card;
import com.meterware.totalizer.Race;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;

/**
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 */
public class ControlTerminal {

    private SocketServerAccess _serverAccess;
    private JPanel _view;
    private Card _card;
    private int[] _openRaces;
    private int _raceNum;
    private int _winner;
    private int _placer;

    JFileChooser _fileChooser;
    JLabel _trackLabel;
    JButton _submitButton;
    JComboBox _raceComboBox;
    JComboBox _winComboBox;
    JComboBox _placeComboBox;
    JComboBox _showComboBox;
    int _show;
    JButton _displayButton;

    public static void main( String[] args ) {
        try {
            if (args.length == 0) {
                launchTerminal( "localhost", 5001 );
            } else if (args.length == 2) {
                launchTerminal( args[0], Integer.parseInt( args[1] ) );
            } else {
                System.out.println( "Usage: java -jar ControlTerminal <host-name> <port>" );
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private static void launchTerminal( String hostName, int port ) throws IOException {
        Frame frame = new Frame();
        frame.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                System.exit( 0 );
            }
        } );
        frame.setLayout( new GridLayout() );
        SocketServerAccess serverAccess = SocketServerAccess.connectToRemoteServer(hostName, port);
        ControlTerminal controlTerminal = new ControlTerminal( serverAccess );
        frame.add( controlTerminal.getPanel() );
        frame.setSize( 400, 200 );
        frame.setVisible( true );
    }


    public ControlTerminal( SocketServerAccess serverAccess ) {
        _serverAccess = serverAccess;
        _serverAccess.resume();
        _serverAccess.setHandler( new TerminalMessageHandler() );
        _view = new JPanel( new BorderLayout() );
        _view.add( BorderLayout.NORTH, createLoadPanel() );
        _view.add( BorderLayout.CENTER, createControlPanel() );
    }


    private JPanel getPanel() {
        return _view;
    }


    private void updateOfferedRaces() {
        for (int i = 0; i < _openRaces.length; i++) {
            int openRace = _openRaces[i];
            if (openRace == _raceNum) {
                int[] races = new int[_openRaces.length-1];
                System.arraycopy( _openRaces, 0, races, 0, i );
                System.arraycopy( _openRaces, i+1, races, i, races.length-i );
                _openRaces = races;
                this.setOfferedRaces( _openRaces );
                break;
            }
        }
    }


    private void selectPlace( int horseNum ) {
        _placer = horseNum;
        Race race = _card.getRace( _raceNum );
        ArrayList horses = new ArrayList();
        for (int i = 0; i < race.getNumHorses(); i++) {
            if (_winner == i+1) continue;
            if (_placer == i+1) continue;
            horses.add( (i+1) + " " + race.getHorse( i+1 ).getName() );
        }
        setPossibleShowers( (String[]) horses.toArray( new String[ horses.size() ] ) );
    }


    private void selectWinner( int horseNum ) {
        _winner = horseNum;
        Race race = _card.getRace( _raceNum );
        ArrayList horses = new ArrayList();
        for (int i = 0; i < race.getNumHorses(); i++) {
            if (_winner == i+1) continue;
            horses.add( (i+1) + " " + race.getHorse( i+1 ).getName() );
        }
        this.setPossiblePlacers( (String[]) horses.toArray( new String[ horses.size() ] ) );
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
        _openRaces = new int[ card.getNumRaces() ];
        for (int i = 0; i < _openRaces.length; i++) {
            _openRaces[i] = (i+1);
        }
        this.setOfferedRaces( _openRaces );
    }


    private void selectRace( int raceNum ) {
        _raceNum = raceNum;
        Race race = _card.getRace( raceNum );
        ArrayList horses = new ArrayList();
        for (int i = 0; i < race.getNumHorses(); i++) {
            horses.add( (i+1) + " " + race.getHorse( i+1 ).getName() );
        }
        this.setPossibleWinners( (String[]) horses.toArray( new String[ horses.size() ] ) );
    }


    private void setPossibleShowers( String[] horses ) {
        _showComboBox.setEnabled( true );
        _showComboBox.removeAllItems();
        _showComboBox.addItem( "-- select show --" );
        for (int i = 0; i < horses.length; i++) {
            _showComboBox.addItem( horses[i] );
        }
        _submitButton.setEnabled( false );
    }


    private void disableShow() {
        _showComboBox.setEnabled( false );
        _showComboBox.removeAllItems();
    }


    private void disablePlace() {
        _placeComboBox.setEnabled( false );
        _placeComboBox.removeAllItems();

        disableShow();
    }


    private void setPossiblePlacers( String[] horses ) {
        _placeComboBox.setEnabled( true );
        _placeComboBox.removeAllItems();
        _placeComboBox.addItem( "-- select place --" );
        for (int i = 0; i < horses.length; i++) {
            _placeComboBox.addItem( horses[i] );
        }

        disableShow();
    }


    private void setPossibleWinners( String[] horses ) {
        _winComboBox.setEnabled( true );
        _winComboBox.removeAllItems();
        _winComboBox.addItem( "-- select winner --" );
        for (int i = 0; i < horses.length; i++) {
            _winComboBox.addItem( horses[i] );
        }

        disablePlace();
        _displayButton.setEnabled( true );
    }


    void setOfferedRaces( int[] openRaces ) {
        _raceComboBox.setEnabled( true );
        _raceComboBox.removeAllItems();
        for (int i = 0; i < openRaces.length; i++) {
            int openRace = openRaces[i];
            _raceComboBox.addItem( new Integer( openRace ) );
        }
    }


    void setTrackName( String track ) {
        _trackLabel.setText( "Welcome to " + track );
    }


    Component createHorseSelectionPanel() {
        JPanel contents = new JPanel( new GridLayout( 5, 2 ) );
        contents.add( new JLabel( "Race: ", JLabel.RIGHT ) );
        contents.add( _raceComboBox = new JComboBox() );
        contents.add( new JLabel( "Win:", JLabel.RIGHT ) );
        contents.add( _winComboBox = new JComboBox() );
        contents.add( new JLabel( "Place:", JLabel.RIGHT ) );
        contents.add( _placeComboBox = new JComboBox() );
        contents.add( new JLabel( "Show:", JLabel.RIGHT ) );
        contents.add( _showComboBox = new JComboBox() );
        contents.add( _displayButton = new JButton( "Display Odds" ) );
        contents.add( _submitButton = new JButton( "Post Results") );

        _raceComboBox.setEnabled( false );
        _winComboBox.setEnabled( false );
        _placeComboBox.setEnabled( false );
        _showComboBox.setEnabled( false );
        _displayButton.setEnabled( false );
        _submitButton.setEnabled( false );

        _raceComboBox.addItemListener( new NumericItemListener() {
            void reportChange( int i ) {
                selectRace( i );
            }
        });

        _winComboBox.addItemListener( new NumericItemListener() {
            void reportChange( int i ) {
                selectWinner( i );
            }
        });

        _placeComboBox.addItemListener( new NumericItemListener() {
            void reportChange( int i ) {
                selectPlace( i );
            }
        });

        _showComboBox.addItemListener( new NumericItemListener() {
            void reportChange( int i ) {
                _show = i;
                _submitButton.setEnabled( true );
            }
        });

        _displayButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                sendRequest( "DISPLAY " + _card.getTrack() + ' ' + _raceNum );
            }
        } );

        _submitButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                sendRequest( "RESULTS " + _card.getTrack() + " " + _raceNum + " " + _winner + " " + _placer + " " + _show );
                updateOfferedRaces();
            }
        } );
        return contents;
    }


    Component createControlPanel() {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.add( BorderLayout.NORTH, _trackLabel = new JLabel( "Choose a track", JLabel.CENTER ) );
        panel.add( BorderLayout.CENTER, createHorseSelectionPanel() );
        return panel;
    }


    Component createLoadPanel() {
        final JPanel panel = new JPanel();
        JButton button = new JButton( "Load Card..." );
        panel.add( button );
        _fileChooser = new JFileChooser( new File( "." ) );
        _fileChooser.setFileFilter( new FileFilter() {
            public boolean accept( File f ) {
                return f.isDirectory() || f.getName().endsWith( ".xml" );
            }

            public String getDescription() {
                return "XML files";
            }
        } );
        button.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if (JFileChooser.APPROVE_OPTION == _fileChooser.showOpenDialog( panel ) ) {
                    File file = _fileChooser.getSelectedFile();
                    sendRequest( ("LOAD " + file.getAbsolutePath()) );
                }
            }
        } );
        return panel;
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

    abstract static class NumericItemListener implements ItemListener {

        public void itemStateChanged( ItemEvent e ) {
            if (e.getStateChange() == ItemEvent.DESELECTED) return;
            String valueString = e.getItem().toString();
            if (!Character.isDigit( valueString.charAt( 0 ) )) return;
            if (valueString.indexOf( ' ' ) > 0) valueString = valueString.substring( 0, valueString.indexOf( ' ' ) );
            reportChange( Integer.parseInt( valueString ) );
        }

        abstract void reportChange( int i );
    }
}
