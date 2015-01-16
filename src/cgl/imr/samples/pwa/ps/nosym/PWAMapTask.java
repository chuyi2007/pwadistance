package cgl.imr.samples.pwa.ps.nosym;

import cgl.imr.base.*;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.MapperConf;
import cgl.imr.samples.pwa.util.GenePartition;
import cgl.imr.samples.pwa.util.SequenceAlignment;
import cgl.imr.types.BytesValue;
import cgl.imr.types.StringKey;
import edu.indiana.salsahpc.AlignedData;
import edu.indiana.salsahpc.AlignmentData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.BioJavaWrapper;
import edu.indiana.salsahpc.DistanceType;
import edu.indiana.salsahpc.MatrixNotFoundException;
import edu.indiana.salsahpc.MatrixUtil;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;
import edu.indiana.salsahpc.SimilarityMatrix;
import edu.indiana.salsahpc.SmithWatermanAligner;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;

/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 * @author Saliya Ekanayake (sekanaya at cs dot indiana dot edu)
 */

public class PWAMapTask implements MapTask {

	private String rowDataDir;
	private String rowGeneBlockPrefix;
	private String colDataDir;
	private String colGeneBlockPrefix;

	private short gapOpen, gapExt;

	private String type;
	private String matrixType;
	private String seqType;
	//FileData fileData;

	@Override
	public void close() throws TwisterException {

	}

	@Override
	public void configure(JobConf jobConf, MapperConf mapConf)
	throws TwisterException {

		rowDataDir = jobConf.getProperty("rowDataDir");
		rowGeneBlockPrefix = jobConf.getProperty("rowGeneBlockPrefix");

		colDataDir = jobConf.getProperty("colDataDir");
		colGeneBlockPrefix = jobConf.getProperty("colGeneBlockPrefix");

		gapOpen = (short) 16;
		gapExt = (short) 4;
		type = jobConf.getProperty("Type");
		matrixType = jobConf.getProperty("MatrixType");
		seqType = jobConf.getProperty("SeqType");
		//fileData = (FileData) mapConf.getDataPartition();
		
	}

	@Override
	public void map(MapOutputCollector collector, Key key, Value val)
	throws TwisterException {
		Region region;
		//String inputFile = fileData.getFileName();
		try {
			region = new Region(val.getBytes());
		} catch (SerializationException e) {
			throw new TwisterException(e);
		}
		//System.out.println("Just into the Mapper");
		List<Block> blocks = region.getBlocks();

		for (Block block : blocks) {
			int rowBlockNumber = block.getRowBlockNumber();
			int columnBlockNumber = block.getColumnBlockNumber();

			String rowBlockFileName = String
			.format("%s%s%s%d", rowDataDir, File.separator, rowGeneBlockPrefix, rowBlockNumber);

			// for each block, read row block and columnBlockNumber block
			Sequence[] rowBlockSequences = null;
			Sequence[] colBlockSequences = null;
			try {
			//System.out.println("11111111111111111111111111111111");
				if(seqType.equals("DNA"))
					rowBlockSequences = GenePartition.ConvertSequences(SequenceParser.parse(rowBlockFileName, Alphabet.DNA));
				else if(seqType.equals("RNA"))
					rowBlockSequences = GenePartition.ConvertSequences(SequenceParser.parse(rowBlockFileName, Alphabet.Protein));
			} catch (Exception e) {
				InetAddress addr;
				try {
					addr = InetAddress.getLocalHost();
					// Get IP Address
				    //byte[] ipAddr = addr.getAddress();

				    // Get hostname
				    String hostname = addr.getHostName();
				    System.out.println(hostname);
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				throw new TwisterException(e);
			}

			String colBlockFileName = String.format("%s%s%s%d", colDataDir, File.separator, colGeneBlockPrefix, columnBlockNumber);
			try {
				if(seqType.equals("DNA"))
					colBlockSequences = GenePartition.ConvertSequences(SequenceParser.parse(colBlockFileName, Alphabet.DNA));
				else if(seqType.equals("RNA"))
					colBlockSequences = GenePartition.ConvertSequences(SequenceParser.parse(colBlockFileName, Alphabet.Protein));
			} catch (Exception e) {
				InetAddress addr;
				try {
					addr = InetAddress.getLocalHost();
					// Get IP Address
					//byte[] ipAddr = addr.getAddress();

					// Get hostname
					String hostname = addr.getHostName();
					System.out.println(hostname);
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				throw new TwisterException(e);
			}
			

			// get the number of sequences of row block and column block
			int rowSize = rowBlockSequences.length;
			int columnSize = colBlockSequences.length;

			//System.out.println("Before the choice!");
			// calculate distance, sequence by sequence
			//short[][] distances = new short[rowSize][columnSize];
			
			short[][] score = new short[rowSize][columnSize];
			short[][] length = new short[rowSize][columnSize];
			short[][] identicalPairs = new short[rowSize][columnSize];
			short[][] scoreReverse = new short[rowSize][columnSize];
			short[][] lengthReverse = new short[rowSize][columnSize];
			short[][] identicalPairsReverse = new short[rowSize][columnSize];
			//This will be the minimum score for NW
			short[][] scoreA = new short[rowSize][columnSize];
			//This will be the maximum score for NW
			short[][] scoreB = new short[rowSize][columnSize];
			//This will be the normalized score for NW
			short[][] scoreNormal = new short[rowSize][columnSize];
			short[][] scoreNormalReverse = new short[rowSize][columnSize];
			
			short[][] scoreAReverse = new short[rowSize][columnSize];
			short[][] scoreBReverse = new short[rowSize][columnSize];
			if(type.equals("NW")){
				gapOpen = 9;
				gapExt = 1;
				// Not a diagonal block. So have to do pairwise distance calculation for each pair
				//System.out.println("rowSize: " + rowSize + "||colSize: " + columnSize);
				for (int j = 0; j < rowSize; j++) {
					for (int k = 0; k < columnSize; k++) {
						SubstitutionMatrix<AminoAcidCompound> queryMatrix = null;
						SubstitutionMatrix<AminoAcidCompound> targetMatrix = null;
						try {
							//System.out.println(rowBlockSequences[j].getId());
							queryMatrix = MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(
									rowBlockSequences[j].getId().toUpperCase(), ".out.ascii");
							targetMatrix =
						        MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(
						        		colBlockSequences[k].getId().toUpperCase(), ".out.ascii");
						} catch (MatrixNotFoundException e) {
							// TODO Auto-generated catch block
							System.out.println("where is my matrix?" + rowBlockSequences[j].getId() + " || " + colBlockSequences[k].getId());
							e.printStackTrace();
						}
//						System.out.println("row: " + rowBlockSequences[j].toString());
//						System.out.println("row matrix: " + queryMatrix);
//						System.out.println("col: " + colBlockSequences[k].toString());
//						System.out.println("col matrix: " + targetMatrix);
						AlignmentData ad = BioJavaWrapper.calculateAlignment(
								new ProteinSequence(rowBlockSequences[j].toString()), 
								new ProteinSequence(colBlockSequences[k].toString()), 
								gapOpen, gapExt, queryMatrix, targetMatrix,DistanceType.PercentIdentity);
						score[j][k] = (short)ad.getScore();
						scoreA[j][k] = (short)ad.getMaxScore();
						scoreB[j][k] = (short)ad.getMinScore();
						scoreNormal[j][k] = (short)((1-ad.getNormalizedScore()) * Short.MAX_VALUE);
						score[j][k] = (short)ad.getScore();
						length[j][k] = (short)ad.getAlignmentLengthExcludingEndGaps();
						identicalPairs[j][k] = (short)ad.getNumIdenticals();
					}
				}
			}
			else if(type.equals("SWG")){
				SimilarityMatrix swgScoringMatrix = null;
				try {
					//System.out.println("Begine to calculate!");
					if(matrixType.equals("blo")){
						swgScoringMatrix = SimilarityMatrix.getBLOSUM62();
						gapOpen = (short) 9;
						gapExt = (short) 1;
					}
					else if(matrixType.equals("edn"))
						swgScoringMatrix = SimilarityMatrix.getEDNAFULL();
					

					// Not a diagonal block. So have to do pairwise distance calculation for each pair
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < columnSize; k++) {
							//System.out.println("row: " + j + " col: " + k);
							Sequence sequenceA = rowBlockSequences[j];
							Sequence sequenceB = colBlockSequences[k];
							List<AlignedData> ads = SequenceAlignment.
									getSWGAlignedData(sequenceA, sequenceB, 
											gapOpen, gapExt, swgScoringMatrix);
							AlignedData ad = null;
							//try{
							int startA, endA, startB, endB;
							if(ads.size() != 0){
								ad = ads.get(0);
								score[j][k] = ad.getScore();
								length[j][k] = ad.getAlignmentLength();
								identicalPairs[j][k] = ad.getNumberOfIdenticalBasePairs(false);

								startA = ad.getFirstAlignedSequenceStartOffset();
								endA = ad.getFirstAlignedSequenceEndOffset();
								startB = ad.getSecondAlignedSeqeunceStartOffset();
								endB = ad.getSecondAlignedSeqeunceEndOffset();
								Sequence partialA =	getPartialSequence(startA, endA, sequenceA);
								scoreA[j][k] = (short) partialA.getSelfAlignedScore(swgScoringMatrix);
								Sequence partialB =	getPartialSequence(startB, endB, sequenceB); 
								
								scoreB[j][k] = (short) partialB.getSelfAlignedScore(swgScoringMatrix);
							}
							else{
								ad = getAdWithZeroValue(sequenceA, sequenceB);
								score[j][k] = 0;
								length[j][k] = 0;
								identicalPairs[j][k] = 0;
								scoreA[j][k] = (short)sequenceA.getSelfAlignedScore(swgScoringMatrix);
								scoreB[j][k] = (short)sequenceB.getSelfAlignedScore(swgScoringMatrix);
							}
							

							if(seqType.equals("DNA")){
								try{
									ads = SequenceAlignment.
											getSWGAlignedData(sequenceA, 
													sequenceB.getReverseComplementedSequence(), 
													gapOpen, gapExt, swgScoringMatrix);
								}
								catch(Exception e){
									System.out.println("Reverse Exception:");
									System.out.print("sequenceB: ");
									for(int x = 0; x < sequenceB.toString().length(); x++){
										char c = sequenceB.toString().charAt(x);
										if(c != 'A' && c != 'C' && c != 'T' && c!= 'G')
											System.out.print(c);
									}
								}
								if(ads.size() != 0){
									ad = ads.get(0);

									scoreReverse[j][k] = ad.getScore();
									lengthReverse[j][k] = ad.getAlignmentLength();
									identicalPairsReverse[j][k] = ad.getNumberOfIdenticalBasePairs(false);

									startA = ad.getFirstAlignedSequenceStartOffset();
									endA = ad.getFirstAlignedSequenceEndOffset();
									startB = ad.getSecondAlignedSeqeunceStartOffset();
									endB = ad.getSecondAlignedSeqeunceEndOffset();

									//ads = aligner.align(ednafull, -gapOpen, -gapExt, 
									Sequence partialReverseA =	getPartialSequence(startA, endA, sequenceA); 
									//		getPartialSequence(startA, endA, sequenceA));
									//ad = ads.get(0);
									scoreAReverse[j][k] = (short) partialReverseA.getSelfAlignedScore(swgScoringMatrix);
									//ad.getScore();
									//ads = aligner.align(ednafull, -gapOpen, -gapExt, 
									Sequence partialReverseB = getPartialSequence(startB, endB, sequenceB.getReverseComplementedSequence()); 
									//		getPartialSequence(startB, endB, sequenceB.getReverseComplementedSequence()));
									//ad = ads.get(0);
									scoreBReverse[j][k] = (short) partialReverseB.getSelfAlignedScore(swgScoringMatrix);
								}
								else{
									ad = getAdWithZeroValue(sequenceA, sequenceB);
									scoreReverse[j][k] = 0;
									lengthReverse[j][k] = 0;
									identicalPairsReverse[j][k] = 0;
									scoreAReverse[j][k] = (short)sequenceA.getSelfAlignedScore(swgScoringMatrix);
									scoreBReverse[j][k] = (short)sequenceB.getSelfAlignedScore(swgScoringMatrix);

								}
									//ad.getScore();
							}				
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			block.setScore(score, false);
			block.setLength(length);
			block.setIdenticalPairs(identicalPairs);
			block.setScoreReverse(scoreReverse);
			block.setScoreA(scoreA);
			block.setScoreB(scoreB);
			block.setScoreNormal(scoreNormal);
			block.setScoreNormalReverse(scoreNormalReverse);
			block.setScoreAReverse(scoreAReverse);
			block.setScoreBReverse(scoreBReverse);
			block.setLengthReverse(lengthReverse);
			block.setIdenticalPairsReverse(identicalPairsReverse);
			try {
				collector.collect(new StringKey("" + block.getRowBlockNumber()),
						new BytesValue(block.getBytes()));
			} catch (SerializationException e) {
				throw new TwisterException(e);
			}
		}
}
	
	private Sequence getPartialSequence(int startIndex, int endIndex, Sequence sequence){
		Sequence partialSequence = null;
		if(seqType.equals("DNA"))
			partialSequence = new Sequence(sequence.toString().substring(startIndex, endIndex+1), 
					sequence.getId(), Alphabet.DNA);
		else if(seqType.equals("RNA"))
			partialSequence = new Sequence(sequence.toString().substring(startIndex, endIndex+1), 
					sequence.getId(), Alphabet.Protein);
		return partialSequence;
	}
	
	private ProteinSequence getPartialSequence(int startIndex, int endIndex, ProteinSequence sequence){
		ProteinSequence partialSequence = null;
		if(seqType.equals("DNA"))
			partialSequence = new ProteinSequence(sequence.toString().substring(startIndex, endIndex+1));
		else if(seqType.equals("RNA"))
			partialSequence = new ProteinSequence(sequence.toString().substring(startIndex, endIndex+1));
		return partialSequence;
	}
	
	private AlignedData getAdWithZeroValue(Sequence sequenceA, Sequence sequenceB){
		AlignedData ad = new AlignedData(sequenceA, sequenceB);
		ad.setScore(0);
		ad.setFirstAlignedSequence(null);
		ad.setSecondAlignedSequence(null);
		ad.setFirstAlignedSequenceStartOffset(-1);
		ad.setFirstAlignedSequenceEndOffset(-1);
		ad.setSecondAlignedSeqeunceStartOffset(-1);
		ad.setSecondAlignedSeqeunceEndOffset(-1);
		ad.setFirstAlignedSequenceInsertionCount(-1);
		ad.setSecondAlignedSeqeunceInsertionCount(-1);
		ad.setFirstOffset(-1);
		ad.setSecondOffset(-1);
		return ad;
	}
}
