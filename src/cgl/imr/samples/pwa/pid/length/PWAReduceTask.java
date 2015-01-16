package cgl.imr.samples.pwa.pid.length;

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
    private String lengthPrefix;
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
        lengthPrefix = jobConf.getProperty("lengthPrefix");
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

        String fnamePercent 
        = dataDir + File.separator + outputPrefix + "percent_" + String.valueOf(rowBlockNumber);
        String fnamePercentChoice
        = dataDir + File.separator + outputPrefix + "percentChoice_" + String.valueOf(rowBlockNumber);
        String fnameJukes 
        = dataDir + File.separator + outputPrefix + "jukes_" + String.valueOf(rowBlockNumber);
        String fnameJukesChoice
        = dataDir + File.separator + outputPrefix + "jukesChoice_" + String.valueOf(rowBlockNumber);
        String fnameKimaru 
        = dataDir + File.separator + outputPrefix + "kimaru_" + String.valueOf(rowBlockNumber);
        String fnameKimaruChoice
        = dataDir + File.separator + outputPrefix + "kimaruChoice_" + String.valueOf(rowBlockNumber);
        String lengthName = dataDir + File.separator + lengthPrefix + String.valueOf(rowBlockNumber);
        String lengthReverseName 
        = dataDir + File.separator + lengthPrefix + "reverse_" + String.valueOf(rowBlockNumber);
        try {
            DataOutputStream dosPercent = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnamePercent)));
            DataOutputStream dosPercentChoice = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnamePercentChoice)));
            DataOutputStream dosJukes = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameJukes)));
            DataOutputStream dosJukesChoice = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameJukesChoice)));
            DataOutputStream dosKimaru = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameKimaru)));
            DataOutputStream dosKimaruChoice = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(fnameKimaruChoice)));
            DataOutputStream dosLength = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(lengthName)));
            DataOutputStream dosLengthReverse = new DataOutputStream(
            		new BufferedOutputStream(new FileOutputStream(lengthReverseName)));
            
            short [][] percent;
            short [][] percentChoice;
            double [][] jukes;
            short [][] jukesChoice;
            double [][] kimaru;
            short [][] kimaruChoice;
            short [][] lengths;
            short [][] lengthsReverse;
            for (int i = 0; i < rowSize; i++) {
                for (Block block : blocks) {
                    percent = block.getPercent();
                    percentChoice = block.getPercentChoice();
                    jukes = block.getJukes();
                    jukesChoice = block.getJukesChoice();
                    kimaru = block.getKimura();
                    kimaruChoice = block.getKimuraChoice();
                    lengths = block.getLengths();
                    lengthsReverse = block.getLengthsReverse();

                    // Won't push "if" inside loop for the sake of performance
                    if (block.isTranspose()) {
                        for (int k = 0; k < block.getColSize(); k++) {
                            dosPercent.writeShort(percent[k][i]);
                            dosPercentChoice.writeShort(percentChoice[k][i]);
                            dosJukes.writeDouble(jukes[k][i]);
                            dosJukesChoice.writeShort(jukesChoice[k][i]);
                            dosKimaru.writeDouble(kimaru[k][i]);
                            dosKimaruChoice.writeShort(kimaruChoice[k][i]);
                            dosLength.writeShort(lengths[k][i]);
                            dosLengthReverse.writeShort(lengthsReverse[k][i]);
                        }
                    } else {
                        for (int k = 0; k < block.getColSize(); k++) {
                        	dosPercent.writeShort(percent[i][k]);
                            dosPercentChoice.writeShort(percentChoice[i][k]);
                            dosJukes.writeDouble(jukes[i][k]);
                            dosJukesChoice.writeShort(jukesChoice[i][k]);
                            dosKimaru.writeDouble(kimaru[i][k]);
                            dosKimaruChoice.writeShort(kimaruChoice[i][k]);
                            dosLength.writeShort(lengths[i][k]);
                            dosLengthReverse.writeShort(lengthsReverse[i][k]);
                        }
                    }
                }
            }
            dosPercent.flush();
            dosPercent.close();
            dosPercentChoice.flush();
            dosPercentChoice.close();
            dosJukes.flush();
            dosJukes.close();
            dosJukesChoice.flush();
            dosJukesChoice.close();
            dosKimaru.flush();
            dosKimaru.close();
            dosKimaruChoice.flush();
            dosKimaruChoice.close();
            dosLength.flush();
            dosLength.close();
            dosLengthReverse.flush();
            dosLengthReverse.close();
            collector.collect(key, new StringValue(fnamePercent));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
