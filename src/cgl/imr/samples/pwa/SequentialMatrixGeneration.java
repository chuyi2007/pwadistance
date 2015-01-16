package cgl.imr.samples.pwa;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

public class SequentialMatrixGeneration implements Runnable{
	SimilarityMatrix swgScoringMatrix;
	@SuppressWarnings("rawtypes")
	SubstitutionMatrix nwScoringMatrix;
	SmithWatermanAligner swgAligner;
	List<AlignedData> swgAds;
	AlignedData swgAd;
	@SuppressWarnings("rawtypes")
	AlignmentData nwAd;
	static int totalSize;
	static List<Sequence> sequences;
	static short[][] score;
	static short[][] scoreA;
	static short[][] scoreB;
	static short[][] lengths;
	static short[][] identicalPairs;
	int row;
	static int gapOpen;
	static int gapExt;
	static String alignerType;
	static int symFlag;
	
	public SequentialMatrixGeneration(int row) throws IOException{
		this.row = row;
		swgScoringMatrix = SimilarityMatrix.getEDNAFULL();
		swgAligner = new SmithWatermanAligner();
		nwScoringMatrix = MatrixUtil.getBlosum62();
	}
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{

			if(alignerType.equals("SWG")){
				Sequence sequenceA = sequences.get(row);
				if(symFlag == 0){
					for(int j = 0; j < totalSize; j++){
						List<AlignedData> ads = swgAligner.align(swgScoringMatrix, -gapOpen, -gapExt, 
								sequences.get(row), sequences.get(j));
						AlignedData ad = ads.get(0);
						score[row][j] = ad.getScore();
						lengths[row][j] = ad.getAlignmentLength();
						identicalPairs[row][j] = ad.getNumberOfIdenticalBasePairs(false);
						int startA = ad.getFirstAlignedSequenceStartOffset();
						int endA = ad.getFirstAlignedSequenceEndOffset();
						int startB = ad.getSecondAlignedSeqeunceStartOffset();
						int endB = ad.getSecondAlignedSeqeunceEndOffset();
						Sequence partialA =	getPartialSequence(startA, endA, sequences.get(row));
						scoreA[row][j] = (short) partialA.getSelfAlignedScore(swgScoringMatrix);
						Sequence partialB =	getPartialSequence(startB, endB, sequences.get(j)); 
						scoreB[row][j] = (short) partialB.getSelfAlignedScore(swgScoringMatrix);
					}
				}
				else{
					for(int j = 0; j <= row; j++){
						List<AlignedData> ads = swgAligner.align(swgScoringMatrix, -gapOpen, -gapExt, 
								sequenceA, sequences.get(j));
						AlignedData ad = ads.get(0);
						score[row][j] = ad.getScore();
						score[j][row] = score[row][j];
						lengths[row][j] = ad.getAlignmentLength();
						lengths[j][row] = lengths[row][j];
						identicalPairs[row][j] = ad.getNumberOfIdenticalBasePairs(false);
						identicalPairs[j][row] = identicalPairs[row][j];
						int startA = ad.getFirstAlignedSequenceStartOffset();
						int endA = ad.getFirstAlignedSequenceEndOffset();
						int startB = ad.getSecondAlignedSeqeunceStartOffset();
						int endB = ad.getSecondAlignedSeqeunceEndOffset();
						Sequence partialA =	getPartialSequence(startA, endA, sequences.get(row));
						scoreA[row][j] = (short) partialA.getSelfAlignedScore(swgScoringMatrix);
						scoreA[j][row] = scoreA[row][j];
						Sequence partialB =	getPartialSequence(startB, endB, sequences.get(j)); 
						scoreB[row][j] = (short) partialB.getSelfAlignedScore(swgScoringMatrix);
						scoreB[j][row] = scoreB[row][j];
					}
				}
			}
			else if (alignerType.equals("NW")){
				ProteinSequence sequenceA = new ProteinSequence(sequences.get(row).toString());

				if(symFlag == 0){
					for(int j = 0; j < totalSize; j++){
						nwAd = BioJavaWrapper.calculateAlignment(sequenceA, 
								new ProteinSequence(sequences.get(j).toString()), 
								(short)gapOpen, (short)gapExt, nwScoringMatrix, DistanceType.PercentIdentity);

						score[row][j] = (short)nwAd.getScore();
						lengths[row][j] = (short)nwAd.getAlignmentLengthExcludingEndGaps();
						identicalPairs[row][j] = (short)nwAd.getNumIdenticals();
						//if(score[row][j] < 0){
						//	System.out.println("WOW!");
						//}
						int startA = nwAd.getFirstAlignedSequenceStartOffset();
						int endA = nwAd.getFirstAlignedSequenceEndOffset();
						int startB = nwAd.getSecondAlignedSequenceStartOffset();
						int endB = nwAd.getSecondAlignedSequenceEndOffset();

						ProteinSequence partialA 
						= new ProteinSequence(sequences.get(row).toString().substring(startA, endA + 1));
						ProteinSequence partialB 
						= new ProteinSequence(sequences.get(j).toString().substring(startB, endB + 1));

						scoreA[row][j] = (short)AlignmentData.getSelfAlignedScore(partialA, nwScoringMatrix);
						scoreB[row][j] = (short)AlignmentData.getSelfAlignedScore(partialB, nwScoringMatrix);
					}
				}
				else{
					for(int j = 0; j <= row; j++){
						nwAd = BioJavaWrapper.calculateAlignment(sequenceA, 
								new ProteinSequence(sequences.get(j).toString()), 
								(short)gapOpen, (short)gapExt, nwScoringMatrix, DistanceType.PercentIdentity);

						score[row][j] = (short)nwAd.getScore();
						lengths[row][j] = (short)nwAd.getAlignmentLengthExcludingEndGaps();
						identicalPairs[row][j] = (short)nwAd.getNumIdenticals();
						//if(score[row][j] < 0){
						//	System.out.println("WOW!");
						//}
						int startA = nwAd.getFirstAlignedSequenceStartOffset();
						int endA = nwAd.getFirstAlignedSequenceEndOffset();
						int startB = nwAd.getSecondAlignedSequenceStartOffset();
						int endB = nwAd.getSecondAlignedSequenceEndOffset();

						ProteinSequence partialA 
						= new ProteinSequence(sequences.get(row).toString().substring(startA, endA + 1));
						ProteinSequence partialB 
						= new ProteinSequence(sequences.get(j).toString().substring(startB, endB + 1));

						scoreA[row][j] = (short)AlignmentData.getSelfAlignedScore(partialA, nwScoringMatrix);
						scoreB[row][j] = (short)AlignmentData.getSelfAlignedScore(partialB, nwScoringMatrix);
						score[j][row] = score[row][j];
						lengths[j][row] = lengths[j][row];
						identicalPairs[j][row] = identicalPairs[row][j];
						scoreA[j][row] = scoreA[row][j];
						scoreB[j][row] = scoreB[row][j];

					}
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
			System.exit(2);
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 7) {
            System.err
                    .println("args:  [input fasta file] " +
                    		"[output matrix file prefix] [aligner type] " +
                    		"[scoring matrix type? pos(position specific)/blo(bloSum62)/edn(ednafull)]" +
                    		"[gap open] [gap extend][symmetric? 0 = no, 1 = yes] [thread Number]");
            System.exit(2);
        }
		String fastaFilePath = args[0];
		String matrixFilePath = args[1];
		alignerType = args[2];
		gapOpen = Integer.parseInt(args[3]);
		gapExt = Integer.parseInt(args[4]);
		symFlag = Integer.parseInt(args[5]);
		int threadNumber = Integer.parseInt(args[6]);
		if(!alignerType.equals("SWG") && !alignerType.equals("NW")){
			System.err.println("The aligner type must be SWG or NW");
			System.exit(2);
		}
		if(!(symFlag == 0 || symFlag == 1)){
			System.err.println("The sym flag must be 0 or 1!");
			System.exit(2);
		}
		singleScoreMatrix(fastaFilePath, matrixFilePath, symFlag, threadNumber);
	}
	
	public static void twoScoreMatrix(String fastaFilePath, 
			String matrixFilePath, 
			String alignerType, int symFlag) throws Exception{
		
		Sequence[] sequences 
		= GenePartition.ConvertSequences(SequenceParser.parse(fastaFilePath, Alphabet.Protein));
		
		int size = sequences.length;
		short[][] score = new short[size][size];
		//short[][] scoreA = new short[size][size];
		//short[][] scoreB = new short[size][size];
		short[][] maxScore = new short[size][size];
		short[][] minScore = new short[size][size];
		short[][] normalScore = new short[size][size];
		short[][] length = new short[size][size];
		short[][] identicalPairs = new short[size][size];


		short gapOpen = (short) 9;
		short gapExt = (short) 1;
		if(symFlag == 0){
			for(int i = 0; i < size; i++)
				for(int j = 0; j < size; j++){

					SubstitutionMatrix<AminoAcidCompound> queryMatrix =
			        MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(sequences[i].getId().toUpperCase(), ".out.ascii");
			        SubstitutionMatrix<AminoAcidCompound> targetMatrix =
			        MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(sequences[j].getId().toUpperCase(), ".out.ascii");
					AlignmentData ad = BioJavaWrapper.calculateAlignment(new ProteinSequence(sequences[i].toString()), 
							new ProteinSequence(sequences[j].toString()), 
							gapOpen, gapExt, queryMatrix, targetMatrix,DistanceType.PercentIdentity);
					score[i][j] = (short)ad.getScore();
					maxScore[i][j] = (short)ad.getMaxScore();
					minScore[i][j] = (short)ad.getMinScore();
					normalScore[i][j] = (short)((1-ad.getNormalizedScore()) * Short.MAX_VALUE);
					length[i][j] = (short)ad.getAlignmentLengthExcludingEndGaps();
					identicalPairs[i][j] = (short)ad.getNumIdenticals();
				}
		}
		else{
			for(int i = 0; i < size; i++)
				for(int j = 0; j <= i; j++){
					SubstitutionMatrix<AminoAcidCompound> queryMatrix =
			        MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(sequences[i].getId().toUpperCase(), ".out.ascii");
			        SubstitutionMatrix<AminoAcidCompound> targetMatrix =
			        MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(sequences[j].getId().toUpperCase(), ".out.ascii");
					AlignmentData ad = BioJavaWrapper.calculateAlignment(new ProteinSequence(sequences[i].toString()), 
							new ProteinSequence(sequences[j].toString()), 
							gapOpen, gapExt, queryMatrix, targetMatrix,DistanceType.PercentIdentity);
					score[i][j] = (short)ad.getScore();
					maxScore[i][j] = (short)ad.getMaxScore();
					minScore[i][j] = (short)ad.getMinScore();
					normalScore[i][j] = (short)((1 - ad.getNormalizedScore()) * Short.MAX_VALUE);
					score[j][i] = score[i][j];
					minScore[j][i] = minScore[i][j];
					maxScore[j][i] = maxScore[i][j];
					normalScore[j][i] = normalScore[i][j];
				}
		}

		DataOutputStream disScore = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "score.bin")));
		
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				disScore.writeShort(score[i][j]);
		
		disScore.close();
		
		DataOutputStream disNormalScore = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "scoreNormal.bin")));
			
			for(int i = 0; i < size; i++)
				for(int j = 0; j < size; j++)
					disNormalScore.writeShort(normalScore[i][j]);
			
			disNormalScore.close();
		
		DataOutputStream disMinScore = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "scoreMin.bin")));
		
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				disMinScore.writeShort(minScore[i][j]);
		disMinScore.close();
		
		DataOutputStream disMaxScore = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "scoreMax.bin")));
		
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				disMaxScore.writeShort(maxScore[i][j]);
		
		disMaxScore.close();
		DataOutputStream disLength = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "length.bin")));
			
			for(int i = 0; i < size; i++)
				for(int j = 0; j < size; j++)
					disLength.writeShort(length[i][j]);
			
			disLength.close();
			DataOutputStream disIdenticalPairs = 
					new DataOutputStream(
							new BufferedOutputStream(
									new FileOutputStream(matrixFilePath + "identicalPairs.bin")));
				
				for(int i = 0; i < size; i++)
					for(int j = 0; j < size; j++)
						disIdenticalPairs.writeShort(identicalPairs[i][j]);
				
				disIdenticalPairs.close();
	}
	
	public static void ScoreForAlignedSequence(String fastaFilePath, 
			String matrixFilePath, 
			String alignerType, int symFlag) throws Exception{
		
		Sequence[] sequences 
		= GenePartition.ConvertSequences(SequenceParser.parse(fastaFilePath, Alphabet.Protein));
		
		int size = sequences.length;
		short[][] score = new short[size][size];
		short[][] normalScore = new short[size][size];
		short[][] length = new short[size][size];
		short[][] identicalPairs = new short[size][size];


		short gapOpen = (short) 9;
		short gapExt = (short) 1;
		if(symFlag == 0){
			for(int i = 0; i < size; i++)
				for(int j = 0; j < size; j++){

					SubstitutionMatrix<AminoAcidCompound> queryMatrix =
			        MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(
			        		sequences[i].getId().toUpperCase(), ".out.ascii");
			        SubstitutionMatrix<AminoAcidCompound> targetMatrix =
			        MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(
			        		sequences[j].getId().toUpperCase(), ".out.ascii");
					AlignmentData ad = BioJavaWrapper.calculateAlignment(new ProteinSequence(sequences[i].toString()), 
							new ProteinSequence(sequences[j].toString()), 
							gapOpen, gapExt, queryMatrix, targetMatrix,DistanceType.PercentIdentity);
					score[i][j] = (short)ad.getScore();
					normalScore[i][j] = (short)((1-ad.getNormalizedScore()) * Short.MAX_VALUE);
					length[i][j] = (short)ad.getAlignmentLengthExcludingEndGaps();
					identicalPairs[i][j] = (short)ad.getNumIdenticals();
				}
		}
		else{
			for(int i = 0; i < size; i++)
				for(int j = 0; j <= i; j++){
					SubstitutionMatrix<AminoAcidCompound> queryMatrix =
			        MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(sequences[i].getId().toUpperCase(), ".out.ascii");
			        SubstitutionMatrix<AminoAcidCompound> targetMatrix =
			        MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(sequences[j].getId().toUpperCase(), ".out.ascii");
					AlignmentData ad = BioJavaWrapper.calculateAlignment(new ProteinSequence(sequences[i].toString()), 
							new ProteinSequence(sequences[j].toString()), 
							gapOpen, gapExt, queryMatrix, targetMatrix,DistanceType.PercentIdentity);
					score[i][j] = (short)ad.getScore();
					normalScore[i][j] = (short)((1 - ad.getNormalizedScore()) * Short.MAX_VALUE);
					score[j][i] = score[i][j];
					normalScore[j][i] = normalScore[i][j];
				}
		}

		DataOutputStream disScore = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath + "score.bin")));
		
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				disScore.writeShort(score[i][j]);
		
		disScore.close();
		
		DataOutputStream disNormalScore = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "scoreNormal.bin")));
			
			for(int i = 0; i < size; i++)
				for(int j = 0; j < size; j++)
					disNormalScore.writeShort(normalScore[i][j]);
			
			disNormalScore.close();
		
		DataOutputStream disLength = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "length.bin")));
			
			for(int i = 0; i < size; i++)
				for(int j = 0; j < size; j++)
					disLength.writeShort(length[i][j]);
			
			disLength.close();
			DataOutputStream disIdenticalPairs = 
					new DataOutputStream(
							new BufferedOutputStream(
									new FileOutputStream(matrixFilePath + "identicalPairs.bin")));
				
				for(int i = 0; i < size; i++)
					for(int j = 0; j < size; j++)
						disIdenticalPairs.writeShort(identicalPairs[i][j]);
				
				disIdenticalPairs.close();
	}	
	
	public static void singleScoreMatrix(String fastaFilePath, 
			String matrixFilePath, int symFlag, int threadNum) throws Exception{
		sequences = SequenceParser.parse(fastaFilePath, Alphabet.Protein);
		totalSize = sequences.size();
		score = new short[totalSize][totalSize];
		scoreA = new short[totalSize][totalSize];
		scoreB = new short[totalSize][totalSize];
		lengths = new short[totalSize][totalSize];
		identicalPairs = new short[totalSize][totalSize];
		
		ExecutorService pool = Executors.newFixedThreadPool(threadNum);
		List<Future> futures = new ArrayList<Future>(totalSize);
		for(int i = 0; i < totalSize; i++)
			futures.add(pool.submit(new SequentialMatrixGeneration(i)));
		for(int i = 0; i < totalSize; i++)
			futures.get(i).get();
		pool.shutdown();
		
		writeToFile(matrixFilePath);
	}
	
	private static void writeToFile(String matrixFilePath) throws IOException{
		DataOutputStream disScore = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "score.bin")));
			
			for(int i = 0; i < totalSize; i++)
				for(int j = 0; j < totalSize; j++)
					disScore.writeShort(score[i][j]);
			
			disScore.close();
			
		DataOutputStream disScoreA = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "scoreA.bin")));
			
			for(int i = 0; i < totalSize; i++)
				for(int j = 0; j < totalSize; j++)
					disScoreA.writeShort(scoreA[i][j]);
			disScoreA.close();
			
			DataOutputStream disScoreB = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "scoreB.bin")));
			
			for(int i = 0; i < totalSize; i++)
				for(int j = 0; j < totalSize; j++)
					disScoreB.writeShort(scoreB[i][j]);
			
			disScoreB.close();
			
			DataOutputStream disLength = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "lengths.bin")));
			
			for(int i = 0; i < totalSize; i++)
				for(int j = 0; j < totalSize; j++)
					disLength.writeShort(lengths[i][j]);
			
			disLength.close();
			
			DataOutputStream disIdenticalPairs = 
				new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(matrixFilePath + "identicalPairs.bin")));
			
			for(int i = 0; i < totalSize; i++)
				for(int j = 0; j < totalSize; j++)
					disIdenticalPairs.writeShort(identicalPairs[i][j]);
			
			disIdenticalPairs.close();
	}
	private static Sequence getPartialSequence(int startIndex, int endIndex, Sequence sequence){
		Sequence partialSequence = null;
			partialSequence = new Sequence(sequence.toString().substring(startIndex, endIndex+1), 
					sequence.getId(), Alphabet.DNA);
		return partialSequence;
	}

}
