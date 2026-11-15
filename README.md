# Music Playlist Manager 🎵
A Spotify-inspired Music Playlist Manager built entirely from scratch using a custom **Circular Doubly Linked List** in Java. 

This was developed as a Data Structures (DSA) Mini-Project to demonstrate how fundamental linked list operations can power modern UI features like song traversal, shuffling, and looping.

## ✨ Features
* **Custom Data Structure:** Powered by a hand-built `PlaylistDLL` class (no `java.util` collections used).
* **O(1) Time Complexity Operations:** Appending songs, removing songs, and next/prev traversal are all optimized to O(1) by maintaining head, tail, and current pointers.
* **Fisher-Yates Shuffle:** Randomizes the playlist by physically rewiring the memory pointers of the list nodes in O(n) time.
* **Dynamic Looping:** Toggles the list between a Linear and Circular Doubly Linked List on the fly by linking/unlinking the head and tail nodes.
* **Live Pointer Visualizer:** A built-in debug view that exposes the real-time memory state of the `prev` and `next` pointers as the playlist plays.
* **Native Persistence:** Automatically saves and loads the tracklist to a `playlist.csv` file using native Java I/O.
* **Custom Dark UI:** Built with Java Swing, featuring a pitch-black theme, electric blue accents, and a simulated playback scrubber.

## 🚀 How to Run
Ensure you have the Java Development Kit (JDK 8+) installed. No external libraries are required.

1. Clone the repository or download the files.
2. Open your terminal in the project directory.
3. Compile the code:
   javac MusicPlaylistManager.java
4. Run the application:
   java MusicPlaylistManager
