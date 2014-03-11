package com.meterware.totalizer;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2004, Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************************************************/

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

/**
 *
 * @author <a href="mailto:russgold@meterware.com">Russell Gold</a>
 **/
public class Track {

    private static Hashtable<String,Track> _tracks = new Hashtable<String,Track>();

    private String      _name;
    private Hashtable<Date,Card>   _cards = new Hashtable<Date,Card>();
    private Hashtable<String,BettingPool>   _pools = new Hashtable<String,BettingPool>();
    private Card        _currentCard;
    private int         _currentRace;


    public static Card loadCard( String fileName ) throws IOException {
        Card card = Card.load( fileName );
        Track track = getTrack( card.getTrack() );
        track.setCard( card.getDate(), card );
        return card;
    }


    public static Track getTrack( String name ) {
        Track track = _tracks.get( name.toLowerCase() );
        if (track == null) {
            track = new Track( name );
            defineTrack( track );
        }
        return track;
    }


    public String getName() {
        return _name;
    }


    public Card getCard( Date date ) {
        return _cards.get( date );
    }


    public Card getCurrentCard() {
        return _currentCard;
    }


    public BettingPool getPool( Card card, String poolId ) {
        BettingPool pool = _pools.get( poolId );
        if (pool == null) {
            pool = BettingPool.create( poolId );
            _pools.put( poolId, pool );
        }
        return pool;
    }


    private void setCard( Date date, Card card ) {
        _cards.put( date, card );
        _currentCard = card;
    }


    static void defineTrack( Track track ) {
        _tracks.put( track.getName().toLowerCase(), track );
        _tracks.put( "any", track );
    }


    private Track( String name ) {
        _name = name;
    }


    public static void clear() {
        _tracks.clear();
    }


    public double getPayoutPortion() {
        return 0.90;
    }


    public void setCurrentRace( int raceNum ) {
        _currentRace = raceNum;
    }


    public int currentRace() {
        return _currentRace;
    }


    public boolean hasEnoughBets() {
        BettingPool pool = getPool( _currentCard, "WIN-"+ _currentRace );
        int numHorsesBetOn = 0;
        int numHorses = _currentCard.getRace( _currentRace ).getNumHorses();
        for (int i = 0; i < numHorses; i++) {
            if (pool.getBet( i ) > 0) numHorsesBetOn++;
        }
        boolean result = numHorsesBetOn*2 > numHorses;
        return result;
    }
}
