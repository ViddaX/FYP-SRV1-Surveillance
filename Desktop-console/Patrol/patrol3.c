void exit_prog()
{
	printf("yes");
	exit(1);
} 
while(1){	
	motors(50,50);
	delay(1000);
	motors(0,0);
	delay(3000);
	motors(40,0);
	delay(2400);
	motors(50,50);
	delay(1000);
	motors(0,0);
	delay(3000);
	motors(40,0);
	delay(2400);
	motors(0,0);
	printf("##requestService\n");
	if (signal()){
		exit_prog();
	}
}