import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Formatter;

public class TestSQLite
{
	public static void main(String args[])
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
			File fichierWAV = new File("sons/test.wav");
			System.out.println("[i] " + fichierWAV.getCanonicalPath());
			System.out.println("[i] Taille du fichier : " + fichierWAV.length() / 1024.0 / 1024.0 + " Mio");
			if(fichierWAV.exists())
			{
				FileInputStream fis = new FileInputStream(fichierWAV);

				Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
				conn.setAutoCommit(false);
				conn.setReadOnly(false);
				Statement stat = conn.createStatement();
				stat.executeUpdate("DROP TABLE if exists test;");
				stat.executeUpdate("CREATE TABLE test (id  INTEGER PRIMARY KEY AUTOINCREMENT, file_path VARCHAR2(4096) NOT NULL, file BLOB, timestamp DATE NOT NULL);");
				long start = System.currentTimeMillis();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				while(fis.read(buf) != -1)
				{
					bos.write(buf);
				}

				byte[] tableauOctetsFichier = bos.toByteArray();
				PreparedStatement ps = conn
						.prepareStatement("INSERT INTO test (file_path,file,timestamp) VALUES (?,?,datetime('now', 'unixepoch', 'localtime'))");
				ps.setString(1, fichierWAV.getCanonicalPath());
				ps.setBytes(2, tableauOctetsFichier);
				int result = ps.executeUpdate();
				if(result > 0)
				{
					conn.commit();
					System.out.println("[i] Le fichier a bien été inséré dans la base de données !");
					System.out.println("[i] Temps nécessaire pour insérer le fichier : "+(System.currentTimeMillis()-start)/1000.0+"s");
				}
				else
				{
					System.out.println("[E] Il y a eu une erreur, impossible d'insérer le fichier dans la base de données.");
				}
				Thread.sleep(2000);
				start = System.currentTimeMillis();
				ResultSet rs = stat.executeQuery("SELECT file FROM test WHERE id = 1;");
				if(rs.next())
				{
					byte tab[] = rs.getBytes("file");
					System.out.println("[i] Temps nécessaire pour récupérer le fichier : "+(System.currentTimeMillis()-start)/1000.0+"s");
					System.out.println("[i] SHA-1 fichier original : "+sha1(tableauOctetsFichier));
					System.out.println("[i] SHA-1 fichier de la BDD : "+sha1(tab));
					FileOutputStream dataOut = new FileOutputStream("sons/test_sortie.wav");
					dataOut.write(tab);
					dataOut.close();
				}
				else
				{
					System.out.println("[E] Il y a eu une erreur, impossible de récupérer le fichier.");
				}
				rs.close();
				fis.close();
				ps.close();
				conn.close();
			}
			else
			{
				System.out.println("[i] " + fichierWAV.getCanonicalPath() + " n'existe pas !");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static String sha1(byte[] convertme) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		return byteArray2Hex(md.digest(convertme));
	}

	private static String byteArray2Hex(final byte[] hash)
	{
		Formatter formatter = new Formatter();
		for (byte b : hash)
		{
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}
}
