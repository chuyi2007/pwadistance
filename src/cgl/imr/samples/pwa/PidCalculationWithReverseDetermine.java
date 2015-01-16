package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PidCalculationWithReverseDetermine {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 6) {
            System.err
                    .println("args:  [Input Identical Pairs Matrix] [Input IndeticalPairs Matrix withReverse]" +
                    		"[Input Length Matrix] [Input Length Matrix with Reverse] " +
                    		"[Output Result Matrix] " +
                            "[Data Size]");
            System.exit(2);
        }
		String inputIPMatrix = args[0];
		String inputIPReverseMatrix = args[1];
		String inputLengthMatrix = args[2];
		String inputLengthReverseMatrix = args[3];
		String outputPIDMatrix = args[4];
		int size = Integer.parseInt(args[5]);
		
		DataInputStream disIP = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputIPMatrix)));
		DataInputStream disIPReverse = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputIPReverseMatrix)));
		DataInputStream disLength = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputLengthMatrix)));
		DataInputStream disLengthReverse = 
			new DataInputStream(
					new BufferedInputStream(
							new FileInputStream(inputLengthReverseMatrix)));
		DataOutputStream dos = new DataOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(outputPIDMatrix)));
		short[][] identicalPairs = new short[size][size];
		short[][] identicalPairsReverse = new short[size][size];
		short[][] lengths = new short[size][size];
		short[][] lengthsReverse = new short[size][size];
		Boolean[] reverseFlag = new Boolean[size];
		for(int i = 0; i < size; i++){
			int reverseCount = 0;
			for(int j = 0; j < size; j++){
				identicalPairs[i][j] = disIP.readShort();
				identicalPairsReverse[i][j] = disIPReverse.readShort();
				lengths[i][j] = disLength.readShort();
				lengthsReverse[i][j] = disLengthReverse.readShort();
				if(lengths[i][j] < lengthsReverse[i][j])
					reverseCount++;
			}
			if(i == 1094)
				System.out.println(reverseCount);
			if((double)reverseCount/(double)size > 0.5)
				reverseFlag[i] = true;
			else
				reverseFlag[i] = false;
		}
		disIP.close();
		disIPReverse.close();
		disLength.close();
		disLengthReverse.close();
		
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				if(reverseFlag[i] == true && reverseFlag[j] == true){
					double pid = 1.0 - (double)identicalPairs[i][j]/(double)lengths[i][j];
					dos.writeShort((short)(pid * Short.MAX_VALUE));
				}
				else if(reverseFlag[i] == true || reverseFlag[j] == true){
					double pidReverse = 1.0 - (double)identicalPairsReverse[i][j]/(double)lengthsReverse[i][j];
					dos.writeShort((short)(pidReverse * Short.MAX_VALUE));
				}
				else{
					double pid = 1.0 - (double)identicalPairs[i][j]/(double)lengths[i][j];
					dos.writeShort((short)(pid * Short.MAX_VALUE));
				}
			}
		}
		dos.close();
		
		System.out.println("Total reverse number: " + countBooleanFlag(reverseFlag));
	}
	
	private static int countBooleanFlag(Boolean[] flags){
		int count = 0;
		//Debug
		int totalCount = 0;
		for(Boolean f : flags){
			if(f){
				count++;
				if(totalCount>543){
					System.out.println("Sequence id: " + totalCount);
				}
			}
			totalCount++;
		}
		return count;
	}
	
	public static void randomSelect(int size){
		
	}

}
