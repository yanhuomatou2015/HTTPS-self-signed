package com.winzhibin.httpsdemo

import android.app.Activity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.ByteArrayOutputStream
import java.io.IOException

import java.net.URL
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


class MainActivity : Activity() {

    //懒加载初始化view
    val mybtn by lazy {
        btn_test
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * 方法一 ,android原生框架实现
         */

      mybtn.onClick {
          doAsync {

               //1.创建SSL上下文对象，设置信任管理器
              val sslContext = SSLContext.getInstance("TLS")
              //获取信任管理器工厂
              val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
              //初始化工厂
              val ks = KeyStore.getInstance(KeyStore.getDefaultType())
              //清空默认证书信息，设置自己的证书
              ks.load(null)
              val cf = CertificateFactory.getInstance("X.509")
              val open = assets.open("yanhuomatou2015.cer")
              val cert = cf.generateCertificate(open)
              ks.setCertificateEntry("wenzhibin",cert)
              tmf.init(ks)
              val tm = tmf.trustManagers
              sslContext.init(null,tm,null)
              val url = URL("https://192.168.254.159:8443/yanhuomatou2015.json")
              var connection: HttpsURLConnection = url.openConnection() as HttpsURLConnection //as关键字是强转的意思
              //使用信任管理器
              connection.sslSocketFactory=sslContext.socketFactory
              //校验主机名
              connection.hostnameVerifier=MyHostnameVerifier()
              //获取服务器返回流
              val ins = connection.inputStream
              //转成字符串
              val bos = ByteArrayOutputStream()
              var buffer = ByteArray(1024)
              var len = 0
              len = ins.read(buffer)
              while (len!=-1){
                  bos.write(buffer,0,len)
                  len = ins.read(buffer)
              }
              val result=bos.toString()
              println("服务器返回= " +result)
          }
      }


        /**
         * 方法二：okthhp框架实现
         *
         */

        try {

            val client = getHttpsClient()
            val request = Request.Builder().get()
                .url("https://192.168.254.159:8443/yanhuomatou2015.json")
                .build()
            var call = client?.newCall(request)
            //异步请求
            call?.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("wenzhibin", "onFailure: $e")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    Log.e("wenzhibin", "OnResponse: " + response.body()?.string())
                }
            })
        }catch (e:Exception) {
            Log.e("wenzhibin ERROR:", "", e)
        }


    }

    /**
     * 实现一个校验主机名接口
     */
    private  class MyHostnameVerifier:HostnameVerifier {
        //主机名校验，当发现是公司接口，返回true
        override fun verify(hostNmae: String?, p1: SSLSession?): Boolean {

//            if(hostNmae==="www.wenzhi.com"){
//                return true
//            }
            return true
        }

    }



    private fun getHttpsClient(): OkHttpClient? {
        var sslContext: SSLContext? = null
        val okhttpClient = OkHttpClient().newBuilder()
        try {
            val certificateFactory =
                CertificateFactory.getInstance("X.509")
            //方法一：将证书放到assets文件夹里面然后获取
            val certificates = assets.open("yanhuomatou2015.cer")

            //方法二：为了不将证书打包到apk里面了，可以将证书内容定义成字符串常量，再将字符串转为流的形式
            //String wenzhibinBook = "拷贝证书里的内容到此处";
            //InputStream certificates = new ByteArrayInputStream(wenzhibinBook.getBytes("UTF-8"));

            // 创建秘钥，添加证书进去
            val keyStore =
                KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null)
            val certificateAlias = Integer.toString(0)
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificates))
            // 创建信任管理器工厂并初始化秘钥
            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            //获取SSL上下文对象，并初始化信任管理器
            sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustManagerFactory.trustManagers, SecureRandom())
            //okhttp设置sokect工厂，并校验主机名
            okhttpClient.sslSocketFactory(sslContext.socketFactory)
            okhttpClient.hostnameVerifier(object :  HostnameVerifier{
                override fun verify(p0: String?, p1: SSLSession?): Boolean {
                    return true
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return okhttpClient
            .readTimeout(7676, TimeUnit.MILLISECONDS)
            .connectTimeout(7676, TimeUnit.MILLISECONDS)
            .build()
    }




}
