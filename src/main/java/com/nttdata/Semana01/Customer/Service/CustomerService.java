package com.nttdata.Semana01.Customer.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nttdata.Semana01.Customer.Entity.Customer;
import com.nttdata.Semana01.Customer.Repository.CustomerRepository;
import com.nttdata.Semana01.Customer.config.BankProperties;
import com.nttdata.Semana01.Customer.response.BankResponse;
import com.nttdata.Semana01.Customer.response.CustomerResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service 
public class CustomerService {

	private final BankProperties bankProperties;
	
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
	
	public Mono<Customer> getCustomerbyId(String id) {
		return customerRepository.findById(id);
	}
	
	public Flux<Customer> getAllCustomerByDNI(String dni) {
		return customerRepository.findAll().filter(x -> x.getDniCustomer().equals(dni));
	}
	
	public Flux<CustomerResponse> getAllCustomerByDNIResponse(String dni) {
		return customerRepository.findAll().filter(x -> x.getDniCustomer().equals(dni))
			   .map(customer -> CustomerResponse.builder()
					.id(customer.getId())
					.codeCustomer(customer.getCodeCustomer())
					.nameCustomer(customer.getNameCustomer())
					.lastNameCustomer(customer.getLastNameCustomer())
					.dniCustomer(customer.getDniCustomer())
					.customertype(customer.getCustomertype())
					.bank(customer.getBank())
					.build());
	}
	
	public Mono<Customer> deleteCustomer(String id) {
		return customerRepository.findById(id).flatMap(existsCustomer -> customerRepository
				.delete(existsCustomer).then(Mono.just(existsCustomer)));
	}
	
	@SuppressWarnings("unchecked")
	public BankResponse comunicationWebClientObtenerBankbyCodeResponse(String dni) { 
		String uri = bankProperties.getBaseUrl() + "/bank/bankbycodeResponse/".concat(dni);
		RestTemplate restTemplate = new RestTemplate();
		BankResponse result = restTemplate.getForObject(uri, BankResponse.class); 
		log.info("Ver lista --->" + result);
		return result;

	}
	
}
