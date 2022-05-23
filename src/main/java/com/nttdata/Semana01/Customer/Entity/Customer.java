package com.nttdata.Semana01.Customer.Entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nttdata.Semana01.Customer.DTO.Bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Document
@Data
@Builder
public class Customer {

	@Id
	private String id;
	
	private String codeCustomer;
	
	private String nameCustomer;
	
	private String lastNameCustomer;
	
	private String directionCustomer;
	
	private String emailCustomer;
	
	private String phoneNumberCustomer;
	
	@JsonFormat(pattern="yyyy-MM-dd",shape=JsonFormat.Shape.STRING)
	private Date birthDateCustomer;
	
	@JsonFormat(pattern="dd-MM-yyyy" , timezone="GMT-05:00")
	private Date registerDateCustomer;
	
	private String dniCustomer;
	
	private CustomerType customertype;
	
	private Bank bank; 
	
}

