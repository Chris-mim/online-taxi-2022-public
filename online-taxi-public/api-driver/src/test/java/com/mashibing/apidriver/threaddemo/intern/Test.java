package com.mashibing.apidriver.threaddemo.intern;

public class Test {

    public static void main(String[] args) {
        String s=new String("a");
        System.out.println(s==s.intern());
    }
}
