package jobanalysis.ml;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;
import jobanalysis.models.JobListing;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JobListingClassifier {
    private StanfordCoreNLP pipeline;
    private Properties props;
    private Map<String, Counter<String>> categoryModels;
    private static final String MODEL_PATH = "data/job_models";
    
    public JobListingClassifier() {
        // Initialize the NLP pipeline with necessary annotators
        props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
        
        // Load or initialize category models
        categoryModels = new HashMap<>();
        try {
            loadTrainedModels();
        } catch (Exception e) {
            System.err.println("Warning: Could not load trained models. Falling back to rule-based classification.");
            System.err.println("Error details: " + e.getMessage());
        }
    }
    
 // 1. Method to extract skills from the job description
    private Set<String> extractSkills(CoreDocument doc) {
        Set<String> skills = new HashSet<>();
        
        // Common technical skills keywords
        Set<String> skillKeywords = new HashSet<>(Arrays.asList(
            "java", "python", "c++", "javascript", "html", "css", "react", "angular", 
            "node.js", "vue", "typescript", "php", "ruby", "sql", "nosql", "mongodb", 
            "postgresql", "mysql", "oracle", "aws", "azure", "gcp", "docker", "kubernetes", 
            "jenkins", "git", "ci/cd", "jira", "agile", "scrum", "devops", "machine learning",
            "ai", "tensorflow", "pytorch", "data science", "analytics", "power bi", "tableau",
            "excel", "linux", "windows", "networking", "cybersecurity", "penetration testing",
            "sécurité", "audit", "pentests", "firewall", "soc", "iam", "gdpr", "iso27001",
            "marketing", "social media", "facebook", "instagram", "linkedin", "twitter",
            "communication", "product management", "ux", "ui", "design", "figma", "sketch"
        ));
        
        // Look for skills in the document
        for (CoreSentence sentence : doc.sentences()) {
            // Extract n-grams up to length 3
            for (int i = 0; i < sentence.tokens().size(); i++) {
                // Single word tokens
                String token = sentence.tokens().get(i).word().toLowerCase();
                if (skillKeywords.contains(token)) {
                    skills.add(token);
                }
                
                // Try bigrams
                if (i < sentence.tokens().size() - 1) {
                    String bigram = token + " " + sentence.tokens().get(i + 1).word().toLowerCase();
                    if (isLikelySkill(bigram)) {
                        skills.add(bigram);
                    }
                }
                
                // Try trigrams
                if (i < sentence.tokens().size() - 2) {
                    String trigram = token + " " + 
                                    sentence.tokens().get(i + 1).word().toLowerCase() + " " +
                                    sentence.tokens().get(i + 2).word().toLowerCase();
                    if (isLikelySkill(trigram)) {
                        skills.add(trigram);
                    }
                }
            }
        }
        
        return skills;
    }

    // Helper method for extractSkills
    private boolean isLikelySkill(String text) {
        // Check for common technical phrases
        return text.matches(".*(machine learning|deep learning|data science|business intelligence|" +
                            "data analysis|artificial intelligence|cloud computing|web development|" +
                            "app development|mobile development|full stack|full-stack|front end|" +
                            "front-end|back end|back-end|devops|data engineering|cyber security|" +
                            "network security|penetration testing|pen testing|database administration|" +
                            "data warehouse|software engineering|systems architecture|ui/ux|ui design|" +
                            "product design|product management|social media marketing).*") ||
               // Check for specific technologies and frameworks
               text.matches(".*(react native|node\\.js|angular js|vue js|spring boot|asp\\.net|" +
                            "\\.net core|express js|django|flask|pandas|numpy|scikit-learn|" +
                            "tensorflow|pytorch|power bi|tableau|looker|microsoft azure|" +
                            "amazon web services|google cloud platform|kubernetes|docker|" +
                            "jenkins|github|gitlab|bitbucket|jira|confluence|salesforce|" +
                            "wordpress|seo|sem|google analytics|google ads|facebook ads|" +
                            "instagram ads|linkedin ads|twitter ads).*");
    }

    // 2. Method to identify experience level
    private String identifyExperienceLevel(CoreDocument doc) {
        // Define keyword patterns for different experience levels
        Map<String, Integer> levelScores = new HashMap<>();
        levelScores.put("JUNIOR", 0);
        levelScores.put("MID", 0);
        levelScores.put("SENIOR", 0);
        
        // Indicator terms for different experience levels
        Set<String> juniorTerms = new HashSet<>(Arrays.asList(
            "junior", "entry", "graduate", "débutant", "stagiaire", "apprenti", "intern",
            "entry-level", "entry level", "0-1", "0-2", "1-2", "less than 2"
        ));
        
        Set<String> midTerms = new HashSet<>(Arrays.asList(
            "mid", "intermediate", "confirmed", "confirmé", "2-3", "2-4", "3-5", "3-4",
            "quelques années", "medium", "middle", "mid-level", "mid level"
        ));
        
        Set<String> seniorTerms = new HashSet<>(Arrays.asList(
            "senior", "experienced", "expert", "lead", "principal", "chef", "head",
            "manager", "director", "5+", "5 years", "5 ans", "6+", "7+", "8+", "10+"
        ));
        
        // Scan document for experience indicators
        String docText = doc.text().toLowerCase();
        
        for (String term : juniorTerms) {
            if (docText.contains(term)) {
                levelScores.put("JUNIOR", levelScores.get("JUNIOR") + 1);
            }
        }
        
        for (String term : midTerms) {
            if (docText.contains(term)) {
                levelScores.put("MID", levelScores.get("MID") + 1);
            }
        }
        
        for (String term : seniorTerms) {
            if (docText.contains(term)) {
                levelScores.put("SENIOR", levelScores.get("SENIOR") + 1);
            }
        }
        
        // Find the experience level with the highest score
        String level = levelScores.entrySet()
                                .stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse("UNSPECIFIED");
        
        // If there's a tie or all are zero, return UNSPECIFIED
        if (level.equals("UNSPECIFIED") || levelScores.get(level) == 0) {
            return "UNSPECIFIED";
        }
        
        return level;
    }

    // 3. Method for rule-based job category classification
    private String classifyJobCategoryRuleBased(String title, String description) {
        String content = (title + " " + description).toLowerCase();
        
        // Development/Engineering
        if (content.contains("developer") || content.contains("développeur") || 
            content.contains("engineer") || content.contains("ingénieur") ||
            content.contains("programmer") || content.contains("programmeur") ||
            content.contains("coding") || content.contains("programming") ||
            content.contains("software") || content.contains("full stack") ||
            content.contains("front end") || content.contains("back end") ||
            content.contains("web") || content.contains("mobile") ||
            content.contains("java") || content.contains("python") ||
            content.contains("javascript") || content.contains("angular") ||
            content.contains("react") || content.contains("node") ||
            content.contains("vue") || content.contains(".net") ||
            content.contains("c#") || content.contains("c++") ||
            content.contains("php") || content.contains("ruby") ||
            content.contains("typescript")) {
            return "DEVELOPMENT";
        }
        
        // IT Security
        if (content.contains("security") || content.contains("sécurité") ||
            content.contains("cybersecurity") || content.contains("cyber-security") ||
            content.contains("information security") || content.contains("sécurité informatique") ||
            content.contains("sécurité si") || content.contains("pentesting") ||
            content.contains("penetration testing") || content.contains("ethical hacking") ||
            content.contains("vulnerability") || content.contains("vulnérabilité") ||
            content.contains("soc") || content.contains("security operations") ||
            content.contains("infosec") || content.contains("audit") ||
            content.contains("compliance") || content.contains("iso27001") ||
            content.contains("gdpr") || content.contains("rgpd")) {
            return "SECURITY";
        }
        
        // Data Science/Analytics
        if (content.contains("data scientist") || content.contains("data science") ||
            content.contains("machine learning") || content.contains("ml") ||
            content.contains("artificial intelligence") || content.contains("ai") ||
            content.contains("deep learning") || content.contains("nlp") ||
            content.contains("natural language processing") ||
            content.contains("data mining") || content.contains("statistics") ||
            content.contains("statistiques") || content.contains("r programming") ||
            content.contains("pandas") || content.contains("numpy") ||
            content.contains("tensorflow") || content.contains("pytorch") ||
            content.contains("scikit-learn") || content.contains("big data") ||
            content.contains("data engineer") || content.contains("data engineering") ||
            content.contains("hadoop") || content.contains("spark") ||
            content.contains("data analyst") || content.contains("analyste de données") ||
            content.contains("data analysis") || content.contains("analytics")) {
            return "DATA";
        }
        
        // Networking
        if (content.contains("network") || content.contains("réseau") ||
            content.contains("technicien réseau") || content.contains("network technician") ||
            content.contains("network engineer") || content.contains("ingénieur réseau") ||
            content.contains("cisco") || content.contains("juniper") ||
            content.contains("routing") || content.contains("switching") ||
            content.contains("firewall") || content.contains("vpn") ||
            content.contains("lan") || content.contains("wan") ||
            content.contains("wifi") || content.contains("wi-fi") ||
            content.contains("tcp/ip") || content.contains("network administration") ||
            content.contains("network infrastructure") ||
            content.contains("infrastructure réseau") ||
            content.contains("network architecture")) {
            return "NETWORKING";
        }
        
        // Business/Management
        if (content.contains("business analyst") || content.contains("analyste métier") ||
            content.contains("product owner") || content.contains("scrum master") ||
            content.contains("project manager") || content.contains("chef de projet") ||
            content.contains("product manager") || content.contains("program manager") ||
            content.contains("director") || content.contains("directeur") ||
            content.contains("chief") || content.contains("cto") ||
            content.contains("cio") || content.contains("it manager") ||
            content.contains("team lead") || content.contains("leadership") ||
            content.contains("management") || content.contains("gestion") ||
            content.contains("agile") || content.contains("transformation") ||
            content.contains("strategy") || content.contains("stratégie") ||
            content.contains("governance") || content.contains("gouvernance")) {
            return "BUSINESS";
        }
        
        // Sales/Marketing/Commercial
        if (content.contains("sales") || content.contains("commercial") ||
            content.contains("marketing") || content.contains("communic") ||
            content.contains("community manager") || content.contains("social media") ||
            content.contains("réseaux sociaux") || content.contains("seo") ||
            content.contains("sem") || content.contains("advertising") ||
            content.contains("publicité") || content.contains("growth") ||
            content.contains("account manager") || content.contains("account executive") ||
            content.contains("business development") ||
            content.contains("développement commercial") ||
            content.contains("sales representative") ||
            content.contains("représentant commercial") ||
            content.contains("digital marketing") ||
            content.contains("marketing digital") ||
            content.contains("brand") || content.contains("marque") ||
            content.contains("pr") || content.contains("public relations") ||
            content.contains("content") || content.contains("contenu")) {
            return "COMMERCIAL";
        }
        
        // Support/Operations
        if (content.contains("support") || content.contains("helpdesk") ||
            content.contains("help desk") || content.contains("service desk") ||
            content.contains("technical support") || content.contains("support technique") ||
            content.contains("it support") || content.contains("desktop support") ||
            content.contains("systems administrator") || content.contains("administrateur système") ||
            content.contains("admin") || content.contains("operations") ||
            content.contains("devops") || content.contains("sre") ||
            content.contains("site reliability") || content.contains("infrastructure") ||
            content.contains("cloud") || content.contains("aws") ||
            content.contains("azure") || content.contains("gcp") ||
            content.contains("google cloud") || content.contains("platform") ||
            content.contains("kubernetes") || content.contains("docker") ||
            content.contains("virtualization") || content.contains("virtualisation") ||
            content.contains("vmware")) {
            return "OPERATIONS";
        }
        
        // Default
        return "OTHER";
    }

    // 4. Method for sentiment analysis
    private double analyzeSentiment(CoreDocument doc) {
        double totalScore = 0.0;
        int sentenceCount = 0;
        
        // Process each sentence
        for (CoreSentence sentence : doc.sentences()) {
            // Get the sentiment annotation
            String sentiment = sentence.sentiment();
            
            // Convert sentiment string to numeric score
            double sentimentScore = 0.0;
            switch (sentiment) {
                case "VERY_NEGATIVE":
                    sentimentScore = -2.0;
                    break;
                case "NEGATIVE":
                    sentimentScore = -1.0;
                    break;
                case "NEUTRAL":
                    sentimentScore = 0.0;
                    break;
                case "POSITIVE":
                    sentimentScore = 1.0;
                    break;
                case "VERY_POSITIVE":
                    sentimentScore = 2.0;
                    break;
                default:
                    continue; // Skip sentences without sentiment
            }
            
            totalScore += sentimentScore;
            sentenceCount++;
        }
        
        // Calculate average sentiment score
        if (sentenceCount > 0) {
            return totalScore / sentenceCount;
        } else {
            return 0.0;
        }
    }
    
    public void trainOnDocument(String category, String document) {
        // Create or get counter for category
        Counter<String> categoryModel = categoryModels.computeIfAbsent(
            category, k -> new ClassicCounter<>());
        
        // Tokenize and process document
        CoreDocument doc = new CoreDocument(document);
        pipeline.annotate(doc);
        
        // Extract features and update model
        for (CoreSentence sentence : doc.sentences()) {
            for (CoreLabel token : sentence.tokens()) {
                String word = token.word().toLowerCase();
                if (isRelevantWord(word, token)) {
                    categoryModel.incrementCount(word);
                }
                
                // Add bigram features
                if (token.index() < sentence.tokens().size() - 1) {
                    CoreLabel nextToken = sentence.tokens().get(token.index() + 1);
                    String bigram = word + " " + nextToken.word().toLowerCase();
                    if (isRelevantBigram(bigram)) {
                        categoryModel.incrementCount(bigram);
                    }
                }
            }
        }
        
        // Save updated models
        try {
            saveModels();
        } catch (IOException e) {
            System.err.println("Warning: Could not save models: " + e.getMessage());
        }
    }
    
    private List<JobListing> readCleanedData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File cleanedFile = new File("data/cleaned_job_listings.json");
        
        if (!cleanedFile.exists()) {
            throw new FileNotFoundException("Could not find cleaned job listings file at: " + 
                cleanedFile.getAbsolutePath());
        }
        
        try {
            // Read the JSON file into a List of JobListing objects
            List<JobListing> jobListings = mapper.readValue(cleanedFile, 
                new TypeReference<List<JobListing>>() {});
                
            System.out.println("Successfully read " + jobListings.size() + 
                " job listings from cleaned data file");
                
            return jobListings;
            
        } catch (IOException e) {
            System.err.println("Error reading cleaned job listings file: " + e.getMessage());
            throw e;
        }
    }
    
    private boolean isRelevantWord(String word, CoreLabel token) {
        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
        // Keep nouns, verbs, and adjectives that aren't stopwords
        return (pos.startsWith("NN") || pos.startsWith("VB") || pos.startsWith("JJ")) 
               && !isStopWord(word);
    }
    
    private boolean isRelevantBigram(String bigram) {
        // Keep technical terms and skill-related phrases
        return bigram.matches(".*(developer|engineer|programmer|analyst|specialist|expert|senior|junior).*") ||
               bigram.matches(".*(java|python|javascript|angular|react|node|aws|azure).*");
    }
    
    private boolean isStopWord(String word) {
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "with", "by", "of", "about", "under", "above", "is", "are", "was", "were"
        ));
        return stopWords.contains(word);
    }
    
    private void saveModels() throws IOException {
        File modelDir = new File(MODEL_PATH);
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }
        
        // Save each category model
        for (Map.Entry<String, Counter<String>> entry : categoryModels.entrySet()) {
            String filename = MODEL_PATH + "/" + entry.getKey() + ".model";
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(filename))) {
                oos.writeObject(entry.getValue());
            }
        }
    }
    
    private void loadTrainedModels() throws IOException, ClassNotFoundException {
        File modelDir = new File(MODEL_PATH);
        if (!modelDir.exists() || !modelDir.isDirectory()) {
            throw new IOException("Model directory not found");
        }
        
        // Load each category model
        for (File file : modelDir.listFiles((dir, name) -> name.endsWith(".model"))) {
            String category = file.getName().replace(".model", "");
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(file))) {
                @SuppressWarnings("unchecked")
                Counter<String> model = (Counter<String>) ois.readObject();
                categoryModels.put(category, model);
            }
        }
    }
    
    public Map<String, Object> analyzeJobListing(String title, String description) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Create annotation object
        CoreDocument doc = new CoreDocument(description);
        pipeline.annotate(doc);
        
        // Extract key skills and requirements
        Set<String> skills = extractSkills(doc);
        analysis.put("skills", skills);
        
        // Identify experience level
        String experienceLevel = identifyExperienceLevel(doc);
        analysis.put("experienceLevel", experienceLevel);
        
        // Use trained models if available, otherwise fall back to rule-based
        if (!categoryModels.isEmpty()) {
            Map<String, Double> scores = classifyWithTrainedModels(title + " " + description);
            String category = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Other");
                
            analysis.put("category", category);
            analysis.put("categoryConfidence", scores);
            analysis.put("classificationMethod", "ML-based");
        } else {
            String category = classifyJobCategoryRuleBased(title, description);
            analysis.put("category", category);
            analysis.put("classificationMethod", "Rule-based");
        }
        
        // Sentiment analysis of job requirements
        double sentimentScore = analyzeSentiment(doc);
        analysis.put("requirementComplexity", sentimentScore);
        
        return analysis;
    }
    
    private Map<String, Double> classifyWithTrainedModels(String text) {
        // Process the input text
        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);
        
        // Create feature vector from input
        Counter<String> features = new ClassicCounter<>();
        for (CoreSentence sentence : doc.sentences()) {
            for (CoreLabel token : sentence.tokens()) {
                String word = token.word().toLowerCase();
                if (isRelevantWord(word, token)) {
                    features.incrementCount(word);
                }
                
                // Add bigram features
                if (token.index() < sentence.tokens().size() - 1) {
                    CoreLabel nextToken = sentence.tokens().get(token.index() + 1);
                    String bigram = word + " " + nextToken.word().toLowerCase();
                    if (isRelevantBigram(bigram)) {
                        features.incrementCount(bigram);
                    }
                }
            }
        }
        
        // Calculate similarity scores with each category
        Map<String, Double> scores = new HashMap<>();
        for (Map.Entry<String, Counter<String>> entry : categoryModels.entrySet()) {
            double similarity = calculateCosineSimilarity(features, entry.getValue());
            scores.put(entry.getKey(), similarity);
        }
        
        // Normalize scores
        double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum > 0) {
            scores.replaceAll((k, v) -> v / sum);
        }
        
        return scores;
    }
    
    private double calculateCosineSimilarity(Counter<String> features1, Counter<String> features2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        // Calculate dot product and norms
        Set<String> allFeatures = new HashSet<>();
        allFeatures.addAll(features1.keySet());
        allFeatures.addAll(features2.keySet());
        
        for (String feature : allFeatures) {
            double val1 = features1.getCount(feature);
            double val2 = features2.getCount(feature);
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }
        
        // Return cosine similarity
        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // [Rest of your existing methods remain the same]
    // extractSkills(), isLikelySkill(), identifyExperienceLevel(),
    // classifyJobCategoryRuleBased(), and analyzeSentiment() methods stay unchanged
}