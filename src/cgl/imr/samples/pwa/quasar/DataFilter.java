package cgl.imr.samples.pwa.quasar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataFilter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length < 2) {
			System.err
			.println("args:  [Input File] [Output File] ");
			System.exit(2);
		}
		
		int[] d5d = {5, 7, 9, 11, 13};
		int[] w5d = {6, 8, 10, 12, 14};
		int[] d10d = {30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
		int[] w10d = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29};

		
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		BufferedWriter bw = new BufferedWriter(new FileWriter(args[1]));
		String line = null;
		String[] tokens;
		
		int before = 0;
		int after = 0;
		while ((line = br.readLine()) != null) {
			tokens = line.split(",");
			++before;
			if (Double.parseDouble(tokens[17]) != 0) {
				continue;
			}
			
			boolean flag = false;
			for (int index : d5d) {
				double val = 0;
				try {
					val = Double.parseDouble(tokens[index]);
				}
				catch (Exception e) {
				}
				if (val > 0) {
					flag = true;
					break;
				}
			}
			
			if (flag) {
				for (int index : w5d) {
					double val = 0;
					try {
						val = Double.parseDouble(tokens[index]);
					}
					catch (Exception e) {
						
					}
					if (val < 0) {
						flag = false;
						break;
					}
				}
				if (flag) {
					bw.write(line + "\n");
					++after;
				}
			}
		}
		br.close();
		bw.close();
		System.out.println("Before : " + before + " After: " + after);
	}

}
