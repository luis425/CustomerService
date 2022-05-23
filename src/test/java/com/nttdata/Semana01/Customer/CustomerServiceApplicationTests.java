package com.nttdata.Semana01.Customer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CustomerServiceApplicationTests {

	@Test
	void contextLoads() {
		Assertions.assertDoesNotThrow(this::doNotThrowException);
	}

	private void doNotThrowException() {
		// This method will never throw exception
	}

}
