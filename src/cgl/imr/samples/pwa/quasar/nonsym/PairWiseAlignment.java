package cgl.imr.samples.pwa.quasar.nonsym;

import java.util.Map;

import org.safehaus.uuid.UUIDGenerator;

import cgl.imr.base.Key;
import cgl.imr.base.TwisterException;
import cgl.imr.base.TwisterMonitor;
import cgl.imr.base.Value;
import cgl.imr.base.impl.GenericCombiner;
import cgl.imr.base.impl.JobConf;
import cgl.imr.client.TwisterDriver;
import cgl.imr.types.StringValue;

/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 */

public class PairWiseAlignment {

	private static UUIDGenerator uuidGen = UUIDGenerator.getInstance();

	public static MaxValues drivePWAMapReduce(int numOfMapTasks,
			int numOfSequences, int numOfPartitions, String dataDir,
			String inputBlockPrefix, String outputPrefix, String fullFilePath)
	throws TwisterException {
		System.out.println("Number of partitions: " + numOfPartitions);

		// JobConfigurations
		JobConf jobConf = new JobConf("Pairwise Alignment-" + uuidGen.generateTimeBasedUUID());
		jobConf.setMapperClass(PWAMapTask.class);
		jobConf.setReducerClass(PWAReduceTask.class);
		jobConf.setCombinerClass(GenericCombiner.class);
		
		jobConf.setNumMapTasks(numOfMapTasks);
		jobConf.setNumReduceTasks(1);

		jobConf.addProperty("dataDir", dataDir);
		jobConf.addProperty("inputBlockPrefix", inputBlockPrefix);
		jobConf.addProperty("outputPrefix", outputPrefix);
		
		jobConf.addProperty("FullFilePath", fullFilePath);
		jobConf.addProperty("numOfSequences", String.valueOf(numOfSequences));
		jobConf.addProperty("numOfPartitions", String.valueOf(numOfPartitions));
		jobConf.addProperty("numOfMapTasks", String.valueOf(numOfMapTasks));
		jobConf.setFaultTolerance();

		TwisterDriver driver = null;
		TwisterMonitor monitor;
		MaxValues maxes = new MaxValues(0, 0, 0, 0);
		try {
			driver = new TwisterDriver(jobConf);
			driver.configureMaps();
			double start = System.currentTimeMillis();
			//System.out.println("Into mapreduce");
			monitor = driver
			.runMapReduce();
			Map<Key, Value> outmap = ((GenericCombiner) driver
					.getCurrentCombiner()).getResults();
			
			monitor.monitorTillCompletion();
			driver.close();
			double end = System.currentTimeMillis();
			maxes = (MaxValues) outmap.values().iterator().next();
			
			System.out.println("The max value for distance 5d: " + maxes.getDist5d());
			System.out.println("The max value for weight 5d: " + maxes.getWeight5d());
			System.out.println("The max value for distance 10d: " + maxes.getDist10d());
			System.out.println("The max value for weight 10d: " + maxes.getWeight10d());
			System.out.println("The writing file time is: " + (end - start)/1000.0);
			//String distanceFile = "distance_" + numOfSequences + "x" + numOfSequences + ".bin";

		} catch (Exception e) {
			if (driver != null) {
				driver.close();
			}
			throw new TwisterException(e);
		}
		return maxes;
	}

	public static void normalizedMapReduce(int numOfMapTasks,
			int numOfSequences, int numOfPartitions, String dataDir,
			String outputPrefix, MaxValues maxes, String inputBlockPrefix, String fullFilePath)
	throws TwisterException {
		System.out.println("Number of partitions: " + numOfPartitions);

		// JobConfigurations
		JobConf jobConf = new JobConf("Pairwise Alignment Normalized" 
				+ uuidGen.generateTimeBasedUUID());
		jobConf.setMapperClass(PWAWritingMapTask.class);
		
		jobConf.setNumMapTasks(numOfMapTasks);
		jobConf.setNumReduceTasks(0);

		jobConf.addProperty("dataDir", dataDir);
		jobConf.addProperty("outputPrefix", outputPrefix);
		
		jobConf.addProperty("numOfSequences", String.valueOf(numOfSequences));
		jobConf.addProperty("numOfPartitions", String.valueOf(numOfPartitions));
		jobConf.addProperty("numOfMapTasks", String.valueOf(numOfMapTasks));
		

		jobConf.addProperty("inputBlockPrefix", inputBlockPrefix);
		
		jobConf.addProperty("FullFilePath", fullFilePath);
		jobConf.setFaultTolerance();

		TwisterDriver driver = null;
		TwisterMonitor monitor;
	
		try {
			//System.out.println(maxes);
			driver = new TwisterDriver(jobConf);
			driver.configureMaps();
			double start = System.currentTimeMillis();
			String memCacheKey = driver.addToMemCache(maxes);
			monitor = driver.runMapReduceBCast(new StringValue(
					memCacheKey));
			monitor.monitorTillCompletion();
			driver.close();
			double end = System.currentTimeMillis();
			System.out.println("The writing file time is: " + (end - start)/1000.0);
			//String distanceFile = "distance_" + numOfSequences + "x" + numOfSequences + ".bin";

		} catch (Exception e) {
			if (driver != null) {
				driver.close();
			}
			throw new TwisterException(e);
		}
	}

	
	public static void main(String args[]) {
		if (args.length != 7) {
			System.err
			.println("123args:  [num_of_map_tasks] [data_size_count] " +
			"[num_of_partitions] [data_dir] [input_block_prefix] [tmp_output_prefix] [full_file_path] ");
			System.exit(2);
		}

		int numOfMapTasks = Integer.parseInt(args[0]);
		int numOfSeqs = Integer.parseInt(args[1]);
		int numOfPartitions = Integer.parseInt(args[2]);
		String dataDir = args[3];
		String inputBlockPrefix = args[4];
		String outputPrefix = args[5];
		String fullFilePath = args[6];

		if ( !outputPrefix.endsWith("_") ) {
			System.err.println("ERROR: The output file prefix must end with an underscore (\"_\").");
			System.exit(2);
		}
		long beforeTime = System.currentTimeMillis();
		double sequentialTime = 0;
		try {
			MaxValues maxes = drivePWAMapReduce(numOfMapTasks, numOfSeqs,
					numOfPartitions, dataDir, inputBlockPrefix, outputPrefix, fullFilePath);
			normalizedMapReduce(numOfMapTasks, numOfSeqs, 
					numOfPartitions, dataDir, outputPrefix, maxes, inputBlockPrefix, fullFilePath);

		} catch (TwisterException e) {
			e.printStackTrace();
		}

		double timeInSeconds = ((double) (System.currentTimeMillis() - beforeTime)) / 1000;
		System.out.println("Total Time for Pairwise Alignment: " + timeInSeconds + " Seconds");
		System.out.println("Sequential Time = (Sigma mapTime + Sigma reduce time: " + sequentialTime + " Seconds");
		System.exit(0);
	}
}