package com.layer.layerparseexample;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


/**
 * Created by abhishek on 09/06/15.
 */
public class VolleySingleton {
    private static RequestQueue mQueue;
    private static VolleySingleton mInstance = null;


    private VolleySingleton(Context mContext) {
        mQueue = Volley.newRequestQueue(mContext);
    }


    public static VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }




    public static RequestQueue getReqQueue(Context context) {
        if (mQueue == null)
            mQueue = Volley.newRequestQueue(context);

        return mQueue;
    }
}
