Feature: World location bookmarking

  User journey that exercise his usage of app functionality
  to bookmark an existing world location to own managed list of locations

  Scenario: User bookmark location
    Given User has no bookmarked locations
    And User launch app from device home
    And Open locations screen
    And Select to add a new location
    Then App should open world locations search screen
    When User find wanted location
    And Select to bookmark it
    Then App should bookmark location and update locations list