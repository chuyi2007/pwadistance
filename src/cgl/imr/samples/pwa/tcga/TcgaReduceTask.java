package cgl.imr.samples.pwa.tcga;

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
public class TcgaReduceTask implements ReduceTask {

	private String outputPrefix;

	private int numOfSequences;
    private int numOfPartitions;
    private String dataDir;
    private double maximumDistance;
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
        maximumDistance = - Double.MAX_VALUE;
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

        String fname = dataDir + File.separator + TcgaConstants.DOUBLE_PREFIX + outputPrefix + String.valueOf(rowBlockNumber);

        try {
        	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fname));
            DataOutputStream dos = new DataOutputStream(bos);
            double [][] distances;
            for (int i = 0; i < rowSize; i++) {
                for (Block block : blocks) {
                    distances = block.getDistances();

                    // Won't push "if" inside loop for the sake of performance
                    if (block.isTranspose()) {
                        for (int k = 0; k < block.getColSize(); k++) {
                            dos.writeDouble(distances[k][i]);
                            if(distances[k][i] > maximumDistance)
                            	maximumDistance = distances[k][i];
                        }
                    } else {
                        for (int k = 0; k < block.getColSize(); k++) {
                            dos.writeDouble(distances[i][k]);
                            if(distances[i][k] > maximumDistance)
                            	maximumDistance = distances[i][k];
                        }
                    }
                }
            }
            System.out.println("maximumDistance: " + maximumDistance);
            dos.flush();
            dos.close();
            collector.collect(key, new DoubleValue(maximumDistance));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
