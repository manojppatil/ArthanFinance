package com.av.arthanfinance.applyLoan

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.av.arthanfinance.R
import com.av.arthanfinance.models.CustomerBankDetailsResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.regex.Pattern

val IFSCCode_Patter: Pattern = Pattern.compile(
    "^[A-Z]{4}0[A-Z0-9]{6}$"
)

class UploadDocsFragment : Fragment() {
    private val BANK_STATEMENTS = 100
    private lateinit var tvUploadAdhar: TextView
    private lateinit var btnNext: Button
    private lateinit var uploadDocButton: Button
    private lateinit var btnMoreBankDocs: Button
    private lateinit var btnCancel: Button
    private lateinit var editTextBankName : EditText
    private lateinit var editTextBankAccountName : EditText
    private lateinit var editTextAccountNo : EditText
    private lateinit var editTextReEnterAccountNo : EditText
    private lateinit var editTextIFSCCode : EditText
    private lateinit var lytUpload : LinearLayout
    private lateinit var bankStatementsRecyclerView: RecyclerView
    var bankStatementList: ArrayList<String> = ArrayList()
    lateinit var bankStatementArrayAdapter: StringAdapter
    private var loanResponse: LoanProcessResponse? = null
    private var actualFile : File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.layout_upload_docs, container, false)
        tvUploadAdhar = view.findViewById(R.id.tv_upload_docs)
        btnNext = view.findViewById(R.id.btn_next)
        uploadDocButton = view.findViewById(R.id.btn_upload)
        btnMoreBankDocs = view.findViewById(R.id.btn_more)
        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse
        lytUpload = view.findViewById(R.id.lyt_upload)
        btnCancel = view.findViewById(R.id.btn_cancel)
        editTextBankName = view.findViewById(R.id.edt_bank_name)
        editTextBankAccountName = view.findViewById(R.id.edt_acc_name)
        editTextAccountNo = view.findViewById(R.id.edt_acc_no)
        editTextIFSCCode = view.findViewById(R.id.edt_ifsc)
        editTextReEnterAccountNo = view.findViewById(R.id.edt_re_acc_no)
        val paint: TextPaint = tvUploadAdhar.getPaint()
        val width = paint.measureText("Upload your Aadhaar Card")

        val shader = LinearGradient(
            0f, 0f, width, tvUploadAdhar.textSize, resources.getColor(
                R.color.dark_orange2, activity?.theme
            ), resources.getColor(
                R.color.indigoBlue, activity?.theme
            ), Shader.TileMode.CLAMP
        )
        tvUploadAdhar.paint.shader = shader

        bankStatementsRecyclerView = view.findViewById<RecyclerView>(R.id.bankStatementsRecyclerView)
        bankStatementsRecyclerView.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayout.VERTICAL,
            false
        )

        bankStatementArrayAdapter = StringAdapter(bankStatementList)
        bankStatementArrayAdapter.notifyDataSetChanged()
        bankStatementsRecyclerView.adapter = bankStatementArrayAdapter
        bankStatementsRecyclerView.setHasFixedSize(true)


        lytUpload.setOnClickListener {
            uploadBankDocumentsToServer()
        }

        uploadDocButton.setOnClickListener {
            uploadBankDocumentsToServer()
        }

        btnMoreBankDocs.setOnClickListener {
            openGalleryForDocumentSelection()
        }

        btnCancel.setOnClickListener {
            lytUpload.visibility = View.GONE
            bankStatementList.clear()
            bankStatementArrayAdapter.notifyDataSetChanged()
        }


        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW, false)
        isCreateFlow?.let {
            if (!it){
                getBankAccountDetails()
                disableEditFeature()
            }
        }

        btnNext.setOnClickListener{
            if (isCreateFlow!!) {
                saveBankAccountDetails()
            }else{
                (activity as UploadKycDetailsActivity?)?.selectIndex(6)
            }
        }
        lytUpload.visibility = View.GONE
        return view
    }

    private fun uploadBankDocumentsToServer() {
        actualFile?.let {
            uploadFile(actualFile!!,loanResponse!!.loanId!!,loanResponse!!.customerId!!)
        }
    }

    private fun getBankAccountDetails() {
        val context = activity?.applicationContext!!

        ApiClient().getAuthApiService(context).getPRBankDetails(loanResponse!!.loanId!!).enqueue(
            object :
                Callback<CustomerBankDetailsResponse> {
                override fun onFailure(call: Call<CustomerBankDetailsResponse>, t: Throwable) {
                    t.printStackTrace()
                }

                override fun onResponse(
                    call: Call<CustomerBankDetailsResponse>,
                    response: Response<CustomerBankDetailsResponse>
                ) {
                    val bankDetails = response.body()
                    bankDetails?.accountName?.let {
                        editTextBankAccountName.setText(it)
                        editTextBankAccountName.isEnabled = false
                    }
                    bankDetails?.bankName?.let {
                        editTextBankName.setText(it)
                        editTextBankName.isEnabled = false
                    }
                    bankDetails?.accountNo?.let {
                        editTextAccountNo.setText(it)
                        editTextAccountNo.isEnabled = false
                        editTextReEnterAccountNo.setText(it)
                        editTextReEnterAccountNo.isEnabled = false
                    }
                    bankDetails?.ifscCode?.let {
                        editTextIFSCCode.setText(it)
                        editTextIFSCCode.isEnabled = false
                    }
                }
            })
    }

    private fun checkIFSCCode(ifscCode: String): Boolean {
        return IFSCCode_Patter.matcher(ifscCode).matches()
    }

    private fun saveBankAccountDetails() {

        if (editTextBankName.text.toString().isEmpty()){
            Toast.makeText(editTextAccountNo.context, "Bank Name is Empty", Toast.LENGTH_SHORT).show()
            return
        }else if (editTextBankAccountName.text.toString().isEmpty()){
            Toast.makeText(
                editTextBankAccountName.context,
                "Bank Account Name is Empty",
                Toast.LENGTH_SHORT
            ).show()
            return
        }else if (editTextAccountNo.text.toString().isEmpty()){
            Toast.makeText(editTextAccountNo.context, "Account Number is Empty", Toast.LENGTH_SHORT).show()
            return
        }else if(!editTextAccountNo.text.toString().equals(editTextReEnterAccountNo.text.toString())){
            Toast.makeText(editTextAccountNo.context, "Account Number not matching", Toast.LENGTH_SHORT).show()
            return
        }
        else if (editTextIFSCCode.text.toString().isEmpty()){
             Toast.makeText(editTextAccountNo.context, "IFSC code is Empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (!checkIFSCCode(editTextIFSCCode.text.toString())){
            Toast.makeText(editTextAccountNo.context, "IFSC code Invalid", Toast.LENGTH_SHORT).show()
            return
        }
        (activity as UploadKycDetailsActivity).showProgressDialog()

        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("accountName", editTextBankAccountName.text.toString())
        jsonObject.addProperty("accountNo", editTextAccountNo.text.toString())
        jsonObject.addProperty("bankName", editTextBankName.text.toString())
        jsonObject.addProperty("ifscCode", editTextIFSCCode.text.toString())

        val context = activity?.applicationContext
        if (context != null) {
            ApiClient().getAuthApiService(context).saveCustomerBankDetails(jsonObject).enqueue(
                object :
                    Callback<LoanProcessResponse> {
                    override fun onResponse(
                        call: Call<LoanProcessResponse>,
                        response: Response<LoanProcessResponse>
                    ) {
                        (activity as UploadKycDetailsActivity).hideProgressDialog()
                        (activity as UploadKycDetailsActivity?)?.selectIndex(6)
                    }

                    override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                        (activity as UploadKycDetailsActivity).hideProgressDialog()
                        t.printStackTrace()
                        Toast.makeText(
                            context,
                            "Service Failure, Once Network connection is stable, will try to resend again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
//        }
    }

    private fun disableEditFeature() {
        tvUploadAdhar.setText("Uploaded Documents")
        uploadDocButton.visibility = View.GONE
        btnMoreBankDocs.visibility = View.GONE
        lytUpload.visibility = View.GONE
    }

    private fun openGalleryForDocumentSelection() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "ChooseFile"), BANK_STATEMENTS)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BANK_STATEMENTS && resultCode == Activity.RESULT_OK) {
            try {
                val fileUri = data!!.data
                val fileName = getFileName(activity?.contentResolver, fileUri) ?: ""
                val fileDescriptor = fileUri?.let { activity?.contentResolver?.openFileDescriptor(
                    it,
                    "r"
                ) }
                actualFile = File(activity?.cacheDir, fileName)
                val inputStream = FileInputStream(fileDescriptor?.fileDescriptor)
                val outputStream = FileOutputStream(actualFile)
                inputStream.copyTo(outputStream)
                bankStatementList.clear()
                bankStatementList.add(actualFile!!.name)
                bankStatementArrayAdapter.notifyDataSetChanged()
                lytUpload.visibility = View.VISIBLE
                uploadDocButton.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getFileName(contentResolver: ContentResolver?, uri: Uri?): String? {
        if (uri == null) return null
        var fileName: String? = null
        uri.let { returnUri ->
            contentResolver?.query(returnUri, null, null, null, null)
        }?.use { cursor ->
            cursor.moveToFirst()
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
        return fileName
    }


    private fun uploadFile(file: File, loanid: String, customerId: String) {
        val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body: MultipartBody.Part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            requestFile
        )
        val loanID: RequestBody = loanid.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val customerID: RequestBody = customerId.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        (activity as UploadKycDetailsActivity).showProgressDialog()

        activity?.applicationContext?.let { ApiClient().getUploadApiService(it).uploadFile(
            body,
            loanID,
            customerID
        ).enqueue(object :
            Callback<FileUploadResponse> {
            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                Toast.makeText(it, "File Upload Failed Please try again", Toast.LENGTH_SHORT).show()
                uploadDocButton.visibility = View.VISIBLE
            }

            override fun onResponse(
                call: Call<FileUploadResponse>,
                response: Response<FileUploadResponse>
            ) {
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                Toast.makeText(it, "File Upload SUCCESS......", Toast.LENGTH_SHORT).show()
                uploadDocButton.visibility = View.GONE
            }

        })
        }
    }

}