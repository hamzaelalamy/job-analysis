package jobanalysis.scraping;

import jobanalysis.models.JobOffer;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JSoupScraperTest {

    private JSoupScraper scraper;
    @Mock private WebDriver mockDriver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scraper = new JSoupScraper();
    }

    @Test
    void testLinkedInScraping() {
        CompletableFuture<List<JobOffer>> future = CompletableFuture.supplyAsync(() -> {
            return scraper.scrapeJobPortal("linkedin",
                    "https://www.linkedin.com/jobs/search?keywords=dev&location=maroc");
        });

        List<JobOffer> jobs = future.join();
        assertFalse(jobs.isEmpty());
        System.out.println("Found " + jobs.size() + " LinkedIn jobs");

        jobs.forEach(job -> {
            assertNotNull(job.getTitle());
            assertNotNull(job.getCompany());
            System.out.println("Job: " + job.getTitle() + " at " + job.getCompany());
        });
    }

    @Test
    void testLinkedInMultiPageScraping() {
        CompletableFuture<List<JobOffer>> future = CompletableFuture.supplyAsync(() -> {
            return scraper.scrapeMultiplePages(
                    "https://www.linkedin.com/jobs/search?keywords=dev&location=maroc", 2);
        });

        List<JobOffer> jobs = future.join();
        assertNotNull(jobs);
        assertTrue(jobs.size() > 0);
        System.out.println("Found " + jobs.size() + " jobs across 2 pages");
    }

    @Test
    void testEmploiMaScraping() {
        CompletableFuture<List<JobOffer>> future = CompletableFuture.supplyAsync(() -> {
            return scraper.scrapeJobPortal("other",
                    "https://www.emploi.ma/recherche-jobs-maroc/développement?f%5B0%5D=im_field_offre_region%3A58");
        });

        List<JobOffer> jobs = future.join();
        assertFalse(jobs.isEmpty());
        System.out.println("Found " + jobs.size() + " Emploi.ma jobs");

        jobs.forEach(job -> {
            assertNotNull(job.getTitle());
            assertNotNull(job.getCompany());
            System.out.println("Job: " + job.getTitle() + " at " + job.getCompany());
        });
    }

    @AfterEach
    void tearDown() {
        if (mockDriver != null) {
            mockDriver.quit();
        }
    }
}