P2P Whiteboard (Java)

Simple peer-to-peer whiteboard using Java sockets and Swing.

Quick start (Windows):

1. Open a terminal (Command Prompt) in this folder.
2. Compile:

   javac WhiteboardPeer.java

3. Run two peers in separate terminals. Example:

   java WhiteboardPeer 5000
   java WhiteboardPeer 5001

4. In the second peer GUI, connect to the first by entering `localhost` and `5000`, then click `Connect`.

Notes:

- Each peer listens on the port you give when starting the app.
- Use `Connect` to open an outgoing connection to another peer; strokes are broadcast to connected peers.
- Protocol is simple text lines: `LINE x1 y1 x2 y2 r g b stroke` and `CLEAR`.
- This project is a minimal demo and does not implement discovery, NAT traversal, or reliable multi-peer topology management.

License: MIT-style (feel free to adapt)
