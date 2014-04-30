#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <xmmintrin.h>
#include <emmintrin.h>
#include <pmmintrin.h>
#include <tmmintrin.h>
#include <smmintrin.h>
#include <nmmintrin.h>


int probe(int probing, int splash_table[][8], int hash[],int rows);



// Reads the dumpfile to get the values of B,S,N,h to probe the probe file and find the corresponding payloads.
// B - Bucket Size
// S - Size
// N - no. of successful insertions
// h - No. of hash functions

int main(int argc, char *argv[])
{
    
    FILE *fp, *fp1, *fp2;
    int b,s,n,h;
    int rows;
    int pay_load;
    int probing[1000];
    
    int key, value,probes;
    int i =0, j=0,k=0,count=0;
    int hash[4];
    
    // Reading the dump file
    
    fp = fopen(argv[1], "r");
    if(!fp)
    {
        printf("\nFile not found\n\n");
        exit(0);
    }
    
    // getting the Input file
    fp1=stdin;
    if(fp1)
    {
    // Reading each line of input file to get the value of probe keys and store them in an array
        
        while(fscanf(fp1,"%d",&probes)!=EOF)
        {
            probing[count]=probes;
            count++;
        }
    }
    
    
    if(fp)
    {
        // Reading the B, S, h and N
        fscanf(fp, "%d%d%d%d", &b,&s,&h,&n);
        // Getting the no. of buckets
        rows = (pow(2,s))/b;
        // Reading the hash functions
        fscanf(fp, "%d%d", &hash[0], &hash[2]);
        int key_pay[rows][8];
        while(fscanf(fp,"%d%d", &key, &value)!=EOF)
		{
            // Reading the key and pay load pair for each slot
            k++;
            if(k>(i+1)*b)
			{
                i++;
                j=0;
			}
            key_pay[i][j]=key;
            key_pay[i][j+4]=value;
            j++;
		}
        
        
        fp2=fopen("result.txt","w");
        fclose(fp2);
        for(i=0;i<count;i++)
        {
            
            // Calling the probe function for each probe key
            pay_load = probe(probing[i], key_pay,hash, rows);
            if(pay_load!=0)
            {
                
                // If the probe key is matched to the splash table, its stored along with its payload to the result file
                printf("%d %d \n" , (int)probing[i], pay_load);
                
                
            }
        }
        fclose(fp);
        fclose(fp1);
        
        
        
    }
    
    
    
    return 1;
    
    
}

// A function to take input the probe key, Splash table, hash functions and the size of the table as the input
// to probe the splash table using SIMD instructions and get the corresponding payload of the key.



int probe(int probing, int splash_table[][8],int hash[], int rows)
{
    int a,b,res,shifting;
    int table_size[4];
    int hash1,hash2,load1,load2;
    int mult[4];
    int slot1,slot2;
    __m128i key,slot,Z,Cmp1,Cmp2,Bucket2_key,Bucket2_val,Bucket1_val,And1,And2,Bucket1_key,Or,or_Across,hashing;
    shifting = (32 - (int)log2(rows));
    
    // Loading the key and making its 3 copy
    key=_mm_set1_epi32(probing);
    
    // Loading the hash multipliers
    hashing=_mm_setr_epi32(hash[0],hash[1],hash[2],hash[3]);
    
    //Multiplying the each copy with the hash multiplier to store the lower bits which is equivalent to mod with pow(2,32)
    Z = _mm_mullo_epi32(key,hashing);
    
    // Right Shifting the multiplied value with 32- log2(rows) so as to get the slots.
    slot=_mm_srli_epi32(Z , shifting);
    
    // Extracting the two slots received and retrieving corresponding keys and payloads
    hash1=(int)(_mm_extract_epi32(slot,0));
    hash2=(int)(_mm_extract_epi32(slot,2));
    Bucket1_key = _mm_setr_epi32(splash_table[hash1][0],splash_table[hash1][1], splash_table[hash1][2], splash_table[hash1][4]);
    Bucket1_val = _mm_setr_epi32(splash_table[hash1][4],splash_table[hash1][5],splash_table[hash1][6],splash_table[hash1][7]);
    Bucket2_key = _mm_setr_epi32(splash_table[hash2][0],splash_table[hash2][1],splash_table[hash2][2],splash_table[hash2][3]);
    Bucket2_val = _mm_setr_epi32(splash_table[hash2][4],splash_table[hash2][5],splash_table[hash2][6],splash_table[hash2][7]);
    
    // Comparing it with the keys of those buckets and then taking And with the payloads
    Cmp1 = _mm_cmpeq_epi32(Bucket1_key,key);
    And1 = _mm_and_si128(Cmp1, Bucket1_val);
    Cmp2 = _mm_cmpeq_epi32(Bucket2_key,key);
    And2 = _mm_and_si128(Cmp2, Bucket2_val);
    
    // Taking or of both the buckets value of And
    Or = _mm_or_si128(And1, And2);
    
    //Doing Or-Across the _m128i vector
    or_Across = _mm_hadd_epi32(Or,Or);
    or_Across = _mm_hadd_epi32(or_Across,or_Across);
    a = _mm_extract_epi32(or_Across,0);
    
    // Returning the payload
    return (int)a;

}

