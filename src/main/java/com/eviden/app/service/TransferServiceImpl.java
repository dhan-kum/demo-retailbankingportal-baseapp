package com.eviden.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.eviden.app.repository.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import com.eviden.app.entity.BankAccount;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.eviden.app.entity.Transfer;
@Service
@Qualifier("TransferService")
public class TransferServiceImpl  {
    
    //@Autowired
    //private AccountRepository accountRepository;
	private static final Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);
    private BankAccountRepository bankAccountRepository;

    public TransferServiceImpl(BankAccountRepository bankAccountRepository){
        this.bankAccountRepository = bankAccountRepository;
    }
    public Map<String, Optional<BankAccount>> transfer(Transfer transfer)throws Exception {
        // Check if both accounts exist
        try
        {
        	Optional<BankAccount> senderAccount = Optional.of(bankAccountRepository.findByAccountNumber(transfer.getSenderAccount()));       
            Optional<BankAccount> receiverAccount = Optional.of(bankAccountRepository.findByAccountNumber(transfer.getReceiverAccount()));
        if (senderAccount.isPresent() && receiverAccount.isPresent()) {
            // Check if sender has enough balance
            if (senderAccount.get().getBalance() > transfer.getAmount()) {
                // Perform the transaction
                senderAccount.get().setBalance(senderAccount.get().getBalance() - transfer.getAmount());
                receiverAccount.get().setBalance(receiverAccount.get().getBalance() + transfer.getAmount());
                bankAccountRepository.save(senderAccount.get());
                bankAccountRepository.save(receiverAccount.get());
                Map<String, Optional<BankAccount>> accountMap = new HashMap<>();
                accountMap.put("senderAccount", senderAccount);
                accountMap.put("receiverAccount", receiverAccount);
                return accountMap;
            }
        }
        }catch(Exception exc) {
            logger.error("ErroMsg.{}.stacktrace.{}","Account number is incorrect", exc.getStackTrace());
            throw exc;
        }
        return null;
    }
}
