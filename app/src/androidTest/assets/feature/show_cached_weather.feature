Feature: Show cached weather data

  User journey that exercise his usage of app functionality
  to cache weather data update and use it when needed

  Scenario: Cached weather data is shown
    Given App has cached weather data for user location weather
    And Device network is disconnected
    When User launch app from device home
    Then Cached weather data shown for user location weather