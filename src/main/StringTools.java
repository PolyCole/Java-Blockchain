import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;

/*
 * Author: Cole Polyak
 * 30 September 2018
 * StringTools.java
 * This class provides several methods to convert strings into hashes.
 */

public class StringTools
{
	/**
	 * @param input : The string needing to be converted to a signature.
	 * @return : The string after it has had the SHA256 algorithm applied.
	 *
	 * This method takes in a string and returns the generated SHA256 hash.
	 */
	public static String applySHA256(String input)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			// Digests our input into a SHA256 hash.
			byte[] hash = md.digest(input.getBytes("UTF-8"));

			StringBuilder hexString = new StringBuilder();

			for(int i = 0; i < hash.length; ++i)
			{
				// Converts each byte to hexidecimal.
				String hex = Integer.toHexString(0xff & hash[i]);

				if(hex.length() == 1) hexString.append(0);
				
				// Adding the resulting hash into the StringBuilder.
				hexString.append(hex);
			}

			return hexString.toString();
		}
		catch(Exception e)
		{
			System.err.println("Ran into exception... Exiting.");
			System.exit(1);
		}

		return "";
	}

	/**
	 * 
	 * @param difficulty: level of difficulty (number of consecutive zeroes)
	 * @return : Target string.
	 * 
	 * This method takes in the difficulty level and returns the target first x characters
	 * in the resulting hash.
	 */
	public static String getDifficultyString(int difficulty) 
	{
		return new String(new char[difficulty]).replace('\0', '0');
	}

	/**
	 * 
	 * @param privatekey : senders private key
	 * @param input : relevant information for transaction.
	 * @return : byte array
	 * 
	 * This method digitally signs the input using ECDSA. 
	 */
	public static byte[] applyECDSASig(PrivateKey privatekey, String input)
	{
		Signature dsa;
		byte[] output = new byte[0];

		try
		{
			dsa = Signature.getInstance("ECDSA", "BC");
			
			// Initializes bytes to be securely signed.
			dsa.initSign(privatekey);
			byte[] strByte = input.getBytes();
			
			// Updates bytes with privatekey to continue signing
			dsa.update(strByte);
			
			// Signs data.
			byte[] realSig = dsa.sign();
			
			output = realSig;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}

		return output;
	}

	/**
	 * 
	 * @param publickey : sender's publickey.
	 * @param data : Relevant transaction information.
	 * @param signature : Signature to be verified.
	 * @return : the validity of the ECDSA Signature.
	 * 
	 * This method verifies the validity of a given ECDSA Signature given
	 * relevant transaction data.
	 */
	public static boolean verifyECDSASig(PublicKey publickey, String data, byte[] signature)
	{
		try
		{
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			
			// Initializes the verification process.
			ecdsaVerify.initVerify(publickey);
			
			// Updates bytes after publickey has been retrieved.
			ecdsaVerify.update(data.getBytes());
			
			// Tries to verify signature.
			return ecdsaVerify.verify(signature);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param transactions : Array of transactions
	 * @return : returns a merkle root to be used on the tree.
	 * 
	 * This method, when completed, will output a merkle root to
	 * be added to the hash tree. Currently, this is a proof of concept
	 * implementation. Further building to come.
	 */
	public static String getMerkleRoot(ArrayList<Transaction> transactions)
	{
		int count = transactions.size();
		ArrayList<String> previousTreeLayer = new ArrayList<>();
		for(Transaction transaction : transactions)
		{
			previousTreeLayer.add(transaction.transactionId);
		}

		ArrayList<String> treeLayer = previousTreeLayer;

		while(count > 1)
		{
			treeLayer = new ArrayList<>();
			for(int i = 1; i < previousTreeLayer.size(); ++i)
			{
				treeLayer.add(StringTools.applySHA256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
			}

			count = treeLayer.size();
			previousTreeLayer = treeLayer;
		}

		String merkleRoot;
		if(treeLayer.size() == 1) merkleRoot = treeLayer.get(0);
		else merkleRoot = "";

		return merkleRoot;
	}

	/**
	 * 
	 * @param key : key from which we'll grab the key.
	 * @return : returns the given key converted to string.
	 */
	public static String getStringFromKey(Key key)
	{
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
}