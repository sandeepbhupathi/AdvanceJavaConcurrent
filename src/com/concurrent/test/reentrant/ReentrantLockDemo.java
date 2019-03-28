package com.concurrent.test.reentrant;

import jdk.nashorn.internal.codegen.CompilerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDemo {
    public static void main(String[] args) {
        List<Integer> intList = new ArrayList<>();
        Lock lock = new ReentrantLock();

        Condition isEmpty = lock.newCondition();
        Condition isFull = lock.newCondition();

        class Consumer implements Callable<String>{

            @Override
            public String call() throws Exception {
                int count = 0;
                while (count++<50){
                    lock.lock();
                    try {
                        while (isEmpty(intList)) {
                            //wait
                            if(!isEmpty.await(10,TimeUnit.MILLISECONDS))
                                throw new TimeoutException("Wait has timedout");
                        }
                        intList.remove(intList.size() - 1);
                        isFull.signal();
                        //signal
                    }finally {
                        lock.unlock();
                    }

                }
                return "Consumed "+(count-1);
            }
        }

        class Producer implements Callable<String>{
            @Override
            public String call() throws Exception {
                int count=0;
                while (count++<50){
                    lock.lock();
                    try{
                        int i = 10/0;
                        while (isFull(intList)){
                            //wait
                            isFull.await();
                        }
                        intList.add(1);
                        isEmpty.signal();
                    }finally {
                        lock.unlock();
                    }
                }
                return "Produced "+(count-1);
            }
        }

        List<Producer> producerList = new ArrayList<>();
        for (int i =0 ;i<4; i++){
            producerList.add(new Producer());
        }
        List<Consumer> consumerList = new ArrayList<>();
        for (int i=0;i<4;i++){
            consumerList.add(new Consumer());
        }

        System.out.println("Producer Consumer Launched");

        List<Callable<String>> callableList = new ArrayList<>();
        callableList.addAll(producerList);
        callableList.addAll(consumerList);

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        try{
            List<Future<String>> futures = executorService.invokeAll(callableList);
            futures.forEach(future->{
                try {
                    System.out.println(future.get());
                } catch (Exception e) {
                    System.out.println(" Exception: "+e.getMessage());
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    private static boolean isEmpty(List<Integer> intList) {
        return intList.isEmpty();
    }

    private static boolean isFull(List<Integer> intList) {
        return intList.size()==50;
    }



}
