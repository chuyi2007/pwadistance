package cgl.imr.samples.pwa.large.sym;

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
import edu.indiana.salsahpc.MatrixUtil;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;
import edu.indiana.salsahpc.SimilarityMatrix;

import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;

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

		gapOpen = (short) 16;
		gapExt = (short) 4;
		type = jobConf.getProperty("Type");
		matrixType = jobConf.getProperty("MatrixType");
		seqType = jobConf.getProperty("SeqType");
	}

	@Override
	public void map(MapOutputCollector collector, Key key, Value val)
	throws TwisterException {
		Region region;
		try {
			region = new Region(val.getBytes());
		} catch (SerializationException e) {
			throw new TwisterException(e);
		}
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

			short[][] length = new short[rowSize][columnSize];
			short[][] identicalPairs = new short[rowSize][columnSize];
			
			// calculate the alignment length size
			if(type.equals("NW")){

				@SuppressWarnings("rawtypes")
				SubstitutionMatrix nwScoringMatrix = null;
				if(matrixType.equals("edn")){
					nwScoringMatrix = MatrixUtil.getEDNAFULL();
				}
				else if(matrixType.equals("blo")){
					nwScoringMatrix = MatrixUtil.getBlosum62();
					gapOpen = (short) 9;
					gapExt = (short) 1;
				}
				
				if (rowBlockNumber != columnBlockNumber) {
					// Not a diagonal block. So have to do pairwise distance calculation for each pair
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < columnSize; k++) {
							@SuppressWarnings({ "rawtypes", "unchecked" })
							AlignmentData ad = BioJavaWrapper.
							calculateAlignment(new ProteinSequence(rowBlockSequences[j].toString()), 
									new ProteinSequence(colBlockSequences[k].toString()), 
									gapOpen, gapExt, nwScoringMatrix, DistanceType.PercentIdentity);
							length[j][k] = (short)ad.getAlignmentLengthExcludingEndGaps();
							identicalPairs[j][k] = (short)ad.getNumIdenticals();
						}
					}
				} else {
					// Diagonal block. Perform pairwise distance calculation only for one triangle
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < j; k++) {
							@SuppressWarnings({ "rawtypes", "unchecked" })
							AlignmentData ad = BioJavaWrapper.
							calculateAlignment(new ProteinSequence(rowBlockSequences[j].toString()), 
									new ProteinSequence(colBlockSequences[k].toString()), 
									gapOpen, gapExt, nwScoringMatrix, DistanceType.PercentIdentity);
							length[j][k] = (short)ad.getAlignmentLengthExcludingEndGaps();
							identicalPairs[j][k] = (short)ad.getNumIdenticals();
						}
						// Pairwise distance for diagonal elements
						@SuppressWarnings({ "rawtypes", "unchecked" })
						AlignmentData ad = BioJavaWrapper.
								calculateAlignment(new ProteinSequence(rowBlockSequences[j].toString()), 
										new ProteinSequence(rowBlockSequences[j].toString()), 
										gapOpen, gapExt, nwScoringMatrix, DistanceType.PercentIdentity);

						length[j][j] = (short) ad.getAlignmentLengthExcludingEndGaps();
						identicalPairs[j][j] = (short) ad.getNumIdenticals();
					}
				}
			}
			else if(type.equals("SWG")){
				SimilarityMatrix swgScoringMatrix = null;
				try {
					if(matrixType.equals("blo")){
						swgScoringMatrix = SimilarityMatrix.getBLOSUM62();
						gapOpen = (short) 9;
						gapExt = (short) 1;
					}
					else if(matrixType.equals("edn"))
						swgScoringMatrix = SimilarityMatrix.getEDNAFULL();
	
					if (rowBlockNumber != columnBlockNumber) {
						// Not a diagonal block. So have to do pairwise distance calculation for each pair
						for (int j = 0; j < rowSize; j++) {
							for (int k = 0; k < columnSize; k++) {
								Sequence sequenceA = rowBlockSequences[j];
								Sequence sequenceB = colBlockSequences[k];
								List<AlignedData> ads = SequenceAlignment.
										getSWGAlignedData(sequenceA, 
												sequenceB, 
												gapOpen, gapExt, swgScoringMatrix);
								AlignedData ad = null;
								try{
									ad = ads.get(0);
								}
								catch(Exception e){
									System.out.println("Exception:");
									System.out.println("SequenceA: " + sequenceA.toString() + 
											"####SequenceB:" + sequenceB.toString());
								}
								length[j][k] = ad.getAlignmentLength();
								identicalPairs[j][k] = ad.getNumberOfIdenticalBasePairs(false);				
							}
						}
					} else {
						// Diagonal block. Perform pairwise distance calculation only for one triangle
						for (int j = 0; j < rowSize; j++) {
							for (int k = 0; k < j; k++) {
								Sequence sequenceA = rowBlockSequences[j];
								Sequence sequenceB = colBlockSequences[k];
								List<AlignedData> ads = SequenceAlignment.
										getSWGAlignedData(sequenceA, 
												sequenceB, 
												gapOpen, gapExt, swgScoringMatrix);
								// We will just take the first alignment
								AlignedData ad = ads.get(0);				          
								length[j][k] = ad.getAlignmentLength();
								length[k][j] = length[j][k];
								identicalPairs[j][k] = ad.getNumberOfIdenticalBasePairs(false);
								identicalPairs[k][j] = identicalPairs[j][k];
							}
							// Pairwise distance for diagonal elements
							List<AlignedData> ads = SequenceAlignment.
									getSWGAlignedData(rowBlockSequences[j], 
											rowBlockSequences[j], 
											gapOpen, gapExt, swgScoringMatrix);
							AlignedData ad = ads.get(0);
							length[j][j] = ad.getAlignmentLength();
							identicalPairs[j][j] = ad.getNumberOfIdenticalBasePairs(false);
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
			block.setLength(length, false);
			block.setIdenticalPairs(identicalPairs);
			try {
				collector.collect(new StringKey("" + block.getRowBlockNumber()),
						new BytesValue(block.getBytes()));
			} catch (SerializationException e) {
				throw new TwisterException(e);
			}

			if (rowBlockNumber != columnBlockNumber) {
				// Creates a transpose block. Note. distances array should be treated as transpose when reading.
				Block transBlock = new Block(block.getColumnBlockNumber(), block.getRowBlockNumber());
				transBlock.setLength(block.getLength(), true);
				transBlock.setIdenticalPairs(block.getIdenticalPairs());
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
