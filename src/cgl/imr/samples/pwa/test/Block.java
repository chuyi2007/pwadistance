package cgl.imr.samples.pwa.test;

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
    short[][] scoreAB;
	short[][] scoreBA;
	short[][] lengthAB;
	short[][] lengthBA;
	short[][] identicalPairsAB;
	short[][] identicalPairsBA;
	
	short[][] scoreABReverse;
	short[][] scoreBAReverse;
	short[][] scoreAReverseBReverse;
	short[][] scoreBReverseAReverse;
	short[][] lengthABReverse;
	short[][] lengthBAReverse;
	short[][] lengthAReverseBReverse;
	short[][] lengthBReverseAReverse;
	short[][] identicalPairsABReverse;
	short[][] identicalPairsBAReverse;
	short[][] identicalPairsAReverseBReverse;
	short[][] identicalPairsBReverseAReverse;
    

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


	/**
     * Sets the distances for this block. If the array of distances represent the transpose
     * for this block then <code>isTranspose</code> should be set to <code>true</code>
     *
     * @param distances the array of distances
     * @param isTranspose indicates if the distance array represents the transpose set of distances for this block
     */
    public void setScore(short[][] scoreAB, boolean isTranspose) {
        this.scoreAB = scoreAB;
        this.isTranspose = isTranspose;
        this.rowSize = isTranspose ? scoreAB[0].length : scoreAB.length;
        this.colSize = isTranspose ? scoreAB.length : scoreAB[0].length;
    }

	public short[][] getScoreAB() {
		return scoreAB;
	}

	public void setScoreAB(short[][] scoreAB) {
		this.scoreAB = scoreAB;
	}

	public short[][] getScoreBA() {
		return scoreBA;
	}

	public void setScoreBA(short[][] scoreBA) {
		this.scoreBA = scoreBA;
	}

	public short[][] getLengthAB() {
		return lengthAB;
	}

	public void setLengthAB(short[][] lengthAB) {
		this.lengthAB = lengthAB;
	}

	public short[][] getLengthBA() {
		return lengthBA;
	}

	public void setLengthBA(short[][] lengthBA) {
		this.lengthBA = lengthBA;
	}

	public short[][] getIdenticalPairsAB() {
		return identicalPairsAB;
	}

	public void setIdenticalPairsAB(short[][] identicalPairsAB) {
		this.identicalPairsAB = identicalPairsAB;
	}

	public short[][] getIdenticalPairsBA() {
		return identicalPairsBA;
	}

	public void setIdenticalPairsBA(short[][] identicalPairsBA) {
		this.identicalPairsBA = identicalPairsBA;
	}

	public short[][] getScoreABReverse() {
		return scoreABReverse;
	}

	public void setScoreABReverse(short[][] scoreABReverse) {
		this.scoreABReverse = scoreABReverse;
	}

	public short[][] getScoreBAReverse() {
		return scoreBAReverse;
	}

	public void setScoreBAReverse(short[][] scoreBAReverse) {
		this.scoreBAReverse = scoreBAReverse;
	}

	public short[][] getLengthABReverse() {
		return lengthABReverse;
	}

	public void setLengthABReverse(short[][] lengthABReverse) {
		this.lengthABReverse = lengthABReverse;
	}

	public short[][] getLengthBAReverse() {
		return lengthBAReverse;
	}

	public void setLengthBAReverse(short[][] lengthBAReverse) {
		this.lengthBAReverse = lengthBAReverse;
	}

	public short[][] getIdenticalPairsABReverse() {
		return identicalPairsABReverse;
	}

	public void setIdenticalPairsABReverse(short[][] identicalPairsABReverse) {
		this.identicalPairsABReverse = identicalPairsABReverse;
	}

	public short[][] getIdenticalPairsBAReverse() {
		return identicalPairsBAReverse;
	}

	public void setIdenticalPairsBAReverse(short[][] identicalPairsBAReverse) {
		this.identicalPairsBAReverse = identicalPairsBAReverse;
	}

	public short[][] getScoreAReverseBReverse() {
		return scoreAReverseBReverse;
	}

	public void setScoreAReverseBReverse(short[][] scoreAReverseBReverse) {
		this.scoreAReverseBReverse = scoreAReverseBReverse;
	}

	public short[][] getScoreBReverseAReverse() {
		return scoreBReverseAReverse;
	}

	public void setScoreBReverseAReverse(short[][] scoreBReverseAReverse) {
		this.scoreBReverseAReverse = scoreBReverseAReverse;
	}

	public short[][] getLengthAReverseBReverse() {
		return lengthAReverseBReverse;
	}

	public void setLengthAReverseBReverse(short[][] lengthAReverseBReverse) {
		this.lengthAReverseBReverse = lengthAReverseBReverse;
	}

	public short[][] getLengthBReverseAReverse() {
		return lengthBReverseAReverse;
	}

	public void setLengthBReverseAReverse(short[][] lengthBReverseAReverse) {
		this.lengthBReverseAReverse = lengthBReverseAReverse;
	}

	public short[][] getIdenticalPairsAReverseBReverse() {
		return identicalPairsAReverseBReverse;
	}

	public void setIdenticalPairsAReverseBReverse(
			short[][] identicalPairsAReverseBReverse) {
		this.identicalPairsAReverseBReverse = identicalPairsAReverseBReverse;
	}

	public short[][] getIdenticalPairsBReverseAReverse() {
		return identicalPairsBReverseAReverse;
	}

	public void setIdenticalPairsBReverseAReverse(
			short[][] identicalPairsBReverseAReverse) {
		this.identicalPairsBReverseAReverse = identicalPairsBReverseAReverse;
	}

	public void setRowBlockNumber(int rowBlockNumber) {
		this.rowBlockNumber = rowBlockNumber;
	}

	public void setColumnBlockNumber(int columnBlockNumber) {
		this.columnBlockNumber = columnBlockNumber;
	}

	public void setRowSize(int rowSize) {
		this.rowSize = rowSize;
	}

	public void setColSize(int colSize) {
		this.colSize = colSize;
	}

	public void setTranspose(boolean isTranspose) {
		this.isTranspose = isTranspose;
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
                
                this.scoreAB = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.scoreAB[i][j] = din.readShort();
                    }
                }
                
                this.scoreBA = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.scoreBA[i][j] = din.readShort();
                    }
                }
                
                this.lengthAB = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.lengthAB[i][j] = din.readShort();
                    }
                }
                
                this.lengthBA = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.lengthBA[i][j] = din.readShort();
                    }
                }
                
                this.identicalPairsAB = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.identicalPairsAB[i][j] = din.readShort();
                    }
                }
                
                this.identicalPairsBA = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.identicalPairsBA[i][j] = din.readShort();
                    }
                }
                
                this.scoreABReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.scoreABReverse[i][j] = din.readShort();
                    }
                }
                
                this.scoreBAReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.scoreBAReverse[i][j] = din.readShort();
                    }
                }
                
                this.scoreAReverseBReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.scoreAReverseBReverse[i][j] = din.readShort();
                    }
                }
                
                this.scoreBReverseAReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.scoreBReverseAReverse[i][j] = din.readShort();
                    }
                }
                this.lengthABReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.lengthABReverse[i][j] = din.readShort();
                    }
                }
                
                this.lengthBAReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.lengthBAReverse[i][j] = din.readShort();
                    }
                }
                
                this.lengthAReverseBReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.lengthAReverseBReverse[i][j] = din.readShort();
                    }
                }
                
                this.lengthBReverseAReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.lengthBReverseAReverse[i][j] = din.readShort();
                    }
                }
                
                this.identicalPairsABReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.identicalPairsABReverse[i][j] = din.readShort();
                    }
                }
                
                this.identicalPairsBAReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.identicalPairsBAReverse[i][j] = din.readShort();
                    }
                }
                
                this.identicalPairsAReverseBReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.identicalPairsAReverseBReverse[i][j] = din.readShort();
                    }
                }
                
                this.identicalPairsBReverseAReverse = new short[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        this.identicalPairsBReverseAReverse[i][j] = din.readShort();
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

            if (scoreAB != null) {
                dout.writeBoolean(isTranspose);
                dout.writeInt(rowSize);
                dout.writeInt(colSize);
            	
                for (short[] row : scoreAB) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : scoreBA) {
                    for (short i : row) {
                        dout.writeShort(i);
                    }
                }
                for (short[] row : lengthAB) {
                    for (short i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : lengthBA) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : identicalPairsAB) {
                    for (short i : row) {
                        dout.writeShort(i);
                    }
                }
                for (short[] row : identicalPairsBA) {
                    for (short i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : scoreABReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : scoreBAReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : scoreAReverseBReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : scoreBReverseAReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : lengthABReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : lengthBAReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : lengthAReverseBReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : lengthBReverseAReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : identicalPairsABReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : identicalPairsBAReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : identicalPairsAReverseBReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
                    }
                }
                
                for (short[] row : identicalPairsBReverseAReverse) {
                    for (int i : row) {
                        dout.writeShort(i);
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
}