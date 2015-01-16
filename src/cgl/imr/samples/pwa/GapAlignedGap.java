package cgl.imr.samples.pwa;

import java.util.List;

import cgl.imr.samples.pwa.util.GenePartition;

import edu.indiana.salsahpc.AlignedData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;
import edu.indiana.salsahpc.SimilarityMatrix;
import edu.indiana.salsahpc.SmithWatermanAligner;

public class GapAlignedGap {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String fastas = args[0];
		Sequence[] sequences = GenePartition.ConvertSequences(SequenceParser.parse(fastas, Alphabet.DNA));
		SimilarityMatrix ednafull = SimilarityMatrix.getBLOSUM62();
		int gapOpen = (short) 9;
		int gapExt = (short) 1;
		SmithWatermanAligner aligner = new SmithWatermanAligner();
		int count = 0;
		for(Sequence a : sequences){
			System.out.println("Checking Sequence "+count);
			for(Sequence b: sequences){
				List<AlignedData> ads = aligner.align(ednafull, -gapOpen, -gapExt, 
						a, b);
				// We will just take the first alignment
				AlignedData ad = ads.get(0);
				Sequence tmpA = ad.getFirstAlignedSequence();
				Sequence tmpB = ad.getSecondAlignedSequence();
				for(int i = 0; i < tmpA.getCount(); i++){
					
					if(tmpA.toString().charAt(i) == '-' 
						&&
							tmpB.toString().charAt(i) == '-'){
						System.out.println(tmpA.getId() + " " + tmpB.getId());
					}
				}
			}
			System.out.println("Sequence finished: " + count);
			count++;
		}
		
	}

}
