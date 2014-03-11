package com.meterware.totalizer;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2004-2005, Russell Gold
 *
 *******************************************************************************************************************/
import java.util.ArrayList;
import java.io.Serializable;

/**
 *
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 **/
public class Race implements Serializable {

    private ArrayList<Horse> m_horses = new ArrayList<Horse>();

    public void addHorse( String name, double initialOdds ) {
        m_horses.add( new Horse( name, initialOdds ) );
    }


    public Horse createHorse() {
        Horse horse = new Horse();
        m_horses.add( horse );
        return horse;
    }

    public int getNumHorses() {
        return m_horses.size();
    }


    public Horse getHorse( int i ) {
        assert i > 0 && i <= m_horses.size();

        return m_horses.get( i-1 );
    }


    public double getInitialOdds( int i ) {
        return getHorse( i ).getInitialOdds();
    }


    public String getHorseName( int i ) {
        return getHorse( i ).getName();
    }


    public int getHorseNum( String horse ) {
        for (int i = 1; i <= m_horses.size(); i++) {
            if (horse.equalsIgnoreCase( getHorseName( i ) )) return i;
        }
        throw new RuntimeException( "No horse named '" + horse + "' in the race" );
    }


    public boolean equals( Object obj ) {
        return getClass().equals( obj.getClass() ) && equals( (Race) obj );
    }


    private boolean equals( Race race ) {
        return m_horses.equals( race.m_horses );
    }


    static public class Horse implements Serializable {
        private String _name;
        private double _initialOdds;


        public Horse() {}


        public Horse( String name, double initialOdds ) {
            _initialOdds = initialOdds;
            _name = name;
        }


        public void setOdds( String initialOdds ) {
            int colonPos = initialOdds.indexOf( '-' );
            if (colonPos < 0) colonPos = initialOdds.indexOf( ':' );
            if (colonPos > 0) {
                _initialOdds = Double.parseDouble( initialOdds.substring( 0, colonPos ) ) /
                               Double.parseDouble( initialOdds.substring( colonPos+1 ));
            }
        }


        public double getInitialOdds() {
            return _initialOdds;
        }


        public void setName( String name ) {
            _name = name;
        }


        public String getName() {
            return _name;
        }


        public boolean equals( Object obj ) {
            return getClass().equals( obj.getClass() ) && equals( (Horse) obj );
        }


        private boolean equals( Horse horse ) {
            return _name.equals( horse._name ) && _initialOdds == horse._initialOdds;
        }
    }


}
