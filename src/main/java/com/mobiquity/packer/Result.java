package com.mobiquity.packer;

import java.util.List;

class Result {
    double cost;
    double weight;
    List<Integer> items;

    public Result(double cost, double weight, List<Integer> items) {
        this.cost = cost;
        this.weight = weight;
        this.items = items;
    }
}