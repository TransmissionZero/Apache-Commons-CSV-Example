# Apache Commons CSV Example

## Table of Contents

  - [Introduction](#introduction)
  - [Building](#building)
  - [Usage](#usage)
  - [License](#license)

## Introduction

This is an example of reading and writing CSV files using the Apache Commons CSV library. The code takes a CSV file, then performs a find and replace operation for all matching values within a column of the CSV file.

## Building

You will need [Apache Maven](https://maven.apache.org/) and a Java JDK installed (the project was built and tested with the Java 8 LTS release on Windows and Linux, but it should be compatible with other JDK versions and OSes).

To build the jar and the site documentation, run `mvn clean verify site`. For further help, refer to the Apache Maven documentation.

## Usage

You are encouraged to refer to the JavaDoc in the Maven generated site for details on how the code is used, and debug the JUnit tests with breakpoints set in your IDE.

## License

This project is licensed under the Unlicense license, which means you can do whatever you like with this code. See `License.txt` for full details.