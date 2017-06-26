import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Clusters {
	//list of clusters with cluster IDs
	HashMap<Integer, Cluster> clusters;
	boolean hasBipartiteGraph = false;
	HashMap<Integer, HashMap<Integer, Double>> ambi_map;
	ArrayList<Integer> c1ids;
	ArrayList<Integer> c2ids;
	
	public Clusters(){
		clusters = new HashMap<Integer, Cluster>();
	}
	
	public Clusters(List<Cluster> cs){
		clusters = new HashMap<Integer, Cluster>();
		for (Cluster c : cs) {
			clusters.put(c.id, c);
		}
	}
	
	public void Print(){
		for(Cluster c : clusters.values()){
			c.Print();
		}
	}
	
	public void InitializeSinglePointCluster(List<Point> points){
		clusters = new HashMap<Integer, Cluster>();
		int id = 0;
		for (Point p : points) {
			Cluster c = new Cluster(id, p);
			clusters.put(c.id, c);
			id = id + 1;
		}
	}

	
	public void AddCluster(Cluster c){
		clusters.put(c.id, c);
	}
	
	public void Merge(int i, int j){
		//assume ids exist, otherwise throw exception
		int tmp; if(i>j){tmp = i; i = j; j = tmp;}
		clusters.get(i).MergeWithCluster(clusters.get(j));
		clusters.remove(j);
	}
	
	
	
	public void ReadFromOutputFile(String filename, ArrayList<Point> points){
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {
			br = new BufferedReader(new FileReader(filename));
			int pid, cid, label;
			
			while ((line = br.readLine()) != null) {
				if( line == "" ) break;
			    //use comma as separator
				String[] fields = line.split(cvsSplitBy);
				pid = Integer.parseInt(fields[0]);
				cid = Integer.parseInt(fields[1]);
				label = Integer.parseInt(fields[2]);
				if( this.clusters.containsKey(cid) ){
					this.clusters.get(cid).points.add(points.get(pid)); //not new Point(pid)
					if(points.get(pid).label>0) {
						this.clusters.get(cid).classCount++;
						if(points.get(pid).label == 1) this.clusters.get(cid).nc1 ++;
						else this.clusters.get(cid).nc2 ++;
						
						this.clusters.get(cid).labeledPoints.add(points.get(pid));
					}
				}
				else {
					Cluster c = new Cluster(cid);
					c.label = label;
					c.points.add(points.get(pid)); //new Point(pid));
					this.clusters.put(cid, c);
					if(points.get(pid).label>0) {
						this.clusters.get(cid).classCount++;
						if(points.get(pid).label == 1) this.clusters.get(cid).nc1 ++;
						else this.clusters.get(cid).nc2 ++;
						
						this.clusters.get(cid).labeledPoints.add(points.get(pid));
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
	
    public void WriteToFile(String filename) {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter(filename));
            for (Cluster c : this.clusters.values()) {
                for (Point p : c.points) {
                    bw.write(p.pointID + "," + c.id + "," + c.label + "\n");
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
	
	//return class ambiguity based on all labeled samples in the cluster group
	public Double KNNAmbiguity(int k){
		
		Cluster c = new Cluster(1);
		for(Cluster ci : clusters.values()){
			c.MergeWithCluster(ci);
		}
		return c.AmbiguityWithClusterKNN(c, k);
	}
	
	public boolean InheritBipartiteGraph(Clusters cls){
		if (!cls.hasBipartiteGraph) return false;
		if (this.hasBipartiteGraph) return true;
		
		//otherwise, inherit from parent clusters
		ambi_map = new HashMap<Integer, HashMap<Integer, Double>>();
		c1ids = new ArrayList<Integer>();
        c2ids = new ArrayList<Integer>();
        for (Cluster c : clusters.values()) {
            if (cls.c1ids.contains(c.id)) {
                c1ids.add(c.id);
            } else if (cls.c2ids.contains(c.id)) {
                c2ids.add(c.id);
            }
        }
        
        for (Integer c1id : c1ids) {
            ambi_map.put(c1id, new HashMap<Integer, Double>());
            for (Integer c2id : c2ids) {
                ambi_map.get(c1id).put(c2id, cls.ambi_map.get(c1id).get(c2id));
                if (!ambi_map.containsKey(c2id)) ambi_map.put(c2id, new HashMap<Integer, Double>());
                ambi_map.get(c2id).put(c1id, cls.ambi_map.get(c1id).get(c2id));
            }
        }
        hasBipartiteGraph = true;
        return true;
	}
	
	public void GenerateBipartiteGraph(int k, int minCls){
		int minClsCount = minCls; //1.5 * k
		ambi_map = new HashMap<Integer, HashMap<Integer, Double>>();
		c1ids = new ArrayList<Integer>();
        c2ids = new ArrayList<Integer>();
        for (Cluster c : clusters.values()) {
            if (c.label == 1) {
                if (c.classCount < minClsCount) continue;
                c1ids.add(c.id);
            } else if (c.label == 2) {
                if (c.classCount < minClsCount) continue;
                c2ids.add(c.id);
            }
        }
        
        for (Integer c1id : c1ids) {
            ambi_map.put(c1id, new HashMap<Integer, Double>());
            for (Integer c2id : c2ids) {
                ambi_map.get(c1id).put(c2id, clusters.get(c1id).AmbiguityWithClusterKNN(clusters.get(c2id), k));
                if (!ambi_map.containsKey(c2id)) ambi_map.put(c2id, new HashMap<Integer, Double>());
                ambi_map.get(c2id).put(c1id, ambi_map.get(c1id).get(c2id));
            }
        }
        hasBipartiteGraph = true;
	}
	
	public Double MaxPairwiseKNNAmbiguity(int k, int minCls){
		if (!hasBipartiteGraph){
			GenerateBipartiteGraph(k, minCls);
		}
		
        double ambi = 0;
        double ambiCid = 0;
        
        for (Integer cid : ambi_map.keySet()){
        	ambiCid = 0;
        	for (Integer c2id : ambi_map.get(cid).keySet()){
        		if (ambi_map.get(cid).get(c2id) > ambiCid){
        			ambiCid = ambi_map.get(cid).get(c2id);
        		}
        	}
        	if( ambiCid > ambi){
        		ambi = ambiCid;
        	}
        }
        
        return ambi;
	}
	
	public Double AvgPairwiseKNNAmbiguity(int k, int minCls){
		if (!hasBipartiteGraph){
			GenerateBipartiteGraph(k, minCls);
		}
		
        double ambi = 0;
        double ambiCid = 0;
        int sizeCid = 0;
        int count = 0;
        
        for (Integer cid : ambi_map.keySet()){
        	ambiCid = 0;
        	sizeCid = 0;
        	for (Integer c2id : ambi_map.get(cid).keySet()){
        		if (ambi_map.get(cid).get(c2id) > ambiCid){
        			ambiCid = ambi_map.get(cid).get(c2id);
        			sizeCid = clusters.get(cid).labeledPoints.size() 
        					+ clusters.get(c2id).labeledPoints.size();
        		}
        	}
        	ambi += ambiCid * sizeCid;
        	count += sizeCid;
        }
        
        return ambi/count;
	}
}


class Cluster{
	//set of points
	public HashSet<Point> points;
	public HashSet<Point> labeledPoints;
	public int id;
	public int label = 0; //0: no class; 1: positive class; 2: negative class; 3: mixed class
	public int classCount = 0; // number of labeled points
	public int nc1 = 0; // number of class 1 points
	public int nc2 = 0; // number of class 2 points
	
	//intermediate aggregates for ensembling
	//need to remove mu and muxx
	
	public int fdim = 0; //fdim 
	
	public Cluster(int idVal){
		id = idVal;
		points = new HashSet<Point>();
		labeledPoints = new HashSet<Point>();
		classCount=nc1=nc2=0;
	}
	
	public Cluster(Cluster c){
		id = c.id;
		label = c.label;
		classCount = c.classCount;
		nc1 = c.nc1;
		nc2 = c.nc2;
		points = new HashSet<Point>();
		labeledPoints = new HashSet<Point>();
		for(Point p : c.points){
			points.add(p);
		}
		for(Point p : c.labeledPoints){
			labeledPoints.add(p);
		}
	}
	
	//single point cluster
	public Cluster(int idVal, Point p){
		id = idVal;
		points = new HashSet<Point>();
		labeledPoints = new HashSet<Point>();
		points.add(p);
		label = p.label;
		if( p.label > 0 ) {
			classCount ++;
			labeledPoints.add(p);
			if( p.label == 1) nc1 ++;
			else nc2++;
		}
		
		fdim = p.fDim;
	}
	
	//initialize
	public Cluster(int idVal, List<Point> Mypoints){
		points = new HashSet<Point>();
		labeledPoints = new HashSet<Point>();
		id = idVal;
		label = 0;
		for (Point p : Mypoints) {
			points.add(p);
			if (p.label > 0) {
				classCount++;
				labeledPoints.add(p);
				if (p.label == 1) nc1++;
				else nc2++;
			}
			if (p.label == 0 || label == 3) continue;
			if (label == 0) {
				label = p.label;
			} else if (label != p.label) {
				label = 3;
			}
		}
		
		if(Mypoints.size()>0){
			fdim = Mypoints.get(0).fDim;
		}
	}
	
	public void Print(){
		System.out.println("cluster: id = " + this.id + "; label = " + this.label);
		for (Point point : this.points) {
			point.Print();
		}
	}
	
	//add points
	public void AddPoint(Point p){
		points.add(p);
		if(p.label > 0) {
			classCount++;
			labeledPoints.add(p);
			if( p.label == 1) nc1 ++;
			else nc2++;
		}
		if(label == 3 || p.label == 0) return;
		if(label == 0) {
			label = p.label;
		}
		else if(label != p.label){
			label = 3;
		}
		if(fdim < 0) fdim = p.fDim;
	}
	
	public void MergeWithCluster(Cluster c){
		classCount += c.classCount;
		nc1 += c.nc1;
		nc2 += c.nc2;
		for (Point point : c.points) {
			points.add(point);
			if(point.label>0){
				labeledPoints.add(point);
			}	
		}
		if(label == 3 || c.label == 0) return;
		
		if(label == 0 ){
			label = c.label;
		}
		else if(label != c.label){
			label = 3;
		}
	}
	
	
	public double SimilarityWithCluster(Cluster c){
		double dist = 0;
		//compute avg distance between points from two clusters
		for(Point pi : points){
			for(Point pj : c.points){
				dist = dist + pi.distanceWith(pj);
			}
		}
		return dist/(points.size()*c.points.size());
	}
	
	//metric: 1 for K-NN, 2 for reverse distance, 3 for ...
	public double AmbiguityWithClusterKNN(Cluster c, int k){
		double amb = 0.0;
		/*if(c.label * this.label == 0 || c.label == this.label) 
			return 0.0;*/
		//ratio of KNN
		//k = 20; //sensitive? No, according to what I see
		//use Min-priorityqueue and negative distance to exclude high distance
		PriorityQueue<QueueElem> PQ = new PriorityQueue<QueueElem>(k);
		double dist;
		for(Point p : this.labeledPoints){
			PQ.clear();
			for(Point pi : this.labeledPoints){
				if(pi.pointID == p.pointID) continue;
				dist = p.distanceWith(pi);
				if( PQ.size() < k ) 
					PQ.add(new QueueElem(-1*dist, pi.pointID, pi.label == p.label ? 1 : 0));
				else {
					if( -1 * dist > PQ.peek().dist ){ //find a bug!!
						PQ.poll();
						PQ.add(new QueueElem(-1*dist, pi.pointID, pi.label == p.label ? 1 : 0));
					}
				}
			}
			for(Point pj : c.labeledPoints){
				dist = p.distanceWith(pj);
				if( PQ.size() < k ) 
					PQ.add(new QueueElem(-1*dist, pj.pointID, pj.label == p.label ? 1 : 0));
				else {
					if( -1*dist > PQ.peek().dist ){
						PQ.poll();
						PQ.add(new QueueElem(-1*dist, pj.pointID, pj.label == p.label ? 1 : 0));
					}
				}
			}
			double sameCount = 0;
			while(!PQ.isEmpty()){
				sameCount += PQ.poll().same;
			}
			amb = amb + 1 - sameCount/k;
		}
		
		for(Point p : c.labeledPoints){
			PQ.clear();
			for(Point pi : this.labeledPoints){
				dist = p.distanceWith(pi);
				if( PQ.size() < k ) 
					PQ.add(new QueueElem(-1*dist, pi.pointID, pi.label == p.label ? 1 : 0));
				else {
					if( -1 * dist > PQ.peek().dist ){
						PQ.poll();
						PQ.add(new QueueElem(-1*dist, pi.pointID, pi.label == p.label ? 1 : 0));
					}
				}
			}
			for(Point pj : c.labeledPoints){
				if(pj.pointID == p.pointID) continue;

				dist = p.distanceWith(pj);
				if( PQ.size() < k ) 
					PQ.add(new QueueElem(-1*dist, pj.pointID, pj.label == p.label ? 1 : 0));
				else {
					if( -1 * dist > PQ.peek().dist ){
						PQ.poll();
						PQ.add(new QueueElem(-1*dist, pj.pointID, pj.label == p.label ? 1 : 0));
					}
				}
			}
			double sameCount = 0;
			while(!PQ.isEmpty()){
				sameCount += PQ.poll().same;
			}
			amb = amb + 1 - sameCount/k;
		}				
		amb = amb / (this.classCount + c.classCount);
		return amb;
	}
	
}

class QueueElem implements Comparable<QueueElem>{
	public double dist;
	public int pid;
	public int same;
	public QueueElem(double d, int p, int s){
		dist = d; pid = p; same = s;
	}
	
	public int compareTo(QueueElem e){
		if( this.dist > e.dist ) return 1;
		return (this.dist == e.dist ? 0 : -1);
	}
}