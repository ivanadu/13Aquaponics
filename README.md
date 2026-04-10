OverflowMonitor.java contains runnable code with indicated variables to change to run on your own device. It reads from the City of London overflow website (https://london.waterwai.com) and detects when an overflow is active. It can also be used to read local files to immitate overflow/non-overflow conditions.  

alarms.php contains website data downloaded when there was an overflow active at the Adelaide treatment facility. This is a local file and immitates reading data from the actual website. This is for testing purposes.

alarms.php contains website data downloaded when there was no overflows active. This also is a local file and immitates reading data from the actual website. This is for testing purposes.

Devices running the program must be an Apple device to generate an iMessage notifcation to Overflow notices. If not, code should still run, but will only generate a response in the Java console.
