package cgl.imr.samples.pwa.quasar.nonsym;

import cgl.imr.base.*;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.MapperConf;
import cgl.imr.samples.pwa.quasar.Tools;
import cgl.imr.types.StringValue;
import cgl.imr.worker.MemCache;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 */

public class PWAWritingMapTask implements MapTask {

	private String dataDir;
	private String inputBlockPrefix;
	private String fullFilePath;
	private int mapNum;
	private int mapTaskNo;
	private String outputPrefix;
	private int partitionNum;
	private MaxValues maxes;

	private JobConf jobConf;
	@Override
	public void close() throws TwisterException {

	}

	@Override
	public void configure(JobConf jobConf, MapperConf mapConf)
			throws TwisterException {

		dataDir = jobConf.getProperty("dataDir");
		inputBlockPrefix = jobConf.getProperty("inputBlockPrefix");
		fullFilePath = jobConf.getProperty("FullFilePath");
		mapTaskNo = mapConf.getMapTaskNo();
		outputPrefix = jobConf.getProperty("outputPrefix");
		partitionNum = Integer.parseInt(jobConf.getProperty("numOfPartitions"));
		mapNum = Integer.parseInt(jobConf.getProperty("numOfMapTasks"));
		this.jobConf = jobConf;
	}

	@Override
	public void map(MapOutputCollector collector, Key key, Value val)
			throws TwisterException {
		int[] d5d = {5, 7, 9, 11, 13, 15};
		int[] w5d = {6, 8, 10, 12, 14};
		int[] d10d = {15, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
		int[] w10d = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29};


		StringValue memCacheKey = (StringValue) val;
		maxes = (MaxValues) (MemCache.getInstance().get(
				jobConf.getJobId(), memCacheKey.toString()));
		
		List<String> inputFiles = new ArrayList<String>();
		List<Integer> magicNumber = new ArrayList<Integer>();
		int miniFiles = partitionNum/mapNum;
		int remainder = partitionNum%mapNum;
		if(mapTaskNo < remainder){
			inputFiles.add((dataDir + "/" + inputBlockPrefix + (miniFiles * mapNum + mapTaskNo)).replaceAll("//", "/")
					.replaceAll("\\.\\.", "\\."));
			magicNumber.add(miniFiles * mapNum + mapTaskNo);
		}
		while(miniFiles > 0){
			miniFiles--;
			inputFiles.add((dataDir + "/" + inputBlockPrefix + (miniFiles * mapNum + mapTaskNo)).replaceAll("//", "/")
					.replaceAll("\\.\\.", "\\."));
			magicNumber.add(miniFiles * mapNum + mapTaskNo);
		}

		String colBlockFileName = fullFilePath;
		int count = 0;
		
		for (String rowBlockFileName : inputFiles) {
			// for each block, read row block and columnBlockNumber block
			double[][] rowBlockInputs = null;
			double[][] colBlockInputs = null;
			try {
				rowBlockInputs = Tools.getInputBlocks(rowBlockFileName);
			} catch (Exception e) {
				throw new TwisterException(e);
			}
			try {
				colBlockInputs = Tools.getInputBlocks(colBlockFileName);
			} catch (Exception e) {
				throw new TwisterException(e);
			}


			// get the number of sequences of row block and column block
			int rowSize = rowBlockInputs.length;
			int columnSize = colBlockInputs.length;

			String fname5d = dataDir + File.separator + outputPrefix + "dist_6d_" + String.valueOf(magicNumber.get(count));
			String wname5d = dataDir + File.separator + outputPrefix + "weight_5d_" + String.valueOf(magicNumber.get(count));
			String fname10d = dataDir + File.separator + outputPrefix + "dist_11d_" + String.valueOf(magicNumber.get(count));
			String wname10d = dataDir + File.separator + outputPrefix + "weight_10d_" + String.valueOf(magicNumber.get(count));

			try {
				DataOutputStream dosf5d = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(fname5d)));
				DataOutputStream dosw5d = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(wname5d)));
				DataOutputStream dosf10d = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(fname10d)));
				DataOutputStream dosw10d = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(wname10d)));

				//Only for max error > 1
				maxes.setWeight5d(Math.min(maxes.getWeight5d(), Short.MAX_VALUE));
				maxes.setWeight10d(Math.min(maxes.getWeight10d(), Short.MAX_VALUE));

				System.out.println(maxes);
				
				for (int j = 0; j < rowSize; j++) {
					for (int k = 0; k < columnSize; k++) {
						dosf5d.writeShort((short)
								((Tools.getDist(rowBlockInputs[j], colBlockInputs[k], d5d) 
										/ maxes.getDist5d()) * Short.MAX_VALUE));

						double error = Tools.getError(rowBlockInputs[j], colBlockInputs[k], w5d);
						if (error > 1) {
							dosw5d.writeShort((short)(maxes.getWeight5d() / error));
						}
						else {
							dosw5d.writeShort((short)(maxes.getWeight5d()));
						}
						
						dosf10d.writeShort((short)
								((Tools.getDist(rowBlockInputs[j], colBlockInputs[k], d10d) 
										/ maxes.getDist10d()) * Short.MAX_VALUE));
						
						error = Tools.getError(rowBlockInputs[j], colBlockInputs[k], w10d);
						if (error > 1) {
							dosw10d.writeShort((short)(maxes.getWeight10d() / error));
						}
						else {
							dosw10d.writeShort((short)(maxes.getWeight10d()));
						}
					}
				}

				dosf5d.close();
				dosw5d.close();
				dosf10d.close();
				dosw10d.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			++count;
		}
		System.out.print("Computation Finished!");
	}
}