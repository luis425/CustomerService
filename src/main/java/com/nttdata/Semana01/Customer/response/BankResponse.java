package com.nttdata.Semana01.Customer.response;

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
public class BankResponse {
	
	private String id;
	 
	private String code;
	
	private String bankName;
	
	private String directionMain;
	  
}
