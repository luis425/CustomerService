package com.nttdata.Semana01.Customer.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Document
@Data
@Builder
public class CustomerType {

	@Id
	private Integer id;
	 
	private String description;
	
}
