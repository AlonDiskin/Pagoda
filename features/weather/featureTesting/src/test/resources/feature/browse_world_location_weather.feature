Feature: Browse world location weather

  #Rule: Show latest weather data for world location

  @world-location-weather-shown
  Scenario: Latest weather shown for world location
    Given User select to view world location weather
    When User open weather screen for world location
    Then Weather is shown for selected location