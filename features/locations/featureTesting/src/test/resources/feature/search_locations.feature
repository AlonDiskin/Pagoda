Feature: Search world locations

  #Rule: Provide world locations search

  @search-location
  Scenario: User search for location
    Given User open location search screen
    When User search for location with partial query
    Then All locations whose name starts with query should be shown
    When User search for location with exact location name
    Then All matching locations by name should be shown

  #Rule: Show selected search result location weather

  @show-location-weather
  Scenario: Search result location weather shown
    Given User open location search screen
    When User search for location
    And Select the first search result
    Then Selected location weather should be shown in weather data screen

  #Rule: Allow to add searched locations to user manged list

  @bookmark-search-result
  Scenario: User bookmark location result
    Given User open location search screen
    When User search for location
    And Select to bookmark first result
    Then App should bookmark select location
    When User open bookmarked locations screen
    Then Bookmarked location should be listed



