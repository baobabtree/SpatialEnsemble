import java.util.*;
import java.io.*;

public class Experiments {
	//parameters related to input data
    int n_feature = 8;
    int nr = 0;
    int nc = 0;
    
    //parameters related to homogeneous patch generation
    int np = 100; //min number of patches for sensitivity
    int n = 100; //fixed number of patches
    int minPatchSize = 30; //remove holes: turned off if minPatchSize is 1
    int step = 100; //option for file writing, every other "step" count
    int r = 1;
    
    //parameters related to footprint (zone) grouping
    int k = 10; // number of nearest neighbor
    int minCls = k + k / 2;
    double alpha = 0.9;
    int num_Zone = 10; // largest number of zones for sensitivity
    int m = 4; //fixed parameter on number of zone
    
	//experiment 1
	public void ExpChanhassen(){
        String pointfile = "data/Experiment/Chanhassen/input.texture.txt";
        String classfile = "data/Experiment/Chanhassen/ref.txt";
        String outputDir = "data/Experiment/Chanhassen";
      
        n_feature = 8; nr = 221; nc = 374;
        minPatchSize = 30; step = 100; r = 1;
        minCls = 15;
        np = 100; n = 100;
        k = 10; alpha = 0.9; num_Zone = 10; m = 6; 
        
        ArrayList<Point> points = Point.ReadPointFile(pointfile, n_feature);

        //generate homogeneous patches
        Clusters cs = new Clusters();
        cs.InitializeSinglePointCluster(points);
        NeighborGraph ng = new NeighborGraph(cs, nr, nc, r);
        ng.timeCount = false; 
        ng.HMergeFaster(np, minPatchSize, step, outputDir);

        //generate zones
        BipartiteEnsemble be = new BipartiteEnsemble();
        be.timeCount = false;
        be.BisectSpatialEnsemble(ng, num_Zone, k, minCls, alpha, outputDir+File.separator+"p"+np+".");
        
        StringBuilder strBld = new StringBuilder();
        String[] models = {"J48", "J48", "J48", "RandomForest"};
        String[] ensembles = {"", "Bagging", "Boosting", ""};
        
        
        //Effect of Number of Zones m
        String resOnMFile = outputDir + File.separator + "EffectOfM.txt";
        strBld.setLength(0);
        for(int mm = 2; mm <= num_Zone; mm++){
        	String footprintfile = outputDir+File.separator+ "p"+np+".f"+mm+".txt"; //already exist from runs above
            String CSVFileStem = outputDir + File.separator + "csvFiles"+File.separator+"EffectOfM.m"+mm +"."; //lots of tmp files!
            //configure different local models 
            for(int i = 0; i < models.length; i++){
            	String model = models[i];
            	String ensemble = ensembles[i];
            	double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, model, ensemble, null);
                strBld.append(mm + "," + LocalModel.ConfusionMatrixToString(cm));
            }
        }
        FileIO.WriteStringToFile(resOnMFile, strBld.toString());
        
        
        
        //Effect of k
        String resOnKFile = outputDir + File.separator + "EffectOfK.txt";
        strBld.setLength(0);
        for(int kk = 5; kk <= 25; kk += 5){
        	be.timeCount = false;
        	String footprintfileStem = outputDir+File.separator+"k"+kk+".";
            be.BisectSpatialEnsemble(ng, num_Zone, kk, minCls, alpha, footprintfileStem);
            String footprintfile = footprintfileStem + "f"+m+".txt";
            String CSVFileStem = outputDir + File.separator + "csvFiles" + File.separator + "EffectOfK.k" + kk + ".";
            //configure different local models
        	for(int i = 0; i < models.length; i ++){
        		String model = models[i];
        		String ensemble = ensembles[i];
        		double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, model, ensemble, null);
                strBld.append(kk + "," + LocalModel.ConfusionMatrixToString(cm));
        	}
        }
        FileIO.WriteStringToFile(resOnKFile, strBld.toString());
        
        
        //Effect of alpha
        String resOnAlphaFile = outputDir + File.separator + "EffectOfAlpha.txt";
        strBld.setLength(0);
        for(double alphaI = 0; alphaI <= 1.001; alphaI += 0.1){
        	be.timeCount = false;
        	String footprintfileStem = outputDir+File.separator+"alpha"+alphaI+".";
            be.BisectSpatialEnsemble(ng, num_Zone, k, minCls, alphaI, footprintfileStem);
            String footprintfile = footprintfileStem + "f"+m+".txt";
            String CSVFileStem = outputDir + File.separator + "csvFiles" + File.separator + "EffectOfAlpha.alpha" + alphaI + ".";
            //configure different local models
        	for(int i = 0; i < models.length; i ++){
        		String model = models[i];
        		String ensemble = ensembles[i];
        		double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, model, ensemble, null);
                strBld.append(Double.toString(alphaI) + "," + LocalModel.ConfusionMatrixToString(cm));
        	}
        }
        FileIO.WriteStringToFile(resOnAlphaFile, strBld.toString());
        
        
        
        /*
        //Effect of number of Patches np
        String resOnNpFile = outputDir + File.separator + "EffectOfNp.txt";
        strBld.setLength(0);
        for(int i = 0; i <= 10; i++){
        	n = np + step * i;
        	String clusterfile = outputDir + File.separator + "cluster." + n + ".txt";
        	String graphfile = outputDir + File.separator + "graph." + n + ".txt";
        	Clusters csI = new Clusters();
        	csI.ReadFromOutputFile(clusterfile, points);
        	NeighborGraph ngI = new NeighborGraph(csI, graphfile);
        	be.timeCount = false;
            be.BisectSpatialEnsemble(ngI, num_Zone, k, minCls, alpha, outputDir+File.separator+"p"+n+".");
            //now footprint files are ready
            String footprintfile = outputDir + File.separator + "p" + n + "." + "f"+m+".txt";
            String CSVFileStem = outputDir + File.separator + "csvFiles" + File.separator + "EffectOfAlpha.Np" + n + ".";
            //configure different local models
        	for(int ii = 0; ii < models.length; ii ++){
        		String model = models[ii];
        		String ensemble = ensembles[ii];
        		double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, model, ensemble, null);
                strBld.append(Double.toString(n) + "," + LocalModel.ConfusionMatrixToString(cm));
        	}
        }
        FileIO.WriteStringToFile(resOnNpFile, strBld.toString());
        */
        
        
        //Effect of Base Classifier
        String[] baseModel = {"J48", "SVM", "NeuralNetwork","LR"};
        String ensemble = "";
        String resOnBaseFile = outputDir + File.separator + "EffectOfBase.txt";
        strBld.setLength(0);
        for(int i = 0; i < baseModel.length; i ++){        	
        	String footprintfile = outputDir+File.separator+"p"+np+".f"+m+".txt";
            String CSVFileStem = outputDir + File.separator + "csvFiles"+File.separator+"EffectOfM.p"+np+".f" + m +"."; //lots of tmp files!
            
            String model = baseModel[i];
        	double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, model, ensemble, null);
            strBld.append(LocalModel.ConfusionMatrixToString(cm));
        }
        FileIO.WriteStringToFile(resOnBaseFile, strBld.toString());
        
	}
	
	
	public void ExpBigStone(){
        String pointfile = "data/Experiment/BigStone/input.texture.txt";
        String classfile = "data/Experiment/BigStone/ref.txt";
        String outputDir = "data/Experiment/BigStone";
      
        n_feature = 8; nr = 718; nc = 830;
        minPatchSize = 30; step = 200; r = 1;
        minCls = 100;
        np = 600; n = 800;
        k = 10; alpha = 0.9; 
        num_Zone = 40; m = 20; 
        
        ArrayList<Point> points = Point.ReadPointFile(pointfile, n_feature);
        
        //generate homogeneous patches
        Clusters cs = new Clusters();
        cs.InitializeSinglePointCluster(points);
        NeighborGraph ng = new NeighborGraph(cs, nr, nc, r);
        ng.timeCount = false; 
        //ng.HMergeFaster(np, minPatchSize, step, outputDir); //used in first run
    	
        String graphfile = outputDir + File.separator + "graph." + n + ".txt";
    	String clusterfile = outputDir + File.separator + "cluster." + n + ".txt";
        cs.ReadFromOutputFile(clusterfile, points);
        ng = new NeighborGraph(cs, graphfile);

        //generate zones
        BipartiteEnsemble be = new BipartiteEnsemble();
        be.timeCount = false;
        be.BisectSpatialEnsemble(ng, num_Zone, k, minCls, alpha, outputDir+File.separator+"p"+np+".");
        
        StringBuilder strBld = new StringBuilder();

        
        //Effect of Number of Zones m
        String[] models = {"J48", "J48", "J48", "RandomForest"};
        String[] ensembles = {"", "Bagging", "Boosting", ""};
        String resOnMFile = outputDir + File.separator + "EffectOfM.txt";
        strBld.setLength(0);
        for(int mm = 2; mm <= num_Zone; mm+=2){
        	String footprintfile = outputDir+File.separator+ "p"+np+".f"+mm+".txt"; //already exist from runs above
            String CSVFileStem = outputDir+File.separator+ "csvFiles" + File.separator + "EffectOfM.m"+mm +"."; //lots of tmp files!
            //configure different local models 
            for(int i = 0; i < models.length; i++){
            	String model = models[i];
            	String ensemble = ensembles[i];
            	double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, model, ensemble, null);
                strBld.append(mm + "," + LocalModel.ConfusionMatrixToString(cm));
            }
        }
        FileIO.WriteStringToFile(resOnMFile, strBld.toString());
        
        
        
        //Effect of k
        String resOnKFile = outputDir + File.separator + "EffectOfK.txt";
        strBld.setLength(0);
        for(int kk = 5; kk <= 25; kk += 5){
        	be.timeCount = false;
        	String footprintfileStem = outputDir+File.separator+"k"+kk+".";
        	cs.ReadFromOutputFile(clusterfile, points);
        	ng = new NeighborGraph(cs, graphfile);
            be.BisectSpatialEnsemble(ng, num_Zone, kk, minCls, alpha, footprintfileStem);
            String footprintfile = footprintfileStem + "f"+m+".txt";
            String CSVFileStem = outputDir + File.separator + "csvFiles" + File.separator + "EffectOfK.k" + kk + ".";
            //configure different local models
        	for(int i = 0; i < models.length; i ++){
        		String model = models[i];
        		String ensemble = ensembles[i];
        		double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, model, ensemble, null);
                strBld.append(kk + "," + LocalModel.ConfusionMatrixToString(cm));
        	}
        }
        FileIO.WriteStringToFile(resOnKFile, strBld.toString());
        
        
        //Effect of alpha
        String resOnAlphaFile = outputDir + File.separator + "EffectOfAlpha.txt";
        strBld.setLength(0);
        for(double alphaI = 0; alphaI <= 1.001; alphaI += 0.1){
        	be.timeCount = false;
        	String footprintfileStem = outputDir+File.separator+"alpha"+alphaI+".";
        	graphfile = outputDir + File.separator + "graph." + n + ".txt";
        	clusterfile = outputDir + File.separator + "cluster." + n + ".txt";
        	cs.ReadFromOutputFile(clusterfile, points);
        	ng = new NeighborGraph(cs, graphfile);
            be.BisectSpatialEnsemble(ng, num_Zone, k, minCls, alphaI, footprintfileStem);
            String footprintfile = footprintfileStem + "f"+m+".txt";
            String CSVFileStem = outputDir + File.separator + "csvFiles" + File.separator +  "EffectOfAlpha.alpha" + alphaI + ".";
            //configure different local models
        	for(int i = 0; i < models.length; i ++){
        		String model = models[i];
        		String ensemble = ensembles[i];
        		double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, model, ensemble, null);
                strBld.append(Double.toString(alphaI) + "," + LocalModel.ConfusionMatrixToString(cm));
        	}
        }
        FileIO.WriteStringToFile(resOnAlphaFile, strBld.toString());
        
        
        
        //Effect of number of Patches np
        String resOnNpFile = outputDir + File.separator + "EffectOfNp.txt";
        strBld.setLength(0);
        for(int i = 0; i < 10; i++){
        	n = np + step * i;
        	clusterfile = outputDir + File.separator + "cluster." + n + ".txt";
        	graphfile = outputDir + File.separator + "graph." + n + ".txt";
        	Clusters csI = new Clusters();
        	csI.ReadFromOutputFile(clusterfile, points);
        	NeighborGraph ngI = new NeighborGraph(csI, graphfile);
        	be.timeCount = false;
            be.BisectSpatialEnsemble(ngI, num_Zone, k, minCls, alpha, outputDir+File.separator+"p"+n+".");
            //now footprint files are ready
            String footprintfile = outputDir + File.separator + "p" + n + "." + "f"+m+".txt";
            String CSVFileStem = outputDir + File.separator + "csvFiles" + File.separator + "EffectOfAlpha.Np" + n + ".";
            //configure different local models
        	for(int ii = 0; ii < models.length; ii ++){
        		String model = models[ii];
        		String ensemble = ensembles[ii];
        		double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, model, ensemble, null);
                strBld.append(Double.toString(n) + "," + LocalModel.ConfusionMatrixToString(cm));
        	}
        }
        FileIO.WriteStringToFile(resOnNpFile, strBld.toString());
        
        
        
        //Effect of Base Classifier
        String[] baseModel = {"J48", "SVM", "NeuralNetwork","LR"};
        String ensemble = "";
        String resOnBaseFile = outputDir + File.separator + "EffectOfBase.txt";
        strBld.setLength(0);
        for(int i = 0; i < baseModel.length; i ++){        	
        	String footprintfile = outputDir+File.separator+"p"+np+".f"+m+".txt";
            String CSVFileStem = outputDir+File.separator+"csvFiles" + File.separator + "EffectOfM.p"+np+".f" + m +"."; //lots of tmp files!
            
            String model = baseModel[i];
        	double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, model, ensemble, null);
            strBld.append(LocalModel.ConfusionMatrixToString(cm));
        }
        FileIO.WriteStringToFile(resOnBaseFile, strBld.toString());
        
	}
	
	
	
	public void ExpChanhassenTimeCount(){
        String pointfile = "data/Experiment/Chanhassen/input.texture.txt";
        String outputDir = "data/Experiment/Chanhassen";
      
        n_feature = 8; nr = 221; nc = 374; k = 10; alpha = 0.9; r = 1; 
        minCls = 15;
        minPatchSize = 1; 
        step = 200; 
        np = 100; n = 100;
        num_Zone = 10; m = 6; 
        
        ArrayList<Point> points = Point.ReadPointFile(pointfile, n_feature);
        
        //time of Homogeneous patch generation: baseline and faster
        Clusters cs = new Clusters();
        cs.InitializeSinglePointCluster(points);
        NeighborGraph ng = new NeighborGraph(cs, nr, nc, r);
        /*ng.timeCount = true; 
        ng.HMergeFaster(np, minPatchSize, step, outputDir);*/
        
        //cs.InitializeSinglePointCluster(points);
        //ng = new NeighborGraph(cs, nr, nc, r);
        //ng.HMergeBaseline(np, minPatchSize, step, outputDir);
        
        //time of Bisect Spatial ensemble
        for(n = np; n <= np + 0 * step; n += step){
        	String clusterfile = outputDir + File.separator + "cluster." + n + ".txt";
        	String graphfile = outputDir + File.separator + "graph." + n + ".txt";
        	String footprintStem = outputDir + File.separator + "p" + n + ".";
        	cs.ReadFromOutputFile(clusterfile, points);
        	ng = new NeighborGraph(cs, graphfile);
        	BipartiteEnsemble be = new BipartiteEnsemble();
        	be.timeCount = true;
        	be.BisectSpatialEnsemble(ng, num_Zone, k, minCls, alpha, footprintStem);
        }	
	}
	

	
	public void ExpBigStoneTimeCount(){
        String pointfile = "data/Experiment/BigStone/input.texture.txt";
        String classfile = "data/Experiment/BigStone/ref.txt";
        String outputDir = "data/Experiment/BigStone/";
      
        n_feature = 8; nr = 718; nc = 830;
        minPatchSize = 30; step = 200; r = 1;
        minCls  = 100;
        np = 800; n = 800;
        k = 10; alpha = 0.9; 
        num_Zone = 50; m = 30; 
        
        ArrayList<Point> points = Point.ReadPointFile(pointfile, n_feature);
        
        //time of Homogeneous patch generation: baseline and faster
        Clusters cs = new Clusters();
        cs.InitializeSinglePointCluster(points);
        NeighborGraph ng = new NeighborGraph(cs, nr, nc, r);
        ng.timeCount = true; 
        /*ng.HMergeFaster(np, minPatchSize, step, outputDir);
        
        cs.InitializeSinglePointCluster(points);
        ng = new NeighborGraph(cs, nr, nc, r);
        ng.HMergeBaseline(np, minPatchSize, step, outputDir);*/
        
        //time of Bisect Spatial ensemble
        for(n = np; n <= np + 0 * step; n += step){
        	String clusterfile = outputDir + File.separator + "cluster." + n + ".txt";
        	String graphfile = outputDir + File.separator + "graph." + n + ".txt";
        	String footprintStem = outputDir + File.separator + "p" + n + ".";
        	cs.ReadFromOutputFile(clusterfile, points);
        	ng = new NeighborGraph(cs, graphfile);
        	BipartiteEnsemble be = new BipartiteEnsemble();
        	be.timeCount = true;
        	be.BisectSpatialEnsemble(ng, num_Zone, k, minCls, alpha, footprintStem);
        }	
	}
	
	public void GlobalLearning(){
		String trainFile = "data/Experiment/Global/ChanhassenTrain.csv";
		String testFile = "data/Experiment/Global/ChanhassenTest.csv";
        String[] models = {"J48", "J48", "J48", "RandomForest"};
        String[] ensembles = {"", "Bagging", "Boosting", ""};
        StringBuilder strBld = new StringBuilder();
        for(int i = 0; i < models.length; i++){
        	String model = models[i];
        	String ensemble = ensembles[i];
        	strBld.append(LocalModel.ConfusionMatrixToString(LocalModel.LocalLearning(trainFile, testFile, model, ensemble, null)));
        }
        FileIO.WriteStringToFile("data/Experiment/Global/globalChanhassen.txt", strBld.toString());
        
        
        
        trainFile = "data/Experiment/Global/BigStoneTrain.csv";
		testFile = "data/Experiment/Global/BigStoneTest.csv";
		strBld.setLength(0);
        for(int i = 0; i < models.length; i++){
        	String model = models[i];
        	String ensemble = ensembles[i];
        	strBld.append(LocalModel.ConfusionMatrixToString(LocalModel.LocalLearning(trainFile, testFile, model, ensemble, null)));
        }
        FileIO.WriteStringToFile("data/Experiment/Global/globalBigStone.txt", strBld.toString());
        
        trainFile = "data/Experiment/Global/ChanhassenTrain.csv";
		testFile = "data/Experiment/Global/ChanhassenTest.csv";
		String[] baseModel = {"J48", "SVM", "NeuralNetwork","LR"};
        strBld.setLength(0);
        for(int i = 0; i < models.length; i++){
        	String model = baseModel[i];
        	String ensemble = "";
        	strBld.append(LocalModel.ConfusionMatrixToString(LocalModel.LocalLearning(trainFile, testFile, model, ensemble, null)));
        }
        FileIO.WriteStringToFile("data/Experiment/Global/globalBaseChanhassen.txt", strBld.toString());
        
        
        trainFile = "data/Experiment/Global/BigStoneTrain.csv";
		testFile = "data/Experiment/Global/BigStoneTest.csv";
        strBld.setLength(0);
        for(int i = 0; i < models.length; i++){
        	String model = baseModel[i];
        	String ensemble = "";
        	strBld.append(LocalModel.ConfusionMatrixToString(LocalModel.LocalLearning(trainFile, testFile, model, ensemble, null)));
        }
        FileIO.WriteStringToFile("data/Experiment/Global/globalBaseBigStone.txt", strBld.toString());
        
	}
	
	public static void main(String[] args){
		Experiments exp = new Experiments();
		//exp.ExpBigStone();
		//exp.GlobalLearning();
		//exp.ExpChanhassen();
		//exp.ExpChanhassenTimeCount();
		exp.ExpBigStoneTimeCount();
	}
	
	
	
}
