Feature: Browse location weather

  User journey that exercise his usage of app functionality
  to browse weather data for current user location

  Scenario: User check location temperature in different unit system
    Given User launch app from device home
    When User select different weather units system
    And Check temperature for current location
    Then Location temperature should be shown according to selected unit