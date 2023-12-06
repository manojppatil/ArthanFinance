package com.av.arthanfinance.serviceRequest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.SeekBar
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.databinding.ActivityEmiCalculatorBinding
import com.av.arthanfinance.databinding.ActivityInitiateApplyLoan2Binding
import kotlinx.android.synthetic.main.activity_emi_calculator.*
import kotlin.math.roundToInt

class Emi_calculator : BaseActivity() {
    private lateinit var activityEmiCalculatorBinding: ActivityEmiCalculatorBinding
    var LOANMIN = 50000
    var LOANMAX = 2000000
    var LOANSTEP = 25000
    var TENMIN = 0
    var TENMAX = 240
    var TENSTEP = 1
    var RATEMIN = 0
    var RATEMAX = 60
    var RATESTEP = 1
    var tenure: Int = 0
    override val layoutId: Int
        get() = R.layout.activity_emi_calculator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityEmiCalculatorBinding = ActivityEmiCalculatorBinding.inflate(layoutInflater)
        setContentView(activityEmiCalculatorBinding.root)

        activityEmiCalculatorBinding.loanAmountSeekbar.max = 78

        activityEmiCalculatorBinding.loanAmountSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var progress_custom = LOANMIN + (i * LOANSTEP)
                if (i == 78) {
                    progress_custom = LOANMAX
                }
                activityEmiCalculatorBinding.loanAmountEditText.setText(progress_custom.toString())


            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"start tracking",Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"stop tracking",Toast.LENGTH_SHORT).show()
            }
        })

        activityEmiCalculatorBinding.loanTenureSeekbar.max = TENMAX

        activityEmiCalculatorBinding.loanTenureSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var progress_custom = TENMIN + (i * TENSTEP)
                if (i == 237) {
                    progress_custom = TENMAX

                }
                activityEmiCalculatorBinding.loanTenureEditText.setText(progress_custom.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"start tracking",Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"stop tracking",Toast.LENGTH_SHORT).show()
            }
        })

        activityEmiCalculatorBinding.loanRateSeekbar.max = RATEMAX

        activityEmiCalculatorBinding.loanRateSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var progress_custom = RATEMIN + (i * RATESTEP)
                if (i == 10) {
                    progress_custom = 10
                }
                activityEmiCalculatorBinding.loanRateEditText.setText(progress_custom.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"start tracking",Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"stop tracking",Toast.LENGTH_SHORT).show()
            }
        })

        loanAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    var loan = 1
                    val loan_param = loanAmountEditText.text.toString()
                    tenure = loanTenureEditText.text.toString().toInt()
                    val roi = loanRateEditText.text.toString()
                    val roiRate = roi.toInt()
                    loan = loan_param.toInt()
                    val param3 =
                        (loan * roiRate.toDouble() / 12 / 100 * Math.pow(
                            1 + roiRate.toDouble() / 12 / 100,
                            tenure.toDouble()
                        )) / (Math.pow(
                            1 + roiRate.toDouble() / 12 / 100,
                            tenure.toDouble()
                        ) - 1)
                    val loanemi = param3.toInt()
                    monthlyEMI.text = loanemi.toString()
                    val totalamountval = loanemi * tenure
                    totalamount.text = totalamountval.toString()
                    val totalInterestVal = totalamountval - loan
                    totalInterest.text = totalInterestVal.toString()
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace()
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        loanRateEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    var loan = 1
                    val loan_param = loanAmountEditText.text.toString()
                    tenure = loanTenureEditText.text.toString().toInt()
                    val roi = loanRateEditText.text.toString()
                    val roiRate = roi.toInt()
                    loan = loan_param.toInt()
                    val param3 =
                        (loan * roiRate.toDouble() / 12 / 100 * Math.pow(
                            1 + roiRate.toDouble() / 12 / 100,
                            tenure.toDouble()
                        )) / (Math.pow(
                            1 + roiRate.toDouble() / 12 / 100,
                            tenure.toDouble()
                        ) - 1)
                    val loanemi = param3.toInt()
                    monthlyEMI.text = loanemi.toString()
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace()
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        loanTenureEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    var loan = 1
                    val loan_param = loanAmountEditText.text.toString()
                    tenure = loanTenureEditText.text.toString().toInt()
                    val roi = loanRateEditText.text.toString()
                    val roiRate = roi.toInt()
                    loan = loan_param.toInt()
                    val param3 =
                        (loan * roiRate.toDouble() / 12 / 100 * Math.pow(
                            1 + roiRate.toDouble() / 12 / 100,
                            tenure.toDouble()
                        )) / (Math.pow(
                            1 + roiRate.toDouble() / 12 / 100,
                            tenure.toDouble()
                        ) - 1)
                    val loanemi = param3.toInt()
                    monthlyEMI.text = loanemi.toString()
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace()
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }
}