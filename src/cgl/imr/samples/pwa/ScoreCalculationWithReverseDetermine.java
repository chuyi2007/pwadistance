package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

public class ScoreCalculationWithReverseDetermine {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 9) {
            System.err
                    .println("args:  [Input Score Matrix] [Input Score Matrix withReverse]" +
                    		"[Input A Score Matrix] [Input A Score Matrix with Reverse] " +
                    		"[Input B Score Matrix] [Input B Score Matrix with Reverse]" +
                            "[Output Normalized Score Matrix] [Data Size] [Test size]");
            System.exit(2);
        }
		String inputScoreMatrix = args[0];
		String inputScoreReverseMatrix = args[1];
		String inputAMatrix = args[2];
		String inputAReverseMatrix = args[3];
		String inputBMatrix = args[4];
		String inputBReverseMatrix = args[5];
		String outputMatrix = args[6];
		int size = Integer.parseInt(args[7]);
		int testSize = Integer.parseInt(args[8]);
		
		double startTime = System.currentTimeMillis();
		DataInputStream disScore = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputScoreMatrix)));
		DataInputStream disScoreReverse = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputScoreReverseMatrix)));
		DataInputStream disA = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputAMatrix)));
		DataInputStream disAReverse = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputAReverseMatrix)));
		DataInputStream disB = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputBMatrix)));
		DataInputStream disBReverse = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputBReverseMatrix)));
		short[][] score = new short[size][testSize];
		short[][] scoreReverse = new short[size][testSize];
		short[][] A = new short[size][testSize];
		short[][] AReverse = new short[size][testSize];
		short[][] B = new short[size][testSize];
		short[][] BReverse = new short[size][testSize];
		
		Set<Integer> testSet = new HashSet<Integer>();
		for(int i = 0; i < testSize; i++){
			testSet.add(i * (size/testSize));
		}
		
		for(int i = 0; i < size; i++){
			
			
			int testSetCount = 0;
			for(int j = 0; j < size; j++){
				short tmpScore = disScore.readShort();
				short tmpScoreReverse = disScoreReverse.readShort();
				short tmpA = disA.readShort();
				short tmpB = disB.readShort();
				short tmpAReverse = disAReverse.readShort();
				short tmpBReverse = disBReverse.readShort();
				if(testSet.contains(j)){
					score[i][testSetCount] = tmpScore;
					scoreReverse[i][testSetCount] = tmpScoreReverse;
					A[i][testSetCount] = tmpA;
					AReverse[i][testSetCount] = tmpAReverse;
					B[i][testSetCount] = tmpB;
					BReverse[i][testSetCount] = tmpBReverse;
					testSetCount++;
				}
			}
			
		}
		disScore.close();
		disScoreReverse.close();
		disA.close();
		disAReverse.close();
		disB.close();
		disBReverse.close();

		Boolean[] reverseFlag = new Boolean[size];
		Boolean continueFlag = true;
		Boolean[] testSetReverseFlag = new Boolean[testSize];
		for(int i = 0; i < testSize; i++)
			testSetReverseFlag[i] = false;
		int iterationCount = 0;
		while(continueFlag){
			continueFlag = false;
			System.out.println("iteration " + iterationCount + "...");
			int testSetCount = 0;
			for(int i = 0; i < size; i++){
				int reverseCount = 0;
				
				
				for(int j = 0; j < testSize; j++){
					
					if(testSetReverseFlag[j] != true && score[i][j] < scoreReverse[i][j])
						reverseCount++;
					else if(testSetReverseFlag[j] == true && score[i][j] >= scoreReverse[i][j])
						reverseCount++;
				}
				
				if((double)reverseCount/(double)testSize > 0.5){
					reverseFlag[i] = true;
					if(testSet.contains(i)){
						if(testSetReverseFlag[testSetCount] != true)
							continueFlag = true;
						testSetReverseFlag[testSetCount] = true;
					}
				}
				else{
					reverseFlag[i] = false;
					if(testSet.contains(i)){
						if(testSetReverseFlag[testSetCount] != false)
							continueFlag = true;
						testSetReverseFlag[testSetCount] = false;
					}
				}
				
				if(testSet.contains(i)){
					testSetCount++;
				}
			}
			iterationCount++;
		}
		
		disScore = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputScoreMatrix)));
		disScoreReverse = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputScoreReverseMatrix)));
		disA = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputAMatrix)));
		disAReverse = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputAReverseMatrix)));
		disB = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputBMatrix)));
		disBReverse = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputBReverseMatrix)));
		DataOutputStream dos = new DataOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(outputMatrix)));
		
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				double normalizedScore = 0;
				short tmpScore = disScore.readShort();
				short tmpA = disA.readShort();
				short tmpB = disB.readShort();
				short tmpScoreReverse = disScoreReverse.readShort();
				short tmpAReverse = disAReverse.readShort();
				short tmpBReverse = disBReverse.readShort();
				if(i==j)
					dos.writeShort(0);
				else if(reverseFlag[i] == true && reverseFlag[j] == true){
					normalizedScore 
					= 1.0 - 2.0 * (double)tmpScore/(double)(tmpA + tmpB);
					dos.writeShort((short)(normalizedScore * Short.MAX_VALUE));
				}
				else if(reverseFlag[i] == true || reverseFlag[j] == true){
					normalizedScore
					= 1.0 - 2.0 * (double)tmpScoreReverse
					/(double)(tmpAReverse + tmpBReverse);
					dos.writeShort((short)(normalizedScore * Short.MAX_VALUE));
				}
				else{
					normalizedScore 
					= 1.0 - 2.0 * (double)tmpScore/(double)(tmpA + tmpB);
					dos.writeShort((short)(normalizedScore * Short.MAX_VALUE));
				}
				if(normalizedScore < 0)
					System.err.println("This is error with i=" + i + " j="+j);
				
			}
		}
		System.out.println("Total reverse number: " + countBooleanFlag(reverseFlag));
		System.out.println("Execution time: " + ((double)System.currentTimeMillis() - startTime)/1000.0);
		dos.close();
	}
	
	private static int countBooleanFlag(Boolean[] flags){
		int count = 0;
		for(Boolean f : flags){
			if(f)
				count++;
		}
		return count;
	}
	public static void randomSelect(int size){
		
	}

}
