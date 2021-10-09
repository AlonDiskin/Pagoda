Feature: World location favoriting

  User journey that exercise his usage of app functionality
  to favorite an existing world location to own favorite locations

  Scenario: User favorite location
    Given User has no favorite locations
    And User launch app from device home
    And Open search locations screen
    When User find wanted location
    And Select to favorite it
    Then App should favorite location and update favorites list
    When User select to unfavorite added location
    Then App should remove location from user favorites listing