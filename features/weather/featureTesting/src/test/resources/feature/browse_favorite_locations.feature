Feature: Browse bookmarked world locations

  #Rule: list all favorite world locations

  @Favorite-listed
  Scenario: Favorite locations listed
    Given Previously favorite locations exist
    When User open favorite screen
    Then All favorite locations are listed in descending added order
    When User select first listed location
    Then App should show selected location weather data

  #Rule: Provide location unfavorite option

  @unfavorite-locations
  Scenario: User unfavorite locations
    Given Previously favorite locations exist
    When User open favorite locations screen
    And Selects to unfavorite first and last listed locations
    Then App should unfavorite locations
    And App should update shown list

