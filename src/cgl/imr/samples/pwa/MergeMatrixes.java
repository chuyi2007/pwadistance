package cgl.imr.samples.pwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import cgl.imr.base.Key;
import cgl.imr.base.Value;
import cgl.imr.types.StringKey;
import cgl.imr.types.StringValue;
/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 */
public class MergeMatrixes {

	public MergeMatrixes() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length != 4) {
            System.err
                    .println("args:  [input map file] [output matrix file path] [sequence_count] " +
                            "[num_of_partitions]");
            System.exit(2);
        }
        //String partitionFile = args[0];
		double start = System.currentTimeMillis();
        String mapFile = args[0];
        String outputFile = args[1];
        int sequenceNumber = Integer.parseInt(args[2]);
        int partitionNumber = Integer.parseInt(args[3]);
        Map<Key, Value> map = readMap(mapFile);
        writeFinalMatrix(map, outputFile, sequenceNumber, partitionNumber);
        System.out.println("merge matrix took " + (System.currentTimeMillis() - start)/1000.0 + " seconds!");
	}

	private static Map<Key, Value> readMap(String fname)
	throws IOException {
		Map<Key, Value> map = new HashMap<Key, Value>();
		BufferedReader br = new BufferedReader(new FileReader(fname));
		String line;
		String[] tokens;
		while((line = br.readLine())!=null){
			tokens = line.split(" ");
			map.put(new StringKey(tokens[0]), new StringValue(tokens[1]));
		}
		return map;
	}
	
	private static void writeFinalMatrix(Map<Key, Value> map, String fname, int numOfSequences, int numOfPartitions)
	throws IOException {
		StringValue[] values = new StringValue[map.values().size()];
		values = map.values().toArray(values);
		Comparator<StringValue> c =new Comparator<StringValue>() {
			@Override
			public int compare(StringValue o1, StringValue o2) {
				String one = o1.toString();
				String two = o2.toString();
				return Integer.parseInt(one.substring(one.lastIndexOf("_") + 1))
				- Integer.parseInt(two.substring(two.lastIndexOf("_")+1));
			}
		};
		Arrays.sort(values, c);

		int seqsPerPart = numOfSequences/numOfPartitions;
		int remainder = numOfSequences % numOfPartitions;
		int suffix;
		int rowSize;
		String f;

		BufferedInputStream bdis;
		BufferedOutputStream bdos = new BufferedOutputStream(new FileOutputStream(fname));
		DataOutputStream dos = new DataOutputStream(bdos);
		//DataInputStream dis;
		//DataOutputStream dos = new DataOutputStream(new FileOutputStream(fname));

		for (StringValue v : values) {
			f = v.toString();
			suffix = Integer.parseInt(f.substring(f.lastIndexOf("_") + 1));
			rowSize = suffix < remainder ? seqsPerPart + 1 : seqsPerPart;
			//dis = new DataInputStream(new FileInputStream(f));
			bdis = new BufferedInputStream(new FileInputStream(f));
			DataInputStream dis = new DataInputStream(bdis);
			
			for (int i = 0; i < rowSize; i++){
				for (int j = 0; j < numOfSequences; j++) {
					dos.writeShort(dis.readShort());
				}
			}
			dis.close();
		}
		dos.flush();
		dos.close();
	}

}
