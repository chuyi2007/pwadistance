package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ScoreToDistanceFixedReverse {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 10) {
            System.err
                    .println("args:  [Input Score Matrix] [Input Score Matrix withReverse]" +
                    		"[Input A Score Matrix] [Input A Score Matrix with Reverse] " +
                    		"[Input B Score Matrix] [Input B Score Matrix with Reverse]" +
                            "[Output Normalized Score Matrix] [Data Size] [StartReverse Index] [EndReverse Index]");
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
		int startIndex = Integer.parseInt(args[8]);
		int endIndex = Integer.parseInt(args[9]);
		
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
				else if(i >= startIndex && i <= endIndex && j >= startIndex && j <= endIndex){
					normalizedScore 
					= 1.0 - 2.0 * (double)tmpScore/(double)(tmpA + tmpB);
					dos.writeShort((short)(normalizedScore * Short.MAX_VALUE));
				}
				else if(i >= startIndex && i <= endIndex || j >= startIndex && j <= endIndex){
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
		System.out.println("Execution time: " + ((double)System.currentTimeMillis() - startTime)/1000.0);
		dos.close();
	}

}
