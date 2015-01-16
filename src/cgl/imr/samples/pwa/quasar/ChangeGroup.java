package cgl.imr.samples.pwa.quasar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class ChangeGroup {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if(args.length != 4){
			System.err.println("Input:" +
					"[sample coordinates file] [group file] [output file] [line count]");
			System.exit(2);
		}
		String coordinatesFile = args[0];
		String groupFile =args[1];
		String outputFile = args[2];
		int maxCount = Integer.parseInt(args[3]);
		BufferedReader br = new BufferedReader(new FileReader(coordinatesFile));
		BufferedReader brGroup = new BufferedReader(new FileReader(groupFile));

		String line = null;
		String lineGroup = null;
		String[] tokens;
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		
		
		for (int i = 0; i < maxCount; ++i) {
			lineGroup = brGroup.readLine();	
			line = br.readLine();
			tokens = line.split("\t");
			
			bw.write(tokens[0] + "\t" + tokens[1] + "\t" + tokens[2] + "\t" 
			+ tokens[3] + "\t" + lineGroup + "\n");
		}
		bw.close();
		br.close();
		brGroup.close();
	}

}
