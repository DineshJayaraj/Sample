package com.od.eai.services.billinghistory.processor;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import com.od.eai.services.subscriptions.beans.*;

public class BillingHistoryElasticProcessor {

    private static Logger log = LoggerFactory.getLogger(BillingHistoryRequest.class);

    public void billingHistoryElasticSearchReq(Exchange exchange) throws ParseException {

        BillingHistoryRequest billingHistoryRequest = exchange.getIn().getBody(BillingHistoryRequest.class);
        TransactionHeader transHeader  = billingHistoryRequest.getBillingHistoryRequest().getTransactionHeader();
        exchange.setProperty("transHeader",transHeader);

        if (billingHistoryRequest.getBillingHistoryRequest().getCustomer() != null &&
                billingHistoryRequest.getBillingHistoryRequest().getCustomer().getPaymentDetails() != null &&
                billingHistoryRequest.getBillingHistoryRequest().getCustomer().getPaymentDetails().getBillingAgreementId() != null) {
            billingHistoryRequest.getBillingHistoryRequest().getCustomer().getPaymentDetails().setBillingAgreementId("");
        }

//        ElasticsearchComponent elasticsearchComponent = new ElasticsearchComponent();
//        elasticsearchComponent.setHostAddresses("10.94.98.50:8443");
//        elasticsearchComponent.setUser("es_admin_eai");
//        elasticsearchComponent.setPassword("eainonprod");
//        elasticsearchComponent.setEnableSSL(true);
//        exchange.getContext().addComponent("elasticsearch-rest", elasticsearchComponent);

        exchange.getOut().setBody(billingHistoryRequest);
    }

    public void billingHistoryElasticSearchResp(Exchange exchange) {
        ElasticSearchResponse elasticSearchResponse = exchange.getIn().getBody(ElasticSearchResponse.class);

        BillingHistoryResponse billingHistoryResponse = new BillingHistoryResponse();
        BillingHistoryResponse_ billingHistoryResponse_ = new BillingHistoryResponse_();

        TransactionHeader transactionHeader = (TransactionHeader)exchange.getProperty("transHeader");
        billingHistoryResponse_.setTransactionHeader(transactionHeader);

        TransactionStatus transactionStatus = new TransactionStatus();
        String status = elasticSearchResponse.getResult();
        Object temp = exchange.getIn().getHeader("CamelHttpResponseCode");
        transactionStatus.setCode(temp.toString());
        transactionStatus.setMessage(status);
        billingHistoryResponse_.setTransactionStatus(transactionStatus);

        billingHistoryResponse.setBillingHistoryResponse(billingHistoryResponse_);
        exchange.getIn().setBody(billingHistoryResponse);

    }

}