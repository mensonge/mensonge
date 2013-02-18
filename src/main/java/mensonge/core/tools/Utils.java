package mensonge.core.tools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Classe utilitaire générale
 * 
 */
public final class Utils
{
	private Utils()
	{
		// Permet d'empêcher l'instanciation de cette classe Utilitaire
	}

	/**
	 * Convertie un nombre d'octets en chaine plus facilement compréhensible avec des préfixes (kio, Mio,...)
	 * 
	 * @param bytes
	 *            Nombre d'octets
	 * @param si
	 *            Si le format devra être au format SI (Système international) ou non
	 * @return Le ,pùbre d'octets en chaine en format plus facilement compréhensible
	 */
	public static String humanReadableByteCount(long bytes, boolean si)
	{
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
		{
			return bytes + " o";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %so", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Récupère la taille de la base de données
	 * 
	 * @return Taille de la base de données
	 */
	public static long getDBSize()
	{
		File dbFile = new File("LieLab.db");
		if (dbFile.exists() && dbFile.isFile())
		{
			return dbFile.length();
		}
		return 0;
	}

	/**
	 * Formatte des secondes en heures:minutes:secondes
	 * 
	 * @param time
	 *            secondes à formatter
	 * @return La chaine contenant les secondes formattées
	 */
	public static String getFormattedTimeS(long time)
	{
		int heures = (int) (time / 3600);
		int minutes = (int) ((time % 3600) / 60);
		int secondes = (int) ((time % 3600) % 60);
		return String.format("%02d:%02d:%02d", heures, minutes, secondes);
	}

	/**
	 * Formatte des millisecondes en heures:minutes:secondes.millisecondes
	 * 
	 * @param time
	 *            Millisecondes à formatter
	 * @return La chaine contenant les millisecondes formattées
	 */
	public static String getFormattedTimeMs(long time)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(time);
	}
}
