package com.dev.ebankbackend.web;


import com.dev.ebankbackend.dtos.*;
import com.dev.ebankbackend.exceptions.BalanceNotSufficientException;
import com.dev.ebankbackend.exceptions.BankAccountNotFoundException;
import com.dev.ebankbackend.exceptions.CustomerNotFoundException;
import com.dev.ebankbackend.services.BankAccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
public class BankAccountRestAPI {
    private BankAccountService bankAccountService;

    public BankAccountRestAPI(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/accounts/{accountId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        return bankAccountService.getBankAccount(accountId);
    }
    @GetMapping("/accounts")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public List<BankAccountDTO> listAccounts(){
        return bankAccountService.bankAccountList();
    }
    @GetMapping("/accounts/{accountId}/operations")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public List<AccountOperationDTO> getHistory(@PathVariable String accountId){
        return bankAccountService.accountHistory(accountId);
    }

    @GetMapping("/accounts/{accountId}/pageOperations")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public AccountHistoryDTO getAccountHistory(
            @PathVariable String accountId,
            @RequestParam(name="page",defaultValue = "0") int page,
            @RequestParam(name="size",defaultValue = "5")int size) throws BankAccountNotFoundException {
        return bankAccountService.getAccountHistory(accountId,page,size);
    }
    @PostMapping("/accounts/debit")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        this.bankAccountService.debit(debitDTO.getAccountId(),debitDTO.getAmount(),debitDTO.getDescription());
        return debitDTO;
    }
    @PostMapping("/accounts/credit")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO) throws BankAccountNotFoundException {
        this.bankAccountService.credit(creditDTO.getAccountId(),creditDTO.getAmount(),creditDTO.getDescription());
        return creditDTO;
    }
    @PostMapping("/accounts/transfer")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public void transfer(@RequestBody TransferRequestDTO transferRequestDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        this.bankAccountService.transfer(
                transferRequestDTO.getAccountSource(),
                transferRequestDTO.getAccountDestination(),
                transferRequestDTO.getAmount());
    }
    @PostMapping("/accounts/current")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public CurrentBankAccountDTO createCurrentAccount(@RequestBody CurrentBankAccountDTO request) throws CustomerNotFoundException {
        return bankAccountService.saveCurrentBankAccount(request.getBalance(), request.getOverDraft(), request.getCustomerDTO().getId());
    }

    @PostMapping("/accounts/saving")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public SavingBankAccountDTO createSavingAccount(@RequestBody SavingBankAccountDTO request) throws CustomerNotFoundException {
        return bankAccountService.saveSavingBankAccount(request.getBalance(), request.getInterestRate(), request.getCustomerDTO().getId());
    }
}
