Feature: Show weather alert notification

  #Rule: Allow user to enable\disable weather alert notification

  Scenario Outline: User enable\disable weather alert notification
    Given Weather alert notification is currently "<current>"
    When User "<user_enable>" notification
    Then Show "<show_notification>" alert notification in status bar when alert received
    Examples:
      | current  | user_enable | show_notification |
      | disabled | enable      | show              |
      | enabled  | disable     | not show          |

  #Rule: Publish error to app if enabling alert notification fail