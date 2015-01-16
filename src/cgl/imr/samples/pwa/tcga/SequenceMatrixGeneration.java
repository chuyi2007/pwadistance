package cgl.imr.samples.pwa.tcga;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;

import edu.indiana.salsahpc.AlignedData;
import edu.indiana.salsahpc.AlignmentData;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.BioJavaWrapper;
import edu.indiana.salsahpc.DistanceType;
import edu.indiana.salsahpc.MatrixUtil;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;
import edu.indiana.salsahpc.SimilarityMatrix;
import edu.indiana.salsahpc.SmithWatermanAligner;

public class SequenceMatrixGeneration {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 4) {
            System.err
                    .println("args:  [input file] [output matrix file] [input size] [input dimension]");
            System.exit(2);
        }
		String inputFilePath = args[0];
		String matrixFilePath = args[1];
		int size = Integer.parseInt(args[2]);
		int dimension = Integer.parseInt(args[3]);
		
		BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
		String line = null;
		String[] tokens;
		//first line is dimension number
		br.readLine();
		double[][] inputNum = new double[size][dimension];
		for(int i = 0; i < size; i++){
			line = br.readLine();
			tokens = line.split("\t");
			for(int j = 0; j < dimension; j++)
				inputNum[i][j] = Double.parseDouble(tokens[j]);
		}
		br.close();
		
		double maximum = - Double.MAX_VALUE;
		double[][] output = new double[size][size];
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				output[i][j] = Euclidean(inputNum[i], inputNum[j]);
				
				if(output[i][j] > maximum)
					maximum = output[i][j];
			}
		}
		System.out.println("maximum: " + maximum);
		maximum = Math.ceil(maximum);
		System.out.println("maximum: " + maximum);
		
		for(int i =0; i < size; i++)
			for(int j = 0; j < size; j++)
				output[i][j] = output[i][j] / maximum;
		
		DataOutputStream resultMatrix = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(matrixFilePath)));
		
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				resultMatrix.writeShort((short)(Short.MAX_VALUE * output[i][j]));
		
		resultMatrix.close();
	}
	
	public static double Euclidean(double[] inputA, double[] inputB){
		double sum = 0;
		for(int i = 0; i < inputA.length; i++){
			sum += (inputA[i] - inputB[i]) * (inputA[i] - inputB[i]);
		}
		return Math.sqrt(sum);
	}
}
