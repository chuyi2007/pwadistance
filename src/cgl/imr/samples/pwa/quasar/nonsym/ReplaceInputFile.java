package cgl.imr.samples.pwa.quasar.nonsym;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReplaceInputFile {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 2) {
			System.err
			.println("args:  [Input File] [Output File] ");
			System.exit(2);
		}
		
		String inputFile = args[0];
		String outputFile = args[1];
		
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		
		String line = null;
		String[] tokens;
		
		while ((line = br.readLine()) != null) {
			tokens = line.split("\t");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 15; ++i) {
				sb.append(tokens[i] + "\t");
			}
			if (Double.parseDouble(tokens[15]) <= -0.999999) {
				sb.append(tokens[16] + "\t");
			}
			else {
				sb.append(tokens[15] + "\t");
			}
			
			for (int i = 16; i < tokens.length - 1; ++i) {
				sb.append(tokens[i] + "\t");
			}
			sb.append(tokens[tokens.length - 1]);
			bw.write(sb.toString() + "\n");
		}
		br.close();
		bw.close();
	}
}
