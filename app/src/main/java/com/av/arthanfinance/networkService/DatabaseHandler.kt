package com.av.arthanfinance.networkService

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHandler(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "CustomerDatabase"
        private const val TABLE_CUSTOMERS = "CustomerTable"

        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_MOBILE = "mobile"
        private const val KEY_DOB = "dob"
        private const val KEY_CUSTID = "customerId"
        private const val KEY_MPIN = "mpin"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        //creating table with fields
        val CREATE_CONTACTS_TABLE = ("CREATE TABLE " + TABLE_CUSTOMERS + "("
                + KEY_NAME + " TEXT PRIMARY KEY,"
                + KEY_EMAIL + " TEXT," + KEY_DOB + " TEXT," + KEY_CUSTID + " TEXT," + KEY_MPIN + " TEXT," + KEY_MOBILE + " TEXT" + ")")
        db?.execSQL(CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS)
        onCreate(db)
    }

    //method to insert data
    fun saveCustomer(customer: Customer):Long{
        val customerList = getCustomers()
        for(cust: Customer in customerList) {
            if(cust.custName == customer.custName) {
                //update user
               return 2
            }
        }

        val db = this.writableDatabase
        val contentValues = ContentValues()
        //contentValues.put(KEY_ID, emp.userId)
        contentValues.put(KEY_NAME, customer.custName) // Customer Name
        contentValues.put(KEY_EMAIL,customer.mailId ) // Customer email
        contentValues.put(KEY_MOBILE,customer.mobNo) // Customer mobile
        contentValues.put(KEY_DOB,customer.dob ) // Customer DOB
        contentValues.put(KEY_CUSTID,customer.custId) // Customer custId
        contentValues.put(KEY_MPIN,"")
        // Inserting Row
        val success = db.insert(TABLE_CUSTOMERS, null, contentValues)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }
    //method to read data
    fun getCustomers():List<Customer>{
        val empList:ArrayList<Customer> = ArrayList<Customer>()
        val selectQuery = "SELECT  * FROM $TABLE_CUSTOMERS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
       // var userId: Int
        var userName: String
        var userEmail: String
        var userMobile: String
        var dob: String
        var custId: String
        var mpin: String
        if (cursor.moveToFirst()) {
            do {
                //userId = cursor.getInt(cursor.getColumnIndex("id"))
                userName = cursor.getString(cursor.getColumnIndex("name"))
                userEmail = cursor.getString(cursor.getColumnIndex("email"))
                userMobile = cursor.getString(cursor.getColumnIndex("mobile"))
                dob = cursor.getString(cursor.getColumnIndex("dob"))
                custId = cursor.getString(cursor.getColumnIndex("customerId"))
                mpin = cursor.getString(cursor.getColumnIndex("mpin"))
                val emp= Customer(userName,userEmail, userMobile,dob, custId, mpin)
                empList.add(emp)
            } while (cursor.moveToNext())
        }
        return empList
    }

    fun getCustomerByCustId(customerId: String): Customer? {
        val db = this.writableDatabase
        val cursor: Cursor =
            db.rawQuery("SELECT * FROM $TABLE_CUSTOMERS WHERE $KEY_CUSTID=$customerId", null)
            if (cursor.moveToFirst()) {
                val userName = cursor.getString(cursor.getColumnIndex("name"))
                val userEmail = cursor.getString(cursor.getColumnIndex("email"))
                val userMobile = cursor.getString(cursor.getColumnIndex("mobile"))
                val dob = cursor.getString(cursor.getColumnIndex("dob"))
                val custId = cursor.getString(cursor.getColumnIndex("customerId"))
                val mpin = cursor.getString(cursor.getColumnIndex("mpin"))
                return Customer(userName,userEmail,userMobile,dob,custId,mpin)
            }

        return null
    }

    fun getCustomerByMobileNo(mobileNumber: String): Customer? {
        val db = this.writableDatabase
        val cursor: Cursor =
            db.rawQuery("SELECT * FROM $TABLE_CUSTOMERS WHERE $KEY_MOBILE=$mobileNumber", null)
        if (cursor.moveToFirst()) {
            val userName = cursor.getString(cursor.getColumnIndex("name"))
            val userEmail = cursor.getString(cursor.getColumnIndex("email"))
            val userMobile = cursor.getString(cursor.getColumnIndex("mobile"))
            val dob = cursor.getString(cursor.getColumnIndex("dob"))
            val custId = cursor.getString(cursor.getColumnIndex("customerId"))
            val mpin = cursor.getString(cursor.getColumnIndex("mpin"))
            return Customer(userName,userEmail,userMobile,dob,custId,mpin)
        }

        return null
    }

    //method to update data
    fun updateCustomer(customer: Customer):Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
//        contentValues.put(KEY_ID, emp.userId)
        contentValues.put(KEY_NAME, customer.custName) // customer Name
        contentValues.put(KEY_EMAIL,customer.mailId ) // customer Email
        contentValues.put(KEY_MOBILE,customer.mobNo ) // customer MobileNo
        contentValues.put(KEY_DOB,customer.dob ) // Customer DOB
        contentValues.put(KEY_CUSTID,customer.custId) // Customer custId
        contentValues.put(KEY_MPIN,customer.mpin) // customer MPIN

        // Updating Row
        val success = db.update(TABLE_CUSTOMERS, contentValues,"customerId="+customer.custId,null)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }
    //method to delete data
    fun deleteUser(customer: Customer): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_NAME, customer.custName) // User UserId
        // Deleting Row
      //  val success = db.delete(TABLE_CONTACTS,KEY_NAME + "=" + emp.userName,null)
        val success = db.execSQL("DELETE FROM " + TABLE_CUSTOMERS+ " WHERE "+KEY_NAME+"='"+customer.custName+"'")

        db.close() // Closing database connection
        if (success != null) {
            return true
        }
        return false
    }
}