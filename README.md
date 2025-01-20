# Overview

Reads xml file and processes xml file respectively.

# Configuration files

Located in main/resources folder.

## config.csv

config file in form of csv

```shell
Document.ZVSPaymentArchive.VersionInterface;java.lang.String;versionInterface;MAP;INCLUDE
Document.ZVSPaymentArchive.DeliveringSystem;java.lang.String;delivery;MAP;INCLUDE
Document.ZVSPaymentArchive.ArchDt;java.time.LocalDate;archivizationDate;MAP;INCLUDE
Document.ZVSPaymentArchive.ArchFileNum;Integer;archivizationNumber;MAP;INCLUDE
Document.ZVSPaymentArchive.IsLastArchFile;Boolean;isLastArchiveFile;MAP;INCLUDE
Document.ZVSPaymentArchive.NbOfTxs;Integer;numberOfTransactions;MAP;INCLUDE
```

### Columns

```shell
[0] - xml path, without namespace
[1] - target class 
[2] - target name of the key
```

## payinfo_mapping.csv

example:

```shell
Document.ZVSPaymentArchive.PayInf.Amt;java.math.BigDecimal;amount
Document.ZVSPaymentArchive.PayInf.Amt.[Ccy];java.lang.String;currency
Document.ZVSPaymentArchive.PayInf.DlvrdExctDt;java.time.LocalDate;delivered_execution_date
```
### Columns

```shell
[0] - xml path, without namespace
[1] - target class 
[2] - target name of the property
```

# App arguments when starting app

```shell
arg[0] - path to xml file for processing
```
first argument is xml to parse

# Running app example

```shell
java -jar BigXmlReader-0.0.1-SNAPSHOT.jar EUMAREARCH.00001.1900.20241031195539.00011.Payment_formatted.xml
```