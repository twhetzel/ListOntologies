
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class ListLatestOntologies {

	static final String REST_URL = "http://data.bioontology.org";
	static final String API_KEY = "YourAPIKey"; 
	static final ObjectMapper mapper = new ObjectMapper();


	public static void main(String[] args) throws JsonParseException, IOException {
		// Get the available resources
		String resourcesString = get(REST_URL + "/");
		JsonNode resources = jsonToNode(resourcesString);

		// Follow the ontologies link by looking for the media type in the list of links
		String link = resources.get("links").findValue("ontologies").asText();
		//System.out.println("Link: "+link);

		// Get the ontologies from the link we found
		JsonNode ontologies = jsonToNode(get(link));
		//System.out.println("JSON Ontologies: "+ontologies);

		// Get the name and ontology id from the returned list AND link for latest_submission
		List<String> ontNames = new ArrayList<String>();
		List<String> latestOntSubmission = new ArrayList<String>();
		for (JsonNode ontology : ontologies) {
			ontNames.add(ontology.get("name").asText() + "\n" + ontology.get("@id").asText()+"\n\n");
			//System.out.println("JsonNode ontology: \n"+ontology+"\n");

			// Follow the latest_submission link by looking for the media type in the list of links
			String latest = ontology.get("links").findValue("latest_submission").asText();          
			latestOntSubmission.add(latest);
			// System.out.println("Latest Ont Submission: "+latest);
		}

		// Print the ontology names and ids
		//  for (String ontName : ontNames) {
		//      System.out.println(ontName);
		//  }        


		// Get metadata for the latest submission for each ontology
		// Documentation for parsing Json with FasterXML: http://wiki.fasterxml.com/JacksonTreeModel
		List<String> latestOntMetadata = new ArrayList<String>();
		for (String sub : latestOntSubmission) {
			//System.out.println("Latest URL: "+sub);
			JsonNode latestOntology = jsonToNode(get(sub));
			System.out.print("...");
			//System.out.println("JsonNode latestOntology: \n"+latestOntology+"\n\n"); //prints the entire Json Node
			//System.out.println(latestOntology.path("contact").path(0).path("name").textValue()+"\n"); //THIS WORKS!!!       			
			//System.out.println(latestOntology.path("ontology").path("acronym").textValue()+"\n");  //THIS WORKS!!!
			//System.out.println(latestOntology.path("hasOntologyLanguage").textValue()+"\n");  //THIS WORKS!!!

			latestOntMetadata.add(latestOntology.path("contact").path(0).path("name").textValue()+"\t");
			latestOntMetadata.add(latestOntology.path("contact").path(0).path("email").textValue()+"\t");
			latestOntMetadata.add(latestOntology.path("ontology").path("acronym").textValue()+"\t");
			latestOntMetadata.add(latestOntology.path("ontology").path("name").textValue()+"\t");
			latestOntMetadata.add(latestOntology.path("hasOntologyLanguage").textValue()+"\t");
			latestOntMetadata.add(latestOntology.path("homepage").textValue()+"\t");
			latestOntMetadata.add(latestOntology.path("version").textValue()+"\t");
			
			//Clean-up text in description, ie remove newline chars
			String str = latestOntology.path("description").textValue();
			//System.out.println(str);
			if (str != null) {
				String new_str = str.replaceAll("<br>|</br>|\n|\r", " ");
				//System.out.println("FORMATTED DESC: "+new_str+"\n");
		
				latestOntMetadata.add(new_str+"\n");
			}
			else {
				latestOntMetadata.add("\n");
			}
		}
		System.out.println();  


		// Prepare to print data file
		try {
			File file = new File("/Users/whetzel/Documents/workspace/BioPortal-ListOntologiesv2/bioportal-ontology-metadata_04182014.txt");
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			//bw.write(content);
			// Print ontology metadata
			for (String l : latestOntMetadata) {
				bw.write(l);
				System.out.print(l);
			}
			bw.close();
			System.out.println("Done");     
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}



	private static JsonNode jsonToNode(String json) {
		JsonNode root = null;
		try {
			root = mapper.readTree(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return root;
	}

	private static String get(String urlToGet) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			url = new URL(urlToGet);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
			conn.setRequestProperty("Accept", "application/json");
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
