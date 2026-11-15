/*
 * ============================================================================
 *  MusicPlaylistManager.java
 *  Data Structures Mini-Project: Music Playlist Manager
 *  Core idea: a hand-built (Circular-capable) DOUBLY LINKED LIST + Swing GUI
 *
 *  Compile :  javac MusicPlaylistManager.java
 *  Run     :  java MusicPlaylistManager
 *  Needs   :  JDK 8 or newer, no external libraries.
 * ============================================================================
 */

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

public class MusicPlaylistManager {

    // ========================================================================
    //  1. SONG NODE
    // ========================================================================
    static class SongNode {
        final String title;
        final String artist;
        final int durationSeconds;

        SongNode prev; 
        SongNode next; 

        SongNode(String title, String artist, int durationSeconds) {
            this.title = title;
            this.artist = artist;
            this.durationSeconds = durationSeconds;
            this.prev = null; 
            this.next = null;
        }

        String durationText() {
            return String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60);
        }
    }

    // ========================================================================
    //  2. DOUBLY LINKED LIST
    // ========================================================================
    static class PlaylistDLL {
        private SongNode head;
        private SongNode tail;
        private SongNode current;
        private int size;
        private boolean circular;
        private final Random random = new Random();

        public boolean isEmpty()        { return size == 0; }
        public int size()               { return size; }
        public boolean isCircular()     { return circular; }
        public SongNode getHead()       { return head; }
        public SongNode getTail()       { return tail; }
        public SongNode getCurrent()    { return current; }

        public void setCurrent(SongNode node) {
            if (node != null) {
                current = node;
            }
        }

        private void unlinkCircle() {
            if (head != null) {
                head.prev = null; 
                tail.next = null; 
            }
        }

        private void linkCircle() {
            if (head != null) {
                tail.next = head; 
                head.prev = tail; 
            }
        }

        public SongNode addSong(String title, String artist, int durationSeconds) {
            if (circular) unlinkCircle();
            SongNode node = new SongNode(title, artist, durationSeconds);
            if (head == null) {
                head = node;
                tail = node;
                current = node;
            } else {
                tail.next = node;
                node.prev = tail;
                tail = node;
            }
            size++;
            if (circular) linkCircle();
            return node;
        }

        public boolean removeSong(SongNode target) {
            if (target == null || size == 0) return false;
            if (circular) unlinkCircle();
            if (current == target) {
                current = (target.next != null) ? target.next : target.prev;
            }
            if (target.prev != null) target.prev.next = target.next;
            else head = target.next;
            
            if (target.next != null) target.next.prev = target.prev;
            else tail = target.prev;
            
            target.prev = null; target.next = null; size--;
            if (size == 0) { head = null; tail = null; current = null; }
            if (circular) linkCircle();
            return true;
        }

        public boolean playNext() {
            if (current == null || current.next == null) return false;
            current = current.next;
            return true;
        }

        public boolean playPrevious() {
            if (current == null || current.prev == null) return false;
            current = current.prev;
            return true;
        }

        public boolean loopPlaylist() {
            if (!circular) { circular = true; linkCircle(); } 
            else { unlinkCircle(); circular = false; }
            return circular;
        }

        public void shufflePlaylist() {
            if (size < 2) return;
            if (circular) unlinkCircle();
            SongNode[] nodes = new SongNode[size];
            int i = 0;
            for (SongNode n = head; n != null; n = n.next) nodes[i++] = n;
            for (int k = size - 1; k > 0; k--) {
                int j = random.nextInt(k + 1); 
                SongNode tmp = nodes[k]; nodes[k] = nodes[j]; nodes[j] = tmp;
            }
            for (int k = 0; k < size; k++) {
                nodes[k].prev = (k == 0) ? null : nodes[k - 1];        
                nodes[k].next = (k == size - 1) ? null : nodes[k + 1]; 
            }
            head = nodes[0]; tail = nodes[size - 1];
            if (circular) linkCircle();
        }

        public SongNode getNodeAt(int index) {
            if (index < 0 || index >= size) return null;
            if (index <= size / 2) {
                SongNode n = head;
                for (int i = 0; i < index; i++) n = n.next;
                return n;
            } else {
                SongNode n = tail;
                for (int i = size - 1; i > index; i--) n = n.prev;
                return n;
            }
        }

        public int indexOf(SongNode target) {
            SongNode n = head;
            for (int i = 0; i < size; i++) { 
                if (n == target) return i;
                n = n.next;
            }
            return -1;
        }

        public String renderChain() {
            if (head == null) return "NULL   (the playlist is empty)";
            StringBuilder sb = new StringBuilder();
            sb.append(circular ? "(" + name(tail) + ") <-> " : "NULL <-> ");
            SongNode n = head;
            for (int i = 0; i < size; i++) {
                sb.append('[').append(n == current ? "> " : "")
                  .append(shorten(n.title, LABEL_MAX)).append(']').append(" <-> ");
                n = n.next;
            }
            sb.append(circular ? "(" + name(head) + ")" : "NULL");
            return sb.toString();
        }

        public String renderVisualization() {
            StringBuilder sb = new StringBuilder();
            sb.append("MODE     : ").append(circular ? "CIRCULAR" : "LINEAR").append('\n');
            sb.append("SIZE     : ").append(size).append('\n');
            sb.append("HEAD     : ").append(name(head)).append('\n');
            sb.append("TAIL     : ").append(name(tail)).append('\n');
            sb.append("CURRENT  : ").append(name(current)).append("\n\n");
            sb.append("LIST CHAIN\n").append(renderChain()).append("\n\n");
            sb.append("POINTER TABLE\n");
            sb.append(String.format("%-4s%-18s%-18s%-18s\n", "#", "NODE", "PREV", "NEXT"));
            sb.append("--------------------------------------------------------\n");
            SongNode n = head;
            for (int i = 0; i < size; i++) {
                String label = (n == current ? "> " : "  ") + shorten(n.title, LABEL_MAX - 2);
                sb.append(String.format("%-4d%-18s%-18s%-18s\n", i + 1, label, name(n.prev), name(n.next)));
                n = n.next;
            }
            return sb.toString();
        }
    }

    // ========================================================================
    //  3. CSV PERSISTENCE
    // ========================================================================
    static class CsvStorage {
        static final String HEADER = "Title,Artist,DurationSeconds";

        static void save(PlaylistDLL list, Path file) throws IOException {
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                w.write(HEADER); w.newLine();
                SongNode n = list.getHead();
                for (int i = 0; i < list.size(); i++) {
                    w.write(escape(n.title) + "," + escape(n.artist) + "," + n.durationSeconds);
                    w.newLine(); n = n.next;
                }
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }

        static int load(PlaylistDLL list, Path file) throws IOException {
            int loaded = 0;
            try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line; boolean firstLine = true;
                while ((line = r.readLine()) != null) {
                    if (firstLine) { firstLine = false; if (line.startsWith("\uFEFF")) line = line.substring(1); }
                    if (line.trim().isEmpty()) continue;
                    String[] f = parseLine(line);
                    if (f == null) continue;
                    if (f[0].trim().equalsIgnoreCase("Title") && f[1].trim().equalsIgnoreCase("Artist")) continue;
                    int seconds = parseDuration(f[2]);
                    if (f[0].trim().isEmpty() || seconds < 0) continue;
                    list.addSong(f[0].trim(), f[1].trim().isEmpty() ? "Unknown Artist" : f[1].trim(), seconds);
                    loaded++;
                }
            }
            return loaded;
        }

        private static String escape(String s) {
            if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0) return "\"" + s.replace("\"", "\"\"") + "\"";
            return s;
        }

        private static String[] parseLine(String line) {
            String[] fields = new String[3]; StringBuilder cur = new StringBuilder();
            boolean inQuotes = false; int count = 0;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (inQuotes) {
                    if (c == '"') {
                        if (i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; } 
                        else { inQuotes = false; }
                    } else { cur.append(c); }
                } else if (c == '"') { inQuotes = true;
                } else if (c == ',') {
                    if (count < 3) fields[count] = cur.toString();
                    count++; cur.setLength(0);
                } else { cur.append(c); }
            }
            if (count < 3) fields[count] = cur.toString(); 
            count++;
            return (count >= 3 && fields[0] != null && fields[1] != null && fields[2] != null) ? fields : null;
        }
    }

    // ========================================================================
    //  SMALL SHARED HELPERS
    // ========================================================================
    static final int LABEL_MAX = 16;
    static String shorten(String s, int max) { return s.length() <= max ? s : s.substring(0, Math.max(1, max - 3)) + "..."; }
    static String name(SongNode n) { return n == null ? "NULL" : shorten(n.title, LABEL_MAX); }
    static int parseDuration(String raw) {
        if (raw == null || raw.trim().isEmpty()) return -1;
        try {
            int colon = raw.indexOf(':');
            if (colon >= 0) return Integer.parseInt(raw.substring(0, colon).trim()) * 60 + Integer.parseInt(raw.substring(colon + 1).trim());
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) { return -1; }
    }
    static String clean(String s) { return s == null ? "" : s.replace('\n', ' ').replace('\r', ' ').trim(); }
    static String html(String s) { return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    // ========================================================================
    //  4. THEME & GUI COMPONENTS
    // ========================================================================
    static class Theme {
        static final Color BG            = new Color(0x000000); // Pitch Black
        static final Color CARD          = new Color(0x1E1E1E); 
        static final Color FIELD         = new Color(0x2A2A2A); 
        static final Color BORDER        = new Color(0x3A3A3A);
        static final Color SELECT        = new Color(0x2F2F2F);
        static final Color TEXT          = Color.WHITE;
        static final Color MUTED         = new Color(0xB3B3B3);
        static final Color ACCENT        = new Color(0x00E5FF); // Electric Blue
        static final Color ACCENT_HOVER  = new Color(0x4DEDFF); 
        static final Color BTN           = new Color(0x2E2E2E);
        static final Color BTN_HOVER     = new Color(0x424242);
        static final Color DANGER        = new Color(0x333333); // Dark Grey
        static final Color DANGER_HOVER  = new Color(0x4D4D4D); 

        static final String SANS = "SansSerif";
        static final String MONO = "Monospaced";
        static Font sans(int style, int size) { return new Font(SANS, style, size); }
        static Font mono(int style, int size) { return new Font(MONO, style, size); }
    }

    static class ModernButton extends JButton {
        private Color normal, hover;
        ModernButton(String text, Color normal, Color hover, Color foreground) {
            super(text); this.normal = normal; this.hover = hover;
            setForeground(foreground); setBackground(normal); setFont(Theme.sans(Font.BOLD, 15));
            setFocusPainted(false); setContentAreaFilled(false); setOpaque(false);
            setBorder(new EmptyBorder(13, 14, 13, 14)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { setBackground(ModernButton.this.hover); }
                @Override public void mouseExited(MouseEvent e)  { setBackground(ModernButton.this.normal); }
            });
        }
        void setColors(Color normal, Color hover, Color foreground) {
            this.normal = normal; this.hover = hover; setForeground(foreground);
            setBackground(getMousePosition() != null ? hover : normal);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? getBackground().darker() : getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
            g2.dispose(); super.paintComponent(g); 
        }
    }

    static class RoundedPanel extends JPanel {
        private final Color fill; private final int arc;
        RoundedPanel(Color fill, int arc) { this.fill = fill; this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose(); super.paintComponent(g);
        }
    }

    static class DarkScrollBarUI extends BasicScrollBarUI {
        @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
        private JButton zeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        @Override protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle r) {
            g.setColor(Theme.CARD); g.fillRect(r.x, r.y, r.width, r.height);
        }
        @Override protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(0x6A6A6A) : new Color(0x4A4A4A));
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8); g2.dispose();
        }
    }

    /** Custom sleek slider for the progress bar */
    static class DarkSliderUI extends BasicSliderUI {
        DarkSliderUI(JSlider b) { super(b); }
        @Override public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.Rectangle t = trackRect;
            g2.setColor(new Color(0x333333));
            g2.fillRoundRect(t.x, t.y + t.height / 2 - 2, t.width, 4, 4, 4);
            int fillLeft = t.x;
            int fillRight = thumbRect.x + thumbRect.width / 2;
            g2.setColor(Theme.ACCENT);
            g2.fillRoundRect(fillLeft, t.y + t.height / 2 - 2, Math.max(0, fillRight - fillLeft), 4, 4, 4);
        }
        @Override public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillOval(thumbRect.x, thumbRect.y + thumbRect.height / 2 - 6, 12, 12);
        }
    }

    static class SongListModel extends AbstractListModel<String> {
        private final PlaylistDLL playlist;
        SongListModel(PlaylistDLL playlist) { this.playlist = playlist; }
        @Override public int getSize() { return playlist.size(); }
        @Override public String getElementAt(int index) {
            SongNode n = playlist.getNodeAt(index); return n == null ? "" : n.title;
        }
    }

    // ========================================================================
    //  6. MAIN WINDOW
    // ========================================================================
    static class PlaylistFrame extends JFrame {
        private final PlaylistDLL playlist = new PlaylistDLL();
        private final Path csvPath = Paths.get("playlist.csv");

        private JLabel npCaption, npTitle, npArtist, npMeta, npPointers, libraryTitle, statusLabel;
        private JTextArea visualizer;
        private JList<String> songList;
        private ModernButton loopButton, playPauseButton;
        
        // Progress Bar & Timer Components
        private JSlider progressBar;
        private JLabel currentTimeLabel, totalTimeLabel;
        private Timer playbackTimer;
        private int elapsedSeconds = 0;
        private boolean isDragging = false;
        private SongNode lastPlayedNode = null;
        private boolean playing = false; 

        PlaylistFrame() {
            super("Playlist Manager");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            buildUi();
            loadPlaylist();   
            refresh();
            setSize(1140, 780);
            setMinimumSize(new Dimension(1000, 680));
            setLocationRelativeTo(null);

            // The Simulation Timer: Ticks every 1000ms (1 second)
            playbackTimer = new Timer(1000, e -> {
                if (playing && playlist.getCurrent() != null && !isDragging) {
                    elapsedSeconds++;
                    int dur = playlist.getCurrent().durationSeconds;
                    if (elapsedSeconds >= dur) {
                        onNext(); // Auto-play the next song!
                    } else {
                        updateProgressUI();
                    }
                }
            });
            playbackTimer.start();
        }

        private void updateProgressUI() {
            if (playlist.getCurrent() != null) {
                currentTimeLabel.setText(formatTime(elapsedSeconds));
                progressBar.setValue(elapsedSeconds);
            }
        }

        private String formatTime(int secs) {
            return String.format("%d:%02d", secs / 60, secs % 60);
        }

        private void buildUi() {
            JPanel root = new JPanel(new BorderLayout(0, 22));
            root.setBackground(Theme.BG);
            root.setBorder(new EmptyBorder(28, 32, 20, 32));
            setContentPane(root);

            JPanel header = new JPanel(new GridLayout(2, 1, 0, 2)); header.setOpaque(false);
            JLabel appTitle = new JLabel("Playlist Manager"); appTitle.setFont(Theme.sans(Font.BOLD, 34)); appTitle.setForeground(Theme.TEXT);
            JLabel appSub = new JLabel("Powered by a custom Doubly Linked List"); appSub.setFont(Theme.sans(Font.PLAIN, 15)); appSub.setForeground(Theme.ACCENT);
            header.add(appTitle); header.add(appSub); root.add(header, BorderLayout.NORTH);

            JPanel centre = new JPanel(new BorderLayout(22, 0)); centre.setOpaque(false);
            centre.add(buildLibraryCard(), BorderLayout.WEST); centre.add(buildPlayerColumn(), BorderLayout.CENTER);
            root.add(centre, BorderLayout.CENTER);

            statusLabel = new JLabel(" "); statusLabel.setFont(Theme.sans(Font.PLAIN, 13)); statusLabel.setForeground(Theme.MUTED);
            statusLabel.setBorder(new EmptyBorder(4, 6, 0, 0)); root.add(statusLabel, BorderLayout.SOUTH);
        }

        private JComponent buildLibraryCard() {
            RoundedPanel card = new RoundedPanel(Theme.CARD, 28); card.setLayout(new BorderLayout(0, 14));
            card.setBorder(new EmptyBorder(22, 22, 22, 22)); card.setPreferredSize(new Dimension(350, 0));
            libraryTitle = new JLabel("LIBRARY"); libraryTitle.setFont(Theme.sans(Font.BOLD, 13)); libraryTitle.setForeground(Theme.MUTED);
            card.add(libraryTitle, BorderLayout.NORTH);

            songList = new JList<>(new SongListModel(playlist)); songList.setBackground(Theme.CARD); songList.setSelectionBackground(Theme.SELECT);
            songList.setFixedCellHeight(62); songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            songList.setCellRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
                    JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
                    SongNode n = playlist.getNodeAt(index);
                    if (n != null) {
                        boolean isCur = (n == playlist.getCurrent());
                        String titleColor = isCur ? "#00E5FF" : "#FFFFFF"; // Electric Blue Sync
                        l.setText("<html><b><font color='" + titleColor + "'>" + (isCur ? "> " : "") + html(n.title) + "</font></b><br><font color='#B3B3B3' size='-1'>" + html(n.artist) + "  |  " + n.durationText() + "</font></html>");
                    }
                    l.setFont(Theme.sans(Font.PLAIN, 15)); l.setOpaque(true); l.setBackground(isSelected ? Theme.SELECT : Theme.CARD); l.setBorder(new EmptyBorder(6, 14, 6, 10));
                    return l;
                }
            });
            songList.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int i = songList.locationToIndex(e.getPoint());
                        java.awt.Rectangle r = (i >= 0) ? songList.getCellBounds(i, i) : null;
                        if (r != null && r.contains(e.getPoint())) {
                            playlist.setCurrent(playlist.getNodeAt(i));
                            playing = true; refresh();
                        }
                    }
                }
            });
            card.add(styledScroll(songList), BorderLayout.CENTER);

            JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0)); buttons.setOpaque(false);
            ModernButton add = new ModernButton("+ Add", Theme.ACCENT, Theme.ACCENT_HOVER, Color.BLACK);
            ModernButton remove = new ModernButton("x Remove", Theme.DANGER, Theme.DANGER_HOVER, Color.WHITE);
            add.addActionListener(e -> showAddDialog()); remove.addActionListener(e -> onRemove());
            buttons.add(add); buttons.add(remove); card.add(buttons, BorderLayout.SOUTH);
            return card;
        }

        private JComponent buildPlayerColumn() {
            JPanel column = new JPanel(new BorderLayout(0, 20)); column.setOpaque(false);

            RoundedPanel now = new RoundedPanel(Theme.CARD, 28);
            now.setLayout(new BoxLayout(now, BoxLayout.Y_AXIS)); now.setBorder(new EmptyBorder(24, 28, 12, 28));

            npCaption = new JLabel("NOW PLAYING"); npCaption.setFont(Theme.sans(Font.BOLD, 13)); npCaption.setForeground(Theme.ACCENT);
            npTitle = new JLabel(" "); npTitle.setFont(Theme.sans(Font.BOLD, 32)); npTitle.setForeground(Theme.TEXT);
            npArtist = new JLabel(" "); npArtist.setFont(Theme.sans(Font.PLAIN, 19)); npArtist.setForeground(Theme.MUTED);
            npMeta = new JLabel(" "); npMeta.setFont(Theme.sans(Font.PLAIN, 14)); npMeta.setForeground(Theme.MUTED);
            npPointers = new JLabel(" "); npPointers.setFont(Theme.mono(Font.PLAIN, 13)); npPointers.setForeground(Theme.ACCENT);

            JLabel[] labels = {npCaption, npTitle, npArtist, npMeta, npPointers};
            for (JLabel l : labels) l.setAlignmentX(Component.LEFT_ALIGNMENT);
            now.add(npCaption); now.add(Box.createVerticalStrut(10)); now.add(npTitle); now.add(Box.createVerticalStrut(4));
            now.add(npArtist); now.add(Box.createVerticalStrut(12)); now.add(npMeta); now.add(Box.createVerticalStrut(6)); now.add(npPointers);

            // ----- Progress Bar (Slider) -----
            JPanel progressPanel = new JPanel(new BorderLayout(15, 0));
            progressPanel.setOpaque(false); progressPanel.setBorder(new EmptyBorder(10, 28, 10, 28));

            currentTimeLabel = new JLabel("0:00"); currentTimeLabel.setFont(Theme.mono(Font.PLAIN, 12)); currentTimeLabel.setForeground(Theme.MUTED);
            totalTimeLabel = new JLabel("0:00"); totalTimeLabel.setFont(Theme.mono(Font.PLAIN, 12)); totalTimeLabel.setForeground(Theme.MUTED);

            progressBar = new JSlider(0, 100, 0);
            progressBar.setOpaque(false); progressBar.setFocusable(false);
            progressBar.setUI(new DarkSliderUI(progressBar));
            progressBar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            progressBar.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { isDragging = true; }
                @Override public void mouseReleased(MouseEvent e) { 
                    isDragging = false; 
                    if (playlist.getCurrent() != null) {
                        elapsedSeconds = progressBar.getValue();
                        updateProgressUI();
                    }
                }
            });
            progressBar.addChangeListener(e -> {
                if (isDragging) currentTimeLabel.setText(formatTime(progressBar.getValue()));
            });

            progressPanel.add(currentTimeLabel, BorderLayout.WEST);
            progressPanel.add(progressBar, BorderLayout.CENTER);
            progressPanel.add(totalTimeLabel, BorderLayout.EAST);

            // ----- Transport Controls -----
            JPanel controls = new JPanel(new GridLayout(1, 5, 10, 0)); controls.setOpaque(false);
            ModernButton prev = new ModernButton("<< Prev", Theme.BTN, Theme.BTN_HOVER, Theme.TEXT);
            ModernButton next = new ModernButton("Next >>", Theme.BTN, Theme.BTN_HOVER, Theme.TEXT);
            ModernButton shuffle = new ModernButton("\u21C4 Shuffle", Theme.BTN, Theme.BTN_HOVER, Theme.TEXT);
            loopButton = new ModernButton("Loop: OFF", Theme.BTN, Theme.BTN_HOVER, Theme.TEXT);
            playPauseButton = new ModernButton("\u25B6 Play", Theme.ACCENT, Theme.ACCENT_HOVER, Color.BLACK);
            
            playPauseButton.addActionListener(e -> onPlayPause()); prev.addActionListener(e -> onPrevious());
            next.addActionListener(e -> onNext()); shuffle.addActionListener(e -> onShuffle()); loopButton.addActionListener(e -> onLoopToggle());
            controls.add(prev); controls.add(playPauseButton); controls.add(next); controls.add(shuffle); controls.add(loopButton);

            JPanel top = new JPanel(new BorderLayout(0, 5)); top.setOpaque(false);
            top.add(now, BorderLayout.NORTH); top.add(progressPanel, BorderLayout.CENTER); top.add(controls, BorderLayout.SOUTH);
            column.add(top, BorderLayout.NORTH);

            RoundedPanel viz = new RoundedPanel(Theme.CARD, 28); viz.setLayout(new BorderLayout(0, 12)); viz.setBorder(new EmptyBorder(22, 24, 22, 24));
            JLabel vizTitle = new JLabel("PLAYLIST VISUALIZER  |  live view of the linked list"); vizTitle.setFont(Theme.sans(Font.BOLD, 13)); vizTitle.setForeground(Theme.MUTED);
            viz.add(vizTitle, BorderLayout.NORTH);

            visualizer = new JTextArea(); visualizer.setEditable(false); visualizer.setLineWrap(true); visualizer.setWrapStyleWord(true);
            visualizer.setFont(Theme.mono(Font.PLAIN, 14)); visualizer.setBackground(Theme.CARD); visualizer.setForeground(new Color(0xE6E6E6));
            visualizer.setCaretColor(Theme.ACCENT); visualizer.setBorder(new EmptyBorder(4, 2, 4, 8));
            viz.add(styledScroll(visualizer), BorderLayout.CENTER); column.add(viz, BorderLayout.CENTER);
            return column;
        }

        private JScrollPane styledScroll(Component view) {
            JScrollPane sp = new JScrollPane(view); sp.setBorder(null); sp.getViewport().setBackground(Theme.CARD);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.getVerticalScrollBar().setUI(new DarkScrollBarUI()); sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
            sp.getVerticalScrollBar().setUnitIncrement(16); return sp;
        }

        private void onNext() {
            if (playlist.playNext()) status("Next >> : current = current.next");
            else status("current.next is NULL (end of a linear list). Turn Loop ON to wrap.");
            refresh();
        }

        private void onPrevious() {
            if (playlist.playPrevious()) status("<< Prev : current = current.prev");
            else status("current.prev is NULL (start of a linear list). Turn Loop ON to wrap.");
            refresh();
        }

        private void onShuffle() {
            if (playlist.size() < 2) return;
            playlist.shufflePlaylist(); persistAnd("Shuffled: every node's prev/next pointers were rewired"); refresh();
        }

        private void onLoopToggle() {
            boolean on = playlist.loopPlaylist();
            status(on ? "Loop ON: tail.next = head" : "Loop OFF: tail.next = NULL"); refresh();
        }

        private void onPlayPause() {
            if (playlist.isEmpty()) return;
            if (playlist.getCurrent() == null) playlist.playNext(); // start if null
            playing = !playing; refresh();
        }

        private void onRemove() {
            int index = songList.getSelectedIndex(); SongNode node = (index >= 0) ? playlist.getNodeAt(index) : null;
            if (node != null) { playlist.removeSong(node); persistAnd("Removed \"" + node.title + "\""); refresh(); }
        }

        private void showAddDialog() {
            JDialog d = new JDialog(this, "Add Song", true); JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS)); body.setBackground(Theme.BG); body.setBorder(new EmptyBorder(26, 30, 24, 30));

            JLabel heading = new JLabel("Add a new song"); heading.setFont(Theme.sans(Font.BOLD, 24)); heading.setForeground(Theme.TEXT);
            JTextField titleField = styledField(); JTextField artistField = styledField(); JTextField durationField = styledField();
            JLabel error = new JLabel(" "); error.setFont(Theme.sans(Font.PLAIN, 13)); error.setForeground(new Color(0xFF6B6B));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); actions.setOpaque(false);
            ModernButton cancel = new ModernButton("Cancel", Theme.BTN, Theme.BTN_HOVER, Theme.TEXT);
            ModernButton save = new ModernButton("Add", Theme.ACCENT, Theme.ACCENT_HOVER, Color.BLACK);
            actions.add(cancel); actions.add(save); cancel.addActionListener(e -> d.dispose());
            save.addActionListener(e -> {
                String title = clean(titleField.getText()); int seconds = parseDuration(durationField.getText());
                if (title.isEmpty() || seconds < 0) { error.setText("Invalid Title or Duration (m:ss)."); return; }
                playlist.addSong(title, clean(artistField.getText()), seconds); d.dispose(); persistAnd("Added"); refresh();
            });

            JComponent[] rows = {heading, caption("TITLE"), titleField, caption("ARTIST"), artistField, caption("DURATION (m:ss)"), durationField, error, actions};
            for (int i = 0; i < rows.length; i++) {
                rows[i].setAlignmentX(Component.LEFT_ALIGNMENT); body.add(rows[i]);
                if (i == 0) body.add(Box.createVerticalStrut(18)); else if (rows[i] instanceof JTextField) body.add(Box.createVerticalStrut(14)); else if (rows[i] instanceof JLabel && rows[i] != error) body.add(Box.createVerticalStrut(6));
            }
            d.setContentPane(body); d.getRootPane().setDefaultButton(save); d.pack(); d.setSize(Math.max(d.getWidth(), 440), d.getHeight()); d.setLocationRelativeTo(this); d.setVisible(true);
        }

        private JTextField styledField() {
            JTextField f = new JTextField(); f.setFont(Theme.sans(Font.PLAIN, 16)); f.setForeground(Theme.TEXT); f.setBackground(Theme.FIELD); f.setCaretColor(Theme.ACCENT);
            f.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(10, 12, 10, 12))); return f;
        }
        private JLabel caption(String text) { JLabel l = new JLabel(text); l.setFont(Theme.sans(Font.BOLD, 12)); l.setForeground(Theme.MUTED); return l; }

        private void loadPlaylist() {
            if (!Files.exists(csvPath)) {
                playlist.addSong("Sprinter", "Central Cee", 224); playlist.addSong("20 Min", "Lil Uzi Vert", 220);
                playlist.addSong("Cry for me", "The Weeknd", 222); playlist.addSong("Flashing Lights", "Kanye West", 237);
                playlist.addSong("Devil In A New Dress", "Kanye West", 351); playlist.addSong("Janice STFU", "Drake", 176); playlist.addSong("My Eyes", "Travis Scott", 251);
                persistAnd("Default tracks created"); return;
            }
            try { CsvStorage.load(playlist, csvPath); } catch (IOException ignored) {}
        }
        private void persistAnd(String msg) { try { CsvStorage.save(playlist, csvPath); status(msg); } catch (IOException ex) {} }
        private void status(String text) { statusLabel.setText(text); }

        private void refresh() {
            SongNode cur = playlist.getCurrent();

            // Detect if the song pointer physically changed so we can reset the timer to 0:00
            if (cur != lastPlayedNode) {
                elapsedSeconds = 0;
                lastPlayedNode = cur;
            }

            if (cur == null) {
                playing = false; npTitle.setText("Nothing playing"); npArtist.setText("Add a song to get started"); npMeta.setText(" "); npPointers.setText(" ");
                progressBar.setValue(0); progressBar.setMaximum(100); progressBar.setEnabled(false);
                currentTimeLabel.setText("0:00"); totalTimeLabel.setText("0:00");
            } else {
                npTitle.setText(cur.title); npArtist.setText(cur.artist);
                npMeta.setText("Track " + (playlist.indexOf(cur) + 1) + " of " + playlist.size());
                npPointers.setText("prev << " + name(cur.prev) + "      next >> " + name(cur.next));
                
                progressBar.setEnabled(true);
                progressBar.setMaximum(cur.durationSeconds);
                progressBar.setValue(elapsedSeconds);
                currentTimeLabel.setText(formatTime(elapsedSeconds));
                totalTimeLabel.setText(formatTime(cur.durationSeconds));
            }

            npCaption.setText(cur != null && !playing ? "NOW PLAYING (PAUSED)" : "NOW PLAYING");
            playPauseButton.setText(playing ? "|| Pause" : "\u25B6 Play");
            visualizer.setText(playlist.renderVisualization()); visualizer.setCaretPosition(0);

            libraryTitle.setText("LIBRARY  |  " + playlist.size() + " songs");
            songList.setModel(new SongListModel(playlist));
            int idx = playlist.indexOf(cur);
            if (idx >= 0) { songList.setSelectedIndex(idx); songList.ensureIndexIsVisible(idx); } else songList.clearSelection();

            if (playlist.isCircular()) { loopButton.setText("Loop: ON"); loopButton.setColors(Theme.ACCENT, Theme.ACCENT_HOVER, Color.BLACK); } 
            else { loopButton.setText("Loop: OFF"); loopButton.setColors(Theme.BTN, Theme.BTN_HOVER, Theme.TEXT); }
            repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
            new PlaylistFrame().setVisible(true);
        });
    }
}