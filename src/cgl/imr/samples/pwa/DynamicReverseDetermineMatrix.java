package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Sequence {
	String sequenceId;
	String sequence;
	
	public Sequence (String id, String seq) {
		sequenceId = id;
		sequence = seq;
	}
	
	public String getSequenceId() {
		return sequenceId;
	}
	
	public void setSequenceId(String sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	public String getSequence() {
		return sequence;
	}
	
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
}

public class DynamicReverseDetermineMatrix {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length < 8) {
            System.err
                    .println("args:  [Input fasta file] [output fasta file] " +
                    		"[Input matrix file] [Input matrix reverse file] [output reverse ids]" +
                            "[Sample Size] [Input group file] [group number]");
            System.exit(2);
        }
		double startTime = System.currentTimeMillis();
		String inputFastaFile = args[0];
		String outputFastaFile = args[1];
		String inputMatrixFile = args[2];
		String inputMatrixReverseFile = args[3];
		String outputIdFile = args[4];
		int sampleSize = Integer.parseInt(args[5]);
		String groupFile = args[6];
		int groupNo = Integer.parseInt(args[7]);
		
		List<Sequence> sequences = parse(inputFastaFile);
		List<Sequence> groupedSequences = new ArrayList<Sequence>();
		
		BufferedReader br = new BufferedReader(new FileReader(groupFile));
		
		int totalSize = sequences.size();
		Set<Integer> sampleSet = new HashSet<Integer>();
		for(int i = 0; i < sampleSize; i++)
			sampleSet.add(i * (totalSize/sampleSize));
		
		short[][] score = new short[totalSize][sampleSize];
		short[][] scoreReverse = new short[totalSize][sampleSize];
		
		String[] tokens;
		DataInputStream din = new DataInputStream(
				new BufferedInputStream(new FileInputStream(inputMatrixFile)));
		DataInputStream dinReverse = new DataInputStream(
				new BufferedInputStream(new FileInputStream(inputMatrixReverseFile)));
		boolean flag = false;
		int count = 0;
		for (int i = 0; i < totalSize; ++i) {
			tokens = br.readLine().split("\t");
			if (Integer.parseInt(tokens[4]) == groupNo)
				flag = true;
			for (int j = 0; j < sampleSize; ++j) {
				short len = din.readShort();
				short lenReverse = dinReverse.readShort();
				if (flag) {
					score[count][j] = len;
					scoreReverse[count][j] = lenReverse;
					++count;
				}
			}
			if (flag)
				groupedSequences.add(sequences.get(i));
			flag = false;
		}
		din.close();
		dinReverse.close();
		br.close();

		boolean[] reverseFlag = new boolean[totalSize];
		boolean continueFlag = true;
		boolean[] testSetReverseFlag = new boolean[sampleSize];
		for(int i = 0; i < sampleSize; i++)
			testSetReverseFlag[i] = false;
		int iterationCount = 0;
		while(continueFlag){
			continueFlag = false;
			System.out.println("iteration: " + iterationCount + 
					"......reverse count: " + countBooleanFlag(reverseFlag));
			int testSetCount = 0;
			for(int i = 0; i < totalSize; i++){
				int reverseCount = 0;
				
				for(int j = 0; j < sampleSize; j++){
					if(testSetReverseFlag[j] != true && score[i][j] < scoreReverse[i][j])
						reverseCount++;
					else if(testSetReverseFlag[j] == true && score[i][j] >= scoreReverse[i][j])
						reverseCount++;
				}
				
				if((double)reverseCount/(double)sampleSize > 0.5){
					reverseFlag[i] = true;
					if(sampleSet.contains(i)){
						if(testSetReverseFlag[testSetCount] != true)
							continueFlag = true;
						testSetReverseFlag[testSetCount] = true;
					}
				}
				else{
					reverseFlag[i] = false;
					if(sampleSet.contains(i)){
						if(testSetReverseFlag[testSetCount] != false)
							continueFlag = true;
						testSetReverseFlag[testSetCount] = false;
					}
				}
				
				if(sampleSet.contains(i)){
					testSetCount++;
				}
			}
			iterationCount++;
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFastaFile));
		BufferedWriter bwId = new BufferedWriter(new FileWriter(outputIdFile));
		for(int i = 0; i < totalSize; i++){
			//if(reverseFlag[i]){
			//	bw.write(sequences.get(i).getId() + "\n");
			//	bw.write(sequences.get(i).getReverseComplementedSequence() + "\n");
			//}
			//else{
			//	bw.write(sequences.get(i).getId() + "\n");
			//	bw.write(sequences.get(i).toString() + "\n");
			//}
			bwId.write(i + "\t" + reverseFlag[i] + "\n");
		}
		bw.close();
		bwId.close();
		//System.out.println("Total reverse number: " + countBooleanFlag(reverseFlag));
		System.out.println("Execution time: " + ((double)System.currentTimeMillis() - startTime)/1000.0);
		System.out.println("Total reverse number: " + countBooleanFlag(reverseFlag));
	}
	
	private static List<Sequence> parse(String fileName) throws IOException {
		List<Sequence> outSamples = new ArrayList<Sequence>();
		BufferedReader br = new BufferedReader(
				new FileReader(fileName));
		String line = null;
		String seq = "";
		String id = "";
		int count = 0;
		while((line = br.readLine()) != null){
			if(line.length() > 0){
				if(line.charAt(0) == '>'){
					id = line;
					if(seq != ""){
						outSamples.add(
							new Sequence (id, seq));
					}
					count++;
					seq = "";
				}
				else{
					seq += line;
				}
			}
		}
		outSamples.add(new Sequence(id, seq));
		br.close();
		return outSamples;
	}
	
	private static int countBooleanFlag(boolean[] flags){
		int count = 0;
		//Debug
		int totalCount = 0;
		for(boolean f : flags){
			if(f){
				count++;
			}
			totalCount++;
		}
		return count;
	}

}
