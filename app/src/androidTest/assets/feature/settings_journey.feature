Feature: App settings

  Journey scenarios demonstrating user pathways through
  app settings feature functionalities.

  @user-change-weather-units
  Scenario: User change data units setting
    Given App weather units system is set to metric
    And User launch app from device home
    And Open app settings screen
    And Select unit system as imperial
    When User open weather data screen
    Then Weather data should be shown in imperial units