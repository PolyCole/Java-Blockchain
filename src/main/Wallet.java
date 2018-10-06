import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * Author: Cole Polyak
 * 30 September 2018
 * Wallet.java
 * 
 * This class creates wallets for users on the blockchain.
 */

public class Wallet 
{
	// User's personal private key.
	public PrivateKey privatekey;
	
	// The public "address"(key) for the wallet.
	public PublicKey publickey;
	
	// A record of the unsigned transaction outputs assocaited with this wallet.
	public HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
	
	public Wallet()
	{
		generateKeyPair();
	}
	
	/**
	 * Generates a pair of keys using the Elliptic Curve KeyPair cryptography as
	 * provided by bouncyCastle.
	 */
	public void generateKeyPair()
	{
		try
		{
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			
			// Generating key pair.
			keyGen.initialize(ecSpec, random);
			KeyPair keyPair = keyGen.generateKeyPair();
			
			privatekey = keyPair.getPrivate();
			publickey = keyPair.getPublic();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 
	 * @return : The current balance of the wallet.
	 * 
	 * This method sums the coins owned by the wallet.
	 */
	public float getBalance()
	{
		float total = 0;
		
		for(Map.Entry<String, TransactionOutput> item : ColeChain.UTXOs.entrySet())
		{
			TransactionOutput UTXO = item.getValue();
			
			// Verifies coin is owned by given wallet.
			if(UTXO.isMine(publickey))
			{
				UTXOs.put(UTXO.id, UTXO);
				total += UTXO.value;
			}
		}
		
		return total;
	}
	
	/**
	 * 
	 * @param _recipient : Wallet publickey that will receive the coins.
	 * @param value : how many coins to be transfered.
	 * @return : Returns a transaction object to be included on block.
	 * 
	 * When transaction is valid, this method sends a certain number of coins
	 * to another wallet address.
	 */
	public Transaction sendFunds(PublicKey _recipient, float value)
	{
		// Insufficient fund check.
		if(getBalance() < value)
		{
			System.out.println("Insufficient funds. Transaction aborted.");
			return null;
		}
		
		ArrayList<TransactionInput> inputs = new ArrayList<>();
		
		float total = 0;
		
		// Returns set so that we can iterate over the map.
		for(Map.Entry<String, TransactionOutput> item : UTXOs.entrySet())
		{
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			
			// Adds the new transaction input.
			inputs.add(new TransactionInput(UTXO.id));
			
			if(total > value) break;
		}
		
		// Creates the new transaction.
		Transaction newTransaction = new Transaction(publickey, _recipient, value, inputs);
		
		// Generates the signature for the new transaction and signs it. 
		newTransaction.generateSignature(privatekey);
		
		for(TransactionInput input : inputs)
		{
			UTXOs.remove(input.transactionOutputId);
		}
		
		return newTransaction;
	}
}
