package com.meterware.totalizer;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2005, Russell Gold
 *
 *******************************************************************************************************************/

/**
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 */
public class DataItem {
    int _row;
    int _col;
    String _value;


    public DataItem( int row, int col, Object value ) {
        _row = row;
        _col = col;
        _value = (String) value;
    }


    public boolean equals( Object obj ) {
        if (!getClass().equals( obj.getClass())) return false;
        DataItem other = (DataItem) obj;
        return _row == other._row && _col == other._col && _value.equals( other._value );
    }


    public int hashCode() {
        return _row * 1000 + _col * 50 + _value.hashCode();
    }


    public String toString() {
        return "Item [" + _row + "," + _col + "," + _value + "]";
    }
}
