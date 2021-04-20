package com.example.arthanfinance


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.arthanfinance.applyLoan.AuthenticationResponse
import com.example.arthanfinance.networkService.ApiClient
import com.example.arthanfinance.networkService.Customer
import com.example.arthanfinance.networkService.DatabaseHandler
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SetNewMpinActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var apiClient: ApiClient

    private lateinit var txt1: EditText
    private lateinit var txt2: EditText
    private lateinit var txt3: EditText
    private lateinit var txt4: EditText
    private lateinit var newTxt1: EditText
    private lateinit var newTxt2: EditText
    private lateinit var newTxt3: EditText
    private lateinit var newTxt4: EditText

    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btn4: Button
    private lateinit var btn5: Button
    private lateinit var btn6: Button
    private lateinit var btn7: Button
    private lateinit var btn8: Button
    private lateinit var btn9: Button
    private lateinit var btn0: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_set_new_mpin)

        if (supportActionBar != null)
            supportActionBar?.hide()
        apiClient = ApiClient()
        btnBack = findViewById(R.id.img_back_mpin)

        txt1 = findViewById(R.id.txt1)
        txt2 = findViewById(R.id.txt2)
        txt3 = findViewById(R.id.txt3)
        txt4 = findViewById(R.id.txt4)
        newTxt1 = findViewById(R.id.newTxt1)
        newTxt2 = findViewById(R.id.newtxt2)
        newTxt3 = findViewById(R.id.newTxt3)
        newTxt4 = findViewById(R.id.newTxt4)

        btn0 = findViewById(R.id.btn10)
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)
        btn4 = findViewById(R.id.btn4)
        btn5 = findViewById(R.id.btn5)
        btn6 = findViewById(R.id.btn6)
        btn7 = findViewById(R.id.btn7)
        btn8 = findViewById(R.id.btn8)
        btn9 = findViewById(R.id.btn9)

        btnBack.setOnClickListener {
            this.finish()
        }

        txt1.focusable = View.FOCUSABLE

        btn1.setOnClickListener {
            if (txt1.text.isEmpty()) {
                txt1.setText("1")
                txt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt2.text.isEmpty()) {
                txt2.setText("1")
                txt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt3.text.isEmpty()) {
                txt3.setText("1")
                txt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt4.text.isEmpty()) {
                txt4.setText("1")
                newTxt1.focusable = View.FOCUSABLE
                return@setOnClickListener
            }

            if (newTxt1.text.isEmpty()) {
                newTxt1.setText("1")
                newTxt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt2.text.isEmpty()) {
                newTxt2.setText("1")
                newTxt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt3.text.isEmpty()) {
                newTxt3.setText("1")
                newTxt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt4.text.isEmpty()) {
                newTxt4.setText("1")
                validateMPIN()
                return@setOnClickListener
            }
        }

        btn2.setOnClickListener {
            if (txt1.text.isEmpty()) {
                txt1.setText("2")
                txt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt2.text.isEmpty()) {
                txt2.setText("2")
                txt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt3.text.isEmpty()) {
                txt3.setText("2")
                txt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt4.text.isEmpty()) {
                txt4.setText("2")
                newTxt1.focusable = View.FOCUSABLE
                return@setOnClickListener
            }

            if (newTxt1.text.isEmpty()) {
                newTxt1.setText("2")
                newTxt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt2.text.isEmpty()) {
                newTxt2.setText("2")
                newTxt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt3.text.isEmpty()) {
                newTxt3.setText("2")
                newTxt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt4.text.isEmpty()) {
                newTxt4.setText("2")
                validateMPIN()
                return@setOnClickListener
            }
        }

        btn3.setOnClickListener {
            if (txt1.text.isEmpty()) {
                txt1.setText("3")
                txt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt2.text.isEmpty()) {
                txt2.setText("3")
                txt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt3.text.isEmpty()) {
                txt3.setText("3")
                txt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt4.text.isEmpty()) {
                txt4.setText("3")
                newTxt1.focusable = View.FOCUSABLE
                return@setOnClickListener
            }

            if (newTxt1.text.isEmpty()) {
                newTxt1.setText("3")
                newTxt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt2.text.isEmpty()) {
                newTxt2.setText("3")
                newTxt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt3.text.isEmpty()) {
                newTxt3.setText("3")
                newTxt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt4.text.isEmpty()) {
                newTxt4.setText("3")
                validateMPIN()
                return@setOnClickListener
            }
        }

        btn4.setOnClickListener {
            if (txt1.text.isEmpty()) {
                txt1.setText("4")
                txt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt2.text.isEmpty()) {
                txt2.setText("4")
                txt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt3.text.isEmpty()) {
                txt3.setText("4")
                txt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt4.text.isEmpty()) {
                txt4.setText("4")
                newTxt1.focusable = View.FOCUSABLE
                return@setOnClickListener
            }

            if (newTxt1.text.isEmpty()) {
                newTxt1.setText("4")
                newTxt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt2.text.isEmpty()) {
                newTxt2.setText("4")
                newTxt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt3.text.isEmpty()) {
                newTxt3.setText("4")
                newTxt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt4.text.isEmpty()) {
                newTxt4.setText("4")
                validateMPIN()
                return@setOnClickListener
            }
        }

        btn5.setOnClickListener {
            if (txt1.text.isEmpty()) {
                txt1.setText("5")
                txt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt2.text.isEmpty()) {
                txt2.setText("5")
                txt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt3.text.isEmpty()) {
                txt3.setText("5")
                txt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt4.text.isEmpty()) {
                txt4.setText("5")
                newTxt1.focusable = View.FOCUSABLE
                return@setOnClickListener
            }

            if (newTxt1.text.isEmpty()) {
                newTxt1.setText("5")
                newTxt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt2.text.isEmpty()) {
                newTxt2.setText("5")
                newTxt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt3.text.isEmpty()) {
                newTxt3.setText("5")
                newTxt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt4.text.isEmpty()) {
                newTxt4.setText("5")
                validateMPIN()
                return@setOnClickListener
            }
        }

        btn6.setOnClickListener {
            if (txt1.text.isEmpty()) {
                txt1.setText("6")
                txt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt2.text.isEmpty()) {
                txt2.setText("6")
                txt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt3.text.isEmpty()) {
                txt3.setText("6")
                txt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt4.text.isEmpty()) {
                txt4.setText("6")
                newTxt1.focusable = View.FOCUSABLE
                return@setOnClickListener
            }

            if (newTxt1.text.isEmpty()) {
                newTxt1.setText("6")
                newTxt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt2.text.isEmpty()) {
                newTxt2.setText("6")
                newTxt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt3.text.isEmpty()) {
                newTxt3.setText("6")
                newTxt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt4.text.isEmpty()) {
                newTxt4.setText("6")
                validateMPIN()
                return@setOnClickListener
            }
        }

        btn7.setOnClickListener {
            if (txt1.text.isEmpty()) {
                txt1.setText("7")
                txt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt2.text.isEmpty()) {
                txt2.setText("7")
                txt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt3.text.isEmpty()) {
                txt3.setText("7")
                txt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt4.text.isEmpty()) {
                txt4.setText("7")
                newTxt1.focusable = View.FOCUSABLE
                return@setOnClickListener
            }

            if (newTxt1.text.isEmpty()) {
                newTxt1.setText("7")
                newTxt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt2.text.isEmpty()) {
                newTxt2.setText("7")
                newTxt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt3.text.isEmpty()) {
                newTxt3.setText("7")
                newTxt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt4.text.isEmpty()) {
                newTxt4.setText("7")
                validateMPIN()
                return@setOnClickListener
            }
        }

        btn8.setOnClickListener {
            if (txt1.text.isEmpty()) {
                txt1.setText("8")
                txt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt2.text.isEmpty()) {
                txt2.setText("8")
                txt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt3.text.isEmpty()) {
                txt3.setText("8")
                txt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt4.text.isEmpty()) {
                txt4.setText("8")
                newTxt1.focusable = View.FOCUSABLE
                return@setOnClickListener
            }

            if (newTxt1.text.isEmpty()) {
                newTxt1.setText("8")
                newTxt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt2.text.isEmpty()) {
                newTxt2.setText("8")
                newTxt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt3.text.isEmpty()) {
                newTxt3.setText("8")
                newTxt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt4.text.isEmpty()) {
                newTxt4.setText("8")
                validateMPIN()
                return@setOnClickListener
            }
        }

        btn9.setOnClickListener {
            if (txt1.text.isEmpty()) {
                txt1.setText("9")
                txt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt2.text.isEmpty()) {
                txt2.setText("9")
                txt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt3.text.isEmpty()) {
                txt3.setText("9")
                txt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt4.text.isEmpty()) {
                txt4.setText("9")
                newTxt1.focusable = View.FOCUSABLE
                return@setOnClickListener
            }

            if (newTxt1.text.isEmpty()) {
                newTxt1.setText("9")
                newTxt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt2.text.isEmpty()) {
                newTxt2.setText("9")
                newTxt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt3.text.isEmpty()) {
                newTxt3.setText("9")
                newTxt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt4.text.isEmpty()) {
                newTxt4.setText("9")
                validateMPIN()
                return@setOnClickListener
            }
        }

        btn0.setOnClickListener {
            if (txt1.text.isEmpty()) {
                txt1.setText("0")
                txt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt2.text.isEmpty()) {
                txt2.setText("0")
                txt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt3.text.isEmpty()) {
                txt3.setText("0")
                txt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (txt4.text.isEmpty()) {
                txt4.setText("0")
                newTxt1.focusable = View.FOCUSABLE
                return@setOnClickListener
            }

            if (newTxt1.text.isEmpty()) {
                newTxt1.setText("0")
                newTxt2.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt2.text.isEmpty()) {
                newTxt2.setText("0")
                newTxt3.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt3.text.isEmpty()) {
                newTxt3.setText("0")
                newTxt4.focusable = View.FOCUSABLE
                return@setOnClickListener
            }
            if (newTxt4.text.isEmpty()) {
                newTxt4.setText("0")
                validateMPIN()
                return@setOnClickListener
            }
        }
    }

    private fun validateMPIN() {
        val txt1 = txt1.text
        val txt2 = txt2.text
        val txt3 = txt3.text
        val txt4 = txt4.text
        val newtxt1 = newTxt1.text
        val newtxt2 = newTxt2.text
        val newtxt3 = newTxt3.text
        val newtxt4 = newTxt4.text

        if(txt1.isNotEmpty() && txt2.isNotEmpty() && txt3.isNotEmpty() && txt4.isNotEmpty() && newtxt1.isNotEmpty() && newtxt2.isNotEmpty() && newtxt3.isNotEmpty() && newtxt4.isNotEmpty()){
            val mpin1 = "${txt1}${txt2}${txt3}${txt4}"
            val mpin2 = "${newtxt1}${newtxt2}${newtxt3}${newtxt4}"
            if(mpin1 == mpin2) {
                setMPIN(mpin2)
            } else {
                clearText()
                Toast.makeText(this@SetNewMpinActivity,"Please enter the valid MPIN",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearText() {
        txt1.setText("")
        txt2.setText("")
        txt3.setText("")
        txt4.setText("")
        newTxt1.setText("")
        newTxt2.setText("")
        newTxt3.setText("")
        newTxt4.setText("")
    }

    private fun setMPIN(mpin: String) {
        val customerId = intent.extras?.get("customerId") as String
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerId)
        jsonObject.addProperty("pin", mpin)
        apiClient.getApiService(this).setCustomerMPIN(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@SetNewMpinActivity,"Please enter the valid MPIN",
                    Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                val custData = response.body()
                if (custData != null) {
                    if (custData.status == "200") {
//                        val databaseHandler: DatabaseHandler = DatabaseHandler(this@SetNewMpinActivity)
//                            var custObject = databaseHandler.getCustomerByCustId(customerId)
//                        if (custObject != null) {
//                            custObject.mpin = mpin
//                            val status =  databaseHandler.updateCustomer(custObject)
////                            if (status > -1) {
////                                Toast.makeText(
////                                    applicationContext,
////                                    "Record saved to DB.",
////                                    Toast.LENGTH_LONG
////                                ).show()
////
////                            } else if (status == -2) {
////                                Toast.makeText(
////                                    applicationContext,
////                                    "Record already exists.",
////                                    Toast.LENGTH_LONG
////                                ).show()
////                            }
//
//
//                        }
                        val intent = Intent(this@SetNewMpinActivity, MPINLoginActivity::class.java)
                        intent.putExtra("customerId",customerId)
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@SetNewMpinActivity,"Please enter the valid MPIN",
                            Toast.LENGTH_SHORT).show()
                    }

                }
            }
        })
    }
}

