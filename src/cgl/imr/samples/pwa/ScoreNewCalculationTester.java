package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

public class ScoreNewCalculationTester {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 6) {
            System.err
                    .println("args:  [Input Score Matrix] " +
                    		"[Input A Score Matrix] " +
                    		"[Input B Score Matrix] " +
                            "[Output Normalized Score Matrix] [Row Size] [Column Size]");
            System.exit(2);
        }
		String inputScoreMatrix = args[0];
		String inputAMatrix = args[1];
		String inputBMatrix = args[2];
		String outputMatrix = args[3];
		int rowSize = Integer.parseInt(args[4]);
		int colSize = Integer.parseInt(args[5]);
		
		double startTime = System.currentTimeMillis();
		DataInputStream disScore = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputScoreMatrix)));
		DataInputStream disA = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputAMatrix)));
		DataInputStream disB = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputBMatrix)));
		//short[][] score = new short[rowSize][colSize];
		//short[][] A = new short[rowSize][colSize];
		//short[][] B = new short[rowSize][colSize];
		
		double zeroCount = 0;
		double zeroScoreSum = 0;
		int largestZeroScore = -Integer.MAX_VALUE;
		int smallestZeroScore = Integer.MAX_VALUE;
		for(int i = 0; i < rowSize; i++){
			for(int j = 0; j < colSize; j++){
				short tmpScore = disScore.readShort();
				short tmpA = disA.readShort();
				short tmpB = disB.readShort();
				if(2 * tmpScore == tmpA + tmpB && i != j){
					zeroCount++;
					zeroScoreSum += tmpScore;
					if(largestZeroScore < tmpScore)
						largestZeroScore = tmpScore;
					if(smallestZeroScore > tmpScore)
						smallestZeroScore = tmpScore;
					
				}
			}
			
		}
		System.out.println("Average Zero Score: " + zeroScoreSum/zeroCount);
		System.out.println("Largest Zero Score: " + largestZeroScore);
		System.out.println("Smallest Zero Score: " + smallestZeroScore);
		disScore.close();
		disA.close();
		disB.close();

	}

}
