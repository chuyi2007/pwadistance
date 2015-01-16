package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;

public class PartitionMerge {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 6) {
            System.err
                    .println("args:  [Input Folder] [Input Prefix Pattern (use : to replace number)]" +
                    		"[Output Result Matrix] " +
                            "[Partition Number] [IDx File] [Symmetric?(0:no, 1:yes)]");
            System.exit(2);
        }
		String inputFolder = args[0];
		String inputPrefixPattern = args[1];
		String outputResultMatrix = args[2];
		int partitionNumber = Integer.parseInt(args[3]);
		String idxFile = args[4];
		
		int symmetricFlag = Integer.parseInt(args[5]);
		
		double startTime = System.currentTimeMillis();
		
		int memoryLimitSize = 10000;
		
		DataOutputStream dos = new DataOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(outputResultMatrix)));
		BufferedReader br = new BufferedReader(new FileReader(idxFile));
		String line;
		String[] tokens;
		int[] PIs = new int[partitionNumber];
		int count = 0;
		while((line = br.readLine()) != null){
			tokens = line.split("\t");
			PIs[count] = Integer.parseInt(tokens[1]);
			count++;
		}
		
		if(symmetricFlag == 1){
			for(int i=0; i < partitionNumber; i++){
				short[][][] partialMatrix = new short[partitionNumber][][];
				tokens = inputPrefixPattern.split(":");
				int maximumOutterCount = PIs[i]/memoryLimitSize;

				for(int m = 0; m <= maximumOutterCount; m++){
					int maximumInnerCount = 0;
					int offSet = m * memoryLimitSize;
					if(m == maximumOutterCount)
						maximumInnerCount = PIs[i] - m * memoryLimitSize;
					else
						maximumInnerCount = memoryLimitSize;
					for(int j = 0; j < partitionNumber; j++){
						partialMatrix[j] = new short[maximumInnerCount][PIs[j]];

						if(i > j){
							String filePrefix = tokens[0] + j + tokens[1] + i + tokens[2];
							String filePath = (inputFolder + "/" + filePrefix).replaceAll("//", "/");

							//needed further edit
							for(int k = 0; k < PIs[j]; k++){
								DataInputStream disInput = 
									new DataInputStream(
											new BufferedInputStream(
													new FileInputStream(filePath)));

								for(int x = 0; x < PIs[j]; x++){
									for(int y = 0; y < PIs[i]; y++){
										short tmp = disInput.readShort();
										if(y >= offSet && y < offSet + maximumInnerCount)
											partialMatrix[j][y-offSet][x] = tmp;
									}
								}
								disInput.close();
							}
						}
						else{
							String filePrefix = tokens[0] + i + tokens[1] + j + tokens[2];
							String filePath = (inputFolder + "/" + filePrefix).replaceAll("//", "/");
							DataInputStream disInput = 
								new DataInputStream(
										new BufferedInputStream(
												new FileInputStream(filePath)));

							for(int x = 0; x < offSet; x++){
								for(int y = 0; y < PIs[j]; y++)
									disInput.readShort();
							}
							for(int x = 0; x < maximumInnerCount; x++){
								for(int y = 0; y < PIs[j]; y++)
									partialMatrix[j][x][y] = disInput.readShort();
							}
							disInput.close();
						}	
					}
					for(int x = 0; x < maximumInnerCount; x++){
						for (int y = 0; y < partitionNumber; y++){
							for (int z = 0; z < partialMatrix[y][x].length; z++){
								dos.writeShort(partialMatrix[y][x][z]);}}}
				}
			}
		}
		else if(symmetricFlag == 0){
			for(int i=0; i < partitionNumber; i++){
				short[][][] partialMatrix = new short[partitionNumber][][];
				tokens = inputPrefixPattern.split(":");
				int maximumOutterCount = PIs[i]/memoryLimitSize;

				for(int m = 0; m <= maximumOutterCount; m++){
					int maximumInnerCount = 0;
					int offSet = m * memoryLimitSize;
					if(m == maximumOutterCount)
						maximumInnerCount = PIs[i] - m * memoryLimitSize;
					else
						maximumInnerCount = memoryLimitSize;
					for(int j = 0; j < partitionNumber; j++){
						partialMatrix[j] = new short[maximumInnerCount][PIs[j]];

						String filePrefix = tokens[0] + i + tokens[1] + j + tokens[2];
						String filePath = (inputFolder + "/" + filePrefix).replaceAll("//", "/");
						DataInputStream disInput = 
							new DataInputStream(
									new BufferedInputStream(
											new FileInputStream(filePath)));

						for(int x = 0; x < offSet; x++){
							for(int y = 0; y < PIs[j]; y++)
								disInput.readShort();
						}
						for(int x = 0; x < maximumInnerCount; x++){
							for(int y = 0; y < PIs[j]; y++)
								partialMatrix[j][x][y] = disInput.readShort();
						}
						disInput.close();
					}
					for(int x = 0; x < maximumInnerCount; x++){
						for (int y = 0; y < partitionNumber; y++){
							for (int z = 0; z < partialMatrix[y][x].length; z++){
								dos.writeShort(partialMatrix[y][x][z]);}}}
				}
			}
		}
		dos.close();
		
		System.out.println("Total execution time: " + (System.currentTimeMillis() - startTime)/1000.0);
	}
}
