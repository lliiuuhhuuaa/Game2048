package com.sp.game2048.util;

import android.util.Log;

import com.sp.game2048.data.SQLiteData;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

import lombok.Getter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * http请求工具
 *
 * @author lh
 */
public class HttpClientUtil {
    private static final String TAG = "HttpClientUtil";
    public enum RequestTypeEnum {
        POST("post"), // post
        GET("get"); // get
        @Getter
        private String value;

        RequestTypeEnum(String value) {
            this.value = value;
        }
    }

    /**
     * @do map转formBody
     * @author lh
     * @date 2020-01-02 10:50
     */
    private static FormBody mapToFormBody(Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
                builder.add(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }
        return builder.build();
    }

    /**
     * @do get请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response get(String url) {
        return execute(url, null, null, RequestTypeEnum.GET, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }

    /**
     * @do get请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response get(String url, Map<String, String> params) {
        FormBody formBody = null;
        if (params != null && params.size() == 1) {
            formBody = mapToFormBody(params);
        }
        return execute(url, formBody, null, RequestTypeEnum.GET, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }

    /**
     * @do get请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response get(String url, Map<String, String> params, Callback callback) {
        FormBody formBody = null;
        if (params != null && params.size() == 1) {
            formBody = mapToFormBody(params);
        }
        return execute(url, formBody, callback, RequestTypeEnum.GET, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }

    /**
     * @do get请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response get(String url, FormBody formBody) {
        return execute(url, formBody, null, RequestTypeEnum.GET, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }

    /**
     * @do get请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response get(String url, FormBody formBody, Callback callback) {
        return execute(url, formBody, callback, RequestTypeEnum.GET, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }

    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response post(String url) {
        return execute(url, new FormBody.Builder().build(), null, RequestTypeEnum.POST, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }
    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response post(String url,Callback callback) {
        return execute(url, new FormBody.Builder().build(), callback, RequestTypeEnum.POST, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }
    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response postAndResult(String url,Runnable runnable) {

        return execute(url, new FormBody.Builder().build(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        }, RequestTypeEnum.POST, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }
    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response post(String url, Map<String, String> params) {
        FormBody formBody = null;
        if (params != null && params.size() == 1) {
            formBody = mapToFormBody(params);
        }
        return execute(url, formBody, null, RequestTypeEnum.POST, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }

    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response post(String url, Map<String, String> params, Callback callback) {
        FormBody formBody = null;
        if (params != null && params.size() == 1) {
            formBody = mapToFormBody(params);
        }
        return execute(url, formBody, callback, RequestTypeEnum.POST, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }

    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response post(String url, Map<String, String> params, MediaType mediaType) {
        FormBody formBody = null;
        if (params != null && params.size() == 1) {
            formBody = mapToFormBody(params);
        }
        return execute(url, formBody, null, RequestTypeEnum.POST, mediaType);
    }

    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response post(String url, Map<String, String> params, Callback callback, MediaType mediaType) {
        FormBody formBody = null;
        if (params != null && params.size() == 1) {
            formBody = mapToFormBody(params);
        }
        return execute(url, formBody, callback, RequestTypeEnum.POST, mediaType);
    }

    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response post(String url, FormBody formBody) {
        return execute(url, formBody, null, RequestTypeEnum.POST, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }

    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response post(String url, FormBody formBody, Callback callback) {
        return execute(url, formBody, callback, RequestTypeEnum.POST, MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"));
    }

    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response post(String url, FormBody formBody, MediaType mediaType) {
        return execute(url, formBody, null, RequestTypeEnum.POST, mediaType);
    }

    /**
     * @do post请求
     * @author lh
     * @date 2020-01-02 11:36
     */
    public static Response post(String url, FormBody formBody, Callback callback, MediaType mediaType) {
        return execute(url, formBody, callback, RequestTypeEnum.POST, mediaType);
    }

    /**
     * @do 执行http请求
     * @author lh
     * @date 2020-01-02 11:04
     */
    public static Response execute(String url, FormBody formBody, Callback callback, RequestTypeEnum requestTypeEnum, MediaType mediaType) {
        String domain = ClassUtil.get(SQLiteData.class).getConfig("domain");
        String token = UserUtil.getToken();
        url = String.format("%s%s%s",domain,url.startsWith("/")?"":"/",url);
        OkHttpClient okHttpClient = new OkHttpClient();
        //构造请求体
        Request.Builder builder = new Request.Builder().url(url);
        if(token!=null){
            builder.addHeader("tk",token);
        }
		/*builder.addHeader("RecordOther-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36");
		builder.addHeader("Content-Type",mediaType.toString());*/
        //post请求
        if (RequestTypeEnum.POST.equals(requestTypeEnum)) {
            builder.post(formBody);
        }
        Request request = builder.build();
        if (callback == null) {
            try {
                //同步调用
                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    //请求错误
                    Log.e(TAG, String.format("请求url:%s,状态码:%s,错误信息:%s", url, response.code(), response.body() == null ? null : response.body().string()));
                    System.out.println(response);
                    return null;
                }
                //请求成功,返回响应体
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, String.format("请求url:%s", url), e);
                return null;
            }
        }
        //异步调用
        okHttpClient.newCall(request).enqueue(callback);
        return null;
    }
}