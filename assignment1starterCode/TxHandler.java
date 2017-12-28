import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Predicate;
import java.util.Collection;

public class TxHandler {

    private UTXOPool _utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        _utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {

      
        //1) outputsClaimedArePresent 
        for(Transaction.Input i : tx.getInputs()){
            if (!_utxoPool.contains(new UTXO(i.prevTxHash, i.outputIndex)))
                return false;
        }

        int inputCounter = 0;
        //2) allInputSignaturesAreValid
        for( Transaction.Input i : tx.getInputs()){
            if(!verifyInputSignature(tx, i, inputCounter++))
                return false;
        }

        boolean noMultipleUtxoClaims = 
            _utxoPool.getAllUTXO().stream()
            .filter(utxo -> (utxo.getTxHash()).equals(tx.getHash())).count() < 2;
        
        boolean nonNegativeTransactionOutputs = tx.getOutputs().stream().allMatch(o -> o.value >= 0.00);

        double inputSum = tx.getInputs().stream().map(i -> {
                UTXO utxo = new UTXO(i.prevTxHash, i.outputIndex);
                Transaction.Output output = _utxoPool.getTxOutput(utxo);
                return output.value;
            }).reduce(0.00, (a,n) -> a + n);

        double outputSum = tx.getOutputs().stream().map(o -> o.value)
            .reduce(0.00, (a,n) -> a + n);

        boolean sumOfInputValuesGraterOrEqualThenOutputValuesSum = 
            inputSum >= outputSum;
            
        
        return noMultipleUtxoClaims 
            && nonNegativeTransactionOutputs
            && sumOfInputValuesGraterOrEqualThenOutputValuesSum ;
    }

    private boolean verifyInputSignature(Transaction tx, Transaction.Input i, int counter){
        UTXO utxo = new UTXO(i.prevTxHash, i.outputIndex);
        Transaction.Output output = _utxoPool.getTxOutput(utxo);
        byte[] message = tx.getRawDataToSign(counter);
        byte[] inputSignature = i.signature;
        return Crypto.verifySignature(output.address, message, inputSignature);
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        
        ArrayList<Transaction> validTransactions = new ArrayList<Transaction>();

        for(Transaction t : possibleTxs){
            if(isValidTx(t)) {

                for(Transaction.Input i : t.getInputs())
                    _utxoPool.removeUTXO(new UTXO(i.prevTxHash, i.outputIndex));
                
                int outputsCounter = 0;
                for(Transaction.Output o : t.getOutputs())
                    _utxoPool.addUTXO(new UTXO(t.getHash(), outputsCounter++), o);

                validTransactions.add(t);
            }
        }

        Transaction[] txs = new Transaction[validTransactions.size()];
        return validTransactions.toArray(txs);

    }

}
