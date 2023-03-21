# Java Chat Room
This project is a multi-client chat room implemented by Java. It has these functions: 
1. Login
2. Register
3. Text Chatting
4. Emoji
5. Sending Pictures
6. Sending Audios (from files or microphone)
7. Voice Chat
8. Online User List
9. Message History

## License
BSD 3-Clause "New" or "Revised" License

## Tested on
Operating System:
- macOS Ventura 13.2.1
- Windows 11 22H2

Java Runtime Environment:
- GraalVM CE 22.3.1
- Microsoft Build of OpenJDK 17.0.6

## Known Issues
Platform Related:
- On Windows, the emojis are black and white, and it seems to be a font issue.
- On Windows with OpenJDK version 17, the default charset will lead to squares when displaying emojis and Chinese characters. To solve it, you should either update your JRE version, or add "-Dfile.encoding=UTF-8" to the command-line argument.
- On macOS, the height of ComboBox is not changable. 

## Implementation Details
1. Java Swing for Java GUI
2. Java Thread for chat client and voice chat client.
3. Java Socket for chat client, voice chat client, login, and register.
4. javax.sound.sampled for playing and recording audio.
5. SQLite database for storing data on server.
6. sqlite-jdbc for connecting SQLite database to Java.
7. File I/O for message history.

## Usage
1. Running the program. On most of the operating systems, double click on "ChatRoom.jar" is enough. But if it's not applicable for your operating system, you can manually run it by executing "java -jar ChatRoom.jar" in the terminal. Be sure that the version of JRE must be 17 or above.
2. Entry form. This form has two buttons: "Server Mode" and "Client Mode." You can enter either of the modes by clicking on the specific button.
3. Server form. This form has three components: one "Server Address" text box, one "Server Port" text box, and one "Start Server" button. The default value of "Server Address" is 127.0.0.1, that is, it is a loopback address. To bind your server to some specific address, please enter it in this text box. Or if you want to broadcast it to all of the addresses, please input "0.0.0.0" to the text box. The default value of "Server Port" is 32768. If this port is occupied or you want to use another port, please modify this field. Click "Start Server" to start the server.
4. Login form. This form is used to login. You must set the "Server Address" and "Server Port" fields to the exact address and port of server. Then you can enter the username and the password in the following two fields. The content of the password field is invisible. You'll only see dots on it. Then you can click on "Login" button or hit the "enter/return" key on the keyboard, and you'll be in "Client" form. If you don't have an account, you can register one after clicking the "Register" button.
5. Register form. This form is used to register. You must set the "Server Address" and "Server Port" fields to the exact address and port of server. Then enter your username, age, address, and password in the following fields. You should also choose your gender in the "Gender" combo box. After completing these works, you should click on "Register" button and it will pop up an window indicating the register process is completed.
6. Client form. This is the main form used for sending and receiving message. The list on the right shows the users currently in the room. The list on the left shows the messages. The text box is the place that you can input your message. The message currently can only be one line. And the send button is used to send the message. You can also hit "enter/return" in the text box to send the message.
7. Sending text. Just input the text to the text box and click on "Send" or hit "enter/return" to send the message. You can enter any visible symbol in Unicode standard. However, the displayability of the character depends on the font of your operating system.
8. Sending emojis. You can directly input the emoji in Unicode standard. You can also click on "Emoji" button and it will pop up a window letting you choose one emoji. The window contains a subset of Unicode emojis. Then you can click on "Choose," and the emoji will be added to the text box.
9. Sending pictures. You can click on "Image" button, and it will pop up a file-choosing window. You can choose one image in this window. After clicking on "Choose," the picture will be sent. If the image is large, it may take a few seconds. After that, the thumbnail of the image will appear in the message list. Click on the image and it will pop up one "Image Viewer." This enables you to view the full sized image and you can change the size of this window.
10. Sending audios (sending videos in the requirement but I doubt if it is a typo). You can click on "Audio" button, and it will pop up a file-choosing window. You can choose one audio file in this window. After clicking on "Choose," the audio will be sent. You can also click on "Speak" and then record the sound. Click "Stop" to stop recording. Then your sound will be sent. Note that if you're on macOS, you may have to grant the permission of using microphone. One "Play Audio" button will appear in the message list. Click on the button to play the sound. If the sound if playing, you can click on "Stop Audio" to stop the audio.
11. Voice chat. Click on "Voice Chat" to get into the voice chat room. Click on "Stop Chat" to stop chatting. Note that you must have two or more computers to test this function, because you won't be able to hear your own sound when chatting.