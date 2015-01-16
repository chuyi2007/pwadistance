package cgl.imr.samples.pwa.crandall;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckForDuplicates {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if(args.length != 4){
			System.err.println("Input: " +
					"[1.Original File Path] [2. Original data size]\n" +
					"Output: [3.Unique Sequence File Path] [4.Duplicates Sequence Name File Path]");
			System.exit(2);
		}
		double startTime = System.currentTimeMillis();
		String inputDataFile = args[0];
		int inputSize = Integer.parseInt(args[1]);
		String outputDataFile = args[2];
		String outputNameFile = args[3];
	
		System.out.println("Start reading file...");
		Map<String, String> uniqueData = new HashMap<String, String>();
		Map<String, List<String>> duplicateData = new HashMap<String, List<String>>();
		
		String[] inputData = Tools.getLines(inputDataFile, inputSize);

		List<String> temp;
		for(int i = 0; i < inputSize; i++){
			String line = inputData[i];
			String[] tokens = line.split("\t");
			String name = tokens[0];
			String data = tokens[1];
			if(!uniqueData.containsKey(data)){
				uniqueData.put(data, name);
				temp = new ArrayList<String>();
				duplicateData.put(uniqueData.get(data), temp);
			}
			else{
				if(duplicateData.containsKey(uniqueData.get(data))){
					temp = duplicateData.get(uniqueData.get(data));
					temp.add(name);
					duplicateData.put(uniqueData.get(data), temp);
				}
				else{
					System.err.println("Processing error!");
					System.exit(2);
				}
			}	
		}
		
		System.out.println("Processing complete! Unique sequence number: " + uniqueData.size());
		System.out.println("Start writing file...");
		BufferedWriter bwUniques = new BufferedWriter(new FileWriter(outputDataFile));
		BufferedWriter bwDuplicates = new BufferedWriter(new FileWriter(outputNameFile));
		int count = 0;
		for(int i = 0; i < inputSize; i++){
			//if(samples.containsKey(sampleSequences.get(i))){
			String name = inputData[i].split("\t")[0];
			String data = inputData[i].split("\t")[1];
			String sampleName =	uniqueData.get(data);
			if(sampleName == name){
				bwUniques.write(name+"\n");
				bwUniques.write(data+"\n");
				bwDuplicates.write(data + " " + (duplicateData.get(name).size() + 1) + " "+ name +" ");
				if(duplicateData.get(name).size() == 0)
					count++;
				for(String tmpName: duplicateData.get(name))
					bwDuplicates.write(tmpName + " ");
				bwDuplicates.write("\n");
			}
		}
		bwUniques.flush();
		bwDuplicates.flush();
		bwUniques.close();
		bwDuplicates.close();
		
		double endTime = System.currentTimeMillis();
		System.out.println("File writing complete! Reducible sequence number: " + count);
		System.out.println("Total Time (seconds): " + (endTime -startTime)/1000.0);
		System.exit(0);
	}

}
