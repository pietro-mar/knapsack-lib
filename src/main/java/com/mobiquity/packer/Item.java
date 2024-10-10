package com.mobiquity.packer;



public class Item {
    int index;
    double weight;
    double cost;

    public Item(int index, double weight, double cost) {
        this.index = index;
        this.weight = weight;
        this.cost = cost;
    }

    public int getIndex() {
        return index;
    }
}
