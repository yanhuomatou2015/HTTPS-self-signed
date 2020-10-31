package com.winzhibin.httpsdemo;

import android.app.Activity;
import android.os.Bundle;
import okhttp3.OkHttpClient;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by wenzhibin(yanhuomatou2015) on 2020/10/23.
 * 
 */
public class certificatesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    /**
     * 信任所有证书
     * @return
     */

    private OkHttpClient getHttpsClient2() {
        OkHttpClient.Builder okhttpClient = new OkHttpClient().newBuilder();
        //主机名校验，
        okhttpClient.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession sslSession) {
                return true;
            }
        });
        //创建信任管理器
        TrustManager[] trustAllCerts = new TrustManager[] {

                new X509TrustManager() {//匿名类实现X509TrustManager接口
                    @Override
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] x509Certificates,
                            String s) throws java.security.cert.CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] x509Certificates,
                            String s) throws java.security.cert.CertificateException {
                    }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[] {};
                    }
                }
        };
        try {
            //获取SSL上下文对象
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            okhttpClient.sslSocketFactory(sslContext.getSocketFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return okhttpClient.build();
    }


    /**
     *
     * 信任指定证书
     * @return
     */
    private OkHttpClient getHttpsClient() {
        SSLContext sslContext = null;
        OkHttpClient.Builder okhttpClient = new OkHttpClient().newBuilder();
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            //方法一：将证书放到assets文件夹里面然后获取
             InputStream certificates = getAssets().open("yanhuomatou2015.cer");

            //方法二：为了不将证书打包到apk里面了，可以将证书内容定义成字符串常量，再将字符串转为流的形式
            //String wenzhibinBook = "拷贝证书里的内容到此处";
            //InputStream certificates = new ByteArrayInputStream(wenzhibinBook.getBytes("UTF-8"));

            // 创建秘钥，添加证书进去
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            String certificateAlias = Integer.toString(0);
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificates));

            // 创建信任管理器工厂并初始化秘钥
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            //获取SSL上下文对象，并初始化信任管理器
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

            //okhttp设置sokect工厂，并校验主机名
            okhttpClient.sslSocketFactory(sslContext.getSocketFactory());
            okhttpClient.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession sslSession) {
                return true;
            }
        });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return okhttpClient
                .readTimeout(7676, TimeUnit.MILLISECONDS)
                .connectTimeout(7676, TimeUnit.MILLISECONDS)
                .build();
    }



}
