package my.personal.budgetTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class BudgetTrackerApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();

		System.setProperty("MYSQL_USERNAME", dotenv.get("MYSQL_USERNAME"));
		System.setProperty("MYSQL_PASSWORD", dotenv.get("MYSQL_PASSWORD"));
        System.setProperty("EMAIL_USERNAME", dotenv.get("EMAIL_USERNAME"));
        System.setProperty("EMAIL_PASSWORD", dotenv.get("EMAIL_PASSWORD"));
        System.setProperty("EMAIL_FROM", dotenv.get("EMAIL_FROM"));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));

		SpringApplication.run(BudgetTrackerApplication.class, args);
	}

}
