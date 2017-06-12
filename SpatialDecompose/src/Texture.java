import java.io.*;

//NOTE: not tested yet!
public class Texture {
	
	//compute texture from input array to output array, pixels are stored row-wise in array
	public static void ComputeTexture(int input[], int nr, int nc, int w, int numBins, int output[]){
		int N = nr * nc;
		if(input.length != N || output.length != N){
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
		int widthBin = (int) (256.0 / numBins);
		int homo = 0;
		
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
						coMat[ input[ii * nc + jj] / widthBin ] [ input[ii * nc + jj + 1] / widthBin ] ++; 
					}
					if( jj > l ){
						coMat[ input[ii * nc + jj] / widthBin ] [ input[ii * nc + jj - 1] / widthBin ] ++;
					}
				}
			}
			
			homo = 0;
			for(ii = 0; ii < numBins; ii++){
				for(jj = 0; jj < numBins; jj++){
					homo += coMat[ii][jj] / (1.0 + Math.abs(ii-jj));
				}
			}
			output[i] = homo;
		}
	}
}
