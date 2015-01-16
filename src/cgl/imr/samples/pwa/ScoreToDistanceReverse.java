package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScoreToDistanceReverse {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 4) {
            System.err
                    .println("args:  [input score matrix] [input score reversed matrix] [size] [output matrix file path]");
            System.exit(2);
        }
		String inputPath = args[0];
		String inputReversePath = args[1];
		int size = Integer.parseInt(args[2]);
		String outputPath = args[3];
		double start = System.currentTimeMillis();
		int[] dscores = new int[size];
		BufferedInputStream bdis = new BufferedInputStream(new FileInputStream(inputPath));
		DataInputStream din = new DataInputStream(bdis);

		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++){
				
				int tmpScore = din.readInt();
				if(i == j)
					dscores[i] = tmpScore;
			}
		System.out.println("Finished reading score matrix!");
		bdis = new BufferedInputStream(new FileInputStream(inputPath));
		din = new DataInputStream(bdis);

		BufferedInputStream bdisReverse = new BufferedInputStream(new FileInputStream(inputReversePath));
		DataInputStream dinReverse = new DataInputStream(bdisReverse);
		
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(outputPath));
		DataOutputStream dos = new DataOutputStream(bdos);
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++){
				int tmpScore = din.readInt();
				int tmpScoreReverse = dinReverse.readInt();
				if(tmpScore < tmpScoreReverse)
					tmpScore = tmpScoreReverse;
				double distanceDouble = 2.0 * (double)(tmpScore) /(double)(dscores[i] + dscores[j]);
				//System.out.println("ad: " + ad.getScore() +  " rowScore[j]" + rowScore[j]);
				short distanceShort = (short)((1 - distanceDouble) * Short.MAX_VALUE);
				dos.writeShort(distanceShort);
			}
		din.close();
		dos.close();
		System.out.println("Total Time: " + (System.currentTimeMillis() - start)/1000.0);
	}
}
