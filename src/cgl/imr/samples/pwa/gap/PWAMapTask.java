package cgl.imr.samples.pwa.gap;

import cgl.imr.base.*;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.MapperConf;
import cgl.imr.samples.pwa.util.GenePartition;
import cgl.imr.types.DoubleValue;
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
		matrixType = jobConf.getProperty("MatrixType");
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
			
			int[][] score = new int[rowSize][columnSize];
			
			double checkSum = 0;
			// calculate the alignment length size
			if(type.equals("NW")){
				if (rowBlockNumber != columnBlockNumber) {
					// Not a diagonal block. So have to do pairwise distance calculation for each pair
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < columnSize; k++) {
							score[j][k] = BioJavaWrapper.calculateDistance(
									rowBlockSequences[j].toString(), colBlockSequences[k].toString(),
									gapOpen , gapExt,
									scoringMatrix);
						}
					}
				} else {
					// Diagonal block. Perform pairwise distance calculation only for one triangle
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < j; k++) {
							score[j][k] = BioJavaWrapper.calculateDistance(
									rowBlockSequences[j].toString(), colBlockSequences[k].toString(),
									gapOpen , gapExt,
									scoringMatrix);
							score[k][j] = score[j][k];
						}
						// Pairwise distance for diagonal elements
						score[j][j] = 0;
					}
				}
			}
			else if(type.equals("SWG")){
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
						
					
					if (rowBlockNumber != columnBlockNumber) {
						// Not a diagonal block. So have to do pairwise distance calculation for each pair
						
						for (int j = 0; j < rowSize; j++) {
							if(j%100 == 0)
							System.out.print(j + "|"  );
							for (int k = 0; k < columnSize; k++) {
								//System.out.println("row: " + j + " col: " + k);
								Sequence sequenceA = rowBlockSequences[j];
								Sequence sequenceB = colBlockSequences[k];
								//Sequence sequenceBReverse = sequenceB.getReverseComplementedSequence();
								List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
										sequenceA, sequenceB);
								// We will just take the first alignment
								AlignedData ad = ads.get(0);
								
								Sequence tmpA = ad.getFirstAlignedSequence();
								Sequence tmpB = ad.getSecondAlignedSequence();
								for(int i = 0; i < tmpA.getCount(); i++){
									
									if(tmpA.toString().charAt(i) == '-' 
										&&
											tmpB.toString().charAt(i) == '-'){
										//System.out.println(tmpA.getId() + " " + tmpB.getId());
										checkSum++;
									}
								}
								
							}
						}
					} else {
						// Diagonal block. Perform pairwise distance calculation only for one triangle
						for (int j = 0; j < rowSize; j++) {
							if(j%100 == 0)
								System.out.println(j + "|");
							for (int k = 0; k < j; k++) {
								//System.out.println("row: " + j + " col: " + k);
								Sequence sequenceA = rowBlockSequences[j];
								Sequence sequenceB = colBlockSequences[k];
								//Sequence sequenceBReverse = sequenceB.getReverseComplementedSequence();
								List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
										sequenceA, sequenceB);
								// We will just take the first alignment
								AlignedData ad = ads.get(0);
								
								Sequence tmpA = ad.getFirstAlignedSequence();
								Sequence tmpB = ad.getSecondAlignedSequence();
								for(int i = 0; i < tmpA.getCount(); i++){
									
									if(tmpA.toString().charAt(i) == '-' 
										&&
											tmpB.toString().charAt(i) == '-'){
										//System.out.println(tmpA.getId() + " " + tmpB.getId());
										checkSum++;
									}
								}
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

				collector.collect(new StringKey("GapOnGapCheck"),
							//new BytesValue(block.getBytes())
							new DoubleValue(checkSum));
			}
		}
	}
}
