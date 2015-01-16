package cgl.imr.samples.pwa.crandall;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tools {
	public static short[][] getInputBlocks(String fileName) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = null;
		String[] tokens;
		List<short[]> block = new ArrayList<short[]>();
		while((line = br.readLine()) != null){
			//System.out.println("count: " + line);
			tokens = line.split("\t");
			tokens = tokens[1].split(" ");
			short[] oneRow = new short[tokens.length];
			for(int i = 0; i < tokens.length; i++)
				oneRow[i] = Short.parseShort(tokens[i]);
			block.add(oneRow);
			
		}
		br.close();
		short[][] realBlock = new short[block.size()][block.get(0).length];
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
	
	public static short getDistance(short[] oneRow, short[] oneColumn) throws Exception{
		int sum = 0;
		for(int i = 0; i < oneRow.length; i++){
			sum += (oneRow[i] - oneColumn[i]) * (oneRow[i] - oneColumn[i]);
		}
		
		int distance = (int)Math.sqrt(sum);
		if(distance > Short.MAX_VALUE){
			System.out.println("Distance is larger than Short Max Value");
			//throw new Exception();
		}
		
		return (short)distance;
	}
}
