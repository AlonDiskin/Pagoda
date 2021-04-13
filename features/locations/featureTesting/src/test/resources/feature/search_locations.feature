Feature: Search world locations

  #Rule: Provide world locations search

  @search-location
  Scenario: User search for location
    Given User open location search screen
    When User search for location with partial query
    Then All locations whose name starts with query should be shown
    When User search for location with exact location name
    Then All matching locations by name should be shown