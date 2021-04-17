package com.udacity.project4.locationreminders.reminderslist

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasItems
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersLVMTest {

    //TODO: Live data objects are tested using shouldReturnError and check_loading testing functions.

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private val reminder1DTO = ReminderDTO("Title1", "desc1",
        "location1", 1.234, 2.345)
    private val reminder1DataItem = ReminderDataItem("Title1", "desc1",
        "location1", 1.234, 2.345, reminder1DTO.id)

    private val reminder2DTO = ReminderDTO("Title2", "desc1",
        "location1", 1.234, 2.345)
    private val reminder2DataItem = ReminderDataItem("Title2", "desc1",
        "location1", 1.234, 2.345, reminder2DTO.id)


    private val remindersList = listOf(reminder1DTO, reminder2DTO).sortedBy { it.id }


    @Test
    fun loadReminders_showsReminders(){
        val remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
            FakeDataSource(remindersList.toMutableList()))
        remindersListViewModel.loadReminders()

        val value: List<ReminderDataItem>? = remindersListViewModel.remindersList.value
        assertThat(value, hasItems(reminder1DataItem, reminder2DataItem)

        )
    }

}