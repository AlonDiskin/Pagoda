Feature: Mange saved world locations

  #Rule: Provide location unbookmarking option

  @unbookmark-locations
  Scenario: User unbookmark locations
    Given Previously bookmarked locations exist
    When User open bookmarked locations screen
    And Selects to unbookmark first and last listed locations
    Then App should unbookmark locations
    And App should update shown list

  #Rule: Provide world location adding option

  @add-bookmarked-location
  Scenario: User add bookmarked location
    Given User open saved locations screen
    When User select to add a new saved location
    Then App should open world location search screen