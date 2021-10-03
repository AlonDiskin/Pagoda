Feature: Browse bookmarked world locations

  #Rule: list all bookmarked world locations

  @locations-listed
  Scenario: Bookmarked locations listed
    Given Previously bookmarked locations exist
    When User open bookmarked locations screen
    Then All bookmarked locations are listed in descending added order
    When User select first listed location
    Then App should show selected location weather data

  #Rule: Provide location unbookmarking option

  @unbookmark-locations
  Scenario: User unbookmark locations
    Given Previously bookmarked locations exist
    When User open bookmarked locations screen
    And Selects to unbookmark first and last listed locations
    Then App should unbookmark locations
    And App should update shown list

