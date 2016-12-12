package pl.grzeslowski.chatbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import pl.grzeslowski.chatbox.rnn.trainer.Trainer;

@SpringBootApplication
@ComponentScan(basePackageClasses = ChatBotApplication.class)
public class ChatBotApplication implements CommandLineRunner{
    private final Trainer trainer;

	@Autowired
    public ChatBotApplication(Trainer trainer) {
        this.trainer = trainer;
    }

	public static void main(String[] args) {
		SpringApplication.run(ChatBotApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
        trainer.trainAndTest();
    }
}
