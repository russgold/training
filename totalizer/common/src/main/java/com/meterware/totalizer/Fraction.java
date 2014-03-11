package com.meterware.totalizer;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2005, Russell Gold
 *
 *******************************************************************************************************************/

import java.util.logging.Logger;

/**
 * Based on <a href="http://www.hpmuseum.org/cgi-sys/cgiwrap/hpmuseum/articles.cgi?read=26"> an algorithm</a>
 * designed by "rupert"
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 */
public class Fraction {

    private int _a;
    private double _x;
    private double _y;

    private int _n;
    private int _d;

    private static final Fraction FRACTION_0 = new Fraction();
    public static final double DEFAULT_ACCURACY = 0.000001;

    private static Logger _logger = Logger.getLogger( Fraction.class.getName() );
    public static final int DEFAULT_ITERATIONS = 8;


    public static Fraction toFraction( double value ) {
        return toFraction( value, DEFAULT_ACCURACY, DEFAULT_ITERATIONS );
    }


    public static Fraction toFraction( double value, double delta, int maxIterations ) {
        Fraction older    = null;
        Fraction previous = FRACTION_0;
        Fraction fraction = new Fraction( value );

        for (int i = 0; i < maxIterations; i++) {
            _logger.fine( "Approximation " + i + " is " + fraction + " which is " + fraction.value() );
            if (Math.abs( value - fraction.value()) < delta) break;
            older = previous;
            previous = fraction;
            fraction = new Fraction( previous, older );
        }
        return fraction;
    }


    public int getNumerator() {
        return _n;
    }


    public int getDenominator() {
        return _d;
    }


    public String toString( String separator ) {
        return _n + separator + _d;
    }


    private Fraction() {  // constructor for value #0
        _n = 1;
        _d = 0;
    }


    private Fraction( double value ) { // constructor for value #1
        _a = (int) value;
        _x = 1;
        _y = value - _a;
        _n = _a * 1 + 0;
        _d = _a * 0 + 1;
    }


    private Fraction( Fraction previous, Fraction earlier ) {  // constructor for value #2+
        _a = (int) (previous._x / previous._y);
        _x = previous._y;
        _y = previous._x - _a * previous._y;
        _n = _a * previous._n + earlier._n;
        _d = _a * previous._d + earlier._d;
    }


    private float value() {
        return ((float) _n) / ((float) _d);
    }


    public String toString() {
        return toString( "/" );
    }
}
