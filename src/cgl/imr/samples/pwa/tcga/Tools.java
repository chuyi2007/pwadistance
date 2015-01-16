package cgl.imr.samples.pwa.tcga;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tools {
	public static double[][] getInputBlocks(String fileName) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = null;
		String[] tokens;
		List<double[]> block = new ArrayList<double[]>();
		while((line = br.readLine()) != null){
			//System.out.println("count: " + line);
			tokens = line.split("\t");
			double[] oneRow = new double[tokens.length];
			for(int i = 0; i < tokens.length; i++)
				oneRow[i] = Double.parseDouble(tokens[i]);
			block.add(oneRow);
			
		}
		br.close();
		double[][] realBlock = new double[block.size()][block.get(0).length];
		for(int i = 0; i < block.size(); i++){
			realBlock[i] = block.get(i);
		}
		return realBlock;
	}
	
	public static String[] getLines(String fileName, int dataSize) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = null;
		String[] lines = new String[dataSize];
		int count = 0;
		while((line = br.readLine()) != null){
			lines[count] = line;
			count++;
		}
		br.close();
		
		return lines;
	}
	
	public static double getDistance(double[] oneRow, double[] oneColumn) throws Exception{
		double sum = 0;
		for(int i = 0; i < oneRow.length; i++){
			sum += (oneRow[i] - oneColumn[i]) * (oneRow[i] - oneColumn[i]);
		}
		
		double distance = Math.sqrt(sum);
		
		return distance;
	}
}
