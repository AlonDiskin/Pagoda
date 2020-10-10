Feature: Data unit type configuration

  # Rule: Allow user to select weather data unit type

    @default-unit-defined
    Scenario: App define default data unit type
      Given User open settings screen
      Then Unit type preference should be set to metric

    @unit-changed
    Scenario Outline: User change unit type
      Given Weather data unit system preference is set as "<existing>"
      When User open app settings screen
      And Select unit system as "<selected>"
      Then App should set unit preference as "<selected>"
      Examples:
        | existing | selected |
        | metric   | imperial |
        | imperial | metric   |


