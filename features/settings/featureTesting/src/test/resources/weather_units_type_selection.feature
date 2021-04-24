Feature: Weather unit type configuration

  # Rule: Allow user to select weather data units type

  @unit-changed
  Scenario Outline: User change unit type preference
    Given Weather data unit system preference has not been changed
    When User open app settings screen
    Then Unit "<unit_type>" should be set as default "<default>"
    When User select unit "<unit_type>" as new selection "<selection>"
    Then App should set unit preference "<unit_type>" as selected "<selection>"
    And Update weather data to selected unit "<unit_type>" as selected "<selection>"
    Examples:
      | unit_type   | default | selection |
      | temperature | metric  | imperial  |
      | wind speed  | metric  | imperial  |
      | time format | 24      | 12        |

