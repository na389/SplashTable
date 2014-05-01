import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * Splash Table is bucketized version of d-ary cuckoo hashing.
 * This hash table mainly improves the probing over cuckoo hashing
 * by removing control dependency from the probing routing and relying
 * on data dependency thus reducing the number of cycles required to
 * perform the probing of the hash-table. A splash table inserts an
 * item using multiple hash functions. A key can be inserted into a
 * bucket corresponding to any one of the functions. Probes need to
 * consult all hash buckets for the key. The cool thing about splash
 * tables is that if there is no room in any bucket for a new key, one
 * can evict some other key to make room. This other key can be re-inserted
 * elsewhere, depending on the values of its other hash functions.
 * Main functions:
 * {@link build(int, KeyValue)},
 * {@link dump()},
 * {@link probe(int)}
 * @class HashFunction: Used to represent hash functions to be used depending
 * upon the number of hash functions specified by the user
 * @class DataArray: Data structure for the splash table and its corresponding operations
 *
 */
public class SplashTable {
	//Log base 2 of number of entries in the table
	private int numElementsLog;

	// Number of entries in bucket
	private int numElemBucket;

	// Number of buckets in the table
	private int numBuckets;

	// Array for storing hashes for different keys
	private HashFunction[] hashFunctions;

	// Number of hash functions
	private int numHashFunctions;

	// Number of elements in table
	private int numElements;

	//Data Structure to be used as hash table
	private DataArray dataArray;

	//Number of re-insertions allowed
	private final int numReinsertions;

	//Total number of successful insertions done
	private int numInsertions;

	//Dump file name
	private String dumpFileName;

	public SplashTable(int numElementsLog, int numElemBucket,
			int numHashFunctions, int numReinsertions) {
		this.numElementsLog = numElementsLog;
		this.numElemBucket = numElemBucket;
		this.numHashFunctions = numHashFunctions;
		this.numReinsertions = numReinsertions;
		this.numElements = (int) Math.pow(2, this.numElementsLog);
		this.numBuckets = (int) (numElements / numElemBucket);
		this.hashFunctions = new HashFunction[numHashFunctions];
		// Create and store the hash functions depending upon the number of hash functions
		createHashFunctions();
		this.dataArray = new DataArray(this.numBuckets, this.numElemBucket);
	}

	/**
	 * Function to calculate and store the hash functions depending upon number
	 * of hash functions to be used
	 */
	private void createHashFunctions() {
		for (int i = 0; i < this.numHashFunctions; i++) {
			hashFunctions[i] = new SplashTable.HashFunction(getRandom(),
					numBuckets);
		}
	}


	//Generating random odd multiplier
	public int getRandom(){
		Random rn = new Random();
		int randomNum = 0;
		randomNum = rn.nextInt(Integer.MAX_VALUE);
		if ((randomNum & 1) == 1) {
			return randomNum;
		} else {
			if(randomNum > 0){
				randomNum = randomNum - 1;
			}
		}
		assert((randomNum & 1) == 1);
		return randomNum;
	}

	/**
	 *
	 * @param data
	 *            Function to insert given KeyValue pair in the table and
	 *            reinsert if not inserted in first attempt till number of
	 *            allowed reinsertions have been reached
	 */
    private void build(int tries, KeyValue data) {
        int[] hashFunctionUsed = new int[this.numHashFunctions];
        int i = 0;
        boolean opStatus = false;
        for (; i < this.numHashFunctions; i++) {
            if(data.getKey() == 0){
            	System.out.println("Invalid Key");
                opStatus = false;
                continue;
            }
            final int hash = (int)hashFunctions[i].hash(data.getKey());
            opStatus = this.dataArray.insertElement(hash, data);
            hashFunctionUsed[i] = (int)hash;
            if (opStatus) {
                break;
            } else {
                continue;
            }
        }

        if(!opStatus && i == this.numHashFunctions ){
        	if(this.numHashFunctions == 2)
        		reInsert(++tries, data, 0);
        	else
        		reInsert(++tries, data, i-1);
        }else{
            numInsertions++;
        }
    }

    /**
     *
     * @param tries
     * @param data
     * @param i
     */
    private void reInsert(int tries, KeyValue data,int i){    	
    	final int hash = (int)hashFunctions[i].hash(data.getKey());
    	// If number of tries exceeds number of time re-insertion value entered by the user don't try anymore
    	if (tries > numReinsertions) {    		
    		dump(dumpFileName);
    		System.exit(0);
    		return;
    	} else {
    		KeyValue elementToReinsert = this.dataArray.tryReInsert( hash, data);
    		if(elementToReinsert != null)
    			build(tries, elementToReinsert);
    		return;
    	}
    }


	/**
	 *
	 * @param key
	 * @return Value of a given key
	 */
	private int probe(int key) {
		int result = 0;
		for (int i = 0; i < this.numHashFunctions && result == 0; i++) {
			int index = (int)hashFunctions[i].hash(key);
			result = this.dataArray.findKey(index, key);
		}
		return result;
	}

	public static void main(String[] args){
		//HOW TO RUN: splash B R S h inputfile dumpfile < probefile > resultfile
		// S = 2 B = 2 h = 1 R = 1 inputfile dumpfile probefile resultfile
		int numElementsLog =2, numElementsBucket=2,numHashFunctions=1, numReinsertions=1;
		if(args.length < 5){
			System.out.println("Not sufficient Command line agruments!\nPlease try again. ");
			return;
		} else{

			String arg1 = args[0]; // Number of elements in a single bucket
			String arg2 = args[1]; // Number of re-insertions allowed
			String arg3 = args[2]; // Logarithm of total number of elements
			String arg4 = args[3]; // Number of hash functions to be used

			try{
				numElementsLog = Integer.parseInt(arg3);// Logarithm of total number of entries in hashTable i. e. size of the hashTable. S
				numElementsBucket = Integer.parseInt(arg1);;// Number of elements in a single bucket. B should be power of 2
				if((numElementsBucket < 0) || ((numElementsBucket & (numElementsBucket - 1)) != 0)){
					System.out.println("Invalid Input for the Size of the Bucket. Should be power of 2.");
					return;
				}
				numHashFunctions = Integer.parseInt(arg4);// Number of hash functions to be used. h
				numReinsertions = Integer.parseInt(arg2);// Number of re-insertions allowed. R
			}catch(NumberFormatException e1){
				System.out.println("Unable to process the request :" +e1.getMessage());
				return;
			}catch (NullPointerException e2 ) {
				System.out.println("Unable to process the request :" +e2.getMessage());
				return;
			}
		}

		String inputFileName = args[4]; // Name of the input file containing key value pairs. filename
		String dumpFileName = args[5]; //Name of the dumpfile		

		BufferedReader br;
		br = new BufferedReader(new InputStreamReader(System.in));

		SplashTable splashTable = new SplashTable(numElementsLog,
				numElementsBucket, numHashFunctions,numReinsertions );
		splashTable.dumpFileName = dumpFileName;
		Scanner inFile = null;
		try {
			inFile = new Scanner(new FileReader(new File(inputFileName)));
		} catch (FileNotFoundException e) {
			System.out.println("Input file not found");
			e.printStackTrace();
		}
		int key;
		int value;

		try{
			while (inFile.hasNext()) {
				key = inFile.nextInt();
				value = inFile.nextInt();
				splashTable.build(0, new KeyValue(key, value));
			}
		}catch(NumberFormatException e1){

		}catch (NullPointerException e2 ) {

		}catch (InputMismatchException e3 ) {
			if(e3 instanceof InputMismatchException){
				System.out.println("Number entered is out of range");
				splashTable.dump(dumpFileName);
				System.exit(0);
			}
		}
		splashTable.dump(dumpFileName);
		inFile.close();


		List<String> probeKeys = new ArrayList<String>();
		 try {
				//probeKeys = splashTable.readTextFile(probefile);
				//probeKeys.add(Integer.parseInt(line.trim()));
				String line;
				while ((line = br.readLine()) != null) {
					probeKeys.add(line);

				}

			}catch(NumberFormatException e1){
				System.out.println("Error Reading Probe File");
				e1.printStackTrace();
			}catch (NullPointerException e2 ) {
				System.out.println("Error Reading Probe File");
				e2.printStackTrace();
			} catch (IOException e) {
				System.out.println("Error Reading Probe File");
				e.printStackTrace();
			}
		List<String> resultFileData = new ArrayList<String>();
		int valueOutput = 0;
		for(String keyInput: probeKeys){
			valueOutput = splashTable.probe(Integer.parseInt(keyInput.trim()));
			switch (valueOutput){
				case 0:{
					System.out.println(keyInput+" 0");
					break;
				}
				default: {
					System.out.println(keyInput+" "+valueOutput);
					break;
				}
			}
		}	
	}

	/**
	 * Creating Dump File
	 * B S h N
	 * H[0] H[1] ... H[h-1]
     * (K[0] P[0]
     * K[1] P[1]
     * ...
     * K[2^S-1] P[2^S-1]
	 * @param dumpFileName
	 */

	private void dump(String dumpFileName){
		ArrayList<String> outputDump = new ArrayList<String>();
		outputDump.add(""+numElemBucket+  " "+numElementsLog+ " "+numHashFunctions+ " "+numInsertions );
		StringBuilder str = new StringBuilder();
		for(HashFunction function : hashFunctions){
			str.append(function.toString());
		}
		outputDump.add(str.toString());
        List<Queue<KeyValue>> list = dataArray.hashTable;
        for (Iterator<Queue<KeyValue>> iterator = list.iterator(); iterator.hasNext();) {
			Queue<KeyValue> queue = (Queue<KeyValue>) iterator.next();
			int i = 0;
			for (Iterator<KeyValue> iterator2 = queue.iterator(); iterator2.hasNext()||i < numElemBucket;) {
				KeyValue keyValue = null;
				if(iterator2.hasNext())
					keyValue = (KeyValue) iterator2.next();
				if(keyValue != null){
					outputDump.add(keyValue.getKey()+" "+keyValue.getValue());					
				}
				else
					outputDump.add(0+" "+0);
				i++;
			}
		}

        try {
			writeTextFile(outputDump, dumpFileName);
		} catch (IOException e) {
			System.out.println("Error Writing Dump File");
			e.printStackTrace();
		} catch(OutOfMemoryError e){
			System.out.println("Too many entries");
		}
	}

	private void writeTextFile(List<String> outputDump, String aFileName) throws IOException {
	    FileWriter writer = new FileWriter(aFileName);
	    for(String str: outputDump) {
	      writer.write(str+"\n");
	    }
	    writer.close();
	  }

	private List<String> readTextFile(String aFileName) throws IOException {
		Scanner s = new Scanner(new File(aFileName));
		ArrayList<String> list = new ArrayList<String>();
		while (s.hasNext()){
		    list.add(s.next());
		}
		s.close();
		return list;
	}


	/**
	 *
	 * @author neha
	 * Class that creates hash function depending upon the given
	 * table size and multiplier
	 */
	private static final class HashFunction {
		// Multiplier to calculate hash
		private final int multiplier;

		//Number of buckets in the table
		private final long sizeTable;

		public HashFunction(int multiplier, long sizeTable) {
			this.multiplier = multiplier;
			this.sizeTable = sizeTable;
		}

		//Method to calculate hash depending upon different multipliers
		public int hash(int key) {
			long s = (long) ((multiplier * key) % (Math.pow(2, 32)));
			s =  (s & (long)(Math.pow(2, 32)-1));//Taking LSBs of the 64 bit number
			int log = 0;
			if(sizeTable > Integer.MAX_VALUE){
				System.out.println("Size of the table too big");
			}else{
				log = log2((int)sizeTable);
			}
			//Number of bits required in the result
			//32 is chosen because word size for integer is maximum 32
			int w_r = 32 - log;
			return (int) (s >>> w_r) ;//Getting required bits of the integer to hash into the buckets
		}

		//Method to find log of the size to determine number of bits to be used in the result
		public static int log2(int v){
			if(v <= 0 || v == 1 ){
				return 0;
			}
			final int b[] = {0x2, 0xC, 0xF0, 0xFF00, 0xFFFF0000};
			final int S[] = {1, 2, 4, 8, 16};
			int i;

			int r = 0; // result
			for (i = 4; i >= 0; i--){
				if ((v & b[i]) != 0){
					v >>= S[i];
					r |= S[i];
				}
			}
			return r;
		}


		@Override
		public String toString() {
			return multiplier+" ";
		}
	}

	/**
	 *
	 * @author neha
	 * Data structure to be used for Hash Table
	 */
	private static final class DataArray {
		private List<Queue<KeyValue>> hashTable;
		private int numElemBucket;
		public DataArray(int numBuckets, int numElemBucket) {
			hashTable = new ArrayList<Queue<KeyValue>>(numBuckets);
			for (int i = 0; i < numBuckets; i++) {
				hashTable.add(i, new LinkedList<KeyValue>());
			}
			this.numElemBucket = numElemBucket;
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
			int value = 0;

			Iterator<KeyValue> iterator2 = hashTable.get(index).iterator();
			KeyValue keyValue = (iterator2.hasNext())? iterator2.next(): null;

			for (;  keyValue!=null && keyValue.getKey()!=key && iterator2.hasNext();) {
				 keyValue = iterator2.next();
			}

			value =(keyValue!=null && keyValue.getKey() == key)? keyValue.getValue(): 0;
			return value;
		}
	}
}