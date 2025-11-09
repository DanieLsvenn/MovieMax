package com.example.moviemax.Api;
import com.example.moviemax.Model.PaymentDto.PaymentRequest;
import com.example.moviemax.Model.PaymentDto.PaymentResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
public interface PaymentApi {

    // MoMo Payment
    @POST("/api/payments/momo/create")
    Call<PaymentResponse> createMoMoPayment(@Body PaymentRequest request);

    @GET("/api/payments/momo/callback")
    Call<PaymentResponse> momoCallback(@Query("orderId") String orderId,
                                       @Query("resultCode") int resultCode);

    // ZaloPay Payment
    @POST("/api/payments/zalopay/create")
    Call<PaymentResponse> createZaloPayPayment(@Body PaymentRequest request);

    @GET("/api/payments/zalopay/callback")
    Call<PaymentResponse> zaloPayCallback(@Query("apptransid") String appTransId,
                                          @Query("status") int status);

    // Cash Payment
    @POST("/api/payments/cash/create")
    Call<PaymentResponse> createCashPayment(@Body PaymentRequest request);

    // Check payment status
    @GET("/api/payments/{id}/status")
    Call<PaymentResponse> getPaymentStatus(@Path("id") long paymentId);

    @GET("/api/payments/booking/{bookingId}")
    Call<PaymentResponse> getPaymentByBookingId(@Path("bookingId") long bookingId);

}