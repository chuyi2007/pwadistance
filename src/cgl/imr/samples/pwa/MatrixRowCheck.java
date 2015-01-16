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

public class MatrixRowCheck {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if(args.length != 4){
			System.out.println("Input:" +
					"[Matrix File Path] [ith Rows] [total rows] [total cols]");
			System.exit(2);
		}
		File file = new File(args[0]);
		int index = Integer.parseInt(args[1]);
		int rows = Integer.parseInt(args[2]);
		int columns = Integer.parseInt(args[3]);
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(
				file));
		DataInputStream din = new DataInputStream(bin);
		short element;
		short max = 0, min = Short.MAX_VALUE;
		double avg = 0;
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < columns; j++){
				element = din.readShort();
				if(i == index) {
					System.out.print(element + "\t");
					if (max < element)
						max = element;
					if (min > element && element > 0)
						min = element;
					avg += element;
				}
			}
		}
		System.out.println("max: " + max);
		System.out.println("min: " + min);
		System.out.println("avg: " + (double) avg / rows);

		din.close();
		bin.close();
		System.exit(0);
	}

}
