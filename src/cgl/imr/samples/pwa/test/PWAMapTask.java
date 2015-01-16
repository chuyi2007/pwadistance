package cgl.imr.samples.pwa.test;

import cgl.imr.base.*;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.MapperConf;
import cgl.imr.samples.pwa.util.GenePartition;
import cgl.imr.types.BytesValue;
import cgl.imr.types.StringKey;
import edu.indiana.salsahpc.AlignedData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.BioJavaWrapper;
import edu.indiana.salsahpc.MatrixUtil;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;
import edu.indiana.salsahpc.SimilarityMatrix;
import edu.indiana.salsahpc.SmithWatermanAligner;

import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.compound.NucleotideCompound;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 * @author Saliya Ekanayake (sekanaya at cs dot indiana dot edu)
 */

public class PWAMapTask implements MapTask {

	private String dataDir;
	private String geneBlockPrefix;

	private SubstitutionMatrix<NucleotideCompound> scoringMatrix;
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

		dataDir = jobConf.getProperty("dataDir");
		geneBlockPrefix = jobConf.getProperty("geneBlockPrefix");

		//scoringMatrix = MatrixUtil.getEDNAFULL();
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

			String rowBlockFileName = String.format("%s%s%s%d", dataDir, File.separator, geneBlockPrefix, rowBlockNumber);

			// for each block, read row block and columnBlockNumber block
			Sequence[] rowBlockSequences = null;
			Sequence[] colBlockSequences = null;
			try {
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

			if (rowBlockNumber == columnBlockNumber) {
				colBlockSequences = rowBlockSequences;
			} else {
				String colBlockFileName = String.format("%s%s%s%d", dataDir, File.separator, geneBlockPrefix, columnBlockNumber);
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
			}

			// get the number of sequences of row block and column block
			int rowSize = rowBlockSequences.length;
			int columnSize = colBlockSequences.length;

			//System.out.println("Before the choice!");
			// calculate distance, sequence by sequence
			//short[][] distances = new short[rowSize][columnSize];
			
			short[][] scoreAB = new short[rowSize][columnSize];
			short[][] scoreBA = new short[rowSize][columnSize];
			short[][] lengthAB = new short[rowSize][columnSize];
			short[][] lengthBA = new short[rowSize][columnSize];
			short[][] identicalPairsAB = new short[rowSize][columnSize];
			short[][] identicalPairsBA = new short[rowSize][columnSize];
			
			
			short[][] scoreABReverse = new short[rowSize][columnSize];
			short[][] scoreBAReverse = new short[rowSize][columnSize];
			short[][] scoreAReverseBReverse = new short[rowSize][columnSize];
			short[][] scoreBReverseAReverse = new short[rowSize][columnSize];
			short[][] lengthABReverse = new short[rowSize][columnSize];
			short[][] lengthBAReverse = new short[rowSize][columnSize];
			short[][] lengthAReverseBReverse = new short[rowSize][columnSize];
			short[][] lengthBReverseAReverse = new short[rowSize][columnSize];
			short[][] identicalPairsABReverse = new short[rowSize][columnSize];
			short[][] identicalPairsBAReverse = new short[rowSize][columnSize];
			short[][] identicalPairsAReverseBReverse = new short[rowSize][columnSize];
			short[][] identicalPairsBReverseAReverse = new short[rowSize][columnSize];
			
			if(type.equals("SWG")){
				SimilarityMatrix ednafull = null;
				try {
					//System.out.println("Begine to calculate!");
					if(matrixType.equals("blo")){
						ednafull = SimilarityMatrix.getBLOSUM62();
						gapOpen = (short) 9;
						gapExt = (short) 1;
					}
					else if(matrixType.equals("edn"))
						ednafull = SimilarityMatrix.getEDNAFULL();
					SmithWatermanAligner aligner = new SmithWatermanAligner();
						
					
					//if (rowBlockNumber != columnBlockNumber) {
						// Not a diagonal block. So have to do pairwise distance calculation for each pair
						for (int j = 0; j < rowSize; j++) {
							for (int k = 0; k < columnSize; k++) {
								//System.out.println("row: " + j + " col: " + k);
								Sequence sequenceA = rowBlockSequences[j];
								Sequence sequenceB = colBlockSequences[k];
								List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
										sequenceA, sequenceB);
								AlignedData ad = null;
								try{
									ad = ads.get(0);
								}
								catch(Exception e){
									System.out.println("Exception:");
									System.out.println("SequenceA: " + sequenceA.toString() + 
											"####SequenceB:" + sequenceB.toString());
								}
								scoreAB[j][k] = ad.getScore();
								lengthAB[j][k] = ad.getAlignmentLength();
								identicalPairsAB[j][k] = ad.getNumberOfIdenticalBasePairs(false);
								
								int startA = ad.getFirstAlignedSequenceStartOffset();
								int endA = ad.getFirstAlignedSequenceEndOffset();
								int startB = ad.getSecondAlignedSeqeunceStartOffset();
								int endB = ad.getSecondAlignedSeqeunceEndOffset();
								
								Sequence partialSequenceA = getPartialSequence(startA, endA, sequenceA);
								lengthAB[j][k] = (short) partialSequenceA.getSelfAlignedScore(ednafull);
								Sequence partialSequenceB = getPartialSequence(startB, endB, sequenceB);
								identicalPairsAB[j][k] = (short) partialSequenceB.getSelfAlignedScore(ednafull);
								
								ads = aligner.align(ednafull, -gapOpen, -gapExt, 
										sequenceB, sequenceA);
								ad = ads.get(0);
								scoreBA[j][k] = ad.getScore();
								lengthBA[j][k] = ad.getAlignmentLength();
								identicalPairsBA[j][k] = ad.getNumberOfIdenticalBasePairs(false);
								
								startA = ad.getFirstAlignedSequenceStartOffset();
								endA = ad.getFirstAlignedSequenceEndOffset();
								startB = ad.getSecondAlignedSeqeunceStartOffset();
								endB = ad.getSecondAlignedSeqeunceEndOffset();
								partialSequenceA = getPartialSequence(startA, endA, sequenceB);
								lengthBA[j][k] = (short) partialSequenceA.getSelfAlignedScore(ednafull);
								partialSequenceB = getPartialSequence(startB, endB, sequenceA);
								identicalPairsBA[j][k] = (short) partialSequenceB.getSelfAlignedScore(ednafull);
								
//								
//								ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//										getPartialSequence(startA, endA, sequenceA), 
//										getPartialSequence(startA, endA, sequenceA));
//								ad = ads.get(0);
//								scoreA[j][k] = ad.getScore();
//								ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//										getPartialSequence(startB, endB, sequenceB), 
//										getPartialSequence(startB, endB, sequenceB));
//								ad = ads.get(0);
//								scoreB[j][k] = ad.getScore();
								
								if(seqType.equals("DNA")){
									try{
									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
											sequenceA, sequenceB.getReverseComplementedSequence());
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
									ad = ads.get(0);
									scoreABReverse[j][k] = ad.getScore();
									lengthABReverse[j][k] = ad.getAlignmentLength();
									identicalPairsABReverse[j][k] = ad.getNumberOfIdenticalBasePairs(false);
									
									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
											sequenceA.getReverseComplementedSequence(), sequenceB);
									ad = ads.get(0);
									scoreBAReverse[j][k] = ad.getScore();
									lengthBAReverse[j][k] = ad.getAlignmentLength();
									identicalPairsBAReverse[j][k] = ad.getNumberOfIdenticalBasePairs(false);
									
									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
											sequenceA.getReverseComplementedSequence(), sequenceB.getReverseComplementedSequence());
									ad = ads.get(0);
									scoreAReverseBReverse[j][k] = ad.getScore();
									lengthAReverseBReverse[j][k] = ad.getAlignmentLength();
									identicalPairsAReverseBReverse[j][k] = ad.getNumberOfIdenticalBasePairs(false);
									
									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
											sequenceB.getReverseComplementedSequence(), sequenceA.getReverseComplementedSequence());
									ad = ads.get(0);
									scoreBReverseAReverse[j][k] = ad.getScore();
									lengthBReverseAReverse[j][k] = ad.getAlignmentLength();
									identicalPairsBReverseAReverse[j][k] = ad.getNumberOfIdenticalBasePairs(false);
									
//									startA = ad.getFirstAlignedSequenceStartOffset();
//									endA = ad.getFirstAlignedSequenceEndOffset();
//									startB = ad.getSecondAlignedSeqeunceStartOffset();
//									endB = ad.getSecondAlignedSeqeunceEndOffset();
//									
//									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//											getPartialSequence(startA, endA, sequenceA), 
//											getPartialSequence(startA, endA, sequenceA));
//									ad = ads.get(0);
//									scoreAReverse[j][k] = ad.getScore();
//									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//											getPartialSequence(startB, endB, sequenceB.getReverseComplementedSequence()), 
//											getPartialSequence(startB, endB, sequenceB.getReverseComplementedSequence()));
//									ad = ads.get(0);
//									scoreBReverse[j][k] = ad.getScore();
								}					
							}
						}
//					} else {
//						// Diagonal block. Perform pairwise distance calculation only for one triangle
//						for (int j = 0; j < rowSize; j++) {
//							for (int k = 0; k < j; k++) {
//								//System.out.println("row: " + j + " col: " + k);
//								Sequence sequenceA = rowBlockSequences[j];
//								Sequence sequenceB = colBlockSequences[k];
//								//Sequence sequenceBReverse = sequenceB.getReverseComplementedSequence();
//								List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//										sequenceA, sequenceB);
//								// We will just take the first alignment
//								AlignedData ad = ads.get(0);
//								scoreAB[j][k] = ad.getScore();
//								scoreAB[k][j] = scoreAB[j][k];					          
//								lengthAB[j][k] = ad.getAlignmentLength();
//								lengthAB[k][j] = lengthAB[j][k];
//								identicalPairs[j][k] = ad.getNumberOfIdenticalBasePairs(false);
//								identicalPairs[k][j] = identicalPairs[j][k];
//								
//								int startA = ad.getFirstAlignedSequenceStartOffset();
//								int endA = ad.getFirstAlignedSequenceEndOffset();
//								int startB = ad.getSecondAlignedSeqeunceStartOffset();
//								int endB = ad.getSecondAlignedSeqeunceEndOffset();
////								
////								System.out.println("Total A:" + sequenceA.toString().length());
////								System.out.println("startA: " + startA);
////
////								System.out.println("endA: " + endA);
////								System.out.println("Total B:" + sequenceB.toString().length());
////
////								System.out.println("startB: " + startB);
////
////								System.out.println("endB: " + endB);
//								ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//										getPartialSequence(startA, endA, sequenceA), 
//										getPartialSequence(startA, endA, sequenceA));
//								ad = ads.get(0);
//								scoreA[j][k] = ad.getScore();
//								scoreA[k][j] = scoreA[j][k];
//								ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//										getPartialSequence(startB, endB, sequenceB), 
//										getPartialSequence(startB, endB, sequenceB));
//								ad = ads.get(0);
//								scoreB[j][k] = ad.getScore();
//								scoreB[k][j] = scoreB[j][k];
//								
//								if(seqType.equals("DNA")){
//									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//											sequenceA, sequenceB.getReverseComplementedSequence());
//									ad = ads.get(0);
//									scoreReverse[j][k] = ad.getScore();
//									scoreReverse[k][j] = scoreReverse[j][k];
//         
//									lengthReverse[j][k] = ad.getAlignmentLength();
//									lengthReverse[k][j] = lengthReverse[j][k];
//									identicalPairsReverse[j][k] = ad.getNumberOfIdenticalBasePairs(false);
//									identicalPairsReverse[k][j] = identicalPairsReverse[j][k];
//									
//									startA = ad.getFirstAlignedSequenceStartOffset();
//									endA = ad.getFirstAlignedSequenceEndOffset();
//									startB = ad.getSecondAlignedSeqeunceStartOffset();
//									endB = ad.getSecondAlignedSeqeunceEndOffset();
//									
//									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//											getPartialSequence(startA, endA, sequenceA), 
//											getPartialSequence(startA, endA, sequenceA));
//									ad = ads.get(0);
//									scoreAReverse[j][k] = ad.getScore();
//									scoreAReverse[k][j] = scoreAReverse[j][k];
//									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//											getPartialSequence(startB, endB, sequenceB.getReverseComplementedSequence()), 
//											getPartialSequence(startB, endB, sequenceB.getReverseComplementedSequence()));
//									ad = ads.get(0);
//									scoreBReverse[j][k] = ad.getScore();
//									scoreBReverse[k][j] = scoreBReverse[j][k];
//								}
//							}
//							// Pairwise distance for diagonal elements
//							List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//									rowBlockSequences[j], rowBlockSequences[j]);
//							AlignedData ad = ads.get(0);
//							score[j][j] = ad.getScore();
//							length[j][j] = ad.getAlignmentLength();
//							identicalPairs[j][j] = ad.getNumberOfIdenticalBasePairs(false);
//							scoreA[j][j] = ad.getScore();
//							scoreB[j][j] = ad.getScore();
//							if(seqType.equals("DNA")){
//								ads = aligner.align(ednafull, -gapOpen, -gapExt, 
//										rowBlockSequences[j], rowBlockSequences[j].getReverseComplementedSequence());
//								ad = ads.get(0);
//								scoreReverse[j][j] = ad.getScore();
//								lengthReverse[j][j] = ad.getAlignmentLength();
//								identicalPairsReverse[j][j] = ad.getNumberOfIdenticalBasePairs(false);
//								scoreAReverse[j][j] = ad.getScore();
//								scoreBReverse[j][j] = ad.getScore();
//							}
//						}
//					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				block.setScore(scoreAB, false);
				block.setScoreBA(scoreBA);
				block.setLengthAB(lengthAB);
				block.setLengthBA(lengthBA);
				block.setIdenticalPairsAB(identicalPairsAB);
				block.setIdenticalPairsBA(identicalPairsBA);
				block.setScoreABReverse(scoreABReverse);
				block.setScoreBAReverse(scoreBAReverse);
				block.setScoreAReverseBReverse(scoreAReverseBReverse);
				block.setScoreBReverseAReverse(scoreBReverseAReverse);
				block.setLengthABReverse(lengthABReverse);
				block.setLengthBAReverse(lengthBAReverse);
				block.setLengthAReverseBReverse(lengthAReverseBReverse);
				block.setLengthBReverseAReverse(lengthBReverseAReverse);
				block.setIdenticalPairsABReverse(identicalPairsABReverse);
				block.setIdenticalPairsBAReverse(identicalPairsBAReverse);
				block.setIdenticalPairsAReverseBReverse(identicalPairsAReverseBReverse);
				block.setIdenticalPairsBReverseAReverse(identicalPairsBReverseAReverse);
				try {
					collector.collect(new StringKey("" + block.getRowBlockNumber()),
							new BytesValue(block.getBytes()));
				} catch (SerializationException e) {
					throw new TwisterException(e);
				}
//
//				if (rowBlockNumber != columnBlockNumber) {
//					// Creates a transpose block. Note. distances array should be treated as transpose when reading.
//					Block transBlock = new Block(block.getColumnBlockNumber(), block.getRowBlockNumber());
//					transBlock.setScore(block.getScore(), true);
//					transBlock.setLength(block.getLength());
//					transBlock.setIdenticalPairs(block.getIdenticalPairs());
//					transBlock.setScoreReverse(block.getScoreReverse());
//					transBlock.setLengthReverse(block.getLengthReverse());
//					transBlock.setIdenticalPairsReverse(block.getIdenticalPairsReverse());
//					transBlock.setScoreA(block.getScoreA());
//					transBlock.setScoreAReverse(block.getScoreAReverse());
//					transBlock.setScoreB(block.getScoreB());
//					transBlock.setScoreBReverse(block.getScoreBReverse());
//					try {
//						collector.collect(new StringKey("" + transBlock.getRowBlockNumber()),
//								new BytesValue(transBlock.getBytes()));
//					} catch (SerializationException e) {
//						throw new TwisterException(e);
//					}
//				}
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
}
