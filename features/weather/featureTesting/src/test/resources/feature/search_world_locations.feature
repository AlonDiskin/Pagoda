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

  @favorite-search-result
  Scenario: User favorite location search result
    Given User open locations search screen
    When User search for location
    And Select to favorite first result
    Then App should favorite select location
    When User open favorite locations screen
    Then Favorite location should be listed



