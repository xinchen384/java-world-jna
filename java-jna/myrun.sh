



gcc -o ./libctest.so -shared cfunc/ctest.c -fPIC 

javac -classpath .:jna.jar -g com/sun/jna/examples/HelloWorld.java
 
java -classpath .:jna.jar  -Djna.library.path=/homes/hny9/chenxin/scala-c/java-jna  com.sun.jna.examples.HelloWorld 


