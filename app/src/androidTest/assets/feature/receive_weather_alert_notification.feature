Feature: Receive weather alerts notification

  User journey that exercise his usage of app functionality
  to receive weather alerts notification for his current location

  Scenario: User receive weather alert notification
    Given User launch app from device home
    And Open settings screen
    And Enable weather alert notification
    When Weather alert for user location is received
    Then App should show alert info in a status bar notification