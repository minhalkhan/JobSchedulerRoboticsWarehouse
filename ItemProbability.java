package filehandling;

/**
 * @author MinhalKhan
 */
public class ItemProbability {

	/*
	 * P(ItemID | 1)
	 */
	public double ONE;
	/*
	 * P(ItemID | 0)
	 */
	public double ZERO;

	
	public ItemProbability(double ONE, double ZERO) {
		this.ONE = ONE;
		this.ZERO = ZERO;
	}
	
}
