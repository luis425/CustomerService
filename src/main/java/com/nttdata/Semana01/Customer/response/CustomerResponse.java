package com.nttdata.Semana01.Customer.response;

import java.util.Date;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nttdata.Semana01.Customer.DTO.Bank;
import com.nttdata.Semana01.Customer.Entity.CustomerType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
	 
	private String id;
	
	private String codeCustomer;
	
	private String nameCustomer;
	
	private String lastNameCustomer;
	
	//private String directionCustomer;
	
	//private String emailCustomer;
	
	//private String phoneNumberCustomer;
	
	//@JsonFormat(pattern="yyyy-MM-dd",shape=JsonFormat.Shape.STRING)
	//private Date birthDateCustomer;
	
	//@JsonFormat(pattern="dd-MM-yyyy" , timezone="GMT-05:00")
	//private Date registerDateCustomer;
	
	private String dniCustomer;
	
	private CustomerType customertype;
	
	private Bank bank; 
	
}

