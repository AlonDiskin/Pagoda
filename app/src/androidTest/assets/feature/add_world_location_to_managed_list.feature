Feature: User adds world location to locations list

  User journey that exercise his usage of app functionality
  to add an existing world location to own managed list of saved locations

  Scenario: User add location
    Given User launch app from device home
    And Open locations screen
    And Select to add a new location
    Then App should open world locations search screen
    When User find wanted location
    And Select to add it
    Then App should add location and update locations list