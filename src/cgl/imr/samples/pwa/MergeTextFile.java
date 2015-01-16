package cgl.imr.samples.pwa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MergeTextFile {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 4) {
            System.err
                    .println("args:  [input folder] [input file prefix] [file count] " +
                            "[output file]");
            System.exit(2);
        }
        //String partitionFile = args[0];
		double start = System.currentTimeMillis();
        String inputFolder = args[0];
        String inputPrefix = args[1];
        int fileCount = Integer.parseInt(args[2]);
        String outputFile = args[3];
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        for(int i = 0; i < fileCount; i++){
        	String fileName = (inputFolder + "/" + inputPrefix + i + ".out").replaceAll("//", "/");
        	BufferedReader br = new BufferedReader(new FileReader(fileName));
        	String line;
        	while((line = br.readLine())!= null){
        		bw.write(line + "\n");
        	}
        	br.close();
        }
        bw.close();
        System.out.println("merge matrix took " + (System.currentTimeMillis() - start)/1000.0 + " seconds!");
	}

}
