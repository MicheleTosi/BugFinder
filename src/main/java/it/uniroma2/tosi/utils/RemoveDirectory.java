package it.uniroma2.tosi.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

public class RemoveDirectory {

    private static final Logger logger = Logger.getLogger(RemoveDirectory.class.getName());

    private RemoveDirectory(){
        throw new IllegalStateException("RemoveDirectory class");
    }

    public static void removeDir(String directory) throws IOException {
        Path directoryPath = Paths.get(directory);

        deleteRecursively(directoryPath);
        logger.log(INFO,"Directory and its contents deleted successfully.");
}

    private static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                for (Path entry : directoryStream) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.delete(path);
    }
}
