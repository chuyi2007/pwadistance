package cgl.imr.samples.pwa.quasar;

import cgl.imr.base.*;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.MapperConf;
import cgl.imr.types.BytesValue;
import cgl.imr.types.StringKey;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 */

public class PWAMapTask implements MapTask {

	private String dataDir;
	private String inputBlockPrefix;
	//FileData fileData;

	@Override
	public void close() throws TwisterException {

	}

	@Override
	public void configure(JobConf jobConf, MapperConf mapConf)
			throws TwisterException {

		dataDir = jobConf.getProperty("dataDir");
		inputBlockPrefix = jobConf.getProperty("inputBlockPrefix");
	}

	@Override
	public void map(MapOutputCollector collector, Key key, Value val)
			throws TwisterException {
		int[] d5d = {5, 7, 9, 11, 13};
		int[] w5d = {6, 8, 10, 12, 14};
		int[] d10d = {30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
		int[] w10d = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29};
		
		Region region;
		//String inputFile = fileData.getFileName();
		try {
			region = new Region(val.getBytes());
		} catch (SerializationException e) {
			throw new TwisterException(e);
		}
		//System.out.println("Just into the Mapper");
		List<Block> blocks = region.getBlocks();

		for (Block block : blocks) {
			int rowBlockNumber = block.getRowBlockNumber();
			int columnBlockNumber = block.getColumnBlockNumber();

			String rowBlockFileName = String.format("%s%s%s%d", dataDir, File.separator, inputBlockPrefix, rowBlockNumber);

			// for each block, read row block and columnBlockNumber block
			double[][] rowBlockInputs = null;
			double[][] colBlockInputs = null;
			try {
				rowBlockInputs = Tools.getInputBlocks(rowBlockFileName);
			} catch (Exception e) {
				throw new TwisterException(e);
			}
			String colBlockFileName = String.format("%s%s%s%d", dataDir, File.separator, inputBlockPrefix, columnBlockNumber);
			try {
				colBlockInputs = Tools.getInputBlocks(colBlockFileName);
			} catch (Exception e) {
				throw new TwisterException(e);
			}


			// get the number of sequences of row block and column block
			int rowSize = rowBlockInputs.length;
			int columnSize = colBlockInputs.length;

			//System.out.println("Before the choice!");
			// calculate distance, sequence by sequence
			double[][] dist5d = new double[rowSize][columnSize];
			double[][] weight5d = new double[rowSize][columnSize];
			double[][] dist10d = new double[rowSize][columnSize];
			double[][] weight10d = new double[rowSize][columnSize];

			try {
				if (rowBlockNumber != columnBlockNumber) {
					// Not a diagonal block. So have to do pairwise distance calculation for each pair
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < columnSize; k++) {

							dist5d[j][k] = 
									Tools.getDist(rowBlockInputs[j], colBlockInputs[k], d5d);

							weight5d[j][k] = 
									Tools.getError(rowBlockInputs[j], colBlockInputs[k], w5d);
							dist10d[j][k] = 
									Tools.getDist(rowBlockInputs[j], colBlockInputs[k], d10d);
							weight10d[j][k] = 
									Tools.getError(rowBlockInputs[j], colBlockInputs[k], w10d);
						}
					}
				} else {
					// Diagonal block. Perform pairwise distance calculation only for one triangle
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < j; k++) {

							dist5d[j][k] =  
									Tools.getDist(rowBlockInputs[j], colBlockInputs[k], d5d);
							dist5d[k][j] = dist5d[j][k];
							
							weight5d[j][k] =  
									Tools.getError(rowBlockInputs[j], colBlockInputs[k], w5d);
							weight5d[k][j] = weight5d[j][k];
							
							dist10d[j][k] =  
									Tools.getDist(rowBlockInputs[j], colBlockInputs[k], d10d);
							dist10d[k][j] = dist10d[j][k];
							
							weight10d[j][k] =  
									Tools.getError(rowBlockInputs[j], colBlockInputs[k], w10d);
							weight10d[k][j] = weight10d[j][k];
						}
						// Pairwise distance for diagonal elements
						dist5d[j][j] = 0;
						weight5d[j][j] = 1;
						dist10d[j][j] = 0;
						weight10d[j][j] = 1;
					}
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			try {
//				System.out.println("rowBlockNumber: " + rowBlockNumber);
//				Thread.sleep(rowBlockNumber * 10);
//				System.out.println("Wake up: " + rowBlockNumber);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			block.setDist5d(dist5d, false);
			block.setWeight5d(weight5d);
			block.setDist10d(dist10d);
			block.setWeight10d(weight10d);
			System.out.print("Computation Finished!");
			//System.out.println(block.getRowBlockNumber() + " mapper finished");
			try {
				collector.collect(new StringKey("" + block.getRowBlockNumber()),
						new BytesValue(block.getBytes()));
			} catch (SerializationException e) {
				throw new TwisterException(e);
			}

			if (rowBlockNumber != columnBlockNumber) {
				// Creates a transpose block. Note. distances array should be treated as transpose when reading.
				Block transBlock = new Block(block.getColumnBlockNumber(), block.getRowBlockNumber());
				transBlock.setDist5d(block.getDist5d(), true);
				transBlock.setWeight5d(block.getWeight5d());
				transBlock.setDist10d(block.getDist10d());
				transBlock.setWeight10d(block.getWeight10d());
				try {
					collector.collect(new StringKey("" + transBlock.getRowBlockNumber()),
							new BytesValue(transBlock.getBytes()));
				} catch (SerializationException e) {
					throw new TwisterException(e);
				}
			}
		}
	}
}