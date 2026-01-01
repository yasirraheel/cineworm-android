package com.cineworm.videostreamingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cineworm.adapter.DeviceAdapter;
import com.cineworm.item.ItemDevice;
import com.cineworm.util.Constant;
import com.cineworm.util.NetworkUtils;
import com.cineworm.util.RvOnClickListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * Created by laxmi.
 */
public class DeviceActivity extends AppCompatActivity {

    ArrayList<ItemDevice> mListItem;
    public RecyclerView recyclerView;
    DeviceAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout lyt_not_found;
    MyApplication myApplication;
    ProgressDialog pDialog;
    boolean isFromDashBoard = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.device_list));
        pDialog = new ProgressDialog(this);
        myApplication = MyApplication.getInstance();
        mListItem = new ArrayList<>();
        lyt_not_found = findViewById(R.id.lyt_not_found);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(DeviceActivity.this, LinearLayoutManager.VERTICAL, false));

        Intent intent = getIntent();
        if (intent.hasExtra("isFromDashBoard")) {
            isFromDashBoard = intent.getBooleanExtra("isFromDashBoard", false);
        }

        if (NetworkUtils.isConnected(DeviceActivity.this)) {
            getDeviceList();
        } else {
            showToast(getString(R.string.conne_msg1));
        }

    }

    private void getDeviceList() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("user_id", myApplication.getUserId());
        client.get(Constant.DEVICE_LIST_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                showProgress(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                showProgress(false);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objJson = jsonArray.getJSONObject(i);
                            if (objJson.has(Constant.STATUS)) {
                                lyt_not_found.setVisibility(View.VISIBLE);
                            } else {
                                ItemDevice objItem = new ItemDevice();
                                objItem.setDeviceName(objJson.getString("device_name"));
                                objItem.setUserId(objJson.getString(Constant.USER_ID));
                                objItem.setSessionName(objJson.getString("user_session_name"));
                                if (myApplication.getUserSession().equals(objJson.getString("user_session_name"))) {
                                    objItem.setSameUser(true);
                                }
                                mListItem.add(objItem);
                            }
                        }
                    }
                    displayData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showProgress(false);
                lyt_not_found.setVisibility(View.VISIBLE);
            }

        });
    }

    private void displayData() {
        adapter = new DeviceAdapter(DeviceActivity.this, mListItem);
        recyclerView.setAdapter(adapter);

        adapter.onClickOnLogout(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                if (NetworkUtils.isConnected(DeviceActivity.this)) {
                    logOutRemoteDevice(mListItem.get(position).getSessionName());
                } else {
                    showToast(getString(R.string.conne_msg1));
                }
            }
        });

        if (adapter.getItemCount() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }
    }


    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lyt_not_found.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void showProgressDialog() {
        pDialog.setMessage(DeviceActivity.this.getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    private void logOutRemoteDevice(String sessionName) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("user_id", myApplication.getUserId());
        params.put("user_session_name", sessionName);
        client.get(Constant.DEVICE_LOGOUT_REMOTE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mListItem.clear();
                showProgress(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                showProgress(false);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    boolean isDeviceLimitReached = mainJson.getBoolean("success");
                    if (isDeviceLimitReached) {
                        JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                        JSONObject objJson;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objJson = jsonArray.getJSONObject(i);
                            ItemDevice objItem = new ItemDevice();
                            objItem.setDeviceName(objJson.getString("device_name"));
                            objItem.setUserId(objJson.getString(Constant.USER_ID));
                            objItem.setSessionName(objJson.getString("user_session_name"));
                            if (myApplication.getUserSession().equals(objJson.getString("user_session_name"))) {
                                objItem.setSameUser(true);
                            }
                            mListItem.add(objItem);
                        }
                        displayData();
                    } else {
                        showToast(getString(R.string.user_logout_remote));
                        if (!isFromDashBoard) {
                            Intent intent = new Intent(DeviceActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            myApplication.saveDeviceLimit(false);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showProgress(false);
                lyt_not_found.setVisibility(View.VISIBLE);
            }

        });
    }

    public void showToast(String msg) {
        Toast.makeText(DeviceActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_log_out) {
            new LogoutOnline(DeviceActivity.this);
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}
