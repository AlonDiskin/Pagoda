Feature: Enabling weather alert notification

  #Rule: Allow user to enable/disable weather alert notification

  Scenario: User enable alert notification
    Given Weather alert notification preference han not been modified
    When User open app settings screen
    Then Alert Notification should be disabled
    When User enable weather alert notification
    Then App should show alert notification upon weather alert