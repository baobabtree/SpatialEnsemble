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
        
    	//parameter settings below
        String input_file_path = "../../Experiment/Chanhassen/chanhassen.input.txt";
        String cluster_file_path = "../../Experiment/Chanhassen/output.lazycluster.txt";
        String graph_file_path = "../../Experiment/Chanhassen/output.lazygraph.txt";
        String output_file_path = "../../Experiment/Chanhassen/output.lazyfootprints.txt";
        
        
        
        //almost fixed parameters
        int r = 2;
        int k = 10; // number of nearest neighbor
        int num_patches = 2; // final number of zones
        
        int n_feature = 4;
        int nr = 221;
        int nc = 374;
        int np = 1000; // number of patches in inputs
        
        //create clusters
        ArrayList<Point> points = Point.ReadPointFile(input_file_path, n_feature);
        System.out.println("finish reading points!");
        Clusters cs = new Clusters();
        cs.InitializeSinglePointCluster(points);
        NeighborGraph ng = new NeighborGraph(cs, nr, nc, r);
        ng.HMergeLazy(np);
        ng.cs.WriteToFile(cluster_file_path);
        ng.WriteGraphToFile(graph_file_path);
        
        
        
        //cs.ReadFromOutputFile(cluster_file_path, points);
        
        //NeighborGraph ng = new NeighborGraph(cs, graph_file_path);
        //ng.Print();

        //BipartiteEnsemble be = new BipartiteEnsemble(ng, k, num_patches);
        //be.WriteToFile(output_file_path);
        
        //BipartiteEnsemble be2 = new BipartiteEnsemble(ng, k, num_patches, true);
        //be2.WriteToFileBisect(output_file_path);
        
    }
}
