import java.security.PublicKey;

/*
 * Author: Cole Polyak
 * 30 September 2018
 * TransactionOutput.java
 * 
 * Transaction outputs are generated once the transaction has
 * been verified and completed. They contain references
 * of which coins were sent to which address.
 */

public class TransactionOutput 
{
	public String id;
	
	// Wallet address recipient.
	public PublicKey recipient;
	
	public float value;
	
	// The transaction that came before. 
	public String parentTransactionId;
	
	
	public TransactionOutput(PublicKey recipient, float value, String parentTransactionId)
	{
		this.recipient = recipient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		
		// Generates an SHA256 hash of the important data points.
		id = StringTools.applySHA256(
				StringTools.getStringFromKey(recipient) + 
				Float.toString(value) + 
				parentTransactionId);
		
	}
	
	// Checks if the coins are owned by your address.
	public boolean isMine(PublicKey publickey)
	{
		return publickey == recipient;
	}
}
