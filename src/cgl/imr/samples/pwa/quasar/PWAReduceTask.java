package cgl.imr.samples.pwa.quasar;

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

        String fname5d = dataDir + File.separator + outputPrefix + "dist_5d_" + String.valueOf(rowBlockNumber);
        String wname5d = dataDir + File.separator + outputPrefix + "weight_5d_" + String.valueOf(rowBlockNumber);
        String fname10d = dataDir + File.separator + outputPrefix + "dist_10d_" + String.valueOf(rowBlockNumber);
        String wname10d = dataDir + File.separator + outputPrefix + "weight_10d_" + String.valueOf(rowBlockNumber);
        
        try {
            DataOutputStream fdos5d = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fname5d)));
            DataOutputStream wdos5d = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(wname5d)));
            DataOutputStream fdos10d = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fname10d)));
            DataOutputStream wdos10d = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(wname10d)));
            
            double[][] dist5d = null;
            double[][] weight5d = null;
            double[][] dist10d = null;
            double[][] weight10d = null;

            System.out.println("Row Size: " + rowSize);
            System.out.println("Col Size: " + blocks[0].getColSize());
            System.out.println("Real Row Size: " + blocks[0].getDist5d().length);
            System.out.println("Real Col Size: " + blocks[0].getDist5d()[0].length);
            for (int i = 0; i < rowSize; i++) {
                for (Block block : blocks) {
                    dist5d = block.getDist5d();
                    dist10d = block.getDist10d();
                    weight5d = block.getWeight5d();
                    weight10d = block.getWeight10d();

                    // Won't push "if" inside loop for the sake of performance
                    if (block.isTranspose()) {
                        for (int k = 0; k < block.getColSize(); k++) {
                            fdos5d.writeDouble(dist5d[k][i]);
                            wdos5d.writeDouble(weight5d[k][i]);
                            fdos10d.writeDouble(dist10d[k][i]);
                            wdos10d.writeDouble(weight10d[k][i]);
                        }
                    } else {
                        for (int k = 0; k < block.getColSize(); k++) {
                            fdos5d.writeDouble(dist5d[i][k]);
                            wdos5d.writeDouble(weight5d[i][k]);
                            fdos10d.writeDouble(dist10d[i][k]);
                            wdos10d.writeDouble(weight10d[i][k]);
                        }
                    }
                }
            }
            fdos5d.close();
            wdos5d.close();
            fdos10d.close();
            wdos10d.close();
            collector.collect(key, new StringValue(fname5d));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
