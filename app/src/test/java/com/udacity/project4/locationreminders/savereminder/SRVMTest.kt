package com.udacity.project4.locationreminders.savereminder

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SRVMTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val reminder1DTO = ReminderDTO("Title1", "desc1",
            "location1", 1.234, 2.345)
    private val reminder1DataItem = ReminderDataItem("Title1", "desc1",
            "location1", 1.234, 2.345, reminder1DTO.id)

    private val reminder2DTO = ReminderDTO("Title2", "desc1",
            "location1", 1.234, 2.345)
    private val reminder2DataItem = ReminderDataItem("Title2", "desc1",
            "location1", 1.234, 2.345, reminder2DTO.id)

    private val reminder3DTO = ReminderDTO("Title3", "desc1",
            "location1", 1.234, 2.345)
    private val reminder3DataItem = ReminderDataItem("Title3", "desc1",
            "location1", 1.234, 2.345, reminder2DTO.id)


    private val remindersList = listOf(reminder1DTO, reminder2DTO).sortedBy { it.id }

    //TODO: provide testing to the SaveReminderView and its live data objects

    @Test
    fun addNewReminder_savesNewReminder(){
        val saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),
            FakeDataSource(remindersList.toMutableList()))



        saveReminderViewModel.saveReminder(reminder3DataItem)

        val titleValue = (saveReminderViewModel.reminderTitle as LiveData<String>).getOrAwaitValue()
        assertThat(
               titleValue, (not(nullValue()))
        )

    }



}