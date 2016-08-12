


/* ctest.c */


#include <stdio.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

#include <sys/sendfile.h>
#include <sys/types.h>
#include <stdint.h>
#define jlong_to_ptr(a) ((void*)(a))

// to compile
// gcc -o libctest.so -shared ctest.c -fPIC
void helloFromC() {
    printf("Hello from C!\n");
}

FILE *fp;
int fd;
char* file_name = NULL;
long written_size;

void myopenFile(const char* fname){
   fp = fopen(fname, "w+");
   fd = fileno(fp);
   if(!fp){
     puts("SPECTRE wins!");
     exit(1);
   }
   file_name = (char*)malloc(strlen(fname) + 1);
   strcpy(file_name, fname);
   written_size = 0;
}

//https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/src/solaris/native/sun/nio/ch/FileDispatcherImpl.c
//https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/share/classes/sun/nio/ch/FileChannelImpl.java
size_t mywrite(long addr, long count){
  void *buf = (void *)jlong_to_ptr(addr);
  //printf("XIN in C write contetn %s\n", buf);
  size_t res = write(fd, buf, count);
  //printf("XIN in C write bytes: %d with length: %d\n", res, count);
  written_size += res;
  return res;
  //fputs(pszVal, fp);
}
size_t mypwrite(long addr, long count, long offset){
  void *buf = (void *)jlong_to_ptr(addr);
  size_t res = pwrite(fd, buf, count, offset);
  //printf("XIN in C write bytes: %d with length: %d\n", res, count);
  written_size += res;
  return res;
}
size_t myread(long addr, long size){
  //*dest = (char*)malloc(sizeof(char) * size);
  //size_t res = pread(fd, *dest, size, pos);
  void *buf = (void *)jlong_to_ptr(addr);
  size_t res = read(fd, buf, size);
  printf("XIN in C read bytes: %d\n",  res);
  return res;
}
size_t mypread(long addr, long size, long pos){
  void *buf = (void *)jlong_to_ptr(addr);
  size_t res = pread(fd, buf, size, (int)pos); 
  printf("XIN in C pread bytes: %d with count %d offset %d \n",  res, size, pos);
  return res;
}

size_t mytransferTo(long pos, long count, int dest_fd){
  off_t offset = (off_t)pos;
  //uint64_t offset = (uint64_t)pos;
  return sendfile(dest_fd, fd, &offset, (size_t)count);
}
long myposition(long offset){
  if (offset < 0)
    return lseek(fd, 0, SEEK_CUR);
  else
    lseek(fd, offset, SEEK_SET);
}
size_t mytruncate(long t_size){
  return ftruncate(fd, t_size);
}

//////////////////////////
void mycleanup(char* pVals)
{
  free(pVals);
}

size_t mysize(){
  struct stat st;
    if(stat(file_name, &st) != 0) {
        return 0;
    }
    printf("XIN file size: in stat %d in writtenSize %d\n", st.st_size, written_size);
    return st.st_size; 
}
void myclose(){
  fclose(fp);
  free(file_name);
}
//http://stackoverflow.com/questions/4742543/filechannelforce-and-buffering
void myforce(int sign){
  if (sign) fsync(fd);
  else fdatasync(fd);
}



