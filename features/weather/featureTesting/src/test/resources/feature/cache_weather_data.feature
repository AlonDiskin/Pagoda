Feature: Cache and use cached location weather data

  #Rule: Cache current location weather data updates.

  @empty-cache
  Scenario: Cache is empty
    Given App has no cached location weather data
    When User open current weather screen
    Then App should loaded weather data and cache it
    And Weather should be shown with cache update time

  @cache-shown-first
  Scenario: Latest cached data shown before updated
    Given App has cached weather for current user location
    When User open current weather screen
    Then App should show cached weather data with update timestamp
    And Load weather update for current location and cache it
    And App should show it with update timestamp

  @cache-update-fail
  Scenario Outline: Weather cache update fail
    Given App has cached location weather data
    And Existing error "<error_type>" that fail update
    When User open current weather screen
    Then App should show cached weather data with update timestamp
    Examples:
      | error_type               |
      | app location permission  |
      | device location sensor   |
      | device network           |
      | remote server            |
