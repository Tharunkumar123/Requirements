package com.example.requirements.datePicker

import android.os.Bundle
import com.example.requirements.R
import com.example.requirements.base.BaseActivity
import kotlinx.android.synthetic.main.activity_date_picker.*

class DatePickerActivity : BaseActivity() {
    override fun setLayout(): Int {
        return R.layout.activity_date_picker
    }

    override fun initView(savedInstanceState: Bundle?) {
        from.setMinDate("22/01/2020")
        to.setMaxDate("01/12/2021")

        addEvent.setOnClickListener {
//            val calendarEvent = Calendar.getInstance()
//            val intent = Intent(Intent.ACTION_EDIT)
//            intent.type = "vnd.android.cursor.item/event"
//            intent.putExtra("beginTime", calendarEvent.timeInMillis)
//            intent.putExtra("endTime", calendarEvent.timeInMillis + 60 * 60 * 1000)
//            intent.putExtra("title", "Sample Event")
//            intent.putExtra("allDay", true)
//            intent.putExtra("rule", "FREQ=YEARLY")
//            startActivity(intent)
        }
    }
}
