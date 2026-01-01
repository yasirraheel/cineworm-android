package com.cineworm.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.cineworm.item.ItemDashBoard;
import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.cineworm.util.NetworkUtils;
import com.cineworm.videostreamingapp.DashboardActivity;
import com.cineworm.videostreamingapp.EditProfileActivity;
import com.cineworm.videostreamingapp.LogoutOnline;
import com.cineworm.videostreamingapp.MyApplication;
import com.cineworm.videostreamingapp.PlanActivity;
import com.cineworm.videostreamingapp.R;
import com.cineworm.videostreamingapp.SignInActivity;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MyAccountFragment extends Fragment {
    ProgressBar progressBar;
    LinearLayout lyt_not_found;
    RelativeLayout lytLogin;
    MaterialButton btnLogin;
    MyApplication myApplication;
    TextView tvLoginFirst, textName, textEmail, textCurrentPlan, textExpiresOn, textChangePlan, tvDashboard, tvEditProfile, tvLogout, tvDeleteAccount;
    NestedScrollView nestedScrollView;
    CircularImageView imageAvatar;
    ItemDashBoard itemDashBoard;

    private ProgressDialog pDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_account, container, false);
        myApplication = MyApplication.getInstance();
        lyt_not_found = rootView.findViewById(R.id.lyt_not_found);
        progressBar = rootView.findViewById(R.id.progressBar1);
        lytLogin = rootView.findViewById(R.id.lytLogin);
        btnLogin = rootView.findViewById(R.id.btnLogin);
        tvLoginFirst = rootView.findViewById(R.id.text);
        tvLoginFirst.setText(getString(R.string.login_first_see_account));
        nestedScrollView = rootView.findViewById(R.id.nestedScrollView);
        textName = rootView.findViewById(R.id.textName);
        textEmail = rootView.findViewById(R.id.textEmail);
        textCurrentPlan = rootView.findViewById(R.id.textCurrentPlan);
        textExpiresOn = rootView.findViewById(R.id.textExpiresOn);
        textChangePlan = rootView.findViewById(R.id.changePlan);
        tvDashboard = rootView.findViewById(R.id.tvDashboard);
        tvEditProfile = rootView.findViewById(R.id.tvEditProfile);
        tvLogout = rootView.findViewById(R.id.tvLogout);
        tvDeleteAccount = rootView.findViewById(R.id.tvDeleteAccount);
        imageAvatar = rootView.findViewById(R.id.imageAvtar);
        pDialog = new ProgressDialog(requireActivity());
        itemDashBoard = new ItemDashBoard();

        textChangePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), PlanActivity.class);
                startActivity(intent);
            }
        });

        tvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        tvDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), DashboardActivity.class);
                startActivity(intent);
            }
        });

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LogoutOnline(requireActivity());
            }
        });
        tvDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAccountConfirm();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSignIn = new Intent(requireActivity(), SignInActivity.class);
                intentSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentSignIn);
                requireActivity().finish();
            }
        });
        nestedScrollView.setVisibility(View.GONE);
        if (myApplication.getIsLogin()) {
            if (NetworkUtils.isConnected(requireActivity())) {
                getDashboard();
            } else {
                Toast.makeText(requireActivity(), getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
            }
        } else {
            nestedScrollView.setVisibility(View.GONE);
            lytLogin.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    private void getDashboard() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getUserId());
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.DASH_BOARD_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                progressBar.setVisibility(View.VISIBLE);
                nestedScrollView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                progressBar.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    if (jsonArray.length() > 0) {
                        objJson = jsonArray.getJSONObject(0);

                        itemDashBoard.setUserName(objJson.getString("name"));
                        itemDashBoard.setUserEmail(objJson.getString("email"));
                        itemDashBoard.setUserImage(objJson.getString("user_image"));
                        itemDashBoard.setCurrentPlan(objJson.getString("current_plan"));
                        itemDashBoard.setExpiresOn(objJson.getString("expires_on"));
                        itemDashBoard.setLastInvoiceDate(objJson.getString("last_invoice_date"));
                        itemDashBoard.setLastInvoicePlan(objJson.getString("last_invoice_plan"));
                        itemDashBoard.setLastInvoiceAmount(objJson.getString("last_invoice_amount"));
                        if (getActivity() != null) {
                            displayData();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        nestedScrollView.setVisibility(View.GONE);
                        lyt_not_found.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                progressBar.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayData() {
        textName.setText(itemDashBoard.getUserName());
        textEmail.setText(itemDashBoard.getUserEmail());
        textEmail.setSelected(true);
        textName.setSelected(true);
        if (!itemDashBoard.getUserImage().isEmpty()) {
            Picasso.get().load(itemDashBoard.getUserImage()).into(imageAvatar);
        }

        if (itemDashBoard.getCurrentPlan().isEmpty()) {
            textCurrentPlan.setText(getString(R.string.n_a));
        } else {
            textCurrentPlan.setText(itemDashBoard.getCurrentPlan());
        }

        if (itemDashBoard.getExpiresOn().isEmpty()) {
            textExpiresOn.setText(getString(R.string.expire_on, getString(R.string.n_a)));
        } else {
            textExpiresOn.setText(getString(R.string.expire_on, itemDashBoard.getExpiresOn()));
        }
    }

    private void deleteAccountConfirm() {
        new AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.delete_account))
                .setMessage(getString(R.string.delete_account_msg))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAccount();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.mipmap.ic_launcher_round)
                .show();
    }

    private void deleteAccount() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getUserId());
        jsObj.addProperty("user_session_name", myApplication.getUserSession());
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.DELETE_USER_URL, params, new AsyncHttpResponseHandler() {

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
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                        Toast.makeText(requireActivity(), objJson.getString(Constant.MSG), Toast.LENGTH_SHORT).show();
                        if (Constant.GET_SUCCESS_MSG == 1) {
                            ActivityCompat.finishAffinity(requireActivity());

                            myApplication.saveIsLogin(false);
                            OneSignal.getUser().addTag("user_session", "");
                            myApplication.saveDeviceLimit(false);
                            Intent intent = new Intent(requireActivity(), SignInActivity.class);
                            intent.putExtra("isLogout", true);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            requireActivity().finish();
                        }
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
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }
}
