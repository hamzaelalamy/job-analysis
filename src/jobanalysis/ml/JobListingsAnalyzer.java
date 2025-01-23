package jobanalysis.ml;

import weka.core.*;
import weka.core.converters.*;
import weka.core.stemmers.LovinsStemmer;
import weka.core.stopwords.Rainbow;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;
import weka.filters.unsupervised.instance.*;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import java.util.*;

public class JobListingsAnalyzer {
    private StringToWordVector filter;
    private Instances dataStructure;
    private NaiveBayesMultinomial classifier;

    public JobListingsAnalyzer() throws Exception {
        // Create attributes for our data
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("title", (List<String>)null));  // Job title
        attributes.add(new Attribute("description", (List<String>)null));  // Job description
        attributes.add(new Attribute("required_skills", (List<String>)null));  // Required skills
        
        // Create nominal attribute for job category
        ArrayList<String> jobCategories = new ArrayList<>();
        jobCategories.add("Software Development");
        jobCategories.add("DevOps");
        jobCategories.add("Data Engineering");
        jobCategories.add("Security");
        jobCategories.add("Project Management");
        attributes.add(new Attribute("category", jobCategories));

        // Create dataset structure
        dataStructure = new Instances("JobListings", attributes, 0);
        dataStructure.setClassIndex(3);  // Category is the class

        // Setup text to vector conversion
        filter = new StringToWordVector();
        filter.setAttributeIndices("1,2,3");  // Apply to title, description, and skills
        filter.setWordsToKeep(2000);
        filter.setOutputWordCounts(true);
        filter.setLowerCaseTokens(true);
        filter.setTFTransform(true);
        filter.setIDFTransform(true);
        filter.setStopwordsHandler(new Rainbow());  // Remove common words
        filter.setStemmer(new LovinsStemmer());  // Apply word stemming
    }

    public void extractKeywords(String text) {
        try {
            // Create instance for analysis
            Instance instance = new DenseInstance(4);
            instance.setValue(0, text);
            instance.setDataset(dataStructure);

            // Apply filter to get word frequencies
            Instances filtered = Filter.useFilter(dataStructure, filter);
            
            // Get top keywords based on TF-IDF scores
            Map<String, Double> termFrequencies = new HashMap<>();
            for (int i = 0; i < filtered.numAttributes() - 1; i++) {
                String term = filtered.attribute(i).name();
                double value = instance.value(i);
                if (value > 0) {
                    termFrequencies.put(term, value);
                }
            }

            // Sort and print top keywords
            termFrequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void analyzeTechStack(String description) {
        // Define technology categories
        Map<String, List<String>> techCategories = new HashMap<>();
        techCategories.put("Programming Languages", Arrays.asList(
            "java", "python", "javascript", "typescript", "c#", "scala", "golang"
        ));
        techCategories.put("Frameworks", Arrays.asList(
            "spring", "angular", "react", "vue.js", "node.js", ".net", "django"
        ));
        techCategories.put("Cloud & DevOps", Arrays.asList(
            "aws", "gcp", "azure", "kubernetes", "docker", "jenkins", "gitlab"
        ));
        techCategories.put("Databases", Arrays.asList(
            "sql", "postgresql", "mongodb", "mysql", "oracle", "elasticsearch"
        ));

        // Create instance for text processing
        Instance instance = new DenseInstance(2);
        instance.setValue(0, description.toLowerCase());
        
        // Analyze each category
        techCategories.forEach((category, technologies) -> {
            System.out.println("\n" + category + ":");
            technologies.stream()
                .filter(tech -> description.toLowerCase().contains(tech))
                .forEach(tech -> System.out.println("- " + tech));
        });
    }

    public void classifyJobRole(String title, String description) throws Exception {
        // Create instance
        Instance instance = new DenseInstance(4);
        instance.setValue(0, title);
        instance.setValue(1, description);
        instance.setDataset(dataStructure);

        // Apply filter
        Instances filteredInstance = Filter.useFilter(dataStructure, filter);
        
        // Get prediction
        double prediction = classifier.classifyInstance(instance);
        String category = dataStructure.classAttribute().value((int) prediction);
        
        System.out.println("Predicted job category: " + category);
        
        // Get confidence scores
        double[] distribution = classifier.distributionForInstance(instance);
        for (int i = 0; i < distribution.length; i++) {
            System.out.printf("%s: %.2f%%\n", 
                dataStructure.classAttribute().value(i), 
                distribution[i] * 100);
        }
    }

    public void identifyRequiredSkills(String description) {
        // Define skill categories
        Map<String, List<String>> skillCategories = new HashMap<>();
        skillCategories.put("Technical Skills", Arrays.asList(
            "programming", "development", "testing", "debugging", "api", "database"
        ));
        skillCategories.put("Soft Skills", Arrays.asList(
            "communication", "teamwork", "leadership", "problem solving", "analytical"
        ));
        skillCategories.put("Tools & Methodologies", Arrays.asList(
            "agile", "scrum", "git", "ci/cd", "devops", "testing"
        ));

        // Process and count skills
        String lowerDesc = description.toLowerCase();
        skillCategories.forEach((category, skills) -> {
            System.out.println("\n" + category + ":");
            skills.stream()
                .filter(skill -> lowerDesc.contains(skill))
                .forEach(skill -> {
                    // Count occurrences and get surrounding context
                    int index = lowerDesc.indexOf(skill);
                    String context = lowerDesc.substring(
                        Math.max(0, index - 30),
                        Math.min(lowerDesc.length(), index + skill.length() + 30)
                    );
                    System.out.println("- " + skill + " (Context: ..." + context + "...)");
                });
        });
    }

    public static void main(String[] args) {
        try {
            JobListingsAnalyzer analyzer = new JobListingsAnalyzer();
            
            // Example usage:
            String jobDesc = "Looking for a Senior Java Developer with Spring Boot experience...";
            
            System.out.println("Extracting Keywords:");
            analyzer.extractKeywords(jobDesc);
            
            System.out.println("\nAnalyzing Tech Stack:");
            analyzer.analyzeTechStack(jobDesc);
            
            System.out.println("\nIdentifying Required Skills:");
            analyzer.identifyRequiredSkills(jobDesc);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}