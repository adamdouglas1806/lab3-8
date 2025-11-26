package sort.parallel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import sort.sequential.SequentialMergeSort;
import sort.sequential.SortingCommon;
import utils.Benchmark;

/*
 * Merge Sort results with thresholding
 * ~~~~~~~~~~~~~~~~~~
 *
 * After parallelisation:
 * - 1 thread
 *   - no threshold:
 *   - threshold=128:
 *   - threshold=512:
 *   - threshold=2048:
 *   - threshold=8192:
 *
 * - 2 threads
 *   - no threshold:
 *   - threshold=128:
 *   - threshold=512:
 *   - threshold=2048:
 *   - threshold=8192:
 *   
 *   - 4 threads
 *   - no threshold:
 *   - threshold=128:
 *   - threshold=512:
 *   - threshold=2048:
 *   - threshold=8192:
 *   
 *   - 8 threads
 *   - no threshold:
 *   - threshold=128:
 *   - threshold=512:
 *   - threshold=2048:
 *   - threshold=8192:
 *   
 *   - 16 threads
 *   - no threshold:
 *   - threshold=128:
 *   - threshold=512:
 *   - threshold=2048:
 *   - threshold=8192:
 *
 *   <insert more if you have more than 2 CPU cores>
 *
 * Parameters of the shortest runtime:
 * - runtime: 975ms
 * - how many threads: 16 threads
 * - threshold value: 8192
 * 
 * Best parallel speedup: 1.5
 * 
 * Parallelism efficiency: 0.094
*/

public class ParallelMergeSortThreshold extends RecursiveTask<LinkedList<Integer>> {
	LinkedList<Integer> arr;
	int threshold;

	public ParallelMergeSortThreshold(LinkedList<Integer> arr, int threshold) {
		this.arr = arr;
		this.threshold = threshold;
	}

	@Override
	protected LinkedList<Integer> compute() {
		int length = arr.size();

		// Q2: rewrite the base case condition and body of this if statement,
		// so that you run:
		//
		// sequential merge sort for small inputs (the "base case")
		// by using SequentialMergeSort.mergeSort(..) 
		//
		// or run
		//
		// parallel merge sort in parallel for large inputs (the "recursive" case)
		if (length < threshold) {
		//The base case was been changed to check if the length is less than the threshold.
			return SequentialMergeSort.mergeSort(arr);
			//If the condition is met then the sequential sort is returned.
		}

		else { // parallel case

			/* compute the size of the two sub arrays */
			int halfSize = length / 2;

			/* declare these as `left` and `right` arrays */
			LinkedList<Integer> left = new LinkedList<Integer>();
			LinkedList<Integer> right = new LinkedList<Integer>();

			/* populate the left array with values */
			Iterator<Integer> it = arr.iterator();
			int index = 0;
			while (index < halfSize) {
				left.add(it.next());
				index++;
			}

			/* populate the right array with values */
			index = 0;
			while (index < length - halfSize) {
				right.add(it.next());
				index++;
			}
			
			// replace this to use the parallel fork/join approach but this
			// time using this ParallelMergeSoftThreshold class to create the two tasks,
			// rather than the ParallelMergeSort class that you used in Q1B. Remember
			// that this time you also need to pass the threshold as the 2nd argument
			// to the constructor.
			// LinkedList<Integer> resultLeft = SequentialMergeSort.mergeSort(left);
			// LinkedList<Integer> resultRight = SequentialMergeSort.mergeSort(right);
			
			ParallelMergeSortThreshold LeftTaskSort = new ParallelMergeSortThreshold(left, threshold);
			//Declaring a new task by instantiating ParallelMergeSortThreshold for the left side.
			
			ParallelMergeSortThreshold rightTaskSort = new ParallelMergeSortThreshold(right, threshold);
			//Declaring a new task by instantiating ParallelMergeSortThreshold for the right side.
			
			LeftTaskSort.fork();
			//Forks the task which sorts the left list.
			
			LinkedList<Integer> resultRight = rightTaskSort.compute();
			//Creating a new linked list called resultsRight which will represent the computed version of the task that sorts the right side of the list.
			
			LinkedList<Integer> resultLeft = LeftTaskSort.join();
			//Creating a new linked list called resultLeft which will represent the joined left task.

			/* merge the sorted sub arrays */
			return SequentialMergeSort.merge(resultLeft, resultRight);
		}
	}

	/**
	 * Threshold based parallel merge sort
	 * 
	 * @param numbers     the input list
	 * @param threshold   when to switch from parallel divide-and-conquer to
	 *                    sequential divide-and-conquer
	 * @param parallelism how many threads to use in the ForkJoin workpool
	 * @return the sorted list
	 */
	public static LinkedList<Integer> parallelMergeSortThreshold(LinkedList<Integer> numbers, int threshold,
			int parallelism) {
		ForkJoinPool pool = new ForkJoinPool(parallelism);
		ParallelMergeSortThreshold mergeSortTask = new ParallelMergeSortThreshold(numbers, threshold);
		LinkedList<Integer> result = pool.invoke(mergeSortTask);
		return result;
	}

	/**
	 * Benchmarks threshold based parallel merge sort
	 */
	public static void main(String[] args) {
		/* generates a random list */
		LinkedList<Integer> numbers = SortingCommon.randomList(50000);

		/* gets the number of cores in this computer's CPU */
		int cpuCores = Runtime.getRuntime().availableProcessors();

		/*
		 * 1. prints the runtime for the parallel merge sort from Q1B.
		 * 
		 * 2. prints the runtime for the threshold based parallel merge sort for the
		 * implementation in Q2.
		 */
		for (int threads = 1; threads <= cpuCores; threads *= 2) {
			System.out.print("mergeSort\t no threshold\t\t");
			Benchmark.parallel(new ParallelMergeSort(numbers), threads);
			for (int threshold = 128; threshold <= 8192; threshold *= 4) {
				System.out.print("mergeSort\t threshold=" + threshold + "\t\t");
				Benchmark.parallel(new ParallelMergeSortThreshold(numbers, threshold), threads);
			}
		}
	}

}