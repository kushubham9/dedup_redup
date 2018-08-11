# Deduplication / Reduplication
Deduplication / Reduplication of file in Java.

# General
Data deduplication refers to a technique for eliminating redundant data in a data set. In the process of deduplication, extra copies of the same data are deleted, leaving only one copy to be stored. Data is analyzed to identify duplicate byte patterns to ensure the single instance is indeed the single file. Then, duplicates are replaced with a reference that points to the stored chunk.

Pick the size of a chunk, which is the granularity (e.g., 1KB) at which you want to deduplicate a file. Inspect every non-overlapping chunk in the file (e.g., 0th KB, 1st KB, ..., 99th KB), and identify the unique ones. For each unique chunk, take note of where they are found in the file (e.g., 0, 1, 2, ..., 99).

# Program
In this program, there are two prime methods dedup() and redup() defined. Both method accepts input and output absolute file locations. The size of the chunk by default is 1Kb. Here checksum is used to verify if the redup and dedup files are exactly the same as expected. 
