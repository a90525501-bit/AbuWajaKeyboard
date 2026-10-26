package com.abuwaja.keyboard

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1A1A1A"))
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "أبو وجع الصبيحي"
            textSize = 28f
            setTextColor(Color.parseColor("#FFD700"))
            gravity = Gravity.CENTER
        }

        val step1 = Button(this).apply {
            text = "١) تفعيل الكيبورد"
            setOnClickListener {
                startActivity(android.content.Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        val step2 = Button(this).apply {
            text = "٢) اختيار الكيبورد"
            setOnClickListener {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showInputMethodPicker()
            }
        }

        root.addView(title)
        root.addView(step1)
        root.addView(step2)
        setContentView(root)
    }
}
