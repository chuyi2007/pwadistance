package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;

import edu.indiana.salsahpc.AlignmentData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.BioJavaWrapper;
import edu.indiana.salsahpc.DistanceType;
import edu.indiana.salsahpc.MatrixNotFoundException;
import edu.indiana.salsahpc.MatrixUtil;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;

public class ScoreToDistancePartialMinMaxNW {

	public static void main(String[] args) throws IOException, MatrixNotFoundException {
		// TODO Auto-generated method stub
		if (args.length != 7) {
            System.err
                    .println("args:  [input score matrix] [input sample sequence file] [input out-sample sequence file]" +
                    		"[Gap open (> 0)] [Gap Extend (> 0)] [output matrix file path][type]");
            System.exit(2);
        }
		String inputPath = args[0];
		List<Sequence> sampleSequences = SequenceParser.parse(args[1], Alphabet.Protein);
		List<Sequence> outSampleSequences = SequenceParser.parse(args[2], Alphabet.Protein);
		int go = -Integer.parseInt(args[3]);
		int ge = -Integer.parseInt(args[4]);
		String outputPath = args[5];
		String type = args[6];
		if(!(type.equals("blo")||type.equals("ps")))
		{
			System.out.println("type must be blo or ps!");
			System.exit(1);
		}
		double start = System.currentTimeMillis();
		short[] sampleScores = new short[sampleSequences.size()];
		short[] outSampleScores = new short[outSampleSequences.size()];
		SubstitutionMatrix<AminoAcidCompound> scoreMatrix = MatrixUtil.getBlosum62();;
		for(int i = 0; i < outSampleScores.length; ++i){
			AlignmentData ad = BioJavaWrapper.
					calculateAlignment(
							new ProteinSequence(outSampleSequences.get(i).toString()), 
							new ProteinSequence(outSampleSequences.get(i).toString()), 
							(short)9, (short)1, scoreMatrix,scoreMatrix, DistanceType.PercentIdentity);
			outSampleScores[i] = (short)ad.getScore();
		}
		for(int j = 0; j < sampleScores.length; ++j){
			if(type.equals("ps"))
			scoreMatrix =
		        MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(
		        		sampleSequences.get(j).getId().substring(1).toUpperCase(), ".out.ascii");
			AlignmentData ad = BioJavaWrapper.
					calculateAlignment(
							new ProteinSequence(sampleSequences.get(j).toString()), 
							new ProteinSequence(sampleSequences.get(j).toString()), 
							(short)9, (short)1, scoreMatrix,scoreMatrix, DistanceType.PercentIdentity);
			sampleScores[j] = (short)ad.getScore();
		}
		
		DataInputStream din = new DataInputStream(
				new BufferedInputStream(new FileInputStream(inputPath)));
		
		DataOutputStream dos = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputPath)));
		double count = 0;
		for(int i = 0; i < outSampleScores.length; i++)
			for(int j = 0; j < sampleScores.length; j++){
				double tmpScore = din.readShort();
				double min = 2 * go + (outSampleSequences.get(i).getCount() + 
						sampleSequences.get(j).getCount()) * ge;
				double max = Math.max(outSampleScores[i], sampleScores[j]);
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
