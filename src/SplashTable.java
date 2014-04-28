import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	// Number of entries in the table
	private int numElementsLog;

	// Number of entries in bucket
	private int numElemBucket;

	// Number of buckets
	private int numBuckets;

	// Array for storing hashes for different keys
	private HashFunction[] hashFunctions;

	// Number of hash functions
	private int numHashFunctions;

	// Number of elements in table
	private int numElements;

	//Data Structure to be used as hash table
	private DataArray dataArray;

	//Number of reinsetions allowed
	private final int numReinsertions;

	//Total number of successful insertions done
	private int numInsertions;

	//Character Encoding to be used while writing to a file
	final static Charset ENCODING = StandardCharsets.UTF_8;
	
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
		Random r = new Random();
		for (int i = 0; i < this.numHashFunctions; i++) {
			hashFunctions[i] = new SplashTable.HashFunction(r.nextDouble(),
					numBuckets);
		}
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
           reInsert(++tries, data, i);         
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
    	System.out.println("tries :"+tries);
    	final int hash = (int)hashFunctions[i-1].hash(data.getKey()); 
    	// If number of tries exceeds number of time re-insertion value entered by the user don't try anymore           
    	if (tries > numReinsertions) {
    		System.out.println("Re-insertions exceeded");
    		dump(dumpFileName);
    		System.exit(0);    		
    		return;
    	} else {                
    		KeyValue elementToReinsert = this.dataArray.tryReInsert( hash, data);    		
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
		int result = -1;
		for (int i = 0; i < this.numHashFunctions && result == -1; i++) {
			int index = (int)hashFunctions[i].hash(key);
			System.out.println("Probe: i= "+i+ " index:"+index);
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
			}catch(NumberFormatException | NullPointerException exception ){
				System.out.println("Unable to process the request :" +exception.getMessage());
				return;
			}
		}

		String inputFileName = args[4]; // Name of the input file containing key value pairs. filename
		String dumpFileName = args[5]; //Name of the dumpfile
		String probefile = args[6]; //Name of probefile
		String resultfile = args[7]; // Name for resultfile
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

		while (inFile.hasNext()) {
			key = inFile.nextInt();
			value = inFile.nextInt();
			splashTable.build(0, new KeyValue(key, value));
		}		
		splashTable.dump(dumpFileName);
		inFile.close();

		
		List<String> probeKeys = new ArrayList<>();
		 try {
				probeKeys = splashTable.readTextFile(probefile);
			} catch (NumberFormatException | NullPointerException| IOException e) {
				System.out.println("Error Reading Probe File");
				e.printStackTrace();
			}
		List<String> resultFileData = new ArrayList<>();
		int valueOutput = -1;
		for(String keyInput: probeKeys){
			valueOutput = splashTable.probe(Integer.parseInt(keyInput.trim()));			
			switch (valueOutput){
				case -1:{
					System.out.println("Result not found for:"+keyInput);
					break;
				}
				default: {
					System.out.println("Result:"+valueOutput);
					resultFileData.add(keyInput+ " "+valueOutput);
					break;
				}
			}
		}
		try {
			splashTable.writeTextFile(resultFileData, resultfile);
		} catch (IOException e) {
			System.out.println("Error Writing Result File");
			e.printStackTrace();
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
		ArrayList<String> outputDump = new ArrayList<>();
		outputDump.add(""+numElemBucket+  " "+numElementsLog+ " "+numHashFunctions+ " "+numInsertions );
		StringBuilder str = new StringBuilder();
		for(HashFunction function : hashFunctions){
			str.append(function.toString());
		}
		outputDump.add(str.toString());
        List<Queue<KeyValue>> list = dataArray.hashTable;
        for (Iterator<Queue<KeyValue>> iterator = list.iterator(); iterator.hasNext();) {
			Queue<KeyValue> queue = (Queue<KeyValue>) iterator.next();
			System.out.println("Queue");
			int i = 0;
			for (Iterator<KeyValue> iterator2 = queue.iterator(); iterator2.hasNext()||i < numElemBucket;) {
				KeyValue keyValue = null;
				if(iterator2.hasNext())
					keyValue = (KeyValue) iterator2.next();
				if(keyValue != null){
					outputDump.add(keyValue.getKey()+" "+keyValue.getValue());
					System.out.println("key values are : "+keyValue.getKey()+ " : "+keyValue.getValue());
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
		}
	}

	private void writeTextFile(List<String> outputDump, String aFileName) throws IOException {
	    Path path = Paths.get(aFileName);
	    Files.write(path, outputDump, ENCODING);
	  }
	
	 private List<String> readTextFile(String aFileName) throws IOException {
		    Path path = Paths.get(aFileName);
		    return Files.readAllLines(path, ENCODING);
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

		public int hash(int key) {
			int s = (int) (multiplier * Math.pow(2, 32));						
			int n = s << -32 >>> -32;	 // Extract the Least Significant Bits of the closest integer	
			int w_r = 32 - (int)(Math.log10(sizeTable)/Math.log10(2));			
			return (n >> w_r) ; //Return the required MSBs of the number depending upon the number of slots
		}

		@Override
		public String toString() {
			return multiplier+" ";
		}
	}

	/**
	 *
	 * @author neha Data structure to be used for Hash Table
	 */
	private static final class DataArray {
		private List<Queue<KeyValue>> hashTable;
		private int numElemBucket;		
		public DataArray(int numBuckets, int numElemBucket) {
			hashTable = new ArrayList<Queue<KeyValue>>(numBuckets);			
			for (int i = 0; i < numBuckets; i++) {
				hashTable.add(i, new LinkedList<KeyValue>());
			}
			//initializeBuckets();
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
			System.out.println("element:" + data.getKey() + " hash index:" + index+ "inserted : "+inserted);
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
			
			Iterator<KeyValue> iterator2 = hashTable.get(index).iterator();
			KeyValue keyValue = iterator2.next();
			for (;  keyValue.getKey()!=key && iterator2.hasNext();) {
				 keyValue = iterator2.next();
				System.out.println("Findkey:"+keyValue);								
			}
			value = keyValue.getKey() == key ? keyValue.getValue() : -1;
			return value;
		}
	}
}