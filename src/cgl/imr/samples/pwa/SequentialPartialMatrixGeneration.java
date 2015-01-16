package cgl.imr.samples.pwa;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;

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

public class SequentialPartialMatrixGeneration {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 5) {
            System.err
                    .println("args:  [input fasta file 1] [input fasta file 2] " +
                    		"[output matrix file] [aligner type] [Index Number]");
            System.exit(2);
        }
		String fastaFilePath1 = args[0];
		String fastaFilePath2 = args[1];
		String matrixFilePath = args[2];
		String alignerType = args[3];
		int indexNum = Integer.parseInt(args[4]);
		//twoScoreMatrix(fastaFilePath1, fastaFilePath2, matrixFilePath, alignerType, indexNum);
		oneScoreMatrix(fastaFilePath1, fastaFilePath2, matrixFilePath, alignerType);
	}
	
	public static void twoScoreMatrix(String fastaFilePath1, String fastaFilePath2,
			String matrixFilePath, 
			String alignerType, int index) throws Exception{
		
		Sequence[] sequences1
		= GenePartition.ConvertSequences(SequenceParser.parse(fastaFilePath1, Alphabet.Protein));
		Sequence[] sequences2
		= GenePartition.ConvertSequences(SequenceParser.parse(fastaFilePath2, Alphabet.Protein));
		
		int row = sequences1.length;
		int col = sequences2.length;
		short[][] score = new short[row][col];
		short[][] maxScore = new short[row][col];
		short[][] minScore = new short[row][col];
		short[][] normalScore = new short[row][col];
		short[][] length = new short[row][col];
		short[][] identicalPairs = new short[row][col];


		short gapOpen = (short) 9;
		short gapExt = (short) 1;
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++){

				SubstitutionMatrix<AminoAcidCompound> queryMatrix =
						MatrixUtil.getBlosum62();
				SubstitutionMatrix<AminoAcidCompound> targetMatrix =
						MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(sequences2[j].getId().toUpperCase(), ".out.ascii");
				AlignmentData ad = BioJavaWrapper.calculateAlignment(new ProteinSequence(sequences1[i].toString()), 
						new ProteinSequence(sequences2[j].toString()), 
						gapOpen, gapExt, queryMatrix, targetMatrix,DistanceType.PercentIdentity);
				score[i][j] = (short)ad.getScore();
				maxScore[i][j] = (short)ad.getMaxScore();
				minScore[i][j] = (short)ad.getMinScore();
				normalScore[i][j] = (short)((1-ad.getNormalizedScore()) * Short.MAX_VALUE);
				length[i][j] = (short)ad.getAlignmentLengthExcludingEndGaps();
				identicalPairs[i][j] = (short)ad.getNumIdenticals();
			}

		DataOutputStream disScore = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "score_" + index)));
		
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				disScore.writeShort(score[i][j]);
		
		disScore.close();
		
		DataOutputStream disNormalScore = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "scoreNormal_" + index)));
			
			for(int i = 0; i < row; i++)
				for(int j = 0; j < col; j++)
					disNormalScore.writeShort(normalScore[i][j]);
			
			disNormalScore.close();
		
		DataOutputStream disMinScore = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "scoreMin_" + index)));
		
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				disMinScore.writeShort(minScore[i][j]);
		disMinScore.close();
		
		DataOutputStream disMaxScore = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "scoreMax_" + index)));
		
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				disMaxScore.writeShort(maxScore[i][j]);
		
		disMaxScore.close();
		DataOutputStream disLength = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "lengths_" + index)));
			
			for(int i = 0; i < row; i++)
				for(int j = 0; j < col; j++)
					disLength.writeShort(length[i][j]);
			
			disLength.close();
			DataOutputStream disIdenticalPairs = 
					new DataOutputStream(
							new BufferedOutputStream(
									new FileOutputStream(matrixFilePath + "identicalPairs_" + index)));
				
				for(int i = 0; i < row; i++)
					for(int j = 0; j < col; j++)
						disIdenticalPairs.writeShort(identicalPairs[i][j]);
				
				disIdenticalPairs.close();
	}
	
	
	public static void oneScoreMatrix(String fasta1, String fasta2, 
			String matrixFilePath, String alignerType) throws Exception{
		if(!alignerType.equals("SWG") && !alignerType.equals("NW")){
			System.err.println("The aligner type must be SWG or NW");
			System.exit(2);
		}
		
		Sequence[] seq1 
		= GenePartition.ConvertSequences(SequenceParser.parse(fasta1, Alphabet.Protein));

		Sequence[] seq2
		= GenePartition.ConvertSequences(SequenceParser.parse(fasta2, Alphabet.Protein));
		int row = seq1.length;
		int col = seq2.length;
		
		short[][] score = new short[row][col];
		short[][] length = new short[row][col];
		short[][] identicalPairs = new short[row][col];
		short[][] scoreA = new short[row][col];
		short[][] scoreB = new short[row][col];
		
		if(alignerType.equals("SWG")){			
			SimilarityMatrix ednafull = SimilarityMatrix.getEDNAFULL();
			int gapOpen = (short) 16;
			int gapExt = (short) 4;
			SmithWatermanAligner aligner = new SmithWatermanAligner();
			for(int i = 0; i < row; i++)
				for(int j = 0; j < col; j++){
					List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
							seq1[i], seq2[j]);
					AlignedData ad = ads.get(0);
					score[i][j] = ad.getScore();
					length[i][j] = ad.getAlignmentLength();
					identicalPairs[i][j] = ad.getNumberOfIdenticalBasePairs(false);
				}
		}
		else if (alignerType.equals("NW")){
			
			SubstitutionMatrix scoringMatrix = MatrixUtil.getBlosum62();
			
			short gapOpen = (short) 9;
			short gapExt = (short) 1;
			for(int i = 0; i < row; i++)
				for(int j = 0; j < col; j++){
					AlignmentData ad = BioJavaWrapper.calculateAlignment(new ProteinSequence(seq1[i].toString()), 
							new ProteinSequence(seq2[j].toString()), 
							gapOpen, gapExt, scoringMatrix, DistanceType.PercentIdentity);
					
					score[i][j] = (short)ad.getScore();
					length[i][j] = (short)ad.getAlignmentLengthExcludingEndGaps();
					identicalPairs[i][j] = (short)ad.getNumIdenticals();
				}
		}
			
		DataOutputStream disScore = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "score.bin")));
		
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				disScore.writeShort(score[i][j]);
		
		disScore.close();
		
		DataOutputStream disScoreA = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "scoreA.bin")));
		
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				disScoreA.writeShort(scoreA[i][j]);
		disScoreA.close();
		
		DataOutputStream disScoreB = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "scoreB.bin")));
		
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				disScoreB.writeShort(scoreB[i][j]);
		
		disScoreB.close();
		
		DataOutputStream disLength = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "lengths.bin")));
		
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				disLength.writeShort(length[i][j]);
		
		disLength.close();
		
		DataOutputStream disIdenticalPairs = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "identicalPairs.bin")));
		
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++)
				disIdenticalPairs.writeShort(identicalPairs[i][j]);
		
		disIdenticalPairs.close();
	}

}
