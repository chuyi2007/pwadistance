package cgl.imr.samples.pwa;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;

import cgl.imr.samples.pwa.util.GenePartition;

import edu.indiana.salsahpc.AlignedData;
import edu.indiana.salsahpc.AlignmentData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.BioJavaWrapper;
import edu.indiana.salsahpc.DistanceType;
import edu.indiana.salsahpc.MatrixUtil;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;
import edu.indiana.salsahpc.SimilarityMatrix;
import edu.indiana.salsahpc.SmithWatermanAligner;

public class SequentialRectangleMatrixGeneration {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 4) {
            System.err
                    .println("args:  [input fasta1 file] [input fasta2 file] [output matrix file] [aligner type]");
            System.exit(2);
        }
		String fasta1FilePath = args[0];
		String fasta2FilePath = args[1];
		String matrixFilePath = args[2];
		String alignerType = args[3];
		
		if(!alignerType.equals("SWG") && !alignerType.equals("NW")){
			System.err.println("The aligner type must be SWG or NW");
			System.exit(2);
		}
		
		Sequence[] sequences1
		= GenePartition.ConvertSequences(SequenceParser.parse(fasta1FilePath, Alphabet.Protein));
		Sequence[] sequences2
		= GenePartition.ConvertSequences(SequenceParser.parse(fasta2FilePath, Alphabet.Protein));
		
		int size1 = sequences1.length;
		int size2 = sequences2.length;
		short[][] score = new short[size1][size2];
		short[][] scoreA = new short[size1][size2];
		short[][] scoreB = new short[size1][size2];
		short[][] length = new short[size1][size2];
		short[][] identicalPairs = new short[size1][size2];
		
		if(alignerType.equals("SWG")){			
			SimilarityMatrix ednafull = SimilarityMatrix.getEDNAFULL();
			int gapOpen = (short) 16;
			int gapExt = (short) 4;
			SmithWatermanAligner aligner = new SmithWatermanAligner();
			for(int i = 0; i < size1; i++)
				for(int j = 0; j < size2; j++){
					List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
							sequences1[i], sequences2[j]);
					AlignedData ad = ads.get(0);
					score[i][j] = ad.getScore();
				}
		}
		else if (alignerType.equals("NW")){
			
			SubstitutionMatrix scoringMatrix = MatrixUtil.getBlosum62();
			
			short gapOpen = (short) 9;
			short gapExt = (short) 1;
			for(int i = 0; i < size1; i++)
				for(int j = 0; j < size2; j++){
					AlignmentData ad = BioJavaWrapper.calculateAlignment(new ProteinSequence(sequences1[i].toString()), 
							new ProteinSequence(sequences2[j].toString()), 
							gapOpen, gapExt, scoringMatrix, DistanceType.PercentIdentity);
					
					score[i][j] = (short)ad.getScore();
					length[i][j] = (short)ad.getAlignmentLengthExcludingEndGaps();
					identicalPairs[i][j] = (short)ad.getNumIdenticals();
					if(score[i][j] < 0){
						System.out.println("WOW!");
					}
					int startA = ad.getFirstAlignedSequenceStartOffset();
					int endA = ad.getFirstAlignedSequenceEndOffset();
					int startB = ad.getSecondAlignedSequenceStartOffset();
					int endB = ad.getSecondAlignedSequenceEndOffset();
					
					ProteinSequence partialA 
					= new ProteinSequence(sequences1[i].toString().substring(startA, endA + 1));
					ProteinSequence partialB 
					= new ProteinSequence(sequences2[j].toString().substring(startB, endB + 1));
					
					scoreA[i][j] = (short)AlignmentData.getSelfAlignedScore(partialA, scoringMatrix);
					scoreB[i][j] = (short)AlignmentData.getSelfAlignedScore(partialB, scoringMatrix);
					
				}
			}
		DataOutputStream disScore = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "score.bin")));
		
		for(int i = 0; i < size1; i++)
			for(int j = 0; j < size2; j++)
				disScore.writeShort(score[i][j]);
		
		disScore.close();
		
		DataOutputStream disScoreA = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "scoreA.bin")));
		
		for(int i = 0; i < size1; i++)
			for(int j = 0; j < size2; j++)
				disScoreA.writeShort(scoreA[i][j]);
		disScoreA.close();
		
		DataOutputStream disScoreB = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "scoreB.bin")));
		
		for(int i = 0; i < size1; i++)
			for(int j = 0; j < size2; j++)
				disScoreB.writeShort(scoreB[i][j]);
		
		disScoreB.close();
		
		DataOutputStream disLength = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "length.bin")));
		
		for(int i = 0; i < size1; i++)
			for(int j = 0; j < size2; j++)
				disLength.writeShort(length[i][j]);
		
		disLength.close();
		
		DataOutputStream disIdenticalPairs = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "identicalPairs.bin")));
		
		for(int i = 0; i < size1; i++)
			for(int j = 0; j < size2; j++)
				disIdenticalPairs.writeShort(identicalPairs[i][j]);
		
		disIdenticalPairs.close();
	}

}
