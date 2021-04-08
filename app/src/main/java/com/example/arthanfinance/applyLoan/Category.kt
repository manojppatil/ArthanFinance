package com.example.arthanfinance.applyLoan

import java.io.Serializable

class Categories(val categories: ArrayList<Category>): Serializable{}
class Category(val categoryId: String, val categoryDesc: String, val segments: List<String>): Serializable {}