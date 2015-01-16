package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;

public class ScoreToDistanceMinMaxSWGGlobal {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 4) {
            System.err
                    .println("args:  [input score matrix] [size] [output matrix file path][max/min/average?(0, 1, 2)]");
            System.exit(2);
        }
		String inputPath = args[0];
		int size = Integer.parseInt(args[1]);
		String outputPath = args[2];
		int choice = Integer.parseInt(args[3]);
		if(choice < 0 || choice > 2)
		{
			System.out.println("Choice must be 0, 1 or 2");
			System.exit(1);
		}
		
		double start = System.currentTimeMillis();
		short[] dscores = new short[size];
		BufferedInputStream bdis = new BufferedInputStream(new FileInputStream(inputPath));
		DataInputStream din = new DataInputStream(bdis);
		
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++){
				
				short tmpScore = din.readShort();
				if(i == j)
					dscores[i] = tmpScore;
			}
		System.out.println("Finished reading score matrix!");
		bdis = new BufferedInputStream(new FileInputStream(inputPath));
		din = new DataInputStream(bdis);
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(outputPath));
		DataOutputStream dos = new DataOutputStream(bdos);;
		double count = 0;
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++){
				double tmpScore = din.readShort();
				double deno;
				if(choice == 0)
					deno= Math.max(dscores[i], dscores[j]);
				else if(choice == 1)
					deno= Math.min(dscores[i], dscores[j]);
				else
					deno= (dscores[i] + dscores[j]) / 2.0;
					
				double distanceDouble = (tmpScore) / (deno);
				
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
		dos.close();
		System.out.println("Total Time: " + (System.currentTimeMillis() - start)/1000.0);
	}

}
