import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple peer-to-peer whiteboard using Java Sockets.
 *
 * Usage:
 *  javac WhiteboardPeer.java
 *  java WhiteboardPeer <listenPort>
 *
 * In the running GUI you can optionally connect to another peer (host/port).
 */
public class WhiteboardPeer extends JFrame {
    private final int listenPort;
    private final List<PrintWriter> peerWriters = new CopyOnWriteArrayList<>();
    private final BufferedImage canvas;
    private Color currentColor = Color.BLACK;
    private float strokeWidth = 3f;
    private volatile boolean running = true;

    private int lastX = -1, lastY = -1;

    public WhiteboardPeer(int listenPort) {
        super("P2P Whiteboard - port " + listenPort);
        this.listenPort = listenPort;
        this.canvas = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        DrawPanel drawPanel = new DrawPanel();
        drawPanel.setPreferredSize(new Dimension(canvas.getWidth(), canvas.getHeight()));
        add(new JScrollPane(drawPanel), BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JTextField hostField = new JTextField("localhost", 12);
        JTextField portField = new JTextField("" + listenPort, 6);
        JButton connectBtn = new JButton("Connect");
        JButton colorBtn = new JButton("Color");
        JButton clearBtn = new JButton("Clear");
        JSpinner thickness = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));

        controls.add(new JLabel("Host:"));
        controls.add(hostField);
        controls.add(new JLabel("Port:"));
        controls.add(portField);
        controls.add(connectBtn);
        controls.add(colorBtn);
        controls.add(new JLabel("Thickness:"));
        controls.add(thickness);
        controls.add(clearBtn);
        add(controls, BorderLayout.NORTH);

        connectBtn.addActionListener(e -> {
            String host = hostField.getText().trim();
            int port;
            try { port = Integer.parseInt(portField.getText().trim()); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Bad port"); return; }
            connectToPeer(host, port);
        });

        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose color", currentColor);
            if (c != null) currentColor = c;
        });

        thickness.addChangeListener(e -> strokeWidth = ((Number)thickness.getValue()).floatValue());

        clearBtn.addActionListener(e -> {
            Graphics2D g = canvas.createGraphics();
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0,0,canvas.getWidth(), canvas.getHeight());
            g.dispose();
            drawPanel.repaint();
            broadcastMessage("CLEAR");
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        startServer();
    }

    private void startServer() {
        Thread t = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(listenPort)) {
                server.setReuseAddress(true);
                while (running) {
                    Socket s = server.accept();
                    try {
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
                        peerWriters.add(out);
                    } catch (IOException ioe) {
                        System.err.println("Failed to create writer for accepted socket: " + ioe.getMessage());
                    }
                    new Thread(() -> handleIncoming(s)).start();
                }
            } catch (IOException e) {
                System.err.println("Server socket error: " + e.getMessage());
            }
        }, "Server-Accept-Thread");
        t.setDaemon(true);
        t.start();
    }

    private void handleIncoming(Socket s) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                final String msg = line;
                SwingUtilities.invokeLater(() -> applyRemoteMessage(msg));
            }
        } catch (IOException e) {
            System.err.println("Incoming connection closed: " + e.getMessage());
        }
    }

    private void connectToPeer(String host, int port) {
        new Thread(() -> {
            try {
                Socket s = new Socket(host, port);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
                peerWriters.add(out);
                // do not close socket here; keep output stream alive
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Failed to connect: " + e.getMessage()));
            }
        }, "Connect-Thread").start();
    }

    private void broadcastMessage(String msg) {
        for (PrintWriter pw : peerWriters) {
            try {
                pw.println(msg);
            } catch (Exception ignored) {}
        }
    }

    private void applyRemoteMessage(String msg) {
        if (msg == null || msg.isEmpty()) return;
        if (msg.equals("CLEAR")) {
            Graphics2D g = canvas.createGraphics();
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0,0,canvas.getWidth(), canvas.getHeight());
            g.dispose();
            repaint();
            return;
        }
        // Expected: LINE x1 y1 x2 y2 r g b stroke
        if (msg.startsWith("LINE ")) {
            try {
                String[] parts = msg.substring(5).split(" ");
                int x1 = Integer.parseInt(parts[0]);
                int y1 = Integer.parseInt(parts[1]);
                int x2 = Integer.parseInt(parts[2]);
                int y2 = Integer.parseInt(parts[3]);
                int r = Integer.parseInt(parts[4]);
                int gcol = Integer.parseInt(parts[5]);
                int b = Integer.parseInt(parts[6]);
                float sw = Float.parseFloat(parts[7]);
                Graphics2D g2 = canvas.createGraphics();
                g2.setColor(new Color(r, gcol, b));
                g2.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x1, y1, x2, y2);
                g2.dispose();
                repaint();
            } catch (Exception e) {
                System.err.println("Bad LINE message: " + msg + " -> " + e.getMessage());
            }
        }
    }

    private class DrawPanel extends JPanel implements MouseListener, MouseMotionListener {
        public DrawPanel() {
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(canvas, 0, 0, null);
        }

        @Override public void mousePressed(MouseEvent e) { lastX = e.getX(); lastY = e.getY(); }
        @Override public void mouseReleased(MouseEvent e) { lastX = -1; lastY = -1; }
        @Override public void mouseDragged(MouseEvent e) {
            int x = e.getX(), y = e.getY();
            if (lastX >= 0) {
                Graphics2D g2 = canvas.createGraphics();
                g2.setColor(currentColor);
                g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(lastX, lastY, x, y);
                g2.dispose();
                repaint();
                String msg = String.format("LINE %d %d %d %d %d %d %d %.1f",
                        lastX, lastY, x, y, currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), strokeWidth);
                broadcastMessage(msg);
            }
            lastX = x; lastY = y;
        }
        @Override public void mouseMoved(MouseEvent e) {}
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java WhiteboardPeer <listenPort>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        SwingUtilities.invokeLater(() -> new WhiteboardPeer(port));
    }
}
