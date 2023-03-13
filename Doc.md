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