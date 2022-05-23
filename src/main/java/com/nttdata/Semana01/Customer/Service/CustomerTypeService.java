package com.nttdata.Semana01.Customer.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.Semana01.Customer.Entity.CustomerType;
import com.nttdata.Semana01.Customer.Repository.CustomerTypeRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerTypeService {

	@Autowired
	CustomerTypeRepository customerTypeRepository;

	public Mono<CustomerType> createCustomerType(CustomerType customerType) {
		return customerTypeRepository.save(customerType);
	}

	public Mono<CustomerType> getCustomerTypebyId(Integer id) {
		return customerTypeRepository.findById(id);
	}

	public Flux<CustomerType> getAllCustomerType() {
		return customerTypeRepository.findAll();
	}

}