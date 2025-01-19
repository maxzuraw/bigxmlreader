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
[2] - target name of the field (only makes sense when using MAP)
[3] - Appearance - MAP or LIST, if MAP -> result will be placed into MAP with key of value [4], if LIST if will be placed in list
[4] - Processing - INCLUDE or EXCLUDE: INCLUDE -> it will be included in result object, EXCLUDE -> it will be excluded from result object
```

## payinfo_mapping.csv

example:

```shell
PayInf.Amt;amount
PayInf.Amt.[Ccy];currency
PayInf.DlvrdExctDt;delivered_execution_date
PayInf.InSttlmInf.Amt;input_settlement_info_amount
PayInf.InSttlmInf.Amt.[Ccy];input_settlement_info_currency
```
### Columns

```shell
[0] - xml path, without namespace
[1] - target property 
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