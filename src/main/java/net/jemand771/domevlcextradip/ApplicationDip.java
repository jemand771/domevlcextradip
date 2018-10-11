package net.jemand771.domevlcextradip;


import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.util.Properties;

public class ApplicationDip {

    public static JTextField field;
    public static JFrame frame;
    public static int updownoffset = 25;
    public static int thickness = 30;
    public static int sleepDelay = 10;
    public static long windowFoundLastAt = 0;
    public static int windowNotFoundTimeMax = 3000;
    public static String configPlaying;
    public static String configNoInfo;

    public static Properties prop;
    private static String driverURL = "jdbc:ucanaccess://";
    private static String dbfilePath;

    private static Connection dbConnection;
    private static Database database;

    public static void main(String[] args) throws IOException, InterruptedException, SQLException {

        loadProperties();
        openConnections();
        initDisplay();
        field.setText("connecting");

        Socket pingSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            pingSocket = new Socket("127.0.0.1", 4212);
        } catch (ConnectException e) {
            System.out.println("VLC NOT RUNNING. WILL NOW EXIT");
            System.exit(1);
        }
        out = new PrintWriter(pingSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(pingSocket.getInputStream()));

        System.out.println(in.readLine());
        out.println("123");
        System.out.println(in.readLine());
        System.out.println(in.readLine());

        field.setText("success");
        try {
            while (true) {

                Thread.sleep(sleepDelay);

                out.println("status");
                String playing = in.readLine();
                if (playing.contains("audio volume:")) {

                    in.readLine();
                    repositionFrame("");
                    field.setText("y u no play");
                    continue;
                }
                in.readLine();
                in.readLine();
                String ytCode = playing.split(":")[3];
                String[] dotsegments = ytCode.split("\\.");
                ytCode = dotsegments[dotsegments.length - 2];
                ytCode = ytCode.substring(ytCode.length() - 11, ytCode.length());
//            System.out.println(blub);
                displayShit(playing, ytCode);
            }
        } catch (SocketException e) {
            System.out.println("YOU CLOSED THE VLC YOU BLITHERING IDIOT (telnet got disrespected)");
            System.exit(0);
        }

//        out.close();
//        in.close();
//        pingSocket.close();
    }


    public static void loadProperties() throws IOException {

        prop = new Properties();
        InputStream input = null;
        input = new FileInputStream("config.properties");

        // load a properties file
        prop.load(input);
        dbfilePath = prop.getProperty("dbpath");
        driverURL += dbfilePath;
        configNoInfo = prop.getProperty("displaynoinfo", "");
        configPlaying = prop.getProperty("displayinfo");
    }

    public static void openConnections() throws SQLException, IOException {

        dbConnection = DriverManager.getConnection(driverURL);
        database = DatabaseBuilder.open(new File(dbfilePath));
    }

    public static void initDisplay() {

        frame = new JFrame("Transparent Window");
        frame.setUndecorated(true);
        frame.setBackground(new Color(255, 255, 255, 255));
        frame.setAlwaysOnTop(true);
        frame.setLocation(300, 300);
        frame.setSize(100, thickness);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        field = new JTextField("[text]");
        field.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        field.setBorder(null);
        field.setOpaque(false);
        field.setBackground(new Color(0, 0, 0, 0));
        frame.add(field);
        frame.setVisible(true);
//        frame.pack();
    }

    public static void displayShit(String playing, String ytCode) throws SQLException {

        String[] parts = playing.split("/");
        String title = parts[parts.length - 1];
        title = new StringBuilder(title).reverse().toString();
        title = title.substring(2);
        title = new StringBuilder(title).reverse().toString();
//        System.out.println("searching for frame: " + title);
        repositionFrame(title);
        String text = "";
        String sql = "SELECT * FROM Tracklist WHERE Tracklist.[YouTube Link] LIKE '*" + ytCode + "*'";
        Statement statement = dbConnection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        if (rs.next()) {
            text = configPlaying;
            for (int i = 0; i < 100; i++) {
                if (text.contains("{dbc" + i + "}")) {
                    text = text.replace("{dbc" + i + "}", rs.getString(i));
                    text = text.replace("{ytcode}", ytCode);
                }
            }
//            System.out.println(text);
        } else {
            text = configNoInfo;
        }
        field.setText(text);
    }

    public static void repositionFrame(String title) {


        String windowName = "VLC media player";
        if (!title.equals("")) windowName = title + " - " + windowName;

        int[] rect;
        try {
            rect = GetWindowRect.getRect(windowName);
//            System.out.println(Arrays.toString(rect));
            frame.setLocation(rect[0] + 8, rect[3] - thickness + updownoffset);
            int width = rect[2] - rect[0] - 16;
            frame.setSize(width, thickness);
            field.setSize(width, thickness);
//            frame.setState(JFrame.NORMAL);
            windowFoundLastAt = System.currentTimeMillis();

        } catch (GetWindowRect.WindowNotFoundException e) {

            if (System.currentTimeMillis() - windowFoundLastAt > windowNotFoundTimeMax) {
                System.out.println("YOU CLOSED THE VLC YOU BLITHERING IDIOT (window not found)");
                System.exit(0);
//                e.printStackTrace();
            } else {
                System.out.println("vlc is switching track, please stand by");
            }
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
        }
    }
}