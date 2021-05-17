Feature: Browse world location temperature

  User journey that exercise his usage of app functionality
  to browse temperature for a world location

  Scenario: User browse world location temperature
    Given User launch app from device home
    And User search for location other then his current one
    Then All matching locations by name should be shown
    And Select the first location result
    Then Selected location temperature should be shown