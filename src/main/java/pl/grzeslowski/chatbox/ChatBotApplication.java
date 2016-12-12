package pl.grzeslowski.chatbox;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import pl.grzeslowski.chatbox.dialogs.Dialog;
import pl.grzeslowski.chatbox.dialogs.DialogParser;
import pl.grzeslowski.chatbox.files.FileReader;
import pl.grzeslowski.chatbox.preprocessor.TextPreprocessor;
import pl.grzeslowski.chatbox.rnn.RnnEngine;
import pl.grzeslowski.chatbox.word2vec.Word2VecService;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
@SpringBootApplication
@ComponentScan(basePackageClasses = ChatBotApplication.class)
public class ChatBotApplication implements CommandLineRunner{
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ChatBotApplication.class);

	@Value("${subtitles.path}")
	private String subtitlesPath;
	@Value("${dialogParser.pathToSubtitles}")
	private String pathToSubtitles;
	@Value("${rnn.maxWordsInDialog}")
	private int maxWordsInDialog;

	@Autowired
	private FileReader fileReader;
	@Autowired
	private Word2VecService word2Vec;
	@Autowired
	private DialogParser dialogParser;
	@Autowired
	private RnnEngine rnnEngine;


	public static void main(String[] args) {
		SpringApplication.run(ChatBotApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		final Word2Vec word2Vec = this.word2Vec.computeModel();
		final Stream<String> lines = fileReader.findAllFilesInDir(pathToSubtitles)
				.map(file -> fileReader.readFile(file))
				.filter(Optional::isPresent)
				.flatMap(Optional::get);
		final Stream<Dialog> dialogs = dialogParser.parse(lines);
		final MultiLayerNetwork multiLayerNetwork = rnnEngine.buildEngine(dialogs, word2Vec);
	}
}
