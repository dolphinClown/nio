import javax.swing.plaf.basic.BasicButtonUI;
import java.nio.ByteBuffer;

public class TestBuffer {

    @Test
    public void test3(){
        //分配直接缓冲区
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        //判断是否为直接缓冲区
        System.out.println(buffer.isDirect());
    }

    @Test
    public void test2() {
        String str = "abcde";
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        buffer.put(str.getBytes());
        buffer.flip();

        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst, 0, 2);
        System.out.println(new String(dst, 0, 2));

        System.out.println(buffer.position());

        //mark(): 标记
        buffer.mark(); //记录当前position的位置
        buffer.get(dst, 2, 2); //从2开始读取了两个字节的数据position位置移动到4
        System.out.println(new String(dst, 2, 2));
        System.out.println(buffer.position());

        //reset(): 恢复到mark的位置
        buffer.reset();
        System.out.println(buffer.position()); //2


        //判断缓冲区是否还有剩余数据
        if (buffer.hasRemaining()) {
            // 如果有还剩多少个
            System.out.println(buffer.remaining());
        }
    }

    @Test
    public void test1() {
        String str = "abcde";
        //1.分配一个指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        System.out.println("-----------allocate-------------");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //2.利用put()存入数据到缓冲区中
        buffer.put(str.getBytes());
        System.out.println("----------put-------------");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //3.切换读取数据模式
        buffer.flip();
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //4.利用get()方法读取缓冲区中的数据
        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst);
        System.out.println("----------get-------------");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //5.rewid()：可重复读数据 也就是将position指针位置移至缓冲区开始出
        buffer.rewind();
        System.out.println("----------rewidt-------------");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //6.clear(): 清空缓冲区，但缓冲区中的数据依然存在，但数据处于被遗忘状态
        buffer.clear();
        System.out.println("----------clear-------------");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        System.out.println((char)buffer.get());
    }
}
