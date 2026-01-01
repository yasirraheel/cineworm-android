package com.cineworm.videostreamingapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LogoutOnline {

    private final ProgressDialog pDialog;
    private final Activity activity;
    private final MyApplication myApplication;

    public LogoutOnline(Activity activity) {
        this.activity = activity;
        pDialog = new ProgressDialog(activity);
        myApplication = MyApplication.getInstance();

        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.menu_logout))
                .setMessage(activity.getString(R.string.logout_msg))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.drawable.ic_logout)
                .show();
    }

    public void logout() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_session_name", myApplication.getUserSession());
        jsObj.addProperty("user_id", myApplication.getUserId());
        params.put("data", API.toBase64(jsObj.toString()));
        Log.e("data", API.toBase64(jsObj.toString()));
        client.post(Constant.LOGOUT_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                showProgressDialog();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dismissProgressDialog();

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson = jsonArray.getJSONObject(0);
                    if (objJson.getInt("success") == 1) {
                        myApplication.saveIsLogin(false);
                        OneSignal.getUser().addTag("user_session","");
                        myApplication.saveDeviceLimit(false);
                        Intent intent = new Intent(activity, SignInActivity.class);
                        intent.putExtra("isLogout", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressDialog();
            }

        });
    }

    private void showProgressDialog() {
        pDialog.setMessage(activity.getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }
}
