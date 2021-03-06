package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PidToDistance {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 5) {
            System.err
                    .println("args:  [input indentical pair matrix] [input aligned length matrix] " +
                    		"[row] [col] [output matrix file path]");
            System.exit(2);
        }
		String inputIPPath = args[0];
		String inputLengthPath = args[1];
		int row = Integer.parseInt(args[2]);
		int col = Integer.parseInt(args[3]);
		String outputPath = args[4];
		double start = System.currentTimeMillis();
		
		BufferedInputStream bIP = new BufferedInputStream(new FileInputStream(inputIPPath));
		DataInputStream dIP = new DataInputStream(bIP);

		BufferedInputStream bLength = new BufferedInputStream(new FileInputStream(inputLengthPath));
		DataInputStream dLength = new DataInputStream(bLength);
		
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(outputPath));
		DataOutputStream dos = new DataOutputStream(bdos);
		for(int i = 0; i < row; i++)
			for(int j = 0; j < col; j++){
				double tmpIP = dIP.readShort();
				double tmpLength = dLength.readShort();
				double distanceDouble = tmpIP /tmpLength;
				//System.out.println("ad: " + ad.getScore() +  " rowScore[j]" + rowScore[j]);
				short distanceShort = (short)((1 - distanceDouble) * Short.MAX_VALUE);
				dos.writeShort(distanceShort);
			}
		dos.close();
		System.out.println("Total Time: " + (System.currentTimeMillis() - start)/1000.0);
	
	}

}
