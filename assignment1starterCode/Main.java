import java.security.*;

import sun.security.provider.SHA2.SHA256;

public class Main {

    public static void main( String[] args ) {

        Tuple<PublicKey,PrivateKey> keys = generatePublicAndPrivateKeys();
        PublicKey pk = keys.x;
        Transaction.Output to = new Transaction.Output(20.00, pk);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] previousHash = digest.digest("test".getBytes(StandardCharsets.UTF_8));
        Transaction.Input ti = new Transaction.Input(previousHash, 0);
        UTXO utxo = new UTXO(previousHash, 0);
        UTXOPool utxoPool = new UTXOPool();
        UTXOPool.addUTXO(utxo, to);

        TxHandler tx = new TxHandler(utxoPool);
        Transaction transaction = new Transaction();
        transaction.addInput(ti.prevTxHash, ti.outputIndex);
        transaction.addOutput(to.value, to.address);

        tx.isValidTx(transaction);

        System.out.println( "Hello World!" );
        System.exit( 0 ); //success
    }

    private class Tuple<X, Y> { 
        public final X x; 
        public final Y y; 
        public Tuple(X x, Y y) { 
          this.x = x; 
          this.y = y; 
        } 
      } 

    private static Tuple<PublicKey,PrivateKey> generatePublicAndPrivateKeys()
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();
        return new Tuple(pub,priv);
    }
}