package com.dev.ebankbackend.dtos;
import com.dev.ebankbackend.enums.AccountStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
@EqualsAndHashCode(callSuper = true)
@Data
public class SavingBankAccountDTO extends BankAccountDTO {
    private String id;
    private double balance;
    private Date createdAt;
    private AccountStatus status;
    private CustomerDTO customerDTO;
    private double interestRate;

}
