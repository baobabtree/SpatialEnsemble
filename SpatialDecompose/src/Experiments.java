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
        String input_file_path = "data/Experiment/Chanhassen/input.texture.txt";
        String outputDir = "data/Experiment/Chanhassen/";
      
        n_feature = 8; nr = 221; nc = 374;
        np = 100; minPatchSize = 30; step = 100; r = 1;
        k = 10; alpha = 0.9; num_Zone = 4; 
        
        ArrayList<Point> points = Point.ReadPointFile(input_file_path, n_feature);

        Clusters cs = new Clusters();
        cs.InitializeSinglePointCluster(points);
        NeighborGraph ng = new NeighborGraph(cs, nr, nc, r);
        ng.HMergeFaster(np, minPatchSize, step, outputDir);

        BipartiteEnsemble be2 = new BipartiteEnsemble(ng, num_Zone, k, alpha, outputDir+File.separator+"p"+np+".");
        
	}
}
