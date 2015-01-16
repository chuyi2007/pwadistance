package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PidToDistanceReverse {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 6) {
            System.err
                    .println("args:  [input indentical pair matrix] [input aligned length matrix] " +
                    		"[input indentical pair reverse matrix] [input aligned length reverse matrix] " +
                    		"[size] [output matrix file path]");
            System.exit(2);
        }
		String inputIPPath = args[0];
		String inputLengthPath = args[1];
		String inputIPReversePath = args[2];
		String inputLengthReversePath = args[3];
		int size = Integer.parseInt(args[4]);
		String outputPath = args[5];
		double start = System.currentTimeMillis();
		
		BufferedInputStream bIP = new BufferedInputStream(new FileInputStream(inputIPPath));
		DataInputStream dIP = new DataInputStream(bIP);

		BufferedInputStream bLength = new BufferedInputStream(new FileInputStream(inputLengthPath));
		DataInputStream dLength = new DataInputStream(bLength);

		BufferedInputStream bIPReverse = new BufferedInputStream(new FileInputStream(inputIPReversePath));
		DataInputStream dIPReverse = new DataInputStream(bIPReverse);

		BufferedInputStream bLengthReverse = new BufferedInputStream(new FileInputStream(inputLengthReversePath));
		DataInputStream dLengthReverse = new DataInputStream(bLengthReverse);
		
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(outputPath));
		DataOutputStream dos = new DataOutputStream(bdos);
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++){
				double tmpIP = dIP.readShort();
				double tmpLength = dLength.readShort();
				double tmpIPReverse = dIPReverse.readShort();
				double tmpLengthReverse = dLengthReverse.readShort();
				if(tmpLength < tmpLengthReverse){
					tmpIP = tmpIPReverse;
					tmpLength = tmpLengthReverse;
				}
				double distanceDouble = tmpIP /tmpLength;
				//System.out.println("ad: " + ad.getScore() +  " rowScore[j]" + rowScore[j]);
				short distanceShort = (short)((1 - distanceDouble) * Short.MAX_VALUE);
				dos.writeShort(distanceShort);
			}
		dos.close();
		System.out.println("Total Time: " + (System.currentTimeMillis() - start)/1000.0);
	
	}

}
