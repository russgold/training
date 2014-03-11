package com.meterware.totalizer;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2004-2005, Russell Gold
 *
 *******************************************************************************************************************/
import java.util.ArrayList;

/**
 *
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 **/
public class BettingPool {

    public static int WIN   = 0;
    public static int PLACE = 1;
    public static int SHOW  = 2;

    private ArrayList<Double> _bets     = new ArrayList<Double>();
    private int _raceNum;
    private int _poolType;


    public void addBet( int horseNum, double amount ) {
        _bets.set( horseNum, amount + getBet( horseNum ));
    }


    public double getBet( int horseNum ) {
        while (_bets.size() <= horseNum) _bets.add( 0.0 );
        return _bets.get(horseNum);
    }


    public double getTotalBets() {
        double totalBets = 0;
        for (int i = 0; i < _bets.size(); i++) {
            totalBets += getBet(i);
        }
        return totalBets;
    }


    public boolean isWinPool() {
        return _poolType == WIN;
    }


    public static BettingPool create( String poolId ) {
        int raceNum = Integer.parseInt( poolId.substring( poolId.indexOf( '-' ) +1 ) );
        String type = poolId.substring( 0, poolId.indexOf( '-' ) );
        if (type.equalsIgnoreCase( "win" ) ) {
            return new BettingPool( WIN, raceNum );
        } else if (type.equalsIgnoreCase( "place" ) ) {
            return new BettingPool( PLACE, raceNum );
        } else if (type.equalsIgnoreCase( "show" ) ) {
            return new BettingPool( SHOW, raceNum );
        } else {
            throw new RuntimeException( "No such pool type: " + poolId );
        }
    }


    private BettingPool( int poolType, int raceNum ) {
        _poolType = poolType;
        _raceNum = raceNum;
    }


    public int getRace() {
        return _raceNum;
    }
}
