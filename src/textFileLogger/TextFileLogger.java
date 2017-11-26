package textFileLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class TextFileLogger {

	private File file;

	private FileWriter writer;

	public TextFileLogger(String filePath) throws IOException {
		this.file = new File(filePath);
		if (!file.exists()) {
			throw new FileNotFoundException(String.format("Die Datei %s wurde nicht gefunden.", filePath));
		}
		this.writer = new FileWriter(file);
		this.log("########## - Start: " + LocalDate.now() + " - ##########");
	}

	public void log(String message) throws IOException {
		this.writer.write(message + "\n");
		this.writer.flush();
	}

	public void logWithConsole(String message) throws IOException {
		this.log(message);
		System.out.println(message);
	}

	public void close() throws IOException {
		this.log("########## - End: " + LocalDate.now() + " - ##########\n");
		this.writer.close();
	}

	public static void main(String... args) {
		try {
			TextFileLogger logger = new TextFileLogger("/home/andre/text.txt");
			logger.logWithConsole("Hallo");
			logger.log("Du");
			logger.logWithConsole("Sau!");
			logger.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Hilfeeeeee");
		}
	}

}
