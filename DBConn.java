import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DBConn {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3308/temp","root","root");
		Statement st=con.createStatement();
		String str="select * from student where id=2";
		String str2="INSERT INTO student VALUES (5,'patil')";
		//int rs=st.executeUpdate(str2);
		//System.out.println("number of rows affected "+rs);
		ResultSet rs=st.executeQuery(str);
		System.out.println(rs);
		while(rs.next())
		{
			System.out.println("Student id "+rs.getInt(1)+" Student name "+rs.getString(2));
		}
	}

}

