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

public class ScoreToDistanceMinMaxNW {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 6) {
            System.err
                    .println("args:  [input score matrix] [input sequence file] " +
                    		"[size] [Gap open (> 0)] [Gap Extend (> 0)] [output matrix file path]");
            System.exit(2);
        }
		String inputPath = args[0];
		List<Sequence> sequences = SequenceParser.parse(args[1], Alphabet.Protein);
		int size = Integer.parseInt(args[2]);
		int go = -Integer.parseInt(args[3]);
		int ge = -Integer.parseInt(args[4]);
		String outputPath = args[5];
		
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
		DataOutputStream dos = new DataOutputStream(bdos);
		double count = 0;
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++){
				double tmpScore = din.readShort();
				double min = 2 * go + (sequences.get(i).getCount() + sequences.get(j).getCount()) * ge;
				double max = Math.max(dscores[i], dscores[j]);
				double distanceDouble = (tmpScore - min) / (max - min);
				
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
