import java.util.*;
import java.io.*;

public class semiSpatialCluster {
    //input filename, number of features, cluster filename, r, output filename
    // ../../Raw_Data/input_data160.txt 12 ../../../SpatialEnsemble/data/output0.txt 2 ../../../SpatialEnsemble/data/output0.txt 10 6
    public static void main(String[] args) {
        /*String input_file_path = args[0];
        int n_feature = Integer.parseInt(args[1]);
        String cluster_file_path = args[2];
        int r = Integer.parseInt(args[3]);
        String output_file_path = args[4];
        int k = Integer.parseInt(args[5]); // number of nearest neighbor
        int num_patches = Integer.parseInt(args[6]); // maximal number of patches */
    	
    	//several internal parameters to remember
    	//1. in cluster.KNNAmbiguity: 1.5*k or 5*k for minimum number of samples per patch to consider
    	//2. in Bipartite Ensemble whether to write footprints and file names
    	//3. in Neighborgraph HMergeLazy whether to write clusters and file names
        
    	//parameter settings below
        String input_file_path = "data/Experiment/Chanhassen/input.texture.txt";//"Data/toyExample/toy.input.txt";
        String outputDir = "data/Experiment/Chanhassen";// "data/Chanhassen/";
        
       
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
        
        /*
        //create clusters
        ArrayList<Point> points = Point.ReadPointFile(input_file_path, n_feature);
        System.out.println("finish reading points!");
        Clusters cs = new Clusters();
        cs.InitializeSinglePointCluster(points);
        NeighborGraph ng = new NeighborGraph(cs, nr, nc, r);
        ng.HMergeFaster(np, minPatchSize, step, outputDir);

        
        //cs.ReadFromOutputFile(cluster_file_path, points);
        //NeighborGraph ng = new NeighborGraph(cs, graph_file_path);
        
        BipartiteEnsemble be2 = new BipartiteEnsemble(ng, num_Zone, k, alpha, outputDir+File.separator+"p100."); */
        
        //LocalModel.WriteTrainTestFiles(input_file_path, n_feature, outputDir+File.separator+"ref.txt", outputDir+File.separator+"p100.footprints.4.txt", outputDir + File.separator + "p100.f4.");
        //LocalModel.LocalLearningTree(outputDir+File.separator+"p100.f4.train.1.csv", outputDir+File.separator+"p100.f4.test.1.csv", n_feature, "SVM", "Bagging", null);
        double[][] res = LocalModel.SpatialEnsembleLearning(input_file_path, n_feature, 
    			outputDir+File.separator+"ref.txt", outputDir+File.separator+"p100.footprints.4.txt",outputDir + File.separator + "p100.footprints.4.", "J48", "Boosting", null);
        
        LocalModel.PrintConfusionMatrix(res);
    }
}
