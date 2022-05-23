package com.nttdata.Semana01.Customer.Repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.nttdata.Semana01.Customer.Entity.Customer;

@Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, String>{
}
