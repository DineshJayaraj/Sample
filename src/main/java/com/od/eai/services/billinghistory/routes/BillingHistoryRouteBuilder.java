package com.od.eai.services.billinghistory.routes;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import com.od.eai.services.subscriptions.beans.*;
import com.od.eai.services.subscriptions.beans.search.ElasticRequest;
import com.od.eai.services.subscriptions.beans.search.ElasticResponse;
import com.od.eai.services.billinghistory.processor.BillingHistoryAOPSProcessor;
import com.od.eai.services.billinghistory.processor.BillingHistoryElasticProcessor;
import com.od.eai.services.billinghistory.processor.BillingHistorySearchProcessor;
import com.od.eai.services.billinghistory.processor.BillingHistoryTransportClient;

import com.od.eai.framework.core.dispatch.Configurator;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.camel.http.common.HttpOperationFailedException;
import com.od.eai.services.billinghistory.util.ExceptionMessageProcessor;
import org.springframework.jdbc.BadSqlGrammarException;

import javax.net.ssl.SSLHandshakeException;

public class BillingHistoryRouteBuilder extends RouteBuilder {

	private BillingHistoryAOPSProcessor billingHistoryAOPSProcessor;
	private BillingHistoryElasticProcessor billingHistoryElasticProcessor;
	private BillingHistorySearchProcessor billingHistorySearchProcessor;
	private BillingHistoryTransportClient billingHistoryTransportClient;


	@Override
	public void configure() {
		onException(HttpHostConnectException.class)
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						TransactionStatus transactionStatus = new TransactionStatus();
						if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null) {
							Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpHostConnectException.class);
							transactionStatus.setCode("404");
							transactionStatus.setMessage(exception.getMessage());
						}
						exchange.getOut().setBody(transactionStatus);
					}
				})
				.handled(true)
				.marshal().json(JsonLibrary.Jackson, TransactionStatus.class);

		onException(HttpOperationFailedException.class )
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						TransactionStatus transactionStatus = new TransactionStatus();
						if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null) {
							Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
							transactionStatus.setCode("400");
							transactionStatus.setMessage(exception.getMessage());
						}
						exchange.getOut().setBody(transactionStatus);
					}
				})
				.handled(true)
				.marshal().json(JsonLibrary.Jackson, TransactionStatus.class);

		onException(JsonMappingException.class )
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						TransactionStatus transactionStatus = new TransactionStatus();
						if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null) {
							Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, JsonMappingException.class);
							transactionStatus.setCode("400");
							transactionStatus.setMessage(exception.getMessage());
						}
						exchange.getOut().setBody(transactionStatus);
					}
				})
				.handled(true)
				.marshal().json(JsonLibrary.Jackson, TransactionStatus.class);

		onException(SSLHandshakeException.class )
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						TransactionStatus transactionStatus = new TransactionStatus();
						if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null) {
							Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, SSLHandshakeException.class);
							transactionStatus.setCode("500");
							transactionStatus.setMessage(exception.getMessage());
						}
						exchange.getOut().setBody(transactionStatus);
					}
				})
				.handled(true)
				.marshal().json(JsonLibrary.Jackson, TransactionStatus.class);

		onException(BadSqlGrammarException.class )
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						TransactionStatus transactionStatus = new TransactionStatus();
						if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null) {
							Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, BadSqlGrammarException.class);
							transactionStatus.setMessage(exception.getMessage());
						}
						exchange.getOut().setBody(transactionStatus);
					}
				})
				.handled(true)
				.marshal().json(JsonLibrary.Jackson, TransactionStatus.class);

		onException(Exception.class)
				.process(new ExceptionMessageProcessor())
				.to("log:com.od.eai.services.billinghistory.BillingHistoryRouteBuilder?level=ERROR&showBodyType=false&showBody=false&showExchangeId=true&showProperties=true&showHeaders=true&showException=true&showCaughtException=true&showStackTrace=true&multiline=true&maxChars=500000").id("error-exception-log")
				.handled(true);

		restConfiguration().enableCORS(true)
				.corsHeaderProperty("Access-Control-Allow-Origin","*")
				.corsHeaderProperty("Access-Control-Allow-Headers","Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");

		restConfiguration().component("servlet")
				.dataFormatProperty("prettyPrint", "true")
				.dataFormatProperty("json.in.disableFeatures", "FAIL_ON_UNKNOWN_PROPERTIES")
				.dataFormatProperty("json.in.enableFeatures", "FAIL_ON_NUMBERS_FOR_ENUMS,USE_BIG_DECIMAL_FOR_FLOATS");

		//EAI health check URL
		rest("eaiapi/health").get().to("direct:eaihealthcheck");
		from("direct:eaihealthcheck")
				.transform().constant("Service UP !!");

		rest("/eaiapi/subscriptions/billingHistory").post().consumes("application/json").produces("application/json")
				.to("log:${body}")
				.to("direct:billinghistory");


		from("direct:billinghistory")
				.log("********** Message Recieved from EBS **********")
				.log(LoggingLevel.DEBUG, "Incoming-Message  -> ${body}")
				.unmarshal().json(JsonLibrary.Jackson,BillingHistoryRequest.class).id(Configurator.getStepId("billingHistoryReq"))
				.log(LoggingLevel.INFO, "Transaction-Id    -> ${in.body.billingHistoryRequest.transactionHeader.consumerTransactionId}")
				.log(LoggingLevel.INFO, "InvoiceNumber     -> ${in.body.billingHistoryRequest.invoice.invoiceNumber}")
				.log(LoggingLevel.INFO, "orderNumber       -> ${in.body.billingHistoryRequest.invoice.orderNumber}")
				.log(LoggingLevel.INFO, "ContractNumber    -> ${in.body.billingHistoryRequest.invoice.serviceContractNumber}")
				.choice()
				.when(simple("{{enable.AOPS}} == 'true'"))
					.log("=====  Request Recieved for AOPS  =====")
					.bean(billingHistoryAOPSProcessor, "billingHistoryStoredProcReq")

					.log("InvoiceNumber - ${body.invoiceNumber}").log("OrderNumber- ${body.orderNumber}").log("NetSale - ${body.netSale}")
					.log("TotalTax- ${body.totalTax}").log("GrossSales- ${body.grossSales}").log("InvoiceDate - ${body.invoiceDate}")
					.log("InvoiceTime- ${body.invoiceTime}").log("InvoiceStatus- ${body.invoiceStatus}").log("Tender - ${body.tender}")
					.log("Body - ${body.details}")

					.to("{{subscription.billing.history.as400StoredProc}}"+"(VARCHAR ${body.invoiceNumber},VARCHAR ${body.orderNumber},"
							+ "VARCHAR ${body.netSale},VARCHAR ${body.totalTax},VARCHAR ${body.grossSales},VARCHAR ${body.invoiceDate},VARCHAR ${body.invoiceTime},"
							+ "VARCHAR ${body.invoiceStatus},VARCHAR ${body.tender},VARCHAR ${body.details},OUT CHAR status)?dataSource="+"{{subscription.billing.history.as400dataSourceName}}")

					.log("********** SUCCESS - AOPS Procedure Call  **********")
					.convertBodyTo(Map.class)
					.log("AOPS-Response -> ${body}")
					.bean(billingHistoryAOPSProcessor, "billingHistoryStoredProcResp")
					.marshal().json(JsonLibrary.Jackson,BillingHistoryResponse.class)
					.setProperty("transHeader", constant(null))
				.otherwise()
					.log("=====  Request Recieved for ElasticSearch POST  =====")
					.bean(billingHistoryElasticProcessor, "billingHistoryElasticSearchReq")
					.marshal().json(JsonLibrary.Jackson,BillingHistoryRequest.class)
					.convertBodyTo(String.class)

					.log("=====  BEF-ES  =====")
					.bean(billingHistoryTransportClient, "ElasticSearchReq")
					.log("=====  AFT-ES  =====")

//					.to("{{elasticSearch.post.url}}?authMethod=Basic&authUsername={{elastic.username}}&authPassword={{elastic.password}}&httpClient.authenticationPreemptive=true")		// For Https Connection
//					.to("{{elasticSearch.post.url}}?bridgeEndpoint=true")	// For Http4 Connection
//					.to("elasticsearch://es-nonprod-dev-sec-2?transportAddresses=10.94.98.46:8443&operation=BULK_INDEX&indexName=subscriptionbillinghistory")
//					.to("elasticsearch-rest://es-nonprod-dev-sec-2?hostAddresses=10.94.98.46:8443,10.94.98.50:8443&user={{elastic.username}}&password={{elastic.password}}&operation=BulkIndex&indexName=subscriptionbillinghistory")
//					.to("elasticsearch-rest://elasticsearch?operation=BULK_INDEX&indexName=subscriptionbillinghistory")
//					.to("elasticsearch://local?operation=INDEX&indexName=twitter&indexType=tweet")
					.log(LoggingLevel.INFO, "Response       	   -> ${in.headers.CamelHttpResponseCode} + ${body}")

					.log("********** SUCCESS - ElasticSearch POST  **********")
//					.unmarshal().json(JsonLibrary.Jackson, ElasticSearchResponse.class)
//					.bean(billingHistoryElasticProcessor, "billingHistoryElasticSearchResp")
//					.marshal().json(JsonLibrary.Jackson,BillingHistoryResponse.class)
					.log(LoggingLevel.INFO, "Response-Body	   -> ${body}")
					.setProperty("transHeader", constant(null));

		rest("/eaiapi/subscriptions/getBillingHistory")
				.get()
				.param().name("contractNumber").type(RestParamType.query).required(true).endParam()
				.consumes("application/json").produces("application/json")
				.bindingMode(RestBindingMode.off)
				.route()
				.log("=====  Request Recieved for ElasticSearch GET  =====")
				.log(LoggingLevel.INFO, "Incoming Contract Number = ${header.contractNumber}")
				.bean(billingHistorySearchProcessor, "processElasticRequest")
				.marshal().json(JsonLibrary.Jackson, ElasticRequest.class)
				.log(LoggingLevel.INFO, "BODY-SEARCH -> ${body}")

//				.to("{{elasticSearch.search.url}}?authMethod=Basic&authUsername={{elastic.username}}&authPassword={{elastic.password}}&httpClient.authenticationPreemptive=true")	// For Https Connection
				.to("{{elasticSearch.search.url}}?bridgeEndpoint=true")	// For Http4 Connection
				.log(LoggingLevel.DEBUG, "Response       	   -> ${in.headers.CamelHttpResponseCode} + ${body}")

				.log("********** SUCCESS - ElasticSearch GET  **********")
				.unmarshal().json(JsonLibrary.Jackson, ElasticResponse.class)
				.bean(billingHistorySearchProcessor, "processElasticResponse")
				.marshal().json(JsonLibrary.Jackson, BillingHistoryResponse.class)
				.log(LoggingLevel.DEBUG, "Response-Body	   -> ${body}");

	}


	public void setBillingHistoryAOPSProcessor(BillingHistoryAOPSProcessor billingHistoryAOPSProcessor) {
		this.billingHistoryAOPSProcessor = billingHistoryAOPSProcessor;
	}


	public void setBillingHistoryElasticProcessor(BillingHistoryElasticProcessor billingHistoryElasticProcessor) {
		this.billingHistoryElasticProcessor = billingHistoryElasticProcessor;
	}


	public void setBillingHistorySearchProcessor(BillingHistorySearchProcessor billingHistorySearchProcessor) {
		this.billingHistorySearchProcessor = billingHistorySearchProcessor;
	}

	public void setBillingHistoryTransportClient(BillingHistoryTransportClient billingHistoryTransportClient) {
		this.billingHistoryTransportClient = billingHistoryTransportClient;
	}

}