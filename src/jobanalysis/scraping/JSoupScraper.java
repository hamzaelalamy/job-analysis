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
    private static final int MAX_RETRIES = 3;
    private static final int BASE_DELAY = 2000;
    private final Random random = new Random();

    private static final Pattern SALARY_PATTERN = Pattern.compile(
            "\\$?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?(?:k|K)?)\\s*-?\\s*" +
                    "\\$?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?(?:k|K)?)?\\s*" +
                    "(?:per|a|/)?\\s*(?:year|yr|month|mo|hour|hr|annual|annually)?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)[+]?\\s*(?:-\\s*\\d+)?\\s*(?:year|yr)s?\\s*(?:of)?\\s*experience",
            Pattern.CASE_INSENSITIVE
    );

    @FunctionalInterface
    private interface JobParser {
        List<JobOffer> parseJobs(Document doc);
    }

    // Public methods
    public List<JobOffer> scrapeJobPortal(String portalName, String url) {
        System.out.println("Starting scrape for " + portalName + " at URL: " + url);
        return switch (portalName.toLowerCase()) {
            case "linkedin" -> scrapeWithSelenium(url, this::parseLinkedInJob);
            case "indeed" -> scrapeWithSelenium(url, this::parseIndeedJob);
            default -> scrapeWithSelenium(url, this::parseGenericJob);
        };

    }

    public List<JobOffer> scrapeMultiplePages(String baseUrl, int numberOfPages) {
        List<JobOffer> allJobs = new ArrayList<>();
        for (int page = 1; page <= numberOfPages; page++) {
            try {
                System.out.println("Scraping page " + page + " of " + numberOfPages);
                String pageUrl = constructPageUrl(baseUrl, page);
                List<JobOffer> pageJobs = scrapeJobPortal("generic", pageUrl);
                allJobs.addAll(pageJobs);
                Thread.sleep(BASE_DELAY + random.nextInt(2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error scraping page " + page + ": " + e.getMessage());
            }
        }
        return allJobs;
    }

    // Core scraping functionality
    private List<JobOffer> scrapeWithSelenium(String url, JobParser parser) {
        WebDriver driver = null;
        List<JobOffer> jobs = new ArrayList<>();

        try {
            driver = initializeDriver();
            System.out.println("Loading page: " + url);
            driver.get(url);
            waitAndScroll(driver);

            String pageSource = driver.getPageSource();
            System.out.println("Page source length: " + pageSource.length());
            Document doc = Jsoup.parse(pageSource);

            List<JobOffer> basicJobs = parser.parseJobs(doc);
            System.out.println("Found " + basicJobs.size() + " jobs, fetching details...");

            for (JobOffer basicJob : basicJobs) {
                try {
                    JobOffer detailedJob = scrapeJobDetails(basicJob, driver);
                    jobs.add(detailedJob);
                    Thread.sleep(BASE_DELAY + random.nextInt(1000));
                } catch (Exception e) {
                    System.err.println("Error getting details for job: " + basicJob.getTitle());
                    System.err.println("Error: " + e.getMessage());
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

    private String constructPageUrl(String baseUrl, int page) {
        if (baseUrl.contains("page=")) {
            return baseUrl.replaceAll("page=\\d+", "page=" + page);
        } else if (baseUrl.contains("p=")) {
            return baseUrl.replaceAll("p=\\d+", "p=" + page);
        } else {
            String separator = baseUrl.contains("?") ? "&" : "?";
            return baseUrl + separator + "start=" + ((page - 1) * 10);
        }
    }
    // Job details scraping
    private JobOffer scrapeJobDetails(JobOffer basicJob, WebDriver driver) throws InterruptedException {
        System.out.println("Getting details for: " + basicJob.getTitle());

        try {
            driver.get(basicJob.getUrl());
            Thread.sleep(2000);

            Document doc = Jsoup.parse(driver.getPageSource());
            return enhanceJobOffer(basicJob, doc);
        } catch (Exception e) {
            System.err.println("Error scraping job details: " + e.getMessage());
            throw e;
        }
    }

    private JobOffer enhanceJobOffer(JobOffer basicJob, Document doc) {
        try {
            String fullDescription = extractFullDescription(doc);
            String requirements = extractRequirements(doc);
            String experienceLevel = extractExperienceLevel(doc, fullDescription);
            String benefits = extractBenefits(doc);
            String companyInfo = extractCompanyInfo(doc);
            String applicationDeadline = extractApplicationDeadline(doc);
            String postedDate = extractPostedDate(doc);
            String salary = parseSalaryRange(basicJob.getSalary());

            return new JobOffer.Builder()
                    .setTitle(basicJob.getTitle())
                    .setCompany(basicJob.getCompany())
                    .setLocation(basicJob.getLocation())
                    .setSalary(salary)
                    .setDescription(fullDescription)
                    .setRequiredSkills(requirements)
                    .setEmploymentType(basicJob.getEmploymentType())
                    .setExperienceLevel(experienceLevel)
                    .setWorkplaceType(basicJob.getWorkplaceType())
                    .setPostedDate(postedDate)
                    .setApplicationDeadline(applicationDeadline)
                    .setBenefits(benefits)
                    .setCompanyDescription(companyInfo)
                    .setUrl(basicJob.getUrl())
                    .build();
        } catch (Exception e) {
            System.err.println("Error enhancing job offer: " + e.getMessage());
            return basicJob;
        }
    }

    // Data extraction methods
    private String extractFullDescription(Document doc) {
        return findFirstMatch(doc,
                "#job-details",
                ".description__text",
                "#jobDescriptionText",
                "[class*=description]",
                ".content",
                "[class*=job-details]",
                "[data-automation*=description]"
        );
    }

    private String extractRequirements(Document doc) {
        return findFirstMatch(doc,
                ".description__text ul",
                "#jobDetailsSection",
                "[class*=requirements]",
                "[class*=qualifications]",
                "div:contains(Requirements) + ul",
                "div:contains(Qualifications) + ul",
                "[data-automation*=requirements]"
        );
    }

    private String extractExperienceLevel(Document doc, String description) {
        String experienceText = findFirstMatch(doc,
                "li:contains(Seniority level)",
                "div[class*=experience-level]",
                "span[class*=experience]",
                "[data-automation*=experience]",
                "div:contains(Experience)",
                "div:contains(Years of experience)"
        );

        if (experienceText.isEmpty() && description != null) {
            Matcher matcher = EXPERIENCE_PATTERN.matcher(description);
            if (matcher.find()) {
                try {
                    int years = Integer.parseInt(matcher.group(1));
                    return categorizeExperience(years);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing experience years: " + e.getMessage());
                }
            }
        }
        return experienceText;
    }

    private String extractBenefits(Document doc) {
        return findFirstMatch(doc,
                "[class*=benefits]",
                "div:contains(Benefits)",
                "section:contains(Benefits)",
                "[data-automation*=benefits]",
                "ul:contains(Health Insurance)",
                "div:contains(What we offer)"
        );
    }

    private String extractCompanyInfo(Document doc) {
        return findFirstMatch(doc,
                ".company-description",
                "[class*=about-company]",
                "#companyDetails",
                "[data-automation*=company-info]",
                "div:contains(About the company)",
                "section:contains(About us)"
        );
    }

    private String extractApplicationDeadline(Document doc) {
        return findFirstMatch(doc,
                "[class*=deadline]",
                "[class*=closing-date]",
                "div:contains(Application Deadline)",
                "span:contains(Apply by)",
                "div:contains(Closing Date)"
        );
    }

    private String extractPostedDate(Document doc) {
        String dateText = findFirstMatch(doc,
                "time[datetime]",
                "span[class*=posted]",
                "div[class*=posted]",
                "span[class*=date]",
                "div:contains(Posted on)",
                "div:contains(Date posted)"
        );

        if (dateText.isEmpty()) {
            Element timeElement = doc.select("time[datetime]").first();
            if (timeElement != null) {
                return timeElement.attr("datetime");
            }
        }

        return dateText;
    }

    private String categorizeExperience(int years) {
        if (years <= 1) return "Entry Level (0-1 years)";
        if (years <= 3) return "Junior (1-3 years)";
        if (years <= 5) return "Mid-Level (3-5 years)";
        if (years <= 8) return "Senior (5-8 years)";
        return "Expert (8+ years)";
    }

    // Utility methods
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

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replaceAll("[\\s\\u00A0]+", " ")
                .replaceAll("\\s*[\\r\\n]+\\s*", " ")
                .trim();
    }

    private String normalizeUrl(String url, String baseUri) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        try {
            if (url.startsWith("http")) {
                return url;
            } else if (url.startsWith("//")) {
                return "https:" + url;
            } else if (url.startsWith("/")) {
                java.net.URL base = new java.net.URL(baseUri);
                return new java.net.URL(base.getProtocol(), base.getHost(), url).toString();
            } else {
                return baseUri + (baseUri.endsWith("/") ? "" : "/") + url;
            }
        } catch (Exception e) {
            System.err.println("Error normalizing URL: " + e.getMessage());
            return url;
        }
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
    // LinkedIn specific parser
    private List<JobOffer> parseLinkedInJob(Document doc) {
        List<JobOffer> jobs = new ArrayList<>();
        Elements jobCards = doc.select(".jobs-search__results-list > li");

        System.out.println("Found " + jobCards.size() + " LinkedIn job cards");

        for (Element card : jobCards) {
            try {
                Map<String, String> jobData = new HashMap<>();

                // Core job data
                jobData.put("title", findFirstMatch(card,
                        "h3.base-search-card__title",
                        "h3[class*=title]",
                        ".job-search-card__title",
                        ".base-card__title",
                        ".job-card-list__title"
                ));

                jobData.put("company", findFirstMatch(card,
                        ".base-search-card__subtitle",
                        ".company-name",
                        ".job-search-card__subtitle",
                        "[data-test-id=company-name]",
                        ".base-card__subtitle"
                ));

                jobData.put("location", findFirstMatch(card,
                        ".job-search-card__location",
                        ".base-search-card__metadata",
                        "[data-test-id=location]",
                        ".job-card-container__metadata-item"
                ));

                jobData.put("salary", findFirstMatch(card,
                        ".job-search-card__salary-info",
                        "span[class*=salary]",
                        "div[class*=compensation]",
                        ".compensation-information"
                ));

                // Additional metadata
                jobData.put("employmentType", findFirstMatch(card,
                        ".job-search-card__employment-type",
                        "span[class*=job-type]",
                        ".employment-type"
                ));

                jobData.put("workplaceType", findFirstMatch(card,
                        ".workplace-type",
                        "span[class*=workplace]",
                        ".work-type-information"
                ));

                jobData.put("postedDate", findFirstMatch(card,
                        "time[datetime]",
                        ".job-search-card__listdate",
                        ".posted-time-ago__text"
                ));

                // URL handling
                String url = card.select("a.base-card__full-link").attr("href");
                if (!url.startsWith("http")) {
                    url = "https://www.linkedin.com" + url;
                }
                jobData.put("url", url);

                if (!jobData.get("title").isEmpty() && !jobData.get("company").isEmpty()) {
                    JobOffer job = new JobOffer.Builder()
                            .setTitle(cleanText(jobData.get("title")))
                            .setCompany(cleanText(jobData.get("company")))
                            .setLocation(cleanText(jobData.get("location")))
                            .setSalary(cleanText(jobData.get("salary")))
                            .setEmploymentType(cleanText(jobData.get("employmentType")))
                            .setWorkplaceType(cleanText(jobData.get("workplaceType")))
                            .setPostedDate(cleanText(jobData.get("postedDate")))
                            .setUrl(jobData.get("url"))
                            .build();

                    jobs.add(job);
                    System.out.println("Parsed LinkedIn job: " + jobData.get("title") + " at " + jobData.get("company"));
                }
            } catch (Exception e) {
                System.err.println("Error parsing LinkedIn job: " + e.getMessage());
            }
        }
        return jobs;
    }

    // Indeed specific parser
    private List<JobOffer> parseIndeedJob(Document doc) {
        List<JobOffer> jobs = new ArrayList<>();
        Elements jobCards = doc.select("div.job_seen_beacon, div.jobsearch-ResultsList > div.cardOutline");

        System.out.println("Found " + jobCards.size() + " Indeed job cards");

        for (Element card : jobCards) {
            try {
                Map<String, String> jobData = new HashMap<>();

                // Core job data
                jobData.put("title", findFirstMatch(card,
                        "h2.jobTitle span[title]",
                        "h2.jobTitle",
                        "a.jcs-JobTitle",
                        "div[class*=title] span[title]",
                        "td.resultContent a[data-jk]"
                ));

                jobData.put("company", findFirstMatch(card,
                        "span.companyName",
                        "a.companyName",
                        "[data-testid=company-name]",
                        "span[class*=companyName]",
                        "div.company_location > pre"
                ));

                jobData.put("location", findFirstMatch(card,
                        ".companyLocation",
                        "div[class*=location]",
                        ".job-location",
                        "div.company_location > div"
                ));

                jobData.put("salary", findFirstMatch(card,
                        ".salary-snippet",
                        ".estimated-salary",
                        "div[class*=salary]",
                        ".metadata.salary-snippet-container",
                        "div[class*=metadata] > div:contains($)"
                ));

                // Additional metadata
                jobData.put("employmentType", findFirstMatch(card,
                        ".metadata > div:contains(Full-time)",
                        ".metadata > div:contains(Part-time)",
                        ".metadata > div:contains(Contract)",
                        "[class*=jobTypes]"
                ));

                jobData.put("workplaceType", findFirstMatch(card,
                        ".metadata > div:contains(Remote)",
                        ".metadata > div:contains(Hybrid)",
                        ".metadata > div:contains(On-site)",
                        "[class*=workplace]"
                ));

                jobData.put("postedDate", findFirstMatch(card,
                        ".date",
                        "span.date",
                        ".posting-date",
                        "span[class*=posted]"
                ));

                // URL handling
                Element linkElement = card.select("a[id^=job_], a[data-jk], h2.jobTitle a").first();
                String url = "";
                if (linkElement != null) {
                    url = linkElement.attr("href");
                    if (!url.startsWith("http")) {
                        url = "https://www.indeed.com" + url;
                    }
                }
                jobData.put("url", url);

                if (!jobData.get("title").isEmpty() && !jobData.get("company").isEmpty()) {
                    JobOffer job = new JobOffer.Builder()
                            .setTitle(cleanText(jobData.get("title")))
                            .setCompany(cleanText(jobData.get("company")))
                            .setLocation(cleanText(jobData.get("location")))
                            .setSalary(cleanText(jobData.get("salary")))
                            .setEmploymentType(cleanText(jobData.get("employmentType")))
                            .setWorkplaceType(cleanText(jobData.get("workplaceType")))
                            .setPostedDate(cleanText(jobData.get("postedDate")))
                            .setUrl(jobData.get("url"))
                            .build();

                    jobs.add(job);
                    System.out.println("Parsed Indeed job: " + jobData.get("title") + " at " + jobData.get("company"));
                }
            } catch (Exception e) {
                System.err.println("Error parsing Indeed job: " + e.getMessage());
            }
        }
        return jobs;
    }

    // Generic parser
    private List<JobOffer> parseGenericJob(Document doc) {
        List<JobOffer> jobs = new ArrayList<>();
        Elements jobCards = findJobCards(doc);

        System.out.println("Found " + jobCards.size() + " potential job cards");

        for (Element card : jobCards) {
            try {
                Map<String, String> jobData = extractGenericJobData(card, doc.baseUri());

                if (!jobData.get("title").isEmpty() && (!jobData.get("company").isEmpty() || !jobData.get("url").isEmpty())) {
                    JobOffer job = new JobOffer.Builder()
                            .setTitle(cleanText(jobData.get("title")))
                            .setCompany(cleanText(jobData.get("company")))
                            .setLocation(cleanText(jobData.get("location")))
                            .setSalary(cleanText(jobData.get("salary")))
                            .setDescription(cleanText(jobData.get("description")))
                            .setEmploymentType(cleanText(jobData.get("employmentType")))
                            .setWorkplaceType(cleanText(jobData.get("workplaceType")))
                            .setPostedDate(cleanText(jobData.get("postedDate")))
                            .setExperienceLevel(cleanText(jobData.get("experienceLevel")))
                            .setRequiredSkills(cleanText(jobData.get("requiredSkills")))
                            .setBenefits(cleanText(jobData.get("benefits")))
                            .setUrl(jobData.get("url"))
                            .build();

                    jobs.add(job);
                    System.out.println("Found job: " + jobData.get("title") + " at " + jobData.get("company"));
                }
            } catch (Exception e) {
                System.err.println("Error parsing generic job card: " + e.getMessage());
            }
        }
        return jobs;
    }

    private Elements findJobCards(Document doc) {
        Elements cards = new Elements();

        String[] containerSelectors = {
                // Semantic containers
                "article[class*=job], article[class*=position]",
                "div[itemtype*=JobPosting]",
                // Common class patterns
                "div[class*=job-card], div[class*=jobCard]",
                "div[class*=vacancy], div[class*=position]",
                "div[class*=job-listing], div[class*=jobListing]",
                // List items
                "li[class*=job-item], li[class*=jobItem]",
                // Data attributes
                "div[data-test*=job], div[data-automation*=job]",
                "div[data-type=job], div[data-entity-type=job]",
                // Generic job-related classes
                ".job-result, .search-result, .listing-item",
                // Broader patterns as fallback
                "div[class*=job], div[class*=career], div[class*=posting]"
        };

        for (String selector : containerSelectors) {
            try {
                Elements found = doc.select(selector);
                if (!found.isEmpty()) {
                    cards.addAll(found);
                }
            } catch (Exception e) {
                System.err.println("Error with selector " + selector + ": " + e.getMessage());
            }
        }

        return deduplicateCards(cards);
    }

    private Elements deduplicateCards(Elements cards) {
        Set<String> seen = new HashSet<>();
        Elements uniqueCards = new Elements();

        for (Element card : cards) {
            String titleText = card.select("[class*=title]").text();
            String companyText = card.select("[class*=company]").text();
            String signature = (titleText + companyText).trim().toLowerCase();

            if (signature.isEmpty()) {
                signature = card.text().trim().substring(0, Math.min(100, card.text().trim().length()));
            }

            if (seen.add(signature)) {
                uniqueCards.add(card);
            }
        }

        return uniqueCards;
    }

    private Map<String, String> extractGenericJobData(Element card, String baseUri) {
        Map<String, String> data = new HashMap<>();

        // Title
        data.put("title", findFirstMatch(card,
                "h1,h2,h3,h4",
                "[class*=job-title], [class*=jobtitle], [class*=job_title]",
                "[data-test*=title], [data-automation*=title]",
                "[class*=title]:not(html):not(head):not(body)"
        ));

        // Company
        data.put("company", findFirstMatch(card,
                "[class*=company-name], [class*=companyName]",
                "[class*=employer], [class*=organization]",
                "*:contains(Company:), *:contains(Employer:)"
        ));

        // Location
        data.put("location", findFirstMatch(card,
                "[class*=location], [class*=address]",
                "[class*=city], [class*=region]",
                "*:contains(Location:)"
        ));

        // Salary
        String salary = findFirstMatch(card,
                "[class*=salary], [class*=compensation]",
                "*:contains($), *:contains(€), *:contains(£)",
                "*:contains(Salary:)"
        );
        data.put("salary", parseSalaryRange(salary));

        // Other fields
        data.put("description", findFirstMatch(card, "[class*=description]"));
        data.put("employmentType", findFirstMatch(card, "[class*=employment-type], [class*=job-type]"));
        data.put("workplaceType", findFirstMatch(card, "[class*=workplace-type], [class*=work-type]"));
        data.put("postedDate", findFirstMatch(card, "time[datetime], [class*=posted]"));
        data.put("experienceLevel", findFirstMatch(card, "[class*=experience], [class*=seniority]"));
        data.put("requiredSkills", findFirstMatch(card, "[class*=skills], [class*=requirements]"));
        data.put("benefits", findFirstMatch(card, "[class*=benefits], [class*=perks]"));

        // URL handling
        data.put("url", findAndNormalizeUrl(card, baseUri));

        return data;
    }

    private String findAndNormalizeUrl(Element card, String baseUri) {
        Element link = null;

        // Try direct job links first
        link = card.select("a[href*=job], a[href*=career], a[href*=position]").first();

        // Try links containing the job title
        if (link == null) {
            String titleText = card.select("[class*=title]").text();
            for (Element a : card.select("a[href]")) {
                if (a.text().contains(titleText)) {
                    link = a;
                    break;
                }
            }
        }

        // Fallback to any link
        if (link == null) {
            link = card.select("a[href]").first();
        }

        if (link != null) {
            String url = link.attr("abs:href");
            if (url.isEmpty()) {
                url = link.attr("href");
            }
            return normalizeUrl(url, baseUri);
        }

        return "";
    }
}