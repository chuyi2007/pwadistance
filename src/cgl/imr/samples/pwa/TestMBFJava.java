package cgl.imr.samples.pwa;

import java.io.IOException;
import java.util.List;

import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;

import edu.indiana.salsahpc.AlignedData;
import edu.indiana.salsahpc.AlignmentData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.BioJavaWrapper;
import edu.indiana.salsahpc.DistanceType;
import edu.indiana.salsahpc.MatrixUtil;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SimilarityMatrix;
import edu.indiana.salsahpc.SmithWatermanAligner;

public class TestMBFJava {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		SimilarityMatrix 
				ednafull = SimilarityMatrix.getEDNAFULL();
		SmithWatermanAligner aligner = new SmithWatermanAligner();
		SubstitutionMatrix scoringMatrix = MatrixUtil.getBlosum62();
		Sequence sequenceA = new Sequence(
				"AAAGATGAAAAGAACTTTGAAAAGAGAGTTAAACAGTACGTGAAATTGTTGAAAGGGAAACGATTGAGGCCAGTCATGCCTGCGAGAAATCAATCAATTGATTGGGCTTTCTTTTGAATTCTTAGTTAGTTGGTGCACTTTTTCGCGTGGCAGGTTAGCATCAACTTTGATTGTTGTAAAATGGTGGTCGGAATGTGACTCTTTTAGAGTGTTATAGCCGATTACAGATGCAACGATCGAGGTTGAGGTTTGCGACAAATGCCTTTGGGCTACTTACCTTCCTTCTGATATGTTCACTTTGAAATGACAGCTTGCTGATATTTTAAAGGGCGTTCAATCAATAGGGTAGAGTAGGATAAATTTGTCAA", Alphabet.DNA);
		Sequence sequenceB = new Sequence(//"MGFYSGYSGGYSGGGYGSSFVLIVVLFILLIIVGATFLY", Alphabet.Protein);
				"AAAGATGAAAAGAACTTTGAAAAGAGAGTTAAACAGTACGTGAAATTGTTGAAAGGGAAACGATTGAGGCCAGTCATGCCTGCGAGAAATCAATCAATTGATTGGGTTTTCTTTTGAATTCTTAGTTAGTTGGTGCACTTTTTCGCGTGGCAGGTTAGCATCAACTTTGATTGTTGTAAAATGGTGGTCGGAATGTGACTCTTTTAGAGTGTTATAGCCGATTACAGATGCAACGATCGAGGTTGAGGTTTGCGACAAATGCCTTTGGGCTACTTACCTTCCTTCTGATATGTTCACTTTGAAATGACAGCTTGCTGATATTTTAAAGGGCGTTCAATCAATAGGGTAGAGTAGGATAAATTTGGCAA", Alphabet.DNA);

		//System.out.println(A.getReverseComplementedSequence().toString());
//		List<AlignedData> ads = aligner.align(ednafull, 9, 1, 
//				sequenceA, sequenceB);
//		AlignedData ad = null;
		short gapOpen = -16;
		short gapExt = -4;
		//AlignmentData ad = BioJavaWrapper.calculateAlignment(new ProteinSequence(sequenceA.toString()), 
		//		new ProteinSequence(sequenceB.toString()), 
		//		gapOpen, gapExt, scoringMatrix, DistanceType.PercentIdentity);

		List<AlignedData> ads = aligner.align(ednafull, gapOpen, gapExt, 
				sequenceA, sequenceB);
		AlignedData ad = ads.get(0);
		System.out.println("alignment1: " + ad.getFirstAlignedSequence());
		System.out.println("alignment2: " + ad.getSecondAlignedSequence());
		System.out.println("Score: " + (short)ad.getScore());
		System.out.println("identical " + ad.getNumberOfIdenticalBasePairs(false));
		System.out.println("length " + ad.getAlignmentLength());
		System.out.println("length: " + ad.getAlignmentLengthExcludingEndGaps());
		System.out.println("pid: " + 
		(1 - (double)ad.getNumberOfIdenticalBasePairs(false)
				/(double)ad.getAlignmentLengthExcludingEndGaps()));
//		}
//		catch(Exception e){
//			System.out.println("Exception:");
//			System.out.println("SequenceA: " + sequenceA.toString() + 
//					"####SequenceB:" + sequenceB.toString());
//			throw e;
//		}
		
	}
	private static Sequence getPartialSequence(int startIndex, int endIndex, Sequence sequence){
		Sequence partialSequence = null;
//		if(seqType.equals("DNA"))
//			partialSequence = new Sequence(sequence.toString().substring(startIndex, endIndex+1), 
//					sequence.getId(), Alphabet.DNA);
//		else if(seqType.equals("RNA"))
			partialSequence = new Sequence(sequence.toString().substring(startIndex, endIndex+1), 
					sequence.getId(), Alphabet.Protein);
		return partialSequence;
	}

}
