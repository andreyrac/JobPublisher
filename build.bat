rmdir /Q/S bin
del /Q/F filelist.txt
dir /s/b *.java >filelist.txt
mkdir bin
javac -d bin -g:lines -g:source -sourcepath src @filelist.txt