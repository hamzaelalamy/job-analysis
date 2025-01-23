package jobanalysis.ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class JobModelTrainer {
    private StanfordCoreNLP pipeline;
    private Properties props;
    private Map<String, Counter<String>> categoryModels;
    private String modelPath = "data/job_models";
    
    public JobModelTrainer() {
        // Initialize NLP pipeline
        props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
        
        // Initialize category models
        categoryModels = new HashMap<>();
        
        // Create model directory if it doesn't exist
        new File(modelPath).mkdirs();
    }
    
    public void trainModel() throws IOException {
        System.out.println("Starting model training process...");
        
        // Read and process the cleaned job listings
        List<JobListing> jobListings = readCleanedData();
        System.out.println("Loaded " + jobListings.size() + " job listings");
        
        // Train on each job listing
        for (JobListing job : jobListings) {
            String category = determineCategory(job);
            trainOnDocument(category, job);
        }
        
        // Save the trained models
        saveModels();
        System.out.println("Models saved to: " + modelPath);
        
        // Evaluate the model
        evaluateModel(jobListings);
    }
    
    private void trainOnDocument(String category, JobListing job) {
        // Get or create counter for this category
        Counter<String> categoryModel = categoryModels.computeIfAbsent(
            category, k -> new ClassicCounter<>());
        
        // Process job title and description
        String text = job.getTitle() + " " + job.getDescription();
        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);
        
        // Extract features
        for (CoreSentence sentence : doc.sentences()) {
            for (CoreLabel token : sentence.tokens()) {
                String word = token.word().toLowerCase();
                if (isRelevantWord(word, token)) {
                    categoryModel.incrementCount(word);
                }
                
                // Add technical term bigrams
                if (token.index() < sentence.tokens().size() - 1) {
                    CoreLabel nextToken = sentence.tokens().get(token.index() + 1);
                    String bigram = word + " " + nextToken.word().toLowerCase();
                    if (isRelevantBigram(bigram)) {
                        categoryModel.incrementCount(bigram);
                    }
                }
            }
        }
    }
    
    private boolean isRelevantWord(String word, CoreLabel token) {
        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
        // Keep nouns, verbs, and adjectives that aren't stopwords
        return (pos.startsWith("NN") || pos.startsWith("VB") || pos.startsWith("JJ")) 
               && !isStopWord(word);
    }
    
    private boolean isRelevantBigram(String bigram) {
        // Technical terms and important phrases
        return bigram.matches(".*(developer|engineer|programmer|analyst|scientist).*") ||
               bigram.matches(".*(java|python|javascript|angular|react|node|aws|azure).*");
    }
    
    private boolean isStopWord(String word) {
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "with", "by", "of", "about", "under", "above", "is", "are"
        ));
        return stopWords.contains(word);
    }
    
    private void saveModels() throws IOException {
        for (Map.Entry<String, Counter<String>> entry : categoryModels.entrySet()) {
            String filename = modelPath + "/" + entry.getKey() + ".model";
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(filename))) {
                oos.writeObject(entry.getValue());
            }
        }
    }
    
    public Map<String, Counter<String>> loadModels() throws IOException {
        Map<String, Counter<String>> models = new HashMap<>();
        File modelDir = new File(modelPath);
        
        if (modelDir.exists() && modelDir.isDirectory()) {
            for (File file : modelDir.listFiles((dir, name) -> name.endsWith(".model"))) {
                try (ObjectInputStream ois = new ObjectInputStream(
                        new FileInputStream(file))) {
                    String category = file.getName().replace(".model", "");
                    @SuppressWarnings("unchecked")
                    Counter<String> model = (Counter<String>) ois.readObject();
                    models.put(category, model);
                } catch (ClassNotFoundException e) {
                    System.err.println("Error loading model: " + e.getMessage());
                }
            }
        }
        
        return models;
    }
    
    // [Previous methods remain the same: readCleanedData(), determineCategory(), 
    // sanitizeText(), and JobListing inner class]
    
    private void evaluateModel(List<JobListing> testData) {
        System.out.println("\nModel Evaluation:");
        
        // Split data for evaluation (80-20 split)
        int testSize = testData.size() / 5;
        List<JobListing> evaluationData = testData.subList(0, testSize);
        
        int correct = 0;
        int total = 0;
        
        for (JobListing job : evaluationData) {
            String actualCategory = determineCategory(job);
            
            // Create feature vector for this job
            Counter<String> features = extractFeatures(job);
            
            // Find best matching category
            String predictedCategory = findBestCategory(features);
            
            if (actualCategory.equals(predictedCategory)) {
                correct++;
            }
            total++;
        }
        
        double accuracy = (double) correct / total * 100;
        System.out.printf("Accuracy: %.2f%% (%d/%d correct)\n", 
            accuracy, correct, total);
    }
    
    private Counter<String> extractFeatures(JobListing job) {
        Counter<String> features = new ClassicCounter<>();
        String text = job.getTitle() + " " + job.getDescription();
        
        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);
        
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
        
        return features;
    }
    
    private String findBestCategory(Counter<String> features) {
        String bestCategory = "OTHER";
        double bestSimilarity = 0.0;
        
        for (Map.Entry<String, Counter<String>> entry : categoryModels.entrySet()) {
            double similarity = calculateCosineSimilarity(features, entry.getValue());
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestCategory = entry.getKey();
            }
        }
        
        return bestCategory;
    }
    
    private double calculateCosineSimilarity(Counter<String> features1, 
                                           Counter<String> features2) {
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
}