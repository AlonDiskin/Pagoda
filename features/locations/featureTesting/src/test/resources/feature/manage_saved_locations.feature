Feature: Mange saved world locations

  #Rule: Provide saved location removal option

  @delete-saved-location
  Scenario: User delete saved location
    Given Previously saved locations exist
    When User open saved locations screen
    And Selects to remove first and last listed locations
    Then App should remove locations from user saved locations
    And App should update shown list

  #Rule: Provide world location adding option

  @add-location
  Scenario: User add location
    Given User open saved locations screen
    When User select to add a new saved location
    Then App should open world location search screen