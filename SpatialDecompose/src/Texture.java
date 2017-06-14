import java.io.*;
import java.util.*;

//NOTE: not tested yet!
public class Texture {
	
	//compute texture from input array to output array, pixels are stored row-wise in array
	public static ArrayList<Double> ComputeTexture(ArrayList<Double> input, int nr, int nc, int w, int numBins){
		
		ArrayList<Double> output = new ArrayList<Double>(input.size());
		int N = nr * nc;
		if(input.size() != N ){
			System.out.println("Wrong input for Texture, input and output array length not matching nr nc!");
			System.exit(1);
		}
		
		int[][] coMat = new int [numBins][numBins];
		for(int i = 0; i < numBins; i++){
			for(int j = 0; j < numBins; j++){
				coMat[i][j] = 0;
			}
		}
		
		int ii = 0, jj = 0, ri = 0, ci = 0, l = 0, r = 0, u = 0, d = 0;
		int widthBin = 256 % numBins == 0 ? 256 / numBins : 256 / numBins + 1;
		double homo;
		
		//horizontal direction of texture
		for(int i = 0; i < N; i++){
			ri = i / nc;
			ci = i % nc;
			u = ri - w < 0 ? 0 : ri - w;
			d = ri + w > nr - 1 ? nr - 1 : ri + w;
			l = ci - w < 0 ? 0 : ci - w;
			r = ci + w > nc - 1 ? nc - 1 : ci + w;
			for(ii = u; ii <= d; ii++){
				for(jj = l; jj <= r; jj++){
					if( jj < r ){
						coMat[ (int) (input.get(ii * nc + jj) / widthBin) ] [ (int) (input.get(ii * nc + jj + 1) / widthBin) ] ++; 
						coMat[ (int) (input.get(ii * nc + jj + 1) / widthBin) ] [ (int) (input.get(ii * nc + jj) / widthBin) ] ++;
					}
				}
			}
			
			homo = 0;
			for(ii = 0; ii < numBins; ii++){
				for(jj = 0; jj < numBins; jj++){
					homo += coMat[ii][jj] / (1.0 + Math.abs(ii-jj));
				}
			}
			output.add(homo);
		}
		
		return output;
	}
	
	
	
	public static ArrayList<ArrayList<Double>> ReadFeaturesIntoArray(String filename, int fDim){
		ArrayList<ArrayList<Double>> features = new ArrayList<ArrayList<Double>>(fDim);
		for(int i = 0; i < fDim; i++){
			features.add(new ArrayList<Double>(10000));
		}
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {

			br = new BufferedReader(new FileReader(filename));

			while ((line = br.readLine()) != null) {

			    //use comma as separator
				String[] fields = line.split(cvsSplitBy);
				int fld = 0;
				for(fld = 0; fld < fDim; fld++){
					features.get(fld).add(Double.parseDouble(fields[fld]));
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
		return features;
	}
	
	
	public static void main(){
		String filename = "input.txt";
		int nf = 4;
		int nr = 111;
		int nc = 111;
		int w = 2;
		int numBins = 32;
		
		ArrayList<ArrayList<Double>> features = ReadFeaturesIntoArray(filename, nf);
		ArrayList<ArrayList<Double>> textures = new ArrayList<ArrayList<Double>>(nf);
		for(int i = 0; i < nf; i++){
			textures.add(ComputeTexture(features.get(i), nr, nc, w, numBins));
		}
		//print out textures
	}
}