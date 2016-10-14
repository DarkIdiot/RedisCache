package com.gome.supermain.redis.util;

/**
 * 二分查找工具类
 * @author darkidiot
 */
public class BinarySearchUtil {

	private BinarySearchUtil() {
	}

	/**
	 * 二分查找算法
	 * @param srcArray 有序数组
	 * @param des 查找元素
	 * @return des的数组下标，没找到返回-1
	 */
	public static int binarySearch(int[] srcArray, int des) {
		int low = 0;
		int high = srcArray.length - 1;
		while (low <= high) {
			int middle = (low + high) / 2;
			if (des == srcArray[middle]) {
				return middle;
			} else if (des < srcArray[middle]) {
				high = middle - 1;
			} else {
				low = middle + 1;
			}
		}
		return -1;
	}

	/**
	 * 二分查找特定整数在整型数组中的位置(递归)
	 * @param dataset
	 * @param data
	 * @param beginIndex
	 * @param endIndex
	 * @return index
	 */
	public static int binarySearch(int[] dataset, int data, int beginIndex, int endIndex) {
		int midIndex = (beginIndex + endIndex) / 2;
		if (data < dataset[beginIndex] || data > dataset[endIndex] || beginIndex > endIndex) {
			return -1;
		}
		if (data < dataset[midIndex]) {
			return binarySearch(dataset, data, beginIndex, midIndex - 1);
		} else if (data > dataset[midIndex]) {
			return binarySearch(dataset, data, midIndex + 1, endIndex);
		} else {
			return midIndex;
		}
	}

	public static void main(String[] args) {
		int[] src = new int[] { 1, 3, 5, 7, 8, 9 };
		System.out.println(binarySearch(src, 3));
		System.out.println(binarySearch(src, 3, 0, src.length - 1));
	}
}
