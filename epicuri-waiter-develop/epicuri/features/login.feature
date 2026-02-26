Feature: Login feature

    Scenario: As a manager I can log into my app
        When I enter "manager" into "Username"
        Then I enter "password" into "Password"
        Then I enter "1023" into "Restaurant ID"
        Then I press "Log In"
        Then I wait for progress
        Then I wait for the "HubActivity" screen to appear
        Then I see "Print queue not running"
        Then I press "Dismiss"
