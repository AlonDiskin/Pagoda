Feature: Setting app theme as dark

  User journey that exercise his usage of app functionality
  to change app theme as dark.

  Scenario: User enables dark mode
    Given User has not changed default preferences
    And User launch app from device home
    And Open settings screen
    When User enables dark mode
    Then App theme should change to dark
    When User relaunches app from home
    Then App theme should remain dark