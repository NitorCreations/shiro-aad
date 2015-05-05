package com.nitorcreations.willow.shiro.aad;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Dummy ExecutorService that just runs the Runnable in the current thread synchronously.
 * 
 * @author Mikko Tommila
 */
public class SynchronousExecutorService extends AbstractExecutorService {

  /**
   * The singleton instance.
   */
  public static final ExecutorService INSTANCE = new SynchronousExecutorService();

  private boolean shutdown;

  private SynchronousExecutorService() {
    // Only the singleton can be constructed
  }

  @Override
  public void execute(Runnable command) {
    command.run();
  }

  @Override
  public void shutdown() {
    this.shutdown = true;
  }

  @Override
  public List<Runnable> shutdownNow() {
    return Collections.emptyList();
  }

  @Override
  public boolean isShutdown() {
    return this.shutdown;
  }

  @Override
  public boolean isTerminated() {
    return this.shutdown;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return true;
  }
}
