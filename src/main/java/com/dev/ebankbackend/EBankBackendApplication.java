package com.dev.ebankbackend;

import com.dev.ebankbackend.dtos.BankAccountDTO;
import com.dev.ebankbackend.dtos.CurrentBankAccountDTO;
import com.dev.ebankbackend.dtos.CustomerDTO;
import com.dev.ebankbackend.dtos.SavingBankAccountDTO;
import com.dev.ebankbackend.entities.AccountOperation;
import com.dev.ebankbackend.entities.CurrentAccount;
import com.dev.ebankbackend.entities.Customer;
import com.dev.ebankbackend.entities.SavingAccount;
import com.dev.ebankbackend.enums.AccountStatus;
import com.dev.ebankbackend.enums.OperationType;
import com.dev.ebankbackend.exceptions.CustomerNotFoundException;
import com.dev.ebankbackend.repositories.AccountOperationRepository;
import com.dev.ebankbackend.repositories.BankAccountRepository;
import com.dev.ebankbackend.repositories.CustomerRepository;
import com.dev.ebankbackend.services.BankAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EBankBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EBankBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(BankAccountService bankAccountService) {
        return args -> {
            // Création de clients avec des informations supplémentaires
            Stream.of("Othmane", "Oussama", "Hicham","Abdelhakim","Anas").forEach(name -> {
                CustomerDTO customer = new CustomerDTO();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                customer.setPassword("12345"); // Mot de passe par défaut pour l'exemple
                customer.setRole("USER"); // Rôle par défaut
                bankAccountService.saveCustomer(customer);
            });

            // Création de comptes bancaires pour chaque client
            bankAccountService.listCustomers().forEach(customer -> {
                try {
                    bankAccountService.saveCurrentBankAccount(Math.random() * 90000, 9000, customer.getId());
                    bankAccountService.saveSavingBankAccount(Math.random() * 120000, 5.5, customer.getId());
                } catch (CustomerNotFoundException e) {
                    e.printStackTrace();
                }
            });

            // Traitement des opérations sur les comptes bancaires
            List<BankAccountDTO> bankAccounts = bankAccountService.bankAccountList();
            for (BankAccountDTO bankAccount : bankAccounts) {
                for (int i = 0; i < 10; i++) {
                    String accountId;
                    if (bankAccount instanceof SavingBankAccountDTO) {
                        accountId = ((SavingBankAccountDTO) bankAccount).getId();
                    } else {
                        accountId = ((CurrentBankAccountDTO) bankAccount).getId();
                    }
                    bankAccountService.credit(accountId, 10000 + Math.random() * 120000, "Credit");
                    bankAccountService.debit(accountId, 1000 + Math.random() * 9000, "Debit");
                }
            }
        };
    }

    //@Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationRepository accountOperationRepository) {
        return args -> {
            // Création de clients avec des informations supplémentaires
            Stream.of("hakim", "messi", "lewandowski").forEach(name -> {
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                customer.setPassword("12345"); // Mot de passe par défaut pour l'exemple
                customer.setRole("USER"); // Rôle par défaut
                customerRepository.save(customer);
            });

            // Création de comptes bancaires pour chaque client
            customerRepository.findAll().forEach(cust -> {
                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setBalance(Math.random() * 90000);
                currentAccount.setCreatedAt(new Date());
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setCustomer(cust);
                currentAccount.setOverDraft(9000);
                bankAccountRepository.save(currentAccount);

                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setBalance(Math.random() * 90000);
                savingAccount.setCreatedAt(new Date());
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setCustomer(cust);
                savingAccount.setInterestRate(5.5);
                bankAccountRepository.save(savingAccount);
            });

            // Création d'opérations sur les comptes bancaires
            bankAccountRepository.findAll().forEach(acc -> {
                for (int i = 0; i < 10; i++) {
                    AccountOperation accountOperation = new AccountOperation();
                    accountOperation.setOperationDate(new Date());
                    accountOperation.setAmount(Math.random() * 12000);
                    accountOperation.setType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT);
                    accountOperation.setBankAccount(acc);
                    accountOperationRepository.save(accountOperation);
                }
            });
        };
    }
}
