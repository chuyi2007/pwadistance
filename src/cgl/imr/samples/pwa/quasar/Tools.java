package cgl.imr.samples.pwa.quasar;

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
			for(int i = 0; i < tokens.length; i++) {
				try {
					oneRow[i] = Double.parseDouble(tokens[i]);
				}
				catch (Exception e) {
					oneRow[i] = 0;
				}
			}
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
	
	public static double getDist(double[] oneRow, double[] oneColumn, int[] dimensions) throws Exception{
		double sum = 0;
		//System.out.println("row: " + oneRow.length);
		//System.out.println("col: " + oneColumn.length);
		for(int i = 0; i < dimensions.length; i++){
			
			double val = (oneRow[dimensions[i]] - oneColumn[dimensions[i]]) * 
					(oneRow[dimensions[i]] - oneColumn[dimensions[i]]);
			if (dimensions[i] == 15)
				val *= 40;
			sum += val;
		}
		
		double distance = Math.sqrt(sum);
		if(distance > Short.MAX_VALUE) {
			System.out.println("Distance is larger than Short Max Value");
			//throw new Exception();
		}
		return distance;
	}
	
	public static double getError(double[] oneRow, double[] oneColumn, int[] dimensions) throws Exception{
		double sum = 0;
		for(int i = 0; i < dimensions.length; i++){
			sum += (oneRow[dimensions[i]] + oneColumn[dimensions[i]]) 
					* (oneRow[dimensions[i]] + oneColumn[dimensions[i]]);
		}
		
		double distance = Math.sqrt(sum);
		if(distance > Short.MAX_VALUE){
			System.out.println("Error is larger than Short Max Value");
			//throw new Exception();
		}
		return distance;
	}
}
