package com.example.serbisyo_it342_g3.data

import com.google.gson.annotations.SerializedName

data class Schedule(
    @SerializedName("scheduleId")
    val scheduleId: Long? = null,
    
    @SerializedName("providerId")
    val providerId: Long? = null,
    
    @SerializedName("dayOfWeek")
    val dayOfWeek: String,
    
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String,
    
    @SerializedName(value = "isAvailable", alternate = ["available"])
    val isAvailable: Boolean = true,
    
    @SerializedName("serviceProvider")
    val serviceProvider: Any? = null
) 