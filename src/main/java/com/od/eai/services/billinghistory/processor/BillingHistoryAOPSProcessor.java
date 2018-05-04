package com.od.eai.services.billinghistory.processor;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Map;
import com.od.eai.services.subscriptions.beans.*;

public class BillingHistoryAOPSProcessor {

	private static Logger log = LoggerFactory.getLogger(BillingHistoryRequest.class);

	public void billingHistoryStoredProcReq(Exchange exchange) throws ParseException {
		BillingHistoryRequest billingHistoryRequest = exchange.getIn().getBody(BillingHistoryRequest.class);
		TransactionHeader transHeader  = billingHistoryRequest.getBillingHistoryRequest().getTransactionHeader();
		exchange.setProperty("transHeader",transHeader);

		/* HEADERS */
		BillingEvent billingEvent = new BillingEvent();
		billingEvent.setInvoiceNumber(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getInvoiceNumber());
		billingEvent.setOrderNumber(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getOrderNumber());
		billingEvent.setNetSale(fmtAmount(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getTotals().getSubTotal()));
		billingEvent.setTotalTax(fmtAmount(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getTotals().getTax()));
		billingEvent.setGrossSales(fmtAmount(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getTotals().getTotal()));
		billingEvent.setInvoiceDate(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getInvoiceDate());
		billingEvent.setInvoiceTime(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getInvoiceTime());
		billingEvent.setInvoiceStatus(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getInvoiceStatus());

		/* DETAILS */
		StringBuilder stringBuilder = new StringBuilder();
		String details = "";
		String finalDetails = "";

		int cnt = billingHistoryRequest.getBillingHistoryRequest().getInvoice().getInvoiceLines().getInvoiceLine().size();
		for (int i = 0; i < cnt; i++) {

			int quantity = Integer.valueOf(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getInvoiceLines().getInvoiceLine().get(i).getQuantity());
			int unitPrice = Integer.valueOf(StringUtils.remove(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getInvoiceLines().getInvoiceLine().get(i).getUnitPrice(),"."));
			int extPrice = quantity * unitPrice;

			details = StringUtils.leftPad(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getInvoiceLines().getInvoiceLine().get(i).getOrderLineNumber(), 5, '0') +
					StringUtils.leftPad(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getInvoiceLines().getInvoiceLine().get(i).getItemNumber(), 20, '0') +
					StringUtils.leftPad(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getInvoiceLines().getInvoiceLine().get(i).getQuantity(), 5, '0') +
					StringUtils.leftPad(fmtAmount(String.valueOf(extPrice)), 20, '0');
			finalDetails = stringBuilder.append(details).toString();
		}
		billingEvent.setDetails(finalDetails);

		/* TENDERS */
		String tenders = "";
		tenders= StringUtils.leftPad(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getTenders().getTenderLineNumber(), 5, '0') +
				StringUtils.rightPad(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getTenders().getCardType(), 12, ' ') +
				StringUtils.leftPad(fmtAmount(billingHistoryRequest.getBillingHistoryRequest().getInvoice().getTenders().getAmount()), 20, '0') +
				StringUtils.leftPad(StringUtils.right(billingHistoryRequest.getBillingHistoryRequest().getCustomer().getPaymentDetails().getPaymentCard().getCardHighValueToken(), 4), 4, " ") +
				StringUtils.leftPad(billingHistoryRequest.getBillingHistoryRequest().getCustomer().getPaymentDetails().getPaymentCard().getExpirationDate(), 4, '4');
		billingEvent.setTender(tenders);

		exchange.getIn().setBody(billingEvent);
	}

	public void billingHistoryStoredProcResp(Exchange exchange) {
		Map<String, String> body = (Map<String, String>) exchange.getIn().getBody();

		BillingHistoryResponse billingHistoryResponse = new BillingHistoryResponse();
		BillingHistoryResponse_ billingHistoryResponse_ = new BillingHistoryResponse_();

		TransactionHeader transactionHeader = (TransactionHeader)exchange.getProperty("transHeader");
		billingHistoryResponse_.setTransactionHeader(transactionHeader);

		TransactionStatus transactionStatus = new TransactionStatus();
		String status = body.get("status");
		transactionStatus.setCode(body.get("status"));
		billingHistoryResponse_.setTransactionStatus(transactionStatus);

		billingHistoryResponse.setBillingHistoryResponse(billingHistoryResponse_);
		exchange.getIn().setBody(billingHistoryResponse);

	}

	public static String fmtAmount(String sAmount) throws ParseException {

		int amount=0;
		if (StringUtils.isNotEmpty(sAmount)) {
			amount = Integer.valueOf(StringUtils.remove(sAmount, "."));
		}

		if (amount < 0) {
			amount = amount * -1;
			sAmount = StringUtils.leftPad(String.valueOf(amount), 19, "0") + "-";
		} else {
			sAmount = StringUtils.leftPad(String.valueOf(amount), 20, "0");
		}

		return sAmount;
	}

}