package jobanalysis.scraping;

import jobanalysis.models.JobOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JSoupScraper {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";
    private static final int TIMEOUT = 10000;
    private final Random random = new Random();

    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Safari/605.1.15",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Firefox/89.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Chrome/91.0.4472.114 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Edge/91.0.864.59"
    };

    public List<JobOffer> scrapeJobPortal(String portalName, String url) {
        System.out.println("Scraping " + portalName + " with URL: " + url);
        return switch (portalName.toLowerCase()) {
            case "linkedin" -> scrapeLinkedIn(url);
            case "indeed" -> scrapeIndeed(url);
            default -> scrapeGeneric(url);
        };
    }

    public List<JobOffer> scrapeIndeed(String url) {
        List<JobOffer> jobs = new ArrayList<>();
        try {
            System.out.println("Scraping Indeed URL: " + url);
            Document doc = getDocument(url);

            Elements jobCards = doc.select(String.join(",",
                    "div.job_seen_beacon",
                    "div.jobsearch-ResultsList > div",
                    "div[class*=job-card]",
                    "div[class*=tapItem]",
                    "div[data-jk]",
                    "td.resultContent",
                    "div[class*=job_seen_beacon]",
                    "div[class*=desktop-job-card]"
            ));

            System.out.println("Found " + jobCards.size() + " Indeed job cards");

            for (Element card : jobCards) {
                try {
                    String title = findFirstNonEmpty(card,
                            ".jobTitle",
                            "h2.title",
                            "a[data-jk]",
                            "span[title]",
                            "a[id^=job_]",
                            "h2[class*=title]",
                            "h2[class*=jobTitle]",
                            "a[class*=jobtitle]"
                    );

                    String company = findFirstNonEmpty(card,
                            ".companyName",
                            "span.company",
                            "[data-company-name]",
                            "div[class*=company]",
                            "span[class*=companyName]",
                            "a[data-tn-element='companyName']"
                    );

                    String location = findFirstNonEmpty(card,
                            ".companyLocation",
                            "div[class*=location]",
                            ".location",
                            "span[class*=location]"
                    );

                    String salary = findFirstNonEmpty(card,
                            ".salary-snippet",
                            ".estimated-salary",
                            "div[class*=salary]",
                            "span[class*=salary]",
                            ".salaryText"
                    );

                    String description = findFirstNonEmpty(card,
                            ".job-snippet",
                            ".summary",
                            "div[class*=description]",
                            "li[class*=description]",
                            ".jobDescriptionText",
                            "div[class*=snippet]"
                    );

                    // Extract job URL
                    String jobUrl = "";
                    Element linkElement = card.select("a[id^=job_], a[data-jk], a[class*=title], a[href*=viewjob], a[href*=jobs]").first();
                    if (linkElement != null) {
                        jobUrl = linkElement.attr("href");
                        if (!jobUrl.startsWith("http")) {
                            jobUrl = "https://indeed.com" + jobUrl;
                        }
                    }

                    // Extract additional details
                    String employmentType = findFirstNonEmpty(card,
                            "[class*=jobTypes]",
                            "[class*=employmentType]",
                            "[class*=job-type]"
                    );

                    String postedDate = findFirstNonEmpty(card,
                            ".date",
                            "span[class*=date]",
                            "div[class*=posted]"
                    );

                    System.out.printf("Found Indeed job: %s at %s%n", title, company);

                    if (!title.isEmpty() && !company.isEmpty()) {
                        jobs.add(new JobOffer.Builder()
                                .setTitle(cleanText(title))
                                .setCompany(cleanText(company))
                                .setLocation(cleanText(location))
                                .setSalary(cleanText(salary))
                                .setDescription(cleanText(description))
                                .setUrl(jobUrl)
                                .setEmploymentType(cleanText(employmentType))
                                .setPostedDate(cleanText(postedDate))
                                .build());
                    }
                } catch (Exception e) {
                    System.err.println("Error processing Indeed job card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error scraping Indeed: " + e.getMessage());
            e.printStackTrace();
        }
        return jobs;
    }

    public List<JobOffer> scrapeLinkedIn(String url) {
        List<JobOffer> jobs = new ArrayList<>();
        try {
            System.out.println("Scraping LinkedIn URL: " + url);
            Document doc = getDocument(url);

            Elements jobCards = doc.select(String.join(",",
                    ".jobs-search__results-list > li",
                    "div[class*=job-card]",
                    "div[class*=job-search-card]",
                    "div[class*=job-result-card]",
                    ".jobs-search-results__list-item",
                    "div[class*=base-card]"
            ));

            System.out.println("Found " + jobCards.size() + " LinkedIn job cards");

            for (Element card : jobCards) {
                try {
                    String title = findFirstNonEmpty(card,
                            ".base-search-card__title",
                            ".job-card-list__title",
                            "h3[class*=title]",
                            ".jobs-search-result-item__title",
                            "h3[class*=base-search]"
                    );

                    String company = findFirstNonEmpty(card,
                            ".base-search-card__subtitle",
                            ".job-card-container__company-name",
                            ".job-result-card__subtitle",
                            "h4[class*=company]",
                            "[class*=company-name]",
                            "a[class*=company]"
                    );

                    String location = findFirstNonEmpty(card,
                            ".job-search-card__location",
                            ".job-result-card__location",
                            "[class*=location]",
                            ".job-card-container__metadata-item",
                            "span[class*=location]"
                    );

                    String jobUrl = card.select(String.join(",",
                            "a.base-card__full-link",
                            "a[class*=job-card]",
                            "a[class*=result-card]",
                            "a[href*='/jobs/view/']"
                    )).attr("href");

                    // Get additional details
                    String workplaceType = findFirstNonEmpty(card,
                            "[class*=workplace-type]",
                            "span[class*=remote]"
                    );

                    String employmentType = findFirstNonEmpty(card,
                            ".job-search-card__job-type",
                            "[class*=job-type]",
                            "span[class*=type]"
                    );

                    System.out.printf("Found LinkedIn job: %s at %s%n", title, company);

                    if (!title.isEmpty() || !company.isEmpty()) {
                        jobs.add(new JobOffer.Builder()
                                .setTitle(cleanText(title))
                                .setCompany(cleanText(company))
                                .setLocation(cleanText(location))
                                .setUrl(jobUrl)
                                .setWorkplaceType(cleanText(workplaceType))
                                .setEmploymentType(cleanText(employmentType))
                                .build());
                    }
                } catch (Exception e) {
                    System.err.println("Error processing LinkedIn job card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error scraping LinkedIn: " + e.getMessage());
            e.printStackTrace();
        }
        return jobs;
    }

    public List<JobOffer> scrapeGeneric(String url) {
        List<JobOffer> jobs = new ArrayList<>();
        try {
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            System.out.println("Scraping generic URL: " + url);
            Document doc = getDocument(url);

            Elements jobCards = doc.select(String.join(",",
                    "div[class*=job]",
                    "div[class*=card]",
                    "div[class*=position]",
                    "article",
                    "div[class*=vacancy]",
                    "div[class*=listing]",
                    ".job-search-card",
                    ".job_seen_beacon",
                    "div[data-job-id]",
                    "li[class*=job]"
            ));

            System.out.println("Found " + jobCards.size() + " generic job cards");

            for (Element card : jobCards) {
                try {
                    String title = findFirstNonEmpty(card,
                            "h1,h2,h3,h4",
                            "[class*=title]",
                            "a[class*=job]",
                            "a[class*=position]"
                    );

                    String company = findFirstNonEmpty(card,
                            "[class*=company]",
                            "[class*=employer]",
                            "[class*=organization]"
                    );

                    String location = findFirstNonEmpty(card,
                            "[class*=location]",
                            "[class*=address]",
                            "[class*=city]"
                    );

                    String description = findFirstNonEmpty(card,
                            "[class*=description]",
                            "[class*=summary]",
                            "p"
                    );

                    String jobUrl = card.select("a").attr("href");
                    if (!jobUrl.startsWith("http")) {
                        if (jobUrl.startsWith("/")) {
                            jobUrl = new java.net.URL(new java.net.URL(url), jobUrl).toString();
                        } else {
                            jobUrl = url;
                        }
                    }

                    System.out.printf("Found generic job: %s at %s%n", title, company);

                    if (!title.isEmpty() || !company.isEmpty()) {
                        jobs.add(new JobOffer.Builder()
                                .setTitle(cleanText(title))
                                .setCompany(cleanText(company))
                                .setLocation(cleanText(location))
                                .setDescription(cleanText(description))
                                .setUrl(jobUrl)
                                .build());
                    }
                } catch (Exception e) {
                    System.err.println("Error processing generic job card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error scraping generic site: " + e.getMessage());
            e.printStackTrace();
        }
        return jobs;
    }

    public List<JobOffer> scrapeMultiplePages(String baseUrl, int numberOfPages) {
        List<JobOffer> allJobs = new ArrayList<>();
        for (int page = 1; page <= numberOfPages; page++) {
            System.out.println("Scraping page " + page + " of " + numberOfPages);
            String pageUrl = baseUrl + (baseUrl.contains("?") ? "&" : "?") + "start=" + ((page - 1) * 10);
            allJobs.addAll(scrapeJobPortal("generic", pageUrl));

            // Add random delay between pages
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
        String userAgent = USER_AGENTS[random.nextInt(USER_AGENTS.length)];
        return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(TIMEOUT)
                .get();
    }

    private String findFirstNonEmpty(Element element, String... selectors) {
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
        return text.replaceAll("[\\s\\u00A0]+", " ").trim();
    }
}