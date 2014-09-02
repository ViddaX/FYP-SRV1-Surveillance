int x;
int y = 0;
while (y<4){
	vcap();
	x = vjpeg(4);
	vsend(x);
	motors(50,0);
	delay(1000);
	motors(0,0);
	delay(1000);
	y++;
}
