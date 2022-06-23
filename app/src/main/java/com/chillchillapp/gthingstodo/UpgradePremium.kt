package com.chillchillapp.gthingstodo

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_upgrade_premium.*

class UpgradePremium: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setContentView(R.layout.activity_upgrade_premium)


        event()
    }

    private fun event(){

        closeIV.setOnClickListener {
            finish()
        }

        buyEverytimeRL.setOnClickListener {

        }

        buyYearlyRL.setOnClickListener {

        }

        buyMonthlyRL.setOnClickListener {

        }
    }
}