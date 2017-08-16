package ssimwave.job.impl;

import java.util.TreeMap;

import ssimwave.job.Work;

public class WorkTree
{
	private TreeMap<Long,Work> tree;

	public WorkTree()
	{
		tree = new TreeMap<Long,Work>();
	}

	/**
	 * Enqueues work
	 * @param work work to enqueue
	 * @throws IllegalArgumentException if work is null
	 */
	public synchronized void put(long key, Work work)
	{
		tree.put(key, work);
	}

	/**
	 * Remove largest key value work item.
	 * @return largest key value work item
	 */
	public synchronized Work removeHighest()
	{
		if (isEmpty()) return null;
		return tree.remove(tree.lastKey());
	}

	/**
	 * Searches and removes work item provided.
	 * @param work the work intended to be found
	 * @return true parameter if found, false if not found
	 * @throws IllegalArgumentException if work is null
	 */
	public synchronized boolean remove(long key)
	{
		return tree.remove(key) != null;
	}

	/**
	 * @return true if queue is empty
	 */
	public synchronized boolean isEmpty()
	{
		return tree.size() == 0;
	}
}