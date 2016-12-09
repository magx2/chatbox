package pl.grzeslowski.chatbox;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import pl.grzeslowski.chatbox.files.FileReader;
import pl.grzeslowski.chatbox.word2vec.Word2VecService;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@ComponentScan(basePackageClasses = ChatBotApplication.class)
public class ChatBotApplication implements CommandLineRunner{
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ChatBotApplication.class);

	@Value("${word2vec.pathToInputFile}")
	private String pathToInputFile;

	@Value("${subtitles.path}")
	private String subtitlesPath;

	@Autowired
	private FileReader fileReader;

	@Autowired
	private Word2VecService word2Vec;

	public static void main(String[] args) {
		SpringApplication.run(ChatBotApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		prepareInputFileForWord2Vec();

		final Word2Vec word2Vec = this.word2Vec.computeModel();
	}

	private void prepareInputFileForWord2Vec() {
		final Path path = Paths.get(pathToInputFile);
		if(!path.toFile().exists()) {
			log.info("Creating input file for Word2Vec...");
			fileReader.joinAllFilesInDirToSingleFile(subtitlesPath, pathToInputFile);
		}
	}
}
