Feature: Search world location weather

  User journey that exercise his usage of app functionality
  to search weather data for a world location

  Scenario: User search world location weather
    Given User launch app from device home
    And User search for location other then his current one
    Then All matching locations by name should be shown
#    And Select the first location result
#    Then Selected location weather data should be shown