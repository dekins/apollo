package com.ctrip.framework.apollo.portal.components;

import com.google.common.io.BaseEncoding;

import com.ctrip.framework.apollo.portal.service.ServerConfigService;

import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean {
  @Autowired
  private HttpMessageConverters httpMessageConverters;

  @Autowired
  private ServerConfigService serverConfigService;

  private RestTemplate restTemplate;

  public RestTemplate getObject() {
    return restTemplate;
  }

  public Class<RestTemplate> getObjectType() {
    return RestTemplate.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void afterPropertiesSet() throws UnsupportedEncodingException {
    Collection<Header> defaultHeaders = new ArrayList<Header>();
    Header header = new BasicHeader("Authorization",
        "Basic " + BaseEncoding.base64().encode("apollo:".getBytes("UTF-8")));
    defaultHeaders.add(header);

    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials("apollo", ""));
    CloseableHttpClient httpClient =
        HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider)
            .setDefaultHeaders(defaultHeaders).build();


    restTemplate = new RestTemplate(httpMessageConverters.getConverters());
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory(httpClient);
    requestFactory.setConnectTimeout(getConnectTimeout());
    requestFactory.setReadTimeout(getReadTimeout());

    restTemplate.setRequestFactory(requestFactory);
  }

  private int getConnectTimeout() {
    String connectTimeout = serverConfigService.getValue("api.connectTimeout", "3000");

    return Integer.parseInt(connectTimeout);
  }

  private int getReadTimeout() {
    String readTimeout = serverConfigService.getValue("api.readTimeout", "10000");

    return Integer.parseInt(readTimeout);
  }


}
