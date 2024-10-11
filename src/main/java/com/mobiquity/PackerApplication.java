package com.mobiquity;

import com.mobiquity.exception.APIException;
import com.mobiquity.packer.Packer;

public class PackerApplication {
    public static void main(String[] args) {
        try {
            String result = Packer.pack("example_input");
            System.out.println(result);
        } catch (APIException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
