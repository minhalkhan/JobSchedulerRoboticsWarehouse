package filehandling;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import types.Item;
import types.Job;
import types.Task;

/**
 * @author MinhalKhan
 */
public class TrainingJobs {
	/*
	 * Ck = 0.5 since we have two values {0,1} giving 1/n = 0.5
	 */
	private final double Ck = (double) 1/2;
	/**
	 * 0.0 Factor = 0.001
	 */
	private final double ZeroFactor = (double) 1/10000;
	/**
	 * Those cancelled will be stored in this table
	 * Hash-map mapping to its Tasks
	 * 10100 -> Task Id -> Quantity
	 */
	public HashMap<String, HashMap<String, Integer>> cancelledTable = new HashMap<>();
	/*
	 * Size of table cancelledTable
	 */
	public int numOfCancelled = 0;
	/**
	 * Those not cancelled will be stored here
	 */
	public HashMap<String, HashMap<String, Integer>> notCancelledTable = new HashMap<>();
	/*
	 * Size of table cancelledTable
	 */
	public int numOfNotCancelled = 0;
	/*
	 * Creates probabilities for each item
	 */
	public HashMap<String, ItemProbability> itemProbabilities = new HashMap<>();
	
	public TrainingJobs() throws IOException {
		//Creates tables with separate values
		createTables();
		//Run probabilities for each item
		runProbabilities(new ItemTable().itemTable);
	}
	/*
	 * P(1 | "Job ID")
	 * Returns 1 (cancel) or 0 (not cancelled)
	 */
	public int runNaiveBayes(ArrayList<Task> job){
		double ZeroProb = (double) Ck;
		double OneProb = (double) Ck;
		//Runs through each task, and gets P(crntTask | {0,1}) then multiplies it by One/ZeroProb to give the final probability
		for (Iterator<Task> iterator = job.iterator(); iterator.hasNext();) {
			Task crntT = iterator.next();
			ItemProbability crnProb = itemProbabilities.get(crntT.getItemId());
			ZeroProb = (double) (ZeroProb * (double) crnProb.ZERO); 
			OneProb = (double) (OneProb * (double) crnProb.ONE); 
		}
		//Argmax Function
		//If the probability of it being cancelled > probability of it being not cancelled 
		//then return 1
		if (Double.compare(OneProb, ZeroProb) > 0) {
			//Zero > 1
			return 1;
		}
		//else return 0 not cancelled
		return 0;
	}
	/**
	 * Used to train and create a probability table
	 * @param itemTable
	 */
	private void runProbabilities(HashMap<String, Item>  itemTable){
		
		Iterator<Entry<String, Item>> it = itemTable.entrySet().iterator();
		
		while(it.hasNext()){
			//Run probabilities for each item
			String crntId = it.next().getKey();
			double ZERO = ((double) runNotCancelTable(crntId)/numOfNotCancelled);
			double ONE = ((double) runCancelTable(crntId)/numOfCancelled);
			//Checking if P(..) = 0.0  if so set as constant zero factor of 0.0001
			if (Double.compare(ZERO, Double.MIN_VALUE) < 0) {
				ZERO = ZeroFactor;
			}
						
			if (Double.compare(ONE, Double.MIN_VALUE) < 0) {
				ONE = ZeroFactor;
			}
			
			this.itemProbabilities.put(crntId, new ItemProbability(ONE, ZERO));
		}
		
	}
	/**
	 * Calculates P(x1, x2 ... xn | 1)
	 */
	private int runCancelTable(String ItemId) {
		int sum = 0;
		for (String crntJobID: cancelledTable.keySet()){
			HashMap<String, Integer> mapOfTasks = cancelledTable.get(crntJobID);
			
			if (mapOfTasks.containsKey(ItemId)) {
				sum += mapOfTasks.get(ItemId);
			}
		}
		
		return sum;
	}
	/**
	 * Calculates P(x1, x2 ... xn | 0)
	 * @return sum of items including quantities
	 */
	private int runNotCancelTable(String ItemId) {
		int sum = 0;
		for (String crntJobID: notCancelledTable.keySet()){
			HashMap<String, Integer> mapOfTasks = notCancelledTable.get(crntJobID);
			
			if (mapOfTasks.containsKey(ItemId)) {
				sum += mapOfTasks.get(ItemId);
			}
		}
		return sum;
	}
	
	/**
	 * Calculate success rate - change job tables file to FileHandling.TRAINING_NAME not JOBS_FILE_NAME or we get 0.0 
	 * @return
	 */
	public double successRate() {
		int sum = 0;
		int valid = 0;
		
		HashMap<String, Job> jt = new JobTable().getJobTable();
		
		for (Iterator<Entry<String, Job>> iterator = jt.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Job> crntT = iterator.next();
			sum++;
			int outcome = runNaiveBayes(crntT.getValue().getItemList());
			
			if ((outcome == 1 && this.cancelledTable.containsKey(crntT.getKey()))){
				valid++;
			}else if ((outcome == 0 && this.notCancelledTable.containsKey(crntT.getKey()))) {
				valid++;
			}
			
		}

		return ((double)valid/sum)*100;
	}
	
	
	/**
	 * Creating table based on training_jobs.csv file and cancellations.csv
	 * @throws IOException - Such as FileNotFoundExceptions
	 */
	private void createTables() throws IOException{
		
		BufferedReader trainingSet = new BufferedReader(FileHandling.getFileReader(FileHandling.TRAINING_NAME));
		BufferedReader cancellation = new BufferedReader(FileHandling.getFileReader(FileHandling.CANCELLATION_NAME));
		//each line are corresponding
		String jobLine = ""; 
		String cancelLine = "";
		//Loop through each line
		while ((jobLine = trainingSet.readLine()) != null && (cancelLine = cancellation.readLine()) != null) {
			String[] line = jobLine.split(FileHandling.cvsSplitBy);
			String[] canc = cancelLine.split(FileHandling.cvsSplitBy);
			String key = line[0]; //same key for both training and cancellation
			int cancelled = Integer.parseInt(canc[1]);
			
			HashMap<String, Integer> tasks = new HashMap<>();
			for(int i=1;i<line.length-1;i++){
				String itemId = line[i];
				int quantity = Integer.parseInt(line[++i]);
				if (cancelled == 1) {
					numOfCancelled += quantity;
				}else{
					numOfNotCancelled += quantity;
				}
				tasks.put(itemId, quantity);
			}
			//Adding jobs into those cancelled and those not 
			if (cancelled == 1) {
				//cancelled
				cancelledTable.put(key, tasks);	
			}else {
				notCancelledTable.put(key, tasks);
			}
		}
		
	}
	

}
