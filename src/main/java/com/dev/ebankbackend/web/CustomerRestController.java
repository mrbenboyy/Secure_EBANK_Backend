package com.dev.ebankbackend.web;

import com.dev.ebankbackend.dtos.BankAccountDTO;
import com.dev.ebankbackend.dtos.CustomerDTO;
import com.dev.ebankbackend.entities.Customer;
import com.dev.ebankbackend.exceptions.CustomerNotFoundException;
import com.dev.ebankbackend.mappers.BankAccountMapperImpl;
import com.dev.ebankbackend.repositories.CustomerRepository;
import com.dev.ebankbackend.services.BankAccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin("*")
public class CustomerRestController {
    private BankAccountService bankAccountService;
    private final CustomerRepository customerRepository; // Inject the repository
    private final BankAccountMapperImpl dtoMapper;
    @GetMapping("/customers")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public List<CustomerDTO> customers(){
        return bankAccountService.listCustomers();
    }
    @GetMapping("/customers/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public List<CustomerDTO> searchCustomers(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return bankAccountService.searchCustomers("%"+keyword+"%");
    }
    @GetMapping("/customers/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public CustomerDTO getCustomer(@PathVariable(name = "id") Long customerId) throws CustomerNotFoundException {
        return bankAccountService.getCustomer(customerId);
    }
    @PostMapping("/customers")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<CustomerDTO> saveCustomer(@RequestBody CustomerDTO customerDTO) {
        try {
            CustomerDTO savedCustomer = bankAccountService.saveCustomer(customerDTO);
            return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error saving customer: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/customers/{customerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public CustomerDTO updateCustomer(@PathVariable Long customerId, @RequestBody CustomerDTO customerDTO){
        customerDTO.setId(customerId);
        return bankAccountService.updateCustomer(customerDTO);
    }
    @DeleteMapping("/customers/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public void deleteCustomer(@PathVariable Long id){
        bankAccountService.deleteCustomer(id);
    }
    @GetMapping("/customers/{customerId}/accounts")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public List<BankAccountDTO> getCustomerAccounts(@PathVariable Long customerId) {
        return bankAccountService.getCustomerAccounts(customerId);
    }
    @GetMapping("/customers/email/{email}")
    public CustomerDTO getCustomerByEmail(@PathVariable String email) throws CustomerNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return dtoMapper.fromCustomer(customer);
    }
}
