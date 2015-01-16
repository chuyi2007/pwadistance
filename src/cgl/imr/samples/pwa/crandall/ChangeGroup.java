package cgl.imr.samples.pwa.crandall;

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
					"[sample coordinates file] [group file] [output file][clusterNumber]");
			System.exit(2);
		}
		String coordinatesFile = args[0];
		String groupFile =args[1];
		String outputFile = args[2];
		int clusterNum = Integer.parseInt(args[3]);

		BufferedReader br = new BufferedReader(new FileReader(coordinatesFile));
		BufferedReader brGroup = new BufferedReader(new FileReader(groupFile));

		String line = null;
		String lineGroup = null;
		String[] tokens;
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		
		while((lineGroup = brGroup.readLine()) != null){
			line = br.readLine();
			tokens = line.split("\t");
			
			bw.write(tokens[0] + "\t" + tokens[1] + "\t" + tokens[2] + "\t" 
			+ tokens[3] + "\t" + lineGroup.split("\t")[1] + "\t" + lineGroup.split("\t")[0] + "\n");
		}
		int count = 0;
		while((line = br.readLine())!= null){
			tokens = line.split("\t");
			
			bw.write(tokens[0] + "\t" + tokens[1] + "\t" + tokens[2] + "\t" 
			+ tokens[3] + "\t" + clusterNum + "\t" + "centeroid_"+ count + "\n");
			clusterNum++;
			count++;
		}
		bw.close();
		br.close();
		brGroup.close();
	}

}
