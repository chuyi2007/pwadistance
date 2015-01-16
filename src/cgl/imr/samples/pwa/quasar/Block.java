package cgl.imr.samples.pwa.quasar;

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

public class Block implements Value {
    // Represents final row block number
    private int rowBlockNumber;
    // Represents final column block number
    private int columnBlockNumber;
    // represents final row size
    private int rowSize;
    // represents final column size
    private int colSize;
    // indicates if distances should be treated as transpose
    // If so, the distances is of size colSize x rowSize, else rowSize x colSize
    private boolean isTranspose = false;
    // Represents the set of distances in its original form, i.e. this may be the
    // transpose set of distances for this block.
    private double[][] dist5d = null;
    private double[][] weight5d = null;
    private double[][] dist10d = null;
    private double[][] weight10d = null;

    public Block(int rowBlockNumber, int colBlockNumber) {
        this.rowBlockNumber = rowBlockNumber;
        this.columnBlockNumber = colBlockNumber;
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
    public Block(byte[] bytes) throws SerializationException {
        this(0, 0);
        fromBytes(bytes);
    }

    /**
     * Returns the final row block number for this block. This is irrespective
     * of the fact whether the stored set of distances represents the transpose
     * or not.
     * @return final row block number
     */
    public int getRowBlockNumber() {
        return rowBlockNumber;
    }

    /**
     * Returns the final column block number for this block. This is irrespective
     * of the fact whether the stored set of distances represents the transpose
     * or not.
     * @return final column block number
     */
    public int getColumnBlockNumber() {
        return columnBlockNumber;
    }

    /**
     * Indicates if the stored set of distances in this block represents the
     * transpose values in the final output.
     * @return true if the stored distances represent the transpose, false otherwise.
     */
    public boolean isTranspose() {
        return isTranspose;
    }

    /**
     * Returns the final row size for this block. block. This is irrespective
     * of the fact whether the stored set of distances represents the transpose
     * or not.
     * @return the final row size
     */
    public int getRowSize() {
        return rowSize;
    }

    /**
     * Returns the final column size for this block. block. This is irrespective
     * of the fact whether the stored set of distances represents the transpose
     * or not.
     * @return the final column size
     */
    public int getColSize() {
        return colSize;
    }

    @Override
    /**
     * Builds a block from an array of bytes. The byte array should encode the following values
     * in order. Note. The last four values are optional.
     *
     * int: row block number
     * int: column block number
     * boolean: is transpose or not
     * int: row size
     * int: column size
     * short[][]: (isTranspose ? colSize : rowSize) X (isTranspose ? rowSize : colSize)
     */
    public void fromBytes(byte[] bytes) throws SerializationException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(bytes);
        DataInputStream din = new DataInputStream(baInputStream);

        try {
            this.rowBlockNumber = din.readInt();
            this.columnBlockNumber = din.readInt();

            if (din.available() > 0) {
                this.isTranspose = din.readBoolean();
                this.rowSize = din.readInt();
                this.colSize = din.readInt();

                int rows = isTranspose ? colSize : rowSize;
                int cols = isTranspose ? rowSize : colSize;
                this.dist5d = new double[rows][cols];

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.dist5d[i][j] = din.readDouble();
                    }
                }
                
                this.weight5d = new double[rows][cols];

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.dist5d[i][j] = din.readDouble();
                    }
                }
                
                this.dist10d = new double[rows][cols];

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.dist10d[i][j] = din.readDouble();
                    }
                }
                
                this.weight10d = new double[rows][cols];

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.weight10d[i][j] = din.readDouble();
                    }
                }
            }
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
            dout.writeInt(rowBlockNumber);
            dout.writeInt(columnBlockNumber);

            if (dist5d != null) {
                dout.writeBoolean(isTranspose);
                dout.writeInt(rowSize);
                dout.writeInt(colSize);

                for (double[] distance : dist5d) {
                    for (double i : distance) {
                        dout.writeDouble(i);
                    }
                }
                
                for (double[] distance : weight5d) {
                    for (double i : distance) {
                        dout.writeDouble(i);
                    }
                }
                
                for (double[] distance : dist10d) {
                    for (double i : distance) {
                        dout.writeDouble(i);
                    }
                }
                
                for (double[] distance : weight10d) {
                    for (double i : distance) {
                        dout.writeDouble(i);
                    }
                }
            }
            dout.flush();
            marshalledBytes = baOutputStream.toByteArray();
            dout.close();
        } catch (IOException ioe) {
            throw new SerializationException(ioe);
        }
        return marshalledBytes;
    }

	public double[][] getWeight10d() {
		return weight10d;
	}

	public void setWeight10d(double[][] weight10d) {
		this.weight10d = weight10d;
	}

	public double[][] getDist10d() {
		return dist10d;
	}

	public void setDist10d(double[][] dist10d) {
		this.dist10d = dist10d;
	}

	public double[][] getWeight5d() {
		return weight5d;
	}

	public void setWeight5d(double[][] weight5d) {
		this.weight5d = weight5d;
	}

	public double[][] getDist5d() {
		return dist5d;
	}

	public void setDist5d(double[][] dist5d, boolean isTranspose) {
		this.dist5d = dist5d;
        this.isTranspose = isTranspose;
        this.rowSize = isTranspose ? dist5d[0].length : dist5d.length;
        this.colSize = isTranspose ? dist5d.length : dist5d[0].length;
	}
}