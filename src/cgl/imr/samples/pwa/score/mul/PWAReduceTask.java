package cgl.imr.samples.pwa.score.mul;

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

        String fnameScore
        = dataDir + File.separator + outputPrefix + "score_" + String.valueOf(rowBlockNumber);
        String fnameLength
        = dataDir + File.separator + outputPrefix + "length_" + String.valueOf(rowBlockNumber);
        String fnameLengthNoEndGap
        = dataDir + File.separator + outputPrefix + "lengthNoEndGap_" + String.valueOf(rowBlockNumber);
        String fnameIdenticalPairs
        = dataDir + File.separator + outputPrefix + "identicalPairs_" + String.valueOf(rowBlockNumber);
        String fnameDistance
        = dataDir + File.separator + outputPrefix + "distance_" + String.valueOf(rowBlockNumber);
        try {
            DataOutputStream dosScore = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameScore)));
            DataOutputStream dosLength = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameLength)));
            DataOutputStream dosLengthNoEndGap = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameLengthNoEndGap)));
            DataOutputStream dosIdenticalPairs = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameIdenticalPairs)));
            DataOutputStream dosDistance = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameDistance)));
            
            int [][] score;
            short [][] length;
            short [][] lengthNoEndGap;
            short [][] identicalPairs;
            short [][] distances;
            for (int i = 0; i < rowSize; i++) {
                for (Block block : blocks) {
                    score = block.getScore();
                    length = block.getLength();
                    lengthNoEndGap = block.getLengthNoEndGap();
                    identicalPairs = block.getIdenticalPairs();
                    distances = block.getDistances();

                    // Won't push "if" inside loop for the sake of performance
                    if (block.isTranspose()) {
                        for (int k = 0; k < block.getColSize(); k++) {
                            dosScore.writeInt(score[k][i]);
                            dosLength.writeShort(length[k][i]);
                            dosLengthNoEndGap.writeShort(lengthNoEndGap[k][i]);
                            dosIdenticalPairs.writeShort(identicalPairs[k][i]);
                            dosDistance.writeShort(distances[k][i]);
                        }
                    } else {
                        for (int k = 0; k < block.getColSize(); k++) {
                        	dosScore.writeInt(score[i][k]);
                        	dosLength.writeShort(length[i][k]);
                        	dosLengthNoEndGap.writeShort(lengthNoEndGap[i][k]);
                        	dosIdenticalPairs.writeShort(identicalPairs[i][k]);
                        	dosDistance.writeShort(distances[i][k]);
                        }
                    }
                }
            }
            dosScore.close();
            dosLength.close();
            dosLengthNoEndGap.close();
            dosIdenticalPairs.close();
            dosDistance.close();
            collector.collect(key, new StringValue(fnameScore));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
