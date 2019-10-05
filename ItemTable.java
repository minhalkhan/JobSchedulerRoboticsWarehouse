package filehandling;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import types.Item;

/**
 * @author Minhal - Job Selection
 */
public class ItemTable { 
	
	/*
	 * Stores all items into a hash map, where the key is item ID which maps to its item class
	 */
	public HashMap<String, Item> itemTable;

	public ItemTable() throws IOException{
		this.itemTable = createTable();
	}
	/*
	 * Get item with id 'a'-'z'
	 */
	public Item getItem(String id){
		return itemTable.get(id);
	}
	/*
	 * Creates table based on CSV files
	 */
	public static HashMap<String, Item> createTable() throws IOException{
		HashMap<String, Item> itemTable = new HashMap<>();
		//Opening files to read
		BufferedReader items = new BufferedReader(FileHandling.getFileReader(FileHandling.ITEM_FILE_NAME));
		BufferedReader itemLocation = new BufferedReader(FileHandling.getFileReader(FileHandling.ITEM_LOCATION_FILE_NAME));
		//Each line in both files corresponds to the same items
		//current lines we are looping
		String itemLine = ""; 
		String crntItemLocation = "";
		//Loop through each line
		while ((itemLine = items.readLine()) != null && (crntItemLocation = itemLocation.readLine()) != null) {
			//[item name, reward, weight]
            String[] itms = itemLine.split(FileHandling.cvsSplitBy);
			//[x, y, item name]
            String[] itmsLoc = crntItemLocation.split(FileHandling.cvsSplitBy);
            //Assigning values 
            String key = itms[0];
            int x = Integer.parseInt(itmsLoc[0]);
            int y = Integer.parseInt(itmsLoc[1]);
            float reward = Float.parseFloat(itms[1]);
            float weight = Float.parseFloat(itms[2]);
            //Adding new item to table
            itemTable.put(key, new Item(x, y, reward, weight));
		}
		return itemTable;
	}
	


}
