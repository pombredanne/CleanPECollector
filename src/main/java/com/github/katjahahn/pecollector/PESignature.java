/*******************************************************************************
 * Copyright 2014 Katja Hahn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.github.katjahahn.pecollector;

import static com.github.katjahahn.pecollector.ByteArrayUtil.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Reads the offset of the PE signature and the signature itself.
 * <p>
 * Can be used to verify that the file is indeed a PE file.
 * 
 * @author Katja Hahn
 * 
 */
public class PESignature {

	/**
	 * The location of the PE signature offset is {@value} .
	 */
	public static final int PE_OFFSET_LOCATION = 0x3c;

	/**
	 * The signature bytes of the string PE\0\0.
	 */
	public static final byte[] PE_SIG = "PE\0\0".getBytes();

	/**
	 * The length of the PE signature is {@value} .
	 */
	public static final int PE_SIG_LENGTH = 4;

	private long peOffset = -1;
	private final File file;

	/**
	 * Creates a PESignature instance with the input file.
	 * 
	 * @param file
	 *            the PE file that should be checked for the signature
	 */
	public PESignature(File file) {
		this.file = file;
	}

	/**
	 * Reads the PE signature and sets the peOffset.
	 * 
	 * @throws FileFormatException
	 *             if file is not a PE file
	 * @throws IOException
	 *             if something went wrong while trying to read the file
	 */
	public void read() throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			throwIf(file.length() < PE_OFFSET_LOCATION + 2);
			raf.seek(PE_OFFSET_LOCATION);
			byte[] offsetBytes = new byte[2];
			raf.readFully(offsetBytes);
			peOffset = (long) bytesToInt(offsetBytes);
			throwIf(file.length() < peOffset + 4);
			raf.seek(peOffset);
			byte[] peSigVal = new byte[4];
			raf.readFully(peSigVal);
			for (int i = 0; i < PE_SIG.length; i++) {
				throwIf(peSigVal[i] != PE_SIG[i]);
			}
		}
	}

	/**
	 * Tries to read the PE signature of the current file and returns true, iff
	 * it was successfull.
	 * 
	 * @return true if the file has the PE signature, false otherwise.
	 */
	public boolean hasSignature() {
		try {
			read();
			return true;
		} catch (FileFormatException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Throws FileFormatException and sets peOffset to absent iff b is true.
	 * 
	 * @param b
	 * @throws FileFormatException
	 */
	private void throwIf(boolean b) throws FileFormatException {
		if (b) {
			peOffset = -1;
			throw new FileFormatException("given file is no PE file");
		}
	}

	/**
	 * Returns the offset of the PE signature. Returns -1 if
	 * file hasn't been read yet or the read file was no PE file.
	 * 
	 * @return offset of PE signature, -1 if file not read or file
	 *         is no PE
	 */
	public long getOffset() {
		return peOffset;
	}

}
