package com.meterware.totalizer.display;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2005, Russell Gold
 *
 *******************************************************************************************************************/
import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 */
public class ToteBoardLayout {

    private JPanel _panel;
    private boolean _showOdds;
    private Font _font;
    private Font _titleFont;
    private String[][] _data = new String[1][];
    private Point[][] _positions;
    private int[] _left;


    public void setFont( Font font ) {
        _font = font;
    }


    public void setTitleFont( Font font ) {
        _titleFont = font;
    }


    public String getTitle() {
        return (_showOdds ? "Odds" : "Results") + " for Race " + _data[0][1] + " at " + _data[0][0];
    }


    public Point getTitlePosition() {
        return new Point( (_panel.getWidth() - getTitleFontMetrics().stringWidth( getTitle() )) / 2,
                          2 * getTitleFontMetrics().getHeight()  );
    }


    private FontMetrics getTitleFontMetrics() {
        return _panel.getFontMetrics( _titleFont );
    }


    public Point getItemPosition( int row, int column ) {
        if (_positions == null) {
            int numColumns = 0;
            _positions = new Point[_data.length][];
            for (int i = 1; i < _data.length; i++) {  // ignore the title data
                _positions[i] = new Point[_data[i].length];
                numColumns = Math.max( numColumns, _data[i].length );
            }
            int[] widths = new int[ numColumns ];
            _left = new int[ numColumns ];
            int right = 0;
            for (int i = 1; i < _data.length; i++) {
                for (int j = 0; j < _data[i].length; j++) {
                    if (_data[i][j] == null) continue;
                    if (_showOdds && j == 2 && _data[i][j].indexOf( '-' ) >= 0) {
                        _left[j] = Math.max( _left[j], _panel.getFontMetrics( _font ).stringWidth( _data[i][j].substring( 0, _data[i][j].indexOf( '-' ) ) ) );
                        right = Math.max( right, _panel.getFontMetrics( _font ).stringWidth( _data[i][j].substring( _data[i][j].indexOf( '-' ) ) ) );
                        widths[j] = _left[j] + right;
                    } else {
                        widths[j] = Math.max( widths[j], _panel.getFontMetrics( _font ).stringWidth( _data[i][j] ) );
                    }
                }
            }
            int totalWidth = widths[0];
            for (int i = 1; i < widths.length; i++) {
                totalWidth += widths[i] + _panel.getFontMetrics( _font ).stringWidth( "M" );
            }
            int[] colX = new int[ numColumns ];
            int width = _panel.getWidth();
            colX[0] = (width - totalWidth) / 2;
            for (int i = 1; i < widths.length; i++) {
                colX[i] = colX[i-1] + widths[i-1] + _panel.getFontMetrics( _font ).stringWidth( "M" );
            }
            int rowY[] = new int[ _positions.length ];
            rowY[1] = 3 * getTitleFontMetrics().getHeight() + _panel.getFontMetrics( _font ).getHeight();
            for (int i = 2; i < rowY.length; i++) {
                rowY[i] = rowY[i-1] + _panel.getFontMetrics( _font ).getHeight();
            }
            for (int i = 1; i < _data.length; i++) {
                for (int j = 0; j < _data[i].length; j++) {
                    if (_data[i][j] == null) continue;
                    _positions[i][j] = new Point( adjustedX( j, colX[j], widths[j], _data[i][j] ), rowY[i] );
                }
            }


        }
        return _positions[ row ][ column ];
    }


    private int adjustedX( int column, int colX, int colWidth, String value ) {
        if (column == 1) return colX;
        if (_left[column] == 0) {
            return colX + colWidth - _panel.getFontMetrics( _font ).stringWidth( value );
        } else {
            int left = _panel.getFontMetrics( _font ).stringWidth( value.substring( 0, value.indexOf( '-' ) ) );
            return colX + _left[column] - left;
        }
    }


    void draw( Graphics g ) {
        g.setFont( _titleFont );
        Point p = getTitlePosition();
        g.drawString( getTitle(), (int) p.getX(), (int) p.getY() );

        g.setFont( _font );
        for (int i = 1; i < _data.length; i++) {
            for (int j = 0; j < _data[i].length; j++) {
                if (_data[i][j] != null) {
                    Point p2 = getItemPosition( i, j );
                    g.drawString( _data[i][j], (int) p2.getX(), (int) p2.getY() );
                }
            }
        }
    }


    public ToteBoardLayout( JPanel panel, boolean showOdds ) {
        _panel = panel;
        _showOdds = showOdds;
    }


    public void setValue( int row, int column, String value ) {
        if (value == null) return;
        _positions = null;

        if (_data.length <= row) {
            String[][] newData = new String[row+1][];
            System.arraycopy( _data, 0, newData, 0, _data.length );
            _data = newData;
        }

        if (_data[ row ] == null) {
            _data[ row ] = new String[ column+1 ];
        } else if (_data[ row ].length <= column) {
            String[] newRow = new String[ column+1 ];
            System.arraycopy( _data[ row ], 0, newRow, 0, _data[ row ].length );
            _data[ row ] = newRow;
        }

        _data[ row ][ column ] = value;
    }
}
