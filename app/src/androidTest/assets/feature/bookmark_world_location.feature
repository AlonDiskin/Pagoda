Feature: World location bookmarking

  User journey that exercise his usage of app functionality
  to bookmark an existing world location to own managed list of locations

  Scenario: User bookmark location
    Given User has no bookmarked locations
    And User launch app from device home
    And Open search locations screen
    When User find wanted location
    And Select to bookmark it
    Then App should bookmark location and update bookmarks list
    When User select to remove bookmark
    Then App should remove bookmark from user bookmarks listing