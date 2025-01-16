# Overview

Reads config.csv as configuration and xml file and processes xml file respectively.

# config.csv

config file in form of csv

```shell
ZVSPaymentArchive.VersionInterface;java.lang.String;versionInterface;MAP;INCLUDE
ZVSPaymentArchive.DeliveringSystem;java.lang.String;delivery;MAP;INCLUDE
ZVSPaymentArchive.ArchDt;java.time.LocalDate;archivizationDate;MAP;INCLUDE
ZVSPaymentArchive.ArchFileNum;Integer;archivizationNumber;MAP;INCLUDE
ZVSPaymentArchive.IsLastArchFile;Boolean;isLastArchiveFile;MAP;INCLUDE
ZVSPaymentArchive.NbOfTxs;Integer;numberOfTransactions;MAP;INCLUDE
ZVSPaymentArchive.PayInf;pl.bigxml.reader.domain.PayInfo;;LIST;EXCLUDE
```

## Columns

```shell
[0] - xml path, without namespace
[1] - target class 
[2] - target name of the field (only makes sense when using MAP)
[3] - Appearance - MAP or LIST, if MAP -> result will be placed into MAP with key of value [4], if LIST if will be placed in list
[4] - Processing - INCLUDE or EXCLUDE: INCLUDE -> it will be included in result object, EXCLUDE -> it will be excluded from result object
```


# App arguments when starting app

```shell
arg[0] - path to config.csv
arg[1] - path to xml file for processing
```

first argument is config.csv
second argument is xml to parse

# Running app example

```shell
java -jar BigXmlReader-0.0.1-SNAPSHOT.jar C:\Users\maxzu\Documents\cpl\zadanko_od_piotra\config.csv C:\Users\maxzu\Documents\cpl\zadanko_od_piotra\EUMAREARCH.00001.1900.20241031195539.00011.Payment_formatted.xml
```