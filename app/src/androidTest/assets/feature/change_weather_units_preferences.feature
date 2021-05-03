Feature: Changing weather units preferences

  User journey that exercise his usage of app functionality
  to change weather data units preferences.

  Scenario: User changes weather units
    Given User has not changed default units preferences
    And User launch app from device home
    And Open settings screen
    When User change temperature and time format units
    And Open location weather screen
    Then Current temp and time should be updated according to units change