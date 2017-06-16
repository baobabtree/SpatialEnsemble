import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.lang.Math;

public class BipartiteEnsemble {
    ArrayList<ClusterPair> pairs;
    HashMap<Integer, HashMap<Integer, Double>> ambi_map;
    HashSet<Integer> footprint1;
    HashSet<Integer> footprint2;
    int footprintSize1 = 0;
    int footprintSize2 = 0;
    
    double alpha = 0.95;
    ArrayList<NeighborGraph> footprints; //use NeighborGraph's to represent all footprints
    
    
    public BipartiteEnsemble(NeighborGraph ng, int k, int m, boolean flag){
    	footprints = new ArrayList<NeighborGraph>();
    	footprints.add(ng);
    	while(footprints.size() < m){
    		//now found most ambiguous footprint
    		double maxAmbi = -1;
    		int maxAmbiI = 0;
    		
    		for(int i = 0; i < footprints.size(); i++){
    			double curAmbi = footprints.get(i).cs.KNNAmbiguity(k);
    			if( curAmbi > maxAmbi ){
    				maxAmbi = curAmbi;
    				maxAmbiI = i;
    			}
    		} 
    		
    		System.out.println(maxAmbi);
    		
    		//bisectOneStep to split most ambiguous footprints
    		NeighborGraph ngMax = footprints.remove(maxAmbiI);
    		Bisect(ngMax, k); //results saved in footprint1, footprint2
    		
    		//split footprint graph into two subgraphs, then add to queue
    		ArrayList<NeighborGraph> nglist = ngMax.NeighborGraphBiSplit(footprint1, footprint2);
    		footprints.add(nglist.get(0));
    		footprints.add(nglist.get(1));
    	}//results saved to footprints list
    }
    
    //new Bisecting Ensemble method
    public void Bisect(NeighborGraph ng, int k){
    	ambi_map = new HashMap<Integer, HashMap<Integer, Double>>();
        ArrayList<Integer> c1ids = new ArrayList<Integer>();
        ArrayList<Integer> c2ids = new ArrayList<Integer>();
        for (Cluster c : ng.cs.clusters.values()) {
            if (c.label == 1) {
                if (c.classCount < 2.5 * k) continue;
                c1ids.add(c.id);
            } else if (c.label == 2) {
                if (c.classCount < 2.5 * k) continue;
                c2ids.add(c.id);
            }
        }

        ng.GenerateAPSP();
        
        pairs = new ArrayList<ClusterPair>(c1ids.size() * c2ids.size());
        for (Integer c1id : c1ids) {
            ambi_map.put(c1id, new HashMap<Integer, Double>());
            for (Integer c2id : c2ids) {
                ambi_map.get(c1id).put(c2id, ng.cs.clusters.get(c1id).AmbiguityWithClusterKNN(ng.cs.clusters.get(c2id), k));
                if (!ambi_map.containsKey(c2id)) ambi_map.put(c2id, new HashMap<Integer, Double>());
                ambi_map.get(c2id).put(c1id, ambi_map.get(c1id).get(c2id));
                pairs.add(new ClusterPair(c1id, c2id, ambi_map.get(c1id).get(c2id)));
            }
        }
        Collections.sort(pairs, Collections.reverseOrder());
        
        //debug: print out all pairs
        for(ClusterPair pair : pairs){
        	if(pair.amb > 0.05){
        		System.out.println(pair.c1id + "," + pair.c2id + ":" + pair.amb);
        	}
        	else {
        		break;
        	}
        }

        footprint1 = new HashSet<Integer>();
        footprint2 = new HashSet<Integer>();
        footprintSize1 = 0;
        footprintSize2 = 0;
        
        Iterator<ClusterPair> pairIter = pairs.iterator();
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
        	
        	System.out.println("footprint " + maxFid + " adds node " + maxNid);
        	
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
     }

    public BipartiteEnsemble(NeighborGraph ng, int k, int num_patches_threshold) {
        ambi_map = new HashMap<Integer, HashMap<Integer, Double>>();
        ArrayList<Integer> c1ids = new ArrayList<Integer>();
        ArrayList<Integer> c2ids = new ArrayList<Integer>();
        for (Cluster c : ng.cs.clusters.values()) {
            if (c.label == 1) {
                if (c.classCount < 2.5 * k) continue;
                c1ids.add(c.id);
            } else if (c.label == 2) {
                if (c.classCount < 2.5 * k) continue;
                c2ids.add(c.id);
            }
        }

        ng.GenerateAPSP();

        pairs = new ArrayList<ClusterPair>(c1ids.size() * c2ids.size());
        for (Integer c1id : c1ids) {
            ambi_map.put(c1id, new HashMap<Integer, Double>());
            for (Integer c2id : c2ids) {
                ambi_map.get(c1id).put(c2id, ng.cs.clusters.get(c1id).AmbiguityWithClusterKNN(ng.cs.clusters.get(c2id), k));
                if (!ambi_map.containsKey(c2id)) ambi_map.put(c2id, new HashMap<Integer, Double>());
                ambi_map.get(c2id).put(c1id, ambi_map.get(c1id).get(c2id));
                pairs.add(new ClusterPair(c1id, c2id, ambi_map.get(c1id).get(c2id)));
            }
        }
        Collections.sort(pairs, Collections.reverseOrder());

        footprint1 = new HashSet<Integer>();
        footprint2 = new HashSet<Integer>();
        // first step:
        // allocate ambiguous pairs
        double ambiThreshold = 0.04;
        AmbiguityProof(ng, ambiThreshold);

        // allocate the rest cluster according to the distance and class balance
        Allocate2Nearest(ng);
        
        
        
        // adjust the boundary
        //while (NumberOfPatches(ng) > num_patches_threshold) {
        while (true) {
        	System.out.println("starting spatial rearrangement!");
            ArrayList<Integer> outlier_ids = GetOutlier_ClusterAutocorrelation(ng, 0);
            if(outlier_ids.size() == 0){
            	System.out.println("No outliers any more! Break!");
            	break;
            }
            Integer best_outlier_id = -1;
            double smallest_ambi = Double.MAX_VALUE;
            for (Integer cId :
                    outlier_ids) {
                boolean inft1 = footprint1.contains(cId);
                double largest_ambi = Double.NEGATIVE_INFINITY; //-Double.MAX_VALUE;
                
                if(ambi_map.containsKey(cId)){
                	for (Integer dId :
                        ambi_map.get(cId).keySet()) {
                        if (inft1 == footprint1.contains(dId)) continue;
                        if (largest_ambi < ambi_map.get(cId).get(dId)) {
                            largest_ambi = ambi_map.get(cId).get(dId);
                        }

                	}
                }
                
                if (smallest_ambi > largest_ambi) {
                    smallest_ambi = largest_ambi;
                    best_outlier_id = cId;
                }
            }
            if (smallest_ambi > 0.1) break;
            if (best_outlier_id == -1) {
            	System.out.println("Invalid best_outlier_id!");
            	break;
            }
            if (footprint1.contains(best_outlier_id)){
                footprint1.remove(best_outlier_id);
                footprint2.add(best_outlier_id);
            }else {
                footprint1.add(best_outlier_id);
                footprint2.remove(best_outlier_id);
            }
        }
    }

    private void AmbiguityProof(NeighborGraph ng, double ambiThreshold) {
        // initialize the final 2 footprint
        Iterator<ClusterPair> pairIter = pairs.iterator();
        ClusterPair currentPair = pairIter.next();
        footprint1.add(currentPair.c1id);
        footprint2.add(currentPair.c2id);

        // iterate until ambiguity is less than the threshold
        while (pairIter.hasNext()) {
            currentPair = pairIter.next();
            //currentPair.Print(ng.cs);//for debugging
            
            if (currentPair.amb < ambiThreshold) break;

            if (footprint1.contains(currentPair.c1id)) {
                if (!footprint1.contains(currentPair.c2id) &&
                        !footprint2.contains(currentPair.c2id)) {
                    footprint2.add(currentPair.c2id);
                }
                continue;
            } else if (footprint2.contains(currentPair.c1id)) {
                if (!footprint2.contains(currentPair.c2id) &&
                        !footprint1.contains(currentPair.c2id)) {
                    footprint1.add(currentPair.c2id);
                }
                continue;
            } else if (footprint1.contains(currentPair.c2id)) {
                if (!footprint1.contains(currentPair.c1id) &&
                        !footprint2.contains(currentPair.c1id)) {
                    footprint2.add(currentPair.c1id);
                }
                continue;
            } else if (footprint2.contains(currentPair.c2id)) {
                if (!footprint1.contains(currentPair.c1id) &&
                        !footprint2.contains(currentPair.c1id)) {
                    footprint1.add(currentPair.c1id);
                }
                continue;
            }

            int dist11 = Distance2Footprint(currentPair.c1id, 1, ng);
            int dist12 = Distance2Footprint(currentPair.c1id, 2, ng);
            int dist21 = Distance2Footprint(currentPair.c2id, 1, ng);
            int dist22 = Distance2Footprint(currentPair.c2id, 2, ng);

            if (dist11 < dist12) {
                if (dist21 > dist22) {
                    footprint1.add(currentPair.c1id);
                    footprint2.add(currentPair.c2id);
                } else {
                    if (dist12 > dist22) {
                        footprint1.add(currentPair.c1id);
                        footprint2.add(currentPair.c2id);
                    } else {
                        footprint1.add(currentPair.c2id);
                        footprint2.add(currentPair.c1id);
                    }
                }
            } else {
                if (dist21 < dist22) {
                    footprint1.add(currentPair.c2id);
                    footprint2.add(currentPair.c1id);
                } else {
                    if (dist11 > dist21) {
                        footprint1.add(currentPair.c2id);
                        footprint2.add(currentPair.c1id);
                    } else {
                        footprint1.add(currentPair.c1id);
                        footprint2.add(currentPair.c2id);
                    }
                }
            }
        }
    }

    private void Allocate2Nearest(NeighborGraph ng) {
        int rest_cluster_num = ng.cs.clusters.size() - footprint1.size() - footprint2.size();
        for (int idx = 0; idx < rest_cluster_num; idx++) {
            ArrayList<Integer> nearestIds = new ArrayList<Integer>();
            ArrayList<Integer> nearestFts = new ArrayList<Integer>();
            double nearestDist = Double.MAX_VALUE;
            for (Cluster c : ng.cs.clusters.values()) {
                if (footprint1.contains(c.id) || footprint2.contains(c.id))
                    continue;

                int dist1 = Distance2Footprint(c.id, 1, ng);
                int dist2 = Distance2Footprint(c.id, 2, ng);

                if (dist1 < dist2) {
                    if (dist1 < nearestDist) {
                        nearestDist = dist1;
                        nearestFts.clear();
                        nearestFts.add(1);
                        nearestIds.clear();
                        nearestIds.add(c.id);
                    } else if (dist1 == nearestDist) {
                        nearestFts.add(1);
                        nearestIds.add(c.id);
                    }
                } else if (dist1 > dist2) {
                    if (dist2 < nearestDist) {
                        nearestDist = dist2;
                        nearestFts.clear();
                        nearestFts.add(2);
                        nearestIds.clear();
                        nearestIds.add(c.id);
                    } else if (dist2 == nearestDist) {
                        nearestFts.add(2);
                        nearestIds.add(c.id);
                    }
                } else {
                    if (dist1 < nearestDist) {
                        if (c.label == 0) {
                            nearestDist = dist1;
                            nearestFts.clear();
                            nearestFts.add(1);
                            nearestIds.clear();
                            nearestIds.add(c.id);
                        } else {
                            double entr1 = Entropy2FootprintDelta(c.id, 1, ng);
                            double entr2 = Entropy2FootprintDelta(c.id, 2, ng);
                            if (entr1 > entr2) {
                                nearestDist = dist1;
                                nearestFts.clear();
                                nearestFts.add(1);
                                nearestIds.clear();
                                nearestIds.add(c.id);
                            } else {
                                nearestDist = dist2;
                                nearestFts.clear();
                                nearestFts.add(2);
                                nearestIds.clear();
                                nearestIds.add(c.id);
                            }
                        }
                    } else if (dist1 == nearestDist) {
                        if (c.label == 0) {
                            nearestFts.add(1);
                            nearestIds.add(c.id);
                        } else {
                            double entr1 = Entropy2FootprintDelta(c.id, 1, ng);
                            double entr2 = Entropy2FootprintDelta(c.id, 2, ng);
                            if (entr1 > entr2) {
                                nearestFts.add(1);
                                nearestIds.add(c.id);
                            } else {
                                nearestFts.add(2);
                                nearestIds.add(c.id);
                            }
                        }
                    }
                }
            }
            int nearestFt = 0;
            int nearestId = 0;
            double highestEntr = -Double.MAX_VALUE;
            for (int idx2 = 0; idx2 < nearestFts.size(); idx2++) {
                double entrTemp = Entropy2FootprintDelta(nearestIds.get(idx2), nearestFts.get(idx2), ng);
                if (entrTemp > highestEntr) {
                    nearestFt = nearestFts.get(idx2);
                    nearestId = nearestIds.get(idx2);
                    highestEntr = entrTemp;
                }
            }
            switch (nearestFt) {
                case 1:
                    footprint1.add(nearestId);
                    break;
                case 2:
                    footprint2.add(nearestId);
                    break;
            }
        }
    }

    private double Entropy2FootprintDelta(int cId, int footprintId, NeighborGraph ng) {
        HashSet<Integer> currentFP = null;
        switch (footprintId) {
            case 1:
                currentFP = footprint1;
                break;
            case 2:
                currentFP = footprint2;
                break;
            default:
                return -1;
        }

        int numPos = 0; // ng.cs.clusters.get(cId).nc1;
        int numNeg = 0; // ng.cs.clusters.get(cId).nc2;
        for (Integer dId : currentFP
                ) {
            numPos += ng.cs.clusters.get(dId).nc1;
            numNeg += ng.cs.clusters.get(dId).nc2;
        }
        double p_old = (double) numPos / (numPos + numNeg);
        double entr_old = CalculateEntropy(p_old);
        numPos += ng.cs.clusters.get(cId).nc1;
        numNeg += ng.cs.clusters.get(cId).nc2;
        double p_new = (double) numPos / (numPos + numNeg);
        double entr_new = CalculateEntropy(p_new);
        return entr_new - entr_old;
    }

    private double CalculateEntropy(double p) {
        if (p < 0.0000001 || p > 0.9999999) return -100;
        double log2 = Math.log(2);
        return -1 * p * Math.log(p) / log2 - (1 - p) * Math.log(1 - p) / log2;
    }

    private int Distance2Footprint(int cId, int footprintId, NeighborGraph ng) {
        HashSet<Integer> currentFP = null;
        switch (footprintId) {
            case 1:
                currentFP = footprint1;
                break;
            case 2:
                currentFP = footprint2;
                break;
            default:
                return -1;
        }

        Integer minDist = Integer.MAX_VALUE;
        HashMap<Integer, Integer> currentDistMap = ng.APSP.get(cId);
        for (Integer dId : currentFP
                ) {
            if (currentDistMap.get(dId) < minDist) {
                minDist = currentDistMap.get(dId);
            }
        }
        return minDist;
    }

    private int NumberOfPatches(NeighborGraph ng) {
        ArrayList<Integer> visited = new ArrayList<Integer>();
        ArrayList<Integer> visitQ = new ArrayList<Integer>();
        int num_patches = 0;
        for (Integer cId :
                footprint1) {
            if (visited.contains(cId)) continue;

            num_patches++;
            visited.add(cId);
            visitQ.clear();
            visitQ.addAll(ng.graph.get(cId).neighborList.keySet());
            while (visitQ.size() != 0) {
                cId = visitQ.remove(0);
                if (visited.contains(cId)) continue;
                for (Integer nId:
                        ng.graph.get(cId).neighborList.keySet()) {
                    if (footprint2.contains(nId)) continue;
                    visitQ.add(nId);
                }
                visited.add(cId);
            }
        }
        for (Integer cId :
                footprint2) {
            if (visited.contains(cId)) continue;

            num_patches++;
            visited.add(cId);
            visitQ.clear();
            visitQ.addAll(ng.graph.get(cId).neighborList.keySet());
            while (visitQ.size() != 0) {
                cId = visitQ.remove(0);
                if (visited.contains(cId)) continue;
                for (Integer nId:
                        ng.graph.get(cId).neighborList.keySet()) {
                    if (footprint1.contains(nId)) continue;
                    visitQ.add(nId);
                }
                visited.add(cId);
            }
        }
        return num_patches;
    }

    private ArrayList<Integer> GetOutlier_ClusterAutocorrelation(NeighborGraph ng, double autocorrelation_threshold) {
        ArrayList<Integer> result_list = new ArrayList<Integer>();
        for (Integer cId :
                ng.graph.keySet()) {
            int num_pos = 0;
            int num_neg = 0;
            boolean inft1 = false;
            if (footprint1.contains(cId)) inft1 = true;

            for (Integer dId :
                    ng.graph.get(cId).neighborList.keySet()) {
            	//parameter 15 below is adhoc
            	if(ng.cs.clusters.get(dId).points.size() < 15 ) {
            		continue;
            	}
                if (footprint1.contains(dId) == inft1) {
                    num_pos++;
                } else {
                    num_neg++;
                }
            }
            double temp_correlation = 1;
            if(num_pos+num_neg>0) temp_correlation = ((double)num_pos - num_neg) / (num_pos + num_neg);
            else temp_correlation = 1;

            if (temp_correlation < autocorrelation_threshold) {
                result_list.add(cId);
            }
            //for debugging?
            if( cId == 215 ) 
            	{ int a = 1; }
        }
        return result_list;
    }

    public void WriteToFile(String filename) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filename));
            //bw.write("Footprint1:\n");
            for (Integer ft :
                    footprint1) {
                bw.write(1+","+ft.toString() + "\n");
            }
            //bw.write("\nFootprint2:\n");
            for (Integer ft :
                    footprint2) {
            	bw.write(2+","+ft.toString() + "\n");
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
    
    public void WriteToFileBisect(String filename) {
    	
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
            }
    		
    	
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

