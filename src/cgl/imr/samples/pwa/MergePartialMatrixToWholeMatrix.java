package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import cgl.imr.types.StringValue;

public class MergePartialMatrixToWholeMatrix {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 7) {
            System.err
                    .println("args:  [input first diagnal matrix] " +
                    		"[input rectangle matrix] " +
                    		"[input second diagnosal matrix matrix] " +
                    		"[first size] [second size]" +
                    		"[output total matrix] [type]");
            System.exit(2);
        }
		double startTime = System.currentTimeMillis();
		String firstDiagnalMatrixPath = args[0];
		String rectangleMatrixPath = args[1];
		String secondDiagnalMatrixPath = args[2];
		int firstSize = Integer.parseInt(args[3]);
		int secondSize = Integer.parseInt(args[4]);
		String outputMatrixPath = args[5];
		String type = args[6];
		if(!type.equals("short")){
			System.err.println("Only support short right now");
			System.exit(2);
		}
		DataInputStream disRectangle = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(rectangleMatrixPath)));
		short[][] rectangleMatrix = new short[firstSize][secondSize];
		for(int i = 0; i < firstSize; i++)
			for(int j = 0; j < secondSize; j++){
				rectangleMatrix[i][j] = disRectangle.readShort();
			}
		disRectangle.close();
		
		disRectangle = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(rectangleMatrixPath)));
		DataInputStream disFirst = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(firstDiagnalMatrixPath)));
		DataInputStream disSecond = new DataInputStream(
				new BufferedInputStream(
						new FileInputStream(secondDiagnalMatrixPath)));
		DataOutputStream dos = new DataOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(outputMatrixPath)));

		for (int i = 0; i < firstSize + secondSize; i++) {
			for(int j = 0; j < firstSize + secondSize; j++){
				short tmp = -Short.MAX_VALUE;
				if(i < firstSize && j < firstSize){
					tmp = disFirst.readShort();
				}
				else if(i < firstSize && j >= firstSize){
					tmp = rectangleMatrix[i][j - firstSize];
				}
				else if(i >= firstSize && j < firstSize){
					tmp = rectangleMatrix[j][i - firstSize];
				}
				else if(i >=firstSize && j >= firstSize){
					tmp = disSecond.readShort();
				}
				dos.writeShort(tmp);	
			}
		}
		dos.close();
		disRectangle.close();
		disFirst.close();
		disSecond.close();
		System.out.println("Target partial to total matrix time: " + (System.currentTimeMillis() - startTime)/1000.0);
	}

}
