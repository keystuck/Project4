package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.util.regex.Matcher

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemsLVMTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

   @ExperimentalCoroutinesApi
   @get:Rule
   var mainCoroutineRule = MainCoroutineRule()


    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource


    private val reminder1DTO = ReminderDTO("Title1", "desc1",
        "location1", 1.234, 2.345)
    private val reminder1DataItem = ReminderDataItem("Title1", "desc1",
        "location1", 1.234, 2.345, reminder1DTO.id)

    private val reminder2DTO = ReminderDTO("Title2", "desc1",
        "location1", 1.234, 2.345)
    private val reminder2DataItem = ReminderDataItem("Title2", "desc1",
        "location1", 1.234, 2.345, reminder2DTO.id)


    private val remindersList = listOf(reminder1DTO, reminder2DTO).sortedBy { it.id }


    @Before
    fun setUp(){
        fakeDataSource = FakeDataSource(remindersList.toMutableList())
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
              fakeDataSource)
    }

    @After
    fun tearDown(){
        stopKoin()
    }

    @Test
    fun loadReminders_showsReminders(){

        remindersListViewModel.loadReminders()

        val value: List<ReminderDataItem>? = remindersListViewModel.remindersList.value
        assertThat(value, hasItems(reminder1DataItem, reminder2DataItem)
        )
    }

    @Test
    fun loadRemindersWithReturnError_returnsError() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
        fakeDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()

        assertThat(
                remindersListViewModel.showSnackBar.getOrAwaitValue(),
                `is`("Test Exception")
        )
    }

    @Test
    fun loadrems_showload() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(
                remindersListViewModel.showLoading.getOrAwaitValue(),
                `is`(true)
        )

        mainCoroutineRule.resumeDispatcher()
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )
    }

}