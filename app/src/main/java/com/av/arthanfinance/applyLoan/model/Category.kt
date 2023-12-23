package com.av.arthanfinance.applyLoan.model

data class Categories(
    val data: List<Category>?
)

data class Category(
    val id: String,
    val value: String,
    val description: String,
    val segments: List<String>
)