package com.diskin.alon.pagoda.common.events

/**
 * [WeatherUnitsEventHandler] unit test class.
 */
class WeatherUnitsEventHandlerTest {

//    // Test subject
//    private lateinit var eventHandler: WeatherUnitsEventHandler
//
//    // Collaborators
//    private val app: Application= mockk()
//
//    @Before
//    fun setUp() {
//        eventHandler = WeatherUnitsEventHandler(app)
//    }
//
//    @Test
//    fun updateEventValueWhenEventPosted() {
//        // Test case fixture
//        val sp: SharedPreferences = mockk()
//        val editor: SharedPreferences.Editor = mockk()
//
//        every { app.getSharedPreferences(any(),any()) } returns sp
//        every { sp.edit() } returns editor
//        every { editor.putInt(any(),any()) } returns editor
//        every { editor.apply() } returns Unit
//
//        // Given
//
//        // When
//        val  event = UnitSystemEvent.IMPERIAL
//        eventHandler.post(event)
//
//        // Then
//        verify { app.getSharedPreferences(WeatherUnitsEventHandler.PREF_FILE,Context.MODE_PRIVATE) }
//        verify { editor.putInt(WeatherUnitsEventHandler.UNIT_KEY,event.ordinal) }
//    }
}