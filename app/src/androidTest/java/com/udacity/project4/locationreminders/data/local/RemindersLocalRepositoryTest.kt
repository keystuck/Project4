package com.udacity.project4.locationreminders.data.local

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO("Title1", "desc1",
    "location1", 1.234, 2.345)
    private val reminder2 = ReminderDTO("Title2", "desc1",
            "location1", 1.234, 2.345)
    private val reminder3 = ReminderDTO("Title3", "desc1",
            "location1", 1.234, 2.345)



//    private lateinit var reminderDataSource: FakeDataSource
    private lateinit var remindersRepository: RemindersLocalRepository



    @Before
    fun initDb(){
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        )
                .allowMainThreadQueries()
                .build()
        remindersRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)

    }

    @After
    fun closeDb() = database.close()

    @Test
    fun getReminders_requestsAllRemindersFromDataSource() = runBlocking {
        remindersRepository.saveReminder(reminder1)
        val remindersResult = remindersRepository.getReminders()

        assertThat((remindersResult is Result.Success), `is`(true))
            val resultList = remindersResult as Result.Success
            assertThat(resultList.data, notNullValue())
            assertThat(resultList.data[0].title, `is`(reminder1.title))

    }


    @Test
    fun getReminderById_returnsCorrectReminder() = runBlocking {
        remindersRepository.saveReminder(reminder1)
        val remindersResult = remindersRepository.getReminder(reminder1.id)

        assertThat((remindersResult is Result.Success), `is`(true))
        val resultList = remindersResult as Result.Success
        assertThat(resultList.data, notNullValue())
        assertThat(resultList.data.title, `is`(reminder1.title))
    }

    @Test
    fun deleteAllReminders_deletesAllReminders() = runBlocking {
        remindersRepository.saveReminder(reminder1)
        remindersRepository.deleteAllReminders()

        val remindersResult = remindersRepository.getReminders()

        assertThat((remindersResult is Result.Success), `is`(true))
        val resultList = remindersResult as Result.Success
        assertThat(remindersResult.data, empty())
    }



}