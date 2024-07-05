import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TiroParabolicoApp extends JFrame {
    private JTextField initialVelocityField;
    private JTextField angleField;
    private JTextField maxDistanceField;
    private JTextField initialXField;
    private JTextField initialYField;
    private JLabel positionLabel;
    private JLabel timeLabel;
    private JButton startButton;
    private JButton resetButton;
    private JPanel canvasPanel;

    private double initialVelocity;
    private double angle;
    private double maxDistance;
    private double initialX;
    private double initialY;
    private final double gravity = 9.81;
    private int scale = 10;

    private double time;
    private double x, y;
    private final double timeInterval = 0.1;
    private Timer timer;

    private List<Point> trajectory;

    public TiroParabolicoApp() {
        setTitle("Simulación de Tiro Parabólico");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        trajectory = new ArrayList<>();

        initialVelocityField = new JTextField(10);
        angleField = new JTextField(10);
        maxDistanceField = new JTextField(10);
        initialXField = new JTextField(10);
        initialYField = new JTextField(10);
        positionLabel = new JLabel("Posición: (0, 0)");
        timeLabel = new JLabel("Tiempo: 0 s");
        startButton = new JButton("Iniciar");
        resetButton = new JButton("Reiniciar");
        canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawTrajectory(g);
            }
        };

        JScrollPane scrollPane = new JScrollPane(canvasPanel);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(new JLabel("Velocidad inicial (m/s): "), gbc);
        gbc.gridx = 1;
        controlPanel.add(initialVelocityField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(new JLabel("Ángulo de lanzamiento (grados): "), gbc);
        gbc.gridx = 1;
        controlPanel.add(angleField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        controlPanel.add(new JLabel("Distancia máxima (m): "), gbc);
        gbc.gridx = 1;
        controlPanel.add(maxDistanceField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        controlPanel.add(new JLabel("Posición inicial X (m): "), gbc);
        gbc.gridx = 1;
        controlPanel.add(initialXField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        controlPanel.add(new JLabel("Posición inicial Y (m): "), gbc);
        gbc.gridx = 1;
        controlPanel.add(initialYField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        controlPanel.add(startButton, gbc);
        gbc.gridx = 1;
        controlPanel.add(resetButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 6;
        controlPanel.add(positionLabel, gbc);
        gbc.gridx = 1;
        controlPanel.add(timeLabel, gbc);

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSimulation();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetSimulation();
            }
        });

        time = 0;

        timer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePosition();
                canvasPanel.repaint();
                if (y < 0 || x >= maxDistance) {
                    timer.stop();
                }
                time += timeInterval;
                timeLabel.setText(String.format("Tiempo: %.1f s", time));
            }
        });
    }

    private void startSimulation() {
        try {
            initialVelocity = Double.parseDouble(initialVelocityField.getText());
            angle = Math.toRadians(Double.parseDouble(angleField.getText()));
            maxDistance = Double.parseDouble(maxDistanceField.getText());
            initialX = Double.parseDouble(initialXField.getText());
            initialY = Double.parseDouble(initialYField.getText());

            if (initialVelocity <= 0 || angle < 0 || angle > Math.PI || maxDistance <= 0) {
                throw new NumberFormatException();
            }

            time = 0;
            x = initialX;
            y = initialY;
            trajectory.clear();
            positionLabel.setText(String.format("Posición: (%.1f, %.1f)", x, y));
            timeLabel.setText("Tiempo: 0 s");

            adjustCanvasSize();

            timer.start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa números válidos y positivos.");
        }
    }

    private void resetSimulation() {
        timer.stop();
        trajectory.clear();
        time = 0;
        x = 0;
        y = 0;
        canvasPanel.repaint();
        initialVelocityField.setText("");
        angleField.setText("");
        maxDistanceField.setText("");
        initialXField.setText("0");
        initialYField.setText("0");
        positionLabel.setText("Posición: (0, 0)");
        timeLabel.setText("Tiempo: 0 s");
    }

    private void updatePosition() {
        x = initialX + initialVelocity * Math.cos(angle) * time;
        y = initialY + (initialVelocity * Math.sin(angle) * time - 0.5 * gravity * time * time);

        trajectory.add(new Point((int) (x * scale), canvasPanel.getHeight() - 50 - (int) (y * scale)));

        positionLabel.setText(String.format("Posición: (%.1f, %.1f)", x, y));

        if (y < 0 || x >= maxDistance) {
            timer.stop();
        }
        time += timeInterval;
    }

    private void adjustCanvasSize() {
        double maxHeight = (initialVelocity * Math.sin(angle)) * (initialVelocity * Math.sin(angle)) / (2 * gravity);
        int canvasWidth = (int) (maxDistance * scale);
        int canvasHeight = (int) (maxHeight * scale) + 100;

        canvasPanel.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
        canvasPanel.revalidate();
    }

    private void drawTrajectory(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= canvasPanel.getWidth(); i += 50) {
            g.drawLine(i, 0, i, canvasPanel.getHeight());
        }
        for (int i = 0; i <= canvasPanel.getHeight(); i += 50) {
            g.drawLine(0, i, canvasPanel.getWidth(), i);
        }
        g.setColor(Color.BLACK);
        g.drawLine(0, canvasPanel.getHeight() - 50, canvasPanel.getWidth(), canvasPanel.getHeight() - 50);
        g.drawLine(50, 0, 50, canvasPanel.getHeight());
        g.setColor(Color.RED);

        for (int i = 1; i < trajectory.size(); i++) {
            Point p1 = trajectory.get(i - 1);
            Point p2 = trajectory.get(i);
            g.drawLine(p1.x + 50, p1.y, p2.x + 50, p2.y);
        }

        if (x >= 0 && y >= 0) {
            g.setColor(Color.BLUE);
            int drawX = 50 + (int) (x * scale);
            int drawY = canvasPanel.getHeight() - 50 - (int) (y * scale);
            g.fillOval(drawX - 5, drawY - 5, 10, 10);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TiroParabolicoApp app = new TiroParabolicoApp();
            app.setVisible(true);
        });
    }
}
