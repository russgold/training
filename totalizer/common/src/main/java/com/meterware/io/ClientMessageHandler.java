package com.meterware.io; /********************************************************************************************************************
 * $Id: com.meterware.io.ClientMessageHandler.java,v 1.1.1.1 2004/09/19 19:47:40 russgold Exp $
 *
 * Copyright (c) 2004, Russell Gold
 *
 *******************************************************************************************************************/

/**
 *
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 **/
public interface ClientMessageHandler {

    /**
     * Handles the specified message content. Returns false if the handling thread should exit.
     */
    boolean handleMessage( byte[] bytes );

}
