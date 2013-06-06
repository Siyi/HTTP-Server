package server;

import java.util.Vector;
import java.util.concurrent.locks.*;

public final class StaticThreadPool
{
	private static StaticThreadPool instance = null;
	private boolean debug = false;
	private WaitingRunnableQueue queue = null;
	private Vector<ThreadPoolThread> availableThreads = null;
	
	private static ReentrantLock lock = new ReentrantLock();
	
	public static StaticThreadPool getInstance(int maxThreadNum, boolean debug)
	{
		lock.lock();
		lock.tryLock();
		if (instance == null)
			instance = new StaticThreadPool(maxThreadNum, debug);
		lock.unlock();
		
		return instance;
	}

	private StaticThreadPool(int maxThreadNum, boolean debug)
	{
		this.debug = debug;
		queue = new WaitingRunnableQueue(this);
		availableThreads = new Vector<ThreadPoolThread>();
		for(int i = 0; i < maxThreadNum; i++)
		{
			ThreadPoolThread th = new ThreadPoolThread(this, queue, i);
			availableThreads.add(th);
			th.start();
		}
	}

	public void execute(Runnable runnable)
	{
		queue.put(runnable);
	}
	
	public void shutdown()
	{
		for(int i = 0; i < availableThreads.size(); i++)					
			availableThreads.get(i).setStopped();	
	}

	public int getWaitingRunnableQueueSize()
	{
		try
		{
			queue.queueLock.lock();
			return queue.size();
		}
		finally
		{
			queue.queueLock.unlock();
		}
	}

	public int getThreadPoolSize()
	{
		return availableThreads.size();
	}


	private class WaitingRunnableQueue
	{
		private Vector<Runnable> runnables = new Vector<Runnable>();
		private StaticThreadPool pool;
		private ReentrantLock queueLock;
		private Condition runnablesAvailable;

		public WaitingRunnableQueue(StaticThreadPool pool)
		{
			this.pool = pool;
			queueLock = new ReentrantLock();
			runnablesAvailable = queueLock.newCondition();
		}

		public int size()
		{
			return runnables.size();
		}

		public void put(Runnable obj)
		{
			queueLock.lock();
			try
			{
				runnables.add(obj);
				if(pool.debug==true) System.out.println("A runnable queued.");
				runnablesAvailable.signalAll();
			}
			finally
			{
				queueLock.unlock();
			}
		}

		public Runnable get()
		{
			queueLock.lock();
			try
			{
				while(runnables.isEmpty())
				{
					if(pool.debug==true) System.out.println("Waiting for a runnable");
					runnablesAvailable.await();
				}
				if(pool.debug==true) System.out.println("A runnable dequeued.");
				return runnables.remove(0);
			}
			catch(InterruptedException ex)
			{
				return null;
			}
			finally
			{
				queueLock.unlock();
			}
		}
	}


	private class ThreadPoolThread extends Thread
	{
		private StaticThreadPool pool;
		private WaitingRunnableQueue queue;
		private int id;
		
		private boolean stopped = false;

		public ThreadPoolThread(StaticThreadPool pool, WaitingRunnableQueue queue)
		{
			this(pool, queue, -1);
		}

		public ThreadPoolThread(StaticThreadPool pool, WaitingRunnableQueue queue, int id)
		{
			this.pool = pool;
			this.queue = queue;
			this.id = id;
		}
		
		public void setStopped()
		{
			stopped = true;
			this.interrupt();
		}

		public void run()
		{
			queue.queueLock.lock();
			if(pool.debug==true) System.out.println("Thread " + id + " starts.");
			while(true && !stopped)
			{
				Runnable runnable = queue.get();
				if(runnable==null)
				{
					if(pool.debug==true)
						System.out.println("Thread " + this.id + " is being stopped due to an InterruptedException.");
					continue;
				}
				else
				{
					if(pool.debug==true) System.out.println("Thread " + id + " executes a runnable.");
					runnable.run();
					if(pool.debug == true)
						System.out.println("ThreadPoolThread " + id + " finishes executing a runnable.");
				}
			}
			queue.queueLock.unlock();
		}
	}
	
	
}

