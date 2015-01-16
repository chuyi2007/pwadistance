package cgl.imr.samples.pwa.ps.nosym;

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
    private String outputDataDir;
    private String seqType;
    //private String idxFile;

	@Override
	public void close() throws TwisterException {
	}

	@Override
	public void configure(JobConf jobConf, ReducerConf mapConf)
			throws TwisterException {
        outputDataDir = jobConf.getProperty("outputDataDir");
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

        String fnameScore
        = outputDataDir + File.separator + outputPrefix + "score_" + String.valueOf(rowBlockNumber);
        String fnameLength
        = outputDataDir + File.separator + outputPrefix + "length_" + String.valueOf(rowBlockNumber);
        String fnameIdenticalPairs
        = outputDataDir + File.separator + outputPrefix + "identicalPairs_" + String.valueOf(rowBlockNumber);
        
        String fnameScoreReverse
        = outputDataDir + File.separator + outputPrefix + "scoreReverse_" + String.valueOf(rowBlockNumber);
        String fnameLengthReverse
        = outputDataDir + File.separator + outputPrefix + "lengthReverse_" + String.valueOf(rowBlockNumber);
        String fnameIdenticalPairsReverse
        = outputDataDir + File.separator + outputPrefix + "identicalPairsReverse_" + String.valueOf(rowBlockNumber);
        
        String fnameScoreA
        = outputDataDir + File.separator + outputPrefix + "scoreA_" + String.valueOf(rowBlockNumber);
        String fnameScoreAReverse
        = outputDataDir + File.separator + outputPrefix + "scoreAReverse_" + String.valueOf(rowBlockNumber);
        String fnameScoreB
        = outputDataDir + File.separator + outputPrefix + "scoreB_" + String.valueOf(rowBlockNumber);
        String fnameScoreBReverse
        = outputDataDir + File.separator + outputPrefix + "scoreBReverse_" + String.valueOf(rowBlockNumber);
        String fnameScoreNormal
        = outputDataDir + File.separator + outputPrefix + "scoreNormal_" + String.valueOf(rowBlockNumber);
        String fnameScoreNormalReverse
        = outputDataDir + File.separator + outputPrefix + "scoreNormalReverse_" + String.valueOf(rowBlockNumber);
        //System.out.println("Reducer reached!");
        try {
            DataOutputStream dosScore = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameScore)));
            DataOutputStream dosLength = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameLength)));
            DataOutputStream dosIdenticalPairs = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameIdenticalPairs)));
            DataOutputStream dosScoreA = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameScoreA)));
            DataOutputStream dosScoreB = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameScoreB)));
            DataOutputStream dosScoreNormal = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameScoreNormal)));
            
            DataOutputStream dosScoreReverse = null;
            DataOutputStream dosLengthReverse = null;
            DataOutputStream dosIdenticalPairsReverse = null;
            DataOutputStream dosScoreAReverse = null;
            DataOutputStream dosScoreBReverse = null;
            DataOutputStream dosScoreNormalReverse = null;
            if(seqType.equals("DNA")){
            	dosScoreReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameScoreReverse)));
                dosLengthReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameLengthReverse)));
                dosIdenticalPairsReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameIdenticalPairsReverse)));
                dosScoreAReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameScoreAReverse)));
                dosScoreBReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameScoreBReverse)));
                dosScoreNormalReverse = new DataOutputStream(
                		new BufferedOutputStream(new FileOutputStream(fnameScoreNormalReverse)));
            }
            
            short [][] score;
            short [][] length;
            short [][] identicalPairs;
            short [][] scoreReverse;
            short [][] lengthReverse;
            short [][] identicalPairsReverse;
            short [][] scoreA;
            short [][] scoreAReverse;
            short [][] scoreB;
            short [][] scoreBReverse;
            short [][] scoreNormal;
            short [][] scoreNormalReverse;
            for (int i = 0; i < rowSize; i++) {
            	//System.out.println("rowSize: " + i);
                for (Block block : blocks) {
                    score = block.getScore();
                    length = block.getLength();
                    identicalPairs = block.getIdenticalPairs();
                    scoreReverse = block.getScoreReverse();
                    lengthReverse = block.getLengthReverse();
                    identicalPairsReverse = block.getIdenticalPairsReverse();
                    scoreA = block.getScoreA();
                    scoreAReverse = block.getScoreAReverse();
                    scoreB = block.getScoreB();
                    scoreBReverse = block.getScoreBReverse();
                    scoreNormal = block.getScoreNormal();
                    scoreNormalReverse = block.getScoreNormalReverse();
                    // Won't push "if" inside loop for the sake of performance
                    //if (block.isTranspose()) {
                    for (int k = 0; k < block.getColSize(); k++) {
                    	try{
                    	dosScore.writeShort(score[i][k]);
                    	dosLength.writeShort(length[i][k]);
                    	dosIdenticalPairs.writeShort(identicalPairs[i][k]);
                    	dosScoreA.writeShort(scoreA[i][k]);
                    	dosScoreB.writeShort(scoreB[i][k]);
                    	dosScoreNormal.writeShort(scoreNormal[i][k]);
                    	if(seqType.equals("DNA")){
                    		dosScoreReverse.writeShort(scoreReverse[i][k]);
                    		dosLengthReverse.writeShort(lengthReverse[i][k]);
                    		dosIdenticalPairsReverse.writeShort(identicalPairsReverse[i][k]);
                    		dosScoreAReverse.writeShort(scoreAReverse[i][k]);
                    		dosScoreBReverse.writeShort(scoreBReverse[i][k]);
                    		dosScoreNormalReverse.writeShort(scoreNormalReverse[i][k]);
                    	}}
                    	catch(Exception e){
                    		System.out.println("The block size row:" + block.getRowSize() 
                    				+ "col: " + block.getColSize() + "Current index i: " + " k:" + k);
                    		e.printStackTrace();
                    	}
                    }
                }
            }
            dosScore.close();
            dosLength.close();
            dosIdenticalPairs.close();
            dosScoreA.close();
            dosScoreB.close();
            dosScoreNormal.close();
            if(seqType.equals("DNA")){
            	dosScoreReverse.close();
                dosLengthReverse.close();
                dosIdenticalPairsReverse.close();
                dosScoreAReverse.close();
                dosScoreBReverse.close();
                dosScoreNormalReverse.close();
            }
            collector.collect(key, new StringValue(fnameScore));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
