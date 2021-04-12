package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.events.UnitSystemEvent
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventProvider
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.implementations.AppPrefsStoreImpl
import com.diskin.alon.pagoda.weatherinfo.domain.UnitSystem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

/**
 * [AppPrefsStoreImpl] unit test class.
 */
class AppPrefsStoreImplTest {

    // Test subject
    private lateinit var store: AppPrefsStoreImpl

    // Collaborators
    private val eventProvider: WeatherUnitsEventProvider = mockk()
    private val mapper: Mapper<UnitSystemEvent, UnitSystem> = mockk()

    @Before
    fun setUp() {
        store = AppPrefsStoreImpl(eventProvider, mapper)
    }

    @Test
    fun fetchUnitPrefWhenQueried() {
        // Test case fixture
        val event: UnitSystemEvent = mockk()
        val mappedValue: UnitSystem = mockk()

        every { eventProvider.get() } returns Observable.just(event)
        every { mapper.map(any()) } returns mappedValue

        // Given

        // When store queried for units pref
        val observer = store.getUnitSystem().test()

        // Then store should get observable units pref stream
        verify { eventProvider.get() }

        // And propagate a mapped result model to client
        verify { mapper.map(event) }
        observer.assertValue(Result.Success(mappedValue))
    }
}