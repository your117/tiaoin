package com.tiaoin.crawl.core.spider;

public class Counter {

    private int count = 0;

    public int getCount() {
        return count;
    }

    public void plus() {
        count++;
    }

    @Override
    public String toString() {
        return "Counter [count=" + count + "]";
    }

}
