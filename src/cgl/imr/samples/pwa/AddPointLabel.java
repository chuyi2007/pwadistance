package cgl.imr.samples.pwa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;

public class AddPointLabel {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 3) {
            System.err
                    .println("args:  [input fasta file] [Input Plot file] [length break size]");
            System.exit(2);
        }
		List<Sequence> sequences = SequenceParser.parse(args[0], Alphabet.Protein);
		BufferedWriter bw = new BufferedWriter(new FileWriter(args[1]));
		for(int i = 0; i < sequences.size(); i++){
			Sequence sequence = sequences.get(i);
			bw.write(sequence.toString().length()/Integer.parseInt(args[2]) + "\n");
		}
		bw.close();
	}

}
