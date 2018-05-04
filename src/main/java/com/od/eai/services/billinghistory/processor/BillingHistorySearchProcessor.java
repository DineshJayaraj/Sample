package com.od.eai.services.billinghistory.processor;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import com.od.eai.services.subscriptions.beans.search.ElasticRequest;
import com.od.eai.services.subscriptions.beans.search.ElasticResponse;
import com.od.eai.services.subscriptions.beans.search.BillingHistorySearchResponse;
import org.apache.camel.Properties;
import org.apache.camel.component.properties.PropertiesComponent;

public class BillingHistorySearchProcessor {

  public void processElasticRequest(
          @Header("contractNumber") String contractNumber,
          Exchange exchange
  ) throws Exception {
    ElasticRequest request = new ElasticRequest();

    PropertiesComponent props = exchange.getContext().getComponent("properties", PropertiesComponent.class);
    String pageSize = props.parseUri("{{pageSize}}");

    request.setSize(pageSize);
    request.query.match.serviceContractNumber = contractNumber;

    exchange.getOut().setBody(request);
  }

  public void processElasticResponse(
          @Body ElasticResponse elasticResponse,
          Exchange exchange
  ) {

    BillingHistorySearchResponse billingHistorySearchResponse = new BillingHistorySearchResponse();
    billingHistorySearchResponse.setBillingHistoryResponse(new BillingHistorySearchResponse.Inner());

    if (elasticResponse.getHits() != null) {
      elasticResponse.getHits().getHits().forEach(h -> {
        BillingHistorySearchResponse.BillingHistoryRecord record = new BillingHistorySearchResponse.BillingHistoryRecord();
        if (h.getSource() != null && h.getSource().getBillingHistoryRequest() != null) {
          record.setCustomer(h.getSource().getBillingHistoryRequest().getCustomer());
          record.setInvoice(h.getSource().getBillingHistoryRequest().getInvoice());

          billingHistorySearchResponse.getBillingHistoryResponse().getBillingHistoryRecord().add(record);
        }
      });
    }

    exchange.getOut().setBody(billingHistorySearchResponse);
  }
}
