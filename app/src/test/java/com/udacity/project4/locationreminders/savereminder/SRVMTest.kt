package com.udacity.project4.locationreminders.savereminder

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin



@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SRVMTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val reminder1DTO = ReminderDTO("Title1", "desc1",
            "location1", 1.234, 2.345)

    private val reminder2DTO = ReminderDTO("Title2", "desc1",
            "location1", 1.234, 2.345)

    private val reminder3DataItem = ReminderDataItem("Title3", "desc1",
            "location1", 1.234, 2.345, reminder2DTO.id)

    private lateinit var saveReminderViewModel: SaveReminderViewModel


    private val remindersList = listOf(reminder1DTO, reminder2DTO).sortedBy { it.id }


    @Before
    fun initViewModel(){
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),
                FakeDataSource(remindersList.toMutableList()))
    }

    @After
    fun tearDown(){
        stopKoin()
    }

    @Test
    fun validateAndSaveReminder_savesNewReminder(){

        saveReminderViewModel.validateAndSaveReminder(reminder3DataItem)

        val value = saveReminderViewModel.showToast.value
        assertThat(
               value, `is`("Reminder Saved !")
        )
    }

    @Test
    fun clearData_voidsValues(){
        saveReminderViewModel.onClear()

        val value = saveReminderViewModel.reminderTitle.getOrAwaitValue()
        assertThat(value, nullValue())
    }

    @Test
    fun validateAndSave_rejectsEmptyTitle(){
        val badReminder1DataItem = ReminderDataItem("", "description", "location", 1.234, 2.345)

        saveReminderViewModel.validateAndSaveReminder(badReminder1DataItem)
        val value = saveReminderViewModel.showSnackBarInt.value
        assertThat(value, `is`(R.string.err_enter_title))
    }
}