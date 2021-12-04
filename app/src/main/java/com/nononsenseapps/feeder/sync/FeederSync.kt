package com.nononsenseapps.feeder.sync

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FeederSync {
    @POST("create")
    suspend fun create(
        @Body request: CreateRequest
    ): JoinResponse

    @POST("join")
    suspend fun join(
        @Header("X-FEEDER-ID") syncChainId: String,
        @Body request: JoinRequest
    ): JoinResponse

    @GET("devices")
    suspend fun getDevices(
        @Header("X-FEEDER-ID") syncChainId: String,
        @Header("X-FEEDER-DEVICE-ID") currentDeviceId: Long
    ): DeviceListResponse

    @DELETE("devices/{deviceId}")
    suspend fun removeDevice(
        @Header("X-FEEDER-ID") syncChainId: String,
        @Header("X-FEEDER-DEVICE-ID") currentDeviceId: Long,
        @Path("deviceId") deviceId: Long
    ): DeviceListResponse

    @GET("readmark")
    suspend fun getReadMarks(
        @Header("X-FEEDER-ID") syncChainId: String,
        @Header("X-FEEDER-DEVICE-ID") currentDeviceId: Long,
        @Query("since") sinceMillis: Long
    ): GetReadMarksResponse

    @POST("readmark")
    suspend fun sendReadMarks(
        @Header("X-FEEDER-ID") syncChainId: String,
        @Header("X-FEEDER-DEVICE-ID") currentDeviceId: Long,
        @Body request: SendReadMarkBulkRequest
    ): SendReadMarkResponse

    @GET("ereadmark")
    suspend fun getEncryptedReadMarks(
        @Header("X-FEEDER-ID") syncChainId: String,
        @Header("X-FEEDER-DEVICE-ID") currentDeviceId: Long,
        @Query("since") sinceMillis: Long
    ): GetEncryptedReadMarksResponse

    @POST("ereadmark")
    suspend fun sendEncryptedReadMarks(
        @Header("X-FEEDER-ID") syncChainId: String,
        @Header("X-FEEDER-DEVICE-ID") currentDeviceId: Long,
        @Body request: SendEncryptedReadMarkBulkRequest
    ): SendReadMarkResponse

    @GET("feeds")
    suspend fun getFeeds(
        @Header("X-FEEDER-ID") syncChainId: String,
        @Header("X-FEEDER-DEVICE-ID") currentDeviceId: Long,
    ): GetFeedsResponse

    @POST("feeds")
    suspend fun updateFeeds(
        @Header("X-FEEDER-ID") syncChainId: String,
        @Header("X-FEEDER-DEVICE-ID") currentDeviceId: Long,
        @Header("If-Match") etagValue: String,
        @Body request: UpdateFeedsRequest,
    ): UpdateFeedsResponse
}
