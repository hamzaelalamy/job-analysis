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