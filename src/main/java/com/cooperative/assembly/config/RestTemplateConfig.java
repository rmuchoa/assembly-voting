package com.cooperative.assembly.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	
	@Value("${app.config.http.client.connectionManagerMaxTotal}")
	private Integer connectionManagerMaxTotal;
	
	@Value("${app.config.http.client.connectionManagerDefaultMaxPerRoute}")
	private Integer connectionManagerDefaultMaxPerRoute; 
	
	@Value("${app.config.http.client.connectionRequestTimeout}")
	private Integer connectionRequestTimeout;
	
	@Value("${app.config.http.client.connectTimeout}")
	private Integer connectTimeout;
	
	@Value("${app.config.http.client.socketTimeout}")
	private Integer socketTimeout;
	
	@Value("${app.config.http.client.readTimeout}")
	private Integer readTimeout;
	
	@Bean
	public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
	    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
	    connectionManager.setMaxTotal(connectionManagerMaxTotal);
	    connectionManager.setDefaultMaxPerRoute(connectionManagerDefaultMaxPerRoute);
	    return connectionManager;
	}

	@Bean
	public RequestConfig requestConfig() {
	    return RequestConfig.custom()
	        .setConnectionRequestTimeout(connectionRequestTimeout)
	        .setConnectTimeout(connectTimeout)
	        .setSocketTimeout(socketTimeout)
	        .build();
	}

	@Bean
	public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager, RequestConfig requestConfig) {
	    return HttpClientBuilder
	        .create()
	        .setConnectionManager(poolingHttpClientConnectionManager)
	        .setDefaultRequestConfig(requestConfig)
	        .disableCookieManagement()
	        .build();
	}

	@Bean
	public RestTemplate restTemplate(HttpClient httpClient) {
	    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
	    requestFactory.setHttpClient(httpClient);
	    requestFactory.setReadTimeout(readTimeout);
	    requestFactory.setConnectTimeout(connectTimeout);
	    return new RestTemplate(requestFactory);
	}

}
