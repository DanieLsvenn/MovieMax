package com.example.moviemax.Helper;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PaymentHelper {
    private static final String TAG = "PaymentHelper";

    // MoMo Sandbox Credentials
    private static final String MOMO_ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create";
    private static final String MOMO_PARTNER_CODE = "MOMO";
    private static final String MOMO_ACCESS_KEY = "F8BBA842ECF85";
    private static final String MOMO_SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private static final String MOMO_REDIRECT_URL = "moviemax://payment/momo";
    private static final String MOMO_IPN_URL = "https://webhook.site/your-webhook-url";

    // ZaloPay Sandbox Credentials
    private static final String ZALOPAY_ENDPOINT = "https://sb-openapi.zalopay.vn/v2/create";
    private static final String ZALOPAY_APP_ID = "2553";
    private static final String ZALOPAY_KEY1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
    private static final String ZALOPAY_REDIRECT_URL = "moviemax://payment/zalopay";

    /**
     * Create MoMo payment request
     * @param bookingId Booking ID
     * @param amount Payment amount
     * @param orderInfo Order description
     * @return Payment URL to open in browser, or null if failed
     */
    public static String createMoMoPayment(long bookingId, double amount, String orderInfo) {
        try {
            String orderId = "BOOKING_" + bookingId + "_" + System.currentTimeMillis();
            String requestId = orderId;
            String requestType = "payWithMethod"; // Allows user to choose payment method
            String extraData = "";

            // Create raw signature string (order is critical)
            String rawSignature = "accessKey=" + MOMO_ACCESS_KEY +
                    "&amount=" + (long) amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + MOMO_IPN_URL +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + MOMO_PARTNER_CODE +
                    "&redirectUrl=" + MOMO_REDIRECT_URL +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            Log.d(TAG, "MoMo Raw Signature: " + rawSignature);

            // Generate HMAC SHA256 signature
            String signature = hmacSHA256(rawSignature, MOMO_SECRET_KEY);

            // Create request body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("partnerCode", MOMO_PARTNER_CODE);
            jsonBody.put("partnerName", "MovieMax");
            jsonBody.put("storeId", "MovieMaxStore");
            jsonBody.put("requestId", requestId);
            jsonBody.put("amount", (long) amount);
            jsonBody.put("orderId", orderId);
            jsonBody.put("orderInfo", orderInfo);
            jsonBody.put("redirectUrl", MOMO_REDIRECT_URL);
            jsonBody.put("ipnUrl", MOMO_IPN_URL);
            jsonBody.put("lang", "vi");
            jsonBody.put("extraData", extraData);
            jsonBody.put("requestType", requestType);
            jsonBody.put("signature", signature);
            jsonBody.put("accessKey", MOMO_ACCESS_KEY);

            Log.d(TAG, "MoMo Request Body: " + jsonBody.toString());

            // Send POST request
            String response = sendPostRequest(MOMO_ENDPOINT, jsonBody.toString());
            Log.d(TAG, "MoMo Response: " + response);

            // Parse response
            JSONObject jsonResponse = new JSONObject(response);
            int resultCode = jsonResponse.optInt("resultCode", -1);
            String message = jsonResponse.optString("message", "Unknown error");

            if (resultCode == 0 && jsonResponse.has("payUrl")) {
                String payUrl = jsonResponse.getString("payUrl");
                Log.d(TAG, "✅ MoMo Payment URL created successfully");
                return payUrl;
            } else {
                Log.e(TAG, "❌ MoMo Error [" + resultCode + "]: " + message);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ MoMo Payment Exception: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Create ZaloPay payment request
     * @param bookingId Booking ID
     * @param amount Payment amount
     * @param description Order description
     * @return Payment URL to open in browser, or null if failed
     */
    public static String createZaloPayPayment(long bookingId, double amount, String description) {
        try {
            // 1. Generate app_trans_id with format: yyMMdd_xxxx
            String appTransId = getCurrentTimestamp("yyMMdd") + "_" + (System.currentTimeMillis() % 1000000);

            // 2. App user
            String appUser = "user_" + bookingId;

            // 3. Current timestamp in milliseconds
            long appTime = System.currentTimeMillis();

            // 4. Create embed_data as JSONObject then convert to string
            JSONObject embedDataObj = new JSONObject();
            embedDataObj.put("redirecturl", ZALOPAY_REDIRECT_URL);
            String embedDataStr = embedDataObj.toString();

            // 5. Create item array as JSONArray then convert to string
            JSONArray itemsArr = new JSONArray();
            JSONObject itemObj = new JSONObject();
            itemObj.put("itemid", "booking_" + bookingId);
            itemObj.put("itemname", description);
            itemObj.put("itemprice", (long) amount);
            itemObj.put("itemquantity", 1);
            itemsArr.put(itemObj);
            String itemStr = itemsArr.toString();

            // 6. Create MAC data string (order is critical)
            String data = ZALOPAY_APP_ID + "|" +
                    appTransId + "|" +
                    appUser + "|" +
                    (long) amount + "|" +
                    appTime + "|" +
                    embedDataStr + "|" +
                    itemStr;

            Log.d(TAG, "ZaloPay MAC Data: " + data);

            // 7. Generate MAC using HMAC SHA256
            String mac = hmacSHA256(data, ZALOPAY_KEY1);

            Log.d(TAG, "ZaloPay MAC: " + mac);

            // 8. Create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("app_id", Integer.parseInt(ZALOPAY_APP_ID));
            requestBody.put("app_user", appUser);
            requestBody.put("app_time", appTime);
            requestBody.put("amount", (long) amount);
            requestBody.put("app_trans_id", appTransId);
            requestBody.put("bank_code", "");
            requestBody.put("embed_data", embedDataStr);  // String, not JSONObject
            requestBody.put("item", itemStr);              // String, not JSONArray
            requestBody.put("callback_url", ZALOPAY_REDIRECT_URL);
            requestBody.put("description", description);
            requestBody.put("mac", mac);

            Log.d(TAG, "ZaloPay Request Body: " + requestBody.toString());

            // 9. Send POST request
            String response = sendPostRequest(ZALOPAY_ENDPOINT, requestBody.toString());
            Log.d(TAG, "ZaloPay Response: " + response);

            // 10. Parse response
            JSONObject jsonResponse = new JSONObject(response);
            int returnCode = jsonResponse.optInt("return_code", -1);
            String returnMessage = jsonResponse.optString("return_message", "Unknown error");

            if (returnCode == 1 && jsonResponse.has("order_url")) {
                String orderUrl = jsonResponse.getString("order_url");
                Log.d(TAG, "✅ ZaloPay Payment URL created successfully");
                return orderUrl;
            } else {
                int subReturnCode = jsonResponse.optInt("sub_return_code", 0);
                String subReturnMessage = jsonResponse.optString("sub_return_message", "");

                Log.e(TAG, "❌ ZaloPay Error [" + returnCode + "]: " + returnMessage);
                Log.e(TAG, "Sub Error [" + subReturnCode + "]: " + subReturnMessage);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ ZaloPay Payment Exception: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate HMAC SHA256 signature
     * @param data Data to sign
     * @param key Secret key
     * @return Hex string of signature
     */
    private static String hmacSHA256(String data, String key) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKey);
        byte[] hmacBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hmacBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Send POST request to payment gateway
     * @param endpoint API endpoint
     * @param jsonBody JSON request body
     * @return Response string
     */
    private static String sendPostRequest(String endpoint, String jsonBody) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000); // 30 seconds
        conn.setReadTimeout(30000);

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        BufferedReader br;
        if (responseCode >= 200 && responseCode < 300) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line.trim());
        }
        br.close();

        return response.toString();
    }

    /**
     * Get current timestamp in specified format with GMT+7 timezone
     * @param format Date format (e.g., "yyMMdd")
     * @return Formatted timestamp string
     */
    private static String getCurrentTimestamp(String format) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, java.util.Locale.getDefault());
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("GMT+7");
        sdf.setTimeZone(tz);
        return sdf.format(new java.util.Date());
    }
}