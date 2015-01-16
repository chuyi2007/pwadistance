package cgl.imr.samples.pwa.quasar.nonsym;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class NormalizedValue {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 4) {
			System.err
			.println("args:  [Input Matrix Path] [row] " +
			"[col] [Output Matrix Path]");
			System.exit(2);
		}

		String inputPath = args[0];
		int row = Integer.parseInt(args[1]);
		int col = Integer.parseInt(args[2]);
		String outputPath = args[3];
		
		DataInputStream din = new DataInputStream(
				new BufferedInputStream(new FileInputStream(inputPath)));
		
		double max = 0;
		for (int i = 0; i < row; ++i) {
			for (int j = 0; j < col; ++j) {
				max = Math.max(max, din.readDouble());
			}
		}
		
		din.close();
		
		System.out.println("The max value is: " + max);
		DataOutputStream dout = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputPath)));
		din = new DataInputStream(
				new BufferedInputStream(new FileInputStream(inputPath)));
		
		for (int i = 0; i < row; ++i) {
			for (int j = 0; j < col; ++j) {
				dout.writeShort((short) (din.readDouble() / max * Short.MAX_VALUE));
			}
		}
		din.close();
		dout.close();
	}
}
