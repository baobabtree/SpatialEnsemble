import java.io.*;
import java.util.*;


public class NeighborGraph {

    public HashMap<Integer, AdjacencyNode> graph;

    public Clusters cs;
    
    public boolean debug = false;
    public boolean timeCount = false;

    
    public NeighborGraph() {
        graph = new HashMap<Integer, AdjacencyNode>();
        cs = new Clusters();
    }
    
    public NeighborGraph(HashMap<Integer, AdjacencyNode> g, Clusters cls) {
        graph = g;
        cs = cls;
    }

    public NeighborGraph(Clusters cs1, int r) {
        cs = cs1;
        graph = new HashMap<Integer, AdjacencyNode>();
        for (Cluster ci : cs.clusters.values()) {
            graph.put(ci.id, new AdjacencyNode());
            for (Cluster cj : cs.clusters.values()) {
                if (cj.id == ci.id) continue;
                boolean isNeigh = false;
                for (Point pi : ci.points) {
                    if (isNeigh) break;
                    for (Point pj : cj.points) {
                        if (Math.abs(pi.x - pj.x) <= r && Math.abs(pi.y - pj.y) <= r) {
                            isNeigh = true;
                            //graph.get(ci.id).cid = ci.id;
                            graph.get(ci.id).neighborList.put(cj.id, 0.0); //here we ignore cluster (feature) distances! put as 0
                            break;
                        }
                    }
                }
            }
        }
    }
    
    //read neighbor graph from file, assume clusters are given
    public NeighborGraph(Clusters cs1, String filename){
    	cs = cs1;
    	graph = new HashMap<Integer, AdjacencyNode>();

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {
			br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {
			    //use comma as separator
				String[] fields = line.split(cvsSplitBy);
				int i = 0; int cid = -1;
				while( i < fields.length){
					if( i == 0 ) { 
						cid = Integer.parseInt(fields[0]); graph.put(cid, new AdjacencyNode()); i++;
					}
					else{
						graph.get(cid).neighborList.put(Integer.parseInt(fields[i]), Double.parseDouble(fields[i+1]));
						i+=2;
					}
				}
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
    }
    
    
    public NeighborGraph(Clusters cls, int nr, int nc, int r) {
        //each cluster is a node, check neighbors
        graph = new HashMap<Integer, AdjacencyNode>();
        cs = cls;

        for (Integer ci : cs.clusters.keySet()) {
            //create a node
            graph.put(ci, new AdjacencyNode());
            //graph.get(ci).cid = ci;
        }

        System.out.println("finished NG part 1");

        int ii, jj, i, j, u, d, l, ri, nid;
        double dist;

        //r = 2; int nr = 221, nc = 374;
        for (Integer ci : cs.clusters.keySet()) {
            i = (int) ci / nc;
            j = (int) ci % nc;
            u = i - r < 0 ? 0 : i - r;
            d = i + r < nr ? i + r : (nr - 1);
            l = j - r < 0 ? 0 : j - r;
            ri = j + r < nc ? j + r : (nc - 1);

            for (ii = u; ii <= d; ii++) {
                for (jj = l; jj <= ri; jj++) {
                    if (i == ii && j == jj) continue;
                    nid = jj + ii * nc;
                    dist = cs.clusters.get(ci).SimilarityWithCluster(cs.clusters.get(nid));
                    graph.get(ci).neighborList.put(nid, dist);
                }
            }
        }
    }
    
    public ArrayList<NeighborGraph> NeighborGraphBiSplit(HashSet<Integer> footprint1, HashSet<Integer> footprint2){
    	ArrayList<NeighborGraph> nglist = new ArrayList<NeighborGraph>(2);
    	Clusters cls1 = new Clusters();
    	Clusters cls2 = new Clusters();
    	//break down cluster set
    	for(Integer cid : footprint1){
    		cls1.AddCluster(cs.clusters.get(cid));
    	}
    	for(Integer cid : footprint2){
    		cls2.AddCluster(cs.clusters.get(cid));
    	}
    	//break down graph
    	HashMap<Integer, AdjacencyNode> graph1 = new HashMap<Integer, AdjacencyNode>();
    	HashMap<Integer, AdjacencyNode> graph2 = new HashMap<Integer, AdjacencyNode>();
    	for(Integer cid : graph.keySet()){
    		if( footprint1.contains(cid) ){
    			graph1.put(cid, new AdjacencyNode());
    		}
    		else{
    			graph2.put(cid, new AdjacencyNode());
    		}
    	}
    	for(Integer cid : graph.keySet()){
    		for(Integer cj : graph.get(cid).neighborList.keySet()){
    			if( footprint1.contains(cid) && footprint1.contains(cj) ){
    				graph1.get(cid).neighborList.put(cj, graph.get(cid).neighborList.get(cj));
    			}
    			else if( footprint2.contains(cid) && footprint2.contains(cj)){
    				graph2.get(cid).neighborList.put(cj, graph.get(cid).neighborList.get(cj));
    			}
    		}
    	}
    	NeighborGraph ng1 = new NeighborGraph(graph1, cls1);
    	NeighborGraph ng2 = new NeighborGraph(graph2, cls2);
    	
    	nglist.add(ng1);
    	nglist.add(ng2);
    	
    	//compute ambiguity pair map
    	if (!ng1.cs.hasBipartiteGraph){
    		ng1.cs.InheritBipartiteGraph(this.cs);
    	}
    	if (!ng2.cs.hasBipartiteGraph){
    		ng2.cs.InheritBipartiteGraph(this.cs);
    	}
    	
    	return nglist;
    }
    
    public void ReadGraphFromFile(String filename){
    	graph = new HashMap<Integer, AdjacencyNode>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {
			br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {
			    //use comma as separator
				String[] fields = line.split(cvsSplitBy);
				int i = 0; int cid = -1;
				while( i < fields.length){
					if( i == 0 ) { 
						cid = Integer.parseInt(fields[0]); graph.put(cid, new AdjacencyNode()); i++;
					}
					else{
						graph.get(cid).neighborList.put(Integer.parseInt(fields[i]), Double.parseDouble(fields[i+1]));
						i+=2;
					}
				}
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
    }

    
    public void WriteGraphToFile(String filename){
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filename));
            for(Integer cid : graph.keySet()){
            	bw.write(cid.toString());
            	for(Integer nid : graph.get(cid).neighborList.keySet()){
            		bw.write(","+nid+","+graph.get(cid).neighborList.get(nid));
            	}
            	bw.write("\n");
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
        }
    }

    public void Print() {
        for (Map.Entry<Integer, AdjacencyNode> entry : graph.entrySet()) {
            cs.clusters.get(entry.getKey()).Print();
            System.out.print(entry.getKey());
            entry.getValue().Print();
        }
    }



    private int MergeTwoClusterNewNode(int ci, int cj, int maxCid) {
        // the actual process to merge two clusters

        // the number of points in the original ci and cj
        int ci_node_num = cs.clusters.get(ci).points.size();
        int cj_node_num = cs.clusters.get(cj).points.size();

        // update neighborhood graph
        // remove ci and cj from each other's list
        graph.get(ci).neighborList.remove(cj);
        graph.get(cj).neighborList.remove(ci);
        // the adjacent nodes of ci and cj
        AdjacencyNode ani = graph.get(ci);
        AdjacencyNode anj = graph.get(cj);

        // new neighbor list of ci
        HashMap<Integer, Double> new_neighborList = new HashMap<Integer, Double>();

        for (Integer cineigh : ani.neighborList.keySet()) {

            int cineigh_node_num = cs.clusters.get(cineigh).points.size();

            if (anj.neighborList.containsKey(cineigh)) {
                new_neighborList.put(cineigh, (ani.neighborList.get(cineigh) * ci_node_num * cineigh_node_num
                        + anj.neighborList.get(cineigh) * cj_node_num * cineigh_node_num)
                        / (ci_node_num * cineigh_node_num + cj_node_num * cineigh_node_num));
            } else {
                new_neighborList.put(cineigh, (ani.neighborList.get(cineigh) * ci_node_num * cineigh_node_num
                        + cs.clusters.get(cj).SimilarityWithCluster(cs.clusters.get(cineigh)) * cj_node_num * cineigh_node_num)
                        / (ci_node_num * cineigh_node_num + cj_node_num * cineigh_node_num));
            }
        }
        for (Integer cjneigh : anj.neighborList.keySet()) {
            if (new_neighborList.containsKey(cjneigh)) {
                continue;
            }//avoid repeated work

            int cjneigh_node_num = cs.clusters.get(cjneigh).points.size();

            if (ani.neighborList.containsKey(cjneigh)) {
                new_neighborList.put(cjneigh, (ani.neighborList.get(cjneigh) * ci_node_num * cjneigh_node_num
                        + anj.neighborList.get(cjneigh) * cj_node_num * cjneigh_node_num)
                        / (ci_node_num * cjneigh_node_num + cj_node_num * cjneigh_node_num));
            } else {
                new_neighborList.put(cjneigh, (cs.clusters.get(ci).SimilarityWithCluster(cs.clusters.get(cjneigh)) * ci_node_num * cjneigh_node_num
                        + anj.neighborList.get(cjneigh) * cj_node_num * cjneigh_node_num)
                        / (ci_node_num * cjneigh_node_num + cj_node_num * cjneigh_node_num));
            }
        }
        graph.put(maxCid+1, new AdjacencyNode(new_neighborList));
        graph.remove(ci);
        graph.remove(cj);
        for (Integer cineigh : new_neighborList.keySet()) {
            //update neighborList of old neihgbors of ci and cj 
            graph.get(cineigh).neighborList.remove(ci);
            graph.get(cineigh).neighborList.remove(cj);
            graph.get(cineigh).neighborList.put(maxCid+1, new_neighborList.get(cineigh));
        }

        //merge cj into ci, remove cluster cj
        cs.clusters.get(ci).MergeWithCluster(cs.clusters.get(cj)); //get new merged cluster
        cs.clusters.get(ci).id = maxCid + 1;
        cs.clusters.put(maxCid+1, new Cluster(cs.clusters.get(ci))); //add into clusters group
        cs.clusters.remove(ci); //remove old cluster ci
        cs.clusters.remove(cj); //remove old cluster cj

        return maxCid+1;
    }
    
  
    
    //not tested!
    public int RemoveHoles(int minSize, int maxId){
    	
    	HashSet<Integer> holes = new HashSet<Integer>();
    	for(Integer cid : cs.clusters.keySet()){
			if( cs.clusters.get(cid).points.size() < minSize ){
				holes.add(cid);
			}
		}
    	while(! holes.isEmpty() ){
    		int cid = holes.iterator().next(); 
    		//merge cid with its neighbors
    		int largestNeigh = -1; int largestNeighSize = -1;
			for(Integer nid : graph.get(cid).neighborList.keySet()){
				if( cs.clusters.get(nid).label * cs.clusters.get(cid).label != 0 
						&& cs.clusters.get(cid).label != cs.clusters.get(nid).label ){
					continue;
				}
				if( cs.clusters.get(nid).points.size() > largestNeighSize ){
					largestNeighSize = cs.clusters.get(nid).points.size();
					largestNeigh = nid;
				}
			}
			
			if(largestNeigh != -1 ){
				maxId = MergeTwoClusterNewNode(cid, largestNeigh, maxId);//return last used maxId
				if( holes.contains(largestNeigh) ){
					holes.remove(largestNeigh);
					holes.add(maxId);
				}
			}
			holes.remove(cid);
    	}
    	
		return maxId;
    }

    public boolean HMergeFaster(int nPatch, int minPatchSize, int step, String patchFileDir) {
    	long tStart = System.nanoTime();//start time counter
    	StringBuilder strBlder = new StringBuilder();
    	
    	PriorityQueue<PatchPair> PQ = new PriorityQueue<PatchPair>();
    	HashSet<Integer> obsoleteCids = new HashSet<Integer>();
    	PQ.add(new PatchPair(0,0,0)); //dummy element for stop criteria
    	int maxId = cs.clusters.size(); // last maxid taken
    	
    	int nextSize = cs.clusters.size(); //when cluster.size reach this number, redo PriorityQueue
    	int checkMinSize = (int) (cs.clusters.size() * 0.05); //used for cleanup small holes
    	
    	while( cs.clusters.size() > nPatch && !PQ.isEmpty() ){
    		//add in a step to remove holes
    		if( cs.clusters.size() == checkMinSize ){
    			//remove small whole; TBD
    			int lastMaxId = maxId;
    			maxId = RemoveHoles(minPatchSize, maxId);
    			if(lastMaxId != maxId){
    				PQ.clear();
        			obsoleteCids.clear();
                    for (Integer cid : graph.keySet()) {
                        AdjacencyNode an = graph.get(cid);
                        for (Integer nid : an.neighborList.keySet()) {
                            int labeli = cs.clusters.get(cid).label;
                            int labelj = cs.clusters.get(nid).label;
                            //no need to compute feature distance if different labels or id order unsatisfied
                            if (labeli * labelj != 0 && labeli != labelj || cid > nid ) continue;
                            PQ.add(new PatchPair(cid, nid, an.neighborList.get(nid)));
                        }
                    }
                    
                    while(cs.clusters.size() < nextSize){
                    	nextSize = nextSize / 2;
                    }
    			}//since the obsolete pairs are no longer valid, should regenerate all things
    		}
    		
    		if(cs.clusters.size() <= nextSize){
                //clear PQ and Reload Priority Queue
    			PQ.clear();
    			obsoleteCids.clear();
                for (Integer cid : graph.keySet()) {
                    AdjacencyNode an = graph.get(cid);
                    for (Integer nid : an.neighborList.keySet()) {
                        int labeli = cs.clusters.get(cid).label;
                        int labelj = cs.clusters.get(nid).label;
                        //no need to compute feature distance if different labels or id order unsatisfied
                        if (labeli * labelj != 0 && labeli != labelj || cid > nid ) continue;
                        PQ.add(new PatchPair(cid, nid, an.neighborList.get(nid)));
                    }
                }
                nextSize = nextSize / 2;
    		}//update priority queue with neighbor pairs and their distances
    		
    		while( !PQ.isEmpty() && ( obsoleteCids.contains(PQ.peek().c1id) 
    				|| obsoleteCids.contains(PQ.peek().c2id) ) ){
    			PQ.poll();
    		}
    		//extract the current min dist VALID pair
    		PatchPair pair = PQ.poll();
    		if(cs.clusters.get(pair.c1id).label * cs.clusters.get(pair.c2id).label > 0 
    				&& cs.clusters.get(pair.c1id).label != cs.clusters.get(pair.c2id).label ){
    			continue;
    		}//it may risk that 
    			 
    		maxId = MergeTwoClusterNewNode(pair.c1id, pair.c2id, maxId);//return last used maxId
    		
    		//update PriorityQueue with New Pairs
    		for(Integer nid : this.graph.get(maxId).neighborList.keySet()){
    			PQ.add(new PatchPair(maxId, nid, this.graph.get(maxId).neighborList.get(nid)));
    		}
    		obsoleteCids.add(pair.c1id);
    		obsoleteCids.add(pair.c2id);
    		
    		if (debug){
    			if (cs.clusters.size() % 1000 == 0) System.out.println(cs.clusters.size());
    		}
    		
    		if (patchFileDir != ""){
    			if (this.timeCount){
    				if ( (cs.clusters.size() - nPatch) % step == 0 ) {
    					long tEnd = System.nanoTime();
        				System.out.println("HMergeFaster," + cs.clusters.size() + "," + Long.toString((tEnd-tStart)/1000000000) );
    					strBlder.append(cs.clusters.size() + "," + Long.toString((tEnd-tStart)/1000000000) +"\n");
    				}
    			}
    			else if (cs.clusters.size() <= nPatch + 10 * step && (cs.clusters.size() - nPatch) % step == 0){
                	int size = cs.clusters.size();
                	String filenameC = patchFileDir + File.separator + "cluster." + Integer.toString(size) + ".txt";
                	String filenameG = patchFileDir + File.separator + "graph." + Integer.toString(size) + ".txt";
                	this.cs.WriteToFile(filenameC);
                	this.WriteGraphToFile(filenameG);
                }
    		}
    	}
    	
    	//flush result to timeLog.txt
    	if (this.timeCount){
    		FileIO.WriteStringToFile(patchFileDir+File.separator+"HMergeFaster.time.txt", strBlder.toString());
    	}
        return true;
    }
    
    public boolean HMergeBaseline(int nPatch, int minPatchSize, int step, String patchFileDir) {
    	long tStart = System.nanoTime();
    	StringBuilder strBlder = new StringBuilder();
    	
    	int maxId = cs.clusters.size(); // last maxid taken
    	int checkMinSize = (int) (cs.clusters.size() * 0.05); //used for cleanup small holes
    	
    	while( cs.clusters.size() > nPatch ){
    		//add in a step to remove holes
    		if( cs.clusters.size() == checkMinSize ){
    			//remove small whole; TBD
    			maxId = RemoveHoles(minPatchSize, maxId);
    		}    		
    		
    		//extract the VALID cluster(node) pair with min feature distance
    		int minCid1 = -1, minCid2 = -1;
    		double minDistance = Double.MAX_VALUE;
    		for(Integer c1id : graph.keySet()){
    			for(Integer c2id : graph.get(c1id).neighborList.keySet()){
    				if (cs.clusters.get(c1id).label * cs.clusters.get(c2id).label > 0 
    						&& cs.clusters.get(c1id).label != cs.clusters.get(c2id).label ){
    					continue;
    				}
    				if (graph.get(c1id).neighborList.get(c2id) < minDistance){
    					minCid1 = c1id;
    					minCid2 = c2id;
    					minDistance = graph.get(c1id).neighborList.get(c2id);
    				}
    			}
    		}
    		
    		maxId = MergeTwoClusterNewNode(minCid1, minCid2, maxId);//return last used maxId
    		
    		if (debug){
    			if (cs.clusters.size() % 1000 == 0) System.out.println(cs.clusters.size());
    		}
            
    		if (patchFileDir != ""){
    			if (this.timeCount){
    				if ((cs.clusters.size() - nPatch) % step == 0) {
    					long tEnd = System.nanoTime();
        				//System.out.println("HMergeBaseline," + cs.clusters.size() + "," + Long.toString((tEnd-tStart)/1000000000) );
    					strBlder.append(cs.clusters.size() + "," + Long.toString((tEnd-tStart)/1000000000) +"\n");
    				}
    			}
    			else if (cs.clusters.size() <= nPatch + 10 * step && (cs.clusters.size() - nPatch) % step == 0){
                	int size = cs.clusters.size();
                	String filenameC = patchFileDir + File.separator + "cluster." + Integer.toString(size) + ".txt";
                	String filenameG = patchFileDir + File.separator + "graph." + Integer.toString(size) + ".txt";
                	this.cs.WriteToFile(filenameC);
                	this.WriteGraphToFile(filenameG);
                }
    		}
    	}
    	
    	//flush result to timeLog.txt
    	if (this.timeCount){
    		FileIO.WriteStringToFile(patchFileDir+File.separator+"HMergeBaseline.time.txt", strBlder.toString());
    	}
    		
        return true;
    }
 
        
}


class AdjacencyNode {
    //Adjacency List

    public HashMap<Integer, Double> neighborList;

    public AdjacencyNode() {
        neighborList = new HashMap<Integer, Double>();
    }
    
    public AdjacencyNode(HashMap<Integer, Double> nl) {
        neighborList = nl;
    }

    public void Print() {
        //System.out.println("cluster id = " + cid + "; ");
        for (Integer neighcid : neighborList.keySet()) {
            System.out.print("(" + neighcid + "," + neighborList.get(neighcid) + "),");
        }
        System.out.println("\n");
    }
}

class PatchPair implements Comparable<PatchPair> {
    public int c1id; // cluster 1 id
    public int c2id; // cluster 2 id
    public double dist; // cluster pair-wise ambiguity

    public PatchPair(int c1, int c2, double dis) {
        c1id = c1;
        c2id = c2;
        dist = dis;
    }

    public int compareTo(PatchPair p) {
        if (this.dist > p.dist) return 1;
        else if (this.dist < p.dist) return -1;
        else return 0;
    }

    public void Print(Clusters cs) {
        System.out.println(c1id + "," + c2id + "," + dist);
        //System.out.println(c1id+"("+cs.clusters.get(c1id).classCount+")," +c2id+"("+cs.clusters.get(c2id).classCount+"):"+amb);
    }
}