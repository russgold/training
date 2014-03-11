package com.meterware.io; /********************************************************************************************************************
 * $Id: com.meterware.io.MessageIterator.java,v 1.1 2004/09/27 00:54:55 russgold Exp $
 *
 * Copyright (c) 2004, Russell Gold
 *
 *******************************************************************************************************************/
import java.util.Iterator;
import java.nio.ByteBuffer;

/**
 *
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 **/
class MessageIterator implements Iterator {

    private ByteBuffer _buffer;

    MessageIterator( byte[] bytes ) {
        _buffer = ByteBuffer.wrap( bytes );
    }


    public boolean hasNext() {
        return _buffer.hasRemaining();
    }


    public Object next() {
        int length = _buffer.getInt();
        byte[] message = new byte[length];
        _buffer.get( message );
        return message;
    }


    public void remove() {
        throw new java.lang.UnsupportedOperationException();
    }

}
