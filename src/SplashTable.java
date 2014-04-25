import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

/**
 * 
 * @author neha
 *
 */
public class SplashTable {

	// Buckets storing key in the splash table
	// private KeyValue[][] bucketKey;

	// Buckets storing values for the key
	// private int[][] bucketValues;

	// Number of entries in the table
	private int numElementsLog;

	// Number of entries in bucket
	private int numElemBucket;

	// Number of elements per bucket
	private int numBuckets;

	// Array for storing hashes for different keys
	private HashFunction[] hashFunctions;

	// Number of hash functions
	private int numHashFunctions;

	// Number of elements in table
	private int numElements;

	private DataArray dataArray;

	private int numReinsertions;

	private int numInsertions;

	public SplashTable(int numElementsLog, int numElemBucket,
			int numHashFunctions, int numReinsertions) {
		this.numElementsLog = numElementsLog;
		this.numElemBucket = numElemBucket;
		this.numHashFunctions = numHashFunctions;
		this.numReinsertions = numReinsertions;
		this.numElements = (int) Math.pow(2, this.numElementsLog);
		this.numBuckets = (int) (numElements / numElemBucket);
		// this.bucketKey = new KeyValue[numBuckets][numElemBucket];
		// this.bucketValues = new int[numBuckets][numElemBucket];
		this.hashFunctions = new HashFunction[numHashFunctions];
		this.dataArray = new DataArray(this.numElements, this.numElemBucket);
	}

	/*
	 * private long hash(Integer key) { Random r = new Random();
	 * System.out.println(this.numBuckets + " " + this.numElementsLog + "  " +
	 * this.numElemBucket); long random = (long) (this.numBuckets * (key *
	 * r.nextDouble() % 1));
	 * 
	 * return random; }
	 */

	/**
	 * Function to calculate and store the hash functions depending upon number
	 * of hash functions to be used
	 */
	private void createHashFunctions() {
		Random r = new Random();
		for (int i = 0; i < this.numHashFunctions; i++) {
			hashFunctions[i] = new SplashTable.HashFunction(r.nextDouble(),
					numElements);
		}
	}

	/**
	 * 
	 * @param data
	 *            Function to insert given KeyValue pair in the table and
	 *            reinsert if not inserted in first attempt till number of
	 *            allowed reinsertions have been reached
	 */

	private void build(KeyValue data) {
		// TODO: Find solution for if else using ternary or something to use
		// break and continue conditionally
		int tries = 0;
		boolean opStatus = false;
		for (int i = 0; i < this.numHashFunctions; i++) {
			final long hash = hashFunctions[i].hash(data.getKey());
			opStatus = this.dataArray.insertElement((int) hash, data);
			if (opStatus) {
				numInsertions++;
				break; // Need to find a solution of this using ternary operator
			} else {
				tries++;
				// If number of tries exceeds number of time re-insertion value
				// entered by the user don't try anymore
				KeyValue elementToReinsert = this.dataArray.tryReInsert(
						(int) hash, data);
				data = elementToReinsert;
				// assert(tries > numReinsertions)? break: continue;
				if (tries > numReinsertions) {
					break;
				} else {
					numInsertions++;
					continue;
				}
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @return Find the value of a given key
	 */
	private int probe(int key) {
		for (int i = 0; this.dataArray.findKey(
				(int) hashFunctions[i].hash(key), key) != -1
				&& i < this.numHashFunctions; i++) {
			// this.dataArray.findKey((int)hashFunctions[i].hash(key), key);
			return this.dataArray
					.findKey((int) hashFunctions[i].hash(key), key);
		}
		return -1;
	}

	public static void main(String[] args) throws FileNotFoundException {
		//HOW TO RUN: splash B R S h inputfile dumpfile < probefile > resultfile
		
		int numElementsLog =2, numElementsBucket=2,numHashFunctions=1, numReinsertions=1;
		if(args.length < 5){
			System.out.println("Not sufficient Command line agruments!\nPlease try again. ");
			return;
		} else{
			
			String arg1 = args[0]; // Number of elements in a single bucket 
			String arg2 = args[1]; // Number of re-insertions allowed 	
			String arg3 = args[2]; // Logarithm of total number of elements 				
			String arg4 = args[3]; // Number of hash functions to be used
			//String dumpfileName = args[5];
			//String probefile = args[6];
			//String resultfile = args[7];		
			try{		
				numElementsLog = Integer.parseInt(arg3);// Logarithm of total number of entries in hashTable i. e. size of the hashTable. S
				numElementsBucket = Integer.parseInt(arg1);;// Number of elements in a single bucket. B should be power of 2
				if((numElementsBucket < 0) || ((numElementsBucket & (numElementsBucket - 1)) != 0)){
					System.out.println("Invalid Input for the Size of the Bucket. Should be power of 2.");
					return;
				}
				numHashFunctions = Integer.parseInt(arg4);// Number of hash functions to be used. h 
				numReinsertions = Integer.parseInt(arg2);// Number of re-insertions allowed. R
			}catch(NumberFormatException | NullPointerException exception ){
				System.out.println("Unable to process the request :" +exception.getMessage());
				return;	
			}
		}

		String fileName = args[4]; // Name of the input file containing key value pairs. filename
		String url = System.getProperty("user.dir");
		System.out.println(url);
		SplashTable splashTable = new SplashTable(numElementsLog,
				numElementsBucket, numHashFunctions,numReinsertions );

		// Create and store the hash functions depending upon the number of hash
		// functions
		splashTable.createHashFunctions();

		Scanner inFile = new Scanner(new FileReader(new File(url+"/src/"+fileName)));
		int key;
		int value;

		while (inFile.hasNext()) {
			key = inFile.nextInt();
			value = inFile.nextInt();
			System.out.println("key and values are :"+ key +" , "+value);
			splashTable.build(new KeyValue(key, value));
		}
		inFile.close();

        List<Queue<KeyValue>> list = splashTable.dataArray.hashTable;
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Queue<KeyValue> queue = (Queue<KeyValue>) iterator.next();
			System.out.println("Queue");
			for (Iterator iterator2 = queue.iterator(); iterator2.hasNext();) {
				KeyValue keyValue = (KeyValue) iterator2.next();
				System.out.println("key values are : "+keyValue.getKey()+ " : "+keyValue.getValue());
			}
		}
	}

	/**
	 * 
	 * @author neha Class that creates hash function depending upon the given
	 *         table size and multiplier
	 */
	private static final class HashFunction {
		// Multiplier to calculate hash
		private final double multiplier;
		private final long sizeTable;

		public HashFunction(double multiplier, long sizeTable) {
			this.multiplier = multiplier;
			this.sizeTable = sizeTable;
		}

		public long hash(int key) {
			return (long) (sizeTable * (key * multiplier % 1));
		}
	}

	/**
	 * 
	 * @author neha Data structure to be used for Hash Table
	 */
	private static final class DataArray {
		private List<Queue<KeyValue>> hashTable;
		private int numElemBucket;

		// private int numBuckets;

		public DataArray(int numElements, int numElemBucket) {
			hashTable = new ArrayList<Queue<KeyValue>>(numElements);
			for (int i = 0; i < numElements; i++) {
				hashTable.add(i, new LinkedList<KeyValue>());
			}
			this.numElemBucket = numElemBucket;
			// this.numBuckets = numBuckets;
		}

		/**
		 * 
		 * @param index
		 * @param data
		 * @return Method to insert elements into the hash table and return true
		 *         if inserted and false if the correspinding bucket is full
		 */
		public boolean insertElement(int index, KeyValue data) {
			
			boolean inserted = (hashTable.get(index).size() < this.numElemBucket) ? hashTable
					.get(index).add(data) : false;
			System.out.println("element inserted : "+inserted);
					return inserted;
			

		}

		/**
		 * 
		 * @param index
		 * @param data
		 * @return Try to reinsert the element which could be inserted earlier
		 *         by removing element inserted in the very beginning and insert
		 *         that again later
		 */

		public KeyValue tryReInsert(int index, KeyValue data) {
			// Its known that the corresponding bucket is full so move the data
			// around
			KeyValue elementInsertedFirst = hashTable.get(index).poll();
			hashTable.get(index).add(data);
			return elementInsertedFirst;
		}

		/**
		 * 
		 * @param index
		 * @param key
		 * @return Find a given key in the HashTable
		 */
		public int findKey(int index, int key) {
			// Assuming there are only positive keys
			int value = -1;
			for (KeyValue keyValue : hashTable.get(index)) {
				value = keyValue.getKey() == key ? keyValue.getValue() : -1;
				return value;
			}
			return value;
		}
	}
}