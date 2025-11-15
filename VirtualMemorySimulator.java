import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

public class VirtualMemorySimulator extends JFrame {
    private JPanel controlPanel, inputPanel, visualizationPanel;
    private JComboBox<String> algorithmCombo;
    private JTextField frameInput, sequenceInput;
    private JTextArea resultArea, explanationArea;
    private JButton setupButton, visualizeButton , analyseButton;
    private int numFrames;
    private String selectedAlgorithm;
    private int[] pageSequence;
    private Timer animationTimer;
    private int currSeqIndex = 0;
    private int[] faults = {0};
    private Set<Integer> frames = new LinkedHashSet<>();
    private Queue<Integer> fifoQueue = new LinkedList<>();
    private Deque<Integer> lifoStack = new ArrayDeque<>();
    private Random random = new Random();
    private Map<Integer,Integer> recentUse = new HashMap<>();
    private Map<Integer,String> segmentMapping = new HashMap<>();

    public VirtualMemorySimulator() {
        setTitle("Virtual Memory Management Simulator");
        setSize(1300, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0x006A71));

        setupControlPanel();
        setupInputPanel();

        add(controlPanel, BorderLayout.WEST);
        add(inputPanel, BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(new Dimension(300, getHeight()));
        controlPanel.setBackground(new Color(0x48A6A7));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel title = new JLabel("Configure Simulator");
        title.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        title.setForeground(new Color(0x0F0E47));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(title);
        controlPanel.add(Box.createVerticalStrut(15));

        controlPanel.add(createLabel("Number of Frames:"));
        frameInput = createTextField();
        controlPanel.add(frameInput);
        controlPanel.add(Box.createVerticalStrut(10));

        controlPanel.add(createLabel("Select Algorithm:"));
        algorithmCombo = new JComboBox<>(new String[]{"FIFO", "LRU", "MRU", "Optimal", "LIFO", "Random"});
        algorithmCombo.setMaximumSize(new Dimension(200, 30));
        algorithmCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        algorithmCombo.setBackground(new Color(0x9ACBD0));
        algorithmCombo.setForeground(new Color(0x0F0E47));
        controlPanel.add(algorithmCombo);
        controlPanel.add(Box.createVerticalStrut(20));

        setupButton = new JButton("Setup");
        styleButton(setupButton, new Color(0x006A71));
        controlPanel.add(setupButton);
        controlPanel.add(Box.createVerticalStrut(20));

        controlPanel.add(createLabel("Results:"));
        resultArea = new JTextArea(4, 15);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBackground(new Color(0xD8E6AD));
        resultArea.setForeground(new Color(0x0F0E47));
        resultArea.setFont(new Font("Segoe", Font.ITALIC, 14));
        resultArea.setBorder(BorderFactory.createLineBorder(new Color(0x006A71)));
        controlPanel.add(new JScrollPane(resultArea));

        setupButton.addActionListener(e -> {
            try {
                numFrames = Integer.parseInt(frameInput.getText().trim());
                selectedAlgorithm = (String) algorithmCombo.getSelectedItem();
                inputPanel.setVisible(true);
                inputPanel.revalidate();
                inputPanel.repaint();
                resultArea.setText(getAlgorithmDescription(selectedAlgorithm));

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number of frames.");
            }
        });
    }

    private void setupInputPanel() {
        inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(new Color(0x006A71));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JPanel top = new JPanel();
        top.setBackground(new Color(0x006A71));
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JLabel seqLabel = new JLabel("Enter Page Sequence:");
        seqLabel.setForeground(new Color(0xF2EFE7));
        seqLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        top.add(seqLabel);

        sequenceInput = new JTextField(25);
        sequenceInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sequenceInput.setBackground(new Color(0x9ACBD0));
        sequenceInput.setForeground(new Color(0x0F0E47));
        top.add(sequenceInput);

        visualizeButton = new JButton("Visualize");
        styleButton(visualizeButton, new Color(0x006A71));
        top.add(visualizeButton);

        analyseButton = new JButton("Analyse");
        styleButton(analyseButton, new Color(0x4B8E8D));
        top.add(analyseButton);

        inputPanel.add(top, BorderLayout.NORTH);

        visualizationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        visualizationPanel.setBackground(new Color(34, 40, 49));
        inputPanel.add(new JScrollPane(visualizationPanel), BorderLayout.CENTER);

        explanationArea = new JTextArea(8, 80);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setBackground(new Color(0xD8E6AD));
        explanationArea.setForeground(new Color(0x0F0E47));
        explanationArea.setBorder(BorderFactory.createTitledBorder(new LineBorder(new Color(34, 40, 49)), "Execution Steps", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.ITALIC, 18), new Color(34, 40, 49)));

        inputPanel.add(explanationArea, BorderLayout.SOUTH);
        inputPanel.setVisible(false);

        visualizeButton.addActionListener(e -> {
            try {
                String[] seq = sequenceInput.getText().trim().split("\\s+");
                pageSequence = Arrays.stream(seq).mapToInt(Integer::parseInt).toArray();
                simulateAndVisualize();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid page sequence.");
            }
        });

        analyseButton.addActionListener(e -> {
            try {
                String[] seq = sequenceInput.getText().trim().split("\\s+");
                int[] testSequence = Arrays.stream(seq).mapToInt(Integer::parseInt).toArray();
                Map<String, Integer> faultResults = new LinkedHashMap<>();
                String[] algorithms = {"FIFO", "LRU", "MRU", "Optimal", "LIFO", "Random"};

                for (String algo : algorithms) {
                    faultResults.put(algo, simulatePageFaults(algo, testSequence));
                }

                int minFaults = Collections.min(faultResults.values());
                List<String> bestAlgos = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : faultResults.entrySet()) {
                    if (entry.getValue() == minFaults) bestAlgos.add(entry.getKey());
                }

                visualizationPanel.removeAll();
                visualizationPanel.setLayout(new BorderLayout());

                ChartPanel chart = new ChartPanel(faultResults);
                visualizationPanel.add(chart, BorderLayout.CENTER);

                JTextArea summary = new JTextArea();
                summary.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                summary.setBackground(new Color(0xD8E6AD));
                summary.setForeground(new Color(0x0F0E47));
                summary.setEditable(false);
                summary.setLineWrap(true);
                summary.setWrapStyleWord(true);

                StringBuilder result = new StringBuilder("Performance Summary:\n");
                for (Map.Entry<String, Integer> entry : faultResults.entrySet()) {
                    result.append(entry.getKey()).append(" ➝ ").append(entry.getValue()).append(" faults\n");
                }

                result.append("\nBest Performer")
                    .append(bestAlgos.size() > 1 ? "s" : "")
                    .append(": ").append(String.join(", ", bestAlgos))
                    .append(" 🎯");

                summary.setText(result.toString());
                summary.setBorder(BorderFactory.createTitledBorder("Comparison Summary"));
                visualizationPanel.add(summary, BorderLayout.SOUTH);

                visualizationPanel.revalidate();
                visualizationPanel.repaint();

            } 
            catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid page sequence.");
            }
        });
    }

    private void simulateAndVisualize() {
        visualizationPanel.removeAll();
        visualizationPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        resultArea.setText("");
        explanationArea.setText("");
        currSeqIndex = 0;
        faults[0] = 0;
        frames.clear();
        fifoQueue.clear();
        lifoStack.clear();
        recentUse.clear();
        segmentMapping.clear();

        animationTimer = new Timer(800, e -> {
            if (currSeqIndex >= pageSequence.length) {
                showFinalSummary();
                animationTimer.stop();
                return;
            }

            int page = pageSequence[currSeqIndex];
            boolean fault = false;
            int victim = -1;

            StringBuilder explanation = new StringBuilder("Step " + (currSeqIndex + 1) + ": Page " + page);

            if (!frames.contains(page)) {
                fault = true;
                explanation.append(" MISS. Page Fault.");
                if (frames.size() == numFrames) {
                    switch (selectedAlgorithm) {
                        case "FIFO":
                            victim = fifoQueue.poll();
                            explanation.append(" Page ").append(victim).append(" was replaced (using FIFO).");
                            break;
                        case "LRU":
                            victim = Collections.min(recentUse.entrySet(), Map.Entry.comparingByValue()).getKey();
                            explanation.append(" Page ").append(victim).append(" was replaced (using LRU).");
                            break;
                        case "MRU":
                            victim = Collections.max(recentUse.entrySet(), Map.Entry.comparingByValue()).getKey();
                            explanation.append(" Page ").append(victim).append(" was replaced (using MRU).");
                            break;
                        case "Optimal":
                            victim = selectOptimalVictim(currSeqIndex);
                            explanation.append(" Page ").append(victim).append(" was replaced (using OPTIMAL)");
                            break;
                        case "LIFO":
                            victim = lifoStack.pop();
                            explanation.append(" Page ").append(victim).append(" was replaced (using LIFO).");
                            break;
                        case "Random":
                            int index = random.nextInt(frames.size());
                            victim = new ArrayList<>(frames).get(index);
                            explanation.append(" Page ").append(victim).append(" was replaced (using RANDOM).");
                            break;
                    }
                    frames.remove(victim);
                    fifoQueue.remove(victim);
                    recentUse.remove(victim);
                    lifoStack.remove(victim);
                }
                frames.add(page);
                if ("FIFO".equals(selectedAlgorithm)) fifoQueue.offer(page);
                if ("LIFO".equals(selectedAlgorithm)) lifoStack.push(page);
                faults[0]++;
            } else {
                explanation.append(" HIT. The page was already in memory.");
            }

            if ("LRU".equals(selectedAlgorithm) || "MRU".equals(selectedAlgorithm)) {
                recentUse.put(page, currSeqIndex);
            }

            segmentMapping.put(page, "Code");
            updateVisualization(frames, page, fault);
            explanationArea.append(explanation.toString() + "\n");
            currSeqIndex++;
        });
        animationTimer.start();
    }

    private int selectOptimalVictim(int step) {
        int farthest = -1, victim = -1;
        for (int f : frames) {
            int next = Integer.MAX_VALUE;
            for (int j = step + 1; j < pageSequence.length; j++) {
                if (pageSequence[j] == f) { next = j; break; }
            }
            if (next > farthest) { farthest = next; victim = f; }
        }
        return victim;
    }

    private int simulatePageFaults(String algorithm, int[] sequence) {
        Set<Integer> localFrames = new LinkedHashSet<>();
        Queue<Integer> fifo = new LinkedList<>();
        Deque<Integer> lifo = new ArrayDeque<>();
        Map<Integer, Integer> recent = new HashMap<>();
        Random rand = new Random();
        int faults = 0;

        for (int i = 0; i < sequence.length; i++) {
            int page = sequence[i];
            if (!localFrames.contains(page)) {
                faults++;
                if (localFrames.size() == numFrames) {
                    int victim = -1;
                    switch (algorithm) {
                        case "FIFO":
                            victim = fifo.poll();
                            break;
                        case "LRU":
                            victim = Collections.min(recent.entrySet(), Map.Entry.comparingByValue()).getKey();
                            break;
                        case "MRU":
                            victim = Collections.max(recent.entrySet(), Map.Entry.comparingByValue()).getKey();
                            break;
                        case "Optimal":
                            int farthest = -1;
                            for (int f : localFrames) {
                                int next = Integer.MAX_VALUE;
                                for (int j = i + 1; j < sequence.length; j++) {
                                    if (sequence[j] == f) { next = j; break; }
                                }
                                if (next > farthest) {
                                    farthest = next;
                                    victim = f;
                                }
                            }
                            break;
                        case "LIFO":
                            victim = lifo.pop();
                            break;
                        case "Random":
                            victim = new ArrayList<>(localFrames).get(rand.nextInt(localFrames.size()));
                            break;
                    }
                    localFrames.remove(victim);
                    fifo.remove(victim);
                    recent.remove(victim);
                    lifo.remove(victim);
                }
                localFrames.add(page);
                if ("FIFO".equals(algorithm)) fifo.offer(page);
                if ("LIFO".equals(algorithm)) lifo.push(page);
            }
            if ("LRU".equals(algorithm) || "MRU".equals(algorithm)) {
                recent.put(page, i);
            }
        }
        return faults;
    }


    private void showFinalSummary() {
        StringBuilder sb = new StringBuilder();
        frames.forEach(p -> sb.append(p).append(" "));
        /*resultArea.setText(
            "Page Faults: " + faults[0] +
            "\nTotal Pages: " + pageSequence.length +
            "\nHit Ratio: " + String.format("%.2f", 1 - (double)faults[0]/pageSequence.length) + "\uD83D\uDE00" +
            "\nFinal Frames: " + sb.toString().trim()*/


        String message = "<html>"
        + "<body style='font-family:sans-serif;'>"
        + "<h3 style='color:#DAA520;'><i>Page Replacement Result</i></h3>" // darkgoldenrod
        + "<p style='color:#FF0000;'><b><i>Page Misses:</i></b> " + faults[0] + "</p>" // red
        + "<p style='color:#00CC00;'><b><i>Page Hits:</i></b> " + (pageSequence.length - faults[0]) + "</p>" // green
        + "<p style='color:#FF8C00;'><b><i>Hit Ratio:</i></b> " + String.format("%.2f", 1 - (double)faults[0]/pageSequence.length) + "</p>" // teal
        + "<p style='color:#5F9EA0;'><b><i>Final Page Sequence:</i></b><br>" + sb.toString().trim() + "</p>" // cadetblue
        + "</body></html>";

        JOptionPane.showMessageDialog(this, message, "Simulation Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateVisualization(Set<Integer> frames, int current, boolean fault) {
        JPanel cell = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fault ? new Color(255, 99, 132, 50) : new Color(46, 204, 113, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        cell.setPreferredSize(new Dimension(160, 250));
        cell.setOpaque(false);
        cell.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(99, 110, 114)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));

        for (int p : frames) {
            JLabel lbl = new JLabel("Page " + p + " (" + segmentMapping.get(p) + ")");
            lbl.setOpaque(true);
            lbl.setBackground(p == current
                ? (fault ? new Color(255, 99, 132) : new Color(46, 204, 113))
                : new Color(78, 81, 84)
            );
            lbl.setForeground(Color.WHITE);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            lbl.setFont(new Font("Consolas", Font.BOLD, 14));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            cell.add(lbl);
            cell.add(Box.createVerticalStrut(4));
        }
        //If remaining frames are empty
        for (int i = frames.size(); i < numFrames; i++) {
            JLabel empty = new JLabel("-");
            empty.setFont(new Font("Consolas", Font.PLAIN, 14));
            empty.setForeground(new Color(99, 110, 114));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            cell.add(empty);
            cell.add(Box.createVerticalStrut(4));
        }
        JLabel status = new JLabel(fault ? "MISS" : "HIT");
        status.setFont(new Font("Segoe UI", Font.BOLD, 16));
        status.setForeground(fault ? new Color(255, 99, 132) : new Color(46, 204, 113));
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(Box.createVerticalGlue());
        cell.add(status);

        visualizationPanel.add(cell);
        visualizationPanel.revalidate();
        visualizationPanel.repaint();
    }

    private String getAlgorithmDescription(String algo) {
        switch (algo) {
            case "FIFO":
                return "FIFO (First-In-First-Out): Replaces the oldest page in memory, the one that came in first.";
            case "LRU":
                return "LRU (Least Recently Used): Replaces the page that has not been used for the longest time.";
            case "MRU":
                return "MRU (Most Recently Used): Replaces the page that was most recently used.";
            case "Optimal":
                return "Optimal: Replaces the page that will not be used for the longest period in the future.";
            case "LIFO": 
                return "LIFO (Last-In-First-Out) replaces the most recently loaded page.";
            case "Random": 
                return "Random replaces a randomly selected page from memory.";
            default:
                return "";
        }
    }

    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        return l;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setMaximumSize(new Dimension(200, 30));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(new Color(0x9ACBD0));
        tf.setForeground(new Color(0x0F0E47));
        tf.setBorder(BorderFactory.createLineBorder(new Color(99, 110, 114)));
        return tf;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setMaximumSize(new Dimension(200, 35));
    }

    class ChartPanel extends JPanel {
        private final Map<String, Integer> data;
        private final int maxFaults;
        public ChartPanel(Map<String, Integer> data) {
            this.data = data;
            this.maxFaults = Collections.max(data.values());
            setPreferredSize(new Dimension(800, 400));
            setBackground(new Color(34, 40, 49));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int barWidth = getWidth() / data.size();
            int x = 30;

            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            int index = 0;

            Color[] palette = {
                new Color(0x6A0572), new Color(0xAB83A1), new Color(0x4B8E8D),
                new Color(0x5EAAA8), new Color(0xA3E4DB), new Color(0xF59E0B)
            };

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int barHeight = (int) ((double) entry.getValue() / maxFaults * 250);
                g2.setColor(palette[index % palette.length]);
                g2.fillRoundRect(x, getHeight() - barHeight - 50, barWidth - 40, barHeight, 10, 10);

                g2.setColor(Color.WHITE);
                g2.drawString(entry.getKey(), x + 5, getHeight() - 25);
                g2.drawString(entry.getValue() + " faults", x + 5, getHeight() - barHeight - 60);

                x += barWidth;
                index++;
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VirtualMemorySimulator().setVisible(true));
    }
}