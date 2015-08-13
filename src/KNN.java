import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class KNN {
	
	int k;
	int b;
	double[][] normalVector;
	HashMap<Integer,ArrayList<Integer>> bucketMap = new HashMap<Integer,ArrayList<Integer>>();
	
	
	public KNN(){
		b = 2;
	}
	
	
	public void setK(int k){
		this.k = k;
	}
	
	public void generateVector(int wordCount){
		normalVector = new double[b][wordCount];
		for(int i = 0;i<b;i++){
			for(int j = 0;j<wordCount;j++){
				normalVector[i][j] = -1+Math.random()*2;
			}
		}
	}
	public int[] bruteForce(double[] testPost, double[][] trainPost){	
		int[] ret = new int[k];
		Map<Double, Integer> dis = new HashMap<Double, Integer>();
		double[] sortArr = new double[trainPost.length];
		
		for(int i=0;i<trainPost.length;i++){
			double d = innerProduct(testPost, trainPost[i]);
			double testD = vectorLength(testPost);
			double trainD = vectorLength(trainPost[i]);
			d = d/(testD*trainD);
			dis.put(d, i);
			sortArr[i]=d;
		}
		
		for(int i = 0;i<sortArr.length;i++){
			int max=i;
			for(int j = i+1;j<sortArr.length;j++){
				if(sortArr[max]<sortArr[j])
					max=j;
			}
			if(max!=i){
				double temp = sortArr[i];
				sortArr[i] = sortArr[max];
				sortArr[max] = temp;
			}
		}
		for(int i = 0;i<k;i++){
			ret[i] = dis.get(sortArr[i]);
		}
		return ret;
	}
	
	public double innerProduct(double[] v1,double[] v2){	//内积
		double product=0;
		for(int i=0;i<v1.length;i++){
			product += v1[i]*v2[i];
		}
		return product;
	}
	
	public double vectorLength(double[] v1){		//向量模长
		double d = 0;
		for(int i = 0;i<v1.length;i++)
			d+=v1[i]*v1[i];
		return Math.sqrt(d);
	}
	
	
	
	public void LSH(double[][] trainPost){		//作散列，把训练集分散到桶中
		for(int i=0;i<trainPost.length;i++){
			int bit=0;
			for(int x=0;x<b;x++){
				bit = bit<<1;
				//System.out.println("在判断之前:"+b+" : "+bit);
				if(innerProduct(trainPost[i], normalVector[x])>0){
					bit+=1;
				}
				//System.out.println(i+" : "+b+" : "+bit);
			}
			if(!bucketMap.containsKey(bit)){
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(i);
				bucketMap.put(bit, list);
			}
			else {
				ArrayList<Integer> list = bucketMap.get(bit);
				list.add(i);
				bucketMap.put(bit, list);
			}
		}
	}
	
	public int[] LSHClassify(double[] testPost,double[][] trainPost){	//在桶内暴力搜索。
		int[] ret = new int[k];
		int bit=0;
		for(int x=0;x<b;x++){
			bit=bit<<1;
			if(innerProduct(testPost, normalVector[x])>0){
				bit+=1;
			}
		}
		
		ArrayList<Integer> list = bucketMap.get(bit);
		
		Map<Double, Integer> dis = new HashMap<Double, Integer>();
		double[] sortArr = new double[list.size()];
		for(int i=0;i<list.size();i++){
			int row = list.get(i);
			double d = innerProduct(testPost, trainPost[row]);
			double vlProduct = vectorLength(testPost)*vectorLength(trainPost[row]);
			d=d/vlProduct;
			sortArr[i]=d;
			dis.put(d, i);
		}
		for(int i=0;i<sortArr.length;i++){
			int max=i;
			for(int j = i+1;j<sortArr.length;j++){
				if(sortArr[max]<sortArr[j])
					max=j;
			}
			if(max!=i){
				double temp = sortArr[i];
				sortArr[i] = sortArr[max];
				sortArr[max] = temp;
			}
		}
		for(int i=0;i<k;i++){
			ret[i] = list.get(dis.get(sortArr[i]));
		}
		
		
		return ret;
	}
	
	
}
