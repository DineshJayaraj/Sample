package com.od.eai.services.billinghistory.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.security.KeyStore;
import java.util.Collections;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

public class ESRestClient {
    public static void main(String args[]) {
        restClient();
    }

    private static final void restClient() {

        final String body = readFromFile();
        System.out.println("00000000000000000000000000000000000000000000000000000000000Request Body : " + body);

        final HttpHost host = new HttpHost("10.94.98.46", 8443, "https");

        Header[] headers = { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),
                new BasicHeader("Role", "Read") };
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("es_admin_eai", "eainonprod"));
        RestClient restClient = RestClient.builder(host).setDefaultHeaders(headers)
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder arg0) {

                        arg0.setDefaultCredentialsProvider(credentialsProvider);
                        arg0.setSSLContext(sslContext());

                        return arg0;
                    }
                }).build();

        System.out.println("2.Inside-ES");

        final Map<String, String> params = Collections.emptyMap();
        HttpEntity bodyEntity = new NStringEntity(body, ContentType.APPLICATION_JSON);
        Response response = null;
        System.out.println("3.Inside-ES");
        try {
            response = restClient.performRequest("POST", "/subscriptionbillinghistory", params, bodyEntity);
            InputStream resposneis = response.getEntity().getContent();
            String theString = convert(resposneis, Charset.defaultCharset());
            System.out.println(theString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String readFromFile() {
        try {
            final BufferedReader bs = new BufferedReader(new FileReader(new File("C:\\Git\\es.json")));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = bs.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convert(InputStream inputStream, Charset charset) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        return stringBuilder.toString();
    }

//     private static RestClientBuilder sslHost(final HttpHost host) {
//            try {
//
//            KeyStore truststore = KeyStore.getInstance("jks");
//            // InputStream is = new FileInputStream(new File("c://root"));
//            try (InputStream is = new FileInputStream(new File("c://root"))) {
//                  truststore.load(is, null);
//            }
//            SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
//            final SSLContext sslContext = sslBuilder.build();
//            RestClientBuilder builder = RestClient.builder(host)
//                         .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
//                                 @Override
//                                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
//                                       return httpClientBuilder.setSSLContext(sslContext);
//                                }
//                         });
//            return builder;
//            }catch(Exception e) {
//                  e.printStackTrace();
//            }
//            return null;
//     }

    private static final SSLContext sslContext() {
        try {
            KeyStore truststore = KeyStore.getInstance("jks");
            // InputStream is = new FileInputStream(new File("c://root"));
            try (InputStream is = new FileInputStream(new File("C:\\Git\\subscription-billing-history\\ca"))) {
                truststore.load(is, null);
            }
            SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
            final SSLContext sslContext = sslBuilder.build();
            return sslContext;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

