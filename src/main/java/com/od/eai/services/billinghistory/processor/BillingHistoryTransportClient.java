package com.od.eai.services.billinghistory.processor;

import org.apache.camel.Exchange;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.*;

import java.io.*;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

import org.elasticsearch.client.Client;

public class BillingHistoryTransportClient {

    public void ElasticSearchReq(Exchange exchange) throws Exception

    {

        System.out.println("1.Inside-ES");
//        transportClient();
        restClient(exchange);

    }

    private static void transportClient() {

        InetAddress ip = null;
        byte[] bytes = ip.getAddress();

        byte[] ipAddress = {10, 94, 98, 46};
        try {
            InetAddress.getByName("10.94.98.46");
            Settings settings = Settings.settingsBuilder()
                    .put("username", "es_admin_eai")
                    .put("password", "eainonprod")
                    .put("index", "subscriptionbillinghistory")
                    .build();

            Client client = TransportClient.builder()
                    .settings(settings)
                    .build()
//                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.94.98.46"), 8443))
//                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.94.98.50"), 8443))
//                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.94.98.84"), 8443))
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByAddress(bytes), 9300));
//            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.94.98.109").isSiteLocalAddress()));

        } catch (Exception e) {
            throw new RuntimeException("Error while accessing through Transport Client: ", e);
        }
    }

    private static final void restClient(final Exchange exchange) {

        final String body = exchange.getIn().getBody(String.class);
        System.out.println("00000000000000000000000000000000000000000000000000000000000Request Body : " + body);

        Header[] headers = { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),
                new BasicHeader("Role", "Read") };
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("es_admin_eai", "eainonprod"));
        RestClient restClient = RestClient.builder(new HttpHost("10.94.98.46", 8443, "https")).setDefaultHeaders(headers)
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder arg0) {

                        return arg0.setDefaultCredentialsProvider(credentialsProvider);
                    }
                })
                .build();

        System.out.println("2.Inside-ES");

        final Map<String, String> params  = Collections.emptyMap();
        HttpEntity bodyEntity = new NStringEntity(body, ContentType.APPLICATION_JSON);
        Response response = null;
        System.out.println("3.Inside-ES");
        try {
            response = restClient.performRequest("POST", "/subscriptionbillinghistory", params, bodyEntity);
            System.out.println("11111111111111111111111111111 "+response);
            InputStream resposneis = response.getEntity().getContent();
            StringWriter writer = new StringWriter();
            IOUtils.copy(resposneis, writer, "UTF-8");
            String theString = writer.toString();
            System.out.println("22222222222222222222222222222"+theString);
        }catch(Exception e) {
            System.out.println("33333333333333333333333333333");
            e.printStackTrace();
        }

    }

}