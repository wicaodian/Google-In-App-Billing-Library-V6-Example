package com.memo.iapsample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;

public class NonConsumable extends AppCompatActivity {

    private final String PRODUCT_PREMIUM = "lifetime";
    private final String NoAds = "NoAds";
    private ArrayList<String> purchaseItemIDs = new ArrayList<String>() {{
        add(PRODUCT_PREMIUM);
        add(NoAds);
    }};

    private String TAG = "iapSample";

    private BillingClient billingClient;

    Button btn_premium, btn_restore;
    TextView tv_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_consumable);

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(
                        (billingResult, list) -> {

                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                for (Purchase purchase : list) {

                                    Log.d(TAG, "Response is OK");
                                    handlePurchase(purchase);
                                }
                            } else {

                                Log.d(TAG, "Response NOT OK");
                            }
                        }
                ).build();

        //start the connection after initializing the billing client
        establishConnection();
        init();
    }

    void init() {
        btn_premium = this.findViewById(R.id.btn_premium);
        btn_restore = this.findViewById(R.id.btn_restore);
        tv_status = this.findViewById(R.id.tv_status);

        btn_premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetSingleInAppDetail();
            }
        });

        btn_restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restorePurchases();
            }
        });
    }


    void establishConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    // The BillingClient is ready. You can query purchases here.

                    //Use any of function below to get details upon successful connection

                    // GetSingleInAppDetail();
                    //GetListsInAppDetail();

                    Log.d(TAG, "Connection Established");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d(TAG, "Connection NOT Established");
                establishConnection();
            }
        });
    }

    /*
     *
     * The official examples use an ImmutableList for some reason to build the query,
     * but you don't actually need to use that.
     * The setProductList method just takes List<Product> as its input, it does not require ImmutableList.
     *
     * */

    /*
     * If you have API < 24, you could just make an ArrayList instead.
     * */

    void GetSingleInAppDetail() {
        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        //Set your In App Product ID in setProductId()
        productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_PREMIUM)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {

                //Do Anything that you want with requested product details

                //Calling this function here so that once products are verified we can start the purchase behavior.
                //You can save this detail in separate variable or list to call them from any other location
                //Create another function if you want to call this in establish connections' success state
                LaunchPurchaseFlow(list.get(0));


            }
        });
    }


    void GetListsInAppDetail() {
        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        //Set your In App Product ID in setProductId()
        for (String ids : purchaseItemIDs) {
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(ids)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {

                for (ProductDetails li : list) {
                    Log.d(TAG, "IN APP item Price" + li.getOneTimePurchaseOfferDetails().getFormattedPrice());
                }
                //Do Anything that you want with requested product details
            }
        });
    }

    void LaunchPurchaseFlow(ProductDetails productDetails) {
        ArrayList<BillingFlowParams.ProductDetailsParams> productList = new ArrayList<>();

        productList.add(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build());

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productList)
                .build();

        billingClient.launchBillingFlow(this, billingFlowParams);
    }

    void handlePurchase(Purchase purchases) {
        if (!purchases.isAcknowledged()) {
            billingClient.acknowledgePurchase(AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchases.getPurchaseToken())
                    .build(), billingResult -> {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (String pur : purchases.getProducts()) {
                        if (pur.equalsIgnoreCase(PRODUCT_PREMIUM)) {
                            Log.d("TAG", "Purchase is successful" + pur);
                            tv_status.setText("Yay! Purchased");
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    void restorePurchases() {

        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {
        }).build();
        final BillingClient finalBillingClient = billingClient;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), (billingResult1, list) -> {
                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    if (list.size() > 0) {

                                        Log.d("TAG", "IN APP SUCCESS RESTORE: " + list);
                                        for (int i = 0; i < list.size(); i++) {

                                            if (list.get(i).getProducts().contains(PRODUCT_PREMIUM)) {
                                                tv_status.setText("Premium Restored");
                                                Log.d("TAG", "Product id " + PRODUCT_PREMIUM + " will restore here");
                                            }

                                        }
                                    } else {
                                        tv_status.setText("Nothing found to Restored");
                                        Log.d("TAG", "In APP Not Found To Restore");
                                    }
                                }
                            });
                }
            }
        });
    }
}