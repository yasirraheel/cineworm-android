package com.cineworm.item;

public class ItemPaymentSetting {

    private String currencyCode;
    private String stripePublisherKey;
    private String razorPayKey;
    private String payStackPublicKey;
    private boolean isPayPal = false;
    private boolean isStripe = false;
    private boolean isRazorPay = false;
    private boolean isPayStack = false;
    private boolean isPayUMoney = false;
    private boolean isPayUMoneySandbox = false;
    private String payUMoneyMerchantId;
    private String payUMoneyMerchantKey;
    private boolean isInstaMojo = false;
    private boolean isInstaMojoSandbox = false;
    private boolean isPayTM = false;
    private boolean isPayTMSandbox = false;
    private String payTMMid;
    private boolean isCashFree = false;
    private boolean isCashFreeSandbox = false;
    private String cashFreeAppId;
    private boolean isFlutterWave = false;
    private String fwPublicKey;
    private String fwEncryptionKey;
    private boolean isCoinGate = false;
    private boolean isMollie = false;
    private boolean isBankTransfer = false;
    private String bankTransferTitle;
    private String bankTransferInfo;

    private boolean isSsl = false;
    private boolean isSslSandbox = false;
    private String sslStoreId;
    private String sslStorePassword;

    private boolean isCinetPay = false;
    private String cpApiKey;
    private String cpSiteId;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public boolean isPayPal() {
        return isPayPal;
    }

    public void setPayPal(boolean payPal) {
        isPayPal = payPal;
    }

    public boolean isStripe() {
        return isStripe;
    }

    public void setStripe(boolean stripe) {
        isStripe = stripe;
    }

    public boolean isRazorPay() {
        return isRazorPay;
    }

    public void setRazorPay(boolean razorPay) {
        isRazorPay = razorPay;
    }

    public String getStripePublisherKey() {
        return stripePublisherKey;
    }

    public void setStripePublisherKey(String stripePublisherKey) {
        this.stripePublisherKey = stripePublisherKey;
    }

    public String getRazorPayKey() {
        return razorPayKey;
    }

    public void setRazorPayKey(String razorPayKey) {
        this.razorPayKey = razorPayKey;
    }

    public String getPayStackPublicKey() {
        return payStackPublicKey;
    }

    public void setPayStackPublicKey(String payStackPublicKey) {
        this.payStackPublicKey = payStackPublicKey;
    }

    public boolean isPayStack() {
        return isPayStack;
    }

    public void setPayStack(boolean payStack) {
        isPayStack = payStack;
    }

    public boolean isPayUMoney() {
        return isPayUMoney;
    }

    public void setPayUMoney(boolean payUMoney) {
        isPayUMoney = payUMoney;
    }

    public boolean isPayUMoneySandbox() {
        return isPayUMoneySandbox;
    }

    public void setPayUMoneySandbox(boolean payUMoneySandbox) {
        isPayUMoneySandbox = payUMoneySandbox;
    }

    public String getPayUMoneyMerchantId() {
        return payUMoneyMerchantId;
    }

    public void setPayUMoneyMerchantId(String payUMoneyMerchantId) {
        this.payUMoneyMerchantId = payUMoneyMerchantId;
    }

    public String getPayUMoneyMerchantKey() {
        return payUMoneyMerchantKey;
    }

    public void setPayUMoneyMerchantKey(String payUMoneyMerchantKey) {
        this.payUMoneyMerchantKey = payUMoneyMerchantKey;
    }

    public boolean isInstaMojo() {
        return isInstaMojo;
    }

    public void setInstaMojo(boolean instaMojo) {
        isInstaMojo = instaMojo;
    }

    public boolean isInstaMojoSandbox() {
        return isInstaMojoSandbox;
    }

    public void setInstaMojoSandbox(boolean instaMojoSandbox) {
        isInstaMojoSandbox = instaMojoSandbox;
    }

    public boolean isPayTM() {
        return isPayTM;
    }

    public void setPayTM(boolean payTM) {
        isPayTM = payTM;
    }

    public boolean isPayTMSandbox() {
        return isPayTMSandbox;
    }

    public void setPayTMSandbox(boolean payTMSandbox) {
        isPayTMSandbox = payTMSandbox;
    }

    public String getPayTMMid() {
        return payTMMid;
    }

    public void setPayTMMid(String payTMMid) {
        this.payTMMid = payTMMid;
    }

    public boolean isCashFree() {
        return isCashFree;
    }

    public void setCashFree(boolean cashFree) {
        isCashFree = cashFree;
    }

    public boolean isCashFreeSandbox() {
        return isCashFreeSandbox;
    }

    public void setCashFreeSandbox(boolean cashFreeSandbox) {
        isCashFreeSandbox = cashFreeSandbox;
    }

    public String getCashFreeAppId() {
        return cashFreeAppId;
    }

    public void setCashFreeAppId(String cashFreeAppId) {
        this.cashFreeAppId = cashFreeAppId;
    }

    public boolean isFlutterWave() {
        return isFlutterWave;
    }

    public void setFlutterWave(boolean flutterWave) {
        isFlutterWave = flutterWave;
    }

    public String getFwPublicKey() {
        return fwPublicKey;
    }

    public void setFwPublicKey(String fwPublicKey) {
        this.fwPublicKey = fwPublicKey;
    }

    public String getFwEncryptionKey() {
        return fwEncryptionKey;
    }

    public void setFwEncryptionKey(String fwEncryptionKey) {
        this.fwEncryptionKey = fwEncryptionKey;
    }

    public boolean isCoinGate() {
        return isCoinGate;
    }

    public void setCoinGate(boolean coinGate) {
        isCoinGate = coinGate;
    }

    public boolean isMollie() {
        return isMollie;
    }

    public void setMollie(boolean mollie) {
        isMollie = mollie;
    }

    public boolean isBankTransfer() {
        return isBankTransfer;
    }

    public void setBankTransfer(boolean bankTransfer) {
        isBankTransfer = bankTransfer;
    }

    public String getBankTransferTitle() {
        return bankTransferTitle;
    }

    public void setBankTransferTitle(String bankTransferTitle) {
        this.bankTransferTitle = bankTransferTitle;
    }

    public String getBankTransferInfo() {
        return bankTransferInfo;
    }

    public void setBankTransferInfo(String bankTransferInfo) {
        this.bankTransferInfo = bankTransferInfo;
    }

    public boolean isSsl() {
        return isSsl;
    }

    public void setSsl(boolean ssl) {
        isSsl = ssl;
    }

    public boolean isSslSandbox() {
        return isSslSandbox;
    }

    public void setSslSandbox(boolean sslSandbox) {
        isSslSandbox = sslSandbox;
    }

    public String getSslStoreId() {
        return sslStoreId;
    }

    public void setSslStoreId(String sslStoreId) {
        this.sslStoreId = sslStoreId;
    }

    public String getSslStorePassword() {
        return sslStorePassword;
    }

    public void setSslStorePassword(String sslStorePassword) {
        this.sslStorePassword = sslStorePassword;
    }

    public boolean isCinetPay() {
        return isCinetPay;
    }

    public void setCinetPay(boolean cinetPay) {
        isCinetPay = cinetPay;
    }

    public String getCpApiKey() {
        return cpApiKey;
    }

    public void setCpApiKey(String cpApiKey) {
        this.cpApiKey = cpApiKey;
    }

    public String getCpSiteId() {
        return cpSiteId;
    }

    public void setCpSiteId(String cpSiteId) {
        this.cpSiteId = cpSiteId;
    }
}
