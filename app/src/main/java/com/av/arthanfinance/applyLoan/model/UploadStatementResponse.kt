package com.av.arthanfinance.applyLoan.model

data class UploadStatementResponse(
    val docId: String?,
    val status: String?,
    val bankStatement: String?,
    val fileName: String?,
    val uploadTime: String?,
    val ocrFile: String?,
    val fileCount: String?,
    val totalPages: String?,
    val error: String?,
    val trackingId: String,
    val timestamp: String,
    val message: String
)