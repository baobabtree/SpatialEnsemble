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
	
	public static void main (String[] args){
		LocalLearningTree("data/weka/train.csv", "data/weka/test.csv", 1, null);
	}
}
