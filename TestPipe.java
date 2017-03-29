package com.clay.nio;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 管道 两个线程之间的“单向”数据连接
 */
public class TestPipe {
    //创建不带返回值得Callable<Void>，作用和Runnable相同
    //写管道线程
    class SinkThread implements Callable<Void> {
        private Pipe.SinkChannel sinkChannel;
        private ByteBuffer buf;

        public SinkThread(Pipe.SinkChannel sinkChannel, ByteBuffer buf) {
            this.sinkChannel = sinkChannel;
            this.buf = buf;
        }

        @Override
        public Void call() throws Exception {
            buf.put("Sink线程写入的数据".getBytes());
            buf.flip();
            sinkChannel.write(buf);
            return null;
        }
    }

    //读管道线程
    class SourceThread implements Callable<Void> {
        private Pipe.SourceChannel sourceChannel;
        private ByteBuffer buf;

        public SourceThread(Pipe.SourceChannel sourceChannel, ByteBuffer buf) {
            this.sourceChannel = sourceChannel;
            this.buf = buf;
        }

        @Override
        public Void call() throws Exception {
            buf.flip();
            sourceChannel.read(buf);
            System.out.println(new String(buf.array(), 0, buf.limit()));
            return null;
        }
    }

    @Test
    public void testWithThread() {
        Pipe.SinkChannel sinkChannel = null;
        Pipe.SourceChannel sourceChannel = null;
        try {
            //创建管道
            Pipe pipe = Pipe.open();
            sinkChannel = pipe.sink();
            sourceChannel = pipe.source();

            //创建缓冲区
            ByteBuffer buf = ByteBuffer.allocate(1024);

            //创建FutureTask实例，执行线程任务
            FutureTask futureTask1 = new FutureTask(new SinkThread(sinkChannel, buf));
            FutureTask futureTask2 = new FutureTask(new SourceThread(sourceChannel, buf));

            new Thread(futureTask1).start();
            new Thread(futureTask2).start();

            try {
                futureTask1.get();
                futureTask2.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sinkChannel != null) {
                try {
                    sinkChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (sourceChannel != null) {
                try {
                    sourceChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void test() {
        //1.获取管道
        Pipe.SinkChannel sinkChannel = null;
        Pipe.SourceChannel sourceChannel = null;
        try {
            Pipe pipe = Pipe.open();
            //2.创建缓冲区
            ByteBuffer buf = ByteBuffer.allocate(1024);

            //3.向管道中写数据
            sinkChannel = pipe.sink();
            buf.put("管道中的数据".getBytes());
            buf.flip();
            sinkChannel.write(buf);

            //4.读取管道中的数据
            sourceChannel = pipe.source();
            buf.flip();
            sourceChannel.read(buf);
            System.out.println(new String(buf.array(), 0, buf.limit()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sinkChannel != null) {
                try {
                    sinkChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (sourceChannel != null) {
                try {
                    sourceChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
