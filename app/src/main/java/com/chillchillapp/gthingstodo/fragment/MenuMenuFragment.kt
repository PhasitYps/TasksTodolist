package com.chillchillapp.gthingstodo.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import com.bumptech.glide.Glide
import com.chillchillapp.gthingstodo.*
import com.chillchillapp.gthingstodo.dialog.SignOutDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_menu.*

@SuppressLint("UseRequireInsteadOfGet")
class MenuMenuFragment : BaseFragment(R.layout.fragment_menu)  {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBase()

        setEvent()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }


    private fun setEvent(){

        signRL.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            activity!!.startActivity(intent)
        }

        signoutIV.setOnClickListener {
            if (auth.currentUser != null){
                showSignOutDialog()
            }
        }

        menuCategoryCV.setOnClickListener {
            val intent = Intent(activity!!, CategoryActivity::class.java)
            startActivity(intent)
        }

        menuFavoriteCV.setOnClickListener {
            val intent = Intent(activity!!, FavoriteActivity::class.java)
            startActivity(intent)
        }

        menuThemeCV.setOnClickListener {
            val intent = Intent(activity!!, ThemeActivity::class.java)
            startActivity(intent)
        }

        menuSyncCV.setOnClickListener {
            val intent = Intent(activity!!, SynchronizetionActivity::class.java)
            startActivity(intent)
        }

        menuLanguageCV.setOnClickListener {
            val intent = Intent(activity!!, LanguageActivity::class.java)
            startActivity(intent)
        }

        menuUpgradeProCV.setOnClickListener {
            val intent = Intent(requireContext(), UpgradePremium::class.java)
            startActivity(intent)
        }
    }

    private fun showSignOutDialog(){
        val d = SignOutDialog(activity!!)
        d.setOnMyEvent(object : SignOutDialog.OnMyEvent{
            override fun OnPositiveListener(it: Task<Void>) {
                if(it.isSuccessful){
                    signRL.visibility = View.VISIBLE
                    outRL.visibility = View.GONE
                }
            }
        })

    }

    private fun updateUI(){
        val user = auth.currentUser
        if (user != null){
            signRL.visibility = View.GONE
            outRL.visibility = View.VISIBLE

            Glide.with(this).asBitmap().circleCrop().load(user!!.photoUrl).into(imageUrlUserIV)
            usernameTV.text = user.displayName
            useremailTV.text = user.email

        }else{
            signRL.visibility = View.VISIBLE
            outRL.visibility = View.GONE
        }
    }

}