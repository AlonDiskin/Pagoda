Feature: Current location weather

  #Rule: Provide latest weather data for user current location

  @latest-weather-shown
  Scenario Outline: Latest location weather data shown
    Given User open current weather screen
    Then Weather detail of user current location is shown
    When Change happen in "<change>"
    And User refresh weather data
    Then Latest updated data is shown
    Examples:
      | change    |
      | location  |
      | weather   |

  @weather-browsing-error
  Scenario Outline: Weather data browsing error handled
    Given Existing error "<error_type>"
    When User open current weather screen to browse weather
    Then App should handle error accordingly "<error_handling>"
    Examples:
      | error_type               | error_handling                      |
      | app location permission  | prompt user for location permission |
      | device location sensor   | prompt user for location setting    |
      | device network           | prompt user to connect to network   |
      | remote server            | prompt user to retry later          |

  #Rule: update data upon unit pref change

  @weather-units-changed
  Scenario Outline: Weather units preference changed
    Given Units preference is set as "<current>"
    And User open current weather screen
    Then Weather data units is shown according to "<current>" preference
    When User change unit system preference to "<selected>"
    Then Shown weather data should be updated to "<selected>" units
    Examples:
      | current    | selected |
      | metric     | imperial |
      | imperial   | metric   |

