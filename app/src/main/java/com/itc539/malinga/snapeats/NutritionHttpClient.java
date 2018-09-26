package com.itc539.malinga.snapeats;

import com.loopj.android.http.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NutritionHttpClient {
    private static final String APP_ID = "96fabe45";
    private static final String APP_KEY = "dfa0761a51a41bda63ee3d196c72683d";
    private static String BASE_URL = "https://api.edamam.com/api/nutrition-data?app_id=" + APP_ID
                        + "&app_key=" + APP_KEY + "&ingr=";


    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + encode(relativeUrl);
    }

    public static String encode(String url)
    {
        try {
            String encodeURL = URLEncoder.encode( url, "UTF-8" );
            return encodeURL;
        } catch (UnsupportedEncodingException e) {
            return "Issue while encoding" +e.getMessage();
        }
    }
}
