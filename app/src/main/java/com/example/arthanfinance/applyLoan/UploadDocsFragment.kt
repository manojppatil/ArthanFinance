package com.example.arthanfinance.applyLoan

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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arthanfinance.R
import com.example.arthanfinance.networkService.ApiClient
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


class UploadDocsFragment : Fragment() {
    private val BANK_STATEMENTS = 100
    private lateinit var tvUploadAdhar: TextView
    private lateinit var btnNext: Button
    private lateinit var uploadDocButton: Button
    private lateinit var btnMoreBankDocs: Button
    private lateinit var bankStatementsRecyclerView: RecyclerView
    var bankStatementList: ArrayList<String> = ArrayList()
    lateinit var bankStatementArrayAdapter: StringAdapter
    private var loanResponse: LoanProcessResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.layout_upload_docs, container, false)
        tvUploadAdhar = view.findViewById(R.id.tv_upload_docs)
        btnNext = view.findViewById(R.id.btn_next)
        uploadDocButton = view.findViewById(R.id.btn_upload)
        btnMoreBankDocs = view.findViewById(R.id.btn_more)
        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse

        val paint: TextPaint = tvUploadAdhar.getPaint()
        val width = paint.measureText("Upload your Aadhaar Card")

        val shader = LinearGradient(0f, 0f, width, tvUploadAdhar.textSize, resources.getColor(
            R.color.dark_orange2, activity?.theme), resources.getColor(
            R.color.indigoBlue, activity?.theme), Shader.TileMode.CLAMP)
        tvUploadAdhar.paint.shader = shader

        bankStatementsRecyclerView = view.findViewById<RecyclerView>(R.id.bankStatementsRecyclerView)
        bankStatementsRecyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext, LinearLayout.VERTICAL, false)

        bankStatementArrayAdapter = StringAdapter(bankStatementList)
        bankStatementArrayAdapter.notifyDataSetChanged()
        bankStatementsRecyclerView.adapter = bankStatementArrayAdapter
        bankStatementsRecyclerView.setHasFixedSize(true)

        uploadDocButton.setOnClickListener {
            uploadBankDocuments()
        }

        btnMoreBankDocs.setOnClickListener {
            uploadBankDocuments()
        }

        btnNext.setOnClickListener{
            (activity as UploadKycDetailsActivity?)?.selectIndex(6)
//            val fragment =
//                ReferenceDetailsFragment()
//            val fragmentManger = activity?.supportFragmentManager
//            val transaction = fragmentManger?.beginTransaction()
//            transaction?.setCustomAnimations(
//                R.anim.enter_from_right,
//                R.anim.exit_to_left,
//                R.anim.enter_from_left,
//                R.anim.exit_to_right
//            );
//            transaction?.replace(R.id.container, fragment)
//            transaction?.addToBackStack(null)
//            transaction?.commit()
        }
        return view
    }

    private fun uploadBankDocuments() {
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
                val fileDescriptor = fileUri?.let { activity?.contentResolver?.openFileDescriptor(it,"r") }
                val actualFile = File(activity?.cacheDir, fileName)
                val inputStream = FileInputStream(fileDescriptor?.fileDescriptor)
                val outputStream = FileOutputStream(actualFile)
                inputStream.copyTo(outputStream)

                val loanId = loanResponse?.loanId//arguments!!.getString("loanId")
                val customerId = loanResponse?.customerId //arguments!!.getString("customerId")
                if (loanId != null) {
                    if (customerId != null) {
                        uploadFile(actualFile, loanId, customerId)
                    }
                }
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
        val body: MultipartBody.Part = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val loanID: RequestBody = loanid.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val customerID: RequestBody = customerId.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        activity?.applicationContext?.let { ApiClient().getUploadApiService(it).uploadFile(body,loanID,customerID).enqueue(object  :
            Callback<FileUploadResponse> {
            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                Toast.makeText(it,"File Upload Failed Please try again", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<FileUploadResponse>,
                response: Response<FileUploadResponse>
            ) {
                Toast.makeText(it,"File Upload SUCCESS......", Toast.LENGTH_SHORT).show()
                bankStatementList.add(file.name)
                bankStatementArrayAdapter.notifyDataSetChanged()
            }

        })
        }
    }

}