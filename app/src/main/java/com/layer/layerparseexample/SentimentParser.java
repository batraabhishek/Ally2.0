package com.layer.layerparseexample;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by abhishek on 27/06/15 at 4:59 PM.
 */
public class SentimentParser {


    public SentimentParser() {
    }

    public void sendRequest(Context context, final String text) {

        RequestQueue queue = VolleySingleton.getReqQueue(context);

        String url = "http://access.alchemyapi.com/calls/text/TextGetTextSentiment";

        StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("apikey", "7579faa3bcd87e301a67e62298fbfeae52f1f899");
                params.put("outputMode", "json");
                params.put("text", text);
                return params;
            }
        };

        queue.add(request);
    }

    Response.Listener<String> responseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
//            Log.d("JSON", response.toString());
            String perc;
            try {
                JSONObject jsonObject = new JSONObject(response);
                perc = jsonObject.getJSONObject("docSentiment").getString("score");
            } catch (JSONException e) {
                perc = "0";
                e.printStackTrace();
            }

            double percDouble = Double.parseDouble(perc);
            percDouble = percDouble * 10.0;

            int parInt = (int) percDouble;
            parInt += 10;

            Log.d("JSON", "Round Off : " + (int) percDouble);
        }
    };

    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("JSON", "Error: " + error.getMessage());
        }
    };
}
