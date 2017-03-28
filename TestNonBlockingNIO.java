import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
/**
 * 非阻塞式  TCP通信
 */
public class TestNonBlockingNIO1 {

    @Test
    public void client() {
        //1.创建套接字通道
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));

            //2.分配缓冲区
            ByteBuffer buf = ByteBuffer.allocate(1024);

            //3.从控制台输入数据传至通道
            Scanner scanner = new Scanner(System.in);
            System.out.println("请输入...");
            while (scanner.hasNext()) {
                String str = scanner.next();
                buf.put((new Date().toString() + "\n" + str).getBytes());

                //切换读模式
                buf.flip();
                socketChannel.write(buf);
                buf.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if (socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Test
    public void server() {

        //1.创建通道
        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();

            //2.切换非阻塞模式
            serverSocketChannel.configureBlocking(false);

            //3.绑定端口
            serverSocketChannel.bind(new InetSocketAddress(9898));

            //4.获取选择器
            Selector selector = Selector.open();

            //5.将通道注册到选择器上（一个选择器可以注册多个通道）,并指定监听接收事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            //6.轮询式的获取选择器上已经注册通道中“准备就绪”的事件对应的SelectionKey，添加到被选择SelectionKey的集合中
            while (selector.select() > 0) {
                //7.获取当前选择器中所有注册的“已准备就绪的事件”选择键的集合，并获取它的迭代器
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    //8.获取准备就绪的事件
                    SelectionKey key = iterator.next();

                    //9.判断是什么准备就绪
                    if (key.isAcceptable()) {
                        //10.接收准备就绪，获取客户端连接
                        SocketChannel socketChannel = serverSocketChannel.accept();

                        //11.切换非阻塞模式
                        socketChannel.configureBlocking(false);

                        //12.将此通道注册到选择器上
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if(key.isReadable()){
                        //13.获取当前通道上“读就绪”的通道
                        SocketChannel socketChannel = (SocketChannel) key.channel();

                        //14.读取数据
                        ByteBuffer buf = ByteBuffer.allocate(1024);

                        socketChannel.read(buf);

                        int len = -1;
                        while ((len = socketChannel.read(buf)) > 0) {
                            buf.flip();
                            System.out.println(new String(buf.array(),0,len));
                            buf.clear();
                        }

                    }
                }
                //15.取消选择键
                iterator.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
