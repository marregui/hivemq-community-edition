```shell
cd src/main/c
```

```shell
g++ -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I${JAVA_HOME}/include/darwin files.cpp -o files.o
g++ -dynamiclib -o ../resources/libfiles.dylib files.o -lc
```

