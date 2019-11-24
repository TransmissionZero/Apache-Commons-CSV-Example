package com.github.transmissionzero.examples;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** JUnit tests for {@link CommonsCsvExample}. */
@DisplayName("Commons CSV Example Tests")
public class CommonsCsvExampleTest
{
  /** Ensure a NoSuchFileException is thrown when trying to read a file which doesn't exist. */
  @Test
  @DisplayName("Test File Not Found Handling")
  public void testFileNotFound()
  {
    // Attempt to perform find and replace operation. It should throw NoSuchFileException.
    final CommonsCsvExample csvExample = new CommonsCsvExample();

    Assertions.assertThrows(NoSuchFileException.class, () ->
    {
      csvExample.updateRowValues("does-not-exist.csv", "", "", "");
    }, "Trying to read a CSV file which doesn't exist should throw NoSuchFileException.");
  }

  /**
   * Ensure that attempting to process a file with a duplicate column name (i.e. is invalid) results in no modifications
   * to the input file and no stray temporary files left around.
   *
   * @param tempDir
   *        JUnit managed temporary directory where temporary files can be created.
   * @throws IOException
   *         When there is an I/O error reading or writing the CSV file.
   */
  @Test
  @DisplayName("Test Invalid Data Handling")
  public void testInvalidData(@TempDir final Path tempDir) throws IOException
  {
    // Create copy of the test data file so we aren't clobbering the project test data
    final Path testDataPath = Paths.get("src/test/resources/Invalid Data.csv");
    final Path csvFile = this.copyTestData(testDataPath, tempDir);

    // Attempt to perform find and replace operation. It should throw IllegalArgumentException.
    final CommonsCsvExample csvExample = new CommonsCsvExample();

    Assertions.assertThrows(IllegalArgumentException.class, () ->
    {
      csvExample.updateRowValues(csvFile.toString(), "id", "", "");
    }, "Attempting to process file with duplicate header names should throw IllegalArgumentException.");

    // Ensure the CSV is unmodified and no temporary file exists
    this.assertOutputCorrect(testDataPath, csvFile);
  }

  /**
   * Ensure find and replace operations work as expected on valid files.
   *
   * @param expectedOutputFileName
   *        Filename of a CSV file in "src/test/resources/Expected Output". The CSV produced by the code will be
   *        compared against this to ensure the output is correct. Note: The test will load the file contents into a
   *        string, so heap usage should be considered if using large files with this test.
   * @param column
   *        The name of the column where instances of <code>oldValue</code> should be replaced with
   *        <code>newValue</code>.
   * @param oldValue
   *        The value which should be replaced.
   * @param newValue
   *        The replacement value.
   * @param tempDir
   *        JUnit managed temporary directory where temporary files can be created.
   * @throws IOException
   *         When there is an I/O error reading or writing the CSV file.
   */
  @ParameterizedTest(name = "{0}")
  @MethodSource
  @DisplayName("Test CSV Replacement")
  public void testCsvReplacement(final String expectedOutputFileName, final String column, final String oldValue,
      final String newValue, @TempDir final Path tempDir) throws IOException
  {
    // Create copy of the test data file so we aren't clobbering the project test data
    final Path csvFile = this.copyTestData(Paths.get("src/test/resources/Input Data.csv"), tempDir);

    // Perform find and replace operation
    final CommonsCsvExample csvExample = new CommonsCsvExample();
    csvExample.updateRowValues(csvFile.toString(), column, oldValue, newValue);

    // Ensure output CSV file is correct and the temporary file is deleted
    this.assertOutputCorrect(Paths.get("src/test/resources/Expected Output", expectedOutputFileName), csvFile);
  }

  /**
   * Provide arguments for the parameterised test {@link #testCsvReplacement(String, String, String, String, Path)}.
   *
   * @return The arguments for the parameterised test {@link #testCsvReplacement(String, String, String, String, Path)}.
   */
  public static Stream<Arguments> testCsvReplacement()
  {
    return Stream.of(Arguments.of("Replace Apple with Lime.csv", "bar", "apple", "lime"),
                     Arguments.of("Replace Orange with Banana.csv", "baz", "orange", "banana"),
                     Arguments.of("Column Doesn't Exist.csv", "bear", "apple", "lime"));
  }

  /**
   * Deploy a copy of the test data to a temporary directory. Required because the code being tested modifies files, so
   * we don't want to overwrite the project test data under "src/test/resources".
   *
   * @param testData
   *        The test data file to deploy to the temporary directory.
   * @param tempDir
   *        The path of a temporary directory where the test data should be deployed to.
   * @return The path of the copy of the test data.
   * @throws IOException
   *         When copying the test data fails.
   */
  protected Path copyTestData(final Path testData, final Path tempDir) throws IOException
  {
    // Copy the test data to the temporary directory
    final Path testDataCopy = tempDir.resolve(testData.getFileName());
    Files.copy(testData, testDataCopy);

    return testDataCopy;
  }

  /**
   * Ensure the output is correct, i.e. that the produced file has the correct contents, and that the temporary file
   * created during processing is deleted on completion.
   *
   * @param expectedFile
   *        A file containing the expected CSV output.
   * @param actualFile
   *        The file containing the actual CSV output.
   * @throws IOException
   *         When there is an I/O error during verification.
   */
  protected void assertOutputCorrect(final Path expectedFile, final Path actualFile) throws IOException
  {
    // Ensure the CSV file contents are correct
    Assertions.assertEquals(this.readFileAsString(expectedFile), this.readFileAsString(actualFile),
        "Produced CSV file contents are not as expected.");

    // Ensure temporary directory contains only one file (i.e. the temp file has been deleted)
    final int fileCount = actualFile.getParent().toFile().list().length;
    Assertions.assertEquals(fileCount, 1, String.format(
        "Temporary directory should contain just the test data file, but it actually contained %d files.", fileCount));
  }

  /**
   * Read a file into a string, normalising line endings to Unix to ensure tests are platform independent.
   *
   * @param fileToRead
   *        The file whose contents should be read into a string. Contents must be UTF-8 encoded.
   * @return The file contents as a string with Unix line endings.
   * @throws IOException
   *         When there is an I/O error reading the file.
   */
  protected String readFileAsString(final Path fileToRead) throws IOException
  {
    final byte[] fileContents = Files.readAllBytes(fileToRead);
    return new String(fileContents, StandardCharsets.UTF_8).replace("\r", "");
  }
}
