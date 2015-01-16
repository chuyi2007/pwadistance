package cgl.imr.samples.pwa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;

public class LengthCalculation {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 2) {
            System.err
                    .println("args:  [input fasta file] [output csv file]");
            System.exit(2);
        }
		List<Sequence> sequences = SequenceParser.parse(args[0], Alphabet.Protein);
		BufferedWriter bw = new BufferedWriter(new FileWriter(args[1]));
		for(int i = 0; i < sequences.size(); ++i)
			bw.write(sequences.get(i).getCount() + "\n");
		bw.close();
	}

}
