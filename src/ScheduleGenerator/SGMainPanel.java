package ScheduleGenerator;

import Common.logging.LogPanel;
import Common.logging.LogsListener;
import HomeworkGatherer.InfoPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public class SGMainPanel extends JPanel {
    public static final int SG_LOG_INSTANCE = LogPanel.createNewInstance();

    Parser parser;

    ScheduleDrawer scheduleDrawer =
            new ScheduleDrawer(
                    new Point2D.Double(30, 100),
                    new Point2D.Double(1.5, 1.5),
                    Parser.getHourIntervals()
            );

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
       // scheduleDrawer.paintComponent(g);
    }

    public SGMainPanel(JFrame appFrame, Point appSize) {
        setLayout(new FlowLayout());
        setBounds(0,0, appSize.x, appSize.y);

        parser = new Parser("621");
        scheduleDrawer.setSpecificProps(parser.getTopLeftCont(), parser.getCourses());

        ControlPanel controlPanel = new ControlPanel(new Point(0,0), new Point(300,50),
                parser.getMajor(), parser.getGroup(), Integer.toString(parser.getStudYear()));
        add(controlPanel);

        controlPanel.addActionListener(e -> {
            String selGroup = controlPanel.getSelectedGroup();
            if(selGroup != null) {
                parser = new Parser(selGroup);
                scheduleDrawer.setSpecificProps(selGroup, parser.getCourses());
                repaint();
            }
        });

        JButton generateSG = new JButton("Generálás");
        generateSG.addActionListener(e -> repaint());

        add(generateSG);
        generateSG.addActionListener(e -> {
            try {
                saveSchedule();
            } catch (Exception ex) {
                LogPanel.logln(Arrays.toString(ex.getStackTrace()), SG_LOG_INSTANCE);
            }
        });

        // displays clsPresetDesc and Logs in a TabbedPane
        InfoPanel infoPanel = new InfoPanel();
        infoPanel.setPreferredSize(new Dimension(400,200));
        // infoPanel.setBounds(12, 10, 400, 210);
        infoPanel.addTab(LogPanel.getScrollableTextArea(SG_LOG_INSTANCE), "Logs", "A generálási folyamatrol ír ki hasznos infókat");
        add(infoPanel);

        setVisible(true);
    }

    public void saveSchedule() throws Exception {
        JFileChooser chooser;
        chooser = new JFileChooser();
        chooser.setDialogTitle("Órarend lementése");

        // choose directory
        chooser.setSelectedFile(new File("orarend.png"));
        int userSelection = chooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();

            ScheduleDrawer highRes = scheduleDrawer.getHighResVersion();
            Dimension d = highRes.getSize();
            BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            highRes.paintComponent(g2d);
            g2d.dispose();
            ImageIO.write(image, "png", new File(path));
        }
    }


}
