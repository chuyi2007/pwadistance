package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ScoreToDistanceCutLowAlignment {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 6) {
            System.err
                    .println("args:  [input score matrix] [input score A matrix] " +
                    		"[input score B matrix] [size] [output matrix file path] [threshold]");
            System.exit(2);
        }
		String inputScorePath = args[0];
		String inputScoreAPath = args[1];
		String inputScoreBPath = args[2];
		int size = Integer.parseInt(args[3]);
		String outputPath = args[4];
		double threshold = Double.parseDouble(args[5]);
		
		double start = System.currentTimeMillis();
		short[] dscores = new short[size];
		DataInputStream dinScore = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(inputScorePath)));
		
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++){
				short tmpScore = dinScore.readShort();
				if(i == j)
					dscores[i] = tmpScore;
			}
		System.out.println("Finished reading score matrix!");
		dinScore = new DataInputStream(
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
				
				if((double)(tmpScoreA + tmpScoreB) > threshold * (double)(dscores[i] + dscores[j])){
					double distanceDouble = 2.0 * (double)(tmpScore) /(double)(dscores[i] + dscores[j]);
				//System.out.println("ad: " + ad.getScore() +  " rowScore[j]" + rowScore[j]);
					short distanceShort = (short)((1 - distanceDouble) * Short.MAX_VALUE);
					dos.writeShort(distanceShort);
				}
				else{
					dos.writeShort(Short.MAX_VALUE);
				}
			}
		dinScore.close();
		dinScoreA.close();
		dinScoreB.close();
		dos.close();
		System.out.println("Total Time: " + (System.currentTimeMillis() - start)/1000.0);
	}

}
