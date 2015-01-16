/**
 * @author Yang Ruan (yangruan@indiana.edu)
 * PhD Candidate
 * Pervasive Technology Institute
 * Indiana University
 */
package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MatrixChecker {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if(args.length != 5){
			System.out.println("Input:" +
					"[Matrix File Path] [Block Index] [Rows] [Columns] [Diaganose(0:yes 1:no)]");
			System.exit(2);
		}
		int index = Integer.parseInt(args[1]);
		int rows = Integer.parseInt(args[2]);
		File file = new File(args[0]);
		int columns = Integer.parseInt(args[3]);
		int dia = Integer.parseInt(args[4]);
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(
				file));
		DataInputStream din = new DataInputStream(bin);
		//System.out.println(din.available());
		short element;
//		for(int i = 0; i < skippedSize; i++){
//			for(int j = 0; j < skippedSize; j++)
//				din.readShort();
//		}
		//int size = totalSize - skippedSize;
		if(dia == 0){
			for(int i = 0; i < rows; i++){
				for(int j = 0; j < columns; j++){
					element = din.readShort();
					
					if(element < 0 || element >= Short.MAX_VALUE - 1){
						System.out.print(i + "-" + j + ":" + element + "|");
					}
					//if(element == Short.MAX_VALUE){
						//System.out.println("=1 "+ i + " " + j + " " + element);
					//}
				}
				//System.out.println("-----------------------------------------------------------------------");
				//System.out.print(element + " ");
			}
		}
		else{
			int count = 0;
			for(int i = 0; i < rows; i++){
				for(int j = 0; j < columns; j++){
					element = din.readShort();
					if(element == Short.MAX_VALUE){
						count++;
					}
					System.out.print(element + "|");
				}
				System.out.println();
				//System.out.println("-----------------------------------------------------------------------");
				//System.out.print(element + " ");
			}
			System.out.println(count);
		}
		//for(int i = 0; i < size; i++)
		//	for(int j = 0; j < size; j++){
		//		element = din.readShort();
			
		//		if(element < 0 || element > Short.MAX_VALUE)
		//			System.out.println("The element in i: " + i + ", j: " + j + " is " + element);
		//	}
		din.close();
		bin.close();
		System.out.println("Finished!");
		System.exit(0);
	}

}
