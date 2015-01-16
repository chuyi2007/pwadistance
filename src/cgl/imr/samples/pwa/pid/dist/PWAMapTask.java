package cgl.imr.samples.pwa.pid.dist;

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

	private SubstitutionMatrix<NucleotideCompound> nwScoringMatrix;
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

		nwScoringMatrix = MatrixUtil.getEDNAFULL();
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
					throw new TwisterException(e);
				}
			}

			// get the number of sequences of row block and column block
			int rowSize = rowBlockSequences.length;
			int columnSize = colBlockSequences.length;

			//System.out.println("Before the choice!");
			// calculate distance, sequence by sequence
			short[][] distances = new short[rowSize][columnSize];
			if(type.equals("NW")){
				if (rowBlockNumber != columnBlockNumber) {
					// Not a diagonal block. So have to do pairwise distance calculation for each pair
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < columnSize; k++) {
							distances[j][k] = BioJavaWrapper.calculateDistance(
									rowBlockSequences[j].toString(), colBlockSequences[k].toString(),
									gapOpen , gapExt,
									nwScoringMatrix);
						}
					}
				} else {
					// Diagonal block. Perform pairwise distance calculation only for one triangle
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < j; k++) {
							distances[j][k] = BioJavaWrapper.calculateDistance(
									rowBlockSequences[j].toString(), colBlockSequences[k].toString(),
									gapOpen , gapExt,
									nwScoringMatrix);
							distances[k][j] = distances[j][k];
						}
						// Pairwise distance for diagonal elements
						distances[j][j] = 0;
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
							for (int k = 0; k < columnSize; k++) {
								//System.out.println("row: " + j + " col: " + k);
								List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
										rowBlockSequences[j], colBlockSequences[k]);

								// We will just take the first alignment
								AlignedData ad = ads.get(0);
								short tmpDistance = DistanceUtil.computePercentIdentityDistanceAsShort(ad);
								if(seqType.equals("DNA")){
									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
											rowBlockSequences[j], colBlockSequences[k].getReverseComplementedSequence());
									ad = ads.get(0);
									short tempDNA = DistanceUtil.computePercentIdentityDistanceAsShort(ad);
									if(tmpDistance > DistanceUtil.computePercentIdentityDistanceAsShort(ad)){
										tmpDistance = tempDNA;
									}
								}
								distances[j][k] = tmpDistance;
							}
						}
					} else {
						// Diagonal block. Perform pairwise distance calculation only for one triangle
						for (int j = 0; j < rowSize; j++) {
							for (int k = 0; k < j; k++) {
								//System.out.println("Diagnose row: " + j + " col: " + k);
								List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
										rowBlockSequences[j], colBlockSequences[k]);
								AlignedData ad = ads.get(0);
								short tmpDistance = DistanceUtil.computePercentIdentityDistanceAsShort(ad);
								if(seqType.equals("DNA")){
									ads = aligner.align(ednafull, -gapOpen, -gapExt, 
											rowBlockSequences[j], colBlockSequences[k].getReverseComplementedSequence());
									ad = ads.get(0);
									short tempDNA = DistanceUtil.computePercentIdentityDistanceAsShort(ad);
									if(tmpDistance > DistanceUtil.computePercentIdentityDistanceAsShort(ad)){
										tmpDistance = tempDNA;
									}
								}
								distances[j][k] = tmpDistance;
								distances[k][j] = distances[j][k];
							}
							// Pairwise distance for diagonal elements
							distances[j][j] = 0;
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
			block.setDistances(distances, false);

			try {
				collector.collect(new StringKey("" + block.getRowBlockNumber()),
						new BytesValue(block.getBytes()));
			} catch (SerializationException e) {
				throw new TwisterException(e);
			}

			if (rowBlockNumber != columnBlockNumber) {
				// Creates a transpose block. Note. distances array should be treated as transpose when reading.
				Block transBlock = new Block(block.getColumnBlockNumber(), block.getRowBlockNumber());
				transBlock.setDistances(block.getDistances(), true);
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

