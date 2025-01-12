package jobanalysis.scraping;

import jobanalysis.models.JobOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class JSoupScraper {
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Firefox/89.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Safari/605.1.15",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Edge/91.0.864.59"
    };
    private static final int TIMEOUT = 30000;
    private final Random random = new Random();
    private static final Pattern SALARY_PATTERN = Pattern.compile("\\$?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?(?:k|K)?)\\s*-?\\s*\\$?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?(?:k|K)?)?\\s*(?:per|a|/)?\\s*(?:year|yr|month|mo|hour|hr|annual|annually)?");
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)[+]?\\s*(?:-\\s*\\d+)?\\s*(?:year|yr)s?\\s*(?:of)?\\s*experience", Pattern.CASE_INSENSITIVE);

    static {
        // Set ChromeDriver path here if needed
        // System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
    }

    public List<JobOffer> scrapeJobPortal(String portalName, String url) {
        System.out.println("Starting scrape for " + portalName + " at URL: " + url);
        return switch (portalName.toLowerCase()) {
            case "linkedin" -> scrapeWithSelenium(url, this::parseLinkedInJob);
            case "indeed" -> scrapeWithSelenium(url, this::parseIndeedJob);
            default -> scrapeWithSelenium(url, this::parseGenericJob);
        };
    }

    private List<JobOffer> scrapeWithSelenium(String url, JobParser parser) {
        WebDriver driver = null;
        List<JobOffer> jobs = new ArrayList<>();
        try {
            driver = initializeDriver();
            System.out.println("Loading page: " + url);
            driver.get(url);
            waitAndScroll(driver);

            Document doc = Jsoup.parse(driver.getPageSource());
            List<JobOffer> basicJobs = parser.parseJobs(doc);
            System.out.println("Found " + basicJobs.size() + " jobs, fetching details...");

            for (JobOffer basicJob : basicJobs) {
                try {
                    JobOffer detailedJob = scrapeJobDetails(basicJob, driver);
                    jobs.add(detailedJob);
                    Thread.sleep(1000 + random.nextInt(1000));
                } catch (Exception e) {
                    System.err.println("Error getting details for job: " + basicJob.getTitle());
                    jobs.add(basicJob);
                }
            }
        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return jobs;
    }

    private JobOffer scrapeJobDetails(JobOffer basicJob, WebDriver driver) throws InterruptedException {
        System.out.println("Getting details for: " + basicJob.getTitle());
        driver.get(basicJob.getUrl());
        Thread.sleep(2000);

        Document doc = Jsoup.parse(driver.getPageSource());
        return enhanceJobOffer(basicJob, doc);
    }

    private JobOffer enhanceJobOffer(JobOffer basicJob, Document doc) {
        try {
            String fullDescription = extractFullDescription(doc);
            String requirements = extractRequirements(doc);
            String experienceLevel = extractExperienceLevel(doc, fullDescription);
            String benefits = extractBenefits(doc);
            String companyInfo = extractCompanyInfo(doc);

            return new JobOffer.Builder()
                    .setTitle(basicJob.getTitle())
                    .setCompany(basicJob.getCompany())
                    .setLocation(basicJob.getLocation())
                    .setSalary(basicJob.getSalary())
                    .setDescription(fullDescription)
                    .setRequiredSkills(requirements)
                    .setEmploymentType(basicJob.getEmploymentType())
                    .setExperienceLevel(experienceLevel)
                    .setWorkplaceType(basicJob.getWorkplaceType())
                    .setPostedDate(basicJob.getPostedDate())
                    .setBenefits(benefits)
                    .setCompanyDescription(companyInfo)
                    .setUrl(basicJob.getUrl())
                    .build();
        } catch (Exception e) {
            System.err.println("Error enhancing job offer: " + e.getMessage());
            return basicJob;
        }
    }

    private List<JobOffer> parseLinkedInJob(Document doc) {
        List<JobOffer> jobs = new ArrayList<>();
        Elements jobCards = doc.select(".jobs-search__results-list > li");

        for (Element card : jobCards) {
            try {
                String title = findFirstMatch(card,
                        "h3.base-search-card__title",
                        "h3[class*=title]",
                        ".job-search-card__title"
                );

                String company = findFirstMatch(card,
                        ".base-search-card__subtitle",
                        ".company-name",
                        ".job-search-card__subtitle",
                        "[data-test-id=company-name]"
                );

                String location = findFirstMatch(card,
                        ".job-search-card__location",
                        ".base-search-card__metadata",
                        "[data-test-id=location]"
                );

                String salary = findFirstMatch(card,
                        ".job-search-card__salary-info",
                        "span[class*=salary]",
                        "div[class*=compensation]"
                );

                String url = card.select("a.base-card__full-link").attr("href");

                if (!title.isEmpty() && !company.isEmpty()) {
                    jobs.add(createBasicJobOffer(title, company, location, salary, url));
                }
            } catch (Exception e) {
                System.err.println("Error parsing LinkedIn job: " + e.getMessage());
            }
        }
        return jobs;
    }

    private List<JobOffer> parseIndeedJob(Document doc) {
        List<JobOffer> jobs = new ArrayList<>();
        Elements jobCards = doc.select("div.job_seen_beacon, div.jobsearch-ResultsList > div");

        for (Element card : jobCards) {
            try {
                String title = findFirstMatch(card,
                        "h2.jobTitle span[title]",
                        "h2.jobTitle",
                        "a.jcs-JobTitle"
                );

                String company = findFirstMatch(card,
                        "span.companyName",
                        "a.companyName",
                        "[data-testid=company-name]"
                );

                String location = findFirstMatch(card,
                        ".companyLocation",
                        "div[class*=location]"
                );

                String salary = findFirstMatch(card,
                        ".salary-snippet",
                        ".estimated-salary",
                        "div[class*=salary]"
                );

                String jobUrl = card.select("a[id^=job_], a[data-jk]").attr("href");
                if (!jobUrl.startsWith("http")) {
                    jobUrl = "https://indeed.com" + jobUrl;
                }

                if (!title.isEmpty() && !company.isEmpty()) {
                    jobs.add(createBasicJobOffer(title, company, location, salary, jobUrl));
                }
            } catch (Exception e) {
                System.err.println("Error parsing Indeed job: " + e.getMessage());
            }
        }
        return jobs;
    }

    private List<JobOffer> parseGenericJob(Document doc) {
        List<JobOffer> jobs = new ArrayList<>();
        Elements jobCards = doc.select("div[class*=job], div[class*=vacancy], article");

        for (Element card : jobCards) {
            try {
                String title = findFirstMatch(card,
                        "h1,h2,h3,h4",
                        "[class*=title]",
                        "a[class*=job]"
                );

                String company = findFirstMatch(card,
                        "[class*=company]",
                        "[class*=employer]",
                        "[class*=organization]"
                );

                String location = findFirstMatch(card,
                        "[class*=location]",
                        "[class*=address]",
                        "[class*=city]"
                );

                String salary = findFirstMatch(card,
                        "[class*=salary]",
                        "[class*=compensation]",
                        "*:contains($)"
                );

                String jobUrl = findUrl(card, "a");

                if (!title.isEmpty() || !company.isEmpty()) {
                    jobs.add(createBasicJobOffer(title, company, location, salary, jobUrl));
                }
            } catch (Exception e) {
                System.err.println("Error parsing generic job: " + e.getMessage());
            }
        }
        return jobs;
    }

    private WebDriver initializeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--disable-gpu",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--remote-allow-origins=*",
                "--window-size=1920,1080"
        );
        return new ChromeDriver(options);
    }

    private void waitAndScroll(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Thread.sleep(5000);

            long lastHeight = (long) js.executeScript("return document.documentElement.scrollHeight");
            int noChangeCount = 0;

            while (noChangeCount < 3) {
                js.executeScript("window.scrollTo(0, document.documentElement.scrollHeight)");
                Thread.sleep(3000);

                long newHeight = (long) js.executeScript("return document.documentElement.scrollHeight");
                if (newHeight == lastHeight) {
                    noChangeCount++;
                } else {
                    noChangeCount = 0;
                    lastHeight = newHeight;
                }

                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String findFirstMatch(Element element, String... selectors) {
        for (String selector : selectors) {
            try {
                Elements found = element.select(selector);
                if (!found.isEmpty()) {
                    String text = found.first().text().trim();
                    if (!text.isEmpty() && !text.matches("^[\\s*]+$")) {
                        return text;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error with selector " + selector + ": " + e.getMessage());
            }
        }
        return "";
    }

    private String extractFullDescription(Document doc) {
        return findFirstMatch(doc,
                "#job-details",
                ".description__text",
                "#jobDescriptionText",
                "[class*=description]",
                ".content"
        );
    }

    private String extractRequirements(Document doc) {
        return findFirstMatch(doc,
                ".description__text ul",
                "#jobDetailsSection",
                "[class*=requirements]",
                "[class*=qualifications]"
        );
    }

    private String extractExperienceLevel(Document doc, String description) {
        String experienceText = findFirstMatch(doc,
                "li:contains(Seniority level)",
                "div[class*=experience-level]",
                "span[class*=experience]"
        );

        if (experienceText.isEmpty() && description != null) {
            Matcher matcher = EXPERIENCE_PATTERN.matcher(description);
            if (matcher.find()) {
                int years = Integer.parseInt(matcher.group(1));
                return categorizeExperience(years);
            }
        }
        return experienceText;
    }

    private String extractBenefits(Document doc) {
        return findFirstMatch(doc,
                "[class*=benefits]",
                "div:contains(Benefits)",
                "section:contains(Benefits)"
        );
    }

    private String extractCompanyInfo(Document doc) {
        return findFirstMatch(doc,
                ".company-description",
                "[class*=about-company]",
                "#companyDetails"
        );
    }

    private String categorizeExperience(int years) {
        if (years <= 1) return "Entry Level (0-1 years)";
        if (years <= 3) return "Junior (1-3 years)";
        if (years <= 5) return "Mid-Level (3-5 years)";
        if (years <= 8) return "Senior (5-8 years)";
        return "Expert (8+ years)";
    }

    private JobOffer createBasicJobOffer(String title, String company, String location, String salary, String url) {
        return new JobOffer.Builder()
                .setTitle(cleanText(title))
                .setCompany(cleanText(company))
                .setLocation(cleanText(location))
                .setSalary(cleanText(salary))
                .setUrl(url)
                .build();
    }

    private String findUrl(Element element, String selector) {
        Element link = element.select(selector).first();
        return link != null ? link.attr("href") : "";
    }

    private String cleanText(String text) {
        return text != null ? text.replaceAll("[\\s\\u00A0]+", " ").trim() : "";
    }

    @FunctionalInterface
    private interface JobParser {
        List<JobOffer> parseJobs(Document doc);
    }

    public List<JobOffer> scrapeMultiplePages(String baseUrl, int numberOfPages) {
        List<JobOffer> allJobs = new ArrayList<>();
        for (int page = 1; page <= numberOfPages; page++) {
            System.out.println("Scraping page " + page + " of " + numberOfPages);
            String pageUrl = baseUrl + (baseUrl.contains("?") ? "&" : "?") + "start=" + ((page - 1) * 10);
            allJobs.addAll(scrapeJobPortal("generic", pageUrl));

            // Add random delay between pages to avoid blocking
            try {
                Thread.sleep(1500 + random.nextInt(2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return allJobs;
    }

    private Document getDocument(String url) throws IOException {
        int maxRetries = 3;
        int currentTry = 0;
        int delay = 2000; // Initial delay in milliseconds

        while (currentTry < maxRetries) {
            try {
                return Jsoup.connect(url)
                        .userAgent(USER_AGENTS[random.nextInt(USER_AGENTS.length)])
                        .timeout(TIMEOUT)
                        .get();
            } catch (IOException e) {
                currentTry++;
                if (currentTry == maxRetries) {
                    throw e;
                }
                try {
                    // Exponential backoff
                    Thread.sleep(delay * (long) Math.pow(2, currentTry - 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry", e);
                }
            }
        }
        throw new IOException("Failed after " + maxRetries + " retries");
    }

    private String normalizeUrl(String url, String baseUrl) {
        if (url.startsWith("http")) {
            return url;
        } else if (url.startsWith("/")) {
            try {
                return new java.net.URL(new java.net.URL(baseUrl), url).toString();
            } catch (Exception e) {
                return baseUrl + url;
            }
        }
        return baseUrl + "/" + url;
    }

    private String extractPostedDate(Document doc) {
        String dateText = findFirstMatch(doc,
                "time[datetime]",
                "span[class*=posted]",
                "div[class*=posted]",
                "span[class*=date]"
        );

        if (!dateText.isEmpty()) {
            return dateText;
        }

        // Try to find date attribute
        Element timeElement = doc.select("time[datetime]").first();
        return timeElement != null ? timeElement.attr("datetime") : "";
    }

    private String parseSalaryRange(String salaryText) {
        if (salaryText == null || salaryText.isEmpty()) {
            return "";
        }

        Matcher matcher = SALARY_PATTERN.matcher(salaryText);
        if (matcher.find()) {
            String min = matcher.group(1);
            String max = matcher.group(2);

            if (min != null && max != null) {
                return min + " - " + max + " per year";
            } else if (min != null) {
                return min + "+ per year";
            }
        }
        return salaryText;
    }
}