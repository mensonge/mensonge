package mensonge.core;

/**
 * 
 * Interface utilisée pour les plugins pour qu'ils aient connaissance des méthodes d'extraction
 *
 */
public interface IExtraction
{
	/**
	 * Extrait les échantillons audio d'un fichier multimédia
	 * 
	 * @param fichier
	 *            Fichier multimédia où extraire les échantillons du premier flux audio trouvé
	 * @return Un tableau de double contenant les échantillons
	 */
	double[][] extraireEchantillons(String filePath);

	/**
	 * Extrait le flux audio d'un fichier multimédia et le converti en WAV, format PCM Signé 16 bit little endian
	 * @param fichier Fichier multimédia où extraire l'intervalle défini du premier flux audio trouvé
	 * @param debut La borne de début de l'intervalle en millisecondes où commencer l'extraction 
	 * @param fin La borne de fin de l'intervalle en millisecondes où terminer l'extraction
	 * @return Un tableau d'octet contenant le fichier WAV
	 */
	byte[] extraireIntervalle(String filePath, long debut, long fin);
}
