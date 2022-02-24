package ScheduleGenerator;

import HomeworkGatherer.logging.LogPanel;
import Common.models.Vec;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class SGMainPanel extends JPanel {
    Parser parser = new Parser();

    PrintScheduleDrawer printScheduleDrawer;
    ArrayList<Course> courses = parser.getCourses("621");

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        PrintScheduleDrawer printScheduleDrawer =
                new PrintScheduleDrawer(
                        new Point2D.Double(30, 50),
                        new Point2D.Double(2D, 2D),
                        parser.getHourIntervals()
                );
        printScheduleDrawer.paintComponents(g, courses);
    }

    public SGMainPanel(JFrame appFrame, Vec appSize) {
        setLayout(new FlowLayout());
        setBounds(0,0, appSize.x, appSize.y);

        /*JLabel parsedLink = new JLabel(getLink());
        add(parsedLink);

        JTextField yearSelector = new JTextField("2021");
        add(yearSelector);

        JComboBox<String> profSelector = new JComboBox<>(professions.keySet().toArray(new String[0]));
        add(profSelector);

        JComboBox<String> groupSelector = new JComboBox<>(professions.keySet().toArray(new String[0]));
        add(profSelector);*/

        //PrintScheduleDrawer printScheduleDrawer = new PrintScheduleDrawer();
        //add(printScheduleDrawer);

        //JComboBox<String>

        JButton btn = new JButton("TEST");
        btn.addActionListener(e -> repaint());

        add(btn);

        setVisible(true);
    }

    /*public void parse() {
        try {
            Document doc = Jsoup.connect("https://www.cs.ubbcluj.ro/files/orar/2021-2/grafic/IM2.html").get();

            Elements links = doc.select("tr");

            String groups;
            String subgroups = "";

            // [szak][gr][subgr][tantargyszam]
            // pl. i[521][1][]

            int i = 0;
            int day = 0;
            for (Element link : links) {

                if (i == 1) { // th's contain groups
                    //System.out.println(link.select("th").text());
                } else if (i == 2) { // th's contain subgroups
                    subgroups = link.select("th").text();
                    //System.out.println(link.select("th").text());
                }

                if (i >= 2 && link.select("th").text().contains(subgroups)) {
                    //  System.out.println(WORK_DAYS_OF_WEEK[day]);
                    day++;
                }

                //if(link.hasAttr("[class=\"tipC\"]")) {
               //     System.out.println(link.select("th").text());
                //}

                // System.out.println(link.select("th").text());


                if (!link.select("td").text().equals("")) { // classes
                    Elements cls = link.select("td");
                    System.out.println(link.select("th").text());
                    System.out.println(cls.attr("rowspan"));
                    System.out.println(cls.text());
                }

                i++;
            }
        } catch (Exception e) {
            LogPanel.logln("VIGYÁZAT: Sikertelen verzió ellenõrzés! " + e);
        }
    }*/


}
