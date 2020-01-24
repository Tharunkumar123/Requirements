package com.example.requirements.widgets

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.requirements.R
import kotlinx.android.synthetic.main.custom_search_view.view.*

class ClearableEditText : EditText, TextWatcher {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (!isInEditMode)
            initWithAttrs()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (!isInEditMode)
            initWithAttrs()
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun afterTextChanged(s: Editable) {
        if (s.toString().isNotEmpty()) {
            showIcon(R.drawable.ic_clear)
        } else {
            showIcon(R.drawable.ic_search)
        }

        searchEditText.onRightDrawableClicked {
            if (s.toString().length>1){
                it.text.clear()
            }
        }
    }

    private fun initWithAttrs() {
        showIcon(R.drawable.ic_search)
        addTextChangedListener(this)
    }

    private fun showIcon(icon: Int) {
        setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ContextCompat.getDrawable(context, icon),
            null
        )
    }

    private fun EditText.onRightDrawableClicked(OnClicked:(view:EditText)->Unit){
        setOnTouchListener { v, event ->
            var initialIconClear = false
            if (v is EditText) if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    OnClicked(this)
                }
                initialIconClear = true
            }
            initialIconClear
        }
    }
}