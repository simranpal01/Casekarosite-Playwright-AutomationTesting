Feature: Mobile Cover Search and Data Extraction
  As a customer
  I want to search for mobile covers on CaseKaro website
  So that I can find iPhone 16 Pro covers with pricing details

  Background:
    Given I navigate to CaseKaro website

  Scenario: Search for iPhone 16 Pro mobile covers and extract product details
    When I click on Mobile covers
    And I search for "Apple" in the search box
    Then I should see only Apple brand results
    When I click on Apple brand
    And I search for "iPhone 16 Pro" model
    And I click on iPhone 16 Pro from search results
    Then I should see iPhone 16 Pro products in the results
    When I apply "In stock" filter
    Then I should see the availability filter is applied
    When I extract product details from 2 pages
    Then I should have product details with price after discount, actual price, item description, and image link
    And I should store all the extracted details
    And I should print all data in ascending order of discounted price
