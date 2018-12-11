# avro-converter
Преобразование из csv формата в Avro и преобразование схемы. Задача реализована с помощью Apache NiFi.

За основу взят стандартный процессор Apache Nifi - kite. 
AvroConverter.xml -  Apache NiFi template
Dockerfile - для сборки образа ApacheNiFi c библиотеками avro-converter
titanic.csv - файл с входными данными
avro-converter/nifi-converter-nar/target/nifi-converter-nar-1.0-SNAPSHOT.nar - собранный проект

## data format
Входной формат - titanic.csv – входные данные в формате CSV 1,0,3,"Braund, Mr. Owen Harris",male,22,1,0,A/5 21171,7.25,,S)
Avro schema csv-Avro:

Формат преобразования из csv в Avro
{
  "type": "record",
  "name": "titanic",
  "namespace": "test.data.titanic",
  "doc": "",
  "fields": [
    {
        "name": "PassengerId",
        "doc": "increment id",
        "type": "int"
    },
    {
        "name": "Survived",
        "doc": "survival Survival 0 = No, 1 = Yes",
        "type": "int"
    },
    {
        "name": "Pclass",
        "doc": "pclass Ticket class 1 = 1st, 2 = 2nd, 3 = 3rd ",
        "type": "int"
    },
    {
        "name": "Name",
        "doc": "passengers name",
        "type": "string"
    },
    {
        "name": "Sex",
        "doc": "sex: female,male",
        "type": "string"
    },
    {
        "name": "Age",
        "doc": "Age in years ",
        "type": ["null", "float"]
    },
        {
        "name": "SibSp",
        "doc": "sibsp # of siblings / spouses aboard the Titanic",
        "type": "int"
    },
    {
        "name": "Parch",
        "doc": "parch # of parents / children aboard the Titanic",
        "type": "int"
    },
    {
        "name": "Ticket",
        "doc": "Ticket number",
        "type": "string"
    },
    {
        "name": "Fare",
        "doc": "Passenger fare",
        "type": "float"
    },
    {
        "name": "Cabin",
        "doc": "Cabin number",
        "type": ["null", "string"]
    },
    {
        "name": "Embarked",
        "doc": "embarked Port of Embarkation C = Cherbourg, Q = Queenstown, S = Southampton",
        "type": "string"
    }
  ]
}


Выходная схема Avro:
{
  "type": "record",
  "name": "titanic",
  "namespace": "test.data.titanic.short",
  "doc": "",
  "fields": [
    {
        "name": "Gender",
        "doc": "sex: female,male",
        "type": "int"
    },
    {
        "name": "Year",
        "doc": "Age in years ",
        "type": ["null", "float"]
    }
  ]
}

## install & deploy
Сборка проекта - mvn clean install 

Загрузка Apache NiFi и добавить собранный *.nar в директорию nifi/lib

Сборка Docker образа:
docker build -t nifitest .  
docker run -p 8080:8080 nifitest

Зайти на http://127.0.0.1:8080/nifi и загрузить template (инструкция по загрузке  темлейтов ( https://docs.hortonworks.com/HDPDocuments/HDF3/HDF-3.1.1/bk_getting-started-with-apache-nifi/content/working-with-templates.html )

Запуск собранного docker образа из docker hub
https://hub.docker.com/r/255945/avro-converter/

docker pull 255945/avro-converter
