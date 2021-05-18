package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeTestRepository
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.test.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var repository: FakeTestRepository
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun init() {
        repository = FakeTestRepository()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
        repository)

        stopKoin()

        val myModule = module{
            viewModel {
                RemindersListViewModel(getApplicationContext(),
                        repository)
            }
            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext())
            }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(myModule))

        }
    }

    @After
    fun tearDown(){
        stopKoin()
    }



    @Test
    fun reminderList_DisplayedInUI() = runBlockingTest {
        val reminder = ReminderDTO("TestReminder", "desc", "loc", 1.0, 2.0)

        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
                repository)
        repository.saveReminder(reminder)
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(withText("TestReminder")))

        onView(withId(R.id.description)).check(matches(isDisplayed()))
        onView(withId(R.id.description)).check(matches(withText("desc")))

        onView(withId(R.id.location)).check(matches(isDisplayed()))
        onView(withId(R.id.location)).check(matches(withText("loc")))

    }

    @Test
    fun noData_showsNoData() = runBlockingTest {
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
                repository)
        repository.deleteAllReminders()
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

    }

    @Test
    fun clickFAB_navigateToSaveReminderFragment() = runBlockingTest {
        val navController = mock(NavController::class.java)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }



//
}