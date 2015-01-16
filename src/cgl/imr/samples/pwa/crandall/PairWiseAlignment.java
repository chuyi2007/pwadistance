package cgl.imr.samples.pwa.crandall;

import java.io.*;
import java.util.*;

import org.safehaus.uuid.UUIDGenerator;

import cgl.imr.base.Key;
import cgl.imr.base.KeyValuePair;
import cgl.imr.base.TwisterException;
import cgl.imr.base.TwisterMonitor;
import cgl.imr.base.Value;
import cgl.imr.base.impl.GenericCombiner;
import cgl.imr.base.impl.JobConf;
import cgl.imr.client.TwisterDriver;
import cgl.imr.types.BytesValue;
import cgl.imr.types.StringKey;
import cgl.imr.types.StringValue;

/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 */

public class PairWiseAlignment {

	private static UUIDGenerator uuidGen = UUIDGenerator.getInstance();

	public static double drivePWAMapReduce(int numOfMapTasks, int numOfReduceTasks,
			int numOfSequences, int numOfPartitions, String dataDir,
			String inputBlockPrefix, String outputPrefix, String outputFile)
	throws TwisterException {

		// Total computable blocks (n * (n + 1)) / 2 , i.e. lower or upper triangle blocks including diagonal blocks
		int numOfBlocks = (numOfPartitions * (numOfPartitions + 1)) / 2;

		System.out.println("Number of partitions: " + numOfPartitions);
		System.out.println("Number of computable blocks: " + numOfBlocks);

		List<Block> blocks = buildComputingBlocks(numOfPartitions);
		List<Region> regions = buildComputingRegions(blocks, numOfMapTasks);

		// JobConfigurations
		JobConf jobConf = new JobConf("Pairwise Alignment-" + uuidGen.generateTimeBasedUUID());
		jobConf.setMapperClass(PWAMapTask.class);
		jobConf.setReducerClass(PWAReduceTask.class);
		jobConf.setCombinerClass(GenericCombiner.class);

		jobConf.setNumMapTasks(numOfMapTasks);
		jobConf.setNumReduceTasks(numOfReduceTasks);

		jobConf.addProperty("dataDir", dataDir);
		jobConf.addProperty("inputBlockPrefix", inputBlockPrefix);
		jobConf.addProperty("outputPrefix", outputPrefix);

		jobConf.addProperty("numOfSequences", String.valueOf(numOfSequences));
		jobConf.addProperty("numOfPartitions", String.valueOf(numOfPartitions));
		jobConf.setFaultTolerance();

		TwisterDriver driver = null;
		TwisterMonitor monitor;
		try {
			driver = new TwisterDriver(jobConf);
			driver.configureMaps();
			//System.out.println("Into mapreduce");
			monitor = driver
			.runMapReduce(getKeyValuePairsFrom(regions));
			monitor.monitorTillCompletion();
			double start = System.currentTimeMillis();
			Map<Key, Value> outmap = ((GenericCombiner) driver
					.getCurrentCombiner()).getResults();
			driver.close();
			//writeFinalMatrix(outmap, outputFile,  numOfSequences,  numOfPartitions);
			writeMapFile(outmap, outputFile);
			double end = System.currentTimeMillis();
			System.out.println("The writing file time is: " + (end - start)/1000.0);
			//String distanceFile = "distance_" + numOfSequences + "x" + numOfSequences + ".bin";


		} catch (Exception e) {
			if (driver != null) {
				driver.close();
			}
			throw new TwisterException(e);
		}
		return monitor.getTotalSequentialTimeSeconds();
	}

	private static void writeMapFile(Map<Key, Value> map, String fileName) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		Iterator<Key> ite = map.keySet().iterator();
		while(ite.hasNext()){
			StringKey key = (StringKey)ite.next();
			StringValue val = (StringValue)map.get(key);
			bw.write(key.getString() + " " + val.toString() + "\n");
		}
		bw.flush();
		bw.close();
	}

	private static List<Block> buildComputingBlocks(int numOfPartitions) {
		List<Block> blocks = new ArrayList<Block>();
		for (int row = 0; row < numOfPartitions; row++) {
			for (int column = 0; column < numOfPartitions; column++) {

				if (((row >= column) & ((row + column) % 2 == 0))
						| ((row <= column) & ((row + column) % 2 == 1))) {

					Block c = new Block(row, column);
					blocks.add(c);
				}
			}
		}
		return blocks;
	}


	private static List<Region> buildComputingRegions(List<Block> blocks,
			int numMapTasks) {
		List<Region> regions = new ArrayList<Region>(numMapTasks);
		int numOfBlocks = blocks.size();

		int minBlocksPerMap = numOfBlocks / numMapTasks;
		int remainder = numOfBlocks % numMapTasks;

		int count = 0;
		int blocksPerMap;

		Region region;
		for (int i = 0; i < numMapTasks; i++) {
			blocksPerMap = (remainder-- <= 0) ? minBlocksPerMap : minBlocksPerMap + 1;

			region = new Region();
			for (int j = 0; j < blocksPerMap; j++) {
				region.addBlock(blocks.get(count));
				count++;
			}
			regions.add(region);
		}
		return regions;
	}

	private static List<KeyValuePair> getKeyValuePairsFrom(
			List<Region> regions) throws Exception {
		List<KeyValuePair> keyValues = new ArrayList<KeyValuePair>();

		StringKey key;
		BytesValue value;

		int numOfRegions = regions.size();

		for (int i = 0; i < numOfRegions; i++) {
			key = new StringKey("" + i);
			value = new BytesValue(regions.get(i).getBytes());
			keyValues.add(new KeyValuePair(key, value));
		}
		return keyValues;
	}

	public static void main(String args[]) {

		if (args.length != 8) {
			System.err
			.println("args:  [num_of_map_tasks] [num_of_reduce_tasks] [data_size_count] " +
			"[num_of_partitions] [data_dir] [input_block_prefix] [tmp_output_prefix] [output_map_file]");
			System.exit(2);
		}

		//String partitionFile = args[0];
		int numOfMapTasks = Integer.parseInt(args[0]);
		int numOfReduceTasks = Integer.parseInt(args[1]);
		int numOfSeqs = Integer.parseInt(args[2]);
		int numOfPartitions = Integer.parseInt(args[3]);
		String dataDir = args[4];
		String inputBlockPrefix = args[5];
		String outputPrefix = args[6];
		String outputFile = args[7];

		if ( !outputPrefix.endsWith("_") ) {
			System.err.println("ERROR: The output file prefix must end with an underscore (\"_\").");
			System.exit(2);
		}
		long beforeTime = System.currentTimeMillis();
		double sequentialTime = 0;
		try {
			sequentialTime = drivePWAMapReduce(numOfMapTasks, numOfReduceTasks, numOfSeqs,
					numOfPartitions, dataDir, inputBlockPrefix, outputPrefix, outputFile);

		} catch (TwisterException e) {
			e.printStackTrace();
		}

		double timeInSeconds = ((double) (System.currentTimeMillis() - beforeTime)) / 1000;
		System.out.println("Total Time for Pairwise Alignment: " + timeInSeconds + " Seconds");
		System.out.println("Sequential Time = (Sigma mapTime + Sigma reduce time: " + sequentialTime + " Seconds");
		System.exit(0);
	}
}