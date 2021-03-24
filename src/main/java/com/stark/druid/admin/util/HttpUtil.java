package com.stark.druid.admin.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Java 发送 HTTP 请求。
 * @author Ben
 * @since 1.0.0
 * @version 1.0.0
 */
public class HttpUtil {
	
	private static final String CHARSET = "utf-8";
	
	/**
	 * 发送 get 请求。
	 * @param url 请求地址。
	 * @param headers 请求头参数集合。
	 * @param querys 地址栏请求参数集合。
	 * @return 响应对象。
	 * @throws Exception
	 */
	public static HttpResponse doGet(String url, Map<String, String> headers, Map<String, String> querys)
			throws Exception {
    	HttpClient httpClient = wrapClient(url);

    	HttpGet request = new HttpGet(buildUrl(url, querys));
    	setConfig(request);
    	setHeaders(request, headers);
        
        HttpResponse response = httpClient.execute(request);
        return response;
    }
	
	/**
	 * 根据 url 参数 map 获得查询字符串。
	 * @param queryParamMap 参数 map 。
	 * @throws UnsupportedEncodingException
	 */
	public static String getQueryString(Map<String, String> queryParamMap) {
		if (MapUtils.isEmpty(queryParamMap)) {
			return "";
		}
		
		String queryString = queryParamMap.entrySet()
			.stream()
			.filter(entry -> StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getValue()))
			.map(entry -> {
				try {
					return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			})
			.reduce("", (s1, s2) -> s1 + ("".equals(s1) ? "" : "&") + s2);
		return queryString;
	}
	
	/**
	 * 设置 http 客户端。
	 * @param url 请求地址。 请求地址。
	 * @return http 客户端。
	 */
	private static CloseableHttpClient wrapClient(String url) {
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		if (url.startsWith("https://")) {
			clientBuilder
				.setSSLContext(createSSLContext())
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
		}
		CloseableHttpClient httpClient = clientBuilder.build();
		return httpClient;
	}
	
	/**
	 * 请求配置。
	 * @param request 请求对象。
	 */
	private static void setConfig(HttpRequestBase request) {
		int connectTimeout = Integer.parseInt(System.getProperty("sun.net.client.defaultConnectTimeout", "3000"));
        int readTimeout = Integer.parseInt(System.getProperty("sun.net.client.defaultReadTimeout", "5000"));
		
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(connectTimeout)	// 建立连接超时时间
				.setSocketTimeout(readTimeout)	// 接收数据超时时间
				.build();
		request.setConfig(config);
	}
	
	/**
	 * 设置请求头信息。
	 * @param request 请求对象。
	 * @param headers 请求头参数集合。
	 */
	private static void setHeaders(HttpRequestBase request, Map<String, String> headers) {
		if (MapUtils.isNotEmpty(headers)) {
			headers.entrySet().forEach(entry -> request.addHeader(entry.getKey(), entry.getValue()));
    	}
	}
	
	/**
	 * 创建 SSL 上下文。
	 * @return SSL 上下文对象。
	 */
	private static SSLContext createSSLContext() {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
			X509TrustManager trustManager = new X509TrustManager() {
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
						String paramString) throws CertificateException {
				}
	
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
						String paramString) throws CertificateException {
				}
	
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			sslContext.init(null, new TrustManager[] { trustManager }, null);
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
		return sslContext;
	}
	
	/**
	 * 把地址栏请求参数拼接到 url 中。
	 * @param url 请求地址。
	 * @param querys 地址栏请求参数集合。
	 * @return 附加地址栏请求参数的 url 。
	 * @throws UnsupportedEncodingException
	 */
	private static String buildUrl(String url, Map<String, String> querys) throws UnsupportedEncodingException {
    	StringBuffer sbUrl = new StringBuffer();
    	sbUrl.append(url.endsWith("/") ? url.substring(0, url.length() - 1) : url);
    	if (MapUtils.isNotEmpty(querys)) {
    		StringBuffer sbQuery = new StringBuffer();
        	for (Entry<String, String> query : querys.entrySet()) {
        		if (sbQuery.length() > 0) {
        			sbQuery.append("&");
        		}
        		if (StringUtils.isBlank(query.getKey()) && StringUtils.isNotBlank(query.getValue())) {
        			sbQuery.append(query.getValue());
                }
        		if (StringUtils.isNotBlank(query.getKey())) {
        			sbQuery.append(query.getKey());
        			if (StringUtils.isNotBlank(query.getValue())) {
        				sbQuery.append("=").append(URLEncoder.encode(query.getValue(), CHARSET));
        			}
                }
        	}
        	if (sbQuery.length() > 0) {
        		sbUrl.append(url.contains("?") ? "&" : "?").append(sbQuery);
        	}
        }
    	url = sbUrl.toString();
    	return url;
    }
	
}
