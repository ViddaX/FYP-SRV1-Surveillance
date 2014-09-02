int size;
vcap();
size = vjpeg(4);
printf("##IMJ5%c%c%c%c", size & 0x000000FF, (size & 0x0000FF00) / 0x100, (size & 0x00FF0000) / 0x10000, 0); 
vsend(size);
exit(1);
