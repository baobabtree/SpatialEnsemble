import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.CSVLoader;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.*;
public class LocalModel {
	
	public static void LocalLearningTree(String trainFile, String testFile, int fDim, String[] options){
		
		try{
			CSVLoader loader = new CSVLoader();
			loader.setNoHeaderRowPresent(true);
			loader.setSource(new File(trainFile));
			Instances trainIns = loader.getDataSet();
			loader.setSource(new File(testFile));
			Instances testIns = loader.getDataSet();
			
			NumericToNominal nu2no = new NumericToNominal();
			int [] classIdx = new int [1]; 
			classIdx[0] = trainIns.numAttributes() - 1;
			nu2no.setAttributeIndicesArray(classIdx);
			nu2no.setInputFormat(trainIns);
			trainIns = Filter.useFilter(trainIns, nu2no);
			testIns = Filter.useFilter(testIns, nu2no);
			
			if (trainIns.classIndex() == -1 || testIns.classIndex() == -1){
				trainIns.setClassIndex(trainIns.numAttributes()-1);
				testIns.setClassIndex(testIns.numAttributes()-1);
			}
			
			//add model selection
			J48 tree = new J48();
			if (options != null) tree.setOptions(options);
			tree.buildClassifier(trainIns);
			Evaluation eval = new Evaluation(trainIns);
			eval.evaluateModel(tree, testIns);
			System.out.print(eval.toClassDetailsString());
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally{
			
		}
	}
	
	
    public void WriteTrainTestFiles(String inputPointFile, int fDim, String refFile, String footprintFile, String outputCSVStem){
    	//read input points
    	ArrayList<Point> points = Point.ReadPointFile(inputPointFile, fDim);
    	ArrayList<Integer> classes = ReadClasses(refFile);
    	HashMap<Integer,HashSet<Integer>> footprint = ReadFootprint(footprintFile);
    	for(Integer fid : footprint.keySet()){
    		//TBD
    	}

    }
    
    public static HashMap<Integer,HashSet<Integer>> ReadFootprint(String footprintFile){
    	//part 1: read class labels of entire map by right order
    	HashMap<Integer,HashSet<Integer>> footprint = new HashMap<Integer,HashSet<Integer>>();
		BufferedReader br = null;
		String line = "";
		
		try {
			br = new BufferedReader(new FileReader(footprintFile));			
			while ((line = br.readLine()) != null) {
				String[] fields = line.split(",");
				int pid = Integer.parseInt(fields[1]);
				int fid = Integer.parseInt(fields[0]);
				if (!footprint.containsKey(fid)){
					footprint.put(fid, new HashSet<Integer>());
				}
				footprint.get(fid).add(pid);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return footprint;
    }
	
    public static ArrayList<Integer> ReadClasses(String refFile){
    	//part 1: read class labels of entire map by right order
    	ArrayList<Integer> classes = new ArrayList<Integer>(10000);
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(refFile));			
			while ((line = br.readLine()) != null) {
				classes.add(Integer.parseInt(line));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return classes;
    }
    
	public static void main (String[] args){
		LocalLearningTree("data/weka/train.csv", "data/weka/test.csv", 1, null);
	}
}
