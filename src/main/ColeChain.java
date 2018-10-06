import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

//import com.google.gson.GsonBuilder;

/*
 * Author: Cole Polyak
 * 30 September 2018
 * ColeChain.java
 * The chain of blocks. 
 */

public class ColeChain 
{
	// Our chain.
	public static ArrayList<Block> blockchain = new ArrayList<>();
	
	// Our collection of all transactions on the chain. 
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

	// Problem difficulty for miners.
	public static int difficulty = 3;
	
	// Minimum transaction tolerated on chain.
	public static float minimumTransaction = 0.1f;

	// Two test wallets.
	public static Wallet walletOne;
	public static Wallet walletTwo;

	// The original block.
	public static Transaction genesisTransaction;

	public static void main(String[] args)
	{
		//Adds bouncycastle as a security provider in order to use algorithms.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider

		//Create wallets
		walletOne = new Wallet();
		walletTwo = new Wallet();	
		
		Wallet coinbase = new Wallet();

		// Creates genesis transaction. Sends 100 ColeCoins to walletOne.
		genesisTransaction = new Transaction(coinbase.publickey, walletOne.publickey, 100, null);
		
		// Manually signing genesis block.
		genesisTransaction.generateSignature(coinbase.privatekey);	
		
		// Setting genesis ID.
		genesisTransaction.transactionId = "0"; 
		
		//Adding transaction outputs. 
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId)); 
		
		// Adds genesis transaction to our unsigned transaction outputs. 
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		System.out.println("Creating and Mining Genesis block");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);

		// Testing several blocks.
		Block block1 = new Block(genesis.hash);
		System.out.println("\nwalletOne's balance is: " + walletOne.getBalance());
		System.out.println("\nwalletOne is Attempting to send 65 coins to walletTwo");
		block1.addTransaction(walletOne.sendFunds(walletTwo.publickey, 65));
		addBlock(block1);
		System.out.println("\nwalletOne's balance is: " + walletOne.getBalance());
		System.out.println("walletTwo's balance is: " + walletTwo.getBalance());

		Block block2 = new Block(block1.hash);
		System.out.println("\nwalletOne Attempting to send 1000 coins, more than it has");
		block2.addTransaction(walletOne.sendFunds(walletTwo.publickey, 1000));
		addBlock(block2);
		System.out.println("\nwalletOne's balance is: " + walletOne.getBalance());
		System.out.println("walletTwo's balance is: " + walletTwo.getBalance());

		Block block3 = new Block(block2.hash);
		System.out.println("\nwalletTwo is Attempting to send 20 coins to walletOne");
		block3.addTransaction(walletTwo.sendFunds( walletOne.publickey, 10));
		System.out.println("\nwalletOne's balance is: " + walletOne.getBalance());
		System.out.println("walletTwo's balance is: " + walletTwo.getBalance());

		// Is the chain valid?
		isValid();

	}


	/**
	 * 
	 * @return : boolean determining if the chain is in fact valid.
	 * 
	 * Iterates over entire chain ensuring that the chain hasn't been tampered with.
	 */
	public static boolean isValid()
	{
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');

		// A hashmap to temporarily add the unsigned transaction outputs to.
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
		
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		// Iterates over blockchain starting after genesis.
		for(int i = 1; i < blockchain.size(); ++i)
		{
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);

			// Ensures the recorded and generated hashes match.
			if(!(currentBlock.hash.equals(currentBlock.generateHash())))
			{
				System.out.println("Registed hash and generated hash for block " + currentBlock + " do not match.");
				return false;
			}

			// Ensures previous block's hash and current block's previous hash match.
			if(!(previousBlock.hash.equals(currentBlock.previousHash)))
			{
				System.out.println("Previous hash of block " + currentBlock + " doesn't match.");
				return false;
			}

			// Ensures block has been mined legitimately.
			if(!(currentBlock.hash.substring(0, difficulty).equals(hashTarget)))
			{
				System.out.println(currentBlock + " hasn't been mined");
				return false;
			}

			// Verifies transactions.
			TransactionOutput tempOutput;
			for(int t = 0; t < currentBlock.transactions.size(); ++t)
			{
				Transaction currentTransaction = currentBlock.transactions.get(t);

				// Ensures signature is valid.
				if(!(currentTransaction.verifySignature()))
				{
					System.out.println("Signature on transaction " + t + " is invalid.");
					return false;
				}

				// Ensures transaction makes a change.
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue())
				{
					System.out.println("Inputs are not equal to outputs on Transaction " + t);
					return false;
				}

				// Verifies inputs in transaction
				for(TransactionInput input : currentTransaction.inputs)
				{
					tempOutput = tempUTXOs.get(input.transactionOutputId);

					if(tempOutput == null)
					{
						System.out.println("Referenced input on transaction " + t + " is missing.");
						return false;
					}

					if(input.UTXO.value != tempOutput.value)
					{
						System.out.println("Regerenced input Transaction " + t + " value is invalid");
						return false;
					}

					tempUTXOs.remove(input.transactionOutputId);
				}

				// Adds transaction outputs to our unsigned outputs.
				for(TransactionOutput output : currentTransaction.outputs)
				{
					tempUTXOs.put(output.id, output);
				}

				if(currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) 
				{
					System.out.println("Transaction " + t + " output recipient is not who it should be");
					return false;
				}

				if(currentTransaction.outputs.get(1).recipient != currentTransaction.sender)
				{
					System.out.println("Transaction " + t + " output 'change' is not sender");
					return false;
				}
			}
		}

		System.out.println("Blockchain is valid.");
		return true;
	}

	/**
	 * 
	 * @param newBlock : Block to be added to the chain.
	 * 
	 * Begins by mining the new block before adding it
	 * to the chain.
	 */
	public static void addBlock(Block newBlock) 
	{
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
}
