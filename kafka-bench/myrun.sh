

CLASSPATH=".:/homes/hny9/chenxin/kafka/kafka-0.10.0.0-src/core/build/libs/kafka_2.10-0.10.0.0.jar:/homes/hny9/chenxin/kafka/kafka-0.10.0.0-src/core/build/dependant-libs-2.10.6/*"
CLASSPATH="$CLASSPATH:/homes/hny9/chenxin/kafka/kafka-0.10.0.0-src/tools/build/libs/kafka-tools-0.10.0.0.jar:/homes/hny9/chenxin/kafka/kafka-0.10.0.0-src/tools/build/dependant-libs-2.10.6/*"
CLASSPATH="$CLASSPATH:/homes/hny9/chenxin/kafka/kafka-0.10.0.0-src/clients/build/libs/kafka-clients-0.10.0.0.jar"
CLASSPATH="$CLASSPATH:/homes/hny9/chenxin/install/scala-2.11.8/lib/*.jar"

#scalac -cp $CLASSPATH ./com/consumer/*.scala 

#jar -cvfm ConsumerGroupExample.jar manifest.txt com/consumer/*.class

echo " start running ... "

#java -cp $CLASSPATH com.consumer.ConsumerPerformance --zookeeper localhost:2181 --messages 10 --topic ttt --threads 1  
java -cp $CLASSPATH com.consumer.ConsumerPerformance --new-consumer true --zookeeper localhost:2181 --broker-list localhost:9092 --messages 5000000 --topic ttt --threads 1 --group 2&
java -cp $CLASSPATH com.consumer.ConsumerPerformance --new-consumer true --zookeeper localhost:2181 --broker-list localhost:9092 --messages 5000000 --topic ttt --threads 1 --group 1 

