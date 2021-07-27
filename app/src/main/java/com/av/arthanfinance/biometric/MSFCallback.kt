package com.nttdata.mobillitysecurityframework.msfcomponents

interface MSFCallback {
    fun onSuccess(message: String)
    fun onFailure(errorString: String)
}