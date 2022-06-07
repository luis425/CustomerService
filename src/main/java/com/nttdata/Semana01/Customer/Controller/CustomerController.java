package com.nttdata.Semana01.Customer.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nttdata.Semana01.Customer.DTO.Bank;
import com.nttdata.Semana01.Customer.Entity.Customer;
import com.nttdata.Semana01.Customer.Entity.CustomerType;
import com.nttdata.Semana01.Customer.Service.CustomerService;
import com.nttdata.Semana01.Customer.Service.CustomerTypeService;
import com.nttdata.Semana01.Customer.response.BankResponse;
import com.nttdata.Semana01.Customer.response.CustomerResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

	@Autowired
	CustomerService customerService;

	@Autowired
	CustomerTypeService customerTypeSerivce;

	private String codigoValidatorBank;

	private String messageBadRequest;

	private Integer codigoValidatorCustomerType;

	public final ObjectMapper mapper = new ObjectMapper();

	private WebClient bankServiceClient = WebClient.builder().baseUrl("http://localhost:8080").build();

	private static final String CUSTOMER_CONTACT_TO_BANKSERVICE = "customerContactToBankService";

	@GetMapping("/get")
	public Mono<ResponseEntity<Flux<Customer>>> getAllCustomer() {
		Flux<Customer> list = this.customerService.getAllCustomer();
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list));
	}

	@PostMapping("/register")
	@CircuitBreaker(name = CUSTOMER_CONTACT_TO_BANKSERVICE, fallbackMethod = "customerContacttoBank")
	public Mono<Customer> create(@RequestBody Customer customer) throws InterruptedException {

		boolean validationvalue = this.validationRegisterCustomerRequest(customer);

		if (validationvalue) {

			Flux<Customer> listCodeCustomerFlux = this.customerService.getAllCustomerByCode(customer.getCodeCustomer());

			List<Customer> listCustomerCode = new ArrayList<>();

			listCodeCustomerFlux.collectList().subscribe(listCustomerCode::addAll);

			/*Mono<Bank> endpointResponse = bankServiceClient.get().uri("/bank/".concat(customer.getBank().getCode()))
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Bank.class).log().doOnError(ex -> {
						throw new RuntimeException("the exception message is - " + ex.getMessage());
					});

			List<Bank> listBank = new ArrayList<>();

			endpointResponse.flux().collectList().subscribe(listBank::addAll);
			*/
			
			BankResponse  endpointResponseBank = this.customerService.comunicationWebClientObtenerBankbyCodeResponse(customer.getBank().getCode());

			Flux<Customer> listCustomerFlux = this.customerService.getAllCustomerByCode(customer.getCodeCustomer());

			List<Customer> listCustomer = new ArrayList<>();

			listCustomerFlux.collectList().subscribe(listCustomer::addAll);

			long temporizador = (3 * 1000);

			Thread.sleep(temporizador);

			var typeCostumer = this.customerTypeSerivce.getCustomerTypebyId(customer.getCustomertype().getId());

			List<CustomerType> listCustomerType = new ArrayList<>();

			typeCostumer.flux().collectList().subscribe(listCustomerType::addAll);

			if (listCustomerCode.isEmpty()) {

				try {

					long temporizador2 = (3 * 1000);
					Thread.sleep(temporizador2);

					//codigoValidatorBank = this.validardorBank(listBank, customer);
					codigoValidatorBank = this.validardorBank(endpointResponseBank, customer);

					log.info("Validar Codigo Repetido Banco --->" + codigoValidatorBank);

					codigoValidatorCustomerType = this.validardorCustomerType(listCustomerType, customer);

					log.info("Obtener valor para validar Id Tipo Cliente --->" + codigoValidatorCustomerType);

					if (codigoValidatorBank.equals("")) {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"El Codigo de Banco no existe"));
					}

					if (codigoValidatorCustomerType == 0) {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"El Id de Tipo Cliente no existe"));
					}

					customer.setRegisterDateCustomer(new Date());
					return this.customerService.createCustomer(customer);

				} catch (InterruptedException e) {
					log.info(e.toString());
					Thread.currentThread().interrupt();
					return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
				}

			} else {
				return Mono.error(
						new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "El Codigo de Cliente ya existe"));
			}

		} else {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, messageBadRequest));
		}

	}

	@PutMapping(value = "/{code}")
	@CircuitBreaker(name = CUSTOMER_CONTACT_TO_BANKSERVICE, fallbackMethod = "customerContacttoBank")
	public Mono<Customer> updateCustomer(@PathVariable String code, @RequestBody Customer customer)
			throws InterruptedException {

		List<Bank> listBank = new ArrayList<>();
		
		BankResponse endpointResponseBank = new BankResponse();
		
		List<CustomerType> listCustomerType = new ArrayList<>();

		// Condicion para validar que no se puede actualizar el ID

		if (customer.getId() != null) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"El Atributo id no puede actualizarse por ser un dato unico"));
		}

		if (customer.getCodeCustomer() != null) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"El Atributo codeCustomer no puede actualizarse por ser un dato unico"));
		}

		if (customer.getRegisterDateCustomer() != null) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"El Atributo registerDateCustomer no puede actualizarse."));
		}

		Flux<Customer> listCustomerFlux = this.customerService.getAllCustomerByCode(code);

		List<Customer> listCustomer = new ArrayList<>();

		listCustomerFlux.collectList().subscribe(listCustomer::addAll);

		if (customer.getBank() != null) {

			if (customer.getBank().getCode() == null) {
				return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"El Atributo bank.code es necesario para cambiar el Bank de relacion."));
			}

			/*
			
			Mono<Bank> endpointResponse = bankServiceClient.get().uri("/bank/".concat(customer.getBank().getCode()))
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Bank.class).log();

			endpointResponse.flux().collectList().subscribe(listBank::addAll);

			long temporizador = (7 * 1000);

			Thread.sleep(temporizador);
			*/

			endpointResponseBank = this.customerService.comunicationWebClientObtenerBankbyCodeResponse(customer.getBank().getCode());

		}

		if (customer.getCustomertype() != null) {

			if (customer.getCustomertype().getId() == null) {
				return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"El Atributo customertype.id es necesario para cambiar el Customertype de relacion."));
			}

			var typeCostumer = this.customerTypeSerivce.getCustomerTypebyId(customer.getCustomertype().getId());

			typeCostumer.flux().collectList().subscribe(listCustomerType::addAll);

		}

		try {

			long temporizador2 = (7 * 1000);
			Thread.sleep(temporizador2);

			if (customer.getBank() != null) {

				//codigoValidatorBank = this.validardorBank(listBank, customer);
				codigoValidatorBank = this.validardorBank(endpointResponseBank, customer);

				log.info("Validar Codigo Repetido Banco --->" + codigoValidatorBank);

				if (codigoValidatorBank.equals("")) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Codigo de Banco no existe"));
				}

			}

			if (customer.getCustomertype() != null) {

				codigoValidatorCustomerType = this.validardorCustomerType(listCustomerType, customer);

				log.info("Obtener valor para validar Id Tipo Cliente --->" + codigoValidatorCustomerType);

				if (codigoValidatorCustomerType == 0) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Id de Tipo Cliente no existe"));
				}

			}

			if (listCustomer.isEmpty()) {
				return Mono.error(
						new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "El Codigo Cliente no existe"));
			} else {
				Customer customerUpdate = this.validationUpdateCustomerRequest(listCustomer, customer);
				return this.customerService.createCustomer(customerUpdate);
			}

		} catch (InterruptedException e) {
			log.info(e.toString());
			Thread.currentThread().interrupt();
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}

	}

	@GetMapping(value = "/{codeCustomer}")
	public Mono<ResponseEntity<Customer>> getCustomerByCode(@PathVariable String codeCustomer) {

		try {

			Flux<Customer> customerflux = this.customerService.getAllCustomerByCode(codeCustomer);

			List<Customer> list1 = new ArrayList<>();

			customerflux.collectList().subscribe(list1::addAll);

			long temporizador = (5 * 1000);

			Thread.sleep(temporizador);

			if (list1.isEmpty()) {
				return null;

			} else {
				return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list1.get(0)))
						.defaultIfEmpty(ResponseEntity.notFound().build());
			}

		} catch (InterruptedException e) {
			log.info(e.toString());
			Thread.currentThread().interrupt();
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}
	}

	@GetMapping(value = "/CustomerByCodeBank/{codeBank}")
	public Mono<ResponseEntity<Flux<Customer>>> getCustomerByCodeBank(@PathVariable String codeBank) {
		Flux<Customer> list = this.customerService.getAllCustomerByCodeBank(codeBank);
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@GetMapping(value = "/customerbydniResponse/{dni}")
	public Mono<ResponseEntity<CustomerResponse>> getCustomerByDNIResponse(@PathVariable String dni) {

		try {

			Flux<CustomerResponse> customerflux = this.customerService.getAllCustomerByDNIResponse(dni);

			List<CustomerResponse> list1 = new ArrayList<>();

			customerflux.collectList().subscribe(list1::addAll);

			long temporizador = (5 * 1000);

			Thread.sleep(temporizador);

			if (list1.isEmpty()) {
				return null;

			} else {
				return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list1.get(0)))
						.defaultIfEmpty(ResponseEntity.notFound().build());
			}

		} catch (InterruptedException e) {
			log.info(e.toString());
			Thread.currentThread().interrupt();
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}
	}
	
	@GetMapping(value = "/customerbydni/{dni}")
	public Mono<ResponseEntity<Customer>> getCustomerByDNI(@PathVariable String dni) {

		try {

			Flux<Customer> customerflux = this.customerService.getAllCustomerByDNI(dni);

			List<Customer> list1 = new ArrayList<>();

			customerflux.collectList().subscribe(list1::addAll);

			long temporizador = (5 * 1000);

			Thread.sleep(temporizador);

			if (list1.isEmpty()) {
				return null;

			} else {
				return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list1.get(0)))
						.defaultIfEmpty(ResponseEntity.notFound().build());
			}

		} catch (InterruptedException e) {
			log.info(e.toString());
			Thread.currentThread().interrupt();
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}
	}
	
	@GetMapping(value = "/customerbyId/{id}")
	public Mono<Customer> getCustomerById(@PathVariable String id) {
		return this.customerService.getCustomerbyId(id); 
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteCustomerById(@PathVariable String id) {

		try {
			return this.customerService.deleteCustomer(id).map(r -> ResponseEntity.ok().<Void>build())
					.defaultIfEmpty(ResponseEntity.notFound().build());

		} catch (Exception e) {
			log.info(e.toString());
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}

	}
	
	public boolean validationRegisterCustomerRequest(Customer customer) {

		boolean validatorcustomer;

		if (customer.getCodeCustomer() == null || customer.getCodeCustomer().equals("")) {
			validatorcustomer = false;
			messageBadRequest = "codeCustomer no puede ser Vacio";
		} else if (customer.getNameCustomer() == null || customer.getNameCustomer().equals("")) {
			validatorcustomer = false;
			messageBadRequest = "nameCustomer no puede ser Vacio";
		} else if (customer.getLastNameCustomer() == null || customer.getLastNameCustomer().equals("")) {
			validatorcustomer = false;
			messageBadRequest = "lastNameCustomer no puede ser Vacio";
		} else if (customer.getDirectionCustomer() == null || customer.getDirectionCustomer().equals("")) {
			validatorcustomer = false;
			messageBadRequest = "directionCustomer no puede ser Vacio";
		} else if (customer.getEmailCustomer() == null || customer.getEmailCustomer().equals("")) {
			validatorcustomer = false;
			messageBadRequest = "emailCustomer no puede ser Vacio";
		} else if (customer.getPhoneNumberCustomer() == null || customer.getPhoneNumberCustomer().equals("")) {
			validatorcustomer = false;
			messageBadRequest = "phoneNumberCustomer no puede ser Vacio";
		} else if (customer.getDniCustomer() == null || customer.getDniCustomer().equals("")) {
			validatorcustomer = false;
			messageBadRequest = "dniCustomer no puede ser Vacio";
		} else if (customer.getCustomertype().getId() == null || customer.getCustomertype().getId() == 0) {
			validatorcustomer = false;
			messageBadRequest = "customertype.Id no puede ser Vacio";
		} else if (customer.getBank().getCode() == null || customer.getBank().getCode().equals("")) {
			validatorcustomer = false;
			messageBadRequest = "bank.code no puede ser Vacio";
		} else if (customer.getBirthDateCustomer() == null) {
			validatorcustomer = false;
			messageBadRequest = "birthDateCustomer no puede ser Vacio";
		} else if (customer.getRegisterDateCustomer() != null) {
			validatorcustomer = false;
			messageBadRequest = "registerDateCustomer no debe enviarse";
		} else {
			validatorcustomer = true;
		}

		return validatorcustomer;
	}

	//public String validardorBank(List<Bank> list1, Customer customer) {
	public String validardorBank(BankResponse list1, Customer customer) { 
		
		//if (list1.isEmpty()) {
		if (list1 == null) {
			codigoValidatorBank = "";
		} else {
			
			//codigoValidatorBank = list1.get(0).getCode();
			
			codigoValidatorBank = list1.getCode();

			log.info("Validar listBank -->:" + list1.getCode());
			
			customer.getBank().setId(list1.getId());
			customer.getBank().setCode(list1.getCode());
			customer.getBank().setBankName(list1.getBankName());
			customer.getBank().setDirectionMain(list1.getDirectionMain());
			//customer.getBank().setId(list1.get(0).getId());
			//customer.getBank().setCode(codigoValidatorBank);
			//customer.getBank().setBankName(list1.get(0).getBankName());
			//customer.getBank().setDirectionMain(list1.get(0).getDirectionMain());

		}

		return codigoValidatorBank;
	}

	public Integer validardorCustomerType(List<CustomerType> list1, Customer customer) {

		if (list1.isEmpty()) {
			codigoValidatorCustomerType = 0;
		} else {
			codigoValidatorCustomerType = list1.get(0).getId();

			log.info("Validar listCustomerType -->:" + list1.get(0).toString());
			customer.getCustomertype().setId(codigoValidatorCustomerType);
			customer.getCustomertype().setDescription(list1.get(0).getDescription());
		}

		return codigoValidatorCustomerType;
	}

	public Customer validationUpdateCustomerRequest(List<Customer> listCustomer, Customer customer) {

		if (customer.getId() == null || customer.getId().equals("")) {
			customer.setId(listCustomer.get(0).getId());
		}

		if (customer.getCodeCustomer() == null || customer.getCodeCustomer().equals("")) {
			customer.setCodeCustomer(listCustomer.get(0).getCodeCustomer());
		}

		if (customer.getRegisterDateCustomer() == null) {
			customer.setRegisterDateCustomer(listCustomer.get(0).getRegisterDateCustomer());
		}

		if (customer.getNameCustomer() == null || customer.getNameCustomer().equals("")) {
			customer.setNameCustomer(listCustomer.get(0).getNameCustomer());
		} else {
			// se mantiene el dato enviado en el request
			log.info("Valor nameCustomer -->" + customer.getNameCustomer());
		}

		if (customer.getLastNameCustomer() == null || customer.getLastNameCustomer().equals("")) {
			customer.setLastNameCustomer(listCustomer.get(0).getLastNameCustomer());
		} else {
			// se mantiene el dato enviado en el request
			log.info("Valor lastNameCustomer -->" + customer.getLastNameCustomer());
		}

		if (customer.getDirectionCustomer() == null || customer.getDirectionCustomer().equals("")) {
			customer.setDirectionCustomer(listCustomer.get(0).getDirectionCustomer());
		} else {
			// se mantiene el dato enviado en el request
			log.info("Valor directionCustomer -->" + customer.getDirectionCustomer());
		}

		if (customer.getEmailCustomer() == null || customer.getEmailCustomer().equals("")) {
			customer.setEmailCustomer(listCustomer.get(0).getEmailCustomer());
		} else {
			// se mantiene el dato enviado en el request
			log.info("Valor emailCustomer -->" + customer.getEmailCustomer());
		}

		if (customer.getBirthDateCustomer() == null) {
			customer.setBirthDateCustomer(listCustomer.get(0).getBirthDateCustomer());
		} else {
			// se mantiene el dato enviado en el request
			log.info("Valor birthDateCustomer -->" + customer.getBirthDateCustomer());
		}

		if (customer.getPhoneNumberCustomer() == null || customer.getPhoneNumberCustomer().equals("")) {
			customer.setPhoneNumberCustomer(listCustomer.get(0).getPhoneNumberCustomer());
		} else {
			// se mantiene el dato enviado en el request
			log.info("Valor phoneNumberCustomer -->" + customer.getPhoneNumberCustomer());
		}

		if (customer.getDniCustomer() == null || customer.getDniCustomer().equals("")) {
			customer.setDniCustomer(listCustomer.get(0).getDniCustomer());
		} else {
			// se mantiene el dato enviado en el request
			log.info("Valor dniCustomer -->" + customer.getDniCustomer());
		}

		if (customer.getBank() == null) {
			customer.setBank(listCustomer.get(0).getBank());
		} else {
			// se mantiene el dato enviado en el request
			log.info("Valor Bank -->" + customer.getBank());
		}

		if (customer.getCustomertype() == null) {
			customer.setCustomertype(listCustomer.get(0).getCustomertype());
		} else {
			// se mantiene el dato enviado en el request
			log.info("Valor Customertype -->" + customer.getCustomertype());
		}

		return customer;
	}

	public Mono<Customer> customerContacttoBank(Throwable ex) { 
		log.info("Message ---->" + ex.getMessage());
		Customer mockServiceResponse = null;
		return Mono.just(mockServiceResponse);
	}
}
