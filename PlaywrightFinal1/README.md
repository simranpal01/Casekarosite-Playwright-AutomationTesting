# Mobile Cover Search Automation with Cucumber BDD

This project implements automated testing for mobile cover search functionality on CaseKaro website using Java, Playwright, and Cucumber BDD framework.

## Project Structure

```
src/
├── test/
│   ├── java/
│   │   ├── runners/
│   │   │   └── TestRunner.java          # Cucumber test runner
│   │   ├── stepDefinitions/
│   │   │   └── MobileCoverSteps.java    # Step definitions for BDD scenarios
│   │   └── Playwrights1/
│   │       └── Myassignament1.java      # Original non-BDD test
│   └── resources/
│       └── features/
│           └── mobile_cover_search.feature  # BDD feature file
```

## Test Steps Covered

1. Navigate to CaseKaro website
2. Click on Mobile covers
3. Search for "Apple" and validate only Apple brand results
4. Click on Apple brand
5. Search for "iPhone 16 Pro" model
6. Apply "In stock" filter
7. Extract product details from 2 pages including:
   - Price after discount
   - Actual price
   - Item description
   - Image link
8. Store and print data in ascending order of discounted price

## Technologies Used

- **Java 11** - Programming language
- **Playwright** - Web automation framework
- **Cucumber** - BDD framework
- **JUnit 5** - Testing framework
- **Maven** - Build tool

## Dependencies

- playwright: 1.40.0
- cucumber-java: 7.15.0
- cucumber-junit-platform-engine: 7.15.0
- junit-jupiter: 5.10.0
- junit-platform-suite: 1.10.0

## Running Tests

### Prerequisites
1. Java 11 or higher
2. Maven 3.6 or higher
3. Internet connection for downloading browser binaries

### Execute BDD Tests

1. **Run all Cucumber tests:**
   ```bash
   mvn test
   ```

2. **Run specific test runner:**
   ```bash
   mvn test -Dtest=TestRunner
   ```

3. **Run original non-BDD test:**
   ```bash
   mvn test -Dtest=Myassignament1
   ```

### Test Reports

- Console output shows detailed step execution
- Test results are available in `target/surefire-reports/`
- Browser trace files are saved as `trace.zip`

## BDD Feature File

The feature file (`mobile_cover_search.feature`) contains:
- **Feature**: Mobile Cover Search and Data Extraction
- **Background**: Common setup steps
- **Scenario**: Complete search and data extraction workflow

## Step Definitions

The step definitions (`MobileCoverSteps.java`) include:
- Browser setup and teardown with `@Before` and `@After` hooks
- Page object interactions using Playwright
- Data extraction and validation logic
- Assertions for test validation

## Key Features

1. **No try-catch blocks** - As per requirement
2. **Comprehensive assertions** - Validates each step
3. **Clean code structure** - Separated concerns
4. **BDD approach** - Human-readable scenarios
5. **Browser tracing** - For debugging support

## Notes

- Tests run in non-headless mode for visibility
- Automatic browser binary downloads handled by Playwright
- Product sorting by discounted price implemented
- Negative validation for non-Apple brands included
- Support for multiple pages of product data extraction

## Troubleshooting

1. **Browser not found**: Run `mvn exec:java -e -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"`
2. **Test failures**: Check browser compatibility and internet connection
3. **Missing dependencies**: Run `mvn clean install` to refresh dependencies
