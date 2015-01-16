package cgl.imr.samples.pwa.quasar;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileSplitter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 5) {
			System.err
					.println("args:  [Input File] [sequence_count] " +
							"[num_of_partitions] [out_dir] [gene_block_prefix] [output_idx file]");
			System.exit(2);
		}

		String seqFileName = args[0];
		int seqCount = Integer.parseInt(args[1]);
		int partitions = Integer.parseInt(args[2]);
		String outDir = args[3];
		String geneBlockPrefix = args[4];
		String idxFile = args[5];

		try {
			buildGeneBlocks(seqFileName, seqCount, partitions, outDir, geneBlockPrefix, idxFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void buildGeneBlocks(String fname, int dataSize, int partitions, String outDir,
			String blockPrefix, String idxFile) throws Exception {

        //SequenceParser parser = new SequenceParser();
        String[] lines = Tools.getLines(fname, dataSize);
        
        System.out.println("Total Sequences detected: " + lines.length);
        // Integer division automatically results in the floor of numOfSequences / partitions
        int minSeqsPerPartition = dataSize / partitions;
        int remainder = dataSize % partitions;

        int count = 0; // keep track of how many sequences are read and written back to disk
        int seqsPerPartition;
        //FileOutputStream fos;
        BufferedWriter bw;
        BufferedWriter idxWriter = new BufferedWriter(new FileWriter(idxFile));
		// loop for different blocks
		for (int i = 0; i < partitions; i++) {
            //fos = new FileOutputStream(new File(outDir + blockPrefix + i));
			idxWriter.write(i + "\t");
			bw = new BufferedWriter(new FileWriter(outDir + blockPrefix +i));
            seqsPerPartition = (remainder-- <= 0 ? minSeqsPerPartition : minSeqsPerPartition + 1);
            idxWriter.write(seqsPerPartition + "\t" + dataSize + "\t" + i + "\t" + count + "\n");
			// loop for one block
			for (int j = 0; j < seqsPerPartition; j++){
				bw.write(lines[count] + "\n");
				//FastaWriterHelper.writeSequence(fos, sequences[count]);
                count++;
			}
            bw.flush();
            bw.close();
		}
		idxWriter.flush();
		idxWriter.close();
	}

}
