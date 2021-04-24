Feature: Browse current location temperature

  User journey that exercise his usage of app functionality
  to browse temperature for current user location

  Scenario: User browse current location temperature
    Given User launch app from device home
    When User select different weather units system
    And Check temperature for current location
    Then Location temperature should be shown according to selected unit