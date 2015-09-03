all: install test

install:
	mvn install -DskipTests 

test: install
	mvn test 

clean:
	mvn clean
