package com.example.serbisyo_it342_g3.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Transaction(
    @SerializedName("transactionId") val transactionId: Long = 0,
    @SerializedName("booking") val booking: Booking? = null,
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("paymentMethod") val paymentMethod: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("transactionDate") val transactionDate: String = ""
) : Serializable 