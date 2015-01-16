package cgl.imr.samples.pwa.quasar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class GetGroup {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if(args.length != 3){
			System.err.println("Input:" +
					"[original file] [output group file] [col index]");
			System.exit(2);
		}
		String inputFile = args[0];
		String outputFile =args[1];
		int index = Integer.parseInt(args[2]);

		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

		String line = null;
		String[] tokens;
		long startTime = System.currentTimeMillis();
		Map<String, Integer> map = new HashMap<String, Integer>();
		while((line = br.readLine()) != null){
			//line = br.readLine();
			tokens = line.split("\t");
			if (!map.containsKey(tokens[index])) {
				map.put(tokens[index], map.size() + 1);
			}
		}
		br.close();
		
		System.out.println("Get the map!");
		for (String key : map.keySet()) {
			System.out.println(key + ": " + map.get(key));
		}
		
		br = new BufferedReader(new FileReader(inputFile));
		while((line = br.readLine())!= null){
			tokens = line.split("\t");		
			bw.write(map.get(tokens[index]) + "\n"); 
			
		}
		bw.close();
		br.close();
		
		System.out.println("total time: " + (System.currentTimeMillis() - startTime) / 1000.0);
	}
}
