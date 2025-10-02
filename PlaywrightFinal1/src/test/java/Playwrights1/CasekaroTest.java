package Playwrights1;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.util.*;


public class CasekaroTest {
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
  
  private static int parsePrice(String priceText) {
    if (priceText == null || priceText.isEmpty()) return 0;
    priceText = priceText.replaceAll("[^0-9.]", "");
    if (!priceText.matches("\\d+(\\.\\d+)?")) return 0;
    double price = Double.parseDouble(priceText);
    return (int) Math.round(price);
  }

  @Test
  public void testCasekaroProductScraping() {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
      BrowserContext context = browser.newContext();
      Page page = context.newPage();

      context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
      
      page.navigate("https://casekaro.com/");
      
      
      // Navigate to mobile covers
      page.locator("#HeaderMenu-mobile-covers-69").click();
      page.locator("#search-bar-cover-page").fill("Apple");
      
      // ASSERTION: Verify Apple search results appear (negative validation for other brands)
      page.waitForSelector("a:has-text('Apple')", new Page.WaitForSelectorOptions().setTimeout(5000));
      List<Locator> searchResults = page.locator("div.predictive-search__results a").all();
      for (Locator result : searchResults) {
          String resultText = result.innerText().toLowerCase();
          Assertions.assertTrue(resultText.contains("apple"), 
              "Non-Apple brand found in search results: " + resultText);
      }
      
      // Wait for Apple popup and click
      Page page2 = page.waitForPopup(() -> {
          page.locator("a:has-text('Apple')").click();
      });
      page2.waitForLoadState();
      
      // Search for iPhone 16 Pro
      page2.locator("#search-bar-cover-page").fill("Iphone 16 pro");
      page2.keyboard().press("Enter");
      page2.locator("a[href='https://casekaro.com/collections/iphone-16-pro-back-covers#MainContent']").click();
      
      // ASSERTION: Verify iPhone 16 Pro collection page is loaded
      Assertions.assertTrue(page2.url().contains("iphone-16-pro-back-covers"), 
          "Should navigate to iPhone 16 Pro collection page but URL is: " + page2.url());
      
      // Apply availability filter
      Locator availability = page2.locator("summary:has(span.facets__summary-label:has-text('Availability'))");
      availability.waitFor();
      availability.click();
      page2.getByLabel("Filter:").getByRole(AriaRole.LIST).locator("svg").first().click();
      page2.getByLabel("Availability (1 selected)").click();
      
      // ASSERTION : Verify availability filter is applied
      Locator appliedFilter = page2.locator("span.facets__summary-label:has-text('Availability')");
      Assertions.assertTrue(appliedFilter.isVisible(), 
          "Availability filter should be visible and applied");
      
      // Initialize product list
      List<Product> productList = new ArrayList<>();
      
      // Getting product details from page 1
      scrapeProductsFromCurrentPage(page2, productList);
      
      // Navigate to page 2 and scrape products
      page2.getByLabel("Page 2").click();
      page2.waitForTimeout(3000);
      scrapeProductsFromCurrentPage(page2, productList);
      
      Page page3 = context.newPage();
      page3.navigate("https://casekaro.com/pages/iphone-covers-cases");
      
      page3.locator("#search-bar-cover-page").fill("Iphone 15 pro");
      page3.keyboard().press("Enter");
      page3.locator("a[href='https://casekaro.com/collections/iphone-15-pro-back-covers#MainContent']").click();
      
      Locator availability1 = page3.locator("summary:has(span.facets__summary-label:has-text('Availability'))");
      availability1.waitFor();
      availability1.click();
      page3.getByLabel("Filter:").getByRole(AriaRole.LIST).locator("svg").first().click();
      page3.getByLabel("Availability (1 selected)").click();
      scrapeProductsFromCurrentPage(page3, productList);
   
      // ASSERTION: Verify products are extracted successfully
      Assertions.assertTrue(productList.size() > 0, 
          "No products were extracted. Expected at least 1 product but found: " + productList.size());
      
      // Sort products by discounted price
      productList.sort(Comparator.comparingInt(p -> p.actualPrice));
      
      // Print results
      System.out.println("\n=== PRODUCTS SORTED BY DISCOUNTED PRICE ===");
      for (Product p : productList) {
          System.out.println(p);
          System.out.println("-----------------------------");
      }
      System.out.println("Total Product: " + productList.size());
    }
  }
  private static void scrapeProductsFromCurrentPage(Page page, List<Product> productList) {
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
        Locator actualPriceElement = priceContainer.locator("s.price-item--regular");
        if (actualPriceElement.count() > 0) actualPrice = parsePrice(actualPriceElement.first().innerText());

        Locator discountedPriceElement = priceContainer.locator("span.price-item--sale");
        if (discountedPriceElement.count() > 0) discountedPrice = parsePrice(discountedPriceElement.first().innerText());

        if (discountedPrice == 0) {
          Locator regular = priceContainer.locator("span.price-item");
          if (regular.count() > 0) discountedPrice = parsePrice(regular.first().innerText());
          if (actualPrice == 0) actualPrice = discountedPrice;
        }
        
        if (discountedPrice > 0 && actualPrice == 0) actualPrice = discountedPrice;
      }

      if (!title.isEmpty() && discountedPrice > 0) {
        productList.add(new Product(title, discountedPrice, actualPrice, imageUrl));
      }
    }
  }
}