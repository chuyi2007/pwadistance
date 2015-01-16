package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MatrixComparitor {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 5) {
            System.err
                    .println("args:  [input matrix path 1] [input matrix path 2] [row] [col] [threshold]");
            System.exit(2);
        }
        //String partitionFile = args[0];
		double startTime = System.currentTimeMillis();
        String inputMatrix1 = args[0];
        String inputMatrix2 = args[1];
        int row = Integer.parseInt(args[2]);
        int col = Integer.parseInt(args[3]);
        double threshold = Double.parseDouble(args[4]);
        
        short shortThreshold = (short)(Short.MAX_VALUE * threshold);
        BufferedInputStream bin1 = new BufferedInputStream(new FileInputStream(
        		inputMatrix1));
		DataInputStream din1 = new DataInputStream(bin1);
		BufferedInputStream bin2 = new BufferedInputStream(new FileInputStream(
				inputMatrix2));
		DataInputStream din2 = new DataInputStream(bin2);
		double sum1 = 0, sum2 = 0;
		double count1 = 0, count2 = 0;
		short[][] matrix1 = new short[row][col];
		short[][] matrix2 = new short[row][col];
		for(int i = 0; i < row; i++){
			for(int j = 0; j < col; j++){
				short tmp1 = din1.readShort();
				short tmp2 = din2.readShort();
				if(tmp1 <= shortThreshold){
				matrix1[i][j] = tmp1;
				matrix2[i][j] = tmp2;
				if(tmp1!=tmp2)
					//System.out.println(i + " " +
					//		 + j + " tmp1: " + tmp1 + " tmp2: " + tmp2);
				
					sum1 += (double)tmp1/(double)Short.MAX_VALUE;
					count1++;
				}
				if(tmp2 <= shortThreshold){
					sum2 += (double)tmp2/(double)Short.MAX_VALUE;
					count2++;
				}
			}
		}
		din1.close();
		din2.close();
		bin1.close();
		bin2.close();
		bin1 = new BufferedInputStream(new FileInputStream(
				inputMatrix1));
		din1 = new DataInputStream(bin1);
		bin2 = new BufferedInputStream(new FileInputStream(
				inputMatrix2));
		din2 = new DataInputStream(bin2);
		double mean1 = sum1/count1;
		double mean2 = sum2/count2;
		System.out.println("The distance passes cut for matrix 1 is: " + count1);
		System.out.println("The distance passes cut for matrix 2 is: " + count2);
		System.out.println("The mean for matrix 1 is: " + mean1);
		System.out.println("The mean for matrix 2 is: " + mean2);
		
		getVariance(inputMatrix1, inputMatrix2, mean1, mean2, row,col, shortThreshold);
		
		sum1 = 0;
		sum2 = 0;
		double denominator = 0;
		for(int i = 0; i < row; i++){
			for(int j = 0; j < col; j++){
				short tmp1 = din1.readShort();
				short tmp2 = din2.readShort();
				if(tmp1 < shortThreshold && tmp2 < shortThreshold){
					double x = (mean1 - (double)tmp1/(double)Short.MAX_VALUE);
					double y = (mean2 - (double)tmp2/(double)Short.MAX_VALUE);
					sum1 += x * x;
					sum2 += y * y;
					denominator += x * y;
				}
				
			}
		}
		double sum = Math.sqrt(sum1*sum2);
		double correlation = denominator / (sum);
		System.out.println("The correlation is: " + correlation);
		double endTime = System.currentTimeMillis();
		System.out.println("The total time is: " + (endTime - startTime)/1000.0);
	}
	
	public static void getVariance(String matrixFile1, String matrixFile2, 
			double mean1, double mean2, int row, int col, short shortThreshold){
    	try{
    		BufferedInputStream bin1 = new BufferedInputStream(new FileInputStream(
    				matrixFile1));
    		DataInputStream din1 = new DataInputStream(bin1);
    		BufferedInputStream bin2 = new BufferedInputStream(new FileInputStream(
    				matrixFile1));
    		DataInputStream din2 = new DataInputStream(bin2);
    		double sum1 = 0, sum2 = 0;
    		double count1 = 0, count2 = 0;
    		//short threshold = (short)(cut * (double) Short.MAX_VALUE);
    		for(int i = 0; i < row; i++){
    			for(int j = 0; j < col; j++){
    				short tmp1 = din1.readShort();
    				short tmp2 = din2.readShort();
    				if(tmp1 <= shortThreshold){
    				double tmpVal1 = (double)tmp1/(double)Short.MAX_VALUE;
    				
    				sum1 += (tmpVal1 - mean1) * (tmpVal1 - mean1);
    				count1++;
    				}
    				
    				if(tmp2 <= shortThreshold){

    				double tmpVal2 = (double)tmp2/(double)Short.MAX_VALUE;
    				sum2 += (tmpVal2 - mean2) * (tmpVal2 - mean2);
    				count2++;
    				}
    				
    			}
    		}
    		din1.close();
    		bin1.close();
    		din2.close();
    		bin2.close();
    		System.out.println("The std for matrix 1 is: " + sum1/count1);
			System.out.println("The std for matrix 2 is: " + sum2/count2);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }

}
