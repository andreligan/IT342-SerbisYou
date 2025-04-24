package com.example.serbisyo_it342_g3.data

data class Barangay(
    val code: String,
    val name: String,
    val municipalityCode: String
) {
    override fun toString(): String = name
}