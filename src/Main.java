import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;


public class Main {

	public static void main(String[] args) throws IOException{
		
		Map<String,Double> idfMap = new HashMap<String, Double>();
		Map<String, Integer> wordMap = new HashMap<String, Integer>();	//词和序号的map
		ArrayList<String> wordArrayList = new ArrayList<String>();
		ArrayList<Integer> numPostPerTheme = new ArrayList<Integer>();
		Map<Integer, Integer> postToThemeMap = new HashMap<Integer, Integer>();	//tfidf矩阵的行对应的主题
		ArrayList<String> postArrayList = new ArrayList<String>();
		
		
		int wordMapIndex=0;		//词的索引结构，最后得到的是词数
		int countPost=0;
		int countTheme=10;
		//int postIndex = 0;
		String str = "啊测试分词工具一些停止词";
		String directory = "data\\";
		String basketball=directory+"Basketball.txt";
		String computer=directory+"D_Computer.txt";
		String fleaMarket = directory+"FleaMarket.txt";
		String girls = directory + "Girls.txt";
		String jobExpress = directory+"JobExpress.txt";
		String mobile = directory + "Mobile.txt";
		String stock = directory + "Stock.txt";
		String suggestion = directory+"V_Suggestions.txt";
		String warAndPeace = directory+"WarAndPeace.txt";
		String WorldFootball = directory + "WorldFootball.txt";
		
		String[] post = {basketball,computer,fleaMarket,girls,jobExpress,mobile,stock,suggestion,
				warAndPeace,WorldFootball};
        
		
		for(int i=0;i<post.length;i++){			//得到一个词-序号的map
			File file = new File(post[i]);
			Scanner input = new Scanner(file);
			int postPerTheme=0;
	        while(input.hasNext()){
	        	postPerTheme++;
	        	postToThemeMap.put(countPost, i);
	        	countPost++;
	        	str = input.nextLine();
	        	postArrayList.add(str);
	        	StringReader reader = new StringReader(str);
	        	IKSegmenter ik = new IKSegmenter(reader,true);
	        	
	        	Lexeme lexeme = null;
	        	while((lexeme = ik.next())!=null){
	        		String word = lexeme.getLexemeText();
	        		
	        		if(!wordMap.containsKey(word)){
	        			wordMap.put(word, wordMapIndex);
	        			
	        			wordArrayList.add(word);
	        			wordMapIndex++;
	        		}
	        	}
	        }
	        numPostPerTheme.add(postPerTheme);
	        input.close();
		}
		
		double[][] tfidfMatrix = new double[countPost][wordMapIndex];
		for(int i = 0;i<countPost;i++)
			for(int j = 0;j<wordMapIndex;j++)
				tfidfMatrix[i][j] = 0;
		
		for(int i = 0;i<postArrayList.size();i++){		//得到一个词频数的矩阵。
			String string = postArrayList.get(i);
			StringReader reader = new StringReader(string);
			IKSegmenter ik = new IKSegmenter(reader, true);
			Lexeme lx = null;
			while((lx = ik.next())!=null){
				String word = lx.getLexemeText();
				int column = wordMap.get(word).intValue();
				tfidfMatrix[i][column] = tfidfMatrix[i][column]+1;
			}
			
		}
		
		for(int i=0;i<countPost;i++){
			double sum = 0;
			for(int j=0;j<wordMapIndex;j++){
				sum+=tfidfMatrix[i][j];
			}
			for(int j = 0;j<wordMapIndex;j++){
				tfidfMatrix[i][j] = tfidfMatrix[i][j]/sum;
			}
		}
		
		
		for(int j=0;j<wordMapIndex;j++){			//得到每个词在多少个帖子中出现过，以用来计算idf的值。
			String word = wordArrayList.get(j);
			if(!idfMap.containsKey(word)){
				idfMap.put(word, 0.0);
			}
			double sum = 0;
			for(int i=0;i<countPost;i++){
				if(tfidfMatrix[i][j]>0)
					sum = sum+1;
			}
			idfMap.put(word, sum);
		}
		
		
		
		Set<String> set = idfMap.keySet();
		
		Iterator<String> iterator = set.iterator();
		while(iterator.hasNext()){		//计算每个词的idf值
			String word = iterator.next();
			
			double d = idfMap.get(word).doubleValue();
			d=Math.log((countPost)/(1+d));
			
			idfMap.put(word, d);
		}
		
		for(int i=0;i<tfidfMatrix.length;i++){
			for(int j=0;j<wordMapIndex;j++){
				double idf = idfMap.get(wordArrayList.get(j));
				tfidfMatrix[i][j] = tfidfMatrix[i][j]*idf;
			}
		}
		
		double[][] testPost = new double[200][wordMapIndex];
		double[][] trainPost = new double[countPost-200][wordMapIndex];
		Map<Integer, Integer> testPostToThemeMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> trainPostToThemeMap = new HashMap<Integer, Integer>();
		int flagRow=0;
		int indTest = 0;
		int indTrain = 0;
		for(int i=0;i<10;i++){		//把集合拆成两部分。
			int n = numPostPerTheme.get(i);
			
			for(int k = flagRow;k<(flagRow+20);k++){
				for(int j = 0;j<wordMapIndex;j++){
					//System.out.println(i+"  "+testPost.length+"  "+indTest);
					testPost[indTest][j] = tfidfMatrix[k][j];
					testPostToThemeMap.put(indTest, i);
					
				}
				indTest++;
			}
			for(int k=flagRow+20;k<(flagRow+n);k++){
				for(int j = 0;j<wordMapIndex;j++){
					trainPost[indTrain][j] = tfidfMatrix[k][j];
					trainPostToThemeMap.put(indTrain, i);
				}
				indTrain++;
			}
			flagRow = flagRow+n;
		}
		
		
		int[] kArr = {10,20,30,40,50};
		//brute-force
		for(int r=0;r<kArr.length;r++){
			long startTime = System.currentTimeMillis();
			ArrayList<Double> accuracyList = new ArrayList<Double>();
			for(int i=0;i<200;i++){
				KNN knn = new KNN();
				knn.setK(kArr[r]);
				int[] kTheme=knn.bruteForce(testPost[i], trainPost);
				int realTheme = testPostToThemeMap.get(i);
				double sum = 0;
				for(int k = 0;k<kTheme.length;k++){
					int predictTheme = trainPostToThemeMap.get(kTheme[k]);
					if(predictTheme==realTheme){
						sum+=1;
					}
				}
				double d=sum/kArr[r];
				accuracyList.add(d);
			}
			long endTime = System.currentTimeMillis();
			
			double accuracyRatio=0;
			for(int i=0;i<accuracyList.size();i++){
				accuracyRatio+=accuracyList.get(i);
			}
			accuracyRatio=accuracyRatio/accuracyList.size();
			
			double variance = 0;
			for(int i = 0;i<accuracyList.size();i++){
				variance += (accuracyList.get(i)-accuracyRatio)*(accuracyList.get(i)-accuracyRatio);
			}
			variance = variance/accuracyList.size();
			System.out.println("k="+kArr[r]+"时，bruteforce 准确率为:"+accuracyRatio+",方差为："+variance+"运行时间为:"
					+(endTime-startTime)+"ms");
		
		}
		
		
		//local-sensitivity hashing
		for(int r = 0;r<kArr.length;r++){
			long startTime = System.currentTimeMillis();
			KNN lshKnn = new KNN();
			lshKnn.setK(kArr[r]);
			lshKnn.generateVector(wordMapIndex);
			lshKnn.LSH(trainPost);
			
			ArrayList<Double> lshAccuracyList = new ArrayList<Double>();
			for(int i=0;i<200;i++){
				int[] kTheme = lshKnn.LSHClassify(testPost[i], trainPost);
				int realTheme = testPostToThemeMap.get(i);
				double sum=0;
				for(int k=0;k<kTheme.length;k++){
					int predictTheme = trainPostToThemeMap.get(kTheme[k]);
					if(predictTheme==realTheme){
						sum=sum+1;
					}
				}
				double d=sum/kArr[r];
				lshAccuracyList.add(d);
			}
			long endTime = System.currentTimeMillis();
			double lshAccuracyRatio = 0;
			for(int i=0;i<lshAccuracyList.size();i++){
				lshAccuracyRatio+=lshAccuracyList.get(i);
			}
			lshAccuracyRatio=lshAccuracyRatio/lshAccuracyList.size();
			
			double variance=0;
			for(int i=0;i<lshAccuracyList.size();i++){
				variance+=(lshAccuracyList.get(i)-lshAccuracyRatio)*(lshAccuracyList.get(i)-lshAccuracyRatio);
			}
			variance=variance/lshAccuracyList.size();
			System.out.println("k="+kArr[r]+"时,lsh 准确率为:"+lshAccuracyRatio+",方差为："+variance+"运行时间为:"
					+(endTime-startTime)+"ms");
		}
	}
	
}




