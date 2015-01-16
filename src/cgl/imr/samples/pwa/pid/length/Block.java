package cgl.imr.samples.pwa.pid.length;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cgl.imr.base.SerializationException;
import cgl.imr.base.Value;

/**
 * @author Yang Ruan (yangruan@cs.indiana.edu)
 * @author Saliya Ekanayake (sekanaya at cs dot indiana dot edu)
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
    private short[][] percent = null;
    
    //This is the alignment lengths
    private short[][] lengths = null;
    
    private short[][] lengthsReverse = null;
    
    //This is the JukesCantor distance
    private double[][] jukes = null;
    
    //This is the Kimura2
    private double[][] kimura = null;
    
    //percent choice of reverse or non-reverse
    private short[][] percentChoice = null;
    
    //jukes choice of reverse or non-reverse
    private short[][] jukesChoice = null;
    
    //kimura choice of reverse or non-reverse
    private short[][] kimuraChoice = null;
    

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
     * Returns the distances array for this block. Note. This may represent the transpose
     * set of distances.
     *
     * @return distance array
     */
    public short[][] getPercent() {
        return percent;
    }
    
    public short[][] getLengths(){
    	return lengths;
    }

    public double[][] getJukes() {
		return jukes;
	}

	public double[][] getKimura() {
		return kimura;
	}

	public short[][] getPercentChoice() {
		return percentChoice;
	}

	public void setPercentChoice(short[][] percentChoice) {
		this.percentChoice = percentChoice;
	}

	public short[][] getJukesChoice() {
		return jukesChoice;
	}

	public void setJukesChoice(short[][] jukesChoice) {
		this.jukesChoice = jukesChoice;
	}

	public short[][] getKimuraChoice() {
		return kimuraChoice;
	}

	public void setKimuraChoice(short[][] kimuraChoice) {
		this.kimuraChoice = kimuraChoice;
	}

	public void setJukes(double[][] jukes) {
		this.jukes = jukes;
	}

	public void setKimura(double[][] kimura) {
		this.kimura = kimura;
	}

	public short[][] getLengthsReverse() {
		return lengthsReverse;
	}

	public void setLengthsReverse(short[][] lengthsReverse) {
		this.lengthsReverse = lengthsReverse;
	}

	/**
     * Sets the distances for this block. If the array of distances represent the transpose
     * for this block then <code>isTranspose</code> should be set to <code>true</code>
     *
     * @param distances the array of distances
     * @param isTranspose indicates if the distance array represents the transpose set of distances for this block
     */
    public void setPercent(short[][] percent, boolean isTranspose) {
        this.percent = percent;
        this.isTranspose = isTranspose;
        this.rowSize = isTranspose ? percent[0].length : percent.length;
        this.colSize = isTranspose ? percent.length : percent[0].length;
    }
    
    public void setLengths(short[][] lengths){
    	this.lengths = lengths;
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
                
                this.percent = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.percent[i][j] = din.readShort();
                    }
                }
                
                this.percentChoice = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.percentChoice[i][j] = din.readShort();
                    }
                }
                
                this.jukes = new double[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.jukes[i][j] = din.readDouble();
                    }
                }
                
                this.jukesChoice = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.jukesChoice[i][j] = din.readShort();
                    }
                }
                
                this.kimura = new double[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.kimura[i][j] = din.readDouble();
                    }
                }
                
                this.kimuraChoice = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.kimuraChoice[i][j] = din.readShort();
                    }
                }
                   
                this.lengths = new short[rows][cols];
                for(int i = 0; i < rows; i++) {
                	for(int j = 0; j < cols; j++){
                		this.lengths[i][j] = din.readShort();
                	}
                }
                
                this.lengthsReverse = new short[rows][cols];
                for(int i = 0; i < rows; i++) {
                	for(int j = 0; j < cols; j++){
                		this.lengthsReverse[i][j] = din.readShort();
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

            if (percent != null) {
                dout.writeBoolean(isTranspose);
                dout.writeInt(rowSize);
                dout.writeInt(colSize);

                for (short[] row : percent) {
                    for (short i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : percentChoice) {
                    for (short i : row) {
                        dout.writeShort(i);
                    }
                }
                for (double[] row : jukes) {
                    for (double i : row) {
                        dout.writeDouble(i);
                    }
                }
                for (short[] row : jukesChoice) {
                    for (short i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (double[] row : kimura) {
                    for (double i : row) {
                        dout.writeDouble(i);
                    }
                }
                
                for (short[] row : kimuraChoice) {
                    for (short i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : lengths) {
                	for (short i: row)
                		dout.writeShort(i);
                }
                
                for (short[] row : lengthsReverse) {
                	for (short i: row)
                		dout.writeShort(i);
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
}