package myos.manager.files;

import myos.utils.ThreadPoolUtil;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

/**
 * 磁盘申请类
 *
 * @author WTDYang
 * @date 2022/12/09
 */
@SuppressWarnings("all")
public class DiskRequest {
    ArrayBlockingQueue<Request> requests;
    List<Request> runningRequests;

    public void init() throws InterruptedException {
        requests = new ArrayBlockingQueue<>(16);
        runningRequests = new Vector<>();
        ThreadPoolUtil.getInstance().execute(SCAN());
    }

    /**
     * 等电梯
     *
     * @param request 请求
     */
    public void waitForElevator(Request request){
        requests.add(request);
    }

    /**
     * 上电梯
     */
    public boolean getOnElevator(){
        //如果是空就一直自旋
        while (requests.isEmpty()){return false;}
        List<Request> list = new Vector<>();
        while (!requests.isEmpty()){
            try {
               list.add(requests.take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        runningRequests.clear();
        runningRequests.addAll(
        list.stream()
                .sorted(((o1, o2) -> (int) (o1.block - o2.block)))
                .collect(Collectors.toList()));

        return true;
    }
    public void handler(Request request) throws IOException {
        request.method.ioMethod();
    }

    public Runnable SCAN(){
        return new Runnable() {
            @Override
            public void run() {
                while (true) {
                    runningRequests.forEach(item -> {
                        try {
                            item.method.ioMethod();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    while (!getOnElevator()) {}
                }
            }
        };
    }

}
class Request{
    Method method;
    long block;
    public Request(int block,Method method) {
        this.method = method;
        this.block = block;
    }
}

interface Method{
    /**
     * 要执行的io方法
     */
    public void ioMethod() throws IOException;
}
