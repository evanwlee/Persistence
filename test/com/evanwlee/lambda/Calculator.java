package com.evanwlee.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Calculator {
	  
    interface IntegerMath {
        int operation(int a, int b);   
    }
    
    interface IntegerMathSingle {
        int operation(int a);   
    }
  
    public int operateBinary(int a, int b, IntegerMath op) {
        return op.operation(a, b);
    }
    
    public int operateBinary(int a, IntegerMathSingle op) {
        return op.operation(a);
    }
 
    public static void main(String... args) {
    
        Calculator myApp = new Calculator();
        IntegerMath addition = (x, z) -> x + z;
        IntegerMath subtraction = (a, b) -> a - b;
        System.out.println("40 + 2 = " + myApp.operateBinary(40, 2, addition));
        System.out.println("20 - 10 = " + myApp.operateBinary(20, 10, subtraction));  
        
        IntegerMathSingle work = (a) -> (a+10) - a;
        System.out.println("20 + 10 - 20 = " + myApp.operateBinary(20, work));
        
  
        
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
        
        System.out.println(myApp.sumAll(numbers, n -> true));
        System.out.println(myApp.sumAll(numbers, n -> n % 2 == 0));
        System.out.println(myApp.sumAll(numbers, n -> n > 3));
    }
    
    
    
    
    
    
    
    public int sumAll(List<Integer> numbers, Predicate<Integer> p) {
        int total = 0;
        for (int number : numbers) {
            if (p.test(number)) {
                total += number;
            }
        }
        return total;
    }
}