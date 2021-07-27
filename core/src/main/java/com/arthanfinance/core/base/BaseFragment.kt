package com.arthanfinance.core.base

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

/* Base class for Fragments when using a view model with data binding.
 * This class provides the binding and the view model to the subclass. The
 * view model is injected and the binding is created when the content view is set.
 * Each subclass therefore has to call the following code in onCreateView():
 *    return setAndBindContentView(inflater, container, savedInstanceState, R.layout.my_fragment_layout)
 *
 * After calling this method, the binding and the view model is initialized.
 * saveInstanceState() and restoreInstanceState() methods of the view model
 * are automatically called in the appropriate lifecycle events when above calls
 * are made.
 */
abstract class BaseFragment {

//    protected fun showSnackbarMessage(message: String, block: () -> Unit) {
//        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).setAction(null) {
//            block()
//        }.show()
//    }
//
//    fun showSnackbarMessage(message: String,isSuccess:Boolean = false) {
//        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG);
//        val view: View = snackbar.getView()
//        val textView = view.findViewById<TextView>(R.id.snackbar_text)
//        try {
//            view.setBackgroundColor(WSResourceUtil.getColorFromAttributeId(view.context,R.attr.textColorPrimary))
//            textView.setTextColor(WSResourceUtil.getColorFromAttributeId(view.context,R.attr.colorBackground))
//            if (!isSuccess) {
//                snackbar.setActionTextColor(ContextCompat.getColor(view.context,R.color.app_red))
//                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_base_warning, 0, 0, 0)
//            }else{
//                snackbar.setActionTextColor(ContextCompat.getColor(view.context,R.color.app_green))
//                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
//            }
//        }catch (e : Exception){
//            view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorProgressBackground))
//            textView.setTextColor(Color.WHITE)
//            if (!isSuccess) {
//                snackbar.setActionTextColor(Color.RED)
//                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_base_warning, 0, 0, 0)
//            }else{
//                snackbar.setActionTextColor(Color.GREEN)
//                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
//            }
//        }
//        textView.compoundDrawablePadding = 8
//        textView.setTextSize(16f)
//        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
//        snackbar.show()
//    }
//    override fun onAttach(context: Context) {
//        AndroidSupportInjection.inject(this)
//        super.onAttach(context)
//    }

    open fun initView() {}

//     override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        initView()
//        observeViewModel()
//    }
}
