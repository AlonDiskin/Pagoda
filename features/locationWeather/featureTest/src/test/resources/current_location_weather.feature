Feature:

  Display the weather info for the user current location

  Rule: Display latest weather detail for current location

    Scenario: Latest location weather data shown
      Given User device location services and connectivity are on
      And User open current weather screen
      Then Weather detail of user current location is shown

    Scenario : Device needed services not turned on
      Given User device service
