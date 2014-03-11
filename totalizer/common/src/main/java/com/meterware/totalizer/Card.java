package com.meterware.totalizer;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2004-2005, Russell Gold
 *
 *******************************************************************************************************************/
import com.meterware.xml.XmlSemantics;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Serializable;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 **/
public class Card implements Serializable {

    private ArrayList<Race> _races = new ArrayList<Race>();
    private String _track;
    private Date _date;


    public void setTrack( String track ) {
        _track = track;
    }


    public String getTrack() {
        return _track;
    }


    public void setDate( Date date ) {
        _date = date;
    }


    public Date getDate() {
        return _date;
    }


    public Race createRace() {
        Race race = new Race();
        _races.add( race );
        return race;
    }


    public int getNumRaces() {
        return _races.size();
    }


    public Race getRace( int raceNum ) {
        assert raceNum > 0 && raceNum <= getNumRaces() : "Race number must be in the range " + 1 + ".." + getNumRaces();

        return _races.get( raceNum-1 );
    }


    public boolean equals( Object obj ) {
        return getClass().equals( obj.getClass() ) && equals( (Card) obj );
    }

    private boolean equals( Card card ) {
        return _date.equals( card._date ) && _track.equals( card._track ) && _races.equals( card._races );
    }


    public static Card load( String fileName ) throws IOException {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new FileInputStream( fileName ) );
            return XmlSemantics.build(document, new Card(), fileName);
        } catch (SAXException e) {
            throw new IOException( e.toString() );
        } catch (ParserConfigurationException e) {
            throw new RuntimeException( "Unable to create a parser for XML documents: " + e );
        }
    }
}
