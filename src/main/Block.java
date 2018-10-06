import java.util.ArrayList;
import java.util.Date;

/*
 * Author: Cole Polyak
 * 30 September 2018
 * Block.java
 * Creates blocks to be used on the chain.
 */

public class Block
{
	// Current block hash
	public String hash;
	
	// Hash of previous block
	public String previousHash;
	
	// Functionality forthcoming. Currently a WIP.
	public String merkleRoot;
	
	// Transactions that are being included in the block.
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	
	// Milliseconds since 1/1/1970
	private long timeStamp;
	
	// Added into mining calculations.
	private int magicNumber;

	/**
	 * 
	 * @param previousHash : Links created block to previous block.
	 */
	public Block(String previousHash)
	{
		this.previousHash = previousHash;
		timeStamp = new Date().getTime();
		hash = generateHash();
	}

	/**
	 * 
	 * @return : Generates the hash for the current block.
	 * 
	 * Combines previous hash, timeStamp, magicNumber, and Merkleroot
	 * into SHA256 Hash.
	 */
	public String generateHash()
	{
		String calculatedHash = StringTools.applySHA256(
			previousHash +  
			Long.toString(timeStamp) +
			Integer.toString(magicNumber) + 
			merkleRoot);
			
		return calculatedHash;
	}
	
	/**
	 * 
	 * @param difficulty : How difficult the hash is to solve.
	 * 
	 * Allows miners to build out the chain. 
	 */
	public void mineBlock(int difficulty)
	{
		/*
		 * So, we have our blockchain which is essentially a 
		 * hash chain. However, for scalability purposes
		 * we want to be able to store thousands of transactions
		 * on the chain. That quickly becomes unsustainable.
		 * 
		 * Enter merkle trees. Merkle trees are a type of tree
		 * called a hash tree. They basically exist in a tree
		 * with a branching factor of 2. Each root contains
		 * the hash of the lables of the children. Each leaf
		 * contains a data block.
		 * 
		 * tl;dr : They make the chain scalable. 
		 * 
		 * **Functionalty forthcoming. Currently a WIP**
		 */
		merkleRoot = StringTools.getMerkleRoot(transactions);
		
		// Determines how many zeroes the target hash must have.
		String target = StringTools.getDifficultyString(difficulty);
		
		// Iterates till a valid hash is discovered.
		while(!hash.substring(0, difficulty).equals(target))
		{
			magicNumber++;
			hash = generateHash();
		}
		
		System.out.println("Block mined: " + hash);
	}
	
	/**
	 * 
	 * @param transaction : The transaction being added to the block.
	 * @return : whether or not the transaction was successfully added.
	 * 
	 * Adds transactions to be included with the block.
	 */
	public boolean addTransaction(Transaction transaction)
	{
		// Null case.
		if(transaction == null) return false;
		
		// For all blocks other than the genesis block.
		if(!("0".equals(previousHash)))
		{
			// Ensures the transaction works
			if(transaction.processTransaction() != true)
			{
				System.out.println("Transaction failed to process. Discarded");
				return false;
			}
		}
		
		transactions.add(transaction);
		System.out.println("Transaction successfully added to block");
		return true;
	}
}