//package com.sun.jna.examples;

/* HelloWorld.java */

import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.File;
import java.io.IOException;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import java.util.Random;
import java.lang.*;

//it's usually used by this class:
//https://github.com/apache/kafka/blob/trunk/core/src/main/scala/kafka/log/FileMessageSet.scala
public class HelloWorld {
  static int numItems = 1024*1024;
  static int recordSize = 1024;
  static byte[] record;

  //static class WriteThread extends Thread {
  static class WriteThread implements Runnable {
    private Thread t;
    private FileChannel fileChannel;
    private ByteBuffer buffer;
    //private ByteBuffer recBuf;
    
    WriteThread( FileChannel fc ){
        fileChannel = fc;
        buffer = ByteBuffer.wrap(record); 
        //recBuf = ByteBuffer.allocate(recordSize);
    }

    public void run() {
      try {
          System.out.println("write Thread " );
          int n = 0;
          //Thread.sleep(50);
          long startMs = System.currentTimeMillis();
          for (int i=0; i < numItems; i++){
              n = fileChannel.write(buffer);
              buffer.rewind();
              // flush to disk every 1MB
              if ( (i+1)%1024 == 0 )
                fileChannel.force(true);
              //n = fileChannel.read(recBuf, 0);
              //if (!checkRecord(recBuf)){
              //  System.out.println( " read wrong record in Write thread!!!  " + recBuf);
              //  return;
              //}
              //System.out.println( "  Write thread bytes  " + n);
          }
          long ellapsed = System.currentTimeMillis() - startMs;
          double diff = (double) ellapsed /1000.0;
          System.out.println(" write file Channel time: " + diff);
          System.out.println( " file Channel write throughput: " + (recordSize * numItems)/diff/1024/1024 + " MB/s");
          System.out.println( " finish writing to FIle Channel!!! " );
      } catch (IOException e) {
          System.out.println("Thread gets interrupted!!!");
      }
      //System.out.println("Thread " +  threadName + " exiting.");
    }

    public void start ()
    {
       if (t == null)
       {
          t = new Thread (this);
          t.start ();
       }
    }
  } 

  //static class ReadThread extends Thread {
  static class ReadThread implements Runnable {
    private Thread t;
    private FileChannel fileChannel;
    private ByteBuffer buffer;
    private int offset;
    
    ReadThread( FileChannel fc ){
        fileChannel = fc;
        buffer = ByteBuffer.allocate(recordSize);
        offset = 0;
    }

    public void run(){
      System.out.println("read Thread " );
      int n=0;
      try {
          long startMs = System.currentTimeMillis();
          for (int i=0; i < numItems; i++){
              buffer.clear();
              // weird => read return -1 if not calling size
              if (offset > fileChannel.size())
                System.out.println( "  read thread: offset  " + offset + " exceeds the channel size " + fileChannel.size());
              n = fileChannel.read(buffer, offset);
              if (!checkRecord(buffer)){
                System.out.println( " read wrong record in Read thread!!!  ");
                return;
              }
              offset += recordSize;
              //System.out.println( "  read thread bytes  " + n);
          }
          long ellapsed = System.currentTimeMillis() - startMs;
          double diff = (double) ellapsed /1000.0;
          System.out.println(" read file Channel time: " + diff);
          System.out.println( " file Channel read throughput: " + (recordSize * numItems)/diff/1024/1024 + " MB/s");
      } catch (IOException e) {
          System.out.println("Thread gets interrupted!!!");
      }
      //System.out.println("Thread " +  threadName + " exiting.");
    }

    public void start () 
    {
       if (t == null)
       {  
          t = new Thread (this);
          t.start ();
          //try{
          //} catch (InterruptedException e){
          //  System.out.println("Thread gets interrupted!!!");
          //}
       }
    }
  }


  static boolean checkRecord( ByteBuffer buffer ){
      if (buffer.capacity()==0){
        System.out.println(" receive 0 record in buffer ");
        return false;
      }
      // one way to convert
      //buffer.clear();
      //byte[] test = new byte[buffer.capacity()];
      //buffer.get(test, 0, test.length);
      // another to convert
      byte[] test = buffer.array();
      //String str = new String(test);
      //System.out.println(" receive in buffer " + str);
      for (int i=0; i<recordSize; i++){
        if (test[i] != record[i]){
          System.out.println(" received " + test[i] + " should be: " + record[i] );
          return false;
        } 
      }    
      return true;
  }
  static public void main(String argv[]) throws IOException, InterruptedException{
      record = new byte[recordSize];
      Random random = new Random(0);
      for (int i=0; i<recordSize; i++)
        record[i] = (byte) (random.nextInt(26) + 65);;
      
      RandomAccessFile randomAccessFile = new RandomAccessFile("/mnt/xin/temp.txt",	"rw");
      FileChannel fileChannel = randomAccessFile.getChannel();

      /*
        String k = "abcd123456 1234567890";
        ByteBuffer b = ByteBuffer.wrap(k.getBytes());
        String v = new String(b.array());
        fileChannel.write(b);

        ByteBuffer s = ByteBuffer.allocate(10);
        fileChannel.read(s, 0);
        System.out.println("read from fileChannel: " + new String(s.array()));
      */
      //WriteThread T1 = new WriteThread( fileChannel );


      //Thread T1 = new Thread( new WriteThread( fileChannel ) );
      //T1.start();
      //T1.join(); 

      System.out.println(" ready to start read");
      //ReadThread T2 = new ReadThread( fileChannel );
      int numRead = 4;
      Thread[] threads = new Thread[numRead];
      for (int i = 0; i<numRead; i++) {
      threads[i] = new Thread( new ReadThread( fileChannel ) );
      threads[i].start();
      }
      for (int i = 0; i<numRead; i++) {
      threads[i].join();
      }
  }
}
