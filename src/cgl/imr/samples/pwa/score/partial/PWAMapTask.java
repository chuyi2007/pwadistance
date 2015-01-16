package cgl.imr.samples.pwa.score.partial;

import cgl.imr.base.*;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.MapperConf;
import cgl.imr.samples.pwa.util.GenePartition;
import cgl.imr.samples.pwa.util.SequenceAlignment;
import edu.indiana.salsahpc.AlignedData;
import edu.indiana.salsahpc.AlignmentData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.BioJavaWrapper;
import edu.indiana.salsahpc.MatrixUtil;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;
import edu.indiana.salsahpc.SimilarityMatrix;
import edu.indiana.salsahpc.SmithWatermanAligner;

import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.NucleotideCompound;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

	private SubstitutionMatrix scoringMatrix;
	private short gapOpen, gapExt;

	private String type;
	private String matrixType;
	private String seqType;
	private int mapNo;
	private String fullFastaFile;
	private String outputPrefix;
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
		mapNo = mapConf.getMapTaskNo();
		fullFastaFile = jobConf.getProperty("FullFastaFile");
		outputPrefix = jobConf.getProperty("outputPrefix");
	}

	@Override
	public void map(MapOutputCollector collector, Key key, Value val)
			throws TwisterException {
		String inputFastaFile = dataDir + "/" + geneBlockPrefix + mapNo;
		String outputScoreFile = dataDir +"/" + outputPrefix + "score_" + mapNo;
		String outputIdenticalPairFile = dataDir +"/" + outputPrefix + "identicalPairs_" + mapNo;
		String outputLengthFile = dataDir +"/" + outputPrefix + "lengths_" + mapNo;
		String outputTxtFile = dataDir + "/" + outputPrefix + "pidTxt_" + mapNo;
		
		Sequence[] rowBlockSequences = null;
		Sequence[] colBlockSequences = null;
		try {
			if(seqType.equals("DNA")){
				rowBlockSequences = GenePartition.ConvertSequences(SequenceParser.parse(inputFastaFile, Alphabet.DNA));
				colBlockSequences = GenePartition.ConvertSequences(SequenceParser.parse(fullFastaFile, Alphabet.DNA));
			} else if(seqType.equals("RNA")){
				rowBlockSequences = GenePartition.ConvertSequences(SequenceParser.parse(inputFastaFile, Alphabet.Protein));
				colBlockSequences = GenePartition.ConvertSequences(SequenceParser.parse(fullFastaFile, Alphabet.Protein));
			}
		} catch (Exception e) {
			InetAddress addr;
			try {
				addr = InetAddress.getLocalHost();
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

		// calculate the alignment length size
		if(type.equals("NW")){
			// Not a diagonal block. So have to do pairwise distance calculation for each pair
			if(matrixType.equals("blo")){
				scoringMatrix = MatrixUtil.getBlosum62();
				gapOpen = (short) 9;
				gapExt = (short) 1;
			}
			else if(matrixType.equals("edn")){
				scoringMatrix = MatrixUtil.getEDNAFULL();
				gapOpen = (short)16;
				gapExt=(short)4;
			}
			for (int j = 0; j < rowSize; j++) {
				for (int k = 0; k < columnSize; k++) {
					AlignmentData ad = SequenceAlignment.getNWAlignedData(
							rowBlockSequences[j].toString(), colBlockSequences[k].toString(),
							gapOpen , gapExt,
							scoringMatrix);
					score[j][k] = (short)ad.getScore();
					length[j][k] = (short)ad.getAlignmentLengthExcludingEndGaps();
					identicalPairs[j][k] = (short)ad.getNumIdenticals();
				}
			}
		}
		else if(type.equals("SWG")){
			try {
				SimilarityMatrix ednafull = null;
				//System.out.println("Begine to calculate!");
				if(matrixType.equals("blo")){
					ednafull = SimilarityMatrix.getBLOSUM62();
					gapOpen = (short) 9;
					gapExt = (short) 1;
				}
				else if(matrixType.equals("edn")){
					ednafull = SimilarityMatrix.getEDNAFULL();
					gapOpen = (short)16;
					gapExt = (short)4;
				}

				SmithWatermanAligner aligner = new SmithWatermanAligner();

				// Not a diagonal block. So have to do pairwise distance calculation for each pair
				for (int j = 0; j < rowSize; j++) {
					for (int k = 0; k < columnSize; k++) {
						//System.out.println("row: " + j + " col: " + k);
						Sequence sequenceA = rowBlockSequences[j];
						Sequence sequenceB = colBlockSequences[k];
						List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
								sequenceA, sequenceB);
						AlignedData ad = ads.get(0);
						short tmpScore = ad.getScore();
						short tmpLength = ad.getAlignmentLength();
						short tmpIdenticalPairs = ad.getNumberOfIdenticalBasePairs(false);

						if(seqType.equals("DNA")){
							ads = aligner.align(ednafull, -gapOpen, -gapExt, 
									sequenceA, sequenceB.getReverseComplementedSequence());
							ad = ads.get(0);
							if(tmpScore < ad.getScore()){
								tmpScore = ad.getScore();
								tmpLength = ad.getAlignmentLength();
								tmpIdenticalPairs = ad.getNumberOfIdenticalBasePairs(false);
							}
						}
						score[j][k] = tmpScore;
						length[j][k] = tmpLength;
						identicalPairs[j][k] = tmpIdenticalPairs;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			writeToFile(outputScoreFile, score);
			writeToFile(outputIdenticalPairFile, identicalPairs);
			writeToFile(outputLengthFile, length);
			short[][] pid = new short[rowSize][columnSize];
			for(int i = 0; i < rowSize; ++i)
				for(int j = 0; j < columnSize; ++j)
					pid[i][j] = (short)(Short.MAX_VALUE * (1 - (double)identicalPairs[i][j]/(double)length[i][j]));
			writeToFileTxt(outputTxtFile, pid, rowBlockSequences, colBlockSequences);
	}
	
	private static void writeToFileTxt(String outputFile, short[][] matrix, Sequence[] rowBlockSequences, 
			Sequence[] colBlockSequences){
        try {
        	BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            for(int i = 0; i < rowBlockSequences.length; ++i)
            	for(int j = 0; j < colBlockSequences.length; ++j)
            		bw.write(rowBlockSequences[i].getId() 
            				+ "\t" + colBlockSequences[j].getId()
            				+ "\t" + (double)matrix[i][j]/(double)Short.MAX_VALUE + "\n");
            bw.close();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	private static void writeToFile(String outputFile, short[][] matrix){
        try {
            DataOutputStream dosScore = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(outputFile)));
            int rowSize = matrix.length;
            int colSize = matrix[0].length;
            for (int i = 0; i < rowSize; i++) {
                for (int j = 0; j < colSize; j++) {
                	dosScore.writeShort(matrix[i][j]);
                }
            }
            dosScore.close();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
	}
}
