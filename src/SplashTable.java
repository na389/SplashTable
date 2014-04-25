import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

public class SplashTable {

	// Buckets storing key in the splash table
	//private KeyValue[][] bucketKey;

	// Buckets storing values for the key
	//private int[][] bucketValues;

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

	public SplashTable(int numElementsLog, int numElemBucket,
			int numHashFunctions, int numReinsertions) {
		this.numElementsLog = numElementsLog;
		this.numElemBucket = numElemBucket;
		this.numHashFunctions = numHashFunctions;
		this.numReinsertions = numReinsertions;
		this.numElements = (int) Math.pow(2, this.numElementsLog);
		this.numBuckets = (int) (numElements / numElemBucket);
		//this.bucketKey = new KeyValue[numBuckets][numElemBucket];
		//this.bucketValues = new int[numBuckets][numElemBucket];
		this.hashFunctions = new HashFunction[numHashFunctions];
		this.dataArray = new DataArray(this.numBuckets, this.numElemBucket);
	}

/*	private long hash(Integer key) {
		Random r = new Random();
		System.out.println(this.numBuckets + " " + this.numElementsLog + "  "
				+ this.numElemBucket);
		long random = (long) (this.numBuckets * (key * r.nextDouble() % 1));

		return random;
	}*/

	private void build(KeyValue data) {
		//TODO: Find solution for if else using ternary or something to use break and continue conditionally 
		int tries = 0;
		boolean opStatus = false;
		for (int i = 0; i < this.numHashFunctions; i++) {
			final long hash = hashFunctions[i].hash(data.getKey());
			opStatus = this.dataArray.insertElement((int) hash, data);
			if (opStatus){
				break; // Need to find a solution of this using ternary operator
			}else{
				tries ++;
				//If number of tries exceeds number of time re-insertion value entered by the user don't try anymore
				KeyValue elementToReinsert = this.dataArray.tryReInsert((int) hash, data);
				data = elementToReinsert;
				if(tries > numReinsertions){
					break;
				}else {
					continue;
				}
			}
		}
	}

	private void createHashFunctions() {
		Random r = new Random();
		for (int i = 0; i < this.numHashFunctions; i++) {
			hashFunctions[i] = new SplashTable.HashFunction(r.nextDouble(),
					numElemBucket);
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		int numElementsLog =2, numElementsBucket=2,numHashFunctions=1, numReinsertions=1;
		if(args.length < 5){
		System.out.println("Not sufficient Command line agruments!\nPlease try again. ");
		return;
		} else{
			
		
		try{		
		 numElementsLog = Integer.parseInt(args[0]);// Logarithm of total number of entries in hashTable i. e. size of the hashTable. S
		 numElementsBucket = Integer.parseInt(args[1]);// Number of elements in a single bucket. B should be power of 2
		 if((numElementsBucket < 0) || ((numElementsBucket & (numElementsBucket - 1)) != 0)){
			 System.out.println("Invalid Input for the Size of the Bucket. Should be power of 2.");
			 return;
		 }
		 numHashFunctions = Integer.parseInt(args[3]);// Number of hash functions to be used. h 
		 numReinsertions = Integer.parseInt(args[4]);// Number of re-insertions allowed. R
		}catch(NumberFormatException | NullPointerException exception ){
			System.out.println("Unable to process the request :" +exception.getMessage());
		return;	
		}
		}
		 
		String fileName = args[2]; // Name of the input file containing key value pairs. filename
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
			splashTable.build(new KeyValue(key, value));
		}
		inFile.close();

	}

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

	private static final class DataArray {
		private List<Queue<KeyValue>> hashTable;
		private int numElemBucket;
		//private int numBuckets;

		public DataArray(int numBuckets, int numElemBucket) {
			hashTable = new ArrayList<Queue<KeyValue>>(numBuckets);
			for (int i = 0; i < numBuckets; i++) {
				hashTable.add(i, new LinkedList<KeyValue>());
			}
			this.numElemBucket = numElemBucket;
			//this.numBuckets = numBuckets;
		}

		public boolean insertElement(int index, KeyValue data) {
			if(hashTable.size() > index){
			boolean inserted = (hashTable.get(index).size() > this.numElemBucket) ? hashTable
					.get(index).add(data) : false;
					System.out.println("Element inserted : "+inserted);
			return inserted;
			} else {
				return false;
			}
		}
		
		public KeyValue tryReInsert(int index, KeyValue data){
			//Its known that the corresponding bucket is full so move the data around
			KeyValue elementInsertedFirst = hashTable.get(index).poll();
			hashTable.get(index).add(data);
			return elementInsertedFirst;			
		}
	}
}