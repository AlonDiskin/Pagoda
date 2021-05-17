Feature: Browse saved locations

  #Rule: list all saved world  locations

  @locations-listed
  Scenario: Saved locations listed
    Given Previously saved locations exist
    When User open saved locations screen
    Then All saved locations are listed in descending added order
    When User select first listed location
    Then App should show selected location weather data
