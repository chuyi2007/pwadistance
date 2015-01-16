package cgl.imr.samples.pwa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;

public class SelectSequenceByID {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 3) {
            System.err
                    .println("args:  [input whole fasta file] [Input ID] [output fasta file]");
            System.exit(2);
        }
		List<Sequence> sequences = SequenceParser.parse(args[0], Alphabet.Protein);
		BufferedReader br = new BufferedReader(new FileReader(args[1]));
		List<Integer> ids = new ArrayList<Integer>();
		String line;
		while((line = br.readLine())!= null){
			ids.add(Integer.parseInt(line));
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(args[2]));
		for(int i = 0; i < ids.size(); i++){
			bw.write(sequences.get(ids.get(i)).getId() + "\n");
			bw.write(sequences.get(ids.get(i)).toString() + "\n");
		}
		bw.close();
		
	}

}
