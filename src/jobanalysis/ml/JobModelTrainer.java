package jobanalysis.ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;
import jobanalysis.models.JobListing;
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
    
 // 1. Method to read cleaned data
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

    // 2. Method to determine job category
    private String determineCategory(JobListing job) {
        String content = (job.getTitle() + " " + job.getDescription()).toLowerCase();
        
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