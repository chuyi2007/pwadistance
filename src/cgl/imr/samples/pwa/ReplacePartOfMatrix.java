package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReplacePartOfMatrix {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 8) {
            System.err
                    .println("args:  [input partial matrix] [input total matrix] [partial rows] [total columns] " +
                    		"[replace column start index] " +
                            "[replace column end index] [output partial matrix] [type]");
            System.exit(2);
        }
		double start = System.currentTimeMillis();
        String inputMatrix = args[0];
        String inputTotalMatrix = args[1];
        int totalRows = Integer.parseInt(args[2]);
        int totalColumns = Integer.parseInt(args[3]);
        int startColumns = Integer.parseInt(args[4]);
        int endColumns = Integer.parseInt(args[5]);
        String outputMatrix  = args[6];
        String type = args[7];
        if(!type.equals("short")&&!type.equals("double")&&!type.equals("int")){
        	System.err.println("The input type must be short,int or double!");
        	System.exit(2);
        }
        if(totalRows + startColumns != endColumns + 1){
        	System.err.println("Input error! The rows and columns is not right!");
        	System.exit(2);
        }
        if(type.equals("short"))
        	writeFinalMatrixShort(inputMatrix, inputTotalMatrix, totalRows, totalColumns, 
        			startColumns, endColumns, outputMatrix);
        System.out.println("Total Time: " + (System.currentTimeMillis() - start)/1000.0);
	}
	
	private static void writeFinalMatrixShort(String inputMatrixPath, String inputTotalMatrixPath, int totalRows, 
			int totalColumns, int startColumns, int endColumns, String outputMatrixPath)
	throws IOException {
		BufferedInputStream bdisPartial = new BufferedInputStream(new FileInputStream(inputMatrixPath));
		DataInputStream disPartial = new DataInputStream(bdisPartial);
		BufferedInputStream bdisTotal = new BufferedInputStream(new FileInputStream(inputTotalMatrixPath));
		DataInputStream disTotal = new DataInputStream(bdisTotal);
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(outputMatrixPath));
		DataOutputStream dos = new DataOutputStream(bdos);
		short valueTotal, valuePartial;
		for (int i = 0; i < totalRows + startColumns; i++){
			for (int j = 0; j < totalColumns; j++) {
				valueTotal = disTotal.readShort();
				if(i>=startColumns && i <= endColumns){
					valuePartial = disPartial.readShort();
					if(j>=startColumns && j <= endColumns)
						dos.writeShort(valueTotal);
					else
						dos.writeShort(valuePartial);
				}
			}
		}
		disPartial.close();
		disTotal.close();
		dos.close();
	}

}
