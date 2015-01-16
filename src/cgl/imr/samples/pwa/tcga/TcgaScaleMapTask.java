package cgl.imr.samples.pwa.tcga;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cgl.imr.base.Key;
import cgl.imr.base.MapOutputCollector;
import cgl.imr.base.MapTask;
import cgl.imr.base.TwisterException;
import cgl.imr.base.Value;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.MapperConf;

public class TcgaScaleMapTask implements MapTask{

	private String dataDir;
	private String outputPrefix;
	private String idxFile;
	//FileData fileData;
	private int rowSize, colSize;
	private int mapTaskNo;
	private int partitionNo;
	private int mapNum;
	private double scaleFactor;

	@Override
	public void close() throws TwisterException {

	}

	@Override
	public void configure(JobConf jobConf, MapperConf mapConf)
			throws TwisterException {

		dataDir = jobConf.getProperty("dataDir");
		outputPrefix = jobConf.getProperty("outputPrefix");
		idxFile = jobConf.getProperty("idxFile");
		partitionNo = Integer.parseInt(jobConf.getProperty("numOfPartitions"));
		scaleFactor = Math.ceil(Double.parseDouble(jobConf.getProperty("maximumDistance")));
		
		mapTaskNo = mapConf.getMapTaskNo();
		mapNum = jobConf.getNumMapTasks();
		
	}

	@Override
	public void map(MapOutputCollector arg0, Key arg1, Value arg2)
			throws TwisterException {
		// TODO Auto-generated method stub
		List<String> inputFiles = new ArrayList<String>();
		List<Integer> ids = new ArrayList<Integer>();
		
		int miniFiles = partitionNo/mapNum;
		int remainder = partitionNo%mapNum;
		if(mapTaskNo < remainder){
			inputFiles.add((dataDir + "/" + TcgaConstants.DOUBLE_PREFIX + outputPrefix + (miniFiles * mapNum + mapTaskNo)).replaceAll("//", "/"));
			ids.add(miniFiles * mapNum + mapTaskNo);
		}
		while(miniFiles > 0){
			miniFiles--;
			inputFiles.add((dataDir + "/" + TcgaConstants.DOUBLE_PREFIX + outputPrefix + (miniFiles * mapNum + mapTaskNo)).replaceAll("//", "/"));
			ids.add(miniFiles * mapNum + mapTaskNo);
		}
		
		try{
			int count = 0;
			for(String fileName: inputFiles){
				int fileNo = ids.get(count);
				String outputFileName = (dataDir + "/" + outputPrefix + fileNo).replaceAll("//", "/");
				BufferedReader br = new BufferedReader(new FileReader(idxFile));
				String line;
				String[] tokens;
				while((line = br.readLine())!=null){
					tokens = line.split("\t");
					if(Integer.parseInt(tokens[0]) == fileNo){
						rowSize = Integer.parseInt(tokens[1]);
						colSize = Integer.parseInt(tokens[2]);
						break;
					}
				}
				br.close();
				
				DataInputStream din = new DataInputStream(
						new BufferedInputStream(
								new FileInputStream(fileName)));
				DataOutputStream dout = new DataOutputStream(
						new BufferedOutputStream(
								new FileOutputStream(outputFileName)));
				for(int i = 0; i < rowSize; i++){
					for(int j = 0; j < colSize; j++){
						dout.writeShort((short)(Short.MAX_VALUE * (din.readDouble()/scaleFactor)));
					}
				}
				din.close();
				dout.close();
				count++;
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}

}
