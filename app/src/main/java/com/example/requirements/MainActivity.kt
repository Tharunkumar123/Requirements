package com.example.requirements

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.requirements.base.BaseActivity
import com.king.view.superslidingpanelayout.SuperSlidingPaneLayout

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.util.*

class MainActivity : BaseActivity() {
    val totalCorners = 30f
    override fun setLayout(): Int {
        return R.layout.activity_main
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun initView(savedInstanceState: Bundle?) {
        Log.v("MAIN ACTIVITY", "fadecolour set" )
        superSlidingPaneLayout.sliderFadeColor = 0



        if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }
        btnLeft.setOnClickListener {
            superSlidingPaneLayout.openPane()

        }

navigationTabLayout()
        superSlidingPaneLayout.setPanelSlideListener(object :
            SuperSlidingPaneLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                Log.v("MAIN ACTIVITY", "onPanelSlide" )
                if (slideOffset>0.1f)
                setCorners(slideOffset)
//                mainLayoutCard.radius = 0f
            }

            override fun onPanelClosed(panel: View?) {
                Log.v("MAIN ACTIVITY", "onPanelClosed" )
                mainLayoutCard.radius = 0f
            }

            override fun onPanelOpened(panel: View?) {
                Log.v("MAIN ACTIVITY", "drawer opened" )
                mainLayoutCard.radius = totalCorners
            }

        })


    }

    private fun navigationTabLayout() {
        val ntbSample5 =
            findViewById<View>(R.id.ntb_sample_5) as NavigationTabBar
        val models5 =
            ArrayList<NavigationTabBar.Model>()

        models5.add(
            NavigationTabBar.Model.Builder(
               Color.parseColor("#FF6F00")
            ).title("jdshgdi").build()
        )
        models5.add(
            NavigationTabBar.Model.Builder(
            Color.parseColor("#FF6F00")
            ).title("jdshgdi").build()
        )
        models5.add(
            NavigationTabBar.Model.Builder(
              Color.parseColor("#FF6F00")
            ).title("jdshgdi").build()
        )
        ntbSample5.models = models5
        ntbSample5.setModelIndex(0, true)

    }

    private fun setCorners(slideOffset: Float) {

        val percentage = slideOffset * 100
        val cornerValue = (totalCorners * percentage) / 100
        mainLayoutCard.radius = cornerValue

    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            Log.v("MAIN ACTIVITY", "window flag 1 " )
            winParams.flags = winParams.flags or bits
        } else {
            Log.v("MAIN ACTIVITY", "window flag 2" )
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

}
