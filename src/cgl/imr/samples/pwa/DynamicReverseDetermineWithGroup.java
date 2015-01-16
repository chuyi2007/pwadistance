package cgl.imr.samples.pwa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cgl.imr.samples.pwa.util.FileOperation;

import edu.indiana.salsahpc.AlignedData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;
import edu.indiana.salsahpc.SimilarityMatrix;
import edu.indiana.salsahpc.SmithWatermanAligner;

public class DynamicReverseDetermineWithGroup implements Runnable{
	SimilarityMatrix ednafull;
	SmithWatermanAligner aligner;
	List<AlignedData> ads;
	AlignedData ad;
	static int totalSize;
	static int sampleSize;
	static HashSet<Integer> sampleSet;
	static List<Sequence> sequences;
	static List<Sequence> totalSequences;
	static short[][] score;
	static short[][] scoreReverse;
	int row;
	
	public DynamicReverseDetermineWithGroup(int row) throws IOException{
		this.row = row;
		ednafull = SimilarityMatrix.getEDNAFULL();
		aligner = new SmithWatermanAligner();
	}
	
	public void run(){
		int testSetCount = 0;
		for(int j = 0; j < sampleSize; j++){
			if(sampleSet.contains(j)){
				Sequence sequenceA = totalSequences.get(this.row);
				Sequence sequenceB = totalSequences.get(j);
				try {
					ads = aligner.align(ednafull, -16, -4, 
							sequenceA, sequenceB);

					ad = ads.get(0);
					score[this.row][testSetCount] = ad.getAlignmentLength();

					ads = aligner.align(ednafull, -16, -4, 
							sequenceA, sequenceB.getReverseComplementedSequence());
					ad = ads.get(0);
					scoreReverse[this.row][testSetCount] = ad.getAlignmentLength();
					testSetCount++;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length < 5) {
            System.err
                    .println("args:  [Input fasta file] [output fasta file] [output reverse ids]" +
                            "[Sample Size] [thread number] (optional: [group file])");
            System.exit(2);
        }
		double startTime = System.currentTimeMillis();
		String inputFastaFile = args[0];
		String outputFastaFile = args[1];
		String outputIdFile = args[2];
		sampleSize = Integer.parseInt(args[3]);
		int threadNum = Integer.parseInt(args[4]);
		
		List<Integer> groups = null;
		if(args.length > 5)
			groups = FileOperation.readGroups(args[5], " ");
		//List<Sequence> totalSequences = new ArrayList<Sequence>();
		totalSequences = SequenceParser.parse(inputFastaFile, Alphabet.DNA);
		sequences = new ArrayList<Sequence>();
		for (int i = 0; i < totalSequences.size(); ++i)
			if(groups.get(i) == 0)
				sequences.add(totalSequences.get(i));
		totalSize = sequences.size();
		sampleSize = totalSequences.size();
		sampleSet = new HashSet<Integer>();
		for(int i = 0; i < sampleSize; i++)
			sampleSet.add(i);
		
		score = new short[totalSize][sampleSize];
		scoreReverse = new short[totalSize][sampleSize];
		
		ExecutorService pool = Executors.newFixedThreadPool(threadNum);
		List<Future> futures = new ArrayList<Future>(totalSize);
		for(int i = 0; i < totalSize; i++)
			futures.add(pool.submit(new DynamicReverseDetermineWithGroup(i)));
		for(int i = 0; i < totalSize; i++)
			futures.get(i).get();
		pool.shutdown();

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
			double sum = 0;
			for(int i = 0; i < totalSize; i++){
				int reverseCount = 0;
				
				for(int j = 0; j < sampleSize; j++){
					if(testSetReverseFlag[j] != true && score[i][j] < scoreReverse[i][j])
						reverseCount++;
					else if(testSetReverseFlag[j] == true && score[i][j] >= scoreReverse[i][j])
						reverseCount++;
				}
				
				double confident = (double)reverseCount/(double)sampleSize;
				sum += confident;
				if(confident > 0.5){
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
			System.out.println("confident: " + sum);
			iterationCount++;
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFastaFile));
		BufferedWriter bwId = new BufferedWriter(new FileWriter(outputIdFile));
		for(int i = 0; i < totalSize; i++){
			if(reverseFlag[i]){
				bw.write(sequences.get(i).getId() + "\n");
				bw.write(sequences.get(i).getReverseComplementedSequence() + "\n");
			}
			else{
				bw.write(sequences.get(i).getId() + "\n");
				bw.write(sequences.get(i).toString() + "\n");
			}
			bwId.write(i + "\t" + reverseFlag[i] + "\n");
		}
		bw.close();
		bwId.close();
		//System.out.println("Total reverse number: " + countBooleanFlag(reverseFlag));
		System.out.println("Execution time: " + ((double)System.currentTimeMillis() - startTime)/1000.0);
		System.out.println("Total reverse number: " + countBooleanFlag(reverseFlag));
	}
	
	private static int countBooleanFlag(boolean[] flags){
		int count = 0;
		//Debug
		int totalCount = 0;
		for(Boolean f : flags){
			if(f){
				count++;
			}
			totalCount++;
		}
		return count;
	}

}
