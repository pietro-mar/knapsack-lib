package com.mobiquity.packer;

import com.mobiquity.exception.APIException;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Packer {

  // Global lists for weights and values
  private static int maxWeight = 0; // Also make maxWeight global if needed

  private Packer() {
  }

  public static String pack(String filePath) throws APIException {
    StringBuilder result = new StringBuilder();
    try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
      String line;
      // Read each line and extract weights and values
      while ((line = br.readLine()) != null) {
        if (!line.trim().isEmpty()) {
          result.append(processLine(line)).append("\n");
        }
      }
      // Calculate maximum value using the knapsack function
    } catch (Exception e) {
      throw new APIException("Error reading file: " + e.getMessage());
    }
    return result.toString().trim();
  }

  private static String processLine(String line){
    List<Item> items;
    try{
      String[] parts = line.split(":");
      //some validations pack format.
      validateLinePackComposition(parts, line);
      //process items per pack
      items = processItemsPack(parts, line);
      //find the best combination of items per pack
      return findOptimalCombination(maxWeight, items);
    }catch (Exception e){
      //process will continue even if one line/item is not valid.
      //TODO put log to show message on what was the validation error
      return "-";
    }
  }

  private static void validateLinePackComposition(String[] parts, String line) throws APIException {
    //validate if line is in correct format.
    validateLineFormat(parts, line);
    //validate packMaxWeight.
    validatePackMaxWeight(parts, line);
    //validate item quantity on pack.
    validatePackItems(parts, line);
  }

  private static void validateLineFormat(String[] parts, String line) throws APIException {
    if (parts.length != 2) {
      throw new APIException("Invalid pack format on line: " + line);
    }
  }

  private static String getBufferPart(String[] parts, int index){
    return parts[index] != null ? parts[index].trim() : "";
  }

  private static void validatePackMaxWeight(String[] parts, String line) throws APIException {
    int packMaxWeight = Integer.parseInt(getBufferPart(parts, 0));
    if(packMaxWeight > 100 || packMaxWeight <= 0){
      throw new APIException("Invalid Pack weight on line: " + line);
    }
  }

  private static void validatePackItems(String[] parts, String line) throws APIException {
    String[] itemStrings = getBufferPart(parts, 1).split("\\s+");
    //TODO validate quantity 15 items per pack
  }

  private static ArrayList<Item> processItemsPack(String[] parts, String line) throws APIException {
    String[] itemStrings = getBufferPart(parts, 1).split("\\s+");
    ArrayList items = new ArrayList();
    for (String itemString : itemStrings) {
      try{
        //transform from
        String[] itemDetails = parseItemDelimiters(itemString);
        //validate item information and structure.
        validateItems(itemDetails, items, line);
        //Transform validated item into item object and add to the item list.
        items.add(transformItem(itemDetails));
      }catch (Exception e){
        //process will continue even if one line/item is not valid.
        //TODO put log to show message on what was the validation error
      }
    }
    return items;
  }

  private static String[] parseItemDelimiters(String itemString){
    String cleanItem = itemString.replace("(", "").replace(")", "");
    return cleanItem.split(",");
  }

  private static void validateItems(String[] itemDetails, ArrayList<Item> items, String line) throws APIException {
    //validate if item after transformation have 3 attributes (index, weight and price)
    validateItemLength(itemDetails,line);
    //validate if item index is valid AND its unique id in the items list
    validateItemIndex(itemDetails, items, line);
  }

  private static void validateItemLength(String[] itemDetails, String line) throws APIException {
    if (itemDetails.length != 3) {
      throw new APIException("Invalid item format: " + Arrays.toString(itemDetails) + "on line: " + line);
    }
  }

  private static void validateItemIndex(String[] itemDetails, ArrayList<Item> items, String line) throws APIException {
    Optional<Item> foundItem = items.stream().filter(i -> {
      itemDetails[0].trim();
      return false;
    }).findAny();
    if (foundItem.isPresent()) {
      throw new APIException("Item index already found on list of items for this package, ignoring it :"+ foundItem.get().getIndex() + "on line: " + line);
    }
  }

  private static Item transformItem(String[] itemDetails){
    int index = Integer.parseInt(itemDetails[0].trim());
    double weight = Double.parseDouble(itemDetails[1].trim());
    double cost = Double.parseDouble(itemDetails[2].replace("€", "").trim());
    return new Item(index, weight, cost);
  }


  // Method to find optimal items using dynamic programming
  private static String findOptimalCombination(int maxWeight, List<Item> items) {
    return null;
  }
}
