Feature: Data unit type configuration

  # Rule: Allow weather unit type selection

    @default-units-defined
    Scenario: App define default data unit type
      Given User open settings screen
      Then Unit type preference should be set to metric

    @unit-system-changed
    Scenario Outline: User change unit system
      Given Weather data unit system preference is set as "<existing>"
      When User open app settings screen
      And Select unit system as "<selected>"
      Then App should set unit preference as "<selected>"
      Examples:
        | existing | selected |
        | metric   | imperial |
        | imperial | metric   |


