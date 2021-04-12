Feature: Weather unit type configuration

  # Rule: Allow user to select weather data unit type

  @unit-selected
  Scenario: User change unit type preference
    Given Weather data unit system preference has not been changed
    When User open app settings screen
    Then Units should be set as metric by default
    When User select unit system as imperial
    Then App should set unit preference as imperial
    And Update weather data to selected unit system



