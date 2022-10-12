package ScheduleGenerator.graphics;

import Common.logging.LogPanel;
import ScheduleGenerator.Course;
import ScheduleGenerator.Parser;
import ScheduleGenerator.TimeFormatter;
import ScheduleGenerator.data.SGData;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static ScheduleGenerator.SGMainPanel.SG_LOG_INSTANCE;
import static ScheduleGenerator.data.SGData.DAYS_OF_WEEK_HU;
import static java.awt.Font.PLAIN;

public class ScheduleDrawer extends JComponent {
    private final Point2D.Double A4InMM = new Point2D.Double(297, 210);
    private final Point2D.Double border = new Point2D.Double(2, 2); // mm
    private final Point2D.Double pos;
    private final Point2D.Double size;
    private final Point2D.Double scale;

    private Graphics2D g2d = null;
    private Font font = null;

    private int cols; // 15 = 1 + 14
    private int rows; // 11 = 1 + (5 * 2)

    // repaint specific elements
    private ArrayList<LocalTime[]> intervals;
    private ArrayList<Course> courses;
    private String group;
    private String subGroup;

    private final boolean colorAfterSubjects = true; // if false -- colors after type(Course, Seminar)

    public ScheduleDrawer(Point2D.Double pos, Point2D.Double size) {
        this.pos = pos;
        this.size = size;
        this.scale = new Point2D.Double(size.x / A4InMM.x, size.y / A4InMM.y);
        setSize(new Dimension((int) size.x, (int) size.y)); // set size of actual element

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("assets/fonts/Eras-Bold-ITC.ttf")).deriveFont(10F);
        } catch (Exception e) {
            font = new Font(Font.SANS_SERIF, PLAIN, 14);
            e.printStackTrace();
            LogPanel.logln("VIGYÁZAT: Sikertelen volt az órarend fontjának beolvasása. Beépített szövegtípus lesz használva helyette. Az órarend generáláshoz erõsen ajánlott az újratelepítés.", SG_LOG_INSTANCE);
        }

        Cell.scale = scale;
        Cell.padding = new Point2D.Double(2.5 * scale.x, 2.5 * scale.y);
        Cell.margin = new Point2D.Double(0.5 * scale.x, 0.5  * scale.y); // milimeters
    }

    public void repaintWithNewProps(ArrayList<LocalTime[]> intervals, ArrayList<Course> courses, String group, String subGroup) {
        this.intervals = intervals;
        this.courses = courses;
        this.group = group;
        this.subGroup = subGroup;

        cols = 1 + this.intervals.size(); // header + num of hours
        rows = 1 + DAYS_OF_WEEK_HU.length * 2; // side + dow * 2

        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g2d = (Graphics2D) g;
        g2d.setFont(font);

        // background color
        Rect.draw(g2d, new Rectangle2D.Double(pos.x, pos.y, size.x, size.y), SGData.Colors.BACKGROUND_COLOR);

        ArrayList<Cell> cells = generateCells(courses);

        drawTable(new Point2D.Double(0,0), new Point2D.Double(A4InMM.x, A4InMM.y), cells);
    }

    private void drawTable(Point2D.Double tPos, Point2D.Double tSize, ArrayList<Cell> cells) {
        if(cells == null){
            System.out.println("No cells defined in drawTable");
            return;
        }

        Point2D.Double absPos = new Point2D.Double(pos.x, pos.y);

        // convert MM to px
        tPos.x *= scale.x;
        tPos.y *= scale.y;
        tSize.x *= scale.x;
        tSize.y *= scale.y;

        tSize.x -= border.x * scale.x * 2; // left, right
        tSize.y -= border.y * scale.y * 2; // top, bottom
        absPos.x += border.x * scale.x  + tPos.x;
        absPos.y += border.y * scale.y + tPos.y;

        // the amount of pixel offset for each col/row
        Point2D.Double offset = new Point2D.Double(tSize.x / cols, tSize.y / rows);

        // the vertical index from top to bottom
        for(int x = 0; x < cols; x++) {
            for(int y = 0; y < rows; y++) {
                // calculate cell (x,y,w,h) considering margins and the whole table's position
                Rectangle2D.Double slotRect = new Rectangle2D.Double(
                        absPos.x + (x * offset.x) + Cell.margin.x,
                        absPos.y + (y * offset.y) + Cell.margin.y,
                        offset.x - (Cell.margin.x * 2),
                        offset.y - (Cell.margin.y * 2)
                );

                boolean inAnyCell = false;
                for(Cell cell : cells) {
                    // if there is a cell marked on the current slot
                    if(cell.indexRect.x == x && cell.indexRect.y == y) {
                        // set the actual size of cell based on calculations
                        cell.setActualRect(new Rectangle2D.Double(
                            slotRect.x, slotRect.y,
                            cell.indexRect.width * slotRect.width,
                            cell.indexRect.height * slotRect.height)
                        );
                        cell.drawCell(g2d);
                    }

                    // check whether given grid coordinate is part of any defined cell
                    if(!inAnyCell) {
                        inAnyCell = !(x >= cell.indexRect.x + cell.indexRect.width) && !(y >= cell.indexRect.y + cell.indexRect.height)
                                && !(x < cell.indexRect.x) && !(y < cell.indexRect.y);
                    }
                }

                // draw small white squares on parts where there are no cells overlapping
                if(!inAnyCell) {
                    Rect.draw(g2d, slotRect, Color.WHITE);
                }
            }
        }
    }

    private ArrayList<Cell> generateCells(ArrayList<Course> courses) {
        if(courses == null)  {
            LogPanel.logln("Could not generate cells! courses is null.", SG_LOG_INSTANCE);
            return null;
        }

        ArrayList<Cell> cells = new ArrayList<>();

        // top left content
        Cell topLeftCell = new Cell(new Rectangle(0, 0, 1, 1),
                SGData.Colors.BASE_COLOR, getTopLeftContent(), null, null, 2.5 * scale.x);
        topLeftCell.setFontStyle(font);
        cells.add(topLeftCell);

        // days
        for(int i = 1; i <= DAYS_OF_WEEK_HU.length; i++) {
            Rectangle t = new Rectangle(0, i * 2 - 1, 1, 2);

            Cell cell = new Cell(t, SGData.Colors.BASE_COLOR, DAYS_OF_WEEK_HU[i - 1], null, null, 0);
            cell.setCenterFontSize(font, (float) (this.scale.x * 9F));

            cells.add(cell);
        }

        // intervals
        for(int i = 1; i <= intervals.size(); i++) {
            Rectangle t = new Rectangle(i, 0, 1, 1);
            String displayInterval = TimeFormatter.localTimeArrToDisplayFormat(intervals.get(i - 1));
            Cell cell = new Cell(t, SGData.Colors.BASE_COLOR, displayInterval, null, null, 2.5 * scale.x);
            cell.setCenterFontSize(font, (float) (this.scale.x * 5F));

            cells.add(cell);
        }

        // courses
        for(Course course : courses) {
            // cell coordinates
            int startX = 0;
            int endX = 0;
            LocalTime[] courseIntArr = course.getIntervalAsLocalTimeArr();
            for(int i = 0; i < intervals.size(); i++) {
                if(intervals.get(i)[0].equals(courseIntArr[0])) startX = i + 1;
                if(intervals.get(i)[1].equals(courseIntArr[1])) endX = i + 2;
            }
            int dayIndex = course.getDayIndexInRO_DAYS();
            int width = endX - startX;
            Rectangle indexRect = new Rectangle(startX, dayIndex * 2 + 1, width, 2);

            // search for duplicates
            Optional<Cell> cellOnSamePos = cells.stream().
                    filter(cell -> Objects.equals(cell.indexRect, indexRect)).
                    findFirst();
            if(cellOnSamePos.isPresent()) {
                indexRect.height = 1;
                cellOnSamePos.get().indexRect.height = 1;

                if(course.getFreqAsNum() == 2) {
                    indexRect.y += 1;
                } else {
                    cellOnSamePos.get().indexRect.y += 1;
                }

                cellOnSamePos.get().evaluateStringsBasedOnSpace();
            }
            Cell clsDrw = new Cell(course, indexRect, 0);

            clsDrw.setBottomFontSize(font, (float) (this.scale.x * 5F));
            clsDrw.setTopLeftFontSize(font, (float) (this.scale.x * 3.5F));

            cells.add(clsDrw);
        }

        return cells;
    }

    public String getTopLeftContent() {
        if(Objects.equals(subGroup, "nincs")){
            return group;
        }

        return group + "\n" + subGroup;
    }

    public ScheduleDrawer getHighResVersion() {
        ScheduleDrawer tmp = new ScheduleDrawer(new Point2D.Double(0,0), new Point2D.Double(3508, 2480));
        tmp.repaintWithNewProps(intervals, courses, group, subGroup);
        return tmp;
    }
}
