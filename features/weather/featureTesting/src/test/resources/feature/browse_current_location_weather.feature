Feature: Browse weather data for current user location

  #Rule: Show latest available weather for current location

  @latest-weather-shown
  Scenario Outline: Latest weather shown for user location
    Given App has "<cached_weather>" weather for last user location
    And User location since last app usage has been "<location_change>"
    And Weather update for user location is "<update_available>"
    When User open current weather screen
    Then App should "<show_cached>" weather
    And App should "<update_weather>" cached and shown weather
    Examples:
      | cached_weather | location_change | update_available | show_cached | update_weather |
      | not cached     | changed         | available        | not show    | update         |
      | cached         | changed         | available        | show        | update         |
      | cached         | not changed     | available        | show        | update         |
      | cached         | not changed     | not available    | show        | not update     |

  #Rule: Update weather upon unit pref change

  @weather-units-changed
  Scenario Outline: Weather units preference changed
    Given Unit "<unit_type>" preference is set as "<current>"
    And App has no cached weather
    When User open weather screen
    Then Weather data unit of type "<unit_type>" is shown according to "<current>" preference
    When Unit "<unit_type>" preference is changed to "<changed>"
    Then Shown weather data of type "<unit_type>" should be updated to "<changed>" type
    Examples:
      | unit_type    | current    | changed  |
      | temperature  | metric     | imperial |
      | wind speed   | metric     | imperial |
      | time format  | 24 hour    | 12 hour  |

  #Rule: Provide error handling for current weather browsing

  @latest-update-fail
  Scenario Outline: Weather update fail
    Given App has cached location weather data
    And Weather update for user location is available
    And Existing error "<error_type>" that fail update
    When User open current weather screen
    Then App should show cached weather
    And App should provide "<error_recovery>"
    Examples:
      | error_type               | error_recovery                       |
      | app location permission  | ask user for location permission     |
      | device location services | ask user to enable location services |
      | device network           | notify network connection error      |
      | remote server            | notify remote server error           |