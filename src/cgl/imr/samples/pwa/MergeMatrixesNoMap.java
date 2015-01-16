package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 */
public class MergeMatrixesNoMap {

	public MergeMatrixesNoMap() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 7) {
            System.err
                    .println("args:  [input folder] [input prefix] [output matrix file path] [Row Number] [Column Number] " +
                            "[num_of_partitions] [type]");
            System.exit(2);
        }
        //String partitionFile = args[0];
		double start = System.currentTimeMillis();
        String inputFolder = args[0];
        String inputPrefix = args[1];
        String outputFile = args[2];
        int rowNumber = Integer.parseInt(args[3]);
        int colNumber = Integer.parseInt(args[4]);
        int partitionNumber = Integer.parseInt(args[5]);
        String type = args[6];
        if(!type.equals("short")&&!type.equals("double")&&!type.equals("int")){
        	System.err.println("The input type must be short,int or double!");
        	System.exit(2);
        }
        //Map<Key, Value> map = readMap(mapFile);
        if(type.equals("short"))
        	writeFinalMatrixShort(inputFolder, inputPrefix, outputFile, rowNumber, colNumber, partitionNumber);
        else if(type.equals("double"))
        	writeFinalMatrixDouble(inputFolder, inputPrefix, outputFile, rowNumber, colNumber, partitionNumber);
        else if(type.equals("int"))

        	writeFinalMatrixInt(inputFolder, inputPrefix, outputFile, rowNumber, colNumber, partitionNumber);
        System.out.println("merge matrix took " + (System.currentTimeMillis() - start)/1000.0 + " seconds!");
	}
	
	private static void writeFinalMatrixInt(String inputFolder, String inputPrefix, 
			String fname, int rowNumber, int colNumber, int numOfPartitions)
	throws IOException {
		int seqsPerPart = rowNumber/numOfPartitions;
		int remainder = rowNumber % numOfPartitions;
		//int suffix;
		int rowSize;

		BufferedInputStream bdis;
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(fname));
		DataOutputStream dos = new DataOutputStream(bdos);
		//DataInputStream dis;
		//DataOutputStream dos = new DataOutputStream(new FileOutputStream(fname));

		for (int x = 0; x < numOfPartitions; x++) {
			String f = (inputFolder + "/" + inputPrefix + x).replaceAll("//", "/");
			
			rowSize = x < remainder ? seqsPerPart + 1 : seqsPerPart;
			//dis = new DataInputStream(new FileInputStream(f));
			bdis = new BufferedInputStream(new FileInputStream(f));
			DataInputStream dis = new DataInputStream(bdis);
			System.out.println("Reading matrix: " + f);
			for (int i = 0; i < rowSize; i++){
				for (int j = 0; j < colNumber; j++) {
					dos.writeInt(dis.readInt());
				}
			}
			dis.close();
		}
		dos.flush();
		dos.close();
	}
	
	private static void writeFinalMatrixShort(String inputFolder, String inputPrefix, 
			String fname, int rowNumber, int colNumber, int numOfPartitions)
	throws IOException {
		int seqsPerPart = rowNumber/numOfPartitions;
		int remainder = rowNumber % numOfPartitions;
		//int suffix;
		int rowSize;

		BufferedInputStream bdis;
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(fname));
		DataOutputStream dos = new DataOutputStream(bdos);
		//DataInputStream dis;
		//DataOutputStream dos = new DataOutputStream(new FileOutputStream(fname));

		for (int x = 0; x < numOfPartitions; x++) {
			String f = (inputFolder + "/" + inputPrefix + x).replaceAll("//", "/");
			
			rowSize = x < remainder ? seqsPerPart + 1 : seqsPerPart;
			//dis = new DataInputStream(new FileInputStream(f));
			bdis = new BufferedInputStream(new FileInputStream(f));
			DataInputStream dis = new DataInputStream(bdis);
			
			for (int i = 0; i < rowSize; i++){
				for (int j = 0; j < colNumber; j++) {
					dos.writeShort(dis.readShort());
				}
			}
			dis.close();
		}
		dos.flush();
		dos.close();
	}
	
	private static void writeFinalMatrixDouble(String inputFolder, String inputPrefix, 
			String fname, int rowNumber, int colNumber, int numOfPartitions)
	throws IOException {
		int seqsPerPart = rowNumber/numOfPartitions;
		int remainder = rowNumber % numOfPartitions;
		//int suffix;
		int rowSize;

		BufferedInputStream bdis;
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(fname));
		DataOutputStream dos = new DataOutputStream(bdos);
		//DataInputStream dis;
		//DataOutputStream dos = new DataOutputStream(new FileOutputStream(fname));

		for (int x = 0; x < numOfPartitions; x++) {
			String f = (inputFolder + "/" + inputPrefix + x).replaceAll("//", "/");
			
			rowSize = x < remainder ? seqsPerPart + 1 : seqsPerPart;
			//dis = new DataInputStream(new FileInputStream(f));
			bdis = new BufferedInputStream(new FileInputStream(f));
			DataInputStream dis = new DataInputStream(bdis);
			
			for (int i = 0; i < rowSize; i++){
				for (int j = 0; j < colNumber; j++) {
					dos.writeDouble(dis.readDouble());
				}
			}
			dis.close();
		}
		dos.flush();
		dos.close();
	}

}
