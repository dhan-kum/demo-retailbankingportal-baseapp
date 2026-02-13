package com.eviden.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

public class TransferDTO {

    @NotNull(message="Sender account number is required")
    @Pattern(regexp = "^[0-9]{12}$", message="Sender account number must be 12 digits")
    private String senderAccount;
    
    @NotNull(message="Receiver account number is required")
    @Pattern(regexp = "^[0-9]{12}$", message="Receiver account number must be 12 digits")
    private String receiverAccount;
    
    @NotNull(message="Amount is required")
    @Positive(message="Amount must be positive")
    private Double amount;

    public TransferDTO() {
    }

    public TransferDTO(String senderAccount, String receiverAccount, Double amount) {
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(String receiverAccount) {
        this.receiverAccount = receiverAccount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
