package cgl.imr.samples.pwa.test;

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
    private String seqType;
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
        seqType = jobConf.getProperty("SeqType");
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

        String fnameScoreAB
        = dataDir + File.separator + outputPrefix + "scoreAB_" + String.valueOf(rowBlockNumber);
        String fnameScoreBA
        = dataDir + File.separator + outputPrefix + "scoreBA_" + String.valueOf(rowBlockNumber);
        String fnameLengthAB
        = dataDir + File.separator + outputPrefix + "lengthAB_" + String.valueOf(rowBlockNumber);
        String fnameLengthBA
        = dataDir + File.separator + outputPrefix + "lengthBA_" + String.valueOf(rowBlockNumber);
        String fnameIdenticalPairsAB
        = dataDir + File.separator + outputPrefix + "identicalPairsAB_" + String.valueOf(rowBlockNumber);
        String fnameIdenticalPairsBA
        = dataDir + File.separator + outputPrefix + "identicalPairsBA_" + String.valueOf(rowBlockNumber);
        
        String fnameScoreABReverse
        = dataDir + File.separator + outputPrefix + "scoreABReverse_" + String.valueOf(rowBlockNumber);
        String fnameScoreBAReverse
        = dataDir + File.separator + outputPrefix + "scoreBAReverse_" + String.valueOf(rowBlockNumber);
        String fnameScoreAReverseBReverse
        = dataDir + File.separator + outputPrefix + "scoreAReverseBReverse_" + String.valueOf(rowBlockNumber);
        String fnameScoreBReverseAReverse
        = dataDir + File.separator + outputPrefix + "scoreBReverseAReverse_" + String.valueOf(rowBlockNumber);
        String fnameLengthABReverse
        = dataDir + File.separator + outputPrefix + "lengthABReverse_" + String.valueOf(rowBlockNumber);
        String fnameLengthBAReverse
        = dataDir + File.separator + outputPrefix + "lengthBAReverse_" + String.valueOf(rowBlockNumber);
        String fnameLengthAReverseBReverse
        = dataDir + File.separator + outputPrefix + "lengthAReverseBReverse_" + String.valueOf(rowBlockNumber);
        String fnameLengthBReverseAReverse
        = dataDir + File.separator + outputPrefix + "lengthBReverseAReverse_" + String.valueOf(rowBlockNumber);
        String fnameIdenticalPairsABReverse
        = dataDir + File.separator + outputPrefix + "identicalPairsABReverse_" + String.valueOf(rowBlockNumber);
        String fnameIdenticalPairsBAReverse
        = dataDir + File.separator + outputPrefix + "identicalPairsBAReverse_" + String.valueOf(rowBlockNumber);
        String fnameIdenticalPairsAReverseBReverse
        = dataDir + File.separator + outputPrefix + "identicalPairsAReverseBReverse_" + String.valueOf(rowBlockNumber); 
        String fnameIdenticalPairsBReverseAReverse
        = dataDir + File.separator + outputPrefix + "identicalPairsBReverseAReverse_" + String.valueOf(rowBlockNumber);
        try {
            DataOutputStream dosScoreAB = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameScoreAB)));
            DataOutputStream dosScoreBA = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameScoreBA)));
            DataOutputStream dosLengthAB = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameLengthAB)));
            DataOutputStream dosLengthBA = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameLengthBA)));
            DataOutputStream dosIdenticalPairsAB = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameIdenticalPairsAB)));
            DataOutputStream dosIdenticalPairsBA = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameIdenticalPairsBA)));
            
            DataOutputStream dosScoreABReverse = null;
            DataOutputStream dosScoreBAReverse = null;
            DataOutputStream dosScoreAReverseBReverse = null;
            DataOutputStream dosScoreBReverseAReverse = null;
            DataOutputStream dosLengthABReverse = null;
            DataOutputStream dosLengthBAReverse = null;
            DataOutputStream dosLengthAReverseBReverse = null;
            DataOutputStream dosLengthBReverseAReverse = null;
            DataOutputStream dosIdenticalPairsABReverse = null;
            DataOutputStream dosIdenticalPairsBAReverse = null;
            DataOutputStream dosIdenticalPairsAReverseBReverse = null;
            DataOutputStream dosIdenticalPairsBReverseAReverse = null;
            if(seqType.equals("DNA")){
            	dosScoreABReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameScoreABReverse)));
            	dosScoreBAReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameScoreBAReverse)));
            	dosScoreAReverseBReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameScoreAReverseBReverse)));
            	dosScoreBReverseAReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameScoreBReverseAReverse)));
            	
                dosLengthABReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameLengthABReverse)));
                dosLengthBAReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameLengthBAReverse)));
                dosLengthAReverseBReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameLengthAReverseBReverse)));
                dosLengthBReverseAReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameLengthBReverseAReverse)));
                
                dosIdenticalPairsABReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameIdenticalPairsABReverse)));
                dosIdenticalPairsBAReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameIdenticalPairsBAReverse)));
                dosIdenticalPairsAReverseBReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameIdenticalPairsAReverseBReverse)));
                dosIdenticalPairsBReverseAReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameIdenticalPairsBReverseAReverse)));
            }
            
            short[][] scoreAB;
        	short[][] scoreBA;
        	short[][] lengthAB;
        	short[][] lengthBA;
        	short[][] identicalPairsAB;
        	short[][] identicalPairsBA;
        	
        	short[][] scoreABReverse;
        	short[][] scoreBAReverse;
        	short[][] scoreAReverseBReverse;
        	short[][] scoreBReverseAReverse;
        	short[][] lengthABReverse;
        	short[][] lengthBAReverse;
        	short[][] lengthAReverseBReverse;
        	short[][] lengthBReverseAReverse;
        	short[][] identicalPairsABReverse;
        	short[][] identicalPairsBAReverse;
        	short[][] identicalPairsAReverseBReverse;
        	short[][] identicalPairsBReverseAReverse;
            for (int i = 0; i < rowSize; i++) {
                for (Block block : blocks) {
                    scoreAB = block.getScoreAB();
                    scoreBA = block.getScoreBA();
                    lengthAB = block.getLengthAB();
                    lengthBA = block.getLengthBA();
                    identicalPairsAB = block.getIdenticalPairsAB();
                    identicalPairsBA = block.getIdenticalPairsBA();
                    scoreABReverse = block.getScoreABReverse();
                    scoreBAReverse = block.getScoreBAReverse();
                    scoreAReverseBReverse = block.getScoreAReverseBReverse();
                    scoreBReverseAReverse = block.getScoreBReverseAReverse();
                    lengthABReverse = block.getLengthABReverse();
                    lengthBAReverse = block.getLengthBAReverse();
                    lengthAReverseBReverse = block.getLengthAReverseBReverse();
                    lengthBReverseAReverse = block.getLengthBReverseAReverse();
                    identicalPairsABReverse = block.getIdenticalPairsABReverse();
                    identicalPairsBAReverse = block.getIdenticalPairsBAReverse();
                    identicalPairsAReverseBReverse = block.getIdenticalPairsAReverseBReverse();
                    identicalPairsBReverseAReverse = block.getIdenticalPairsBReverseAReverse();
                    
                    for (int k = 0; k < block.getColSize(); k++) {
                    	dosScoreAB.writeShort(scoreAB[i][k]);
                    	dosScoreBA.writeShort(scoreBA[i][k]);
                    	dosLengthAB.writeShort(lengthAB[i][k]);
                    	dosLengthBA.writeShort(lengthBA[i][k]);
                    	dosIdenticalPairsAB.writeShort(identicalPairsAB[i][k]);
                    	dosIdenticalPairsBA.writeShort(identicalPairsBA[i][k]);

                    	if(seqType.equals("DNA")){
                    		dosScoreABReverse.writeShort(scoreABReverse[i][k]);
                    		dosScoreBAReverse.writeShort(scoreBAReverse[i][k]);
                    		dosScoreAReverseBReverse.writeShort(scoreAReverseBReverse[i][k]);
                    		dosScoreBReverseAReverse.writeShort(scoreBReverseAReverse[i][k]);
                    		dosLengthABReverse.writeShort(lengthABReverse[i][k]);
                    		dosLengthBAReverse.writeShort(lengthBAReverse[i][k]);
                    		dosLengthAReverseBReverse.writeShort(lengthAReverseBReverse[i][k]);
                    		dosLengthBReverseAReverse.writeShort(lengthBReverseAReverse[i][k]);
                    		dosIdenticalPairsABReverse.writeShort(identicalPairsABReverse[i][k]);
                    		dosIdenticalPairsBAReverse.writeShort(identicalPairsBAReverse[i][k]);
                    		dosIdenticalPairsAReverseBReverse.writeShort(identicalPairsAReverseBReverse[i][k]);
                    		dosIdenticalPairsBReverseAReverse.writeShort(identicalPairsBReverseAReverse[i][k]);
                    	}
                    }

                }
            }
            dosScoreAB.close();
            dosLengthAB.close();
            dosIdenticalPairsAB.close();
            dosScoreBA.close();
            dosLengthBA.close();
            dosIdenticalPairsBA.close();
            if(seqType.equals("DNA")){
            	dosScoreABReverse.close();
        		dosScoreBAReverse.close();
        		dosScoreAReverseBReverse.close();
        		dosScoreBReverseAReverse.close();
        		dosLengthABReverse.close();
        		dosLengthBAReverse.close();
        		dosLengthAReverseBReverse.close();
        		dosLengthBReverseAReverse.close();
        		dosIdenticalPairsABReverse.close();
        		dosIdenticalPairsBAReverse.close();
        		dosIdenticalPairsAReverseBReverse.close();
        		dosIdenticalPairsBReverseAReverse.close();
            }
            collector.collect(key, new StringValue(fnameScoreAB));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
