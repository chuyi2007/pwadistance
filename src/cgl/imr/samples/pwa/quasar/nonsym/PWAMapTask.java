package cgl.imr.samples.pwa.quasar.nonsym;

import cgl.imr.base.*;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.MapperConf;
import cgl.imr.samples.pwa.quasar.Tools;
import cgl.imr.types.StringKey;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 */

public class PWAMapTask implements MapTask {

	private String dataDir;
	private String inputBlockPrefix;
	private String fullFilePath;
	private int mapNum;
	private int mapTaskNo;
	private int partitionNum;

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
		partitionNum = Integer.parseInt(jobConf.getProperty("numOfPartitions"));
		mapNum = Integer.parseInt(jobConf.getProperty("numOfMapTasks"));
	}

	@Override
	public void map(MapOutputCollector collector, Key key, Value val)
			throws TwisterException {
		int[] d5d = {5, 7, 9, 11, 13, 15};
		int[] w5d = {6, 8, 10, 12, 14};
		int[] d10d = {15, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
		int[] w10d = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29};

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
		MaxValues maxes = new MaxValues();
		
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

			try {
				for (int j = 0; j < rowSize; j++) {
					for (int k = 0; k < columnSize; k++) {
						maxes.setDist5d(Math.max(maxes.getDist5d(), 
								Tools.getDist(rowBlockInputs[j], colBlockInputs[k], d5d)));
						maxes.setWeight5d(Math.max(maxes.getWeight5d(), 
								Tools.getError(rowBlockInputs[j], colBlockInputs[k], w5d)));
						maxes.setDist10d(Math.max(maxes.getDist10d(), 
								Tools.getDist(rowBlockInputs[j], colBlockInputs[k], d10d)));
						maxes.setWeight10d(Math.max(maxes.getWeight10d(), 
								Tools.getError(rowBlockInputs[j], colBlockInputs[k], w10d)));
					}
				}
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.print("Computation Finished!");
		collector.collect(new StringKey("FIND-MAX"), maxes);
	}
}