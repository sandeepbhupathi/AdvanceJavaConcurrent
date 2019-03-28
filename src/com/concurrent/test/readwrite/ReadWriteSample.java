package com.concurrent.test.readwrite;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteSample {
    private Map<Long,String> cache = new HashMap<>();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Lock readLock = readWriteLock.readLock();
    private Lock writeLock = readWriteLock.writeLock();

    public void put(Long key,String value){
       writeLock.lock();
        try{
            cache.put(key,value);
        }finally {
          writeLock.unlock();
        }
    }
    public String get(Long key){
        readLock.lock();
        try{
            return cache.get(key);
        }finally {
            readLock.unlock();
        }

    }
    public static void main(String[] args) {
        ReadWriteSample readWriteSample = new ReadWriteSample();

        class Produceer implements Callable<String>{

            private Random random = new Random();
            @Override
            public String call() throws Exception {
                while (true){
                    long key = random.nextInt(1_000);
                    readWriteSample.put(key,Long.toString(key));

                    if(readWriteSample.get(key)==null){
                        System.out.println("Key "+key+" has not been put in the map");
                    }
                }
            }
        }

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        try{
            for (int i = 0; i<4;i++){
                executorService.submit(new Produceer());
            }
        }finally {
            executorService.shutdown();
        }


    }
}
