package ssimwave.job;

/**
 * Work items should implement this interface to have JobPublisher
 */
public interface Work
{
	/**
	 * Blocking function call that performs necessary work.
	 * @throws Exception if an unrecoverable error occurs
	 */
	public void doWork() throws Exception;

	/**
	 * Called and set by JobPublisher so that the items can be uniquely
	 *	identified.
	 * @param id to be set by JobPublisher
	 */
	public void setId(int id);

	/**
	 * The identifier of this object is best if assigned by the JobPublisher.
	 * @return identifier for Work item as set by the JobPublisher
	 */
	public int getId();
}