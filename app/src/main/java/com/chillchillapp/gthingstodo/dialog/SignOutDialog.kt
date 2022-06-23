package com.chillchillapp.gthingstodo.dialog

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.chillchillapp.gthingstodo.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.dialog_logout.*

class SignOutDialog(private var activity: Activity): Dialog(activity) {

    interface OnMyEvent{
        fun OnPositiveListener(it: Task<Void>)
    }
    private var l: OnMyEvent? = null
    fun setOnMyEvent(l: OnMyEvent){
        this.l = l
    }

    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_logout)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(true)
        show()

        setEvent()
    }

    private fun setEvent(){

        positiveTV.setOnClickListener {
            signOut()
            dismiss()
        }

        negativeTV.setOnClickListener {
            dismiss()
        }

    }

    private fun signOut() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            if(it.isSuccessful){
                auth.signOut()
                l?.OnPositiveListener(it)
            }
        }
    }
}