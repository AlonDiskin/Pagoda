Feature: Current location weather

  #Rule: Provide latest weather data for user current location

  @current-location-weather-shown
  Scenario Outline: Latest weather shown for current location
    Given User open current weather screen
    Then Weather detail of user current location is shown
    When Change happen in "<change>"
    And User refresh weather data
    Then Latest updated data is shown
    Examples:
      | change    |
      | location  |
      | weather   |


  #Rule: update data upon unit pref change

  @weather-units-changed
  Scenario Outline: Weather units preference changed
    Given Unit "<unit_type>" preference is set as "<current>"
    And User open weather screen
    Then Weather data unit of type "<unit_type>" is shown according to "<current>" preference
    When Unit "<unit_type>" preference is changed to "<changed>"
    Then Shown weather data of type "<unit_type>" should be updated to "<changed>" type
    Examples:
      | unit_type    | current    | changed  |
      | temperature  | metric     | imperial |
      | wind speed   | metric     | imperial |
      | time format  | 24 hour    | 12 hour  |

  #Rule: Provide error handling for weather data browsing

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


  #Rule: Show latest weather data for world location

  @world-location-weather-shown
  Scenario: Latest weather shown for world location
    Given Weather screen was opened to show world location weather
    Then Weather detail of user current location is shown

