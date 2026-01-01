package com.cineworm.videostreamingapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cinetpay.androidsdk.CinetPayActivity;
import com.cineworm.item.ItemPaymentSetting;
import com.cineworm.util.API;
import com.cineworm.util.Constant;
import com.cineworm.util.IsRTL;
import com.cineworm.util.NetworkUtils;
import com.cineworm.util.StatusBarUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tuyenmonkey.textdecorator.TextDecorator;
import com.tuyenmonkey.textdecorator.callback.OnTextClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SelectPlanActivity extends AppCompatActivity {

    String planId, planName, planPrice, planDuration;
    TextView textPlanName, textPlanPrice, textPlanDuration, textChangePlan, textPlanCurrency, textNoPaymentGateway, tvPlanDesc, tvCurrentPlan, tvCouponCode;
    LinearLayout lytProceed;
    RadioButton radioPayPal, radioStripe, radioRazorPay, radioPayStack, radioInstaMojo, radioPayU, radioPayTM, radioCashFree, radioFlutterWave, radioCoinGate, radioMollie, radioSsl, radioCinetPay, radioBankTransfer;
    MyApplication myApplication;
    ProgressBar mProgressBar;
    LinearLayout lyt_not_found;
    RelativeLayout lytDetails;
    ItemPaymentSetting paymentSetting;
    RadioGroup radioGroup;
    ImageView imageClose;
    ProgressDialog pDialog;
    boolean isCouponCodeUsed = false;
    String couponCode = "", couponPercentage = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.setStatusBarBlack(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_plan);
        IsRTL.ifSupported(this);

        myApplication = MyApplication.getInstance();
        paymentSetting = new ItemPaymentSetting();

        final Intent intent = getIntent();
        planId = intent.getStringExtra("planId");
        planName = intent.getStringExtra("planName");
        planPrice = intent.getStringExtra("planPrice");
        planDuration = intent.getStringExtra("planDuration");

        mProgressBar = findViewById(R.id.progressBar1);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        lytDetails = findViewById(R.id.lytDetails);
        textPlanName = findViewById(R.id.textPackName);
        textPlanPrice = findViewById(R.id.textPrice);
        textPlanCurrency = findViewById(R.id.textCurrency);
        textPlanDuration = findViewById(R.id.textDay);
        tvPlanDesc = findViewById(R.id.tvPlanDesc);
        textChangePlan = findViewById(R.id.changePlan);
        tvCurrentPlan = findViewById(R.id.textCurrentPlan);
        tvCouponCode = findViewById(R.id.tvCouponCode);
        lytProceed = findViewById(R.id.lytProceed);
        radioPayPal = findViewById(R.id.rdPaypal);
        radioStripe = findViewById(R.id.rdStripe);
        radioRazorPay = findViewById(R.id.rdRazorPay);
        radioPayStack = findViewById(R.id.rdPayStack);
        radioInstaMojo = findViewById(R.id.rdInstaMojo);
        radioPayTM = findViewById(R.id.rdPayTM);
        radioPayU = findViewById(R.id.rdPayUMoney);
        radioCashFree = findViewById(R.id.rdCashFree);
        radioFlutterWave = findViewById(R.id.rdFlutterWave);
        radioCoinGate = findViewById(R.id.rdCoinGate);
        radioMollie = findViewById(R.id.rdMollie);
        radioSsl = findViewById(R.id.rdSsl);
        radioCinetPay = findViewById(R.id.rdCinetPay);
        radioBankTransfer = findViewById(R.id.rdBankTransfer);
        textNoPaymentGateway = findViewById(R.id.textNoPaymentGateway);
        radioGroup = findViewById(R.id.radioGrp);
        imageClose = findViewById(R.id.imageClose);
        pDialog = new ProgressDialog(this);

        textPlanName.setText(planName);
        tvCurrentPlan.setText(planName);
        textPlanPrice.setText(planPrice);
        textPlanDuration.setText(getString(R.string.plan_day_for, planDuration));

        textChangePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        lytProceed.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View view) {
                int radioSelected = radioGroup.getCheckedRadioButtonId();
                if (radioSelected != -1) {
                    switch (radioSelected) {
                        case R.id.rdPaypal:
                            goPayPal();
                            break;
                        case R.id.rdStripe:
                            goStripe();
                            break;
                        case R.id.rdRazorPay:
                            goRazorPay();
                            break;
                        case R.id.rdPayStack:
                            goPayStack();
                            break;
                        case R.id.rdInstaMojo:
                            goInstaMojo();
                            break;
                        case R.id.rdPayUMoney:
                            goPayUMoney();
                            break;
                        case R.id.rdPayTM:
                            goPayTm();
                            break;
                        case R.id.rdCashFree:
                            goCashFree();
                            break;
                        case R.id.rdFlutterWave:
                            goFlutterWave();
                            break;
                        case R.id.rdCoinGate:
                            goCoinGate();
                            break;
                        case R.id.rdMollie:
                            goMollie();
                            break;
                        case R.id.rdSsl:
                            goSslCommerz();
                            break;
                        case R.id.rdCinetPay:
                            goCinetPay();
                            break;
                        case R.id.rdBankTransfer:
                            goBankTransfer();
                            break;
                    }
                } else {
                    Toast.makeText(SelectPlanActivity.this, getString(R.string.select_gateway), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (NetworkUtils.isConnected(SelectPlanActivity.this)) {
            getPaymentSetting();
        } else {
            Toast.makeText(SelectPlanActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }

        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tvCouponCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCouponCodeUsed) {
                    showToast(getString(R.string.already_coupon));
                } else {
                    couponCodeDialog();
                }
            }
        });

        buildPlanDesc();
    }

    private void getPaymentSetting() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.PAYMENT_SETTING_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mProgressBar.setVisibility(View.VISIBLE);
                lytDetails.setVisibility(View.GONE);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mProgressBar.setVisibility(View.GONE);
                lytDetails.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    paymentSetting.setCurrencyCode(mainJson.getString(Constant.CURRENCY_CODE));

                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject objJson = jsonArray.getJSONObject(i);
                            String gatewayName = objJson.getString("gateway_name");
                            String gatewayId = objJson.getString("gateway_id");
                            boolean status = objJson.getBoolean("status");
                            JSONObject gatewayInfoJson = objJson.getJSONObject("gateway_info");
                            switch (gatewayId) {
                                case "13":
                                    radioPayPal.setText(gatewayName);
                                    paymentSetting.setPayPal(status);
                                    break;
                                case "2":
                                    radioStripe.setText(gatewayName);
                                    paymentSetting.setStripe(status);
                                    paymentSetting.setStripePublisherKey(gatewayInfoJson.getString(Constant.STRIPE_PUBLISHER));
                                    break;
                                case "3":
                                    radioRazorPay.setText(gatewayName);
                                    paymentSetting.setRazorPay(status);
                                    paymentSetting.setRazorPayKey(gatewayInfoJson.getString(Constant.RAZOR_PAY_KEY));
                                    break;
                                case "4":
                                    radioPayStack.setText(gatewayName);
                                    paymentSetting.setPayStack(status);
                                    paymentSetting.setPayStackPublicKey(gatewayInfoJson.getString(Constant.PAY_STACK_KEY));
                                    break;
                                case "5":
                                    radioInstaMojo.setText(gatewayName);
                                    paymentSetting.setInstaMojo(status);
                                    paymentSetting.setInstaMojoSandbox(gatewayInfoJson.getString(Constant.PAYMENT_MODE).equals("sandbox"));
                                    break;
                                case "6":
                                    radioPayU.setText(gatewayName);
                                    paymentSetting.setPayUMoney(status);
                                    paymentSetting.setPayUMoneySandbox(gatewayInfoJson.getString(Constant.PAYMENT_MODE).equals("sandbox"));
                                    paymentSetting.setPayUMoneyMerchantId(gatewayInfoJson.getString(Constant.PAY_U_MERCHANT_ID));
                                    paymentSetting.setPayUMoneyMerchantKey(gatewayInfoJson.getString(Constant.PAY_U_MERCHANT_KEY));
                                    break;
                                case "7":
                                    radioMollie.setText(gatewayName);
                                    paymentSetting.setMollie(status);
                                    break;
                                case "8":
                                    radioFlutterWave.setText(gatewayName);
                                    paymentSetting.setFlutterWave(status);
                                    paymentSetting.setFwPublicKey(gatewayInfoJson.getString(Constant.FW_PUBLIC_KEY));
                                    paymentSetting.setFwEncryptionKey(gatewayInfoJson.getString(Constant.FW_ENCRYPTION_KEY));
                                    break;
                                case "9":
                                    radioPayTM.setText(gatewayName);
                                    paymentSetting.setPayTM(status);
                                    paymentSetting.setPayTMSandbox(gatewayInfoJson.getString(Constant.PAYMENT_MODE).equals("sandbox"));
                                    paymentSetting.setPayTMMid(gatewayInfoJson.getString(Constant.PAYTM_MID));
                                    break;
                                case "10":
                                    radioCashFree.setText(gatewayName);
                                    paymentSetting.setCashFree(status);
                                    paymentSetting.setCashFreeSandbox(gatewayInfoJson.getString(Constant.PAYMENT_MODE).equals("sandbox"));
                                    paymentSetting.setCashFreeAppId(gatewayInfoJson.getString(Constant.CASHFREE_APPID));
                                    break;
                                case "11":
                                    radioCoinGate.setText(gatewayName);
                                    paymentSetting.setCoinGate(status);
                                    break;
                                case "12":
                                    radioBankTransfer.setText(gatewayName);
                                    paymentSetting.setBankTransfer(status);
                                    paymentSetting.setBankTransferTitle(gatewayName);
                                    paymentSetting.setBankTransferInfo(gatewayInfoJson.getString(Constant.BANK_TRANSFER_INFO));
                                    break;
                                case "14":
                                    radioSsl.setText(gatewayName);
                                    paymentSetting.setSsl(status);
                                    paymentSetting.setSslSandbox(gatewayInfoJson.getString(Constant.PAYMENT_MODE).equals("sandbox"));
                                    paymentSetting.setSslStoreId(gatewayInfoJson.getString(Constant.SL_STORE_ID));
                                    paymentSetting.setSslStorePassword(gatewayInfoJson.getString(Constant.SL_STORE_PASSWORD));
                                    break;
                                case "15":
                                    radioCinetPay.setText(gatewayName);
                                    paymentSetting.setCinetPay(status);
                                    paymentSetting.setCpApiKey(gatewayInfoJson.getString(Constant.CP_API_KEY));
                                    paymentSetting.setCpSiteId(gatewayInfoJson.getString(Constant.CP_SITE_ID));
                                    break;
                            }
                        }
                        displayData();
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        lytDetails.setVisibility(View.GONE);
                        lyt_not_found.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    mProgressBar.setVisibility(View.GONE);
                    lytDetails.setVisibility(View.GONE);
                    lyt_not_found.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error) {
                mProgressBar.setVisibility(View.GONE);
                lytDetails.setVisibility(View.GONE);
                lyt_not_found.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayData() {
        textPlanCurrency.setText(paymentSetting.getCurrencyCode());

        radioPayPal.setVisibility(paymentSetting.isPayPal() ? View.VISIBLE : View.GONE);
        radioStripe.setVisibility(paymentSetting.isStripe() ? View.VISIBLE : View.GONE);
        radioRazorPay.setVisibility(paymentSetting.isRazorPay() ? View.VISIBLE : View.GONE);
        radioPayStack.setVisibility(paymentSetting.isPayStack() ? View.VISIBLE : View.GONE);
        radioInstaMojo.setVisibility(paymentSetting.isInstaMojo() ? View.VISIBLE : View.GONE);
        radioPayU.setVisibility(paymentSetting.isPayUMoney() ? View.VISIBLE : View.GONE);
        radioPayTM.setVisibility(paymentSetting.isPayTM() ? View.VISIBLE : View.GONE);
        radioCashFree.setVisibility(paymentSetting.isCashFree() ? View.VISIBLE : View.GONE);
        radioFlutterWave.setVisibility(paymentSetting.isFlutterWave() ? View.VISIBLE : View.GONE);
        radioCoinGate.setVisibility(paymentSetting.isCoinGate() ? View.VISIBLE : View.GONE);
        radioMollie.setVisibility(paymentSetting.isMollie() ? View.VISIBLE : View.GONE);
        radioBankTransfer.setVisibility(paymentSetting.isBankTransfer() ? View.VISIBLE : View.GONE);
        radioSsl.setVisibility(paymentSetting.isSsl() ? View.VISIBLE : View.GONE);
        radioCinetPay.setVisibility(paymentSetting.isCinetPay() ? View.VISIBLE : View.GONE);

        if (!paymentSetting.isPayPal() && !paymentSetting.isStripe()
                && !paymentSetting.isRazorPay() && !paymentSetting.isPayStack()
                && !paymentSetting.isInstaMojo() && !paymentSetting.isPayUMoney()
                && !paymentSetting.isPayTM() && !paymentSetting.isCashFree() && !paymentSetting.isFlutterWave() && !paymentSetting.isCoinGate() && !paymentSetting.isMollie() && !paymentSetting.isBankTransfer() && !paymentSetting.isSsl() && !paymentSetting.isCinetPay()) {
            textNoPaymentGateway.setVisibility(View.VISIBLE);
            lytProceed.setVisibility(View.GONE);
        }
    }

    private void buildPlanDesc() {
        TextDecorator
                .decorate(tvPlanDesc, getString(R.string.choose_plan, myApplication.getUserEmail()))
                .setTextColor(R.color.highlight, planName, myApplication.getUserEmail(), getString(R.string.menu_logout))
                .makeTextClickable(new OnTextClickListener() {
                    @Override
                    public void onClick(View view, String text) {
                        new LogoutOnline(SelectPlanActivity.this);
                    }
                }, false, getString(R.string.menu_logout))
                .setTextColor(R.color.highlight, getString(R.string.menu_logout))
                .build();
    }

    private void goPayPal() {
        Intent intentPayPal = new Intent(SelectPlanActivity.this, PayPalActivity.class);
        intentPayPal.putExtra("planId", planId);
        intentPayPal.putExtra("planPrice", planPrice);
        intentPayPal.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentPayPal.putExtra("planGateway", "Braintree");
        intentPayPal.putExtra("planGatewayText", getString(R.string.paypal));
        intentPayPal.putExtra("couponCode", couponCode);
        intentPayPal.putExtra("couponPercentage", couponPercentage);
        startActivity(intentPayPal);
    }

    private void goStripe() {
        Intent intentStripe = new Intent(SelectPlanActivity.this, StripeActivity.class);
        intentStripe.putExtra("planId", planId);
        intentStripe.putExtra("planPrice", planPrice);
        intentStripe.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentStripe.putExtra("planGateway", "Stripe");
        intentStripe.putExtra("planGatewayText", getString(R.string.stripe));
        intentStripe.putExtra("stripePublisherKey", paymentSetting.getStripePublisherKey());
        intentStripe.putExtra("couponCode", couponCode);
        intentStripe.putExtra("couponPercentage", couponPercentage);
        startActivity(intentStripe);
    }

    private void goRazorPay() {
        Intent intentRazor = new Intent(SelectPlanActivity.this, RazorPayActivity.class);
        intentRazor.putExtra("planId", planId);
        intentRazor.putExtra("planName", planName);
        intentRazor.putExtra("planPrice", planPrice);
        intentRazor.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentRazor.putExtra("planGateway", "Razorpay");
        intentRazor.putExtra("planGatewayText", getString(R.string.razor_pay));
        intentRazor.putExtra("razorPayKey", paymentSetting.getRazorPayKey());
        intentRazor.putExtra("couponCode", couponCode);
        intentRazor.putExtra("couponPercentage", couponPercentage);
        startActivity(intentRazor);
    }

    private void goPayStack() {
        Intent intentPayStack = new Intent(SelectPlanActivity.this, PayStackActivity.class);
        intentPayStack.putExtra("planId", planId);
        intentPayStack.putExtra("planPrice", planPrice);
        intentPayStack.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentPayStack.putExtra("planGateway", "Paystack");
        intentPayStack.putExtra("planGatewayText", getString(R.string.pay_stack));
        intentPayStack.putExtra("payStackPublicKey", paymentSetting.getPayStackPublicKey());
        intentPayStack.putExtra("couponCode", couponCode);
        intentPayStack.putExtra("couponPercentage", couponPercentage);
        startActivity(intentPayStack);
    }

    private void goInstaMojo() {
        Intent intentInstaMojo = new Intent(SelectPlanActivity.this, InstaMojoActivity.class);
        intentInstaMojo.putExtra("planId", planId);
        intentInstaMojo.putExtra("planName", planName);
        intentInstaMojo.putExtra("planPrice", planPrice);
        intentInstaMojo.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentInstaMojo.putExtra("planGateway", "Instamojo");
        intentInstaMojo.putExtra("planGatewayText", getString(R.string.insta_mojo));
        intentInstaMojo.putExtra("isSandbox", paymentSetting.isInstaMojoSandbox());
        intentInstaMojo.putExtra("couponCode", couponCode);
        intentInstaMojo.putExtra("couponPercentage", couponPercentage);
        startActivity(intentInstaMojo);
    }

    private void goPayUMoney() {
        Intent intentPayU = new Intent(SelectPlanActivity.this, PayUProActivity.class);
        intentPayU.putExtra("planId", planId);
        intentPayU.putExtra("planName", planName);
        intentPayU.putExtra("planPrice", planPrice);
        intentPayU.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentPayU.putExtra("planGateway", "PayUMoney");
        intentPayU.putExtra("planGatewayText", getString(R.string.pay_u));
        intentPayU.putExtra("isSandbox", paymentSetting.isPayUMoneySandbox());
        intentPayU.putExtra("payUMoneyMerchantId", paymentSetting.getPayUMoneyMerchantId());
        intentPayU.putExtra("payUMoneyMerchantKey", paymentSetting.getPayUMoneyMerchantKey());
        intentPayU.putExtra("couponCode", couponCode);
        intentPayU.putExtra("couponPercentage", couponPercentage);
        startActivity(intentPayU);
    }

    private void goPayTm() {
        Intent intentPayTm = new Intent(SelectPlanActivity.this, PayTMActivity.class);
        intentPayTm.putExtra("planId", planId);
        intentPayTm.putExtra("planName", planName);
        intentPayTm.putExtra("planPrice", planPrice);
        intentPayTm.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentPayTm.putExtra("planGateway", "Paytm");
        intentPayTm.putExtra("planGatewayText", getString(R.string.paytm));
        intentPayTm.putExtra("isSandbox", paymentSetting.isPayTMSandbox());
        intentPayTm.putExtra("paytmMid", paymentSetting.getPayTMMid());
        intentPayTm.putExtra("couponCode", couponCode);
        intentPayTm.putExtra("couponPercentage", couponPercentage);
        startActivity(intentPayTm);
    }

    private void goCashFree() {
        Intent intentCashFree = new Intent(SelectPlanActivity.this, CashFreeActivity.class);
        intentCashFree.putExtra("planId", planId);
        intentCashFree.putExtra("planName", planName);
        intentCashFree.putExtra("planPrice", planPrice);
        intentCashFree.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentCashFree.putExtra("planGateway", "Cashfree");
        intentCashFree.putExtra("planGatewayText", getString(R.string.cash_free));
        intentCashFree.putExtra("isSandbox", paymentSetting.isCashFreeSandbox());
        intentCashFree.putExtra("cashFreeAppId", paymentSetting.getCashFreeAppId());
        intentCashFree.putExtra("couponCode", couponCode);
        intentCashFree.putExtra("couponPercentage", couponPercentage);
        startActivity(intentCashFree);
    }

    private void goFlutterWave() {
        Intent intentFlutterWave = new Intent(SelectPlanActivity.this, FlutterWaveActivity.class);
        intentFlutterWave.putExtra("planId", planId);
        intentFlutterWave.putExtra("planName", planName);
        intentFlutterWave.putExtra("planPrice", planPrice);
        intentFlutterWave.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentFlutterWave.putExtra("planGateway", "Flutterwave");
        intentFlutterWave.putExtra("planGatewayText", getString(R.string.flutter_wave));
        intentFlutterWave.putExtra("fwPublicKey", paymentSetting.getFwPublicKey());
        intentFlutterWave.putExtra("fwEncryptionKey", paymentSetting.getFwEncryptionKey());
        intentFlutterWave.putExtra("couponCode", couponCode);
        intentFlutterWave.putExtra("couponPercentage", couponPercentage);
        startActivity(intentFlutterWave);
    }

    private void goCoinGate() {
        Intent intentCoinGate = new Intent(SelectPlanActivity.this, CoinGateActivity.class);
        intentCoinGate.putExtra("planId", planId);
        intentCoinGate.putExtra("planName", planName);
        intentCoinGate.putExtra("planPrice", planPrice);
        intentCoinGate.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentCoinGate.putExtra("planGateway", "Coingate");
        intentCoinGate.putExtra("planGatewayText", getString(R.string.coin_gate));
        intentCoinGate.putExtra("couponCode", couponCode);
        intentCoinGate.putExtra("couponPercentage", couponPercentage);
        startActivity(intentCoinGate);
    }

    private void goMollie() {
        Intent intentCoinGate = new Intent(SelectPlanActivity.this, MollieActivity.class);
        intentCoinGate.putExtra("planId", planId);
        intentCoinGate.putExtra("planName", planName);
        intentCoinGate.putExtra("planPrice", planPrice);
        intentCoinGate.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentCoinGate.putExtra("planGateway", "Mollie");
        intentCoinGate.putExtra("planGatewayText", getString(R.string.mollie));
        intentCoinGate.putExtra("couponCode", couponCode);
        intentCoinGate.putExtra("couponPercentage", couponPercentage);
        startActivity(intentCoinGate);
    }

    private void goSslCommerz() {
        Intent intentSsl = new Intent(SelectPlanActivity.this, SslCommerzActivity.class);
        intentSsl.putExtra("planId", planId);
        intentSsl.putExtra("planName", planName);
        intentSsl.putExtra("planPrice", planPrice);
        intentSsl.putExtra("planCurrency", paymentSetting.getCurrencyCode());
        intentSsl.putExtra("planGateway", "SslCommerz");
        intentSsl.putExtra("planGatewayText", getString(R.string.ssl_commerz));
        intentSsl.putExtra("isSandbox", paymentSetting.isSslSandbox());
        intentSsl.putExtra("sslStoreId", paymentSetting.getSslStoreId());
        intentSsl.putExtra("sslStorePassword", paymentSetting.getSslStorePassword());
        intentSsl.putExtra("couponCode", couponCode);
        intentSsl.putExtra("couponPercentage", couponPercentage);
        startActivity(intentSsl);
    }

    private void goCinetPay() {
        double big = Double.parseDouble(planPrice);
        int amount = (int) (big);
        String transaction_id = getTransactionId();
        Intent intentCp = new Intent(SelectPlanActivity.this, MyCinetPayActivity.class);
        intentCp.putExtra(CinetPayActivity.KEY_API_KEY, paymentSetting.getCpApiKey());
        intentCp.putExtra(CinetPayActivity.KEY_SITE_ID, paymentSetting.getCpSiteId());
        intentCp.putExtra(CinetPayActivity.KEY_TRANSACTION_ID, transaction_id);
        intentCp.putExtra(CinetPayActivity.KEY_AMOUNT, amount);
        intentCp.putExtra(CinetPayActivity.KEY_CURRENCY, paymentSetting.getCurrencyCode());
        intentCp.putExtra(CinetPayActivity.KEY_DESCRIPTION, planName);
        intentCp.putExtra(CinetPayActivity.KEY_CHANNELS, "ALL"); //MOBILE_MONEY
        intentCp.putExtra("planId", planId);
        intentCp.putExtra("planGateway", "CinetPay");
        intentCp.putExtra("couponCode", couponCode);
        intentCp.putExtra("couponPercentage", couponPercentage);
        startActivity(intentCp);
    }

    public String getTransactionId() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        String orderId = String.format(Locale.getDefault(), "%06d", number);
        return "CP" + myApplication.getUserId() + orderId;
    }


    private void goBankTransfer() {
        Intent intentCoinGate = new Intent(SelectPlanActivity.this, BankTransferActivity.class);
        intentCoinGate.putExtra("btTitle", paymentSetting.getBankTransferTitle());
        intentCoinGate.putExtra("btInfo", paymentSetting.getBankTransferInfo());
        startActivity(intentCoinGate);
    }

    private void couponCodeDialog() {
        final Dialog mDialog = new Dialog(SelectPlanActivity.this, R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.dialog_coupon);
        Button buttonCancel = mDialog.findViewById(R.id.btn_cancel);
        Button btnSubmit = mDialog.findViewById(R.id.btn_submit);
        EditText edtCouponCode = mDialog.findViewById(R.id.edt_refCode);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String couponCode = edtCouponCode.getText().toString();
                if (!couponCode.isEmpty()) {
                    if (NetworkUtils.isConnected(SelectPlanActivity.this)) {
                        addCouponCode(couponCode);
                    } else {
                        showToast(getString(R.string.conne_msg1));
                    }
                    mDialog.dismiss();
                }

            }
        });
        mDialog.show();
    }

    private void addCouponCode(String couponCodeEnter) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("user_id", myApplication.getUserId());
        jsObj.addProperty("coupon_code", couponCodeEnter);
        params.put("data", API.toBase64(jsObj.toString()));

        client.post(Constant.APPLY_COUPON_URL, params, new AsyncHttpResponseHandler() {

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
                        showToast(objJson.getString(Constant.MSG));
                        if (Constant.GET_SUCCESS_MSG == 1) {
                            isCouponCodeUsed = true;
                            couponCode = couponCodeEnter;
                            couponPercentage = objJson.getString("coupon_percentage");
                            double planPriceConvert = Double.parseDouble(planPrice);
                            double couponPrice = planPriceConvert - (planPriceConvert * Integer.parseInt(couponPercentage) / 100);
                            planPrice = String.valueOf(couponPrice);
                            textPlanPrice.setText(planPrice);

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
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showToast(String msg) {
        Toast.makeText(SelectPlanActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
