import java.security.MessageDigest;
import java.security.PublicKey;

import sun.security.provider.SHA2.SHA256;

public class Main {

    public static void main( String[] args ) {

        PublicKey pk = new PublicKey();
        Transaction.Output to = new Transaction.Output(20.00, pk);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] previousHash = digest.digest("test".getBytes(StandardCharsets.UTF_8));
        Transaction.Input ti = new Transaction.Input(previousHash, 0);
        UTXOPool utxoPool = new UTXOPool();
        utxoPool.addUTXO(new UTXO(previousHash, 0), to);

        TxHandler tx = new TxHandler(utxoPool);
        Transaction transaction = new Transaction();
        transaction.addInput(ti.prevTxHash, ti.outputIndex);
        transaction.addOutput(to.value, to.address);

        tx.isValidTx(transaction);

        System.out.println( "Hello World!" );
        System.exit( 0 ); //success
    }
}