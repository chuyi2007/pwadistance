package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class PartialMatrixComparitor {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 5) {
            System.err
                    .println("args:  [input matrix path 1] [size]" +
                    		" [input matrix path 2] [start index 2] [end index 2]");
            System.exit(2);
        }
        //String partitionFile = args[0];
		double startTime = System.currentTimeMillis();
        String inputMatrix1 = args[0];
        int size = Integer.parseInt(args[1]);
        String inputMatrix2 = args[2];
        int startIndex2 = Integer.parseInt(args[3]);
        int endIndex2 = Integer.parseInt(args[4]);
        
        if(size != endIndex2 - startIndex2 + 1)
        {
        	System.err.println("The size is not equal!");
        	System.exit(1);
        }
        
        BufferedInputStream bin1 = new BufferedInputStream(new FileInputStream(
        		inputMatrix1));
		DataInputStream din1 = new DataInputStream(bin1);
		BufferedInputStream bin2 = new BufferedInputStream(new FileInputStream(
				inputMatrix2));
		DataInputStream din2 = new DataInputStream(bin2);
		double sum1 = 0, sum2 = 0;
		double count = 0;
		for(int i = 0; i <= endIndex2; i++){
			for(int j = 0; j <= endIndex2; j++){
				if(i < startIndex2 || j < startIndex2)
					din2.readShort();
				else{
					short tmp1 = din1.readShort();
					short tmp2 = din2.readShort();
					sum1 += (double)tmp1/(double)Short.MAX_VALUE;
					sum2 += (double)tmp2/(double)Short.MAX_VALUE;
					count++;
					if(tmp1 != tmp2)
						System.out.println("Not equal! i: " + i + " j: " + j);
				}
			}
		}
		din1.close();
		din2.close();
		bin1.close();
		bin2.close();
		double mean1 = sum1/count;
		double mean2 = sum2/count;
		System.out.println("The distance passes cut is: " + count);
		System.out.println("The mean for matrix 1 is: " + mean1);
		System.out.println("The mean for matrix 2 is: " + mean2);
		double endTime = System.currentTimeMillis();
		System.out.println("The total time is: " + (endTime - startTime)/1000.0);
	}
	
	public static void getVariance(String matrixFile1, String matrixFile2, double mean1, double mean2, int size){
    	try{
    		BufferedInputStream bin1 = new BufferedInputStream(new FileInputStream(
    				matrixFile1));
    		DataInputStream din1 = new DataInputStream(bin1);
    		BufferedInputStream bin2 = new BufferedInputStream(new FileInputStream(
    				matrixFile1));
    		DataInputStream din2 = new DataInputStream(bin2);
    		double sum1 = 0, sum2 = 0;
    		double count = 0;
    		//short threshold = (short)(cut * (double) Short.MAX_VALUE);
    		for(int i = 0; i < size; i++){
    			for(int j = 0; j < size; j++){
    				short tmp1 = din1.readShort();
    				short tmp2 = din2.readShort();
    				double tmpVal1 = (double)tmp1/(double)Short.MAX_VALUE;
    				double tmpVal2 = (double)tmp2/(double)Short.MAX_VALUE;
    				
    				sum1 += (tmpVal1 - mean1) * (tmpVal1 - mean1);
    				sum2 += (tmpVal2 - mean2) * (tmpVal2 - mean2);
    				count++;
    				
    			}
    		}
    		din1.close();
    		bin1.close();
    		din2.close();
    		bin2.close();
    		System.out.println("The std for matrix 1 is: " + sum1/count);
			System.out.println("The std for matrix 2 is: " + sum2/count);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }

}
