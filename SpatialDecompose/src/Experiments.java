import java.util.*;
import java.io.*;

public class Experiments {
	//parameters related to input data
    int n_feature = 8;
    int nr = 221;
    int nc = 374;
    
    //parameters related to homogeneous patch generation
    int np = 100; // number of patches
    int minPatchSize = 30; //remove holes: turned off if minPatchSize is 1
    int step = 100; //option for file writing, every other "step" count
    int r = 1;
    
    //parameters related to footprint (zone) grouping
    int k = 10; // number of nearest neighbor
    double alpha = 0.9;
    int num_Zone = 4; // final number of zones
    
	//experiment 1
	public void Exp1(){
        String pointfile = "data/Experiment/Chanhassen/input.texture.txt";
        String classfile = "data/Experiment/Chanhassen/ref.txt";
        String outputDir = "data/Experiment/Chanhassen/";
      
        n_feature = 8; nr = 221; nc = 374;
        np = 100; minPatchSize = 30; step = 100; r = 1;
        k = 10; alpha = 0.9; num_Zone = 10; 
        
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
        be.BisectSpatialEnsemble(ng, num_Zone, k, alpha, outputDir+File.separator+"p"+np+".");
        
        //spatial ensemble learning
        int m = 4; //number of zones m used! 
        String footprintfile = outputDir+File.separator+"p"+np+".f"+m+".txt";
        String CSVFileStem = outputDir+File.separator+"EXP1.p"+np+".f" + m +"."; //lots of tmp files!
        //configure different model and combination
        double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, "J48", "",null);
        LocalModel.PrintConfusionMatrix(cm);
	}
	
	
	public void Exp2(){
        String pointfile = "data/Experiment/BigStone/input.texture.txt";
        String classfile = "data/Experiment/BigStone/ref.txt";
        String outputDir = "data/Experiment/BigStone/";
      
        n_feature = 8; nr = 718; nc = 830;
        np = 400; minPatchSize = 30; step = 200; r = 1;
        k = 10; alpha = 0.9; num_Zone = 50; 
        
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
        be.BisectSpatialEnsemble(ng, num_Zone, k, alpha, outputDir+File.separator+"p"+np+".");
        
        //spatial ensemble learning
        int m = 4; //number of zones m used! 
        String footprintfile = outputDir+File.separator+"p"+np+".f"+m+".txt";
        String CSVFileStem = outputDir+File.separator+"EXP2.p"+np+".f" + m +"."; //lots of tmp files!
        //configure different model and combination
        double[][] cm = LocalModel.SpatialEnsembleLearning(pointfile, n_feature, classfile, footprintfile, CSVFileStem, "J48", "",null);
        LocalModel.PrintConfusionMatrix(cm);
	}
	
	public void Exp3(){
		
	}
	
}
