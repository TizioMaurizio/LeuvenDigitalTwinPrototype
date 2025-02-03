Instructions to run EV3GUI

If you are on windows just run main.bat,
otherwise execute main.jar with java 11 with the following virtual machine parameters:
java -jar --module-path *PATH TO LIB FOLDER* --add-modules javafx.controls,javafx.fxml main.jar

the Illegal reflective access warning is an inevitable message of paho mqtt library, nothing to worry about

the application will generates mqtt persistence folders to store the mqtt messages while running, i'm working on the automatic deletion of them but at the moments you have to delete them manually after shutting down the program :p
