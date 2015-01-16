package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class ConvertMatrixFromBinToText {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String inputBinMatrix = "/Users/Ryan/Documents/data/heatmap/1291+74+420/1785_pid_debug.bin";
		String outputTxtMatrix = "/Users/Ryan/Documents/data/heatmap/1291+74+420/1785_pid_debug_double.txt";
		BufferedInputStream bdis = new BufferedInputStream(new FileInputStream(inputBinMatrix));
		DataInputStream dis = new DataInputStream(bdis);
		short[][] matrix = new short[1785][1785];
		for(int i = 0; i < 1785; i++)
			for(int j = 0; j < 1785; j++)
				matrix[i][j] = dis.readShort();
		
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputTxtMatrix));
		for(int i = 0; i < 1785; i++){
			for(int j = 0; j < 1785; j++){
				bw.write((double)matrix[i][j]/(double)Short.MAX_VALUE + " ");
			}
			bw.write("\n");
		}
		bw.close();
		System.out.println("Done");
	}

}
