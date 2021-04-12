package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
        if (intent.hasExtra(EXTRA_ReminderDataItem)){
            val serializable = intent.getSerializableExtra(EXTRA_ReminderDataItem)

            val reminderDataItem = serializable as ReminderDataItem
            Log.i("RemDescAct", "title ${reminderDataItem.title}")

            binding.reminderDataItem = reminderDataItem

//
//            reminderString = reminderString.substring(
//                    reminderString.indexOf(",")+2
//            )
//            binding.reminderDataItem!!.description = reminderString.substring(
//                    reminderString.indexOf("=") + 1,
//                    reminderString.indexOf(",")
//            )
//
//            reminderString = reminderString.substring(
//                    reminderString.indexOf(",")+2
//            )
//
//            val location = reminderString.substring(
//                    reminderString.indexOf("="),
//                    reminderString.indexOf(",")
//            )



      }

//        TODO: Add the implementation of the reminder details
    }
}
