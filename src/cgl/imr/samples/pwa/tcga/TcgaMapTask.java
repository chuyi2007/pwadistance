package cgl.imr.samples.pwa.tcga;

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

public class TcgaMapTask implements MapTask {

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
			
//			System.out.println("rowBlock: ");
//			for(double d : rowBlockInputs[0])
//				System.out.print(" " +d);
//			System.out.println("colBlock: ");
//			for(double d: colBlockInputs)
//				System.out.println("columnBlock: " + d);
			//System.out.println("Before the choice!");
			// calculate distance, sequence by sequence
			double[][] distances = new double[rowSize][columnSize];
			
			double maximumDistance = - Double.MAX_VALUE;
			try {
				if (rowBlockNumber != columnBlockNumber) {
					// Not a diagonal block. So have to do pairwise distance calculation for each pair
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < columnSize; k++) {
							double tmpDistance = Tools.getDistance(rowBlockInputs[j], colBlockInputs[k]);
							distances[j][k] = tmpDistance;
							if(tmpDistance > maximumDistance)
								maximumDistance = tmpDistance;
						}
					}
				} else {
					// Diagonal block. Perform pairwise distance calculation only for one triangle
					for (int j = 0; j < rowSize; j++) {
						for (int k = 0; k < j; k++) {
							double tmpDistance = Tools.getDistance(rowBlockInputs[j], colBlockInputs[k]);
							if(tmpDistance > maximumDistance)
								maximumDistance = tmpDistance;
							
							distances[j][k] =  tmpDistance;
							distances[k][j] = distances[j][k];
						}
						// Pairwise distance for diagonal elements
						distances[j][j] = 0;
					}
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("map task: " + maximumDistance);
			block.setDistances(distances, false);
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
				transBlock.setDistances(block.getDistances(), true);
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

