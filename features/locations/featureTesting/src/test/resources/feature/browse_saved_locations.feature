Feature: Browse saved locations

  #Rule: list all saved world  locations

  @locations-listed
  Scenario: Saved locations listed
    Given Previously saved locations exist
    When User open saved locations screen
    Then All saved locations are listed in descending added order
    When User select first listed location
    Then App should show selected location weather data

  #Rule: Provide saved location removal option

  @delete-saved-location
  Scenario: User delete saved location
    Given Previously saved locations exist
    When User open saved locations screen
    And Selects to remove first and last listed locations
    Then App should remove locations from user saved locations
    And App should update shown list
