package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class RemovePartOfPartialMatrix {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 7) {
            System.err
                    .println("args:  [input matrix] [total rows] [total columns] [remove column start index] " +
                            "[remove column end index] [output matrix] [type]");
            System.exit(2);
        }
		double start = System.currentTimeMillis();
        String inputMatrix = args[0];
        int totalRows = Integer.parseInt(args[1]);
        int totalColumns = Integer.parseInt(args[2]);
        int startColumns = Integer.parseInt(args[3]);
        int endColumns = Integer.parseInt(args[4]);
        String outputMatrix  = args[5];
        String type = args[6];
        if(!type.equals("short")&&!type.equals("double")&&!type.equals("int")){
        	System.err.println("The input type must be short,int or double!");
        	System.exit(2);
        }
        if(type.equals("short"))
        	writeFinalMatrixShort(inputMatrix, totalRows, totalColumns, 
        			startColumns, endColumns, outputMatrix);
        System.out.println("Total Time: " + (System.currentTimeMillis() - start)/1000.0);
	}
	
	private static void writeFinalMatrixShort(String inputMatrixPath, int totalRows, 
			int totalColumns, int startColumns, int endColumns, String outputMatrixPath)
	throws IOException {
		BufferedInputStream bdis = new BufferedInputStream(new FileInputStream(inputMatrixPath));
		DataInputStream dis = new DataInputStream(bdis);
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(outputMatrixPath));
		DataOutputStream dos = new DataOutputStream(bdos);
		for (int i = 0; i < totalRows; i++){
			for (int j = 0; j < totalColumns; j++) {
				short value = dis.readShort();
				if(j<startColumns || j > endColumns)
					dos.writeShort(value);
			}
		}
		dis.close();
		dos.close();
	}

}
