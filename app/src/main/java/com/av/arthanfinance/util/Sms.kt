package com.av.arthanfinance.util

class Sms {
    private var id: String? = null
    private var address: String? = null
    private var msg: String? = null
    private var readState: String? = null
    private var time: String? = null
    private var folderName: String? = null


    fun getId(): String? {
        return id
    }

    fun setId(id: String?) {
        this.id = id
    }

    fun getAddress(): String? {
        return address
    }

    fun setAddress(address: String?) {
        this.address = address
    }

    fun getMsg(): String? {
        return msg
    }

    fun setMsg(msg: String?) {
        this.msg = msg
    }

    fun getReadState(): String? {
        return readState
    }

    fun setReadState(readState: String?) {
        this.readState = readState
    }

    fun getTime(): String? {
        return time
    }

    fun setTime(time: String?) {
        this.time = time
    }

    fun getFolderName(): String? {
        return folderName
    }

    fun setFolderName(folderName: String?) {
        this.folderName = folderName
    }

}
