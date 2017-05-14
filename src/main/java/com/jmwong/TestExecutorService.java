package com.jmwong;

import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * Created by jamin on 5/14/17.
 */
public class TestExecutorService extends ThreadPoolExecutor {
    private static Logger logger =Logger.getLogger(TestExecutorService.class);

    public TestExecutorService() {
        super(0, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void execute(Runnable command) {
        logger.info("testexec " + command);
        super.execute(command);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        logger.trace("Submitting" + task);
        return super.submit(task, result);
    }
}
