package com.od.eai.services.billinghistory.util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * This processor gets the error message and set it as value in MessageHeaderConstants.ERROR_MESSAGE header.
 * @author
 *
 */
public class ExceptionMessageProcessor implements Processor {

    @Override
    public void process(Exchange exchg) throws Exception {
        Exception cause = exchg.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        exchg.getIn().setHeader(MessageHeaderConstants.ERROR_MESSAGE, cause.getMessage());
    }

}
