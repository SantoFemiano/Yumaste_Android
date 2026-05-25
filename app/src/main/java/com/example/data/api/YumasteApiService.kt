package com.example.data.api

import com.example.data.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface YumasteApiService {

    // --- PUBLIC AUTHENTICATION & CATALOG LAYERS ---

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/registra")
    fun registra(@Body request: RegistrazioneRequest): Call<ResponseBody>

    @GET("api/public/boxes")
    fun getBoxes(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("categoria") categoria: String? = null,
        @Query("search") search: String? = null
    ): Call<BoxPageResponse>

    @GET("api/public/box/detail/{id}")
    fun getBoxDetail(@Path("id") id: Int): Call<DettaglioBoxData>

    @GET("api/public/box/{id}/generate-description")
    fun getAiDescription(@Path("id") id: Int): Call<ResponseBody>

    @POST("api/public/ai/recommend")
    fun getAiRecommend(@Body request: AiRecommendRequest): Call<AiResult>

    // --- SECURED USER SERVICES LAYERS (Bearer Token Intercepted) ---

    @GET("api/user/profile")
    fun getProfile(): Call<DatiUtente>

    @PUT("api/user/update/profile")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<DatiUtente>

    @PUT("api/user/update/profile/password")
    fun updatePassword(@Body request: UpdatePasswordRequest): Call<ResponseBody>

    @GET("api/user/indirizzo")
    fun getAddresses(): Call<List<Indirizzo>>

    @POST("api/user/insert/indirizzo")
    fun addAddress(@Body request: Indirizzo): Call<Indirizzo>

    @GET("api/user/cart")
    fun getCart(): Call<CarrelloResponse>

    @POST("api/user/cart/add")
    fun addToCart(@Body request: AddCartRequest): Call<ResponseBody>

    @PUT("api/user/cart/update")
    fun updateCartQuantity(@Body request: UpdateCartRequest): Call<ResponseBody>

    @DELETE("api/user/cart/remove/{boxId}")
    fun removeFromCart(@Path("boxId") boxId: Int): Call<ResponseBody>

    @POST("api/user/checkout")
    fun checkout(@Body request: CheckoutRequest): Call<CheckoutResponse>

    @GET("api/user/ordini")
    fun getOrders(): Call<List<Ordine>>

    @GET("api/user/ordine/{id}/dettagli")
    fun getOrderDetails(@Path("id") id: Int): Call<List<OrdiniDettagliDTO>>
}
