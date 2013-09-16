package britannica;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class WikiUtils {

	private static int counter = 0;
	
	// Query wikipedia (search for searchTerm in both title and content of articles)
	public static String wikiQuery(String searchTerm, String returnFormat, String searchLimit, int sroffset, String queryType)
	{
		if (queryType.equalsIgnoreCase("generator")) counter++;
		if (counter >= 10)
		{
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			counter = 0;
		}
		
		String queryString = "";
		if (queryType.equalsIgnoreCase("simple"))
			queryString = String.format("http://en.wikipedia.org/w/api.php?format=%s&action=query&list=search&srsearch=%s&srlimit=%s", returnFormat, searchTerm, searchLimit);
		else if (queryType.equalsIgnoreCase("generator"))
			queryString = String.format("http://en.wikipedia.org/w/api.php?format=%s&action=query&indexpageids&generator=search&gsrsearch=%s&gsrlimit=%s&gsroffset=%s&prop=info|revisions&inprop=url&rvprop=content&rvcontentformat=text/x-wiki", returnFormat, searchTerm, searchLimit, String.valueOf(sroffset));
		
		String queryResult = "";
		try {
			URL wiki = new URL(queryString);
			URLConnection conn = wiki.openConnection();
//			conn.setConnectTimeout(0); // ToDo
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			queryResult = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return queryResult;
	}
	
	// Fill empty space with %20 in the query string
	public static String fillSpace(String queryString)
	{
		String modifiedQueryString = "";
		for (int i = 0; i < queryString.length(); i++)
		{
			if (Character.isSpaceChar(queryString.charAt(i)))
				modifiedQueryString = modifiedQueryString + "%20";
			else
				modifiedQueryString = modifiedQueryString + queryString.charAt(i);
			
		}
		return modifiedQueryString;
	}
	
}
