package cgl.imr.samples.pwa.pid.length;

import cgl.imr.base.*;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.MapperConf;
import cgl.imr.samples.pwa.util.GenePartition;
import cgl.imr.types.BytesValue;
import cgl.imr.types.StringKey;
import edu.indiana.salsahpc.AlignedData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.BioJavaWrapper;
import edu.indiana.salsahpc.DistanceUtil;
import edu.indiana.salsahpc.MatrixUtil;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;
import edu.indiana.salsahpc.SimilarityMatrix;
import edu.indiana.salsahpc.SmithWatermanAligner;

import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.compound.NucleotideCompound;

import java.io.File;
import java.io.IOException;
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
	private String reverseFlag;
	//FileData fileData;

	@Override
	public void close() throws TwisterException {

	}

	@Override
	public void configure(JobConf jobConf, MapperConf mapConf)
	throws TwisterException {

		dataDir = jobConf.getProperty("dataDir");
		geneBlockPrefix = jobConf.getProperty("geneBlockPrefix");

		scoringMatrix = MatrixUtil.getEDNAFULL();
		gapOpen = (short) 16;
		gapExt = (short) 4;
		type = jobConf.getProperty("Type");
		//fileData = (FileData) mapConf.getDataPartition();
		reverseFlag = jobConf.getProperty("ReverseFlag");
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
			Sequence[] rowBlockSequences;
			Sequence[] colBlockSequences;
			try {
				rowBlockSequences = GenePartition.ConvertSequences(SequenceParser.parse(rowBlockFileName, Alphabet.DNA));
			} catch (Exception e) {
				throw new TwisterException(e);
			}

			if (rowBlockNumber == columnBlockNumber) {
				colBlockSequences = rowBlockSequences;
			} else {
				String colBlockFileName = String.format("%s%s%s%d", dataDir, File.separator, geneBlockPrefix, columnBlockNumber);
				try {
					colBlockSequences = GenePartition.ConvertSequences(SequenceParser.parse(colBlockFileName, Alphabet.DNA));
				} catch (Exception e) {
					throw new TwisterException(e);
				}
			}

			// get the number of sequences of row block and column block
			int rowSize = rowBlockSequences.length;
			int columnSize = colBlockSequences.length;

			//System.out.println("Before the choice!");
			// calculate distance, sequence by sequence
			//short[][] distances = new short[rowSize][columnSize];
			
			short[][] percent = new short[rowSize][columnSize];
			double[][] jukes = new double[rowSize][columnSize];
			double[][] kimura = new double[rowSize][columnSize];
			
			short[][] percentChoice = new short[rowSize][columnSize];
			short[][] jukesChoice = new short[rowSize][columnSize];
			short[][] kimuraChoice = new short[rowSize][columnSize];
			// calculate the alignment length size
			
			short[][] lengths = new short[rowSize][columnSize];
			short[][] lengthsReverse = new short[rowSize][columnSize];
			if(type.equals("NW")){
				if (rowBlockNumber != columnBlockNumber) {
					// Not a diagonal block. So have to do pairwise distance calculation for each pair
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < columnSize; k++) {
							percent[j][k] = BioJavaWrapper.calculateDistance(
									rowBlockSequences[j].toString(), colBlockSequences[k].toString(),
									gapOpen , gapExt,
									scoringMatrix);
						}
					}
				} else {
					// Diagonal block. Perform pairwise distance calculation only for one triangle
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < j; k++) {
							percent[j][k] = BioJavaWrapper.calculateDistance(
									rowBlockSequences[j].toString(), colBlockSequences[k].toString(),
									gapOpen , gapExt,
									scoringMatrix);
							percent[k][j] = percent[j][k];
						}
						// Pairwise distance for diagonal elements
						percent[j][j] = 0;
					}
				}
			}
			else if(type.equals("SWG")){
				SimilarityMatrix ednafull;
				try {
					//System.out.println("Begine to calculate!");
					ednafull = SimilarityMatrix.getEDNAFULL();
					SmithWatermanAligner aligner = new SmithWatermanAligner();
					if (rowBlockNumber != columnBlockNumber) {
						// Not a diagonal block. So have to do pairwise distance calculation for each pair
						for (int j = 0; j < rowSize; j++) {
							for (int k = 0; k < columnSize; k++) {
								//System.out.println("row: " + j + " col: " + k);
								Sequence sequenceA = rowBlockSequences[j];
								Sequence sequenceB = colBlockSequences[k];
								Sequence sequenceBReverse = sequenceB.getReverseComplementedSequence();
								List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
										sequenceA, sequenceB);
								// We will just take the first alignment
								AlignedData ad = ads.get(0);
								List<AlignedData> adsReverse = aligner.align(ednafull, -gapOpen, -gapExt, 
										sequenceA, sequenceBReverse);
								// We will just take the first alignment
								AlignedData adReverse = adsReverse.get(0);
								short percent1 = DistanceUtil.computePercentIdentityDistanceAsShort(ad);
								short percentReverse1 = DistanceUtil.computePercentIdentityDistanceAsShort(adReverse);
								
								double jukes1 = DistanceUtil.computeJukesCantorDistance(ad);
								double jukesReverse1 = DistanceUtil.computeJukesCantorDistance(adReverse);
								
								double kimura1 = DistanceUtil.computeKimura2Distance(ad);
								double kimuraReverse1 = DistanceUtil.computeKimura2Distance(adReverse);
								if(reverseFlag.equals("1")){
								if(percent1 <= percentReverse1){
									percent[j][k] = percent1;
									percentChoice[j][k] = 0;
								}
								else{
									percent[j][k] = percentReverse1;
									percentChoice[j][k] = 1;
								}
								
								if(jukes1 <= jukesReverse1){
									jukes[j][k] = jukes1;
									jukesChoice[j][k] = 0;
								}
								else{
									jukes[j][k] = jukesReverse1;
									jukesChoice[j][k] = 1;
								}
								
								if(kimura1 <= kimuraReverse1){
									kimura[j][k] = kimura1;
									kimuraChoice[j][k] = 0;
								}
								else{
									kimura[j][k] = kimuraReverse1;
									kimuraChoice[j][k] = 1;
								}
								}
								else if(reverseFlag.equals("0")){
										percent[j][k] = percent1;
										percentChoice[j][k] = 0;
										jukes[j][k] = jukes1;
										jukesChoice[j][k] = 0;
										kimura[j][k] = kimura1;
										kimuraChoice[j][k] = 0;
								}

								lengths[j][k] = (short)((ad.getFirstAlignedSequenceEndOffset() 
										- ad.getFirstAlignedSequenceStartOffset()) 
										+ 1 + ad.getFirstAlignedSequenceInsertionCount());
								lengthsReverse[j][k] = (short)((adReverse.getFirstAlignedSequenceEndOffset() 
										- adReverse.getFirstAlignedSequenceStartOffset()) 
										+ 1 + adReverse.getFirstAlignedSequenceInsertionCount());
							}
						}
					} else {
						// Diagonal block. Perform pairwise distance calculation only for one triangle
						for (int j = 0; j < rowSize; j++) {
							for (int k = 0; k < j; k++) {
								//System.out.println("row: " + j + " col: " + k);
								Sequence sequenceA = rowBlockSequences[j];
								Sequence sequenceB = colBlockSequences[k];
								Sequence sequenceBReverse = sequenceB.getReverseComplementedSequence();
								List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
										sequenceA, sequenceB);
								// We will just take the first alignment
								AlignedData ad = ads.get(0);
								List<AlignedData> adsReverse = aligner.align(ednafull, -gapOpen, -gapExt, 
										sequenceA, sequenceBReverse);
								// We will just take the first alignment
								AlignedData adReverse = adsReverse.get(0);
								short percent1 = DistanceUtil.computePercentIdentityDistanceAsShort(ad);
								short percentReverse1 = DistanceUtil.computePercentIdentityDistanceAsShort(adReverse);
								
								double jukes1 = DistanceUtil.computeJukesCantorDistance(ad);
								double jukesReverse1 = DistanceUtil.computeJukesCantorDistance(adReverse);
								
								double kimura1 = DistanceUtil.computeKimura2Distance(ad);
								double kimuraReverse1 = DistanceUtil.computeKimura2Distance(adReverse);
								if(reverseFlag.equals("1")){
								if(percent1 <= percentReverse1){
									percent[j][k] = percent1;
									percentChoice[j][k] = 0;
								}
								else{
									percent[j][k] = percentReverse1;
									percentChoice[j][k] = 1;
								}
								
								if(jukes1 <= jukesReverse1){
									jukes[j][k] = jukes1;
									jukesChoice[j][k] = 0;
								}
								else{
									jukes[j][k] = jukesReverse1;
									jukesChoice[j][k] = 1;
								}
								
								if(kimura1 <= kimuraReverse1){
									kimura[j][k] = kimura1;
									kimuraChoice[j][k] = 0;
								}
								else{
									kimura[j][k] = kimuraReverse1;
									kimuraChoice[j][k] = 1;
								}
								}
								else if(reverseFlag.equals("0")){
									percent[j][k] = percent1;
									percentChoice[j][k] = 0;
									jukes[j][k] = jukes1;
									jukesChoice[j][k] = 0;
									kimura[j][k] = kimura1;
									kimuraChoice[j][k] = 0;
							}

								lengths[j][k] = (short)((ad.getFirstAlignedSequenceEndOffset() 
										- ad.getFirstAlignedSequenceStartOffset()) 
										+ 1 + ad.getFirstAlignedSequenceInsertionCount());
								lengthsReverse[j][k] = (short)((adReverse.getFirstAlignedSequenceEndOffset() 
										- adReverse.getFirstAlignedSequenceStartOffset()) 
										+ 1 + adReverse.getFirstAlignedSequenceInsertionCount());
								
								percent[k][j] = percent[j][k];
								percentChoice[k][j] = percentChoice[j][k];
								jukes[k][j] = jukes[j][k];
								jukesChoice[k][j] = jukesChoice[j][k];
								kimura[k][j] = kimura[j][k];
								kimuraChoice[k][j] = kimuraChoice[j][k];
								lengthsReverse[k][j] = lengthsReverse[j][k];
								lengths[k][j] = lengths[j][k];
							}
							// Pairwise distance for diagonal elements
							percent[j][j] = 0;
							percentChoice[j][j] = 0;
							jukes[j][j] = 0;
							jukesChoice[j][j] = 0;
							kimura[j][j] = 0;
							kimuraChoice[j][j] = 0;
							lengthsReverse[j][j] = //rowBlockSequences[j].getCount();
								rowBlockSequences[j].getReverseComplementedSequence().getCount();
							lengths[j][j] = 
								rowBlockSequences[j].getCount();
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				block.setPercent(percent, false);
				block.setPercentChoice(percentChoice);
				block.setJukes(jukes);
				block.setJukesChoice(jukesChoice);
				block.setKimura(kimura);
				block.setKimuraChoice(kimuraChoice);
				block.setLengths(lengths);
				block.setLengthsReverse(lengthsReverse);

				try {
					collector.collect(new StringKey("" + block.getRowBlockNumber()),
							new BytesValue(block.getBytes()));
				} catch (SerializationException e) {
					throw new TwisterException(e);
				}

				if (rowBlockNumber != columnBlockNumber) {
					// Creates a transpose block. Note. distances array should be treated as transpose when reading.
					Block transBlock = new Block(block.getColumnBlockNumber(), block.getRowBlockNumber());
					transBlock.setPercent(block.getPercent(), true);
					transBlock.setPercentChoice(block.getPercentChoice());
					transBlock.setJukes(block.getJukes());
					transBlock.setJukesChoice(block.getJukesChoice());
					transBlock.setKimura(block.getKimura());
					transBlock.setKimuraChoice(block.getKimuraChoice());
					transBlock.setLengths(block.getLengths());
					transBlock.setLengthsReverse(block.getLengthsReverse());
					try {
						collector.collect(new StringKey("" + transBlock.getRowBlockNumber()),
								new BytesValue(transBlock.getBytes()));
					} catch (SerializationException e) {
						throw new TwisterException(e);
					}
				}
			}
		}
	}
}
