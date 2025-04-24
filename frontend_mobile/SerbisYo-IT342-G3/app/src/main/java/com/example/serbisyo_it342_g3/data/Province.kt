package com.example.serbisyo_it342_g3.data

data class Province(
    val code: String,
    val name: String,
    val regionCode: String
) {
    override fun toString(): String = name
}