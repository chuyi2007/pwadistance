package cgl.imr.samples.pwa.tcga;

import java.util.*;

import org.safehaus.uuid.UUIDGenerator;

import cgl.imr.base.KeyValuePair;
import cgl.imr.base.TwisterException;
import cgl.imr.base.TwisterMonitor;
import cgl.imr.base.impl.JobConf;
import cgl.imr.client.TwisterDriver;
import cgl.imr.types.BytesValue;
import cgl.imr.types.StringKey;

/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 */

public class PairWiseAlignment {

	private static UUIDGenerator uuidGen = UUIDGenerator.getInstance();
	
	private static double MAXIMUM_DISTANCE;
	public static double doublePWAMRDistance(int numOfMapTasks, int numOfReduceTasks,
			int numOfSequences, int numOfPartitions, String dataDir,
			String inputBlockPrefix, String outputPrefix)
	throws TwisterException {

		// Total computable blocks (n * (n + 1)) / 2 , i.e. lower or upper triangle blocks including diagonal blocks
		int numOfBlocks = (numOfPartitions * (numOfPartitions + 1)) / 2;

		System.out.println("Number of partitions: " + numOfPartitions);
		System.out.println("Number of computable blocks: " + numOfBlocks);

		List<Block> blocks = buildComputingBlocks(numOfPartitions);
		List<Region> regions = buildComputingRegions(blocks, numOfMapTasks);

		// JobConfigurations
		JobConf jobConf = new JobConf("Pairwise Euclidean Distance Calculation-" + uuidGen.generateTimeBasedUUID());
		jobConf.setMapperClass(TcgaMapTask.class);
		jobConf.setReducerClass(TcgaReduceTask.class);
		jobConf.setCombinerClass(TcgaCombiner.class);

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

			double start = System.currentTimeMillis();
			driver = new TwisterDriver(jobConf);
			driver.configureMaps();
			//System.out.println("Into mapreduce");
			monitor = driver
			.runMapReduce(getKeyValuePairsFrom(regions));
			monitor.monitorTillCompletion();
			MAXIMUM_DISTANCE = ((TcgaCombiner) driver
					.getCurrentCombiner()).getResults();
			driver.close();
			//writeFinalMatrix(outmap, outputFile,  numOfSequences,  numOfPartitions);
			System.out.println("This maximum distance found is: " + MAXIMUM_DISTANCE);
			double end = System.currentTimeMillis();
			System.out.println("The computation time is: " + (end - start)/1000.0);
			//String distanceFile = "distance_" + numOfSequences + "x" + numOfSequences + ".bin";


		} catch (Exception e) {
			if (driver != null) {
				driver.close();
			}
			throw new TwisterException(e);
		}
		return monitor.getTotalSequentialTimeSeconds();
	}
	
	public static void PWAMRDistanceScale(int numOfMapTasks, 
			int numOfSequences, int numOfPartitions, String dataDir, String outputPrefix, String idxFile)
	throws TwisterException {

		// JobConfigurations
		JobConf jobConf = new JobConf("Pairwise Distance Scale-" + uuidGen.generateTimeBasedUUID());
		jobConf.setMapperClass(TcgaScaleMapTask.class);

		jobConf.setNumMapTasks(numOfMapTasks);
		jobConf.setNumReduceTasks(0);

		jobConf.addProperty("dataDir", dataDir);
		jobConf.addProperty("outputPrefix", outputPrefix);

		jobConf.addProperty("numOfSequences", String.valueOf(numOfSequences));
		jobConf.addProperty("numOfPartitions", String.valueOf(numOfPartitions));
		jobConf.addProperty("maximumDistance", String.valueOf(MAXIMUM_DISTANCE));
		jobConf.addProperty("idxFile", idxFile);
		jobConf.setFaultTolerance();

		TwisterDriver driver = null;
		TwisterMonitor monitor;
		try {

			double start = System.currentTimeMillis();
			driver = new TwisterDriver(jobConf);
			driver.configureMaps();
			//System.out.println("Into mapreduce");
			monitor = driver
			.runMapReduce();
			monitor.monitorTillCompletion();
			driver.close();
			double end = System.currentTimeMillis();
			System.out.println("The scaling I/O time is: " + (end - start)/1000.0);
		} catch (Exception e) {
			if (driver != null) {
				driver.close();
			}
			throw new TwisterException(e);
		}
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
			"[num_of_partitions] [data_dir] [input_block_prefix] [tmp_output_prefix] [idx File]");
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
		String idxFile = args[7];

		if ( !outputPrefix.endsWith("_") ) {
			System.err.println("ERROR: The output file prefix must end with an underscore (\"_\").");
			System.exit(2);
		}
		long beforeTime = System.currentTimeMillis();
		double sequentialTime = 0;
		try {
			sequentialTime = doublePWAMRDistance(numOfMapTasks, numOfReduceTasks, numOfSeqs,
					numOfPartitions, dataDir, inputBlockPrefix, outputPrefix);
			PWAMRDistanceScale(numOfMapTasks, numOfSeqs,
					numOfPartitions, dataDir, outputPrefix, idxFile);

		} catch (TwisterException e) {
			e.printStackTrace();
		}

		double timeInSeconds = ((double) (System.currentTimeMillis() - beforeTime)) / 1000;
		System.out.println("Total Time Cost for Pairwise Alignment: " + timeInSeconds + " Seconds");
		System.out.println("Sequential Time = (Sigma mapTime + Sigma reduce time: " + sequentialTime + " Seconds");
		System.exit(0);
	}
}