package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ScoreToDistanceNew {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 5) {
            System.err
                    .println("args:  [input score matrix] [input score A matrix] " +
                    		"[input score B matrix] [size] [output matrix file path]");
            System.exit(2);
        }
		String inputScorePath = args[0];
		String inputScoreAPath = args[1];
		String inputScoreBPath = args[2];
		int size = Integer.parseInt(args[3]);
		String outputPath = args[4];
		
		double start = System.currentTimeMillis();
		DataInputStream dinScore = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(inputScorePath)));
		DataInputStream dinScoreA = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(inputScoreAPath)));
		DataInputStream dinScoreB = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(inputScoreBPath)));
		
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(outputPath));
		DataOutputStream dos = new DataOutputStream(bdos);
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++){
				short tmpScore = dinScore.readShort();
				short tmpScoreA = dinScoreA.readShort();
				short tmpScoreB = dinScoreB.readShort();
				
				double distanceDouble = 2.0 * (double)(tmpScore) /(double)(tmpScoreA + tmpScoreB);
				short distanceShort = (short)((1 - distanceDouble) * Short.MAX_VALUE);
				dos.writeShort(distanceShort);
				
			}
		dinScore.close();
		dinScoreA.close();
		dinScoreB.close();
		dos.close();
		System.out.println("Total Time: " + (System.currentTimeMillis() - start)/1000.0);
	}

}
