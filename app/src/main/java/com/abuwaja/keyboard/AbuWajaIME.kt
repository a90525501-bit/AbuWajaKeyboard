package com.abuwaja.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class AbuWajaIME : InputMethodService() {

    private val row1 = listOf("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج")
    private val row2 = listOf("ش","س","ي","ب","ل","ا","ت","ن","م","ك","ط")
    private val row3 = listOf("ئ","ء","ؤ","ر","لا","ى","ة","و","ز","ظ","د")

    override fun onCreateInputView(): View {
        val view = LayoutInflater.from(this).inflate(R.layout.keyboard_view, null)

        fillRow(view.findViewById(R.id.row1), row1)
        fillRow(view.findViewById(R.id.row2), row2)
        fillRow(view.findViewById(R.id.row3), row3)

        view.findViewById<Button>(R.id.key_space).setOnClickListener {
            currentInputConnection?.commitText(" ", 1)
        }
        view.findViewById<Button>(R.id.key_delete).setOnClickListener {
            currentInputConnection?.deleteSurroundingText(1, 0)
        }
        view.findViewById<Button>(R.id.key_enter).setOnClickListener {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
        return view
    }

    private fun fillRow(row: LinearLayout, keys: List<String>) {
        keys.forEach { k ->
            val b = Button(this).apply {
                text = k
                textSize = 18f
                minWidth = 0
                minimumWidth = 0
                minHeight = 0
                minimumHeight = 0
                setPadding(0, 0, 0, 0)
                setBackgroundResource(R.drawable.key_bg)
                setTextColor(0xFFFFD700.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                ).apply { setMargins(2, 2, 2, 2) }
                setOnClickListener {
                    currentInputConnection?.commitText(k, 1)
                }
            }
            row.addView(b)
        }
    }
}
