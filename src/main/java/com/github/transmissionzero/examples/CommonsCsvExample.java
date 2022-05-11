package com.github.transmissionzero.examples;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * An example of using the Apache Commons CSV library to parse and write CSV files.
 */
public class CommonsCsvExample
{
  /** CSV format for reading and writing CSV. Uses RFC4180 format with a header record. */
  private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
    .setHeader()
    .setSkipHeaderRecord(true)
    .setAllowDuplicateHeaderNames(false)
    .build();

  /**
   * Perform a string find and replace operation in a given column of a CSV file.
   *
   * <p>
   * The operation involves writing the resulting CSV to a temporary file in the same directory as the input CSV file,
   * then replacing the original CSV file with the temporary file on success. The caller will need appropriate file
   * system permissions for writing the temporary file and replacing the original.
   * </p>
   *
   * @param csvFileName
   *        The path of CSV file to process. The file is expected to be UTF-8 encoded and using the RFC4180 CSV format.
   *        Must not be null.
   * @param column
   *        The name of the column where instances of <code>oldValue</code> should be replaced with
   *        <code>newValue</code>. Must not be null.
   * @param oldValue
   *        The value which should be replaced. Must not be null.
   * @param newValue
   *        The replacement value. Must not be null.
   * @throws IOException
   *         When there is an I/O error reading or writing the CSV file.
   */
  public void updateRowValues(final String csvFileName, final String column, final String oldValue,
      final String newValue) throws IOException
  {
    // Ensure parameters are not null
    Objects.requireNonNull(csvFileName);
    Objects.requireNonNull(column);
    Objects.requireNonNull(oldValue);
    Objects.requireNonNull(newValue);

    // Path of CSV file to read
    final Path csvFilePath = Paths.get(csvFileName);

    // Path of the temporary file to write work in progress CSV results to
    Path tempFile = null;

    try
    {
      // Open the CSV file for reading
      try (CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT))
      {
        // Don't waste resources processing the file if it doesn't contain the specified column
        if (!parser.getHeaderNames().contains(column))
          return;

        // Create the temporary file
        tempFile = Files.createTempFile(csvFilePath.getParent(), null, null);

        // Write the output CSV
        this.writeOutputCsv(parser, tempFile, column, oldValue, newValue);
      }

      // Replace original CSV file with the temporary file containing the results
      Files.move(tempFile, csvFilePath, StandardCopyOption.REPLACE_EXISTING);
    }
    catch (final Throwable t)
    {
      // Exceptions we don't handle can result in the temporary file not being deleted. Try to delete it.
      try
      {
        if (tempFile != null)
          Files.deleteIfExists(tempFile);
      }
      catch (final Throwable ignored)
      {
        // Ignored because it will hide the original exception
      }

      // Rethrow since we don't want to or don't know how to handle it
      throw t;
    }
  }

  /**
   * Perform the writing of the CSV to the output file, finding and replacing the specified value in the specified
   * column.
   *
   * @param parser
   *        The initialised CSV parser for reading the input file. Must not be null.
   * @param outputFile
   *        The file to write the resulting CSV to. Must not be null.
   * @param column
   *        The name of the column where instances of <code>oldValue</code> should be replaced with
   *        <code>newValue</code>. Must not be null.
   * @param oldValue
   *        The value which should be replaced. Must not be null.
   * @param newValue
   *        The replacement value. Must not be null.
   * @throws IOException
   *         When there is an I/O error reading or writing the CSV file.
   */
  protected void writeOutputCsv(final CSVParser parser, final Path outputFile, final String column,
      final String oldValue, final String newValue) throws IOException
  {
    // CSV format for writing CSV. Uses the same format and header names as the input CSV.
    final CSVFormat outputCsvFormat = CSVFormat.Builder.create(CSVFormat.RFC4180)
      .setHeader(parser.getHeaderNames().toArray(new String[0]))
      .build();

    // Open output CSV file for writing and create a CSV printer
    try (Writer outputFileWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8,
        StandardOpenOption.WRITE); CSVPrinter csvPrinter = new CSVPrinter(outputFileWriter, outputCsvFormat))
    {
      // Iterate over input CSV records
      for (final CSVRecord record : parser)
      {
        // Get all of the header names and associated values from the record
        final Map<String, String> recordValues = record.toMap();

        // Perform the find and replace operation
        recordValues.replace(column, oldValue, newValue);

        // Write the updated values to the output CSV
        csvPrinter.printRecord(recordValues.values());
      }
    }
  }
}
