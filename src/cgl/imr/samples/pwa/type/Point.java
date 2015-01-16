/**
 * @author Yang Ruan (yangruan@indiana.edu)
 */
package cgl.imr.samples.pwa.type;

public class Point implements Comparable{
	private Vec3D position;
	String id = null;
	int group = 100;
	String label = null;
	
	public Point(String p_id, Vec3D p_position){
		position = new Vec3D(p_position.getX(), p_position.getY(), p_position.getZ());
		id = p_id;
	}
	
	public Point(String p_id, Vec3D p_position, int p_group){
		position = new Vec3D(p_position.getX(), p_position.getY(), p_position.getZ());
		id = p_id;
		group = p_group;
	}
	
	public Point(){
		position = new Vec3D();
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public void reset(){
		id = null;
		position = null;
	}
	public Vec3D getPosition() {
		return position;
	}

	public void setPosition(Vec3D position) {
		this.position = position;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		Point point = (Point) arg0;
		if(Integer.parseInt(this.id) > Integer.parseInt(point.id)) return 1;
		else
			return -1;
	}

	public void setLabel(String label) {
		// TODO Auto-generated method stub
		this.label = label;
	}
	
}
