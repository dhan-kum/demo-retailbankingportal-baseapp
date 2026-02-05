
package com.eviden.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.eviden.app.repository.BankAccountRepository;	
import com.eviden.app.entity.BankAccount;
import org.springframework.context.annotation.Bean;


@Component 
public class DatabaseLoader implements CommandLineRunner { 

	private final BankAccountRepository repository;

	@Autowired 
	public DatabaseLoader(BankAccountRepository repository) {
		this.repository = repository;
	}

	@Override
	public void run(String... strings) throws Exception { 
		//this.repository.save(new Employee("Frodo", "Baggins", "ring bearer"));
		repository.save(new BankAccount("008596512563", "John Doe", 52000.0, "Savings"));
		repository.save(new BankAccount("008596558965", "John Doe", 7500.0, "Checking"));

	}

	/**@Override
	public CommandLineRunner loadData(BankAccountRepository repository) {
		return args -> {
			repository.save(new BankAccount(100001L, "John Doe", 5000.0));
			repository.save(new BankAccount(100002L, "Jane Smith", 7500.0));
		};
	}**/
}
