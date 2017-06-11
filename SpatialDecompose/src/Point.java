import java.util.*;
import java.io.*;

public class Point {
	int pointID;
	int fDim;
	double[] features;
	int x;
	int y;
	int label;

	public Point(int pid){
		pointID = pid;
	}
	
	public Point(int ValpointID, int ValfDim, double[] Valfeatures, int lab, int Valx, int Valy){
		pointID = ValpointID;
		fDim = ValfDim;
		features = Valfeatures;
		label = lab; //0:unlabeled, 1, 2: labels dryland, wetland
		x = Valx;
		y = Valy;
	}
	

	public int hashCode(){
		return pointID;
	}

	public boolean equals(Object o){
		return (o instanceof Point) && ( (Point ) o).pointID == pointID;
	}

	public void Print(){
		System.out.print(pointID + " ");
		for(double f : features){
			System.out.print(f + " ");
		}
		System.out.print( label + " " + x + " " + y + "\n");
	}

	public double distanceWith(Point p){
		double dist = 0;
		//assume same dimension
		int i = 0;
		for(i = 0; i < fDim; i++){
			dist = dist + (p.features[i] - features[i]) * (p.features[i] - features[i]);
		}
		return( Math.sqrt(dist) );
	}

	public static ArrayList<Point> ReadPointFile(String filename, int fDim){
		ArrayList<Point> points = new ArrayList<Point>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {

			br = new BufferedReader(new FileReader(filename));

			int Pid = 0;

			while ((line = br.readLine()) != null) {

			    //use comma as separator
				String[] fields = line.split(cvsSplitBy);
				double[] fvals = new double[fDim];
				int label; int x, y;
				int fld = 0;
				for(fld = 0; fld < fDim; fld++){
					fvals[fld] = Double.parseDouble(fields[fld]);
				}
				label = Integer.parseInt(fields[fDim]);
				x = Integer.parseInt(fields[fDim+1]);
				y = Integer.parseInt(fields[fDim+2]);

				Point p = new Point(Pid, fDim, fvals, label, x, y);
				points.add(p);
				Pid = Pid + 1;
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
		return points;
	}


}