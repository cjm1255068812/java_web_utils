package com.jermine.utils;

import org.apache.commons.collections.MapUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jermine
 * @version 1.0
 * @classname HttpClientUtil
 * @description
 * @date 2021/1/15 15:53
 **/
public class HttpClientUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final Integer CONNECTION_TIMEOUT = 5 * 1000; //设置连接超时时间，单位毫秒
    private static final Integer SO_TIMEOUT = 20 * 1000; //请求获取数据的超时时间，单位毫秒
    private static final Integer CONN_MANAGER_TIMEOUT = 500;//设置从connect Manager获取Connection 超时时间，单位毫秒
    private static SSLConnectionSocketFactory sslsf = null;
    private static PoolingHttpClientConnectionManager cm = null;



    public static String post(String  url, Map<String, String> params, Map<String, String> header, HttpEntity entity) {
        String result = "";
        CloseableHttpClient httpClient = null;
        try {
            httpClient = initHttpClient();
            HttpPost httpPost = new HttpPost(url);
            // 设置头信息
            if (MapUtils.isNotEmpty(header)) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            List<NameValuePair> formParams = new ArrayList<>();
            // 设置请求参数
            if (MapUtils.isNotEmpty(params)) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    //给参数赋值
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
                httpPost.setEntity(urlEncodedFormEntity);
            }
            // 设置实体 优先级高
            if (entity != null) {
                httpPost.setEntity(entity);
            }
            HttpResponse httpResponse = httpClient.execute(httpPost);
            LOGGER.info("创建请求httpPost-URL={},params={}", url, formParams);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity resEntity = httpResponse.getEntity();
                result = EntityUtils.toString(resEntity);
            } else {
                result = readHttpResponse(httpResponse);
            }
        } catch (Exception e) {
            LOGGER.error("请求发送失败：{}", e.getMessage());
            throw new RuntimeException("请求发送失败，URL:"+url+",params:"+ params);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.error("关闭HttpClient出错:{}", e.getMessage());
                }
            }
        }
        return result;
    }

    private static CloseableHttpClient initHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setConnectionRequestTimeout(CONN_MANAGER_TIMEOUT)
                .setSocketTimeout(SO_TIMEOUT).build();
        return HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManager(cm)
                .setConnectionManagerShared(true)
                .setDefaultRequestConfig(config)
                .build();
    }

    /**
     * httpClient get请求
     * @param url 请求url
     * @param params 请求参数 form提交适用
     * @param header 头部信息
     * @return 可能为空 需要处理
     * @throws Exception
     *
     */
    public static String get(String  url, Map<String, String> params, Map<String, String> header) {
        String result = "";
        CloseableHttpClient httpClient = null;
        try {
            HttpGet httpGet = null;
            List<NameValuePair> formParams = new ArrayList<>();
            // 设置请求参数
            if (MapUtils.isNotEmpty(params)) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    //给参数赋值
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
                url += "?"+ EntityUtils.toString(urlEncodedFormEntity);
            }
            httpGet = new HttpGet(url);
            // 设置头信息
            if (MapUtils.isNotEmpty(header)) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            httpClient = initHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            LOGGER.info("创建请求http get请求，URL={}", url);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity resEntity = httpResponse.getEntity();
                result = EntityUtils.toString(resEntity);
            } else {
                result = readHttpResponse(httpResponse);
            }
        } catch (Exception e) {
            LOGGER.error("请求发送失败：{}", e.getMessage());
            throw new RuntimeException("请求发送失败，URL:"+url+",params:"+ params);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.error("关闭HttpClient出错:{}", e.getMessage());
                }
            }
        }
        return result;
    }

    public static String readHttpResponse(HttpResponse httpResponse)
            throws ParseException, IOException {
        StringBuilder builder = new StringBuilder();
        // 获取响应消息实体
        HttpEntity entity = httpResponse.getEntity();
        // 响应状态
        builder.append("status:").append(httpResponse.getStatusLine());
        builder.append("headers:");
        HeaderIterator iterator = httpResponse.headerIterator();
        while (iterator.hasNext()) {
            builder.append("\t").append(iterator.next());
        }
        // 判断响应实体是否为空
        if (entity != null) {
            String responseString = EntityUtils.toString(entity);
            builder.append("response length:").append(responseString.length());
            builder.append("response content:").append(responseString.replace("\r\n", ""));
        }
        return builder.toString();
    }

}