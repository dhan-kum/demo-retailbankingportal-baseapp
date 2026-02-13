package com.eviden.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

@Entity
public class Transfer {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message="Sender account number is required")
    @Pattern(regexp = "^[0-9]{12}$", message="Sender account number must be 12 digits")
    private String senderAccount;
    
    @NotNull(message="Receiver account number is required")
    @Pattern(regexp = "^[0-9]{12}$", message="Receiver account number must be 12 digits")
    private String receiverAccount;
    
    @NotNull(message="Amount is required")
    @Positive(message="Amount must be positive")
    private Double amount;

    public Transfer(){

    }

    public Transfer(String senderAccount, String receiverAccount, Double amount){
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }


    
}
