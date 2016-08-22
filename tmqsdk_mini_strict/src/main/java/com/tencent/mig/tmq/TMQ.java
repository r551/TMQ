package com.tencent.mig.tmq;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TMQ {
    public static int CAPACITY = 1024;
    private static Queue<TmqMsg> preFilterMsgQueue = new LinkedList<>(); // 过滤前的实际消息序列，要有上限，避免一直不校验撑爆内存
    private static Set<String> filterSet = new HashSet<>(); // 可以预先设置消息按类型过滤
    private static List<TmqMsg> msgQueue = new ArrayList<>(); // 过滤后的实际消息序列，要有上限，避免一直不校验撑爆内存
    private static Queue<TmqMsg> expectedQueue = new LinkedList<>(); // 预期消息序列
    private static Queue<TmqMsg> checkedQueue = new LinkedList<>(); // 已校验消息序列
    private static CountDownLatch countDownLatch = null;
    private static String mHead, mFoot;

    public static void setHeadAndFoot(String head, String foot)
    {
        mHead = head;
        mFoot = foot;
    }

    public static boolean report(String tag, Object msg) {
        synchronized (TMQ.class) {
            if (countDownLatch == null) {
                return true;
            }

            if (countDownLatch.getCount()==0) {
                return true;
            }

            TmqMsg tmqMsg = new TmqMsg(tag, msg.toString());

            // 避免一直不校验撑爆内存
            if (preFilterMsgQueue.size() < CAPACITY * 2)
                preFilterMsgQueue.add(tmqMsg);

            // filterSet为空默认所有类型消息都有效
            if (!filterSet.isEmpty() && !filterSet.contains(tag))
            {
                return true;
            }

            // 避免一直不校验撑爆内存
            if (msgQueue.size() < CAPACITY)
            {
                msgQueue.add(tmqMsg);
            }
            if (expectedQueue.peek().equals(tmqMsg))
            {
                checkedQueue.add(expectedQueue.poll());
                countDownLatch.countDown();
                return true;
            }
            return true;
        }
    }

    public static boolean check()
    {
        return check(System.out, true);
    }

    public static boolean check(PrintStream os, boolean ifPrintQueue)
    {
        if (!expectedQueue.isEmpty())
        {
            // 输出失败原因
            printText(mHead, mFoot, os, "not received all expectedQueue message");
        }

        if (ifPrintQueue)
        {
            printHistory(mHead, mFoot, os);
        }

        return true;
    }

    public static boolean checkStrict()
    {
        return checkStrict(System.out, true);
    }

    public static boolean checkStrict(OutputStream os, boolean ifPrintQueue)
    {
        if (!expectedQueue.isEmpty())
        {
            // 输出失败原因
            printText(mHead, mFoot, os, "not received all expectedQueue message");
            return false;
        }
        if (msgQueue.size() != checkedQueue.size())
        {
            // 输出失败原因
            printText(mHead, mFoot, os, "size of msgQueue and expectedQueue not equals");
            return false;
        }
        for (int i = 0; i < checkedQueue.size(); i++)
        {
            if (! msgQueue.get(i).equals(checkedQueue.poll()))
            {
                // 输出失败原因
                printText(mHead, mFoot, os, "message sequence in msgQueue not matched message sequence in expectedQueue");
                return false;
            }
        }
        if (ifPrintQueue)
        {
            printHistory(mHead, mFoot, os);
        }
        return true;
    }

    public static void reset() {
        preFilterMsgQueue.clear();
        msgQueue.clear();
        expectedQueue.clear();
        checkedQueue.clear();
        countDownLatch = null;
    }

    /**
     * 有效的消息序列初始化
     * @param tmqMsgList 有效的消息序列
     */
    public static void iCareWhatMsg(TmqMsg...tmqMsgList) {
        if (tmqMsgList == null || tmqMsgList.length <= 0)
        {
            return;
        }

        // 每次iCareWhatMsg后重置消息队列
        reset();

        countDownLatch = new CountDownLatch(tmqMsgList.length);

        for (TmqMsg tmqMsg : tmqMsgList) {
            expectedQueue.add(tmqMsg);
        }
    }

    public static void clearCaredType()
    {
        filterSet.clear();
    }

    /**
     * 有效的消息序列初始化
     * @param msgTypes 有效的消息类型
     */
    public static void iCareWhatType(String...msgTypes) {
        // 每次iCareWhatTpye后重置消息队列
        reset();
        clearCaredType();

        if (msgTypes == null || msgTypes.length <= 0)
        {
            return;
        }


        for (String msgType : msgTypes) {
            filterSet.add(msgType);
        }
    }

    public static boolean await() {
        return await(10);
    }

    public static boolean await(long s) {
        if(countDownLatch==null) {
            return false;
        }

        try {
            countDownLatch.await(s, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void printHistory(String head, String foot, OutputStream os)
    {
        if (null == os)
        {
            return;
        }
        try {
            if (null != head)
            {
                os.write(head.concat(System.getProperty("line.separator")).getBytes());
            }
            for (TmqMsg tmqMsg : preFilterMsgQueue)
            {
                    // 注意换行用\r\n在PrintStream中是无效的
                    os.write((tmqMsg.toString().concat(System.getProperty("line.separator"))).getBytes());
            }
            if (null != foot)
            {
                os.write(foot.concat(System.getProperty("line.separator")).getBytes());
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printText(String head, String foot, OutputStream os, String text)
    {
        if (null == os || null == text)
        {
            return;
        }
        try {
            if (null != head)
            {
                os.write(head.concat(System.getProperty("line.separator")).getBytes());
            }

            os.write(text.concat(System.getProperty("line.separator")).getBytes());

            if (null != foot)
            {
                os.write(foot.concat(System.getProperty("line.separator")).getBytes());
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class TmqMsg {
        String tag;
        String msg;

        public TmqMsg(String tag, String msg)
        {
            this.tag = tag;
            this.msg = msg;
        }

        @Override
        public String toString()
        {
            return tag + "-" + msg;
        }

        @Override
        public boolean equals(Object another)
        {
            if (null == another) return false;
            if (this == another) return true;
            if (this.getClass() != another.getClass()) return false;
            TmqMsg tmqMsg = (TmqMsg)another;
            return tag.equals(tmqMsg.tag) && msg.equals(tmqMsg.msg);
        }

        @Override
        public int hashCode()
        {
            return tag.hashCode() ^ msg.hashCode();
        }
    }

}
