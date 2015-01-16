package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class ConvertIntBinToShortBin {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String inputBinMatrix = "/Users/Ryan/Documents/data/fasta/haixu/1291_74_420/matrices/1785_scoreA.bin";
		String outputTxtMatrix = "/Users/Ryan/Documents/data/fasta/haixu/1291_74_420/matrices/1785_scoreA_short.bin";
		BufferedInputStream bdis = new BufferedInputStream(new FileInputStream(inputBinMatrix));
		DataInputStream dis = new DataInputStream(bdis);
		short[][] matrix = new short[1785][1785];
		for(int i = 0; i < 1785; i++)
			for(int j = 0; j < 1785; j++)
				matrix[i][j] = (short)dis.readInt();
		
		
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream
				(new FileOutputStream(outputTxtMatrix)));
		for(int i = 0; i < 1785; i++){
			for(int j = 0; j < 1785; j++){
				dos.writeShort(matrix[i][j]);
			}
		}
		dos.close();
		System.out.println("Done");
	}

}
