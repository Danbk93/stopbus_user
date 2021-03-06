package com.connection.stopbus.stopbus_user;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.connection.stopbus.stopbus_user.JsonUtil.getJsonStringFromMap;

/**
 * Motivated by Danbk on 2018-05-14.
 */

public enum NetworkService implements NetworkInterface {
    INSTANCE;

    private static final long CONNECT_TIMEOUT = 20000;   // 2 seconds
    private static final long READ_TIMEOUT = 20000;      // 2 seconds
    private static OkHttpClient okHttpClient = null;


    public final static String URL_BASE = "http://stop-bus.tk/user/";

    /**
     * Method to build and return an OkHttpClient so we can set/get
     * headers quickly and efficiently.
     * @return OkHttpClient
     */
    private OkHttpClient buildClient() {
        if (okHttpClient != null) return okHttpClient;

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);


        // custom interceptor for adding header and NetworkMonitor sliding window
        okHttpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // Add whatever we want to our request headers.
                Request request = chain.request().newBuilder().addHeader("Accept", "application/json").build();
                Response response;
                try {
                    response = chain.proceed(request);
                } catch (SocketTimeoutException | UnknownHostException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
                return response;
            }
        });

        return  okHttpClientBuilder.build();
    }

    private Request.Builder buildRequest(URL url) {
        return new Request.Builder()
                .url(url);
    }


    private URL buildURL(Uri builtUrl) {
        if (builtUrl == null) return null;
        try {
            String urlStr = builtUrl.toString();
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private URL buildURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getData(Request request) {
        OkHttpClient client = buildClient();
        try {
            Response response = client.newCall(request).execute();

            Log.d("sb","response : "+ response);

            if (!response.isSuccessful()) {
                return "api not found";
            }

            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public String postQuery(String api , Map args) {

        JSONObject jsonObject = new JSONObject();
        jsonObject = getJsonStringFromMap(args);

        Log.d("sb","jsonObject:" + jsonObject);

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder()
                .url(URL_BASE+api)
                .post(body)
                .build();

        return getData(request);
    }

    @Override
    public String postQuery2(String api , ArrayList list, String key) {

        JSONArray array =new JSONArray();

        Log.d("sb","list.size:" +list.size());
        Log.d("sb","key:" + key);

        for(int i=0;i<list.size();i++){
            array.put(list.get(i));
        }
        Log.d("sb","array :" + array );
        JSONObject obj =new JSONObject();
        try{
                obj.put(key,array);
            }catch (JSONException e){
                e.printStackTrace();
            }
        Log.d("sb","aobj :" + obj );

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON,obj.toString());
        Request request = new Request.Builder()
                .url(URL_BASE+api)
                .post(body)
                .build();

        return getData(request);
    }


    @Override
    public String getQuery(String api , Map args) {

        Uri uri = null;

        Uri.Builder builder = uri.parse(URL_BASE+api).buildUpon();

        Set<String> keys = args.keySet();

        for(String key: keys){
            builder.appendQueryParameter(key, args.get(key).toString());

        }

        uri = builder.build();
        URL url = buildURL(uri);

        Request request = buildRequest(url)
                .build();

        return getData(request);
    }

}