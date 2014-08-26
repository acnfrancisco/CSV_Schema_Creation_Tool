import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.sql.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainWindow extends JPanel implements ActionListener {
    static private final String newline = "\n";
    JButton openButton, saveButton;
    JTextArea log;
    JFileChooser fc;
    protected Connection connection = null;
    String query = new String();
    Parser Parser = new Parser();

    private JPasswordField  password;
    private JTextField      username;
    private JTextField      database;
    private JTextField      tableName;
    private JTextField      serverIP;
    private JButton         connectButton;
    private JButton         uploadButton;
    private JButton         clearButton;


    public MainWindow() {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        username	    = 	new JTextField      ("Username", 10     );
        password	    = 	new JPasswordField  ("Password", 10     );
        database        =   new JTextField      ("Data Base", 10    );
        tableName       =   new JTextField      ("Table Name", 10   );
        serverIP        =   new JTextField      ("Server IP", 10    );

        connectButton   =   new JButton         ("Connect To DB"   );
        uploadButton    =   new JButton         ("Upload New Table");
        clearButton     =   new JButton         ("Clear Log"       );

        connectButton.addActionListener	(new connectButtonListener());
        uploadButton.addActionListener  (new uploadButtonListener() );
        clearButton.addActionListener   (new clearButtonListener()  );

        username.addFocusListener	(new usernameListener()	);
        password.addFocusListener	(new passwordListener()	);
        database.addFocusListener	(new dbListener()	    );
        serverIP.addFocusListener   (new serverIPListener() );
        tableName.addFocusListener	(new tableListener()	);


        //connectFieldPanel
        JPanel connectFieldPanel = new JPanel();
        connectFieldPanel.setLayout(new GridLayout(0, 1) );

        connectFieldPanel.add(username );
        connectFieldPanel.add(password );
        connectFieldPanel.add(serverIP );
        connectFieldPanel.add(database );
        connectFieldPanel.add(tableName);

        connectFieldPanel.add(connectButton );
        connectFieldPanel.add(uploadButton  );

        //Create a file chooser
        fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(new FileNameExtensionFilter(".csv", "csv"));

        openButton = new JButton("Select a CSV..."     );
        saveButton = new JButton("Print Schema to Log" );

        openButton.addActionListener(this);
        saveButton.addActionListener(this);


        //For layout purposes, put these 3 buttons in a separate panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton  );
        buttonPanel.add(saveButton  );
        buttonPanel.add(clearButton );


        //Add the buttons and the log to this panel.
        add(buttonPanel,        BorderLayout.PAGE_START );
        add(logScrollPane,      BorderLayout.CENTER     );
        add(connectFieldPanel,  BorderLayout.SOUTH      );
    }



    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(MainWindow.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.append("Opening: " + file.getName() + "." + newline);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
            //log.append("Processing CSV..."+newline);
            File file = fc.getSelectedFile();
            //Initialize some tools we'll need
            String line = null;
            int i = 0;

            BufferedReader reader = null;
            try{
                reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
            } catch (FileNotFoundException er){
                log.append("FILE NOT FOUND :("+newline);
            }

        /*
         * We read the file line by line and pass the individual lines to
         * our parser, where all of the magic happens.
         */
            try{
                while ((line = reader.readLine()) != null) {

                    Parser.parse(line,i);
                    i++;
                }
            } catch(IOException er) {
                log.append("ERROR READING CSV FILE :("+newline);
            }

            QueryBuilder qb = new QueryBuilder();
            query = qb.createTable(tableName.getText(), Parser.getColumns());
            log.append("Schema created"+newline);


            //Handle save button action.
        } else if (e.getSource() == saveButton) {
            QueryBuilder qb = new QueryBuilder();
            query = qb.createTable(tableName.getText(), Parser.getColumns());
            log.append(query + newline);
        }
    }

    private static void Build() {
        //Create and set up the window.
        JFrame frame = new JFrame("CSV Schema Tool v1.01");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new MainWindow());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                Build();
            }
        });
    }
    public void connect() {
        try {
            // load jdbc driver package using class loader
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException classNotFoundException) {
            System.out.println("Error:Can't Find DB Connector");
            classNotFoundException.printStackTrace();
            System.exit(0);
        }

        try {
            // create connection
            connection = DriverManager.getConnection("jdbc:mysql://" + serverIP.getText() + ":3306/" +
                    database.getText(), username.getText(), password.getText());
        } catch (SQLException sqlException) {
            log.append("Establishing connection to `" + database.getText() + "`: FAILED" + newline);
        }
        if(connection != null){
            log.append("Connection to `" + database.getText() + "`: Established." + newline);
        }
    }
    public void create() {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e ) {
            log.append("Bad Table Data, try printing to log and editing manually" + newline);
        }
    }

    private class connectButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent ev){
            connect();
        }
    }
    private class uploadButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent ev){
            QueryBuilder qb = new QueryBuilder();
            query = qb.createTable(tableName.getText(), Parser.getColumns());
            create();
        }
    }
    private class clearButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent ev){
            log.setText("");
        }
    }
    private class usernameListener implements FocusListener {
        public void focusGained(FocusEvent e){
            if ( username.getText().equals("Username") )
                username.setText("");
        }

        public void focusLost(FocusEvent e){
            if ( username.getText().equals("") )
                username.setText("Username");
        }
    }
    private class passwordListener implements FocusListener {
        public void focusGained(FocusEvent e){
            if ( password.getText().equals("Password") )
                password.setText("");
        }

        public void focusLost(FocusEvent e){
            if ( password.getText().equals("") )
                password.setText("Password");
        }
    }
    private class dbListener implements FocusListener {
        public void focusGained(FocusEvent e){
            if ( database.getText().equals("Data Base") )
                database.setText("");
        }

        public void focusLost(FocusEvent e){
            if ( database.getText().equals("") )
                database.setText("Data Base");
            }
    }
    private class tableListener implements FocusListener {
        public void focusGained(FocusEvent e){
            if ( tableName.getText().equals("Table Name") )
                tableName.setText("");
        }

        public void focusLost(FocusEvent e){
            if ( tableName.getText().equals("") )
                tableName.setText("Table Name");
        }

    }
    private class serverIPListener implements FocusListener {
        public void focusGained(FocusEvent e){
            if ( serverIP.getText().equals("Server IP") )
                serverIP.setText("");
        }

        public void focusLost(FocusEvent e){
            if ( serverIP.getText().equals("") )
                serverIP.setText("Server IP");
        }
    }
}
