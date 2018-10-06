/*
 * Author: Cole Polyak
 * 30 September 2018
 * TransactionInput.java
 * 
 * This class provides functionality for providing
 * transaction inputs when conducting a transaction.
 * 
 * Transaction inputs are simply a collection of 
 * transaction outputs. 
 */

public class TransactionInput 
{
	// Specific output id.
	public String transactionOutputId;
	
	// Our output object.
	public TransactionOutput UTXO;
	
	public TransactionInput(String transactionOutputId)
	{
		this.transactionOutputId = transactionOutputId;
	}
}
