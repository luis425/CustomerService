package com.nttdata.Semana01.Customer.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.Semana01.Customer.Entity.Customer;
import com.nttdata.Semana01.Customer.Repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerService {

	@Autowired
	CustomerRepository customerRepository;
	 
	public Flux<Customer> getAllCustomer() {
		return customerRepository.findAll();
	}
	
	public Mono<Customer> createCustomer(Customer customer) {
		return customerRepository.save(customer);
	}
 	
	public Flux<Customer> getAllCustomerByCode(String codeCustomer) {
		return customerRepository.findAll().filter(x -> x.getCodeCustomer().equals(codeCustomer));
	}

	public Flux<Customer> getAllCustomerByCodeBank(String codeBank) {
		return customerRepository.findAll().filter(x -> x.getBank().getCode().equals(codeBank));
	}
	
}
