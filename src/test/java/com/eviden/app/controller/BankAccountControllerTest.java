package com.eviden.app.controller;

import com.eviden.app.entity.BankAccount;
import com.eviden.app.entity.Transfer;
import com.eviden.app.repository.BankAccountRepository;
import com.eviden.app.service.ConsumerService;
import com.eviden.app.service.FileService;
import com.eviden.app.service.TransferServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankAccountController.class)
class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferServiceImpl transferService;

    @MockBean
    private BankAccountRepository bankAccountRepository;

    @MockBean
    private FileService fileService;

    @MockBean
    private ConsumerService consumerService;

    @Autowired
    private ObjectMapper objectMapper;

    private BankAccount savingsAccount;
    private BankAccount checkingAccount;

    @BeforeEach
    void setUp() {
        savingsAccount = new BankAccount("10001", "John Doe", 52000.0, "Savings");
        savingsAccount.setId(1L);

        checkingAccount = new BankAccount("10002", "John Doe", 7500.0, "Checking");
        checkingAccount.setId(2L);
    }

    @Test
    void getAllAccounts_shouldReturnAllAccounts() throws Exception {
        List<BankAccount> accounts = Arrays.asList(savingsAccount, checkingAccount);
        when(bankAccountRepository.findAll()).thenReturn(accounts);

        mockMvc.perform(get("/api/bankaccounts/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].accountNumber", is("10001")))
                .andExpect(jsonPath("$[0].accountName", is("John Doe")))
                .andExpect(jsonPath("$[0].balance", is(52000.0)))
                .andExpect(jsonPath("$[0].type", is("Savings")))
                .andExpect(jsonPath("$[1].accountNumber", is("10002")))
                .andExpect(jsonPath("$[1].type", is("Checking")));

        verify(bankAccountRepository, times(1)).findAll();
    }

    @Test
    void getAllAccounts_shouldReturnEmptyList() throws Exception {
        when(bankAccountRepository.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/bankaccounts/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bankAccountRepository, times(1)).findAll();
    }

    @Test
    void getBankAccount_shouldReturnAccountById() throws Exception {
        when(bankAccountRepository.findByAccountNumber("10001")).thenReturn(savingsAccount);

        mockMvc.perform(get("/api/bankaccounts/10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber", is("10001")))
                .andExpect(jsonPath("$.accountName", is("John Doe")))
                .andExpect(jsonPath("$.balance", is(52000.0)))
                .andExpect(jsonPath("$.type", is("Savings")));

        verify(bankAccountRepository, times(2)).findByAccountNumber("10001");
    }

    @Test
    void getBankAccount_shouldReturnNullForNonExistentAccount() throws Exception {
        when(bankAccountRepository.findByAccountNumber("99999")).thenReturn(null);

        mockMvc.perform(get("/api/bankaccounts/99999"))
                .andExpect(status().isOk());

        verify(bankAccountRepository, times(2)).findByAccountNumber("99999");
    }

    @Test
    void transfer_shouldReturnOkOnSuccessfulTransfer() throws Exception {
        Transfer transfer = new Transfer("10001", "10002", 500.0);

        BankAccount updatedSender = new BankAccount("10001", "John Doe", 51500.0, "Savings");
        BankAccount updatedReceiver = new BankAccount("10002", "John Doe", 8000.0, "Checking");

        Map<String, Optional<BankAccount>> resultMap = new HashMap<>();
        resultMap.put("senderAccount", Optional.of(updatedSender));
        resultMap.put("receiverAccount", Optional.of(updatedReceiver));

        when(transferService.transfer(any(Transfer.class))).thenReturn(resultMap);

        mockMvc.perform(post("/api/bankaccounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer was successful."));

        verify(transferService, times(1)).transfer(any(Transfer.class));
    }

    @Test
    void transfer_shouldReturnBadRequestWhenTransferReturnsNull() throws Exception {
        Transfer transfer = new Transfer("10001", "10002", 100000.0);

        when(transferService.transfer(any(Transfer.class))).thenReturn(null);

        mockMvc.perform(post("/api/bankaccounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isBadRequest());

        verify(transferService, times(1)).transfer(any(Transfer.class));
    }

    @Test
    void transfer_shouldReturnBadRequestWhenReceiverAccountNotPresent() throws Exception {
        Transfer transfer = new Transfer("10001", "10002", 500.0);

        Map<String, Optional<BankAccount>> resultMap = new HashMap<>();
        resultMap.put("senderAccount", Optional.of(savingsAccount));
        resultMap.put("receiverAccount", Optional.empty());

        when(transferService.transfer(any(Transfer.class))).thenReturn(resultMap);

        mockMvc.perform(post("/api/bankaccounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isBadRequest());

        verify(transferService, times(1)).transfer(any(Transfer.class));
    }

    @Test
    void transfer_shouldReturnBadRequestWhenSenderAccountNotPresent() throws Exception {
        Transfer transfer = new Transfer("10001", "10002", 500.0);

        Map<String, Optional<BankAccount>> resultMap = new HashMap<>();
        resultMap.put("senderAccount", Optional.empty());
        resultMap.put("receiverAccount", Optional.of(checkingAccount));

        when(transferService.transfer(any(Transfer.class))).thenReturn(resultMap);

        mockMvc.perform(post("/api/bankaccounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isBadRequest());

        verify(transferService, times(1)).transfer(any(Transfer.class));
    }

    @Test
    void transfer_shouldReturnBadRequestWhenAccountNumbersMismatch() throws Exception {
        Transfer transfer = new Transfer("10001", "10002", 500.0);

        BankAccount mismatchedSender = new BankAccount("99999", "Jane Doe", 51500.0, "Savings");
        BankAccount mismatchedReceiver = new BankAccount("88888", "Jane Doe", 8000.0, "Checking");

        Map<String, Optional<BankAccount>> resultMap = new HashMap<>();
        resultMap.put("senderAccount", Optional.of(mismatchedSender));
        resultMap.put("receiverAccount", Optional.of(mismatchedReceiver));

        when(transferService.transfer(any(Transfer.class))).thenReturn(resultMap);

        mockMvc.perform(post("/api/bankaccounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isBadRequest());

        verify(transferService, times(1)).transfer(any(Transfer.class));
    }

    @Test
    void transfer_shouldReturnBadRequestWhenExceptionThrown() throws Exception {
        Transfer transfer = new Transfer("10001", "10002", 500.0);

        when(transferService.transfer(any(Transfer.class))).thenThrow(new RuntimeException("Transfer failed"));

        mockMvc.perform(post("/api/bankaccounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isBadRequest());

        verify(transferService, times(1)).transfer(any(Transfer.class));
    }

    @Test
    void saveLogs_shouldAcceptLogMessage() throws Exception {
        mockMvc.perform(post("/api/bankaccounts/logmessage")
                        .param("logmsg", "Test log message"))
                .andExpect(status().isOk());
    }

    @Test
    void send_shouldCallConsumerServiceConnect() throws Exception {
        doNothing().when(consumerService).connect(anyString());

        mockMvc.perform(get("/api/bankaccounts/connect"))
                .andExpect(status().isOk());

        verify(consumerService, times(1)).connect("my_queue");
    }

    @Test
    void send_shouldThrowExceptionWhenConsumerServiceFails() throws Exception {
        doThrow(new RuntimeException("Connection failed")).when(consumerService).connect(anyString());

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.util.NestedServletException.class,
                () -> mockMvc.perform(get("/api/bankaccounts/connect"))
        );

        verify(consumerService, times(1)).connect("my_queue");
    }
}
