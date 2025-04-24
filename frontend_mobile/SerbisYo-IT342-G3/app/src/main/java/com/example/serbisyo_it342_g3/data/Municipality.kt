package com.example.serbisyo_it342_g3.data

data class Municipality(
    val code: String,
    val name: String,
    val provinceCode: String
) {
    override fun toString(): String = name
}