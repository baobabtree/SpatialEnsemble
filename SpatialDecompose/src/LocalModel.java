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
import weka.classifiers.trees.*;
import weka.classifiers.functions.*;
import weka.classifiers.meta.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.*;
public class LocalModel {
	
	public static double[][] LocalLearningTree(String trainFile, String testFile, int fDim, String modelName, String ensemble, String[] options){
		double [][] confusionMat = new double [2][2]; //hard coded for binary classification now!
		confusionMat[0][0] = confusionMat[0][1] = confusionMat[1][0] = confusionMat[1][1] = 0;
		
		try{
			CSVLoader loader = new CSVLoader();
			loader.setNoHeaderRowPresent(true);
			loader.setSource(new File(trainFile));
			Instances trainIns = loader.getDataSet();
			loader.setSource(new File(testFile));
			Instances testIns = loader.getDataSet();
			
			if (trainIns.classIndex() == -1 || testIns.classIndex() == -1){
				trainIns.setClassIndex(trainIns.numAttributes()-1);
				testIns.setClassIndex(testIns.numAttributes()-1);
			}
			
			
			String[] converterOptions= new String[2];
	        converterOptions[0]="-R";
	        converterOptions[1]=Integer.toString(trainIns.classIndex()+1);  //range of variables to make numeric
	        
	        NumericToNominal converter = new NumericToNominal();
	        converter.setOptions(converterOptions);
			converter.setInputFormat(trainIns);
			trainIns = Filter.useFilter(trainIns, converter);
			converter.setInputFormat(testIns);
			testIns = Filter.useFilter(testIns, converter);
			
			
			if (trainIns.classAttribute().numValues() == 1){
				//unary class in this training set
				Enumeration<Instance> e = testIns.enumerateInstances();
				int PredClass = Integer.parseInt(trainIns.classAttribute().value(0)) - 1;
				while (e.hasMoreElements()){
					Instance ins = e.nextElement();
					int TrueClass = Integer.parseInt(testIns.classAttribute().value((int)ins.classValue())) -1;
					confusionMat[TrueClass][PredClass] ++;
				}
				
			}
			else{
				Evaluation eval = new Evaluation(trainIns);
				
				Classifier model = null;
				//add model selection
				switch (modelName) {
				case "J48": {
					model = new J48(); 
					if (options != null) ((J48) model).setOptions(options);  
					break;  
				}
				case "NeuralNetwork": {
					model = new MultilayerPerceptron();
					if (options != null) ((MultilayerPerceptron) model).setOptions(options);  
					break;
				}
				case "SVM": {
					model = new SMO();
					if (options != null) ((SMO) model).setOptions(options); 
					break;
				}
				case "RandomForest": {
					model = new RandomForest();
					if (options != null) ((RandomForest) model).setOptions(options);  
					break;
				}
				default :
				}
				
				model.buildClassifier(trainIns); 
				eval.evaluateModel(model, testIns);
				
				if (ensemble == ""){
					model.buildClassifier(trainIns);
					eval.evaluateModel(model, testIns);
				}
				else if (ensemble == "Bagging"){
					Bagging bg = new Bagging();
					bg.setClassifier(model);
					bg.buildClassifier(trainIns);
					eval.evaluateModel(model, trainIns);
				}
				else if (ensemble == "Boosting"){
					AdaBoostM1 bs = new AdaBoostM1();
					bs.setClassifier(model);
					bs.buildClassifier(trainIns);
					eval.evaluateModel(model, trainIns);
				}
				
				confusionMat = eval.confusionMatrix();
				
				//System.out.print(eval.toClassDetailsString());
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally{
			
		}
		
		//System.out.println(confusionMat[0][0] + "," + confusionMat[0][1] + "," + confusionMat[1][0] + "," + confusionMat[1][1]);

		return confusionMat;
	}
	
	
    public static void WriteTrainTestFiles(String inputPointFile, int fDim, String refFile, String footprintFile, String outputCSVStem){
    	//read input points
    	ArrayList<Point> points = Point.ReadPointFile(inputPointFile, fDim); //row id is pid
    	ArrayList<Integer> classes = ReadClasses(refFile); //row id is pid
    	HashMap<Integer,HashSet<Integer>> footprint = ReadFootprint(footprintFile); //"fid,pid" in each line
    	for(Integer fid : footprint.keySet()){
    		//write train, test file for footprint fid
    		String trainFile = outputCSVStem + "train." + Integer.toString(fid) + ".csv";
    		String testFile = outputCSVStem + "test." + Integer.toString(fid) + ".csv";
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(trainFile));
                for (Integer pid : footprint.get(fid)) {
                    if(points.get(pid).label == 0) continue;
                    for(int f = 0; f < points.get(pid).fDim; f++){
                    	bw.write(Double.toString(points.get(pid).features[f]) + ",");
                    }
                    bw.write(Integer.toString(points.get(pid).label) + "\n");
                }
                bw.flush();
                bw = new BufferedWriter(new FileWriter(testFile));
                for (Integer pid : footprint.get(fid)) {
                    if(points.get(pid).label > 0) continue;
                    for(int f = 0; f < points.get(pid).fDim; f++){
                    	bw.write(Double.toString(points.get(pid).features[f]) + ",");
                    }
                    bw.write(Integer.toString(classes.get(pid)) + "\n");
                }
                bw.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
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
    
}
