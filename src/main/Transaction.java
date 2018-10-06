import java.security.*;
import java.util.ArrayList;

/*
 * Author: Cole Polyak
 * 30 September 2018
 * Transaction.java
 * 
 * This class stores all the data relevant to transactions.
 */

public class Transaction 
{
	public String transactionId;
	public PublicKey sender;
	public PublicKey recipient; 
	
	// How many coins.
	public float value;
	
	public byte[] signature;
	
	// The arraylist of the transaction outputs that verify the new transaction.
	public ArrayList<TransactionInput> inputs = new ArrayList<>();
	
	// The generated transaction outputs.
	public ArrayList<TransactionOutput> outputs = new ArrayList<>();
	
	private static int sequence = 0;
	
	/**
	 * 
	 * @param from : sender
	 * @param to : recipient
	 * @param value : number of coins
	 * @param inputs : The inputs for the transaction
	 */
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs)
	{
		sender = from;
		recipient = to;
		this.value = value;
		this.inputs = inputs;
	}

	/**
	 * 
	 * @return : Returns status of transaction. Whether or not it was processed.
	 */
	public boolean processTransaction()
	{
		// Invalid signature check.
		if(verifySignature() == false)
		{
			System.out.println("Transaction signature failed to verify.");
			return false;
		}
		
		// Sets the transaction output object for each input.
		for(TransactionInput i : inputs)
		{
			i.UTXO = ColeChain.UTXOs.get(i.transactionOutputId);
		}
		
		// Transaction is too small for supported minimum.
		if(getInputsValue() < ColeChain.minimumTransaction)
		{
			System.out.println("Transaction inputs too small: " + getInputsValue());
			return false;
		}
		
		float leftOver = getInputsValue() - value;
		
		// Generates the transaction id.
		transactionId = calculateHash();
		
		// Adds the recipient to the transaction output, including coins gained.
		outputs.add(new TransactionOutput(this.recipient, value, transactionId));
		
		// Adds sender to the transaction output, including the coins lost.
		outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));
		
		// Adds the transaction outputs to the macroscopic transaction ledeger.
		for(TransactionOutput o : outputs)
		{
			ColeChain.UTXOs.put(o.id, o);
		}
		
		// Removes the specific transaction input. It's become an output.
		for(TransactionInput i : inputs)
		{
			// Transaction cannot be found. Let's skip it.
			if(i.UTXO == null) continue;
			ColeChain.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	// Gets the total of the transaction inputs.
	public float getInputsValue()
	{
		float total = 0;
		
		for(TransactionInput i : inputs)
		{
			// If the transaction can't be found, skip it.
			if(i.UTXO == null) continue;
			total += i.UTXO.value;
		}
		
		return total;
	}
	
	// Gets the total sum of the transaction outputs.
	public float getOutputsValue()
	{
		float total = 0;
		for(TransactionOutput o : outputs)
		{
			total += o.value;
		}
		
		return total;
	}
	
	/**
	 * 
	 * @return : The SHA256 hash of the transaction.
	 */
	private String calculateHash()
	{
		sequence++;
		
		// Combines sender, recipient, value, and sequence values into SHA256 hash.
		return StringTools.applySHA256(
				StringTools.getStringFromKey(sender) + 
				StringTools.getStringFromKey(recipient) + 
				Float.toString(value) +
				sequence);
	}
	
	/**
	 * 
	 * @param privatekey : privatekey of sender.
	 * 
	 * Generates a valid signature for sender of coins.
	 * Uses signature to validate transaction.
	 */
	public void generateSignature(PrivateKey privatekey)
	{
		String data = StringTools.getStringFromKey(sender) + 
					  StringTools.getStringFromKey(recipient) +
					  Float.toString(value);
		signature = StringTools.applyECDSASig(privatekey, data);
	}
	
	/**
	 * 
	 * @return : legitimacy of signature.
	 * 
	 * Verifies that a signature is correct and was generated
	 * correctly. 
	 */
	public boolean verifySignature()
	{
		String data = StringTools.getStringFromKey(sender) + 
					  StringTools.getStringFromKey(recipient) +
					  Float.toString(value);
		return StringTools.verifyECDSASig(sender, data, signature);
	}
	
}
