package com.diskin.alon.pagoda.settings.featurtetesting

import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

/**
 * Step definitions for 'User change unit system' scenario.
 */
class WeatherUnitsChangedSteps : GreenCoffeeSteps(){

    @Given("^Weather data unit system preference is set as \"([^\"]*)\"$")
    fun weatherDataUnitSystemPreferenceIsSetAs(arg0: String?) {

    }

    @When("^User open app settings screen$")
    fun userOpenAppSettingsScreen() {
    }

    @And("^Select unit system as \"([^\"]*)\"$")
    fun selectUnitSystemAs(arg0: String?) {

    }

    @Then("^App should set unit preference as \"([^\"]*)\"$")
    fun appShouldSetUnitPreferenceAs(arg0: String?) {
        TODO()
    }
}