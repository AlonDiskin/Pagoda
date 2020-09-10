package com.diskin.alon.pagoda.settings

import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

/**
 * Step definitions for 'User change data units setting' scenario.
 */
class WeatherUnitsChangeSteps : GreenCoffeeSteps() {

    @Given("^App weather units system is set to metric$")
    fun appWeatherUnitsSystemIsSetToMetric() {
        TODO()
    }

    @And("^User launch app from device home$")
    fun userLaunchAppFromDeviceHome() {
        TODO()
    }

    @And("^Open app settings screen$")
    fun openAppSettingsScreen() {
        TODO()
    }

    @And("^Select unit system as imperial$")
    fun selectUnitSystemAsImperial() {
        TODO()
    }

    @When("^User open weather data screen$")
    fun userOpenWeatherDataScreen() {
        TODO()
    }

    @Then("^Weather data should be shown in imperial units$")
    fun weatherDataShouldBeShownInImperialUnits() {
        TODO()
    }
}