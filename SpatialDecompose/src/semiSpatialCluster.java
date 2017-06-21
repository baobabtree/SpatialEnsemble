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
        String input_file_path = "data/Chanhassen/input.texture.txt";//"Data/toyExample/toy.input.txt";
        String cluster_file_path = "data/Chanhassen/cluster.txt";//"Data/toyExample/toy.cluster.txt";
        String graph_file_path = "data/Chanhassen/graph.txt";//"Data/toyExample/toy.graph.txt";
        String outputDir = "";// "data/Chanhassen/";
        
        //almost fixed parameters
        int r = 1;
        int k = 10; // number of nearest neighbor
        int num_Zone = 10; // final number of zones
        
        int n_feature = 8;
        int nr = 221;
        int nc = 374;
        int np = 100; // number of patches in inputs
        
        //secondary parameter list
        int minPatchSize = 30;
        int step = 100;
        
        
        //create clusters
        ArrayList<Point> points = Point.ReadPointFile(input_file_path, n_feature);
        System.out.println("finish reading points!");
        Clusters cs = new Clusters();
        cs.InitializeSinglePointCluster(points);
        NeighborGraph ng = new NeighborGraph(cs, nr, nc, r);
        ng.HMergeFaster(np, minPatchSize, step, outputDir);
        //ng.cs.WriteToFile(cluster_file_path);
        //ng.WriteGraphToFile(graph_file_path);
        
        //cs.ReadFromOutputFile(cluster_file_path, points);
        //NeighborGraph ng = new NeighborGraph(cs, graph_file_path);
        
        BipartiteEnsemble be2 = new BipartiteEnsemble(ng, k, num_Zone, outputDir);
        //be2.WriteFootprintsToFile(output_file_path);	
        
    }
}
