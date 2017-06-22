import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.lang.Math;

public class BipartiteEnsemble {
	public boolean debug = false;
	public boolean timeCount = true;
	
	public double alpha = 0.90;
	public ArrayList<NeighborGraph> footprints; //use NeighborGraph's to represent all footprints
	
	ArrayList<ClusterPair> pairs;
    HashMap<Integer, HashMap<Integer, Double>> ambi_map;
    HashSet<Integer> footprint1;
    HashSet<Integer> footprint2;
    int footprintSize1 = 0;
    int footprintSize2 = 0;
    
    
    public void BisectSpatialEnsemble(NeighborGraph ng, int m, int k, double alphaVal, String outputFileStem){
    	long sTime = System.nanoTime();
    	StringBuilder strBlder = new StringBuilder();
    	
    	alpha = alphaVal;
    	footprints = new ArrayList<NeighborGraph>();
    	footprints.add(ng);
    	while(footprints.size() < m){
    		//now found most ambiguous footprint
    		double maxAmbi = -1;
    		int maxAmbiI = 0;
    		
    		for(int i = 0; i < footprints.size(); i++){
    			double curAmbi = footprints.get(i).cs.AvgPairwiseKNNAmbiguity(k);
    			//double curAmbi = footprints.get(i).cs.KNNAmbiguity(k);
    			if( curAmbi > maxAmbi ){
    				maxAmbi = curAmbi;
    				maxAmbiI = i;
    			}
    		}
    		
    		if (debug ){
    			System.out.println("Select a zone with max ambiguity =" +maxAmbi);
    		}
    		
    		//bisectOneStep to split most ambiguous footprints
    		NeighborGraph ngMax = footprints.remove(maxAmbiI);
    		boolean success = Bisect(ngMax, k); //results saved in footprint1, footprint2
    		
    		//split footprint graph into two subgraphs, then add to queue
    		ArrayList<NeighborGraph> nglist = ngMax.NeighborGraphBiSplit(footprint1, footprint2);
    		footprints.add(nglist.get(0));
    		footprints.add(nglist.get(1));
    		
    		if (outputFileStem != ""){
    			if (timeCount){
    				long eTime = System.nanoTime();
    				strBlder.append(Integer.toString(footprints.size()) + "," + Long.toString((eTime - sTime)/1000000000) +"\n");
    			}
    			else {
    				String filename = outputFileStem + "f" + footprints.size() + ".txt";
        			this.WriteFootprintsToFile(filename);
    			}
    		}
    		
    		if (!success) break; //cannot break zones any further!
    	}//results saved to footprints list
    	if (timeCount)
    		FileIO.WriteStringToFile(outputFileStem + "SETime.txt", strBlder.toString());
    }
    
    
    //new Bisecting Ensemble method
    public boolean Bisect(NeighborGraph ng, int k){
    	if (!ng.cs.hasBipartiteGraph){
    		ng.cs.GenerateBipartiteGraph(k);
    	}
    	ambi_map = ng.cs.ambi_map;
        ArrayList<Integer> c1ids = ng.cs.c1ids;
        ArrayList<Integer> c2ids = ng.cs.c2ids;
        
        pairs = new ArrayList<ClusterPair>(c1ids.size() * c2ids.size());
        for (Integer c1id : c1ids) {
            for (Integer c2id : c2ids) {
                pairs.add(new ClusterPair(c1id, c2id, ambi_map.get(c1id).get(c2id)));
            }
        }
        Collections.sort(pairs, Collections.reverseOrder());
        
        //debug: print out all pairs
        if (debug){
        	for(ClusterPair pair : pairs){
            	if(pair.amb > 0.05){
            		System.out.println(pair.c1id + "," + pair.c2id + ":" + pair.amb);
            	}
            	else {
            		break;
            	}
            }
        }
        

        footprint1 = new HashSet<Integer>();
        footprint2 = new HashSet<Integer>();
        footprintSize1 = 0;
        footprintSize2 = 0;
        
        //pairs may be empty! particularly when only one label! 
        //In that case, randomly assign a labeled patch
        Iterator<ClusterPair> pairIter = pairs.iterator();
        if (!pairIter.hasNext()){
        	System.out.println("We cannot split the area into more zones! no ambiguity exist within zones!");
        	return false;
        }
        ClusterPair cp = pairIter.next();
        footprint1.add(cp.c1id);
        footprint2.add(cp.c2id);
        footprintSize1 += ng.cs.clusters.get(cp.c1id).points.size();
        footprintSize2 += ng.cs.clusters.get(cp.c2id).points.size();
        
        HashSet<Integer> frontier1 = new HashSet<Integer>(ng.graph.get(cp.c1id).neighborList.keySet()); 
        HashSet<Integer> frontier2 = new HashSet<Integer>(ng.graph.get(cp.c2id).neighborList.keySet()); 
        frontier1.remove(cp.c2id);// make sure frontier does not contain CLOSED nodes in footprints
        frontier2.remove(cp.c1id);
        
        //while froniter not empty, then keep assigning!
        while(!frontier1.isEmpty() || !frontier2.isEmpty()){
        	//heuristic: maximize sup(inter footprint ambiguity), minimize sup(intra footprint ambiguity)
        	//heuristic formula: [1+sup(inter pair)] / [1 + sup(intra pair)]
        	double MaxScore = -1; 
        	int maxNid = -1;
        	int maxFid = 1;
        	
        	for(Integer n1 : frontier1){
        		double supIntra = -1; 
        		double supInter = -1;
        		double curScore = 0;
        		
        		//get size balance score
        		int cSize = ng.cs.clusters.get(n1).points.size(); //size of candidate cluster node
        		double footprintSize1Ratio = (footprintSize1 + cSize + 0.0) / (footprintSize1 + cSize + footprintSize2);
        		double sizeEntropy = -1 * footprintSize1Ratio * Math.log( footprintSize1Ratio ) / Math.log(2)
        				- (1-footprintSize1Ratio ) * Math.log(1- footprintSize1Ratio ) / Math.log(2);    
        		
        		//System.out.println("size entropy " + sizeEntropy);
        		
        		//if no ambiguity for inter or intra, score = (1+0)/(1+0)=1;
        		if( !c1ids.contains(n1) && !c2ids.contains(n1) ){
        			curScore = 1 * alpha + sizeEntropy * (1-alpha); //(0+1)/(0+1)
        			if( curScore > MaxScore ){
        				MaxScore = curScore;
        				maxNid = n1;
        				maxFid = 1;
        			}
        			continue;
        		}
        		
        		//compute sup(intra)
        		for(Integer nj : footprint1){
        			if( ambi_map.get(n1).containsKey(nj) ){
        				supIntra = supIntra < ambi_map.get(n1).get(nj) ? ambi_map.get(n1).get(nj) : supIntra;  
        			}
        			else{
        				supIntra = supIntra < 0 ? 0 : supIntra;
        			}	
        		}
        		
        		//compute sup(inter)
        		for(Integer nj : footprint2){
        			if( ambi_map.get(n1).containsKey(nj) ){
        				supInter = supInter < ambi_map.get(n1).get(nj) ? ambi_map.get(n1).get(nj) : supInter;
        			}
        			else{
        				supInter = supInter < 0 ? 0 : supInter;
        			}
        		}
        		
        		curScore = ( 1 + supInter ) / ( 1 + supIntra ) * alpha + sizeEntropy * (1-alpha); //revised score
    			
        		//update if needed
        		if( curScore > MaxScore ){
    				MaxScore = curScore;
    				maxNid = n1;
    				maxFid = 1;
    			}
        	}//end of checking nodes in frontier1
        	
        	for(Integer n2 : frontier2){
        		double supIntra = -1; 
        		double supInter = -1;
        		double curScore = 0;
        		
        		//get size balance score
        		int cSize = ng.cs.clusters.get(n2).points.size(); //size of candidate cluster node
        		double footprintSize2Ratio = (footprintSize2 + cSize +0.0) / (footprintSize1 + cSize + footprintSize2);
        		double sizeEntropy = -1 * footprintSize2Ratio * Math.log( footprintSize2Ratio ) / Math.log(2)
        				- (1-footprintSize2Ratio ) * Math.log(1- footprintSize2Ratio ) / Math.log(2);    

        		
        		//if no ambiguity for inter or intra, score = (1+0)/(1+0)=1;
        		if( !c1ids.contains(n2) && !c2ids.contains(n2) ){
        			curScore = 1 * alpha + sizeEntropy * (1-alpha);
        			if( curScore > MaxScore ){
        				MaxScore = curScore;
        				maxNid = n2;
        				maxFid = 2;
        			}
        			continue;
        		}
        		
        		//compute sup(intra)
        		for(Integer nj : footprint2){
        			if( ambi_map.get(n2).containsKey(nj) ){
        				supIntra = supIntra < ambi_map.get(n2).get(nj) ? ambi_map.get(n2).get(nj) : supIntra;  
        			}
        			else{
        				supIntra = supIntra < 0 ? 0 : supIntra;
        			}	
        		}
        		
        		//compute sup(inter)
        		for(Integer nj : footprint1){
        			if( ambi_map.get(n2).containsKey(nj) ){
        				supInter = supInter < ambi_map.get(n2).get(nj) ? ambi_map.get(n2).get(nj) : supInter;
        			}
        			else{
        				supInter = supInter < 0 ? 0 : supInter;
        			}
        		}
        		
        		curScore = ( 1 + supInter ) / ( 1 + supIntra ) * alpha + sizeEntropy * (1-alpha);
    			
        		//update if needed
        		if( curScore > MaxScore ){
    				MaxScore = curScore;
    				maxNid = n2;
    				maxFid = 2;
    			}
        	}//end of checking nodes in frontier 2
        	
        	if (debug){
        		System.out.println("footprint " + maxFid + " adds node " + maxNid);
        	}
        	
        	
        	if( maxFid == 1 ){
        		footprint1.add(maxNid);
        		frontier1.remove(maxNid);
        		frontier2.remove(maxNid);
        		for(Integer ni : ng.graph.get(maxNid).neighborList.keySet()){
        			if( !footprint1.contains(ni) && !footprint2.contains(ni) ){
        				frontier1.add(ni);
        			}
        		}
        		footprintSize1 += ng.cs.clusters.get(maxNid).points.size();
        	}
        	else{
        		footprint2.add(maxNid);
        		frontier2.remove(maxNid);
        		frontier1.remove(maxNid);
        		for(Integer ni : ng.graph.get(maxNid).neighborList.keySet()){
        			if( !footprint1.contains(ni) && !footprint2.contains(ni) ){
        				frontier2.add(ni);
        			}
        		}
        		footprintSize2 += ng.cs.clusters.get(maxNid).points.size();
        	}//end: adding node to footprint and updating frontier
        }//end of adding all nodes to footprints
        return true;
     }

    
    public void WriteFootprintsToFile(String filename) {    	
    	BufferedWriter bw = null;
    	try {
                bw = new BufferedWriter(new FileWriter(filename));
            	for(int i = 1; i <= footprints.size(); i ++){
            		//bw.write("Footprint1:\n");
                    NeighborGraph ng = footprints.get(i-1);
                    
                    for(Cluster c : ng.cs.clusters.values()){
                    	for(Point p : c.points){
                    		bw.write(i + ","+ p.pointID +"\n");
                    	}
                    }
            	}
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
            }// end of try-catch    	
    }
    
    public void WriteSamplesToCSVFile(HashSet<Point> set, ArrayList<Integer> classes, String filename) {    	
    	BufferedWriter bw = null;
    	try {
                bw = new BufferedWriter(new FileWriter(filename));
            	for(Point p : set){
            		String line = "";
            		for(int f = 0; f < p.fDim; f++){
            			line += Double.toString(p.features[f]) + ",";
            		}
            		line += Integer.toString(classes.get(p.pointID));
            		line += "\n";
            		bw.write(line);
            	}
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
            }// end of try-catch    	
    }
    
}

class ClusterPair implements Comparable<ClusterPair> {
    public int c1id; // cluster 1 id
    public int c2id; // cluster 2 id
    public double amb; // cluster pair-wise ambiguity

    public ClusterPair(int c1, int c2, double ambi) {
        c1id = c1;
        c2id = c2;
        amb = ambi;
    }

    public int compareTo(ClusterPair p) {
        if (this.amb > p.amb) return 1;
        else if (this.amb < p.amb) return -1;
        else return 0;
    }

    public void Print(Clusters cs) {
        System.out.println(c1id + "," + c2id + "," + amb);
        //System.out.println(c1id+"("+cs.clusters.get(c1id).classCount+")," +c2id+"("+cs.clusters.get(c2id).classCount+"):"+amb);
    }
}

