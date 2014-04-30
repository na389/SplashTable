#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <xmmintrin.h>
#include <emmintrin.h>
#include <pmmintrin.h>
#include <tmmintrin.h>
#include <smmintrin.h>
#include <nmmintrin.h>


int probe(float probing, float splash_table[][8], float hash[],int rows);


float vectorGetByIndex(__m128 V, unsigned int i)
{
    // Function to get the particular index value from a _m128 variable
    union {
        __m128 v;
        float a[4];
    } U;
    
    U.v = V;
    return U.a[i];
}



int main(int argc, char *argv[])
{
    
    FILE *fp, *fp1, *fp2;
    int b,s,n,h;
    int rows;
    int pay_load;
    float probing[50];
    float key_pay[40][8];
    float key, value,probes;
    int i =0, j=0,k=0,count=0;
    float hash[4];
    char *t;
    char f;
    
    // Reading the dump file
    fp = fopen(argv[1], "r");
    
    // getting the Input file
    fp1=stdin;
    if(fp1)
    {
    // Reading each line of input file to get the value of probe keys and store them in an array
        
        while(fscanf(fp1,"%f",&probes)!=EOF)
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
        fscanf(fp, "%f%f", &hash[0], &hash[2]);
        while(fscanf(fp,"%f%f", &key, &value)!=EOF)
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
    return 1;
    
    
}

int probe(float probing, float splash_table[][8],float hash[], int rows)
{
    float a,b,res,shifting;
    float table_size[4];
    int hash1,hash2,load1,load2;
    float mult[4];
    int slot1,slot2;
    __m128 key,hashing,slot,Z,try,table,hash_val,Cmp1,Cmp2,Bucket2_key,Bucket2_val,Bucket1_val,And1,And2,Bucket1_key,Or,S1,S2;
    shifting = 2 *(32 - log2(rows));
    table_size[0]=table_size[2]= shifting;
    // Loading the key and making its 3 copy
    
    
    key=_mm_load_ps1(&probing);
    // Loading the hash multipliers
    hashing=_mm_load_ps(&hash[0]);
    //Multiplying the each copy with the hash multiplier
    Z = _mm_mul_ps(key,hashing);
    // Extracting the first and third element to take mod with pow(2,32)
    a = _mm_extract_ps(Z,0);
    b= _mm_extract_ps(Z,2);
    long int power = pow(2,32);
    load1 = (int) (a) % power;
    load2 = (int)(b) % power;
    load1 = load1 & (power-1);
    load2 = load2 & (power-1);
    mult[0] = (float) load1;
    mult[2] = (float)load2;
    // Loading the new hash value and right shifting it by (32 - log2(size of table)
    hash_val = _mm_load_ps(&mult[0]);
    table= _mm_load_ps(&table_size[0]);
    
    slot=_mm_div_ps(hash_val , table);
    // Extracting the two slots received and retrieving corresponding keys and payloads
    hash1=(int)(vectorGetByIndex(slot,0))%rows;
    hash2=(int)(vectorGetByIndex(slot,2))%rows;
    Bucket1_key = _mm_load_ps(&splash_table[hash1][0]);
    Bucket1_val = _mm_load_ps(&splash_table[hash1][4]);
    Bucket2_key = _mm_load_ps(&splash_table[hash2][0]);
    Bucket2_val = _mm_load_ps(&splash_table[hash2][4]);
    // Comparing it with the keys of those buckets and then taking And with the payloads
    Cmp1 = _mm_cmpeq_ps(Bucket1_key,key);
    And1 = _mm_and_ps(Cmp1, Bucket1_val);
    Cmp2 = _mm_cmpeq_ps(Bucket2_key,key);
    And2 = _mm_and_ps(Cmp2, Bucket2_val);
    // Taking or of both the buckets value of And
    Or = _mm_or_ps(And1, And2);
    // Taking or-across the 4 32 bit values to find the payload
    a = vectorGetByIndex(Or,0);
    S1 = _mm_load_ps1(&a);
    a = vectorGetByIndex(Or,1);
    S2 = _mm_load_ps1(&a);
    S1 = _mm_or_ps(S1, S2);
    a = vectorGetByIndex(Or,2);
    S2 = _mm_load_ps1(&a);
    S1 = _mm_or_ps(S1, S2);
    a = vectorGetByIndex(Or,3);
    S2 = _mm_load_ps1(&a);
    S1 = _mm_or_ps(S1,S2);
    a = vectorGetByIndex(S1,0);
    // Returning the payload
    return (int)a;

}

