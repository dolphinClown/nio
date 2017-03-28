package com.clay.nio;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class TestChannel {

    //6.字符集
    @Test
    public void test6(){
        Charset charset = Charset.forName("GBK");

        //获取编码器
        CharsetEncoder ce = charset.newEncoder();

        //获取解码器
        CharsetDecoder cd = charset.newDecoder();

        CharBuffer charBuffer = CharBuffer.allocate(1024);
        charBuffer.put("what");
        charBuffer.flip(); //切换读模式

        try {
            //编码
            ByteBuffer byteBuffer = ce.encode(charBuffer);
            byte[] dst = new byte[byteBuffer.limit()];
            byteBuffer.get(dst);
            System.out.println(Arrays.toString(dst)); //输出编码的数据
            System.out.println(new String(dst));

            //解码
            byteBuffer.flip();
            CharBuffer charBuffer1 = cd.decode(byteBuffer);
            System.out.println(charBuffer1.toString());
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }

    }

	//5.Charset 列表
    @Test
    public void test5(){

        Map<String, Charset> map = Charset.availableCharsets();

        Set<Map.Entry<String, Charset>> set = map.entrySet();

        for (Map.Entry<String,Charset> entry : set) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }

    //4.分散和聚集
    @Test
    public void test4() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("1.txt","rw");

        //1.获取通道
        FileChannel channel1 = randomAccessFile.getChannel();

        //2.分配指定大小的缓冲区
        ByteBuffer buf1 = ByteBuffer.allocate(100);
        ByteBuffer buf2 = ByteBuffer.allocate(1024);

        //3.分散读取
        ByteBuffer[] bufs = {buf1, buf2};
        channel1.read(bufs);

        for (ByteBuffer byteBuffer : bufs) {
            byteBuffer.flip(); //切换读模式
        }

        System.out.println(new String(bufs[0].array(), 0, bufs[0].limit()));
        System.out.println("-----------------");
        System.out.println(new String(bufs[1].array(), 0, bufs[1].limit()));

        //4. 聚集写入
        RandomAccessFile raf2 = new RandomAccessFile("2.txt", "rw");
        FileChannel channel2 = raf2.getChannel();

        channel2.write(bufs);
    }

    //3.通道之间的数据传输(直接缓冲区的方式)
    @Test
    public void test3() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
        //CREATE_NEW文件存在则报错，否则创建   CREATE文件存在则覆盖，否则创建
        FileChannel outChannel = FileChannel.open(Paths.get("3.jpg"), StandardOpenOption.WRITE, StandardOpenOption.READ,
                StandardOpenOption.CREATE);

//        inChannel.transferTo(0, inChannel.size(), outChannel);
        outChannel.transferFrom(inChannel, 0, inChannel.size());
        inChannel.close();
        outChannel.close();

    }

    //2.使用直接缓冲区完成文件的复制（内存映射文件）只有ByteBuffer支持
    @Test
    public void test2() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
        //CREATE_NEW-文件存在则报错，否则创建   CREATE文件存在则覆盖，否则创建
        FileChannel outChannel = FileChannel.open(Paths.get("3.jpg"), StandardOpenOption.WRITE, StandardOpenOption.READ,
                StandardOpenOption.CREATE_NEW);

        //不需要通道，直接在硬盘和程序间操作
        //通过map方法 内存映射文件
        MappedByteBuffer inMappedBuf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        //注意：这里是READ_WRITE
        MappedByteBuffer outMappedBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

        //直接对缓冲区进行数据的读取操作
        byte[] dst = new byte[inMappedBuf.limit()];
        inMappedBuf.get(dst);
        outMappedBuf.put(dst);

        inChannel.close();
        outChannel.close();

    }

    //1.利用通道完成文件的复制(非直接缓冲区)
    @Test
    public void test1() {
        FileInputStream in = null;
        FileOutputStream out = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            in = new FileInputStream("1.jpg");
            out = new FileOutputStream("2.jpg");

            //1.获取通道
            inChannel = in.getChannel();
            outChannel = out.getChannel();

            //2.分配指定大小的缓冲区
            ByteBuffer buf = ByteBuffer.allocate(1024);

            //3.将通道中的数据存入缓冲区
            while (inChannel.read(buf) != -1) {
                buf.flip(); //切换成读取数据模式
                //4.将缓冲区中的数据写入到通道中
                outChannel.write(buf);
                buf.clear(); //清空缓冲区
        }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
                if (outChannel != null) {
                     try {
                        outChannel.close();
                     } catch (IOException e) {
                        e.printStackTrace();
                     }
                }
                if (inChannel != null) {
                    try {
                        inChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
        }

    }
}
