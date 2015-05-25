PC_DIR = ../../pc-2015-04-17
PC = $(PC_DIR)/pc

install: buffalo.js
	cp buffalo.js.mem ../resources/public/js/buffalo.js.mem
	cat pre.js buffalo.js > ../resources/public/js/buffalo.js

buffalo.js: buffalo.c
	emcc -O3 -std=gnu99 -I$(PC_DIR) buffalo.c -s MODULARIZE=1 -s "EXPORT_NAME='buffalo'" -s AGGRESSIVE_VARIABLE_ELIMINATION=1 -s TOTAL_MEMORY=150994944 -o buffalo.js

buffalo.c: export PC_LIBRARY_DIR = $(PC_DIR)/lib
buffalo.c: buffalo.pl
	$(PC) buffalo.pl
