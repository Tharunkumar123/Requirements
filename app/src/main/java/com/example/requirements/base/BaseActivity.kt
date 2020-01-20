package com.example.requirements.base

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.requirements.R
import com.jaeger.library.StatusBarUtil

abstract class BaseActivity : AppCompatActivity() {

    abstract fun setLayout(): Int

    abstract fun initView(savedInstanceState: Bundle?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(setLayout())
        initView(savedInstanceState)

    }


}