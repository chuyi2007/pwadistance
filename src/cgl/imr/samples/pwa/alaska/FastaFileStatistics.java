package cgl.imr.samples.pwa.alaska;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;

public class FastaFileStatistics {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
            System.err
                    .println("args:  [input R1 fasta file] [input R2 fasta file]");
            System.exit(2);
        }
		List<Sequence> r1Seqs = SequenceParser.parse(args[0], Alphabet.Protein);
		List<Sequence> r2Seqs = SequenceParser.parse(args[1], Alphabet.Protein);
		System.out.println("R1 size: " + r1Seqs.size() + " R2 size: " + r2Seqs.size());
		
		Set<String> r1SeqsName = new HashSet<String>();
		for (Sequence s : r1Seqs) {
			String name = s.getId();
			String[] tokens = name.split(" ");
			r1SeqsName.add(tokens[0] + " 2" + tokens[1].substring(1)); 
		}
		
		int count = 0;
		for (Sequence s : r2Seqs) {
			if (r1SeqsName.contains(s.getId())) {
				++count;
			}
			else {
				//System.out.println(s.getId());
			}
		}
		System.out.println("total 2 found in 1: " + count);
		
		Set<String> r2SeqsName = new HashSet<String>();
		for (Sequence s : r2Seqs) {
			String name = s.getId();
			String[] tokens = name.split(" ");
			r2SeqsName.add(tokens[0] + " 1" + tokens[1].substring(1)); 
		}
		
		count = 0;
		for (Sequence s : r1Seqs) {
			if (r2SeqsName.contains(s.getId())) {
				++count;
			}
			else {
				//System.out.println(s.getId());
			}
		}
		System.out.println("total 1 found in 2: " + count);
	}
}
