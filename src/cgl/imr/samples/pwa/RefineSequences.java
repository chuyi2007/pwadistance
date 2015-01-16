package cgl.imr.samples.pwa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import cgl.imr.samples.pwa.type.*;
import edu.indiana.salsahpc.Alphabet;
import edu.indiana.salsahpc.Sequence;
import edu.indiana.salsahpc.SequenceParser;

public class RefineSequences {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if(args.length != 2){
			System.err.println("[input fasta] [output fasta]");
			System.exit(1);
		}
		String inputFasta = args[0];
		String outputFasta = args[1];
		List<Sequence> sequences = SequenceParser.parse(inputFasta, Alphabet.DNA);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFasta));
		int count = 0;
		String frontLabel = "";
		for(Sequence sequence: sequences){
			if(count < 126)
				frontLabel = "fox_";
			else if(count < 200)
				frontLabel = "genbank_";
			else if(count < 1188)
				frontLabel = "wittiya_";
			else if(count < 2133)
				frontLabel = "kruger_";
			String name = ">" + frontLabel + sequence.getId().replaceAll(" ", "_").replaceAll("_length=.*FoxRegion=", "_")
					.replaceAll("FoxCluster=", "").replaceAll("FoxMethod=", "").replaceAll(",", "")
					.replaceAll("'", "").replaceAll("#", "").replaceAll(":", "").replaceAll("\\(", "\\[")
					.replaceAll("\\)","\\]").replaceAll("__+", "_").replaceAll("\\.", "_").substring(1);
			if(name.endsWith("_"))
				name = name.substring(0, name.length() - 1);
			if(name.startsWith(">wittiya")){
				String[] tokens = name.split("_");
				name = "";
				name = tokens[0] + "_" + tokens[1] + "_" + count;
			}
			bw.write(name + "\n");
			bw.write(sequence.toString().toUpperCase().replaceAll("N+", "") + "\n");
			count++;
		}
		bw.close();
	}
	
	public static void setGroup(String groupFilePath, List<Point> points, HashMap<String, Integer> map) 
			throws IOException{
				BufferedReader br = new BufferedReader(new FileReader(groupFilePath));
				Map<String, Integer> idGroup = new HashMap<String, Integer>();
				String line = null;
				String[] tokens;
				while((line = br.readLine()) != null){
					tokens = line.split("\t");
					idGroup.put(tokens[0], map.get(tokens[1]));
				}
				for(int i = 0; i < points.size(); i++){
					int group = idGroup.get(points.get(i).getId());
					points.get(i).setGroup(group);
				}
			}
	public static List<Point> getPoints(String filePath, String separator) 
			throws IOException{
				BufferedReader br = new BufferedReader(
						new FileReader(filePath));
				String line;
				String[] tokens;
				List<Point> points = new ArrayList<Point>();
				while((line = br.readLine())!= null){
					tokens = line.split(separator);
					Point point = new Point();
					Vec3D position = new Vec3D(Double.parseDouble(tokens[1]), 
							Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]));
					//point.setId(tokens[0]);
					point.setId(tokens[0]);
					point.setPosition(position);
					point.setGroup(Integer.parseInt(tokens[4]));
					points.add(point);
				}
				br.close();
				return points;
			}
	public static void writeToFile(String filePath, List<Point> points) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
		for(Point point: points){
			bw.write(point.getId() + "\t"
					+ point.getPosition().getX() + "\t"
					+ point.getPosition().getY() + "\t"
					+ point.getPosition().getZ() + "\t"
					+ point.getGroup() + "\n");
		}
		bw.flush();
		bw.close();
	}

}
