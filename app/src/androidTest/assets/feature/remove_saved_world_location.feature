Feature: Remove saved location

  User journey that exercise his usage of app functionality
  to remove previously saved world location

  Scenario: User remove saved location
    Given User has previously  saved locations
    And User launch app from device home
    And Open locations screen
    And Removes first listed location
    Then App should show updated locations accordingly