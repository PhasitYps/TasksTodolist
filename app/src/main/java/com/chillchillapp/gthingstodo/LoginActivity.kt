package com.chillchillapp.gthingstodo

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_authentication.*

class LoginActivity: BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleApiClient: GoogleSignInClient
    private var myUserRef: CollectionReference? = null

    private val RC_SIGN_IN = 1
    private var dialog_load: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setTheme()
        setContentView(R.layout.activity_authentication)

        init()
        initDialogLoad()
        setEvent()
    }

    override fun onResume() {
        super.onResume()

        val user = auth.currentUser
        updateUI(user)

    }

    private fun setEvent() {

        googleLoginLL.setOnClickListener {
            signIn()
        }

        backTV.setOnClickListener {
            finish()
        }
    }

    private fun init(){
        backTV.paintFlags = Paint.UNDERLINE_TEXT_FLAG


        auth = FirebaseAuth.getInstance()
        myUserRef = FirebaseFirestore.getInstance().collection(KEY_USERPROFILE)

    }

    private fun initDialogLoad(){
        dialog_load = Dialog(this)
        dialog_load!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog_load!!.setContentView(R.layout.dialog_loading)
        dialog_load!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog_load!!.window!!.setLayout(
            ActionBar.LayoutParams.WRAP_CONTENT,
            ActionBar.LayoutParams.WRAP_CONTENT
        )

        dialog_load!!.create()

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {

                Log.d("test", "signInWithCredential:success")
                val user = auth.currentUser
                myUserRef!!.document(user!!.uid).get().addOnSuccessListener {
                    val uid = it[KEY_UID]

                    if (uid == null) {
                        createUser(user)
                    } else {
                        updateUI(user)
                    }

                    dialog_load!!.dismiss()
                }
            } else {

                Log.w("test", "signInWithCredential:failure", task.exception)
                showToast("Authentication Failed.")
                updateUI(null)
            }
        }
    }

    private fun signIn() {

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE)) //Scope(Scopes.DRIVE_FILE)
            .build()

        //  .requestScopes(Scope(Scopes.PROFILE), Scope("https://www.googleapis.com/auth/drive.file"))

        mGoogleApiClient = GoogleSignIn.getClient(this, signInOptions)
        startActivityForResult(mGoogleApiClient.signInIntent, RC_SIGN_IN)
    }

    private fun createUser(user: FirebaseUser?){
        val uid = user!!.uid
        val username = user!!.displayName
        val profile = user!!.photoUrl.toString()
        val emailuser = user!!.email

        val map: MutableMap<String, Any> = HashMap()
        map[KEY_UID] = uid
        map[KEY_NAME] = username!!
        map[KEY_IMAGEURL] = profile
        map[KEY_EMAIL] = emailuser.toString()

        myUserRef!!.document(uid).set(map).addOnSuccessListener {
            updateUI(user)
        }
    }

    private fun updateUI(user: FirebaseUser?){

        if(user != null){
            finish()
        }
    }

    private fun showToast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            GoogleSignIn.getSignedInAccountFromIntent(data).addOnCompleteListener {
                dialog_load!!.show()

                if(it.isSuccessful){
                    val account = it.result
                    firebaseAuthWithGoogle(account.idToken!!)

                    Log.d("test", "firebaseAuthWithGoogle:" + account.id)
                }else{
                    Log.w("test", "Google sign in failed: ", it.exception)
                    dialog_load!!.dismiss()
                    // ...
                }
            }
            /*
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                dialog_load!!.show()
                val account = task.getResult(ApiException::class.java)!!
                Log.d("test", "firebaseAuthWithGoogle:" + account.id)

                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {

                Log.w("test", "Google sign in failed", e)
                dialog_load!!.dismiss()
                // ...
            }*/
        }
    }


}