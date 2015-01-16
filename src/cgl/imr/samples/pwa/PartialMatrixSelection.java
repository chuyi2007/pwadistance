package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PartialMatrixSelection {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 6) {
            System.err
                    .println("args:  [input matrix path] [total row] [total col]" +
                    		" [output matrix path] [partial row] [partial col]");
            System.exit(2);
        }
		String inputMatrix = args[0];
		int totalRow = Integer.parseInt(args[1]);
		int totalCol = Integer.parseInt(args[2]);
		String outputMatrix = args[3];
		int partialRow = Integer.parseInt(args[4]);
		int partialCol = Integer.parseInt(args[5]);
		
		DataInputStream din = new DataInputStream(
				new BufferedInputStream(new FileInputStream(inputMatrix)));

		DataOutputStream dout = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputMatrix)));
		
		long startTime = System.currentTimeMillis();
		for(int i = 0; i < totalRow; ++i) {
			for (int j = 0; j < totalCol; ++j) {
				short val = din.readShort();
				if(i < partialRow && j < partialCol)
					dout.writeShort(val);
			}
		}
		System.out.println("verification: " + din.available());
		din.close();
		dout.close();
		System.out.println("total time taken: " + (System.currentTimeMillis() - startTime)/1000.0);
	}
}
