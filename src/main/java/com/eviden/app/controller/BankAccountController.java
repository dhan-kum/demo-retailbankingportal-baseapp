package com.eviden.app.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import com.eviden.app.dto.TransferDTO;
import com.eviden.app.entity.BankAccount;
import com.eviden.app.entity.Transfer;
import com.eviden.app.service.ConsumerService;
import com.eviden.app.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipOutputStream;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.eviden.app.service.TransferServiceImpl;
import com.eviden.app.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/bankaccounts")
public class BankAccountController {

    private static final Logger logger = LoggerFactory.getLogger(BankAccountController.class);

    @Autowired
    private TransferServiceImpl transferService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    FileService fileService;

    @Autowired
    ConsumerService consumerService;

    private static final String queueName = "my_queue";

    @GetMapping("/")
    public List<BankAccount> getAllAccounts() {
        logger.info("Inside getAllAccounts() method");
        return bankAccountRepository.findAll();
    }

    @GetMapping("/{id}")
    public BankAccount getBankAccount(@PathVariable @Pattern(regexp = "^[0-9]{12}$", message="Invalid account number format") String id) {
        BankAccount account = bankAccountRepository.findByAccountNumber(id);
        // SECURITY: Don't log full account details
        logger.info("Account lookup requested for account ending in: {}", 
            id.length() >= 4 ? "****" + id.substring(id.length() - 4) : "****");
        return account;
    }

    // SECURITY: This endpoint has been disabled due to log injection vulnerability
    // Logging should be done through proper audit mechanisms, not user-controlled inputs
    /*
    @PostMapping("/logmessage")
    public void saveLogs(@RequestParam String logmsg) {
        logger.info(logmsg);
    }
    */

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@Valid @RequestBody TransferDTO transferDTO) {
        Transfer transfer = new Transfer(transferDTO.getSenderAccount(), transferDTO.getReceiverAccount(), transferDTO.getAmount());
        try {
            Map<String, Optional<BankAccount>> backAccountCollection = transferService.transfer(transfer);
            if (backAccountCollection != null) {
                Optional<BankAccount> receiverOpt = backAccountCollection.get("receiverAccount");
                Optional<BankAccount> senderOpt = backAccountCollection.get("senderAccount");
                if (receiverOpt != null && receiverOpt.isPresent() && senderOpt != null && senderOpt.isPresent()) {
                    BankAccount receiver = receiverOpt.get();
                    BankAccount sender = senderOpt.get();
                    if (Integer.parseInt(transfer.getReceiverAccount()) == Integer.parseInt(receiver.getAccountNumber())
                            && Integer.parseInt(transfer.getSenderAccount()) == Integer.parseInt(sender.getAccountNumber())) {
                        logger.info("Info.{}", "Transfer was successful.");
                        return ResponseEntity.ok("Transfer was successful.");
                    } else {
                        logger.info("Info.{}", "Transfer was not successful.");
                    }
                } else {
                    logger.info("Info.{}", "Transfer was not successful.");
                }
            } else {
                logger.info("Info.{}", "Transfer was not successful.");
            }
            logger.info("Transfer money...");
        } catch (Exception e) {
            logger.error("Transfer failed", e);
        }
        return ResponseEntity.badRequest().build();
    }

    // SECURITY: This endpoint has been disabled due to path traversal vulnerability
    // File operations should be properly sandboxed and validated
    /*
    @GetMapping("/createzip")
    public String createZip(@PathVariable String sourceDir, @PathVariable String zipFile) {
        String msg = null;
        try (FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos)) {
            File directory = new File(sourceDir);
            fileService.addFilesToZip(directory, directory.getName(), zos);
            msg = "Successfully";
        } catch (Exception e) {
            msg = "Error in generating errorMsg-" + e.getMessage();
            logger.error("Error in generating zip", e);
        }
        return msg;
    }
    */

    @GetMapping("/connect")
    public void send() throws Exception {
        consumerService.connect(queueName);
    }
}
