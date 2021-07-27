package com.av.arthanfinance

class Singleton private constructor() {

    private object HOLDER {
        val INSTANCE = Singleton()
    }

    companion object {
        val instance: Singleton by lazy { HOLDER.INSTANCE }
    }
}