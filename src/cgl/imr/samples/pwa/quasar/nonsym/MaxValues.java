package cgl.imr.samples.pwa.quasar.nonsym;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cgl.imr.base.SerializationException;
import cgl.imr.base.Value;

/**
 * @author Yang Ruan (yangruan@cs.indiana.edu)
 */

public class MaxValues implements Value {
    private double dist5d, weight5d, dist10d, weight10d;

    public MaxValues(double dist5d, double weight5d, double dist10d,
			double weight10d) {
		super();
		this.dist5d = dist5d;
		this.weight5d = weight5d;
		this.dist10d = dist10d;
		this.weight10d = weight10d;
	}

	public double getDist5d() {
		return dist5d;
	}

	public double getWeight5d() {
		return weight5d;
	}

	public double getDist10d() {
		return dist10d;
	}

	public double getWeight10d() {
		return weight10d;
	}

	public void setDist5d(double dist5d) {
		this.dist5d = dist5d;
	}

	public void setWeight5d(double weight5d) {
		this.weight5d = weight5d;
	}

	public void setDist10d(double dist10d) {
		this.dist10d = dist10d;
	}

	public void setWeight10d(double weight10d) {
		this.weight10d = weight10d;
	}

	/**
     * Instantiates a new block and initialize values based on the given byte array.
     * Note. The following values should be present in the array (in order) to
     * create an object successfully.
     *
     * int: row block number
     * int: column block number
     * boolean: is transpose or not
     * int: row size
     * int: column size
     * short[][]: (isTranspose ? colSize : rowSize) X (isTranspose ? rowSize : colSize)
     *
     * @param bytes the byte array to use in object creation
     * @throws SerializationException if an error occurs
     */
    public MaxValues(byte[] bytes) throws SerializationException {
        this();
        fromBytes(bytes);
    }

    public MaxValues() {
		// TODO Auto-generated constructor stub
	}

	@Override
    public void fromBytes(byte[] bytes) throws SerializationException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(bytes);
        DataInputStream din = new DataInputStream(baInputStream);

        try {
        	this.dist5d = din.readDouble();
        	this.weight5d = din.readDouble();
        	this.dist10d = din.readDouble();
        	this.weight10d = din.readDouble();
            din.close();
            baInputStream.close();
        } catch (IOException ioe) {
            throw new SerializationException(ioe);
        }
    }

    @Override
    /**
     * Serializes this block as a byte array. The following values are placed in order.
     * Note. Last four values will be present in the output only if distances array is not null
     * int: row block number
     * int: column block number
     * boolean: is transpose or not
     * int: row size
     * int: column size
     * short[][]: (isTranspose ? colSize : rowSize) X (isTranspose ? rowSize : colSize)
     */
    public byte[] getBytes() throws SerializationException {
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baOutputStream);

        byte[] marshalledBytes = null;

        try {
        	dout.writeDouble(dist5d);
        	dout.writeDouble(weight5d);
        	dout.writeDouble(dist10d);
        	dout.writeDouble(weight10d);
            dout.flush();
            marshalledBytes = baOutputStream.toByteArray();
            dout.close();
        } catch (IOException ioe) {
            throw new SerializationException(ioe);
        }
        return marshalledBytes;
    }
    
    @Override
    public String toString() {
    	return dist5d + "\t" + weight5d + "\t" + dist10d + "\t" + weight10d;
    }
}