Feature: Check current location temperature

  User journey that exercise his usage of app functionality
  to check weather data for current user location

  Scenario: User check current location temperature
    Given User launch app from device home for first time
    Then App should show temperature for user location in metric units
    When User leave app
    And Device network is disconnected
    When User relaunch app from home
    Then Temperature for last location should be shown in metric units


