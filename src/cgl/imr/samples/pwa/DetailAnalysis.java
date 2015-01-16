package cgl.imr.samples.pwa;

import java.io.IOException;
import java.util.List;

import edu.indiana.salsahpc.AlignedData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;
import edu.indiana.salsahpc.SimilarityMatrix;
import edu.indiana.salsahpc.SmithWatermanAligner;

public class DetailAnalysis {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 2) {
            System.err
                    .println("args:  [input whole fasta file] [output results]");
            System.exit(2);
        }
		List<Sequence> sequences = SequenceParser.parse(args[0], Alphabet.Protein);
		SimilarityMatrix ednafull = SimilarityMatrix.getEDNAFULL();
		SmithWatermanAligner aligner = new SmithWatermanAligner();
		for(Sequence sequenceA: sequences){
			List<AlignedData> ads = aligner.align(ednafull, -16, -4, 
					sequenceA, sequenceA);
			// We will just take the first alignment
			AlignedData ad = ads.get(0);
			System.out.print(ad.getScore() + " " + ad.getAlignmentLength() + " ");
			
			ads = aligner.align(ednafull, -16, -4, 
					sequenceA, sequences.get(0));
			// We will just take the first alignment
			ad = ads.get(0);
			System.out.println(ad.getScore() + " " + ad.getAlignmentLength() + " " + ad.getNumberOfIdenticalBasePairs(false));
		}
			
	}

}
