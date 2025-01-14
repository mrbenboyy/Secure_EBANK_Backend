package com.dev.ebankbackend.services;

import com.dev.ebankbackend.dtos.*;
import com.dev.ebankbackend.entities.*;
import com.dev.ebankbackend.enums.OperationType;
import com.dev.ebankbackend.exceptions.BalanceNotSufficientException;
import com.dev.ebankbackend.exceptions.BankAccountNotFoundException;
import com.dev.ebankbackend.exceptions.CustomerNotFoundException;
import com.dev.ebankbackend.mappers.BankAccountMapperImpl;
import com.dev.ebankbackend.repositories.AccountOperationRepository;
import com.dev.ebankbackend.repositories.BankAccountRepository;
import com.dev.ebankbackend.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;
    private final BankAccountMapperImpl dtoMapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injection du PasswordEncoder

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        // Encoder le mot de passe avant de sauvegarder
        String encodedPassword = passwordEncoder.encode(customer.getPassword());
        customer.setPassword(encodedPassword); // Mettre à jour le mot de passe avec la version encodée
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null)
            throw new CustomerNotFoundException("Customer not found");
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);
        CurrentAccount savedBankAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null)
            throw new CustomerNotFoundException("Customer not found");
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);
        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }

    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(dtoMapper::fromCustomer)
                .collect(Collectors.toList());
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        if (bankAccount instanceof SavingAccount) {
            return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
        } else {
            return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        if (bankAccount.getBalance() < amount)
            throw new BalanceNotSufficientException("Balance not sufficient");
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource, amount, "Transfer to " + accountIdDestination);
        credit(accountIdDestination, amount, "Transfer from " + accountIdSource);
    }

    @Override
    public List<BankAccountDTO> bankAccountList() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        return bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            } else {
                return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(customerDTO.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Mettre à jour les champs uniquement s'ils sont fournis
        if (customerDTO.getName() != null) {
            customer.setName(customerDTO.getName());
        }
        if (customerDTO.getEmail() != null) {
            customer.setEmail(customerDTO.getEmail());
        }
        if (customerDTO.getRole() != null) {
            customer.setRole(customerDTO.getRole()); // Mettre à jour le rôle
        }
        // Ne pas écraser le mot de passe s'il n'est pas fourni
        if (customerDTO.getPassword() != null && !customerDTO.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(customerDTO.getPassword());
            customer.setPassword(encodedPassword);
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId) {
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        if (bankAccount == null) throw new BankAccountNotFoundException("Account not Found");
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<Customer> customers = customerRepository.searchCustomer(keyword);
        return customers.stream()
                .map(dtoMapper::fromCustomer)
                .collect(Collectors.toList());
    }
    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        customer.setPassword(encodedPassword);
        customerRepository.save(customer);
    }

    @Override
    public List<BankAccountDTO> getCustomerAccounts(Long customerId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByCustomerId(customerId);
        return bankAccounts.stream()
                .map(bankAccount -> {
                    if (bankAccount instanceof SavingAccount) {
                        return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
                    } else {
                        return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
                    }
                })
                .collect(Collectors.toList());
    }
}
