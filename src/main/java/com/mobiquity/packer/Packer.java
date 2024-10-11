package com.mobiquity.packer;

import com.mobiquity.exception.APIException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class Packer {

  //Max Weight of the package/item and price limit
  private static final int MAX_WEIGHT_PRICE_LIMIT = 100;

  Packer() {
  }
  /**
   * This method is responsible for reading the file and processing the information to find the optimal items for each package.
   *
   * @param filePath the path to the file
   * @return the optimal items for each package passed on the input file
   * @throws APIException if there is an error on reading the file or the file not found on filepath provided.
   */
  public static String pack(String filePath) throws APIException {
    StringBuilder result = new StringBuilder();
    try{
      //stream to read file line by line and validate all information
      readFile(filePath).forEach(line -> result.append(processLine(line)).append("\n"));
    } catch (Exception e) {
      throw new APIException("Error reading file: " + e.getMessage());
    }
    return result.toString().trim();
  }

  /**
   * This method is responsible for reading the file from the classpath and returning a list of lines.
   * @param filePath the path to the file
   * @return the list of lines from the file
   * @throws APIException if there is an error on reading the file or the file not found on filepath provided.
   */
  static List<String> readFile(String filePath) throws APIException, URISyntaxException, IOException {
    // Load the file from the classpath using the class loader
    var resource = Optional.ofNullable(Packer.class.getClassLoader().getResource(filePath))
            .orElseThrow(() -> new APIException("File not found: " + filePath));

    // Read all lines from the file using Files.readAllLines with UTF-8 encoding
    return Files.readAllLines(Path.of(resource.toURI()), StandardCharsets.UTF_8);
  }

  /**
   * This method is responsible for processing line on the file and validate them.
   * @param line the line to be processed
   * @return the optimal items for the package
   */
  static String processLine(String line){
    List<Item> items;
    try{
      String[] parts = line.split(":");
      //some validations pack format.
      validateLinePackComposition(parts, line);
      //process items per pack
      items = validateItems(transformItems(parts[1]), line);
      //find the best combination of items per pack
      return mapItemsPerPack(Integer.parseInt(getBufferPart(parts)), items);
    }catch (Exception e){
      //process will abort if line is not valid.
      return "-";
    }
  }

  /**
   * This method is responsible for validating the line format and the pack information.
   * @param parts the parts of the line split by ":"
   * @param line  the line to be processed
   * @throws APIException  if the line is not in the correct format or the pack information is invalid.
   */
  static void validateLinePackComposition(String[] parts, String line) throws APIException {
    //validate if line is in correct format.
    validateLineFormat(parts, line);
    //validate packMaxWeight.
    validatePackMaxWeight(parts, line);
  }

  /**
   * This method is responsible for validating the line format.
   * @param parts the parts of the line split by ":"
   * @param line the line to be processed
   * @throws APIException if the line is not in the correct format.
   */
  private static void validateLineFormat(String[] parts, String line) throws APIException {
    if (parts.length != 2) {
      throw new APIException("Invalid pack format on line: " + line);
    }
  }

  /**
   * This method is responsible for validating the pack max weight.
   * @param parts the parts of the line split by ":"
   * @param line  the line to be processed
   * @throws APIException if the pack max weight is invalid.
   */
  private static void validatePackMaxWeight(String[] parts, String line) throws APIException {
    int packMaxWeight = Integer.parseInt(getBufferPart(parts));
    if (packMaxWeight > MAX_WEIGHT_PRICE_LIMIT || packMaxWeight <= 0) {
      throw new APIException("Invalid Pack weight on line: " + line);
    }
  }

  /**
   * This method is responsible for getting the buffer part of the line.
   *
   * @param parts the parts of the line split by ":"
   * @return the buffer part of the line
   */
  private static String getBufferPart(String[] parts) {
    return parts[0] != null ? parts[0].trim() : "";
  }

  /**
   * This method is responsible for transforming the items from the line into a list of Item objects.
   * @param itemsRaw the string containing the items
   * @return the list of items
   */
  private static List<Item> transformItems(String itemsRaw){
    // Regex to extract all the items in the list with the correct pattern
    Pattern pattern = Pattern.compile("\\((\\d+),(\\d+\\.\\d+),€(\\d+)\\)");
    // Use a stream to find all matches and map them to Item objects
    return pattern.matcher(itemsRaw)
            .results()  // Stream of MatchResult
            .map(match -> new Item(
                    Integer.parseInt(match.group(1)),    // index
                    Double.parseDouble(match.group(2)),  // weight
                    Integer.parseInt(match.group(3))))   // cost
            .toList();  // Collect the results into a List
  }

  /**
   * This method is responsible for validating the items per pack.
   * @param items the list of items per pack
   * @param line the line to be processed
   * @return the list of validated items
   * @throws APIException if the items are not valid.
   */
  private static ArrayList<Item> validateItems(List<Item> items, String line) throws APIException {
    ArrayList<Item> validatedItems = new ArrayList<>();
    //validate item quantity on pack if its < 15 and > 0.
    validatePackItems(items, line);
    // iterate on list of items to validate them
    for(Item itemToValidate : items){
      try{
        // method to validate the weight of the item, if item > 100 or <= 0
        validateItemWeight(itemToValidate, line);
        // method to validate the cost of the item, if item > 100 or <= 0
        validateItemCost(itemToValidate, line);
        // if item fully validated, add him on the list of validated items to make optimal combination
        validatedItems.add(itemToValidate);
      }catch (APIException e){
        //process will continue even if one item is not valid.
        System.out.println("Error validating item: " + itemToValidate + " on line: " + line + " " + e.getMessage());
      }
    }
    return validatedItems;
  }

  /**
   * This method is responsible for validating the weight of the item.
   * @param item the item to be validated
   * @param line the line to be processed
   * @throws APIException if the item weight is invalid.
   */
  static void validateItemWeight(Item item, String line) throws APIException {
    // if item weight is > 100 or <= 0, throw exception
    if (item.getWeight() > MAX_WEIGHT_PRICE_LIMIT || item.getWeight() <= 0) {
      throw new APIException("Item weight is invalid: " + item.getWeight() + " on line: " + line);
    }
  }

  /**
   * This method is responsible for validating the cost of the item.
   * @param item  the item to be validated
   * @param line the line to be processed
   * @throws APIException if the item cost is invalid.
   */
  static void validateItemCost(Item item, String line) throws APIException {
    // if item cost is > 100 or <= 0, throw exception
    if (item.getCost() > MAX_WEIGHT_PRICE_LIMIT || item.getCost() <= 0) {
      throw new APIException("Item cost is invalid: " + item.getCost() + " on line: " + line);
    }
  }


  /***
   * This method is responsible for validating the quantity of items per pack.
   * @param itemsList the list of items per pack
   * @param line the line to be processed
   * @throws APIException if the quantity of items per pack is invalid.
   */
  private static void validatePackItems(List<Item> itemsList, String line) throws APIException {
    if(itemsList.isEmpty() || itemsList.size() > 15){
      throw new APIException("Invalid Pack item quantity: " + line);
    }
  }

  /***
   * This method is responsible for mapping the items per pack to the optimal combination of items.
   * @param maxWeight the maximum weight of the pack
   * @param items the list of items per pack
   * @return the optimal combination of items for the pack
   */
  static String mapItemsPerPack(double maxWeight, List<Item> items) {
    // Find the optimal combination of items that fits within the maximum weight
    Result result = findOptimalCombination(maxWeight, items, items.size());
    // If the result contains items, proceed to format the item indices
    if (!result.items.isEmpty()) {
      // Sort the items by their index for proper order
      Collections.sort(result.items);
      // Create a StringJoiner to join the indices of the selected items into a string
      StringJoiner joiner = new StringJoiner(",");
      // Add each item index to the joiner as a string
      for (int index : result.items) {
        joiner.add(String.valueOf(index));
      }
      // Return the comma-separated string of item indices
      return joiner.toString();
    }
    // If no items are selected, return "-"
    return "-";
  }

  /**
   * This method is responsible for finding the optimal combination of items for the pack.
   * It uses recursion to explore the inclusion or exclusion of each item and returns the best result.
   *
   * @param maxWeight the maximum weight of the pack
   * @param items the list of items per pack
   * @param n the number of items to consider (size of the list)
   * @return the optimal combination of items for the pack
   */
  private static Result findOptimalCombination(double maxWeight, List<Item> items, int n) {
    // Base case: if there are no items left or no weight capacity, return a result with 0 cost and weight
    if (n == 0 || maxWeight == 0) {
      return new Result(0, 0, new ArrayList<>());
    }
    // Get the current item based on the index n-1 (items are 0-indexed)
    Item currentItem = items.get(n - 1);
    // Option 1: Recursively exclude the current item and compute the result
    Result excludeItem = findOptimalCombination(maxWeight, items, n - 1);
    // Option 2: Recursively include the current item if it fits within the remaining weight
    Result itemResult = new Result(0, 0, new ArrayList<>());
    if (currentItem.weight <= maxWeight) {
      // Subtract the current item's weight and add its cost and index to the result
      itemResult = findOptimalCombination(maxWeight - currentItem.weight, items, n - 1);
      itemResult.cost += currentItem.cost;
      itemResult.weight += currentItem.weight;
      itemResult.items.add(currentItem.index);
    }
    // Return the option with the higher cost; in case of a tie, choose the one with the lower weight
    return (itemResult.cost > excludeItem.cost ||
            (itemResult.cost == excludeItem.cost && itemResult.weight < excludeItem.weight) ? itemResult : excludeItem);
  }
}

