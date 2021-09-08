Feature: Check world location temperature

  User journey that exercise his usage of app functionality
  to check temperature for a world location

  Scenario: User browse world location temperature
    Given User launch app from device home
    And User search for a world location
    When User select to browse location result weather
    Then Selected location temperature should be shown