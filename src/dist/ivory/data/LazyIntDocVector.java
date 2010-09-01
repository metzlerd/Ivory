/*
 * Ivory: A Hadoop toolkit for web-scale information retrieval
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package ivory.data;

import ivory.index.TermPositions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.io.WritableUtils;

import uk.ac.gla.terrier.compression.BitInputStream;
import uk.ac.gla.terrier.compression.BitOutputStream;

/**
 * Implementation of {@link IntDocVector} that lazily decodes term and
 * positional information on demand.
 * 
 * @author Tamer Elsayed
 * 
 */
public class LazyIntDocVector implements IntDocVector {

	// Term to list of positions
	// notice that this is an ArrayListOfInts, not ArrayList<Integer>
	TreeMap<Integer, int[]> termPositionsMap = null;
	private byte[] mRawBytes = null;
	transient private ByteArrayOutputStream mBytesOut = null;
	transient private BitOutputStream mBitsOut = null;
	private int nTerms;

	public LazyIntDocVector() {

	}

	public LazyIntDocVector(TreeMap<Integer, int[]> termPositionsMap) {
		this.termPositionsMap = termPositionsMap;
	}

	public void write(DataOutput out) throws IOException {
		nTerms = termPositionsMap.size();
		// write # of terms
		WritableUtils.writeVInt(out, nTerms);
		if (nTerms == 0)
			return;

		if (mRawBytes != null) {
			// this would happen if we're reading in an already-encoded
			// doc vector; if that's the case, simply write out the byte array
			WritableUtils.writeVInt(out, mRawBytes.length);
			out.write(mRawBytes);
			return;
		}
		try {
			mBytesOut = new ByteArrayOutputStream();
			mBitsOut = new BitOutputStream(mBytesOut);

			Iterator<Map.Entry<Integer, int[]>> it = termPositionsMap.entrySet().iterator();
			Map.Entry<Integer, int[]> posting = it.next();
			int[] positions = posting.getValue();
			TermPositions tp = new TermPositions();
			// write out the first termid
			int lastTerm = posting.getKey().intValue();
			mBitsOut.writeBinary(32, lastTerm);
			// write out the tf value
			mBitsOut.writeGamma((short) positions.length);
			tp.set(positions, (short) positions.length);
			// write out the positions
			writePositions(mBitsOut, tp);
			int curTerm;
			while (it.hasNext()) {
				posting = it.next();
				curTerm = posting.getKey().intValue();
				positions = posting.getValue();
				int tgap = curTerm - lastTerm;
				if (tgap <= 0) {
					throw new RuntimeException("Error: encountered invalid t-gap. termid="
							+ curTerm);
				}
				// write out the gap
				mBitsOut.writeGamma(tgap);
				tp.set(positions, (short) positions.length);
				// write out the tf value
				mBitsOut.writeGamma((short) positions.length);
				// write out the positions
				writePositions(mBitsOut, tp);
				lastTerm = curTerm;
			}
			mBitsOut.padAndFlush();
			mBitsOut.close();
			byte[] bytes = mBytesOut.toByteArray();
			WritableUtils.writeVInt(out, bytes.length);
			out.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error adding postings.");
		} catch (ArithmeticException e) {
			e.printStackTrace();
			throw new RuntimeException("ArithmeticException caught \"" + e.getMessage());
		}
	}

	public void readFields(DataInput in) throws IOException {
		nTerms = WritableUtils.readVInt(in);
		if (nTerms == 0) {
			mRawBytes = null;
			return;
		}
		mRawBytes = new byte[WritableUtils.readVInt(in)];
		in.readFully(mRawBytes);
	}

	// passing in docno and tf basically for error checking purposes
	public static void writePositions(BitOutputStream t, TermPositions p) throws IOException {
		int[] pos = p.getPositions();

		if (p.getTf() == 1) {
			// if tf=1, just write out the single term position
			t.writeGamma(pos[0]);
		} else {
			// if tf > 1, write out skip information if we want to bypass the
			// positional information during decoding
			t.writeGamma(p.getEncodedSize());

			// keep track of where we are in the stream
			int skip_pos1 = (int) t.getByteOffset() * 8 + t.getBitOffset();

			// write out first position
			t.writeGamma(pos[0]);
			// write out rest of positions using p-gaps (first order positional
			// differences)
			for (int c = 1; c < p.getTf(); c++) {
				int pgap = pos[c] - pos[c - 1];
				if (pos[c] <= 0 || pgap == 0) {
					throw new RuntimeException("Error: invalid term positions. positions="
							+ p.toString());
				}
				t.writeGamma(pgap);
			}

			// find out where we are in the stream no
			int skip_pos2 = (int) t.getByteOffset() * 8 + t.getBitOffset();

			// verify that the skip information is indeed valid
			if (skip_pos1 + p.getEncodedSize() != skip_pos2) {
				throw new RuntimeException("Ivalid skip information: skip_pos1=" + skip_pos1
						+ ", skip_pos2=" + skip_pos2 + ", size=" + p.getEncodedSize());
			}
		}
	}

	public String toString() {
		StringBuffer s = new StringBuffer("[");
		try {
			IntDocVectorReader r = this.getDocVectorReader();
			while (r.hasMoreTerms()) {
				int id = r.nextTerm();
				TermPositions pos = new TermPositions();
				r.getPositions(pos);
				s.append("(" + id + ", " + pos.getTf() + ", " + pos + ")");
			}
			s.append("]");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s.toString();
	}

	public String toStringWithTerms(PrefixEncodedTermIDMapWithIndex map) {
		StringBuffer s = new StringBuffer("");
		try {
			IntDocVectorReader r = this.getDocVectorReader();
			while (r.hasMoreTerms()) {
				int id = r.nextTerm();
				TermPositions pos = new TermPositions();
				r.getPositions(pos);
				s.append("(" + map.getTerm(id) + ", " + pos.getTf() + ", " + pos + ")");
			}
			s.append("]");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s.toString();
	}

	public IntDocVectorReader getDocVectorReader() throws IOException {
		return new Reader(mRawBytes, nTerms);
	}

	public static class Reader implements IntDocVectorReader {
		private ByteArrayInputStream mBytesIn;
		private BitInputStream mBitsIn;
		private int p = -1;
		private int mPrevTermID = -1;
		private short mPrevTf = -1;
		private int nTerms;
		private boolean mNeedToReadPositions = false;

		public Reader(byte[] bytes, int n) throws IOException {
			nTerms = n;
			if (nTerms > 0) {
				mBytesIn = new ByteArrayInputStream(bytes);
				mBitsIn = new BitInputStream(mBytesIn);
			}
		}

		public int getNumberOfTerms() {
			return nTerms;
		}

		public short getTf() {
			return mPrevTf;
		}

		public void reset() {
			try {
				mBytesIn.reset();
				mBitsIn = new BitInputStream(mBytesIn);
				p = -1;
				mPrevTf = -1;
				mNeedToReadPositions = false;
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error resetting postings.");
			}
		}

		public int nextTerm() {
			int id = -1;
			try {
				p++;
				if (mNeedToReadPositions) {
					skipPositions(mPrevTf);
				}
				mNeedToReadPositions = true;
				if (p == 0) {
					mPrevTermID = mBitsIn.readBinary(32);
					mPrevTf = (short) mBitsIn.readGamma();
					return mPrevTermID;
				} else {
					if (p > nTerms - 1)
						return -1;
					id = mBitsIn.readGamma() + mPrevTermID;
					mPrevTermID = id;
					mPrevTf = (short) mBitsIn.readGamma();
					return id;
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		}

		public int[] getPositions() {
			int[] pos = null;
			try {
				if (mPrevTf == 1) {
					pos = new int[1];
					pos[0] = mBitsIn.readGamma();
				} else {
					mBitsIn.readGamma();
					pos = new int[mPrevTf];
					pos[0] = mBitsIn.readGamma();
					for (int i = 1; i < mPrevTf; i++) {
						pos[i] = (pos[i - 1] + mBitsIn.readGamma());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("A problem in reading bits?" + e);
			}
			mNeedToReadPositions = false;

			return pos;
		}

		public boolean getPositions(TermPositions tp) {
			int[] pos = getPositions();

			if (pos == null)
				return false;

			tp.set(pos, (short) pos.length);

			return true;
		}

		public boolean hasMoreTerms() {
			return !(p >= nTerms - 1);
		}

		private void skipPositions(int tf) throws IOException {
			if (tf == 1) {
				mBitsIn.readGamma();
			} else {
				mBitsIn.skipBits(mBitsIn.readGamma());
			}
		}
	}
}
