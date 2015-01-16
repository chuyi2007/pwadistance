package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScoreToDistanceMinMaxSWG {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 7) {
            System.err
                    .println("args:  [input score matrix] [input score A matrix]" +
                    		"[input score B matrix] [row] [col] [output matrix file path] [max/min/average?(0, 1, 2)]");
            System.exit(2);
        }
		String inputPath = args[0];
		String inputAPath = args[1];
		String inputBPath = args[2];
		int row = Integer.parseInt(args[3]);
		int col = Integer.parseInt(args[4]);
		String outputPath = args[5];
		int choice = Integer.parseInt(args[6]);
		if(choice < 0 || choice > 2)
		{
			System.out.println("Choice must be 0, 1 or 2");
			System.exit(1);
		}
		double start = System.currentTimeMillis();
		
		DataInputStream din = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(inputPath)));
		DataInputStream dinA = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(inputAPath)));
		DataInputStream dinB = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(inputBPath)));
		
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(outputPath));
		DataOutputStream dos = new DataOutputStream(bdos);
		double count = 0;
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++){
				double tmpScore = din.readShort();
				double tmpScoreA = dinA.readShort();
				double tmpScoreB = dinB.readShort();
				double deno;
				if(choice == 0)
					deno= Math.max(tmpScoreA, tmpScoreB);
				else if(choice == 1)
					deno = Math.min(tmpScoreA, tmpScoreB);
				else
					deno = (tmpScoreA + tmpScoreB)/2.0;
				
				double distanceDouble = tmpScore / deno;
				
				//double distanceDouble = 2.0 * (double)(tmpScore) /(double)(dscores[i] + dscores[j]);
				if(distanceDouble < 0){
					distanceDouble = 0;
					count++;
				}
				//System.out.println("ad: " + ad.getScore() +  " rowScore[j]" + rowScore[j]);
				short distanceShort = (short)((1 - distanceDouble) * Short.MAX_VALUE);
				dos.writeShort(distanceShort);
			}
		System.out.println("Total value equals 1: " + count);
		din.close();
		dinA.close();
		dinB.close();
		dos.close();
		System.out.println("Total Time: " + (System.currentTimeMillis() - start)/1000.0);
	}

}
