package com.mobiquity.packer;

import com.mobiquity.exception.APIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

class PackerTest {
    @BeforeEach
    void setUp() {
        // Initialize any shared resources if necessary before each test
    }
    @Test
    void testPackWithValidFile() throws APIException, URISyntaxException, IOException {
        // Prepare the input file with valid data
        var filePath = "example_valid_input";
        // Mock the file reading logic if needed
        Packer packer = Mockito.spy(new Packer());
        doReturn(List.of("81 : (1,53.38,€45) (2,88.62,€98)")).when(packer);
        Packer.readFile(filePath);
        // Test with a valid input file
        String result = Packer.pack(filePath);
        // Verify that the result is as expected
        assertEquals("1", result);
    }

    @Test
    void testPackWithNonexistentFile() {
        // Prepare a nonexistent file path
        String filePath = "nonexistent_file.txt";
        // Expect an APIException when the file is not found
        Exception exception = assertThrows(APIException.class, () -> Packer.pack(filePath));
        // Assert that the exception message is as expected
        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void testProcessLineWithValidInput() {
        // Test the line processing with a valid line
        String line = "81 : (1,53.38,€45) (2,88.62,€98)";

        // Expect the valid result for the given input
        String result = Packer.processLine(line);

        assertEquals("1", result);
    }

    @Test
    void testProcessLineWithInvalidFormat() {
        // Test line processing with invalid format
        String line = "invalid_format";

        // Expect the result to be "-"
        String result = Packer.processLine(line);

        assertEquals("-", result);
    }

    @Test
    void testValidateLinePackCompositionWithInvalidWeight() {
        String[] parts = {"150", "(1,53.38,€45)"}; // 150 is an invalid weight (> 100)
        String line = "150 : (1,53.38,€45)";

        // Expect an APIException when the weight exceeds 100
        Exception exception = assertThrows(APIException.class, () -> Packer.validateLinePackComposition(parts, line));

        // Assert that the exception message is as expected
        assertTrue(exception.getMessage().contains("Invalid Pack weight"));
    }

    @Test
    void testValidateLinePackCompositionWithValidWeight() throws APIException {
        String[] parts = {"80", "(1,53.38,€45)"}; // 80 is a valid weight
        String line = "80 : (1,53.38,€45)";

        // No exception should be thrown
        assertDoesNotThrow(() -> Packer.validateLinePackComposition(parts, line));
    }

    @Test
    void testMapItemsPerPackWithValidItems() {
        List<Item> items = List.of(
                new Item(1, 53.38, 45),
                new Item(2, 88.62, 98)
        );
        // The valid max weight allows item 1 to be picked
        String result = Packer.mapItemsPerPack(80, items);
        assertEquals("1", result);
    }

    @Test
    void testMapItemsPerPackWithNoValidItems() {
        List<Item> items = List.of(
                new Item(1, 150.00, 45), // Exceeds the weight limit
                new Item(2, 200.00, 98)  // Exceeds the weight limit
        );
        // The max weight is smaller than the item weights, so none should be picked
        String result = Packer.mapItemsPerPack(100, items);
        assertEquals("-", result);
    }

    @Test
    void testValidateItemWeightWithInvalidWeight() {
        Item invalidItem = new Item(1, 150, 45); // Weight > 100
        String line = "81 : (1,150.00,€45)";

        Exception exception = assertThrows(APIException.class, () -> Packer.validateItemWeight(invalidItem, line));

        assertTrue(exception.getMessage().contains("Item weight is invalid"));
    }

    @Test
    void testValidateItemCostWithInvalidCost() {
        Item invalidItem = new Item(1, 53.38, 150); // Cost > 100
        String line = "81 : (1,53.38,€150)";

        Exception exception = assertThrows(APIException.class, () -> Packer.validateItemCost(invalidItem, line));

        assertTrue(exception.getMessage().contains("Item cost is invalid"));
    }
}
