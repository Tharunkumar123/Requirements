package com.example.requirements.widgets

import android.app.DatePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.requirements.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DatePickerClass : LinearLayout {

    private var editTxt: EditText? = null
    private var tlMain: TextInputLayout? = null
    private var minimumDate: String = "22/01/2020"
    private var maximumDate: String = "01/12/2021"
    private val format = SimpleDateFormat("dd/MM/yyyy")
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (!isInEditMode)
            init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (!isInEditMode)
            init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.DatePickerClass, defStyleAttr, 0)
        val text = typedArray.getString(R.styleable.DatePickerClass_setTextHint)
        val hintColor = typedArray.getColor(
            R.styleable.DatePickerClass_textColorHint,
            0
        )
        inflateView()
        var calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            context,
            DatePickerDialog.OnDateSetListener { arg0, year, month, day_of_month ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day_of_month)

                when {
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ->
                        Toast.makeText(
                        context,
                        "Starurday not be selected",
                        Toast.LENGTH_LONG
                    ).show()

                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> Toast.makeText(
                        context,
                        "Sunday not be selected",
                        Toast.LENGTH_LONG
                    ).show()
                    else -> editTxt?.setText(format.format(calendar.time))
                }

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        editTxt?.setOnClickListener { view: View ->
            datePickerDialog.datePicker.minDate = getMinimumDate()
            datePickerDialog.datePicker.maxDate = getMaximumDate()
            datePickerDialog.show()
        }
        setHint(text)
        typedArray.recycle()
    }

    private fun getMinimumDate(): Long {
        var milliseconds = 0L
        try {
            val d = format.parse(minimumDate)
            milliseconds = d!!.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return milliseconds
    }

    private fun getMaximumDate(): Long {
        var millisecondss = 0L
        try {
            val date = format.parse(maximumDate)
            millisecondss = date!!.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return millisecondss
    }

    private fun setHint(editTxtHint: String?) {
        tlMain?.hint = editTxtHint ?: ""
    }

    private fun inflateView() {
        val myView = LayoutInflater.from(context).inflate(R.layout.edit_text_date_set, this, true)
        editTxt = myView.findViewById(R.id.dateSelector)
        tlMain = myView.findViewById(R.id.tlMain)
    }

    fun setMinDate(date: String) {
        minimumDate = date
    }

    fun setMaxDate(maxDate: String) {
        maximumDate = maxDate
    }
}
