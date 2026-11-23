package com.crtc.flipiq.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Google Play Billing wrapper for FlipIQ Pro subscription. */
class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_PRO = "flipiq_pro_monthly"
        const val FREE_DAILY_SCANS = 3
    }

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryExistingPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    fun launchPurchaseFlow(activity: Activity) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_PRO)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )).build()

        billingClient.queryProductDetailsAsync(params) { result, productDetailsList ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            val product = productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync
            val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@queryProductDetailsAsync
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .setOfferToken(offerToken)
                        .build()
                )).build()
            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach { handlePurchase(it) }
        }
    }

    private fun queryExistingPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { _, purchases ->
            _isPro.value = purchases.any {
                it.products.contains(PRODUCT_PRO) &&
                it.purchaseState == Purchase.PurchaseState.PURCHASED
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) {}
            }
            _isPro.value = true
        }
    }

    fun disconnect() = billingClient.endConnection()
}
