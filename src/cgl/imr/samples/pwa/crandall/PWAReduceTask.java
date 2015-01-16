package cgl.imr.samples.pwa.crandall;

import java.io.*;
import java.util.List;

import cgl.imr.base.Key;
import cgl.imr.base.ReduceOutputCollector;
import cgl.imr.base.ReduceTask;
import cgl.imr.base.SerializationException;
import cgl.imr.base.TwisterException;
import cgl.imr.base.Value;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.ReducerConf;
import cgl.imr.types.*;

/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 * @author Saliya Ekanayake (sekanaya at cs dot indiana dot edu)
 */
public class PWAReduceTask implements ReduceTask {

	private String outputPrefix;

	private int numOfSequences;
    private int numOfPartitions;
    private String dataDir;
    //private String idxFile;

	@Override
	public void close() throws TwisterException {
	}

	@Override
	public void configure(JobConf jobConf, ReducerConf mapConf)
			throws TwisterException {
        dataDir = jobConf.getProperty("dataDir");
		outputPrefix = jobConf.getProperty("outputPrefix");

		numOfSequences = Integer.parseInt(jobConf.getProperty("numOfSequences"));
        numOfPartitions = Integer.parseInt(jobConf.getProperty("numOfPartitions"));
        //idxFile = jobConf.getProperty("IdxFile");
	}

	@Override
	public void reduce(ReduceOutputCollector collector, Key key,
                       List<Value> values) throws TwisterException {

		
        int rowBlockNumber = Integer.parseInt(((StringKey) key).getString());
        int rowSize = numOfSequences / numOfPartitions;
        int remainder;
        if ((remainder = numOfSequences % numOfPartitions) > 0 && rowBlockNumber < remainder) {
            rowSize++;
        }


        Block[] blocks = new Block[values.size()];
        Block b;
        for (Value value : values) {
            try {
                b = new Block(value.getBytes());
                blocks[b.getColumnBlockNumber()] = b;
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        }

        String fname = dataDir + File.separator + outputPrefix + String.valueOf(rowBlockNumber);
        //System.out.println(idxFile);
//        try {
//        	BufferedWriter bw = new BufferedWriter(new FileWriter(idxFile, true));
//        	bw.write(rowBlockNumber + "\t");
//        	bw.write(rowSize + "\t");
//        	bw.write(blocks[0].getColSize() + "\t");
//        	bw.write(rowBlockNumber + "\t");
//        	if((remainder = numOfSequences % numOfPartitions) > 0 && rowBlockNumber >= remainder)
//        		bw.write(remainder * (rowSize + 1) + (rowBlockNumber - remainder) * rowSize + "\n");
//        	else
//        		bw.write(rowBlockNumber * rowSize + "\n");
//        	bw.flush();
//        	bw.close();
//        } catch (IOException e1) {
//        	// TODO Auto-generated catch block
//        	e1.printStackTrace();
//        }
        try {
        	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fname));
            DataOutputStream dos = new DataOutputStream(bos);
            short [][] distances;
            for (int i = 0; i < rowSize; i++) {
                for (Block block : blocks) {
                    distances = block.getDistances();

                    // Won't push "if" inside loop for the sake of performance
                    if (block.isTranspose()) {
                        for (int k = 0; k < block.getColSize(); k++) {
                            dos.writeShort(distances[k][i]);
                        }
                    } else {
                        for (int k = 0; k < block.getColSize(); k++) {
                            dos.writeShort(distances[i][k]);
                        }
                    }
                }
            }
            dos.flush();
            dos.close();
            collector.collect(key, new StringValue(fname));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
