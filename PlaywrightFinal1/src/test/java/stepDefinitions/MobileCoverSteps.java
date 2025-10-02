package stepDefinitions;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MobileCoverSteps {
    
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private Page page1;
    private List<Product> productList;
    
    // Product class to store product details
    static class Product {
        String title, imageUrl;
        int discountedPrice, actualPrice;

        public Product(String title, int discountedPrice, int actualPrice, String imageUrl) {
            this.title = title;
            this.discountedPrice = discountedPrice;
            this.actualPrice = actualPrice;
            this.imageUrl = imageUrl;
        }

        @Override
        public String toString() {
            return "Title: " + title +
                    "\nDiscounted Price: ₹" + discountedPrice +
                    "\nActual Price: ₹" + actualPrice +
                    "\nImage URL: " + imageUrl;
        }
    }

    @Before
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
        productList = new ArrayList<>();
        
        // Start tracing for debugging
        context.tracing().start(new Tracing.StartOptions()
            .setScreenshots(true)
            .setSnapshots(true)
            .setSources(true));
    }

    @After
    public void tearDown() {
        context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("trace.zip")));
        browser.close();
        playwright.close();
    }

    @Given("I navigate to CaseKaro website")
    public void i_navigate_to_casekaro_website() {
        page.navigate("https://casekaro.com/");
    }

    @When("I click on Mobile covers")
    public void i_click_on_mobile_covers() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Mobile Covers")).click();
    }

    @When("I search for {string} in the search box")
    public void i_search_for_in_the_search_box(String searchTerm) {
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search")).click();
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search")).fill(searchTerm);
        page.waitForTimeout(2000);
    }

    @Then("I should see only Apple brand results")
    public void i_should_see_only_apple_brand_results() {
        List<Locator> searchResults = page.locator("div.predictive-search__results a").all();
        for (Locator result : searchResults) {
            String resultText = result.innerText().toLowerCase();
            assertTrue(resultText.contains("apple"), "Non-Apple brand found: " + resultText);
        }
    }

    @When("I click on Apple brand")
    public void i_click_on_apple_brand() {
        page1 = page.waitForPopup(() -> page.getByRole(AriaRole.LINK, 
            new Page.GetByRoleOptions().setName("Apple")).click());
    }

    @When("I search for {string} model")
    public void i_search_for_model(String model) {
        page1.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search")).click();
        page1.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search")).fill(model);
        page1.keyboard().press("Enter");
    }

    @When("I click on iPhone 16 Pro from search results")
    public void i_click_on_iphone_16_pro_from_search_results() {
        page1.waitForSelector("text=iPhone 16 Pro").click();
    }

    @Then("I should see iPhone 16 Pro products in the results")
    public void i_should_see_iphone_16_pro_products_in_the_results() {
        assertTrue(page1.content().toLowerCase().contains("iphone 16 pro"), 
            "Search results did not contain expected keyword");
    }

    @When("I apply {string} filter")
    public void i_apply_filter(String filterType) {
        page1.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Availability (0 selected)")).click();
        page1.getByLabel("Filter:").getByRole(AriaRole.LIST).locator("svg").first().click();
        page1.getByLabel("Availability (1 selected)").click();
    }

    @Then("I should see the availability filter is applied")
    public void i_should_see_the_availability_filter_is_applied() {
        assertTrue(page1.getByLabel("Availability (1 selected)").isVisible(), 
            "Availability filter not applied");
    }

    
    
    
    @When("I extract product details from {int} pages")
    public void i_extract_product_details_from_pages(int numberOfPages) {
        // Extract products from page 1
        scrapeProductsFromCurrentPage(page1, productList);
        assertTrue(!productList.isEmpty(), "No products found on Page 1");
        
        // Navigate to page 2 and extract products
        page1.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Page 2")).click();
        page1.waitForSelector("div.card-wrapper", new Page.WaitForSelectorOptions().setTimeout(10000));
        page1.waitForTimeout(2000);
        
        int beforePage2Size = productList.size();
        scrapeProductsFromCurrentPage(page1, productList);
        
        assertTrue(productList.size() > beforePage2Size, "No products added from Page 2");
    }

    @Then("I should have product details with price after discount, actual price, item description, and image link")
    public void i_should_have_product_details_with_all_required_fields() {
        assertFalse(productList.isEmpty(), "No products extracted");
        
        // Verify that products have rrequired fields
        for (Product product : productList) {
            assertNotNull(product.title, "Product title is null");
            assertFalse(product.title.isEmpty(), "Product title is empty");
            assertTrue(product.discountedPrice > 0, "Discounted price should be greater than 0");
            assertTrue(product.actualPrice > 0, "Actual price should be greater than 0");
            assertNotNull(product.imageUrl, "Image URL is null");
        }
    }

    @Then("I should store all the extracted details")
    public void i_should_store_all_the_extracted_details() {
        assertTrue(productList.size() > 0, "No products extracted.");
        System.out.println("Total products extracted: " + productList.size());
    }

    @Then("I should print all data in ascending order of discounted price")
    public void i_should_print_all_data_in_ascending_order_of_discounted_price() {
        // Sort products by discounted price
        productList.sort(Comparator.comparingInt(p -> p.discountedPrice));
        
        System.out.println("\n=== PRODUCTS SORTED BY DISCOUNTED PRICE ===");
        for (Product p : productList) {
            System.out.println(p);
            System.out.println("------------------------");
        }
    }

    private void scrapeProductsFromCurrentPage(Page page, List<Product> productList) {
        List<Locator> productCards = page.locator("div.card-wrapper").all();
        
        for (Locator card : productCards) {
            String title = "", imageUrl = "";
            int discountedPrice = 0, actualPrice = 0;

            Locator titleElement = card.locator("h3.card__heading a, .card__heading");
            if (titleElement.count() > 0) title = titleElement.first().innerText().trim();

            Locator imgElement = card.locator("img.motion-reduce");
            if (imgElement.count() > 0) {
                imageUrl = imgElement.first().getAttribute("src");
                if (imageUrl != null && imageUrl.startsWith("//")) imageUrl = "https:" + imageUrl;
            }

            Locator priceContainer = card.locator("div.price");
            if (priceContainer.count() > 0) {
                // Actual Price
                Locator actualPriceElement = priceContainer.locator("s.price-item--regular");
                if (actualPriceElement.count() > 0) actualPrice = parsePrice(actualPriceElement.first().innerText());

                // Discounted Price
                Locator discountedPriceElement = priceContainer.locator("span.price-item--sale");
                if (discountedPriceElement.count() > 0) discountedPrice = parsePrice(discountedPriceElement.first().innerText());
                
                // Fallback: if sale price not present, use regular price
                if (discountedPrice == 0) {
                    Locator regular = priceContainer.locator("span.price-item");
                    if (regular.count() > 0) discountedPrice = parsePrice(regular.first().innerText());
                    if (actualPrice == 0) actualPrice = discountedPrice;
                }
                
                // Another fallback: if actual price is still zero, copy discounted price
                if (discountedPrice > 0 && actualPrice == 0) actualPrice = discountedPrice;
            }

            if (!title.isEmpty() && discountedPrice > 0) {
                productList.add(new Product(title, discountedPrice, actualPrice, imageUrl));
            }
        }
    }

    private int parsePrice(String priceText) {
        if (priceText == null || priceText.isEmpty()) return 0;
        priceText = priceText.replaceAll("[^0-9.]", "");
        if (!priceText.matches("\\d+(\\.\\d+)?")) return 0;
        double price = Double.parseDouble(priceText);
        return (int) Math.round(price);
    }
}
