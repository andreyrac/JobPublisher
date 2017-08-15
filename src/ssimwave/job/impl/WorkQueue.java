package ssimwave.job.impl;

import ssimwave.job.Work;

/**
 * Synchronized queue for Work objects.
 */
public class WorkQueue
{
	private QueueNode head;
	private QueueNode tail;

	private class QueueNode
	{
		QueueNode next;
		Work work;

		QueueNode(Work work)
		{
			if (work == null)
			{
				throw new IllegalArgumentException("work is null");
			}
			next = null;
			this.work = work;
		}
	}

	/**
	 * Create empty work queue
	 */
	public WorkQueue()
	{
		head = tail = null;
	}

	/**
	 * Enqueues work
	 * @param work work to enqueue
	 * @throws IllegalArgumentException if work is null
	 */
	public void enqueue(Work work)
	{
		QueueNode node = new QueueNode(work);
		synchronized (this)
		{
			if (head == null)
			{
				head = tail = node;
			}
			else
			{
				tail = tail.next = node;
			}
		}
	}

	/**
	 * Enqueues work at the top of the queue so that it is dequeued next.
	 * @param work work to enqueue at the head of the queue
	 * @throws IllegalArgumentException if work is null
	 */
	public void enqueueFirst(Work work)
	{
		QueueNode node = new QueueNode(work);
		synchronized (this)
		{
			if (head == null)
			{
				head = tail = node;
			}
			else
			{
				node.next = head;
				head = node;
			}
		}
	}

	/**
	 * Dequeue oldest work item which is then returned.
	 * @return oldest work item in queue, null if empty
	 */
	public synchronized Work dequeue()
	{
		if (head == null) return null;
		Work work = head.work;
		if (head == tail) tail = null; // extracting last in the queue
		head = head.next;
		return work;
	}

	/**
	 * Searches and dequeues work item provided.
	 * @param work the work intended to be found
	 * @return true parameter if found, false if not found
	 * @throws IllegalArgumentException if work is null
	 */
	public synchronized boolean dequeue(Work work)
	{
		if (work == null) throw new IllegalArgumentException("work is null");
		// check if empty
		if (head == null) return false;

		// check if head
		if (head.work.getId() == work.getId())
		{
			// handle if this is the only item in the queue
			if (head == tail) tail = null;
			head = head.next;
			return true;
		}

		// iterate through queue
		QueueNode parent = head;
		QueueNode node = parent.next;
		for (;;)
		{
			if (node == null) break; // reached end of queue
			if (node.work.getId() == work.getId())
			{
				// handle if this is the final item in the queue
				if (node == tail) tail = parent;
				parent.next = node.next;
				return true;
			}
			parent = node;
			node = node.next;
		}

		return false;
	}

	/**
	 * @return true if queue is empty
	 */
	public synchronized boolean isEmpty()
	{
		return head == null;
	}
}