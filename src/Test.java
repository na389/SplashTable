import java.util.Random;

public class Test {
	public static void main(String[] args) {
		System.out.println(hash(123));
		//System.out.println(Math.log10(1024)/Math.log10(2));
		//System.out.println((123 * Math.random() % 1) & (Math.pow(2, 32) - 1));
		//System.out.println(getRandom());
	}
	
	public static int hash(int key) {
		long s = (long) ((getRandom() * key) % (Math.pow(2, 32)));		
		System.out.println("S>>"+s);
		s =  (s & (long)(Math.pow(2, 32)-1));
		System.out.println("S:"+s);
		int w_r = 32 - (int)(Math.log10(16)/Math.log10(2));				
		return (int) (s >>> w_r) ;
	}
	
	public static int getRandom(){
		Random rn = new Random();
	    int randomNum = 0;	
	    while(true){
	    	randomNum = rn.nextInt(Integer.MAX_VALUE);
	    	if((randomNum & 1) == 1 ){
	    		break;
	    	}
	    }
	    return randomNum;
	}
	/**
	 * 		long mkmod232 = (long) ((this.hashMultipliers[i] * key) % Math.pow(2, 32));
			positions[i] = (int) (mkmod232 >> (int) (32 -(Math.log(this.numberOfBuckets) / Math.log(2))));
			System.out.println("Position = " + positions[i]);
	 */
	
}