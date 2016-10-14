package com.gome.supermain.redis.util;

/**
 * 斐波那契数列工具类
 * 斐波纳契数列，又称黄金分割数列，指的是这样一个数列：1、1、2、3、5、8、13、21、……在数学上，斐波纳契数列以如下被以递归的方法定义：F0=0，F1=1，Fn=F(n-1)+F(n-2)（n>=2，n∈N*）
 * @author darkidiot
 */
public class FibonacciUtil {

	private FibonacciUtil() {}
	
	 /**
	  * 递归实现方式  
	  * @param n
	  * @return
	  */
    public static int recursiveFibonacci(int n){  
        if(n <= 2){  
            return 1;  
        }else{  
            return recursiveFibonacci(n-1) + recursiveFibonacci(n-2);  
        }  
    }  
    
    /**
     * 递推实现方式  
     * @param n
     * @return
     */
    public static int circulationFibonacciNormal(int n){  
        if(n <= 2){  
            return 1;  
        }  
        int n1 = 1, n2 = 1, sn = 0;  
        for(int i = 0; i < n - 2; i ++){  
            sn = n1 + n2;  
            n1 = n2;  
            n2 = sn;  
        }  
        return sn;  
    }  
    /**
     * 测试
     */
    public static void main(String[] args) {  
        System.out.println(recursiveFibonacci(6) + ":" + circulationFibonacciNormal(6));  
    }  
}
