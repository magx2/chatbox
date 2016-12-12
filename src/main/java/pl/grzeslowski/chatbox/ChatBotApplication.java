package pl.grzeslowski.chatbox;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import pl.grzeslowski.chatbox.rnn.trainer.Trainer;

@SuppressWarnings("ALL")
@SpringBootApplication
@ComponentScan(basePackageClasses = ChatBotApplication.class)
public class ChatBotApplication implements CommandLineRunner{
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ChatBotApplication.class);

	@Autowired
    private Trainer trainer;


	public static void main(String[] args) {
		SpringApplication.run(ChatBotApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
        trainer.trainAndTest();
    }
}
