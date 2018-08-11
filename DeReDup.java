package com.file.compression;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.security.MessageDigest;
import java.io.RandomAccessFile;

/**
 * The following class de duplicates & re duplicate the file provided as input.
 * 
 * Input File Processing (dedup):
 * The input file is read through FileInputStream and read in chunks of 1KB.
 * It then through a custom checksum function based on MD5 hash generates a unique ID for each chunk.
 * 2 chunks with the same content will have the same checksum.
 * Each checksum is mapped to the chunk index through hash map.
 * Only unique chunks are written to the deduped_file_path.
 * 
 *  Output File Processing (redup)
 *  Over here, the input file in read again through the FileInputStream in chunks of 1KB.
 *  Through the same checksum function, for each chunk, a unique checksum string is generated.
 *  This string is used to look up the indexes in the hashmap (To determine its positions)
 *  RandomAccessFile class is used to write back the data. 
 *  
 * @author shubham
 *
 */
class DeReDup {

	private static HashMap<String, ArrayList<Integer>> chunkMapping = new HashMap<String, ArrayList<Integer>>();

	// The original file path location. used for validation. 
	private static String inputFilePath;
	
	// chunk size.
	private static int chunkSize = 1024;
	
	/**
	 * De Duplicates the input file.
	 * @param input_file_path <p>input file absolute path</p>
	 * @param deduped_file_path <p> Resultant file absolute path </p>
	 */
	static void dedup(String input_file_path, String deduped_file_path) {
		byte[] contentBuffer = new byte[chunkSize];
		inputFilePath = input_file_path;
		String bufferCheckSum;
		int chunkIndex = 0;
		FileInputStream fInput = getInputStream(input_file_path);
		FileOutputStream fOutput = getOutStream(deduped_file_path);

		try {
			/**
			 * Read the input file in chunks of 1KB.
			 */
			while (fInput.read(contentBuffer) != -1) {
				bufferCheckSum = getCheckSum(contentBuffer);

				/**
				 * If the chunk is unique, put it in the hash map with the current Index.
				 * Else, add the current index to the arrayList.
				 */
				if (null == chunkMapping.get(bufferCheckSum)) {
					writeToOutputFile(contentBuffer, fOutput);
					ArrayList<Integer> indexList = new ArrayList<Integer>();
					indexList.add(chunkIndex);
					
					chunkMapping.put(bufferCheckSum, indexList);
				} else {
					chunkMapping.get(bufferCheckSum).add(chunkIndex);
				}
				chunkIndex++;
			}
		} catch (Exception e) {
			// Exception occurred
			// Handle the exception
		} finally {
			// Close the file handlers.
			closeConnections(fInput, fOutput);
		}
	}

	/**
	 * Re-Duplicated the chunked files into the orignal file.
	 * @param deduped_file_path <p> Input file path location </p>
	 * @param output_file_path <p> Output file path </p>
	 * @return
	 */
	static boolean redup(String deduped_file_path, String output_file_path) {
		byte[] contentBuffer = new byte[chunkSize];
		String bufferCheckSum;
		int chunkIndex = 0;
		FileInputStream fInput = getInputStream(deduped_file_path);
		RandomAccessFile fOutput = getRandomOutStream(output_file_path);
		
		try {
			while (fInput.read(contentBuffer)!= -1) {
				bufferCheckSum = getCheckSum(contentBuffer);
				if (null == chunkMapping.get(bufferCheckSum)) {
					// The chunk is not valid.
					return false;
				} else {
					for (Integer index : chunkMapping.get(bufferCheckSum)) {
						// Move the file pointer to the appropriate index.
						fOutput.seek(index * chunkSize);
						fOutput.write(contentBuffer);
					}
				}
			}
		} catch (Exception e) {
			// Exception occurred
		} finally {
			// Close the handlers.
			try {
				fInput.close();
				fOutput.close();
			} catch (Exception e) {
				// Connection close failure.
			}
		}
		
		return isFileSame(output_file_path);
	}
	
	/**
	 * Checks if the 2 files are the same and non corrupted.
	 * @param resultant_file
	 * @return
	 */
	private static boolean isFileSame(String resultant_file) {
		return getFileCheckSum(resultant_file).equals(getFileCheckSum(inputFilePath));
	}
	
	/**
	 * Converts the content to byte[] and returns its checksum
	 * @param input_file Absolute url of the file.
	 * @return
	 */
	private static String getFileCheckSum(String input_file) {
		RandomAccessFile fileHandler = null;
		byte[] resultantByte = {};
		
		try {
			fileHandler = new RandomAccessFile(input_file, "r");
			resultantByte = new byte[(int)fileHandler.length()];
			fileHandler.readFully(resultantByte);
			fileHandler.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return getCheckSum(resultantByte);
	}

	/**
	 * Returns the RandomAccessFile handler
	 * @param output_file_path
	 * @return
	 */
	private static RandomAccessFile getRandomOutStream(String output_file_path) {
		RandomAccessFile randomFileHandler = null;
		try {
			randomFileHandler = new RandomAccessFile(output_file_path, "rw");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return randomFileHandler;
	}

	/**
	 * Returns FileOutputStream handler.
	 * @param deduped_file_path
	 * @return
	 */
	private static FileOutputStream getOutStream(String deduped_file_path) {
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(deduped_file_path, true);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return outputStream;
	}

	/**
	 * Return FileInputStream handler
	 * @param input_file_path
	 * @return
	 */
	private static FileInputStream getInputStream(String input_file_path) {
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(input_file_path);
		} catch (Exception e) {
			// Invalid location
		}
		return inputStream;
	}

	/**
	 * Close the FileInputStream & FileOutputStream handler
	 * @param fInput
	 * @param fOutput
	 */
	private static void closeConnections(FileInputStream fInput, FileOutputStream fOutput) {
		try {
			fInput.close();
			fOutput.close();
		} catch (Exception e) {

		}
	}

	/**
	 * Write the byte[] to the output file.
	 * @param contentBuffer
	 * @param fOutput
	 * @throws IOException
	 */
	private static void writeToOutputFile(byte[] contentBuffer, FileOutputStream fOutput) throws IOException {
		fOutput.write(contentBuffer);
	}

	/**
	 * Compute the unique MD5 checksum
	 * @param contentChunk
	 * @return
	 */
	static private String getCheckSum(byte[] contentChunk) {
		StringBuffer sb = new StringBuffer("");
		try {
			MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(contentChunk);
			byte[] mdbytes = mDigest.digest();

			// Convert the checksum bytes to hex;
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return sb.toString();
	}

}
