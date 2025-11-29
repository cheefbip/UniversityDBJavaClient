import java.sql. *;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Date;

public class Main {
   static final String DB_URL = "";
   static final String USER = "";
   static final String PASS = "";
   
   static final Scanner sc = new Scanner(System.in);

   static final String QUERY_DISPLAYPROJECTS = "SELECT projectno, spons_name, start_date, end_date, budget, principal_investigator FROM Projects";

   public static void main (String [] args) {
      // Open a connection
      try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
         Statement stmt = conn.createStatement();
      ) {
         System.out.println("Welcome to the University Database!");
         while (true) {
            System.out.println("\nCOMMANDS:");
            System.out.println("\t0: Exit program");
            System.out.println("\t1: View Projects");
            System.out.println("\t2: Add Project");
            System.out.println("\t3: Remove Project");
            System.out.println("\t4: Display student information, student advisor, and major department");
            System.out.println("\t5: Display faculty member projects");

            int input = nextInt("Enter a valid command: ");
            if (input == 0) { // Exit program
               break;
            } else if (input == 1) { // View Projects
               displayProjects(conn);
            } else if (input == 2) { // Add Project
               System.out.println("\nEnter corresponding project information below.");
               int projectNo = nextInt("Project number: ");
               String spons_name = nextString("Sponsor name: ", 20, false, true);
               java.sql.Date startDate = new java.sql.Date(nextDate("Start date (yyyy-MM-dd): ").getTime());
               java.sql.Date endDate = new java.sql.Date(nextDate("End date (yyyy-MM-dd): ").getTime());
               double budget = clamp(nextFloat("Budget [0, 1000000]: "), 0, 1000000);
               String principalInvestigator = nextString("Principal investigator SSN (9 digits, empty string = NULL): ", 9, true, true);
               addProject(conn,
                  projectNo,
                  spons_name,
                  startDate,
                  endDate,
                  budget,
                  principalInvestigator
               );
            } else if (input == 3) { // Remove Project
               System.out.println("\nEnter number of project to remove below.");
               int projectNo = nextInt("Project number: ");
               removeProject(conn,
                  projectNo
               );
            } else if (input == 4) {

            } else if (input == 5) {
               System.out.println("\nEnter professor SSN");
               String profSSN = nextString("Professor SSN (9 digits): ", 9, true, false);
               displayFacultyProjects(conn, profSSN);
            } else {
               System.out.println("Enter a valid command!");
            } 


         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
      
   }


   /*
      PostgreSQL Methods
   */

   public static void displayProjects(Connection conn) {
      try (Statement stmt = conn.createStatement();) {
         ResultSet rs=stmt.executeQuery(QUERY_DISPLAYPROJECTS);
         // Print all entries from Projects table.
         while (rs.next()) {
            System.out.println("ID: " + rs.getInt("projectno"));
            System.out.println("\tSponsor: " + rs.getString("spons_name"));
            System.out.println("\tStart/End Date: " + rs.getDate("start_date") + " to " + rs.getDate("end_date"));
            System.out.println("\tBudget: " + rs.getDouble("budget"));
            String PI = rs.getString("principal_investigator");
            if (PI != null) {
               System.out.println("\tPI SSN: " + PI);
            }
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   // Adds entry to Projects table.
   public static void addProject(Connection conn,
      int projectNo,
      String spons_name,
      java.sql.Date startDate,
      java.sql.Date endDate,
      double budget,
      String principalInvestigator
   ) {
      // SQL template
      String sql = "INSERT INTO Projects (ProjectNo, Spons_name, Start_date, End_date, Budget, Principal_Investigator) VALUES (?, ?, ?, ?, ?, ?)";

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, projectNo);
        stmt.setString(2, spons_name);
        stmt.setDate(3, startDate);
        stmt.setDate(4, endDate);
        stmt.setDouble(5, budget);
        stmt.setString(6, principalInvestigator);

        int res = stmt.executeUpdate();

        if (res == 0) {
            System.out.println("Nothing was changed");
        } else {
            System.out.println("Successfully added project number " + projectNo);
        }
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   // Removes entry from Projects table.
   public static void removeProject(Connection conn,
      int projectNo
   ) {
      // SQL template
      String sql = "DELETE FROM Projects WHERE ProjectNo = ?;";

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, projectNo);

        int res = stmt.executeUpdate();

        if (res == 0) {
            System.out.println("No matching project was found with id " + projectNo);
        } else {
            System.out.println("Successfully removed project number " + projectNo);
        }
      } catch (SQLException e) {
         e.printStackTrace();
      }
   } 

   // Displays all projects that the given faculty member works on, searched by SSN
   public static void displayFacultyProjects(Connection conn, String profSSN) {
      String profName = "";
      String query = 
         "SELECT name\n" +
         "FROM professors\n" +
         "WHERE ssn = ?;";

      // Get professor name
      try (PreparedStatement stmt = conn.prepareStatement(query);) {
         stmt.setString(1, profSSN);

         ResultSet rs = stmt.executeQuery();

         if (rs.next()) {
            profName = rs.getString("name");
         } else {
            System.out.println("No professor found with SSN: " + profSSN);
            return;
         }

      } catch (SQLException e) {
         e.printStackTrace();
      }
      
      System.out.println("Projects for Professor " + profName + ":");

      // Queries for Co-Investigator and Principal Investigator projects
      String coQuery = 
         "SELECT P.projectno, spons_name, start_date, end_date, budget, principal_investigator\n" +
         "FROM co_investigators C INNER JOIN projects P\n" +
         "ON C.projectno = P.projectno\n" +
         "WHERE C.ssn = ?;";

      String piQuery = 
         "SELECT *\n" +
         "FROM  projects\n" +
         "WHERE principal_investigator = ?;";

      // Execute Co-Investigator query
      try (PreparedStatement stmt = conn.prepareStatement(coQuery);) {
         stmt.setString(1, profSSN);

         ResultSet rs=stmt.executeQuery();

         // Print all projects that the professor is a Co-Investigator in.
         if(!rs.next()){
            System.out.println();
            System.out.println("\tNo projects found where the professor is a Co-Investigator.");
         } else{
            System.out.println("\nProjects where Professor " + profName + " is a Co-Investigator:");
            do {
               System.out.println("ID: " + rs.getInt("projectno"));
               System.out.println("\tSponsor: " + rs.getString("spons_name"));
               System.out.println("\tStart/End Date: " + rs.getDate("start_date") + " to " + rs.getDate("end_date"));
               System.out.println("\tBudget: " + rs.getDouble("budget"));
               String PI = rs.getString("principal_investigator");
               if (PI != null) {
                  System.out.println("\tPI SSN: " + PI);
               }
            } while (rs.next());
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }

      // Execute Principal Investigator query
      try (PreparedStatement stmt = conn.prepareStatement(piQuery);) {
         stmt.setString(1, profSSN);

         ResultSet rs=stmt.executeQuery();

         // Print all projects that the professor is the principal investigator.
         if(!rs.next()){
            System.out.println();
            System.out.println("\tNo projects found where the professor is the Principal Investigator.");
         } else {
            System.out.println("\nProjects where Professor " + profName + " is the Principal Investigator:");
            do {
               System.out.println("ID: " + rs.getInt("projectno"));
               System.out.println("\tSponsor: " + rs.getString("spons_name"));
               System.out.println("\tStart/End Date: " + rs.getDate("start_date") + " to " + rs.getDate("end_date"));
               System.out.println("\tBudget: " + rs.getDouble("budget"));
               String PI = rs.getString("principal_investigator");
               if (PI != null) {
                  System.out.println("\tPI SSN: " + PI);
               }
            } while (rs.next());
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }


   /*
      Helper Functions
   */

   // Grabs next integer
   public static Integer nextInt(String prompt) {
      Integer input = null;
      
      while (input == null) {
         try {
            System.out.print(prompt);
            input = sc.nextInt();
            sc.nextLine();
            break;
         } catch (Exception e) {
            System.out.println("Enter a valid integer!");
            sc.nextLine();
         }
      }
      return input;
   }

   // Grabs next double value
   public static Double nextFloat(String prompt) {
      Double input = null;
      
      while (input == null) {
         try {
            System.out.print(prompt);
            input = sc.nextDouble();
            sc.nextLine();
            break;
         } catch (Exception e) {
            System.out.println("Enter a valid decimal!");
            sc.nextLine();
         }
      }
      return input;
   }

   // Returns string of length [0, limit] if limit > 0. If "exact" is set to true, then it must be equal to the given limit
   // Meant to serve as a soft limit to string lengths; this doesn't necessarily mean entries MUST be of length ==/<= limit
   // Nullable returns null for length 0 strings
   public static String nextString(String prompt, int limit, boolean isExact, boolean nullable) {
      String input = null;
      

      
      while (input == null) {
         try {
            System.out.print(prompt);
            input = sc.nextLine();
            if (nullable && input.isEmpty()) {
               return null;
            }
            if (limit > 0 && input.length() > limit || isExact && input.length() != limit) {
               throw new Exception();
            }
            break;
         } catch (Exception e) {
            if (isExact) {
               System.out.println("Enter a valid string! (Length == " + limit + ")" );
            } else {
               System.out.println("Enter a valid string! (Length <= " + limit + ")" );
            }
            sc.nextLine();
         }
      }
      return input;
   }

   // Grabs next Date, in the format yyyy-MM-dd
   // Important discrepancy: Date is a java.util.Date object, not java.sql.Date
   // To convert from java.util.Date to java.sql.Date, use "new java.sql.Date(utilDateObj.getTime())"
   public static java.util.Date nextDate(String prompt) {
      java.util.Date date = null;
      
      while (date == null) {
         try {
            System.out.print(prompt);
            String dateString = sc.next();
            date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
            sc.nextLine();
            break;
         } catch (Exception e) {
            System.out.println("Enter a valid date (yyyy-MM-dd)!");
            sc.nextLine();
         }
      }
      return date;
   }

   // Clamps value between [min, max]
   public static double clamp(double x, double min, double max) {
      return Math.max(Math.min(x, max), min);
   }
}