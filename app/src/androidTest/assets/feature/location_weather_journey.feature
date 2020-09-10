Feature: Location weather

  Journey scenarios demonstrating user pathways through
  location weather feature functionalities.

  @user-check-weather
  Scenario: User check weather for his location
    Given User launch app from device home
    And Open current location weather data screen
    Then Weather detail is shown for his current location