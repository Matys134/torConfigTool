@echo off
set /p username="Enter your username: "
set /p password="Enter your password: "

java -cp UserHasher.jar com.example.userhasher.UserHasherApplication %username% %password%