package com.eviden.app.controller;

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
import java.util.logging.Level;
//import java.util.logging.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/bankaccounts")
public class BankAccountController {

    //Logger logger = Logger.getLogger(BankAccountController.class.getName());
    Logger logger = LoggerFactory.getLogger(BankAccountController.class);

    @Autowired
    private TransferServiceImpl transferService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    FileService fileService;

    @Autowired
    ConsumerService consumerService;

    private final String QUEUE_NAME = "my_queue";

    @GetMapping("/")
    public List<BankAccount> getAllAccounts() {
        logger.info("Inside getAllAccounts() method");
        return bankAccountRepository.findAll();
    }

    @GetMapping("/{id}")
    public BankAccount getBankAccount(@PathVariable String id) {
        logger.info("Inside getBankAccount() method -" + bankAccountRepository.findByAccountNumber(id));
        return bankAccountRepository.findByAccountNumber(id);
    }

    @PostMapping("/logmessage")
    public void saveLogs(@RequestParam String logmsg) {
        logger.info(logmsg);
        //logger.error("Uncaught [object HTMLLinkElement]. Unable to load URL");
        //logger.error("ErroMsg.{}.stacktrace.{}","Uncaught [object HTMLLinkElement]. Unable to load URL", "Unable to load URL");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody Transfer transfer) {
        // Call the transfer service
        try {
            Map<String, Optional<BankAccount>> backAccountCollection = transferService.transfer(transfer);
            if (backAccountCollection != null && backAccountCollection.get("receiverAccount").isPresent() && backAccountCollection.get("senderAccount").isPresent()) {
                if (Integer.parseInt(transfer.getReceiverAccount()) == Integer.parseInt(backAccountCollection.get("receiverAccount").get().getAccountNumber()) && Integer.parseInt(transfer.getSenderAccount()) == Integer.parseInt(backAccountCollection.get("senderAccount").get().getAccountNumber())) {
                    logger.info("Info.{}", "Transfer was successful.");
                    return ResponseEntity.ok("Transfer was successful.");
                } else {
                    logger.info("Info.{}", "Transfer was not successful.");
                }
            } else {
                logger.info("Info.{}", "Transfer was not successful.");
            }
            System.out.println("Transfer money...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

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
            e.printStackTrace();
        }
        return msg;
    }

    @GetMapping("/connect")
    public void send() throws Exception {
        consumerService.connect(QUEUE_NAME);
    }
}
